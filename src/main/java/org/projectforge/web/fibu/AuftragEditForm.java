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
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.log4j.Logger;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.apache.wicket.util.convert.IConverter;
import org.projectforge.common.NumberHelper;
import org.projectforge.core.CurrencyFormatter;
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
import org.projectforge.web.wicket.AbstractEditForm;
import org.projectforge.web.wicket.AbstractUnsecureBasePage;
import org.projectforge.web.wicket.WicketUtils;
import org.projectforge.web.wicket.components.DatePanel;
import org.projectforge.web.wicket.components.DatePanelSettings;
import org.projectforge.web.wicket.components.LabelValueChoiceRenderer;
import org.projectforge.web.wicket.components.MaxLengthTextArea;
import org.projectforge.web.wicket.components.MaxLengthTextField;
import org.projectforge.web.wicket.components.MinMaxNumberField;
import org.projectforge.web.wicket.components.RequiredMaxLengthTextField;
import org.projectforge.web.wicket.components.SingleButtonPanel;
import org.projectforge.web.wicket.converter.CurrencyConverter;
import org.projectforge.web.wicket.flowlayout.CheckBoxPanel;
import org.projectforge.web.wicket.flowlayout.DivPanel;
import org.projectforge.web.wicket.flowlayout.DivTextPanel;
import org.projectforge.web.wicket.flowlayout.DivType;
import org.projectforge.web.wicket.flowlayout.FieldsetPanel;
import org.projectforge.web.wicket.flowlayout.HtmlCodePanel;
import org.projectforge.web.wicket.flowlayout.InputPanel;
import org.projectforge.web.wicket.flowlayout.RadioGroupPanel;
import org.projectforge.web.wicket.flowlayout.TextAreaPanel;
import org.projectforge.web.wicket.flowlayout.TextStyle;
import org.projectforge.web.wicket.flowlayout.ToggleContainerPanel;

public class AuftragEditForm extends AbstractEditForm<AuftragDO, AuftragEditPage>
{
  private static final long serialVersionUID = 3150725003240437752L;

  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(AuftragEditForm.class);

  private static final BigDecimal MAX_PERSON_DAYS = new BigDecimal(10000);

  private boolean sendEMailNotification = true;

  @SpringBean(name = "userGroupCache")
  private UserGroupCache userGroupCache;

  protected CheckBox sendEMailNotficationCheckBox;

  protected RepeatingView positionsRepeater;

  protected CustomerSelectPanel kundeSelectPanel;

  private final List<Component> ajaxUpdateComponents = new ArrayList<Component>();

  @SpringBean(name = "rechnungCache")
  private RechnungCache rechnungCache;

  public AuftragEditForm(final AuftragEditPage parentPage, final AuftragDO data)
  {
    super(parentPage, data);
  }

  public boolean isSendEMailNotification()
  {
    return sendEMailNotification;
  }

  public void setSendEMailNotification(final boolean sendEMailNotification)
  {
    this.sendEMailNotification = sendEMailNotification;
  }

  @Override
  @SuppressWarnings("serial")
  protected void init()
  {
    super.init();
    /* GRID8 - BLOCK */
    gridBuilder.newGrid16();
    gridBuilder.newColumnsPanel().newColumnPanel(DivType.COL_50);
    {
      // Number
      final FieldsetPanel fs = gridBuilder.newFieldset(getString("fibu.auftrag.nummer"), true);
      final MinMaxNumberField<Integer> number = new MinMaxNumberField<Integer>(InputPanel.WICKET_ID, new PropertyModel<Integer>(data,
          "nummer"), 0, 99999999);
      number.setMaxLength(8).add(AttributeModifier.append("style", "width: 6em !important;"));
      fs.add(number);
      if (NumberHelper.greaterZero(getData().getNummer()) == false) {
        fs.addHelpIcon(getString("fibu.tooltip.nummerWirdAutomatischVergeben"));
      }
    }
    gridBuilder.newColumnPanel(DivType.COL_50);
    {
      // Net sum
      final FieldsetPanel fs = gridBuilder.newFieldset(getString("fibu.auftrag.nettoSumme"));
      final DivTextPanel netPanel = new DivTextPanel(fs.newChildId(), new Model<String>() {
        @Override
        public String getObject()
        {
          return CurrencyFormatter.format(data.getNettoSumme());
        }
      }, TextStyle.FORM_TEXT);
      fs.add(netPanel);
      fs.setNoLabelFor();
      ajaxUpdateComponents.add(netPanel.getLabel4Ajax());
    }
    gridBuilder.newBlockPanel();
    {
      // Title
      final FieldsetPanel fs = gridBuilder.newFieldset(getString("fibu.auftrag.titel"));
      final MaxLengthTextField subject = new RequiredMaxLengthTextField(InputPanel.WICKET_ID, new PropertyModel<String>(data, "titel"));
      subject.add(WicketUtils.setFocus());
      fs.add(subject);
    }
    gridBuilder.newColumnsPanel().newColumnPanel(DivType.COL_50);
    {
      // reference
      final FieldsetPanel fs = gridBuilder.newFieldset(getString("fibu.common.reference"));
      fs.add(new MaxLengthTextField(InputPanel.WICKET_ID, new PropertyModel<String>(data, "referenz")));
    }
    gridBuilder.newColumnPanel(DivType.COL_50);
    {
      // DropDownChoice status
      final FieldsetPanel fs = gridBuilder.newFieldset(getString("status"));
      final LabelValueChoiceRenderer<AuftragsStatus> statusChoiceRenderer = new LabelValueChoiceRenderer<AuftragsStatus>(this,
          AuftragsStatus.values());
      final DropDownChoice<AuftragsStatus> statusChoice = new DropDownChoice<AuftragsStatus>(fs.getDropDownChoiceId(),
          new PropertyModel<AuftragsStatus>(data, "auftragsStatus"), statusChoiceRenderer.getValues(), statusChoiceRenderer);
      statusChoice.setNullValid(false).setRequired(true);
      fs.add(statusChoice);
    }
    gridBuilder.newBlockPanel();
    {
      // project
      final FieldsetPanel fs = gridBuilder.newFieldset(getString("fibu.projekt")).setNoLabelFor();
      final ProjektSelectPanel projektSelectPanel = new ProjektSelectPanel(fs.newChildId(), new PropertyModel<ProjektDO>(data, "projekt"),
          parentPage, "projektId");
      fs.add(projektSelectPanel);
      projektSelectPanel.init();
    }
    {
      // customer
      final FieldsetPanel fs = gridBuilder.newFieldset(getString("fibu.kunde"), true).setNoLabelFor();
      kundeSelectPanel = new CustomerSelectPanel(fs.newChildId(), new PropertyModel<KundeDO>(data, "kunde"), new PropertyModel<String>(
          data, "kundeText"), parentPage, "kundeId");
      fs.add(kundeSelectPanel);
      kundeSelectPanel.init();
      fs.addHelpIcon(getString("fibu.auftrag.hint.kannVonProjektKundenAbweichen"));
    }
    gridBuilder.newColumnsPanel().newColumnPanel(DivType.COL_50);
    {
      // date
      final FieldsetPanel fs = gridBuilder.newFieldset(getString("fibu.auftrag.datum"));
      final DatePanel angebotsDatumPanel = new DatePanel(fs.newChildId(), new PropertyModel<Date>(data, "angebotsDatum"), DatePanelSettings
          .get().withTargetType(java.sql.Date.class));
      angebotsDatumPanel.setRequired(true);
      fs.add(angebotsDatumPanel);
    }
    gridBuilder.newColumnPanel(DivType.COL_50);
    {
      // Bindungsfrist
      final FieldsetPanel fs = gridBuilder.newFieldset(getString("fibu.auftrag.bindungsFrist"));
      final DatePanel bindungsFristPanel = new DatePanel(fs.newChildId(), new PropertyModel<Date>(data, "bindungsFrist"), DatePanelSettings
          .get().withTargetType(java.sql.Date.class));
      fs.add(bindungsFristPanel);
    }
    gridBuilder.newColumnsPanel().newColumnPanel(DivType.COL_50);
    {
      // contact person
      final FieldsetPanel fs = gridBuilder.newFieldset(getString("contactPerson"));
      final UserSelectPanel contactPersonSelectPanel = new UserSelectPanel(fs.newChildId(), new PropertyModel<PFUserDO>(data,
          "contactPerson"), parentPage, "contactPersonId");
      contactPersonSelectPanel.setRequired(true);
      fs.add(contactPersonSelectPanel);
      contactPersonSelectPanel.init();
    }
    gridBuilder.newColumnPanel(DivType.COL_50);
    {
      // Beauftragungsdatum
      final FieldsetPanel fs = gridBuilder.newFieldset(getString("fibu.auftrag.beauftragungsdatum"));
      final DatePanel beauftragungsDatumPanel = new DatePanel(fs.newChildId(), new PropertyModel<Date>(data, "beauftragungsDatum"),
          DatePanelSettings.get().withTargetType(java.sql.Date.class));
      fs.add(beauftragungsDatumPanel);
    }
    positionsRepeater = new RepeatingView(flowform.newChildId());
    flowform.add(positionsRepeater);
    gridBuilder.newGrid16();
    {
      // comment
      final FieldsetPanel fs = gridBuilder.newFieldset(getString("comment"));
      fs.add(new MaxLengthTextArea(TextAreaPanel.WICKET_ID, new PropertyModel<String>(data, "bemerkung"))).setAutogrow();
    }
    {
      // status comment
      final FieldsetPanel fs = gridBuilder.newFieldset(getString("fibu.auftrag.statusBeschreibung"));
      fs.add(new MaxLengthTextArea(TextAreaPanel.WICKET_ID, new PropertyModel<String>(data, "statusBeschreibung"))).setAutogrow();
    }
    {
      // email
      final FieldsetPanel fs = gridBuilder.newFieldset(getString("email"), true);
      final DivPanel radioGroupPanel = fs.addNewRadioBoxDiv();
      final RadioGroupPanel<Boolean> radioGroup = new RadioGroupPanel<Boolean>(radioGroupPanel.newChildId(), "sendEMailNotification",
          new PropertyModel<Boolean>(this, "sendEMailNotification"));
      radioGroupPanel.add(radioGroup);
      WicketUtils.addYesNo(radioGroup);
      fs.setLabelFor(radioGroup.getRadioGroup());
      fs.addHelpIcon(getString("label.sendEMailNotification"));
    }
    refresh();
  }

  @SuppressWarnings("serial")
  void refresh()
  {
    positionsRepeater.removeAll();
    final boolean hasInsertAccess = getBaseDao().hasInsertAccess(getUser());
    if (CollectionUtils.isEmpty(data.getPositionen()) == true) {
      // Ensure that at least one position is available:
      data.addPosition(new AuftragsPositionDO());
    }
    DivPanel content = null, columns, column;
    for (final AuftragsPositionDO position : data.getPositionen()) {
      final boolean abgeschlossenUndNichtFakturiert = position.isAbgeschlossenUndNichtVollstaendigFakturiert();
      final ToggleContainerPanel positionsPanel = new ToggleContainerPanel(positionsRepeater.newChildId(), DivType.GRID16,
          DivType.ROUND_ALL);
      positionsPanel.getContainer().setOutputMarkupId(true);
      positionsRepeater.add(positionsPanel);
      final StringBuffer heading = new StringBuffer();
      heading.append(escapeHtml(getString("fibu.auftrag.position.short"))).append(" #").append(position.getNumber());
      positionsPanel.setHeading(new HtmlCodePanel(ToggleContainerPanel.HEADING_ID, heading.toString()));
      content = new DivPanel(ToggleContainerPanel.CONTENT_ID);
      positionsPanel.add(content);
      content.add(columns = new DivPanel(content.newChildId(), DivType.BLOCK));
      {
        final FieldsetPanel fs = new FieldsetPanel(columns, getString("fibu.auftrag.titel"));
        fs.add(new MaxLengthTextField(InputPanel.WICKET_ID, new PropertyModel<String>(position, "titel")));
      }
      content.add(columns = new DivPanel(content.newChildId(), DivType.COLUMNS));
      columns.add(column = new DivPanel(columns.newChildId(), DivType.COL_33));
      {
        // DropDownChoice type
        final FieldsetPanel fs = new FieldsetPanel(column, getString("fibu.auftrag.position.art"));
        final LabelValueChoiceRenderer<AuftragsPositionsArt> artChoiceRenderer = new LabelValueChoiceRenderer<AuftragsPositionsArt>(fs,
            AuftragsPositionsArt.values());
        final DropDownChoice<AuftragsPositionsArt> artChoice = new DropDownChoice<AuftragsPositionsArt>(fs.getDropDownChoiceId(),
            new PropertyModel<AuftragsPositionsArt>(position, "art"), artChoiceRenderer.getValues(), artChoiceRenderer);
        artChoice.setNullValid(false);
        artChoice.setRequired(true);
        fs.add(artChoice);
      }
      columns.add(column = new DivPanel(columns.newChildId(), DivType.COL_33));
      {
        // Person days
        final FieldsetPanel fs = new FieldsetPanel(column, getString("projectmanagement.personDays"));
        fs.add(new MinMaxNumberField<BigDecimal>(InputPanel.WICKET_ID, new PropertyModel<BigDecimal>(position, "personDays"),
            BigDecimal.ZERO, MAX_PERSON_DAYS));
      }
      columns.add(column = new DivPanel(columns.newChildId(), DivType.COL_33));
      {
        // Net sum
        final FieldsetPanel fs = new FieldsetPanel(column, getString("fibu.auftrag.nettoSumme"));
        fs.add(new TextField<String>(InputPanel.WICKET_ID, new PropertyModel<String>(position, "nettoSumme")) {
          @SuppressWarnings({ "rawtypes", "unchecked"})
          @Override
          public IConverter getConverter(final Class type)
          {
            return new CurrencyConverter();
          }
        });
        if (abgeschlossenUndNichtFakturiert == true) {
          fs.setWarninngBackground();
        }
      }
      content.add(columns = new DivPanel(content.newChildId(), DivType.COLUMNS));
      columns.add(column = new DivPanel(columns.newChildId(), DivType.COL_33));
      final Set<RechnungsPositionVO> orderPositions = rechnungCache.getRechnungsPositionVOSetByAuftragsPositionId(position.getId());
      final boolean showInvoices = CollectionUtils.isNotEmpty(orderPositions);
      {
        // Invoices
        final FieldsetPanel fs = new FieldsetPanel(column, getString("fibu.rechnungen")).setNoLabelFor();
        if (showInvoices == true) {
          final InvoicePositionsPanel panel = new InvoicePositionsPanel(fs.newChildId());
          fs.add(panel);
          panel.init(orderPositions);
        } else {
          fs.add(AbstractUnsecureBasePage.createInvisibleDummyComponent(fs.newChildId()));
        }
      }
      columns.add(column = new DivPanel(columns.newChildId(), DivType.COL_33));
      {
        // invoiced
        final FieldsetPanel fs = new FieldsetPanel(column, getString("fibu.fakturiert"), true).setNoLabelFor();
        if (showInvoices == true) {
          fs.add(new DivTextPanel(fs.newChildId(), CurrencyFormatter.format(RechnungDao.getNettoSumme(orderPositions))));
        } else {
          fs.add(AbstractUnsecureBasePage.createInvisibleDummyComponent(fs.newChildId()));
        }
        if (userGroupCache.isUserMemberOfFinanceGroup() == true) {
          final DivPanel checkBoxDiv = fs.addNewCheckBoxDiv();
          checkBoxDiv.add(new CheckBoxPanel(checkBoxDiv.newChildId(), new PropertyModel<Boolean>(position, "vollstaendigFakturiert"),
              getString("fibu.auftrag.vollstaendigFakturiert")));
        }
      }
      columns.add(column = new DivPanel(columns.newChildId(), DivType.COL_33));
      {
        // DropDownChoice status
        final FieldsetPanel fs = new FieldsetPanel(column, getString("status"));
        final LabelValueChoiceRenderer<AuftragsPositionsStatus> statusChoiceRenderer = new LabelValueChoiceRenderer<AuftragsPositionsStatus>(
            fs, AuftragsPositionsStatus.values());
        final DropDownChoice<AuftragsPositionsStatus> statusChoice = new DropDownChoice<AuftragsPositionsStatus>(fs.getDropDownChoiceId(),
            new PropertyModel<AuftragsPositionsStatus>(position, "status"), statusChoiceRenderer.getValues(), statusChoiceRenderer);
        statusChoice.setNullValid(true);
        statusChoice.setRequired(false);
        fs.add(statusChoice);
        if (abgeschlossenUndNichtFakturiert == true) {
          fs.setWarninngBackground();
        }
      }
      content.add(columns = new DivPanel(content.newChildId(), DivType.COLUMNS));
      {
        // Task
        final FieldsetPanel fs = new FieldsetPanel(columns, getString("task"));
        final TaskSelectPanel taskSelectPanel = new TaskSelectPanel(fs.newChildId(), new PropertyModel<TaskDO>(position, "task"),
            parentPage, "taskId:" + position.getNumber()) {
          @Override
          protected void selectTask(final TaskDO task)
          {
            super.selectTask(task);
            parentPage.getBaseDao().setTask(position, task.getId());
          }
        };
        fs.add(taskSelectPanel);
        taskSelectPanel.init();
      }
      {
        // Comment
        final FieldsetPanel fs = new FieldsetPanel(columns, getString("comment"));
        fs.add(new MaxLengthTextArea(TextAreaPanel.WICKET_ID, new PropertyModel<String>(position, "bemerkung")));
      }
    }
    if (hasInsertAccess == true) {
      content.add(columns = new DivPanel(content.newChildId(), DivType.BLOCK));
      final Button addPositionButton = new Button(SingleButtonPanel.WICKET_ID) {
        @Override
        public final void onSubmit()
        {
          getData().addPosition(new AuftragsPositionDO());
          refresh();
        }
      };
      final SingleButtonPanel addPositionButtonPanel = new SingleButtonPanel(columns.newChildId(), addPositionButton, getString("add"));
      addPositionButtonPanel.setTooltip(getString("fibu.auftrag.tooltip.addPosition"));
      columns.add(addPositionButtonPanel);
    }
  }

  @Override
  protected Logger getLogger()
  {
    return log;
  }
}
