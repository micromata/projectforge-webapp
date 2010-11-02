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
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.wicket.Component;
import org.apache.wicket.behavior.SimpleAttributeModifier;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.SubmitLink;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.apache.wicket.util.convert.IConverter;
import org.projectforge.common.NumberHelper;
import org.projectforge.common.StringHelper;
import org.projectforge.core.CurrencyFormatter;
import org.projectforge.core.NumberFormatter;
import org.projectforge.fibu.AuftragDO;
import org.projectforge.fibu.AuftragsPositionDO;
import org.projectforge.fibu.AuftragsPositionsArt;
import org.projectforge.fibu.AuftragsPositionsStatus;
import org.projectforge.fibu.AuftragsStatus;
import org.projectforge.fibu.KundeDO;
import org.projectforge.fibu.ProjektDO;
import org.projectforge.fibu.RechnungCache;
import org.projectforge.fibu.RechnungDao;
import org.projectforge.fibu.RechnungsPositionVO;
import org.projectforge.task.TaskDO;
import org.projectforge.user.PFUserDO;
import org.projectforge.user.UserGroupCache;
import org.projectforge.web.task.TaskSelectPanel;
import org.projectforge.web.user.UserSelectPanel;
import org.projectforge.web.wicket.AbstractBasePage;
import org.projectforge.web.wicket.AbstractEditForm;
import org.projectforge.web.wicket.FocusOnLoadBehavior;
import org.projectforge.web.wicket.WebConstants;
import org.projectforge.web.wicket.WicketUtils;
import org.projectforge.web.wicket.components.DatePanel;
import org.projectforge.web.wicket.components.DatePanelSettings;
import org.projectforge.web.wicket.components.LabelValueChoiceRenderer;
import org.projectforge.web.wicket.components.MaxLengthTextArea;
import org.projectforge.web.wicket.components.MaxLengthTextField;
import org.projectforge.web.wicket.components.MinMaxNumberField;
import org.projectforge.web.wicket.components.RequiredMaxLengthTextField;
import org.projectforge.web.wicket.components.TooltipImage;
import org.projectforge.web.wicket.converter.CurrencyConverter;


public class AuftragEditForm extends AbstractEditForm<AuftragDO, AuftragEditPage>
{
  private static final long serialVersionUID = 3150725003240437752L;

  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(AuftragEditForm.class);

  private static final BigDecimal MAX_PERSON_DAYS = new BigDecimal(10000);

  private boolean sendEMailNotification = true;

  @SpringBean(name = "userGroupCache")
  private UserGroupCache userGroupCache;

  protected CheckBox sendEMailNotficationCheckBox;

  protected DatePanel angebotsDatumPanel;

  protected DatePanel bindungsFristPanel;

  protected DatePanel beauftragungsDatumPanel;

  protected RepeatingView positionsRepeater;

  protected KundeSelectPanel kundeSelectPanel;

  private boolean showInactivePositions = false;

  @SpringBean(name = "rechnungCache")
  private RechnungCache rechnungCache;

  private class RefreshCheckBox extends CheckBox
  {
    private static final long serialVersionUID = -8209057947011133420L;

    RefreshCheckBox(final String componentId, final String property)
    {
      super(componentId, new PropertyModel<Boolean>(AuftragEditForm.this, property));
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

  public AuftragEditForm(AuftragEditPage parentPage, AuftragDO data)
  {
    super(parentPage, data);
    this.colspan = 9;
  }

  public boolean isSendEMailNotification()
  {
    return sendEMailNotification;
  }

  public void setSendEMailNotification(boolean sendEMailNotification)
  {
    this.sendEMailNotification = sendEMailNotification;
  }

  @Override
  @SuppressWarnings("serial")
  protected void init()
  {
    super.init();
    add(new RefreshCheckBox("showInactivePositionsCheckBox", "showInactivePositions"));
    final Component nummerField = new TextField<Integer>("nummer", new PropertyModel<Integer>(data, "nummer"));
    add(nummerField);

    add(new TooltipImage("nummerHelp", getResponse(), WebConstants.IMAGE_HELP, getString("fibu.tooltip.nummerWirdAutomatischVergeben")) {
      @Override
      public boolean isVisible()
      {
        return NumberHelper.greaterZero(getData().getNummer()) == false;
      }
    });
    add(new Label("nettoSumme", new Model<String>() {
      @Override
      public String getObject()
      {
        return CurrencyFormatter.format(data.getNettoSumme());
      }
    }));

    // DropDownChoice status
    final LabelValueChoiceRenderer<AuftragsStatus> statusChoiceRenderer = new LabelValueChoiceRenderer<AuftragsStatus>(this, AuftragsStatus
        .values());
    @SuppressWarnings("unchecked")
    final DropDownChoice statusChoice = new DropDownChoice("status", new PropertyModel(data, "auftragsStatus"), statusChoiceRenderer
        .getValues(), statusChoiceRenderer);
    statusChoice.setNullValid(false).setRequired(true);
    add(statusChoice);

    final RequiredMaxLengthTextField titleField = new RequiredMaxLengthTextField("titel", new PropertyModel<String>(data, "titel"));
    add(titleField);
    titleField.add(new FocusOnLoadBehavior());
    add(new MaxLengthTextField("referenz", new PropertyModel<String>(data, "referenz")));
    add(new MaxLengthTextArea("bemerkung", new PropertyModel<String>(data, "bemerkung")));
    add(new MaxLengthTextArea("statusBeschreibung", new PropertyModel<String>(data, "statusBeschreibung")));
    add(new TooltipImage("kundeHelp", getResponse(), WebConstants.IMAGE_HELP, getString("fibu.auftrag.hint.kannVonProjektKundenAbweichen")));

    angebotsDatumPanel = new DatePanel("angebotsDatum", new PropertyModel<Date>(data, "angebotsDatum"), DatePanelSettings.get()
        .withCallerPage(parentPage).withTargetType(java.sql.Date.class));
    angebotsDatumPanel.setRequired(true);
    add(angebotsDatumPanel);
    bindungsFristPanel = new DatePanel("bindungsFrist", new PropertyModel<Date>(data, "bindungsFrist"), DatePanelSettings.get()
        .withCallerPage(parentPage).withTargetType(java.sql.Date.class));
    add(bindungsFristPanel);
    beauftragungsDatumPanel = new DatePanel("beauftragungsDatum", new PropertyModel<Date>(data, "beauftragungsDatum"), DatePanelSettings
        .get().withCallerPage(parentPage).withTargetType(java.sql.Date.class));
    add(beauftragungsDatumPanel);
    sendEMailNotficationCheckBox = new CheckBox("sendEMailNotification", new PropertyModel<Boolean>(this, "sendEMailNotification"));

    add(sendEMailNotficationCheckBox);
    final ProjektSelectPanel projektSelectPanel = new ProjektSelectPanel("projekt", new PropertyModel<ProjektDO>(data, "projekt"),
        parentPage, "projektId");
    add(projektSelectPanel);
    projektSelectPanel.init();
    kundeSelectPanel = new KundeSelectPanel("kunde", new PropertyModel<KundeDO>(data, "kunde"),
        new PropertyModel<String>(data, "kundeText"), parentPage, "kundeId");
    add(kundeSelectPanel);
    kundeSelectPanel.init();
    final UserSelectPanel contactPersonSelectPanel = new UserSelectPanel("contactPerson",
        new PropertyModel<PFUserDO>(data, "contactPerson"), parentPage, "contactPersonId");
    contactPersonSelectPanel.setRequired(true);
    add(contactPersonSelectPanel);
    contactPersonSelectPanel.init();
    positionsRepeater = new RepeatingView("positions");
    add(positionsRepeater);
    refresh();
  }

  @SuppressWarnings("serial")
  void refresh()
  {
    positionsRepeater.removeAll();
    if (CollectionUtils.isEmpty(data.getPositionen()) == true) {
      // Ensure that at least one position is available:
      data.addPosition(new AuftragsPositionDO());
    }
    final List<AuftragsPositionDO> positionen = data.getPositionen();
    final int counter = positionen.size();
    for (final AuftragsPositionDO position : data.getPositionen()) {
      final boolean abgeschlossenUndNichtFakturiert = position.isAbgeschlossenUndNichtVollstaendigFakturiert();
      final WebMarkupContainer item = new WebMarkupContainer(positionsRepeater.newChildId());
      positionsRepeater.add(item);
      final WebMarkupContainer shortPositionView = new WebMarkupContainer("shortPositionView");
      item.add(shortPositionView);
      final WebMarkupContainer normalPositionView = new WebMarkupContainer("normalPositionView");
      item.add(normalPositionView);
      if (isShowInactivePositions() == false
          && (position.isDeleted() == true || position.isVollstaendigFakturiert() == true || position.getStatus() == AuftragsPositionsStatus.NICHT_BEAUFTRAGT)) {
        normalPositionView.setVisible(false);
      } else {
        shortPositionView.setVisible(false);
      }
      final Label numberLabel = new Label("number", String.valueOf(position.getNumber()));
      if (normalPositionView.isVisible() == false) {
        shortPositionView.add(numberLabel);
        final StringBuffer buf = new StringBuffer();
        boolean first = true;
        if (StringUtils.isNotBlank(position.getTitel()) == true) {
          first = StringHelper.append(buf, first, position.getTitel(), ", ");
        }
        if (NumberHelper.isNotZero(position.getPersonDays()) == true) {
          first = StringHelper.append(buf, first, NumberFormatter.format(position.getPersonDays()), ", ");
          buf.append(" ").append(getString("projectmanagement.personDays.short"));
        }
        if (NumberHelper.isNotZero(position.getNettoSumme()) == true) {
          first = StringHelper.append(buf, first, CurrencyFormatter.format(position.getNettoSumme()), ", ");
        }
        if (position.getStatus() != null) {
          first = StringHelper.append(buf, first, getString(position.getStatus().getI18nKey()), ", ");
        }
        if (position.isVollstaendigFakturiert() == true) {
          first = StringHelper.append(buf, first, getString("fibu.auftrag.vollstaendigFakturiert"), ", ");
        }
        shortPositionView.add(new Label("shortText", buf.toString()).setRenderBodyOnly(true));
      } else {
        normalPositionView.add(numberLabel);
        normalPositionView.add(new MaxLengthTextField("titel", new PropertyModel<String>(position, "titel")));

        // DropDownChoice listType
        final LabelValueChoiceRenderer<AuftragsPositionsArt> artChoiceRenderer = new LabelValueChoiceRenderer<AuftragsPositionsArt>(item,
            AuftragsPositionsArt.values());
        @SuppressWarnings("unchecked")
        final DropDownChoice artChoice = new DropDownChoice("art", new PropertyModel(position, "art"), artChoiceRenderer.getValues(),
            artChoiceRenderer);
        artChoice.setNullValid(false);
        artChoice.setRequired(true);
        normalPositionView.add(artChoice);

        final Label statusLabel = new Label("statusLabel", getString("status"));
        if (abgeschlossenUndNichtFakturiert == true) {
          statusLabel.add(new SimpleAttributeModifier("style", WebConstants.CSS_BACKGROUND_COLOR_RED));
        }
        normalPositionView.add(statusLabel);
        // DropDownChoice listType
        final LabelValueChoiceRenderer<AuftragsPositionsStatus> statusChoiceRenderer = new LabelValueChoiceRenderer<AuftragsPositionsStatus>(
            item, AuftragsPositionsStatus.values());
        @SuppressWarnings("unchecked")
        final DropDownChoice statusChoice = new DropDownChoice("status", new PropertyModel(position, "status"), statusChoiceRenderer
            .getValues(), statusChoiceRenderer);
        statusChoice.setNullValid(true);
        statusChoice.setRequired(false);
        normalPositionView.add(statusChoice);

        final Label nettoSummeLabel = new Label("nettoSummeLabel", getString("fibu.auftrag.nettoSumme"));
        if (abgeschlossenUndNichtFakturiert == true) {
          nettoSummeLabel.add(new SimpleAttributeModifier("style", WebConstants.CSS_BACKGROUND_COLOR_RED));
        }
        normalPositionView.add(nettoSummeLabel);
        normalPositionView.add(new TextField<String>("nettoSumme", new PropertyModel<String>(position, "nettoSumme")) {
          @Override
          public IConverter getConverter(Class< ? > type)
          {
            return new CurrencyConverter();
          }
        });

        final Set<RechnungsPositionVO> orderPositions = rechnungCache.getRechnungsPositionVOSetByAuftragsPositionId(position.getId());
        final boolean showInvoices = CollectionUtils.isNotEmpty(orderPositions);
        final Label invoicesLabel = new Label("invoicesLabel", getString("fibu.rechnungen"));
        normalPositionView.add(invoicesLabel).setRenderBodyOnly(true);
        if (showInvoices == true) {
          final InvoicePositionsPanel panel = new InvoicePositionsPanel("invoices");
          normalPositionView.add(panel);
          panel.init(orderPositions);
        } else {
          normalPositionView.add(AbstractBasePage.createInvisibleDummyComponent("invoices"));
        }
        final Label invoicedLabel = new Label("invoicedLabel", getString("fibu.fakturiert"));
        normalPositionView.add(invoicedLabel);
        final Label invoicedSum = new Label("invoicedSum", CurrencyFormatter.format(RechnungDao.getNettoSumme(orderPositions)));
        normalPositionView.add(invoicedSum);
        if (showInvoices == false) {
          invoicesLabel.setVisible(false);
          invoicedLabel.setVisible(false);
          invoicedSum.setVisible(false);
        }

        final TaskSelectPanel taskSelectPanel = new TaskSelectPanel("task", new PropertyModel<TaskDO>(position, "task"), parentPage,
            "taskId:" + position.getNumber()) {
          @Override
          protected void selectTask(TaskDO task)
          {
            super.selectTask(task);
            parentPage.getBaseDao().setTask(position, task.getId());
          }
        };
        normalPositionView.add(taskSelectPanel);
        taskSelectPanel.init();
        taskSelectPanel.setEnableLinks(true);
        normalPositionView.add(new MinMaxNumberField<BigDecimal>("personDays", new PropertyModel<BigDecimal>(position, "personDays"),
            BigDecimal.ZERO, MAX_PERSON_DAYS));
        normalPositionView.add(new MaxLengthTextArea("bemerkung", new PropertyModel<String>(position, "bemerkung")));
        final CheckBox vollstaendigFakturiertCheckBox = new CheckBox("vollstaendigFakturiert", new PropertyModel<Boolean>(position,
            "vollstaendigFakturiert"));
        WicketUtils.addTooltip(vollstaendigFakturiertCheckBox, getString("fibu.auftrag.vollstaendigFakturiert"));
        if (userGroupCache.isUserMemberOfFinanceGroup() == false) {
          vollstaendigFakturiertCheckBox.setVisible(false);
        }
        normalPositionView.add(vollstaendigFakturiertCheckBox);
      }
      final SubmitLink addPositionButton = new SubmitLink("addPosition") {
        public void onSubmit()
        {
          getData().addPosition(new AuftragsPositionDO());
          refresh();
        };
      };
      item.add(addPositionButton);
      addPositionButton.add(WicketUtils.getAddRowImage("addPositionImage", getResponse(), getString("fibu.auftrag.tooltip.addPosition")));
      if (position.getNumber() < counter) {
        // Show only Button for last position.
        addPositionButton.setVisible(false);
      }
    }
  }

  public boolean isShowInactivePositions()
  {
    return showInactivePositions;
  }

  public void setShowInactivePositions(boolean showInactivePositions)
  {
    this.showInactivePositions = showInactivePositions;
  }

  @Override
  protected Logger getLogger()
  {
    return log;
  }
}
