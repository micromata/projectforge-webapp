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

package org.projectforge.web.humanresources;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Iterator;

import org.apache.commons.collections.CollectionUtils;
import org.apache.log4j.Logger;
import org.apache.wicket.Component;
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
import org.hibernate.Hibernate;
import org.projectforge.core.Priority;
import org.projectforge.fibu.ProjektDO;
import org.projectforge.humanresources.HRPlanningDO;
import org.projectforge.humanresources.HRPlanningDao;
import org.projectforge.humanresources.HRPlanningEntryDO;
import org.projectforge.humanresources.HRPlanningEntryDao;
import org.projectforge.humanresources.HRPlanningEntryStatus;
import org.projectforge.user.PFUserDO;
import org.projectforge.web.calendar.DateTimeFormatter;
import org.projectforge.web.fibu.ProjektSelectPanel;
import org.projectforge.web.user.UserSelectPanel;
import org.projectforge.web.wicket.AbstractEditForm;
import org.projectforge.web.wicket.AttributeAppendModifier;
import org.projectforge.web.wicket.WicketUtils;
import org.projectforge.web.wicket.components.DatePanel;
import org.projectforge.web.wicket.components.DateTimePanelSettings;
import org.projectforge.web.wicket.components.JiraIssuesPanel;
import org.projectforge.web.wicket.components.LabelValueChoiceRenderer;
import org.projectforge.web.wicket.components.MaxLengthTextArea;


/**
 * 
 * @author Mario Groß (m.gross@micromata.de)
 * 
 */
public class HRPlanningEditForm extends AbstractEditForm<HRPlanningDO, HRPlanningEditPage>
{
  private static final long serialVersionUID = 3150725003240437752L;

  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(HRPlanningEditForm.class);

  @SpringBean(name = "hrPlanningEntryDao")
  private HRPlanningEntryDao hrPlanningEntryDao;

  @SpringBean(name = "hrPlanningDao")
  private HRPlanningDao hrPlanningDao;

  private boolean showDeletedOnly;

  private WebMarkupContainer showDeletedCheckBoxRow;

  protected DatePanel weekDatePanel;

  protected RepeatingView entriesRepeater;

  public HRPlanningEditForm(HRPlanningEditPage parentPage, HRPlanningDO data)
  {
    super(parentPage, data);
    this.colspan = 9;
  }

  @Override
  protected void validation()
  {
    final Iterator< ? extends Component> it = entriesRepeater.iterator();
    while (it.hasNext() == true) {
      final WebMarkupContainer entry = (WebMarkupContainer) it.next();
      final ProjektSelectPanel projektSelectPanel = (ProjektSelectPanel) entry.get("projekt");
      @SuppressWarnings("unchecked")
      final DropDownChoice<HRPlanningEntryStatus> statusChoice = (DropDownChoice<HRPlanningEntryStatus>) entry.get("status");
      final ProjektDO projekt;
      if (projektSelectPanel.isEnabled() == true) {
        projekt = projektSelectPanel.getConvertedInput();
      } else {
        projekt = projektSelectPanel.getModelObject();
      }
      final HRPlanningEntryStatus status;
      if (statusChoice.isEnabled() == true) {
        status = statusChoice.getConvertedInput();
      } else {
        status = statusChoice.getModelObject();
      }
      if (projekt == null && status == null) {
        addComponentError(projektSelectPanel, "hr.planning.entry.error.statusOrProjektRequired");
      } else if (projekt != null && status != null) {
        addComponentError(projektSelectPanel, "hr.planning.entry.error.statusAndProjektNotAllowed");
      }
    }
    if (hrPlanningDao.doesEntryAlreadyExist(data) == true) {
      addComponentError(weekDatePanel, "hr.planning.entry.error.entryDoesAlreadyExistForUserAndWeekOfYear");
    }
  }

  @Override
  protected void init()
  {
    super.init();
    {
      Hibernate.initialize(data.getUser());
      final UserSelectPanel userSelectPanel = new UserSelectPanel("user", new PropertyModel<PFUserDO>(data, "user"), parentPage, "userId");
      add(userSelectPanel);
      userSelectPanel.setRequired(true);
      userSelectPanel.init();
    }
    showDeletedCheckBoxRow = new WebMarkupContainer("deletedCheckBoxRow");
    add(showDeletedCheckBoxRow);
    @SuppressWarnings("serial")
    final CheckBox deletedCheckBox = new CheckBox("deletedCheckBox", new PropertyModel<Boolean>(this, "showDeletedOnly")) {
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
    };
    showDeletedCheckBoxRow.add(deletedCheckBox);
    // Start Date
    weekDatePanel = new DatePanel("weekDate", new PropertyModel<Date>(data, "week"), (DateTimePanelSettings) DateTimePanelSettings.get()
        .withTabIndex(3).withSelectStartStopTime(false).withCallerPage(parentPage).withTargetType(java.sql.Date.class));
    weekDatePanel.setRequired(true);
    add(weekDatePanel);
    @SuppressWarnings("serial")
    final Label weekOfYear = new Label("weekOfYear", new Model<String>() {
      @Override
      public String getObject()
      {
        if (data.getWeek() != null) {
          return DateTimeFormatter.formatWeekOfYear(data.getWeek());
        } else {
          return "--";
        }
      }
    });
    add(weekOfYear);
    entriesRepeater = new RepeatingView("entries");
    add(entriesRepeater);
    refresh();
    WicketUtils.addShowDeleteRowQuestionDialog(this, hrPlanningEntryDao);
  }

  void refresh()
  {
    showDeletedCheckBoxRow.setVisible(data.hasDeletedEntries());
    if (data.hasDeletedEntries() == false) {
      this.showDeletedOnly = false;
    }
    entriesRepeater.removeAll();
    if (CollectionUtils.isEmpty(data.getEntries()) == true) {
      // Ensure that at least one entry is available:
      data.addEntry(new HRPlanningEntryDO());
    }
    int idx = -1;
    for (final HRPlanningEntryDO entry : data.getEntries()) {
      ++idx;
      if (entry.isDeleted() != showDeletedOnly) {
        // Don't show deleted/undeleted entries.
        continue;
      }
      final WebMarkupContainer item = new WebMarkupContainer(entriesRepeater.newChildId());
      entriesRepeater.add(item);
      final ProjektSelectPanel projektSelectPanel = new ProjektSelectPanel("projekt", new PropertyModel<ProjektDO>(entry, "projekt"),
          parentPage, "projektId:" + idx);
      projektSelectPanel.setRequired(false).setEnabled(!entry.isDeleted());
      item.add(projektSelectPanel);
      projektSelectPanel.init();
      // DropDownChoice status
      {
        final LabelValueChoiceRenderer<HRPlanningEntryStatus> statusChoiceRenderer = new LabelValueChoiceRenderer<HRPlanningEntryStatus>(
            item, HRPlanningEntryStatus.values());
        @SuppressWarnings("unchecked")
        final DropDownChoice statusChoice = new DropDownChoice("status", new PropertyModel(entry, "status"), statusChoiceRenderer
            .getValues(), statusChoiceRenderer);
        statusChoice.setNullValid(true).setRequired(false).setEnabled(!entry.isDeleted());
        item.add(statusChoice);
      }
      // DropDownChoice Priority
      {
        final LabelValueChoiceRenderer<Priority> priorityChoiceRenderer = new LabelValueChoiceRenderer<Priority>(item, Priority.values());
        @SuppressWarnings("unchecked")
        final DropDownChoice priorityChoice = new DropDownChoice("priorityChoice", new PropertyModel(entry, "priority"),
            priorityChoiceRenderer.getValues(), priorityChoiceRenderer);
        priorityChoice.setNullValid(true).setEnabled(!entry.isDeleted());
        item.add(priorityChoice);
      }

      // DropDownChoice probability
      {
        final LabelValueChoiceRenderer<Integer> probabilityChoiceRenderer = new LabelValueChoiceRenderer<Integer>();
        probabilityChoiceRenderer.addValue(25, "25%");
        probabilityChoiceRenderer.addValue(50, "50%");
        probabilityChoiceRenderer.addValue(75, "75%");
        probabilityChoiceRenderer.addValue(95, "95%");
        probabilityChoiceRenderer.addValue(100, "100%");
        @SuppressWarnings("unchecked")
        final DropDownChoice probabilityChoice = new DropDownChoice("probabilityChoice", new PropertyModel(entry, "probability"),
            probabilityChoiceRenderer.getValues(), probabilityChoiceRenderer);
        probabilityChoice.setNullValid(true).setEnabled(!entry.isDeleted());
        item.add(probabilityChoice);
      }
      final TextField<BigDecimal> unassignedHours = new TextField<BigDecimal>("unassignedHours", new PropertyModel<BigDecimal>(entry,
          "unassignedHours"));
      item.add(unassignedHours.setEnabled(!entry.isDeleted()));
      final TextField<BigDecimal> mondayHours = new TextField<BigDecimal>("mondayHours",
          new PropertyModel<BigDecimal>(entry, "mondayHours"));
      item.add(mondayHours.setEnabled(!entry.isDeleted()));
      final TextField<BigDecimal> tuesdayHours = new TextField<BigDecimal>("tuesdayHours", new PropertyModel<BigDecimal>(entry,
          "tuesdayHours"));
      item.add(tuesdayHours.setEnabled(!entry.isDeleted()));
      final TextField<BigDecimal> wednesdayHours = new TextField<BigDecimal>("wednesdayHours", new PropertyModel<BigDecimal>(entry,
          "wednesdayHours"));
      item.add(wednesdayHours.setEnabled(!entry.isDeleted()));
      final TextField<BigDecimal> thursdayHours = new TextField<BigDecimal>("thursdayHours", new PropertyModel<BigDecimal>(entry,
          "thursdayHours"));
      item.add(thursdayHours.setEnabled(!entry.isDeleted()));
      final TextField<BigDecimal> fridayHours = new TextField<BigDecimal>("fridayHours",
          new PropertyModel<BigDecimal>(entry, "fridayHours"));
      item.add(fridayHours.setEnabled(!entry.isDeleted()));
      final TextField<BigDecimal> weekendHours = new TextField<BigDecimal>("weekendHours", new PropertyModel<BigDecimal>(entry,
          "weekendHours"));
      item.add(weekendHours.setEnabled(!entry.isDeleted()));

      item.add(WicketUtils.getJIRASupportTooltipImage(getResponse(), this).setEnabled(!entry.isDeleted()));
      final MaxLengthTextArea description = new MaxLengthTextArea("description", new PropertyModel<String>(entry, "description"));
      item.add(description.setEnabled(!entry.isDeleted()));
      item.add(new JiraIssuesPanel("jiraIssues", entry.getDescription()));
      @SuppressWarnings("serial")
      final SubmitLink deleteUndeleteEntryButton = new SubmitLink("deleteUndeleteEntry") {
        public void onSubmit()
        {
          if (entry.isDeleted() == true) {
            // Undelete
            entry.setDeleted(false);
          } else {
            getData().deleteEntry(entry);
          }
          refresh();
        };
      };
      if (entry.isDeleted() == true) {
        deleteUndeleteEntryButton.add(WicketUtils.getUndeleteRowImage(item, "deleteUndeleteEntryImage", getResponse()));
      } else {
        if (entry.getId() != null) {
          deleteUndeleteEntryButton.add(new AttributeAppendModifier("onclick", "if (showDeleteQuestionDialog() == false) return false;")
              .setPrepend());
        }
        deleteUndeleteEntryButton.add(WicketUtils.getDeleteRowImage(item, "deleteUndeleteEntryImage", getResponse(), entry));
      }
      deleteUndeleteEntryButton.setDefaultFormProcessing(false);
      item.add(deleteUndeleteEntryButton);
      @SuppressWarnings("serial")
      final SubmitLink addEntryButton = new SubmitLink("addEntry") {
        public void onSubmit()
        {
          getData().addEntry(new HRPlanningEntryDO());
          refresh();
        };
      };
      item.add(addEntryButton);
      addEntryButton.add(WicketUtils.getAddRowImage("addEntryImage", getResponse(), getString("hr.planning.tooltip.addEntry")));
      if (showDeletedOnly == true) {
        addEntryButton.setVisible(false);
      }
    }
    final Iterator< ? extends Component> it = entriesRepeater.iterator();
    while (it.hasNext() == true) {
      // Needed because last entry can be deleted:
      final WebMarkupContainer entry = (WebMarkupContainer) it.next();
      if (it.hasNext() == true) {
        // Show only Button for last position.
        final SubmitLink addEntryButton = (SubmitLink) entry.get("addEntry");
        addEntryButton.setVisible(false);
      }
    }
  }

  public boolean isShowDeletedOnly()
  {
    return showDeletedOnly;
  }

  public void setShowDeletedOnly(boolean showDeletedOnly)
  {
    this.showDeletedOnly = showDeletedOnly;
  }

  void setData(final HRPlanningDO planning)
  {
    data = planning;
  }

  @Override
  protected Logger getLogger()
  {
    return log;
  }
}
