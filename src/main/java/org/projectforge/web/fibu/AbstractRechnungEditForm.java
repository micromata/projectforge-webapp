/////////////////////////////////////////////////////////////////////////////
//
// Project ProjectForge Community Edition
//         www.projectforge.org
//
// Copyright (C) 2001-2010 Kai Reinhard (k.reinhard@me.com)
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
import java.math.RoundingMode;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.wicket.Component;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.SubmitLink;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.apache.wicket.util.convert.IConverter;
import org.projectforge.calendar.DayHolder;
import org.projectforge.common.NumberHelper;
import org.projectforge.core.Configuration;
import org.projectforge.core.ConfigurationParam;
import org.projectforge.core.CurrencyFormatter;
import org.projectforge.core.NumberFormatter;
import org.projectforge.fibu.AbstractRechnungDO;
import org.projectforge.fibu.AbstractRechnungsPositionDO;
import org.projectforge.fibu.KostFormatter;
import org.projectforge.fibu.RechnungDO;
import org.projectforge.fibu.kost.Kost1DO;
import org.projectforge.fibu.kost.Kost2DO;
import org.projectforge.fibu.kost.Kost2Dao;
import org.projectforge.fibu.kost.KostZuweisungDO;
import org.projectforge.web.HtmlHelper;
import org.projectforge.web.wicket.AbstractEditForm;
import org.projectforge.web.wicket.AbstractEditPage;
import org.projectforge.web.wicket.AttributeAppendModifier;
import org.projectforge.web.wicket.DatesAsUTCLabel;
import org.projectforge.web.wicket.WicketUtils;
import org.projectforge.web.wicket.components.DatePanel;
import org.projectforge.web.wicket.components.DatePanelSettings;
import org.projectforge.web.wicket.components.LabelValueChoiceRenderer;
import org.projectforge.web.wicket.components.MaxLengthTextArea;
import org.projectforge.web.wicket.components.MinMaxNumberField;
import org.projectforge.web.wicket.components.RequiredMaxLengthTextField;
import org.projectforge.web.wicket.components.SingleButtonPanel;
import org.projectforge.web.wicket.converter.BigDecimalPercentConverter;
import org.projectforge.web.wicket.converter.CurrencyConverter;


public abstract class AbstractRechnungEditForm<O extends AbstractRechnungDO<T>, T extends AbstractRechnungsPositionDO, P extends AbstractEditPage< ? , ? , ? >>
    extends AbstractEditForm<O, P>
{
  private static final long serialVersionUID = 9073611406229693582L;

  public static final int[] ZAHLUNGSZIELE_IN_TAGEN = { 7, 14, 30, 60, 90};

  @SpringBean(name = "kost2Dao")
  private Kost2Dao kost2Dao;

  private DropDownChoice<Integer> zahlungsZielChoice;

  private DropDownChoice<Long> bezahlDatumChoice;

  protected DatePanel datumPanel;

  protected DatePanel bezahlDatumPanel;

  protected DatePanel faelligkeitPanel;

  protected TextField<BigDecimal> zahlBetragField;

  protected RepeatingView positionsRepeater;

  private boolean showTextAreas;

  private boolean showKostZuweisungen;

  private boolean showEditableKostZuweisungen;
  
  private T highlightedPosition;

  protected SingleButtonPanel cloneButtonPanel;

  private class RefreshCheckBox extends CheckBox
  {
    private static final long serialVersionUID = 7529562566480212604L;

    RefreshCheckBox(final String componentId, final String property)
    {
      super(componentId, new PropertyModel<Boolean>(AbstractRechnungEditForm.this, property));
    }

    @Override
    public void onSelectionChanged()
    {
      super.onSelectionChanged();
      refresh();
    }

    @Override
    protected boolean wantOnSelectionChangedNotifications()
    {
      return true;
    }
  }

  public AbstractRechnungEditForm(final P parentPage, final O data)
  {
    super(parentPage, data);
    this.colspan = 10;
  }

  protected abstract void onInit();

  protected void afterInit()
  {

  }

  @SuppressWarnings("serial")
  @Override
  protected void init()
  {
    super.init();
    if (isNew() == true) {
      showTextAreas = true;
    } else {
      if (data.hasKostZuweisungen() == true) {
        showKostZuweisungen = true;
      }
    }
    onInit();
    final RefreshCheckBox showTextAreasCheckBox = new RefreshCheckBox("showTextAreasCheckBox", "showTextAreas");
    if (isNew() == true) {
      showTextAreasCheckBox.setEnabled(false);
    }
    add(showTextAreasCheckBox);
    add(new RefreshCheckBox("showKostZuweisungenCheckBox", "showKostZuweisungen"));
    add(new RefreshCheckBox("showEditableKostZuweisungenCheckBox", "showEditableKostZuweisungen"));
    add(new RequiredMaxLengthTextField("betreff", new PropertyModel<String>(data, "betreff")));
    zahlBetragField = new TextField<BigDecimal>("zahlBetrag", new PropertyModel<BigDecimal>(data, "zahlBetrag")) {
      @Override
      public IConverter getConverter(Class< ? > type)
      {
        return new CurrencyConverter();
      }
    };
    add(zahlBetragField);
    datumPanel = new DatePanel("datum", new PropertyModel<Date>(data, "datum"), DatePanelSettings.get().withCallerPage(
        (ISelectCallerPage) parentPage).withTargetType(java.sql.Date.class));
    add(datumPanel);
    faelligkeitPanel = new DatePanel("faelligkeit", new PropertyModel<Date>(data, "faelligkeit"), DatePanelSettings.get().withCallerPage(
        (ISelectCallerPage) parentPage).withTargetType(java.sql.Date.class));
    add(faelligkeitPanel);
    // DropDownChoice ZahlungsZiel
    final LabelValueChoiceRenderer<Integer> zielChoiceRenderer = new LabelValueChoiceRenderer<Integer>();
    for (final int days : ZAHLUNGSZIELE_IN_TAGEN) {
      zielChoiceRenderer.addValue(days, String.valueOf(days) + " " + getString("days"));
    }
    zahlungsZielChoice = new DropDownChoice<Integer>("zahlungsZielList", new PropertyModel<Integer>(this, "zahlungsZiel"),
        zielChoiceRenderer.getValues(), zielChoiceRenderer) {
      @Override
      public boolean isVisible()
      {
        return data.getFaelligkeit() == null;
      }
    };
    zahlungsZielChoice.setNullValid(true);
    zahlungsZielChoice.setRequired(false);
    add(zahlungsZielChoice);
    final Label zahlungsZiel = new Label("zahlungsZielInTagen", new Model<String>() {
      @Override
      public String getObject()
      {
        return data.getZahlungsZielInTagen() + " " + getString("days");
      }
    }) {
      @Override
      public boolean isVisible()
      {
        return data.getFaelligkeit() != null;
      }
    };
    add(zahlungsZiel);

    bezahlDatumPanel = new DatePanel("bezahlDatum", new PropertyModel<Date>(data, "bezahlDatum"), DatePanelSettings.get().withCallerPage(
        (ISelectCallerPage) parentPage).withTargetType(java.sql.Date.class));
    add(bezahlDatumPanel);
    // DropDownChoice bezahlDatumList
    final LabelValueChoiceRenderer<Long> bezahlDatumChoiceRenderer = WicketUtils.getDatumChoiceRenderer(20);
    bezahlDatumChoice = new DropDownChoice<Long>("bezahlDatumList", new PropertyModel<Long>(this, "bezahlDatumInMillis"),
        bezahlDatumChoiceRenderer.getValues(), bezahlDatumChoiceRenderer) {
      @Override
      public boolean isVisible()
      {
        return data.getBezahlDatum() == null;
      }
    };
    bezahlDatumChoice.setNullValid(true);
    bezahlDatumChoice.setRequired(false);
    add(bezahlDatumChoice);

    add(new Label("netto", new Model<String>() {
      @Override
      public String getObject()
      {
        return CurrencyFormatter.format(data.getNetSum());
      }
    }));
    add(new Label("vatAmount", new Model<String>() {
      @Override
      public String getObject()
      {
        return CurrencyFormatter.format(data.getVatAmountSum());
      }
    }));
    add(new Label("brutto", new Model<String>() {
      @Override
      public String getObject()
      {
        return CurrencyFormatter.format(data.getGrossSum());
      }
    }));

    add(new MaxLengthTextArea("bemerkung", new PropertyModel<String>(data, "bemerkung")));
    add(new MaxLengthTextArea("besonderheiten", new PropertyModel<String>(data, "besonderheiten")));
    add(new DatesAsUTCLabel("utcDatum") {
      @Override
      public Date getStartTime()
      {
        return data.getDatum();
      }
    });
    add(new DatesAsUTCLabel("utcFaelligkeit") {
      @Override
      public Date getStartTime()
      {
        return data.getFaelligkeit();
      }
    });
    add(new DatesAsUTCLabel("utcBezahlDatum") {
      @Override
      public Date getStartTime()
      {
        return data.getBezahlDatum();
      }
    });
    positionsRepeater = new RepeatingView("positions");
    add(positionsRepeater);
    refresh();
    afterInit();
  }

  protected abstract T newPositionInstance();

  @SuppressWarnings("serial")
  void refresh()
  {
    positionsRepeater.removeAll();
    if (CollectionUtils.isEmpty(data.getPositionen()) == true) {
      // Ensure that at least one position is available:
      final T position = newPositionInstance();
      position.setVat(Configuration.getInstance().getPercentValue(ConfigurationParam.FIBU_DEFAULT_VAT));
      data.addPosition(position);
    }
    final List<T> positionen = data.getPositionen();
    final int counter = positionen.size();
    int no = 0;
    for (final T position : data.getPositionen()) {
      // Fetch all kostZuweisungen:
      if (CollectionUtils.isNotEmpty(position.getKostZuweisungen()) == true) {
        for (final KostZuweisungDO zuweisung : position.getKostZuweisungen()) {
          zuweisung.getNetto(); // Fetch
        }
      }
      final WebMarkupContainer item = new WebMarkupContainer(positionsRepeater.newChildId());
      positionsRepeater.add(item);
      final WebMarkupContainer positionRow = new WebMarkupContainer("positionRow");
      if (position == this.highlightedPosition) {
        positionRow.add(new AttributeAppendModifier("style", WicketUtils.getHighlightedRowCssStyle()));
      }
      item.add(positionRow);
      final Label numberLabel = new Label("number", String.valueOf(position.getNumber()));
      positionRow.add(numberLabel);
      final SubmitLink addPositionButton = new SubmitLink("addPosition") {
        public void onSubmit()
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
        };
      };
      item.add(addPositionButton);
      addPositionButton.add(WicketUtils.getAddRowImage("addPositionImage", getResponse(), getString("fibu.rechnung.tooltip.addPosition")));
      final MaxLengthTextArea textArea = new MaxLengthTextArea("editableText", new PropertyModel<String>(position, "text"));
      final Label readOnlyText = new Label("readOnlyText", HtmlHelper.formatText(position.getText(), true));
      if (isShowTextAreas() == true) {
        readOnlyText.setVisible(false);
      } else {
        textArea.setVisible(false);
      }
      positionRow.add(textArea);
      positionRow.add(readOnlyText.setEscapeModelStrings(false));
      positionRow.add(new MinMaxNumberField<BigDecimal>("vat", new PropertyModel<BigDecimal>(position, "vat"), BigDecimal.ZERO,
          NumberHelper.HUNDRED) {
        @Override
        public IConverter getConverter(Class< ? > type)
        {
          return new BigDecimalPercentConverter(true);
        }
      });
      positionRow.add(new MinMaxNumberField<BigDecimal>("menge", new PropertyModel<BigDecimal>(position, "menge"), BigDecimal.ZERO,
          NumberHelper.BILLION));
      positionRow.add(new TextField<BigDecimal>("einzelNetto", new PropertyModel<BigDecimal>(position, "einzelNetto")) {
        @Override
        public IConverter getConverter(Class< ? > type)
        {
          return new CurrencyConverter();
        }
      });
      positionRow.add(new Label("netto", new Model<String>() {
        @Override
        public String getObject()
        {
          return CurrencyFormatter.format(position.getNetSum());
        }
      }));
      positionRow.add(new Label("vatAmount", new Model<String>() {
        @Override
        public String getObject()
        {
          return CurrencyFormatter.format(position.getVatAmount());
        }
      }));
      positionRow.add(new Label("brutto", new Model<String>() {
        @Override
        public String getObject()
        {
          return CurrencyFormatter.format(position.getBruttoSum());
        }
      }));
      if (++no < counter) {
        // Show only Button for last position.
        addPositionButton.setVisible(false);
      }
      final WebMarkupContainer kostZuweisungRow = new WebMarkupContainer("kostZuweisungRow");
      item.add(kostZuweisungRow);
      if (isShowKostZuweisungen() == false && isShowEditableKostZuweisungen() == false) {
        kostZuweisungRow.setVisible(false);
      } else {
        final RepeatingView kostZuweisungsRepeater = new RepeatingView("kostZuweisungen");
        kostZuweisungRow.add(kostZuweisungsRepeater);
        if (CollectionUtils.isNotEmpty(position.getKostZuweisungen()) == true) {
          for (final KostZuweisungDO zuweisung : position.getKostZuweisungen()) {
            final WebMarkupContainer subItem = new WebMarkupContainer(kostZuweisungsRepeater.newChildId());
            kostZuweisungsRepeater.add(subItem);
            subItem.add(new Kost1FormComponent("kost1", new PropertyModel<Kost1DO>(zuweisung, "kost1"), true)
                .setVisible(isShowEditableKostZuweisungen()));
            final Component kost1ReadonlyLabel = new Label("kost1Readonly", KostFormatter.format(zuweisung.getKost1()))
                .setVisible(!isShowEditableKostZuweisungen());
            if (kost1ReadonlyLabel.isVisible() == true) {
              WicketUtils.addTooltip(kost1ReadonlyLabel, KostFormatter.formatToolTip(zuweisung.getKost1()));
            }
            subItem.add(kost1ReadonlyLabel);
            subItem.add(new Kost2FormComponent("kost2", new PropertyModel<Kost2DO>(zuweisung, "kost2"), true)
                .setVisible(isShowEditableKostZuweisungen()));
            final Component kost2ReadonlyLabel = new Label("kost2Readonly", KostFormatter.format(zuweisung.getKost2()))
                .setVisible(!isShowEditableKostZuweisungen());
            if (kost2ReadonlyLabel.isVisible() == true) {
              WicketUtils.addTooltip(kost2ReadonlyLabel, KostFormatter.formatToolTip(zuweisung.getKost2()));
            }
            subItem.add(kost2ReadonlyLabel);
            final Component nettoTextField = new TextField<BigDecimal>("netto", new PropertyModel<BigDecimal>(zuweisung, "netto")) {
              @Override
              public IConverter getConverter(Class< ? > type)
              {
                return new CurrencyConverter(position.getNetSum());
              }
            }.setVisible(isShowEditableKostZuweisungen());
            WicketUtils.addTooltip(nettoTextField, getString("currencyConverter.percentage.help"));
            subItem.add(nettoTextField);
            subItem.add(new Label("nettoReadonly", CurrencyFormatter.format(zuweisung.getNetto())).setVisible(
                !isShowEditableKostZuweisungen()).setRenderBodyOnly(true));
            final BigDecimal percentage;
            if (NumberHelper.isZeroOrNull(position.getNetSum()) == true || NumberHelper.isZeroOrNull(zuweisung.getNetto()) == true) {
              percentage = BigDecimal.ZERO;
            } else {
              percentage = zuweisung.getNetto().divide(position.getNetSum(), RoundingMode.HALF_UP);
            }
            final boolean percentageVisible = NumberHelper.isNotZero(percentage);
            subItem.add(new Label("percentage", NumberFormatter.formatPercent(percentage)).setVisible(percentageVisible));
            final SubmitLink deleteEntryButton = new SubmitLink("deleteEntry") {
              public void onSubmit()
              {
                position.deleteKostZuweisung(zuweisung.getIndex());
                refresh();
              };
            };
            deleteEntryButton.add(WicketUtils.getDeleteTooltipImage(item, "deleteEntryImage", getResponse()));
            deleteEntryButton.setDefaultFormProcessing(false);
            subItem.add(deleteEntryButton);
            if (isShowEditableKostZuweisungen() == false || position.isKostZuweisungDeletable(zuweisung) == false) {
              // Only new created entries and last entries are deletable.
              deleteEntryButton.setVisible(false);
            }
          }
        }
      }
      final WebMarkupContainer addZuweisungRow = new WebMarkupContainer("addZuweisungRow");
      if (showKostZuweisungen == false && showEditableKostZuweisungen == false) {
        addZuweisungRow.setVisible(false);
      }
      kostZuweisungRow.add(addZuweisungRow);
      final SubmitLink addZuweisungButton = new SubmitLink("addZuweisung") {
        public void onSubmit()
        {
          final KostZuweisungDO kostZuweisung = new KostZuweisungDO();
          position.addKostZuweisung(kostZuweisung);
          if (kostZuweisung.getIndex() > 0) {
            final KostZuweisungDO predecessor = position.getKostZuweisung(kostZuweisung.getIndex() - 1);
            if (predecessor != null) {
              kostZuweisung.setKost1(predecessor.getKost1()); // Preset kost1 from the predecessor position.
              kostZuweisung.setKost2(predecessor.getKost2()); // Preset kost2 from the predecessor position.
            }
          }
          if (RechnungDO.class.isAssignableFrom(getData().getClass()) == true && kostZuweisung.getKost2() == null) {
            // Preset kost2 with first kost2 found for the projekt.
            final List<Kost2DO> kost2List = kost2Dao.getActiveKost2(((RechnungDO) getData()).getProjekt());
            if (CollectionUtils.isNotEmpty(kost2List) == true) {
              kostZuweisung.setKost2(kost2List.get(0));
            }
          }
          final BigDecimal remainder = position.getNetSum().subtract(position.getKostZuweisungsNetSum());
          kostZuweisung.setNetto(remainder);
          highlightedPosition = position;
          refresh();
        };
      };
      addZuweisungRow.add(addZuweisungButton);
      if (isShowEditableKostZuweisungen() == false) {
        addZuweisungButton.setVisible(false);
      }
      addZuweisungButton.add(WicketUtils.getAddRowImage("addPositionImage", getResponse(), getString("fibu.rechnung.tooltip.addPosition")));
      final BigDecimal remainder = position.getNetSum().subtract(position.getKostZuweisungsNetSum());
      if (remainder.compareTo(BigDecimal.ZERO) == 0) {
        addZuweisungRow.add(createInvisibleDummyComponent("remainder"));
      } else {
        addZuweisungRow.add(new Label("remainder", CurrencyFormatter.format(remainder)));
      }

      onRenderPosition(item, position);
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

  @SuppressWarnings("serial")
  @Override
  protected void addButtonPanel()
  {
    final Fragment buttonFragment = new Fragment("buttonPanel", "buttonFragment", this);
    buttonFragment.setRenderBodyOnly(true);
    buttonCell.add(buttonFragment);
    cloneButtonPanel = new SingleButtonPanel("clone", new Button("button", new Model<String>(getString("clone"))) {
      @Override
      public final void onSubmit()
      {
        cloneRechnung();
      }
    });
    if (isNew() == true || getData().isDeleted() == true) {
      // Show clone button only for existing time sheets.
      cloneButtonPanel.setVisible(false);
    }
    buttonFragment.add(cloneButtonPanel);
  }

  protected abstract void cloneRechnung();

  @Override
  protected void validation()
  {
    final Long bezahlDatumInMillis = bezahlDatumChoice.getConvertedInput();
    Date bezahlDatum = bezahlDatumPanel.getConvertedInput();
    if (bezahlDatumInMillis != null) {
      final DayHolder day = new DayHolder(new java.sql.Date(bezahlDatumInMillis));
      bezahlDatum = day.getSQLDate();
      data.setBezahlDatum(day.getSQLDate());
      bezahlDatumPanel.markModelAsChanged();
    }

    final Integer zahlungsZiel = zahlungsZielChoice.getConvertedInput();
    Date faelligkeit = faelligkeitPanel.getConvertedInput();
    if (zahlungsZiel != null) {
      Date date = datumPanel.getConvertedInput();
      if (date == null) {
        date = getData().getDatum();
      }
      if (date != null) {
        final DayHolder day = new DayHolder(date);
        day.add(Calendar.DAY_OF_YEAR, zahlungsZiel);
        faelligkeit = day.getSQLDate();
        getData().setFaelligkeit(day.getSQLDate());
        faelligkeitPanel.markModelAsChanged();
      }
    }
    getData().recalculate();

    final BigDecimal zahlBetrag = zahlBetragField.getConvertedInput();
    boolean zahlBetragExists = (zahlBetrag != null && zahlBetrag.compareTo(BigDecimal.ZERO) != 0);
    if (bezahlDatum != null && zahlBetragExists == false) {
      addError("fibu.rechnung.error.bezahlDatumUndZahlbetragRequired");
    }
    if (faelligkeit == null) {
      addFieldRequiredError("fibu.rechnung.faelligkeit");
    }
  }

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
  public void setBezahlDatumInMillis(Long bezahlDatumInMillis)
  {
  }

  /**
   * @return null
   */
  public Integer getZahlungsZiel()
  {
    return null;
  }

  /**
   * Dummy method. Does nothing.
   * @param zahlungsZiel
   */
  public void setZahlungsZiel(Integer zahlungsZiel)
  {
  }

  public boolean isShowTextAreas()
  {
    return showTextAreas;
  }

  public void setShowTextAreas(boolean showTextAreas)
  {
    this.showTextAreas = showTextAreas;
  }

  public boolean isShowKostZuweisungen()
  {
    return showKostZuweisungen;
  }

  public void setShowKostZuweisungen(boolean showKostZuweisungen)
  {
    this.showKostZuweisungen = showKostZuweisungen;
  }

  public boolean isShowEditableKostZuweisungen()
  {
    return showEditableKostZuweisungen;
  }

  public void setShowEditableKostZuweisungen(boolean showEditableKostZuweisungen)
  {
    this.showEditableKostZuweisungen = showEditableKostZuweisungen;
  }
}
