/////////////////////////////////////////////////////////////////////////////
//
// Project ProjectForge Community Edition
//         www.projectforge.org
//
// Copyright (C) 2001-2012 Kai Reinhard (k.reinhard@micromata.com)
//
// ProjectForge is dual-licensed.
//
// This community edition is free software; you can redistribute it and/or
// modify it under the terms of the GNU General Public License as published
// by the Free Software Foundation; version 3 of the License.
//
// This community edition is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
// Public License for more details.
//
// You should have received a copy of the GNU General Public License along
// with this program; if not, see http://www.gnu.org/licenses/.
//
/////////////////////////////////////////////////////////////////////////////

package org.projectforge.web.fibu;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.form.validation.IFormValidator;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.util.convert.IConverter;
import org.projectforge.calendar.DayHolder;
import org.projectforge.common.NumberHelper;
import org.projectforge.core.Configuration;
import org.projectforge.core.ConfigurationParam;
import org.projectforge.core.CurrencyFormatter;
import org.projectforge.fibu.AbstractRechnungDO;
import org.projectforge.fibu.AbstractRechnungsPositionDO;
import org.projectforge.fibu.AuftragsPositionDO;
import org.projectforge.fibu.RechnungsPositionDO;
import org.projectforge.fibu.kost.KostZuweisungDO;
import org.projectforge.fibu.kost.KostZuweisungenCopyHelper;
import org.projectforge.web.wicket.AbstractEditForm;
import org.projectforge.web.wicket.AbstractEditPage;
import org.projectforge.web.wicket.components.DatePanel;
import org.projectforge.web.wicket.components.DatePanelSettings;
import org.projectforge.web.wicket.components.LabelValueChoiceRenderer;
import org.projectforge.web.wicket.components.MaxLengthTextArea;
import org.projectforge.web.wicket.components.MinMaxNumberField;
import org.projectforge.web.wicket.components.SingleButtonPanel;
import org.projectforge.web.wicket.converter.BigDecimalPercentConverter;
import org.projectforge.web.wicket.converter.CurrencyConverter;
import org.projectforge.web.wicket.flowlayout.ButtonPanel;
import org.projectforge.web.wicket.flowlayout.ButtonType;
import org.projectforge.web.wicket.flowlayout.DialogPanel;
import org.projectforge.web.wicket.flowlayout.DivPanel;
import org.projectforge.web.wicket.flowlayout.DivTextPanel;
import org.projectforge.web.wicket.flowlayout.DivType;
import org.projectforge.web.wicket.flowlayout.FieldsetPanel;
import org.projectforge.web.wicket.flowlayout.HtmlCodePanel;
import org.projectforge.web.wicket.flowlayout.IconLinkPanel;
import org.projectforge.web.wicket.flowlayout.IconType;
import org.projectforge.web.wicket.flowlayout.InputPanel;
import org.projectforge.web.wicket.flowlayout.TextAreaPanel;
import org.projectforge.web.wicket.flowlayout.TextPanel;
import org.projectforge.web.wicket.flowlayout.TextStyle;
import org.projectforge.web.wicket.flowlayout.ToggleContainerPanel;

public abstract class AbstractRechnungEditForm<O extends AbstractRechnungDO<T>, T extends AbstractRechnungsPositionDO, P extends AbstractEditPage< ? , ? , ? >>
extends AbstractEditForm<O, P>
{
  private static final long serialVersionUID = 9073611406229693582L;

  public static final int[] ZAHLUNGSZIELE_IN_TAGEN = { 7, 14, 30, 60, 90};

  private static final Component[] COMPONENT_ARRAY = new Component[0];

  private static final String COST_EDIT_DIALOG_ID = "editCostDialog";

  protected RepeatingView positionsRepeater;

  protected SingleButtonPanel cloneButtonPanel;

  private boolean costConfigured;

  private ModalWindow costEditModalWindow;

  private final List<Component> ajaxUpdateComponents = new ArrayList<Component>();

  private Component[] ajaxUpdateComponentsArray;

  protected final FormComponent< ? >[] dependentFormComponents = new FormComponent[5];

  protected DatePanel datumPanel, faelligkeitPanel;

  protected Integer zahlungsZiel;

  public AbstractRechnungEditForm(final P parentPage, final O data)
  {
    super(parentPage, data);
  }

  protected abstract void onInit();

  @SuppressWarnings("unchecked")
  protected void validation()
  {
    final TextField<Date> datumField = (TextField<Date>) dependentFormComponents[0];
    final TextField<Date> bezahlDatumField = (TextField<Date>) dependentFormComponents[1];
    final TextField<Date> faelligkeitField = (TextField<Date>) dependentFormComponents[2];
    final TextField<BigDecimal> zahlBetragField = (TextField<BigDecimal>) dependentFormComponents[3];
    final DropDownChoice<Integer> zahlungsZielChoice = (DropDownChoice<Integer>) dependentFormComponents[4];

    final Date bezahlDatum = bezahlDatumField.getConvertedInput();

    final Integer zahlungsZiel = zahlungsZielChoice.getConvertedInput();
    Date faelligkeit = faelligkeitField.getConvertedInput();
    if (faelligkeit == null && zahlungsZiel != null) {
      Date date = datumField.getConvertedInput();
      if (date == null) {
        date = getData().getDatum();
      }
      if (date != null) {
        final DayHolder day = new DayHolder(date);
        day.add(Calendar.DAY_OF_YEAR, zahlungsZiel);
        faelligkeit = day.getDate();
        getData().setFaelligkeit(day.getSQLDate());
        faelligkeitPanel.markModelAsChanged();
      }
    }
    getData().recalculate();

    final BigDecimal zahlBetrag = zahlBetragField.getConvertedInput();
    final boolean zahlBetragExists = (zahlBetrag != null && zahlBetrag.compareTo(BigDecimal.ZERO) != 0);
    if (bezahlDatum != null && zahlBetragExists == false) {
      addError("fibu.rechnung.error.bezahlDatumUndZahlbetragRequired");
    }
    if (faelligkeit == null) {
      addFieldRequiredError("fibu.rechnung.faelligkeit");
    }
  }

  @SuppressWarnings("serial")
  @Override
  protected void init()
  {
    super.init();
    add(new IFormValidator() {
      @Override
      public FormComponent< ? >[] getDependentFormComponents()
      {
        return dependentFormComponents;
      }

      @Override
      public void validate(final Form< ? > form)
      {
        validation();
      }
    });
    if (Configuration.getInstance().isCostConfigured() == true) {
      costConfigured = true;
    }

    if (isNew() == false && getData().isDeleted() == false && getBaseDao().hasInsertAccess(getUser()) == true) {
      // Clone button for existing and not deleted invoices:
      final Button cloneButton = new Button(SingleButtonPanel.WICKET_ID, new Model<String>("clone")) {
        @Override
        public final void onSubmit()
        {
          cloneRechnung();
        }
      };
      cloneButtonPanel = new SingleButtonPanel(actionButtons.newChildId(), cloneButton, getString("clone"));
      actionButtons.add(2, cloneButtonPanel);
    }

    onInit();

    /* GRID8 - BLOCK */
    gridBuilder.newGrid8();
    gridBuilder.newColumnsPanel().newColumnPanel(DivType.COL_50);
    {
      // Date
      final FieldsetPanel fs = gridBuilder.newFieldset(getString("fibu.rechnung.datum"));
      datumPanel = new DatePanel(fs.newChildId(), new PropertyModel<Date>(data, "datum"), DatePanelSettings.get().withTargetType(
          java.sql.Date.class));
      dependentFormComponents[0] = datumPanel.getDateField();
      datumPanel.setRequired(true);
      fs.add(datumPanel);
    }
    gridBuilder.newColumnPanel(DivType.COL_50);
    {
      // Net sum
      final FieldsetPanel fs = gridBuilder.newFieldset(getString("fibu.common.netto"));
      final DivTextPanel netPanel = new DivTextPanel(fs.newChildId(), new Model<String>() {
        @Override
        public String getObject()
        {
          return CurrencyFormatter.format(data.getNetSum());
        }
      }, TextStyle.FORM_TEXT);
      fs.add(netPanel);
      fs.setNoLabelFor();
      ajaxUpdateComponents.add(netPanel.getLabel4Ajax());
    }
    gridBuilder.newColumnsPanel().newColumnPanel(DivType.COL_50);
    {
      // Vat amount
      final FieldsetPanel fs = gridBuilder.newFieldset(getString("fibu.common.vatAmount"));
      final DivTextPanel vatPanel = new DivTextPanel(fs.newChildId(), new Model<String>() {
        @Override
        public String getObject()
        {
          return CurrencyFormatter.format(data.getVatAmountSum());
        }
      }, TextStyle.FORM_TEXT);
      fs.add(vatPanel);
      fs.setNoLabelFor();
      ajaxUpdateComponents.add(vatPanel.getLabel4Ajax());
    }
    gridBuilder.newColumnPanel(DivType.COL_50);
    {
      // Brutto
      final FieldsetPanel fs = gridBuilder.newFieldset(getString("fibu.common.brutto"));
      final DivTextPanel grossPanel = new DivTextPanel(fs.newChildId(), new Model<String>() {
        @Override
        public String getObject()
        {
          return CurrencyFormatter.format(data.getGrossSum());
        }
      }, TextStyle.FORM_TEXT);
      fs.add(grossPanel);
      fs.setNoLabelFor();
      ajaxUpdateComponents.add(grossPanel.getLabel4Ajax());
    }
    {
      gridBuilder.newColumnsPanel().newColumnPanel(DivType.COL_50);
      // Fälligkeit und Zahlungsziel
      final FieldsetPanel fs = gridBuilder.newFieldset(getString("fibu.rechnung.faelligkeit"), true);
      faelligkeitPanel = new DatePanel(fs.newChildId(), new PropertyModel<Date>(data, "faelligkeit"), DatePanelSettings.get()
          .withTargetType(java.sql.Date.class));
      dependentFormComponents[2] = faelligkeitPanel.getDateField();
      fs.add(faelligkeitPanel);
      fs.setLabelFor(faelligkeitPanel);
      addCellAfterFaelligkeit();

      // DropDownChoice ZahlungsZiel
      final LabelValueChoiceRenderer<Integer> zielChoiceRenderer = new LabelValueChoiceRenderer<Integer>();
      for (final int days : ZAHLUNGSZIELE_IN_TAGEN) {
        zielChoiceRenderer.addValue(days, String.valueOf(days) + " " + getString("days"));
      }
      final DropDownChoice<Integer> zahlungsZielChoice = new DropDownChoice<Integer>(fs.getDropDownChoiceId(), new PropertyModel<Integer>(
          this, "zahlungsZiel"), zielChoiceRenderer.getValues(), zielChoiceRenderer) {
        @Override
        public boolean isVisible()
        {
          return data.getFaelligkeit() == null;
        }
      };
      dependentFormComponents[4] = zahlungsZielChoice;
      zahlungsZielChoice.setNullValid(true);
      zahlungsZielChoice.setRequired(false);

      fs.add(zahlungsZielChoice);
      fs.add(new DivTextPanel(fs.newChildId(), new Model<String>() {
        @Override
        public String getObject()
        {
          data.recalculate();
          return data.getZahlungsZielInTagen() + " " + getString("days");
        }
      }) {
        @Override
        public boolean isVisible()
        {
          return data.getFaelligkeit() != null;
        }
      });
    }
    gridBuilder.newColumnsPanel().newColumnPanel(DivType.COL_50);
    {
      // Bezahldatum
      final FieldsetPanel fs = gridBuilder.newFieldset(getString("fibu.rechnung.bezahlDatum"));
      final DatePanel bezahlDatumPanel = new DatePanel(fs.newChildId(), new PropertyModel<Date>(data, "bezahlDatum"), DatePanelSettings
          .get().withTargetType(java.sql.Date.class));
      dependentFormComponents[1] = bezahlDatumPanel.getDateField();
      fs.add(bezahlDatumPanel);
    }
    gridBuilder.newColumnPanel(DivType.COL_50);
    {
      // Zahlbetrag
      final FieldsetPanel fs = gridBuilder.newFieldset(getString("fibu.rechnung.zahlBetrag"));
      final TextField<BigDecimal> zahlBetragField = new TextField<BigDecimal>(InputPanel.WICKET_ID, new PropertyModel<BigDecimal>(data,
          "zahlBetrag")) {
        @SuppressWarnings({ "rawtypes", "unchecked"})
        @Override
        public IConverter getConverter(final Class type)
        {
          return new CurrencyConverter();
        }
      };
      dependentFormComponents[3] = zahlBetragField;
      fs.add(zahlBetragField);
    }
    /* GRID16 - BLOCK */
    gridBuilder.newGrid16();
    gridBuilder.newColumnsPanel().newColumnPanel(DivType.COL_50);
    {
      // Bemerkung
      final FieldsetPanel fs = gridBuilder.newFieldset(getString("comment"));
      fs.add(new MaxLengthTextArea(TextAreaPanel.WICKET_ID, new PropertyModel<String>(data, "bemerkung"))).setAutogrow();
    }
    gridBuilder.newColumnPanel(DivType.COL_50);
    {
      // Besonderheiten
      final FieldsetPanel fs = gridBuilder.newFieldset(getString("fibu.rechnung.besonderheiten"));
      fs.add(new MaxLengthTextArea(TextAreaPanel.WICKET_ID, new PropertyModel<String>(data, "besonderheiten"))).setAutogrow();
    }
    flowform.add(positionsRepeater = new RepeatingView(flowform.newChildId()));
    if (costConfigured == true) {
      costEditModalWindow = new ModalWindow(COST_EDIT_DIALOG_ID);
      add(costEditModalWindow);
    } else {
      add(new WebMarkupContainer(COST_EDIT_DIALOG_ID).setVisible(false));
    }
    refresh();
  }

  protected void addCellAfterFaelligkeit()
  {
    // Do nothing.
  }

  protected abstract T newPositionInstance();

  @SuppressWarnings("serial")
  void refresh()
  {
    positionsRepeater.removeAll();
    final boolean hasInsertAccess = getBaseDao().hasInsertAccess(getUser());
    if (CollectionUtils.isEmpty(data.getPositionen()) == true) {
      // Ensure that at least one position is available:
      final T position = newPositionInstance();
      position.setVat(Configuration.getInstance().getPercentValue(ConfigurationParam.FIBU_DEFAULT_VAT));
      data.addPosition(position);
    }
    DivPanel content = null, columns, column, subcolumn;
    for (final T position : data.getPositionen()) {
      // Fetch all kostZuweisungen:
      if (CollectionUtils.isNotEmpty(position.getKostZuweisungen()) == true) {
        for (final KostZuweisungDO zuweisung : position.getKostZuweisungen()) {
          zuweisung.getNetto(); // Fetch
        }
      }
      final List<Component> ajaxUpdatePositionComponents = new ArrayList<Component>();
      final RechnungsPositionDO rechnungsPosition = (position instanceof RechnungsPositionDO) ? (RechnungsPositionDO) position : null;
      final ToggleContainerPanel positionsPanel = new ToggleContainerPanel(positionsRepeater.newChildId(), DivType.GRID16,
          DivType.ROUND_ALL) {
        /**
         * @see org.projectforge.web.wicket.flowlayout.ToggleContainerPanel#wantsOnStatusChangedNotification()
         */
        @Override
        protected boolean wantsOnStatusChangedNotification()
        {
          return true;
        }
        /**
         * @see org.projectforge.web.wicket.flowlayout.ToggleContainerPanel#onToggleStatusChanged(org.apache.wicket.ajax.AjaxRequestTarget, boolean)
         */
        @Override
        protected void onToggleStatusChanged(final AjaxRequestTarget target, final boolean toggleClosed)
        {
          // TODO Kai from Johannes: handle persistence of open/closed events
          if(toggleClosed) {
            System.out.println("i am now closed");
          } else {
            System.out.println("i am now open");
          }
        }
      };
      positionsPanel.getContainer().setOutputMarkupId(true);
      positionsRepeater.add(positionsPanel);
      final StringBuffer heading = new StringBuffer();
      heading.append(escapeHtml(getString("fibu.auftrag.position.short"))).append(" #").append(position.getNumber());
      positionsPanel.setHeading(new HtmlCodePanel(ToggleContainerPanel.HEADING_ID, heading.toString()));
      content = new DivPanel(ToggleContainerPanel.CONTENT_ID);
      positionsPanel.add(content);
      content.add(columns = new DivPanel(content.newChildId(), DivType.BLOCK));
      final DivType divType = (rechnungsPosition != null) ? DivType.COL_25 : DivType.COL_33;
      {
        columns.add(column = new DivPanel(columns.newChildId(), DivType.COL_50));
        if (rechnungsPosition != null) {
          // Order
          column.add(subcolumn = new DivPanel(column.newChildId(), DivType.COL_25));
          final FieldsetPanel fieldset = new FieldsetPanel(subcolumn, getString("fibu.auftrag"), true).setLabelSide(false);
          fieldset.add(new InputPanel(fieldset.newChildId(), new AuftragsPositionFormComponent(InputPanel.WICKET_ID,
              new PropertyModel<AuftragsPositionDO>(position, "auftragsPosition"), false)));
          fieldset.add(new IconLinkPanel(fieldset.newChildId(), IconType.CIRCLE_ARROW_EAST, getString("show"), new Link<Void>(
              IconLinkPanel.LINK_ID) {
            /**
             * @see org.apache.wicket.markup.html.link.Link#onClick()
             */
            @Override
            public void onClick()
            {
              if (rechnungsPosition.getAuftragsPosition() != null) {
                final PageParameters parameters = new PageParameters();
                parameters.add(AbstractEditPage.PARAMETER_KEY_ID, rechnungsPosition.getAuftragsPosition().getAuftrag().getId());
                final AuftragEditPage auftragEditPage = new AuftragEditPage(parameters);
                auftragEditPage.setReturnToPage(getParentPage());
                setResponsePage(auftragEditPage);
              }
            }

            @Override
            public boolean isVisible()
            {
              return rechnungsPosition.getAuftragsPosition() != null;
            }
          }).setTopRight());
        }
        {
          // Menge
          column.add(subcolumn = new DivPanel(column.newChildId(), divType));
          final FieldsetPanel fieldset = new FieldsetPanel(subcolumn, getString("fibu.rechnung.menge")).setLabelSide(false);
          final TextField<BigDecimal> amountTextField = new MinMaxNumberField<BigDecimal>(InputPanel.WICKET_ID,
              new PropertyModel<BigDecimal>(position, "menge"), BigDecimal.ZERO, NumberHelper.BILLION);
          amountTextField.add(new AjaxFormComponentUpdatingBehavior("onblur") {
            @Override
            protected void onUpdate(final AjaxRequestTarget target)
            {
              addAjaxComponents(target, ajaxUpdatePositionComponents);
            }
          });
          fieldset.add(amountTextField);
        }
        {
          // Net price
          column.add(subcolumn = new DivPanel(column.newChildId(), divType));
          final FieldsetPanel fieldset = new FieldsetPanel(subcolumn, getString("fibu.rechnung.position.einzelNetto")).setLabelSide(false);
          final TextField<BigDecimal> netTextField = new TextField<BigDecimal>(InputPanel.WICKET_ID, new PropertyModel<BigDecimal>(
              position, "einzelNetto")) {
            @SuppressWarnings({ "rawtypes", "unchecked"})
            @Override
            public IConverter getConverter(final Class type)
            {
              return new CurrencyConverter();
            }
          };
          netTextField.add(new AjaxFormComponentUpdatingBehavior("onblur") {
            @Override
            protected void onUpdate(final AjaxRequestTarget target)
            {
              addAjaxComponents(target, ajaxUpdatePositionComponents);
            }
          });
          fieldset.add(netTextField);
        }
        {
          // VAT
          column.add(subcolumn = new DivPanel(column.newChildId(), divType));
          final FieldsetPanel fieldset = new FieldsetPanel(subcolumn, getString("fibu.rechnung.mehrwertSteuerSatz")).setLabelSide(false);
          final TextField<BigDecimal> vatTextField = new MinMaxNumberField<BigDecimal>(InputPanel.WICKET_ID, new PropertyModel<BigDecimal>(
              position, "vat"), BigDecimal.ZERO, NumberHelper.HUNDRED) {
            @SuppressWarnings({ "rawtypes", "unchecked"})
            @Override
            public IConverter getConverter(final Class type)
            {
              return new BigDecimalPercentConverter(true);
            }
          };
          vatTextField.add(new AjaxFormComponentUpdatingBehavior("onblur") {
            @Override
            protected void onUpdate(final AjaxRequestTarget target)
            {
              addAjaxComponents(target, ajaxUpdatePositionComponents);
            }
          });
          fieldset.add(vatTextField);
        }
      }
      {
        columns.add(column = new DivPanel(columns.newChildId(), DivType.COL_50));
        column.add(subcolumn = new DivPanel(column.newChildId(), DivType.COL_33));
        {
          final FieldsetPanel fieldset = new FieldsetPanel(subcolumn, getString("fibu.common.netto")).setLabelSide(false).setNoLabelFor();
          final TextPanel netTextPanel = new TextPanel(fieldset.newChildId(), new Model<String>() {
            @Override
            public String getObject()
            {
              return CurrencyFormatter.format(position.getNetSum());
            };
          });
          ajaxUpdatePositionComponents.add(netTextPanel.getLabel4Ajax());
          fieldset.add(netTextPanel);
        }
      }
      {
        column.add(subcolumn = new DivPanel(column.newChildId(), DivType.COL_33));
        {
          final FieldsetPanel fieldset = new FieldsetPanel(subcolumn, getString("fibu.common.vatAmount")).setLabelSide(false)
              .setNoLabelFor();
          final TextPanel vatTextPanel = new TextPanel(fieldset.newChildId(), new Model<String>() {
            @Override
            public String getObject()
            {
              return CurrencyFormatter.format(position.getVatAmount());
            };
          });
          fieldset.add(vatTextPanel);
          ajaxUpdatePositionComponents.add(vatTextPanel.getLabel4Ajax());
        }
      }
      {
        column.add(subcolumn = new DivPanel(column.newChildId(), DivType.COL_33));
        {
          final FieldsetPanel fieldset = new FieldsetPanel(subcolumn, getString("fibu.common.brutto")).setLabelSide(false).setNoLabelFor();
          final TextPanel grossTextPanel = new TextPanel(fieldset.newChildId(), new Model<String>() {
            @Override
            public String getObject()
            {
              return CurrencyFormatter.format(position.getBruttoSum());
            };
          });
          fieldset.add(grossTextPanel);
          ajaxUpdatePositionComponents.add(grossTextPanel.getLabel4Ajax());
        }
      }
      content.add(columns = new DivPanel(content.newChildId(), DivType.BLOCK));
      {
        // Text
        if (costConfigured == true) {
          columns.add(column = new DivPanel(columns.newChildId(), DivType.COL_50));
        } else {
          columns.add(column = new DivPanel(columns.newChildId())); // Full width.
        }
        final FieldsetPanel fieldset = new FieldsetPanel(column, getString("fibu.rechnung.text"));
        fieldset.add(new MaxLengthTextArea(TextAreaPanel.WICKET_ID, new PropertyModel<String>(position, "text"))).setAutogrow();
      }

      if (costConfigured == true) {
        {
          // Cost assignments
          columns.add(column = new DivPanel(columns.newChildId(), DivType.COL_50));
          {
            column.add(subcolumn = new DivPanel(column.newChildId(), DivType.COL_50));
            final RechnungCostTablePanel costTable = new RechnungCostTablePanel(subcolumn.newChildId(), position);
            subcolumn.add(costTable);
            ajaxUpdatePositionComponents.add(costTable.refresh().getTable());

            column.add(subcolumn = new DivPanel(column.newChildId(), DivType.COL_50));
            final BigDecimal fehlbetrag = position.getKostZuweisungNetFehlbetrag();
            if (hasInsertAccess == true) {
              ButtonType buttonType;
              if (NumberHelper.isNotZero(fehlbetrag) == true) {
                buttonType = ButtonType.RED;
              } else {
                buttonType = ButtonType.LIGHT;
              }
              final AjaxButton editCostButton = new AjaxButton(ButtonPanel.BUTTON_ID, this) {
                @Override
                protected void onSubmit(final AjaxRequestTarget target, final Form< ? > form)
                {
                  showCostEditModalWindow(target, position, costTable);
                }

                @Override
                protected void onError(final AjaxRequestTarget target, final Form< ? > form)
                {
                  target.add(AbstractRechnungEditForm.this.feedbackPanel);
                }
              };
              editCostButton.setDefaultFormProcessing(false);
              subcolumn.add(new ButtonPanel(subcolumn.newChildId(), getString("edit"), editCostButton, buttonType));
            } else {
              subcolumn.add(new TextPanel(subcolumn.newChildId(), " "));
            }
            if (NumberHelper.isNotZero(fehlbetrag) == true) {
              subcolumn.add(new TextPanel(subcolumn.newChildId(), CurrencyFormatter.format(fehlbetrag), TextStyle.RED));
            }
          }
        }
      }
    }
    if (hasInsertAccess == true) {
      content.add(columns = new DivPanel(content.newChildId(), DivType.BLOCK));
      columns.add(column = new DivPanel(columns.newChildId())); // Full width.
      final Button addPositionButton = new Button(SingleButtonPanel.WICKET_ID) {
        @Override
        public final void onSubmit()
        {
          final T position = newPositionInstance();
          data.addPosition(position);
          if (position.getNumber() > 1) {
            final T predecessor = data.getPosition(position.getNumber() - 2);
            if (predecessor != null) {
              position.setVat(predecessor.getVat()); // Preset the vat from the predecessor position.
            }
          }
          refresh();
        }
      };
      final SingleButtonPanel addPositionButtonPanel = new SingleButtonPanel(column.newChildId(), addPositionButton, getString("add"));
      addPositionButtonPanel.setTooltip(getString("fibu.rechnung.tooltip.addPosition"));
      column.add(addPositionButtonPanel);
    }
  }

  /**
   * Overwrite this method if you need to add own form elements for a order position.
   * @param item
   * @param position
   */
  protected void onRenderPosition(final WebMarkupContainer item, final T position)
  {

  }

  protected void showCostEditModalWindow(final AjaxRequestTarget target, final AbstractRechnungsPositionDO position,
      final RechnungCostTablePanel costTable)
  {
    // Cost edit dialog
    final DialogPanel costEditDialog = new DialogPanel(costEditModalWindow, getString("fibu.rechnung.showEditableKostZuweisungen"));
    costEditModalWindow.setContent(costEditDialog);

    final DivPanel content = new DivPanel(costEditDialog.newChildId());
    costEditDialog.add(content);
    final RechnungCostEditTablePanel rechnungCostEditTablePanel = new RechnungCostEditTablePanel(content.newChildId());
    content.add(rechnungCostEditTablePanel);
    rechnungCostEditTablePanel.add(position);

    @SuppressWarnings("serial")
    final AjaxButton cancelButton = new AjaxButton(SingleButtonPanel.WICKET_ID, new Model<String>("cancel")) {

      @Override
      protected void onSubmit(final AjaxRequestTarget target, final Form< ? > form)
      {
        costEditModalWindow.close(target);
      }

      /**
       * @see org.apache.wicket.ajax.markup.html.form.AjaxButton#onError(org.apache.wicket.ajax.AjaxRequestTarget,
       *      org.apache.wicket.markup.html.form.Form)
       */
      @Override
      protected void onError(final AjaxRequestTarget target, final Form< ? > form)
      {
      }
    };
    cancelButton.setDefaultFormProcessing(false); // No validation
    final SingleButtonPanel cancelButtonPanel = new SingleButtonPanel(costEditDialog.newButtonChildId(), cancelButton, getString("cancel"),
        SingleButtonPanel.CANCEL);
    costEditDialog.addButton(cancelButtonPanel);

    final String label = (isNew() == true) ? "create" : "update";
    @SuppressWarnings("serial")
    final AjaxButton submitButton = new AjaxButton(SingleButtonPanel.WICKET_ID, new Model<String>(label)) {

      @Override
      protected void onSubmit(final AjaxRequestTarget target, final Form< ? > form)
      {
        // Copy edited values to DO object.
        final AbstractRechnungsPositionDO srcPosition = rechnungCostEditTablePanel.getPosition();
        final KostZuweisungenCopyHelper kostZuweisungCopyHelper = new KostZuweisungenCopyHelper();
        kostZuweisungCopyHelper.mycopy(srcPosition.getKostZuweisungen(), position.getKostZuweisungen(), position);
        target.add(costTable.refresh().getTable());
        costEditModalWindow.close(target);
      }

      /**
       * @see org.apache.wicket.ajax.markup.html.form.AjaxButton#onError(org.apache.wicket.ajax.AjaxRequestTarget,
       *      org.apache.wicket.markup.html.form.Form)
       */
      @Override
      protected void onError(final AjaxRequestTarget target, final Form< ? > form)
      {
      }
    };
    final SingleButtonPanel submitButtonPanel = new SingleButtonPanel(costEditDialog.newButtonChildId(), submitButton, getString(label),
        SingleButtonPanel.DEFAULT_SUBMIT);
    costEditDialog.addButton(submitButtonPanel);

    costEditModalWindow.setWindowClosedCallback(new ModalWindow.WindowClosedCallback() {
      private static final long serialVersionUID = 2633814101880954425L;

      public void onClose(final AjaxRequestTarget target)
      {
      }

    });
    costEditModalWindow.setCloseButtonCallback(new ModalWindow.CloseButtonCallback() {
      private static final long serialVersionUID = 6761625465164911336L;

      public boolean onCloseButtonClicked(final AjaxRequestTarget target)
      {
        return true;
      }
    });
    costEditModalWindow.show(target);

  }

  protected abstract void cloneRechnung();

  /**
   * @return null
   */
  public Long getBezahlDatumInMillis()
  {
    return null;
  }

  /**
   * Dummy method. Does nothing.
   * @param bezahlDatumInMillis
   */
  public void setBezahlDatumInMillis(final Long bezahlDatumInMillis)
  {
  }

  private void addAjaxComponents(final AjaxRequestTarget target, final List<Component> components)
  {
    target.add(components.toArray(COMPONENT_ARRAY));
    if (ajaxUpdateComponentsArray == null) {
      ajaxUpdateComponentsArray = ajaxUpdateComponents.toArray(COMPONENT_ARRAY);
    }
    target.add(ajaxUpdateComponentsArray);
  }
}
