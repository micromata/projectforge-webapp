/////////////////////////////////////////////////////////////////////////////
//
// Project ProjectForge Community Edition
//         www.projectforge.org
//
// Copyright (C) 2001-2012 Kai Reinhard (k.reinhard@micromata.de)
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

package org.projectforge.plugins.teamcal.event;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.ObjectUtils;
import org.apache.log4j.Logger;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.projectforge.common.DateHelper;
import org.projectforge.common.DatePrecision;
import org.projectforge.plugins.teamcal.admin.TeamCalDO;
import org.projectforge.plugins.teamcal.admin.TeamCalDao;
import org.projectforge.plugins.teamcal.admin.TeamCalFilter;
import org.projectforge.user.UserGroupCache;
import org.projectforge.web.wicket.AbstractEditForm;
import org.projectforge.web.wicket.WicketUtils;
import org.projectforge.web.wicket.autocompletion.PFAutoCompleteMaxLengthTextField;
import org.projectforge.web.wicket.components.DateTimePanel;
import org.projectforge.web.wicket.components.DateTimePanelSettings;
import org.projectforge.web.wicket.components.LabelValueChoiceRenderer;
import org.projectforge.web.wicket.components.MaxLengthTextArea;
import org.projectforge.web.wicket.components.MaxLengthTextField;
import org.projectforge.web.wicket.flowlayout.CheckBoxPanel;
import org.projectforge.web.wicket.flowlayout.DivPanel;
import org.projectforge.web.wicket.flowlayout.FieldsetPanel;

/**
 * form to edit team events.
 * 
 * @author M. Lauterbach (m.lauterbach@micromata.de)
 * 
 */
public class TeamEventEditForm extends AbstractEditForm<TeamEventDO, TeamEventEditPage>
{
  private static final long serialVersionUID = -8378262684943803495L;

  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(TeamEventEditForm.class);

  @SpringBean(name = "teamCalDao")
  private TeamCalDao teamCalDao;

  @SpringBean(name = "teamEventDao")
  private TeamEventDao teamEventDao;

  @SpringBean(name = "userGroupCache")
  private UserGroupCache userGroupCache;

  private DateTimePanel startDateTimePanel;

  private DateTimePanel endDateTimePanel;

  private boolean access;

  private FieldsetPanel endDateField;

  private FieldsetPanel startDateField;

  final TeamEventRight right = new TeamEventRight();

  /**
   * @param parentPage
   * @param data
   */
  public TeamEventEditForm(final TeamEventEditPage parentPage, final TeamEventDO data)
  {
    super(parentPage, data);
  }

  /**
   * @see org.projectforge.web.wicket.AbstractEditForm#init()
   */
  @Override
  protected void init()
  {
    super.init();

    gridBuilder.newGrid8();

    final TeamCalDO teamCal = data.getCalendar();

    // setting access view
    if (isNew() == true || teamCal.getOwner() == null) {
      access = true;
    } else {
      if (right.hasUpdateAccess(getUser(), data, data) == true) {
        access = true;
      } else {
        access = false;
        if (right.hasMinimalAccess(data, getUserId()) == true) {
          final TeamEventDO newTeamEventDO = new TeamEventDO();
          newTeamEventDO.setId(data.getId());
          newTeamEventDO.setStartDate(data.getStartDate());
          newTeamEventDO.setEndDate(data.getEndDate());
          data = newTeamEventDO;
          access = false;
        }
      }
    }

    // add teamCal drop down
    initTeamCalPicker(gridBuilder.newFieldset(getString("plugins.teamevent.teamCal"), true));

    {
      // SUBJECT
      final FieldsetPanel fieldSet = gridBuilder.newFieldset(getString("plugins.teamevent.subject"));
      final MaxLengthTextField subjectField = new MaxLengthTextField(fieldSet.getTextFieldId(), new PropertyModel<String>(data, "subject"));
      subjectField.setRequired(true);
      fieldSet.add(subjectField);
      if (access == false)
        fieldSet.setEnabled(false);
    }

    {
      // NOTE
      final FieldsetPanel fieldSet = gridBuilder.newFieldset(getString("plugins.teamevent.note"));
      final MaxLengthTextArea noteField = new MaxLengthTextArea(fieldSet.getTextAreaId(), new PropertyModel<String>(data, "note"));
      fieldSet.add(noteField);
      if (access == false)
        fieldSet.setEnabled(false);
    }

    {
      // LOCATION
      final FieldsetPanel fieldSet = gridBuilder.newFieldset(getString("plugins.teamevent.location"));
      @SuppressWarnings("serial")
      final PFAutoCompleteMaxLengthTextField locationTextField = new PFAutoCompleteMaxLengthTextField(fieldSet.getTextFieldId(),
          new PropertyModel<String>(data, "location")) {

        @Override
        protected List<String> getChoices(final String input)
        {
          return teamEventDao.getAutocompletion("location", input);
        }

      };
      fieldSet.add(locationTextField);
      if (access == false)
        fieldSet.setEnabled(false);
    }

    gridBuilder.newGrid8();
    // add date panel
    initDatePanel();

    {
      // ALL DAY CHECKBOX
      final FieldsetPanel fieldSet = gridBuilder.newFieldset("", true).setNoLabelFor();
      final DivPanel divPanel = fieldSet.addNewCheckBoxDiv();
      final CheckBoxPanel checkBox = new CheckBoxPanel(divPanel.newChildId(), new PropertyModel<Boolean>(data, "allDay"),
          getString("plugins.teamevent.allDay"));
      checkBox.getCheckBox().add(new AjaxFormComponentUpdatingBehavior("onChange") {
        private static final long serialVersionUID = 3523446385818267608L;

        @Override
        protected void onUpdate(final AjaxRequestTarget target)
        {
          if (data.isAllDay() == false) {
            setDateDropChoiceVisible(true);
          } else {
            setDateDropChoiceVisible(false);
          }
          target.add(startDateField.getFieldset());
          target.add(endDateField.getFieldset());
        }
      });
      setDateDropChoiceVisible(data.isAllDay() == false);
      divPanel.add(checkBox);
      fieldSet.add(divPanel);
      if (access == false)
        fieldSet.setEnabled(false);
    }
  }

  private void setDateDropChoiceVisible(final boolean visible)
  {
    startDateTimePanel.getHourOfDayDropDownChoice().setVisible(visible);
    startDateTimePanel.getMinuteDropDownChoice().setVisible(visible);
    endDateTimePanel.getHourOfDayDropDownChoice().setVisible(visible);
    endDateTimePanel.getMinuteDropDownChoice().setVisible(visible);
  }

  /**
   * if has access: create drop down with teamCals else create label
   * 
   * @param fieldSet
   */
  @SuppressWarnings("serial")
  private void initTeamCalPicker(final FieldsetPanel fieldSet)
  {

    if (access == false) {
      final Label teamCalTitle = new Label(fieldSet.newChildId(), new PropertyModel<String>(data, "calendar.getTitle()"));
      fieldSet.add(teamCalTitle);
    } else {
      final List<TeamCalDO> result = teamCalDao.getList(new TeamCalFilter());
      final List<TeamCalDO> list = new ArrayList<TeamCalDO>();
      for (final TeamCalDO cal : result) {
        if (right.hasUpdateAccess(getUser(), cal) == true) {
          result.add(cal);
        }
      }
      final PropertyModel<TeamCalDO> selectModel = new PropertyModel<TeamCalDO>(data, "calendar");
      final DropDownChoice<TeamCalDO> teamCalDrop = new DropDownChoice<TeamCalDO>(fieldSet.getDropDownChoiceId(), selectModel, list,
          getLabeledList(list)) {
        /**
         * @see org.apache.wicket.markup.html.form.AbstractSingleSelectChoice#isSelected(java.lang.Object, int, java.lang.String)
         */
        @Override
        protected boolean isSelected(final TeamCalDO object, final int index, final String selected)
        {
          final boolean check = super.isSelected(object, index, selected);
          final TeamCalDO team = data.getCalendar();
          if (ObjectUtils.equals(object.getId(), team.getId()))
            return true;
          else return check;
        }
      };
      teamCalDrop.setNullValid(false);
      teamCalDrop.setRequired(true);
      fieldSet.add(teamCalDrop);
    }
  }

  /**
   * create date panel
   * 
   * @param dateFieldSet
   */
  private void initDatePanel()
  {
    startDateField = gridBuilder.newFieldset(getString("plugins.teamevent.beginDate"), true);
    startDateField.getFieldset().setOutputMarkupId(true);
    startDateTimePanel = new DateTimePanel(startDateField.newChildId(), new PropertyModel<Date>(data, "startDate"),
        (DateTimePanelSettings) DateTimePanelSettings.get().withSelectStartStopTime(true).withTargetType(java.sql.Timestamp.class)
        .withRequired(true), DatePrecision.MINUTE_15);
    startDateField.add(startDateTimePanel);
    dateFieldToolTip(startDateTimePanel);

    endDateField = gridBuilder.newFieldset(getString("plugins.teamevent.endDate"), true);
    endDateField.getFieldset().setOutputMarkupId(true);
    endDateTimePanel = new DateTimePanel(endDateField.newChildId(), new PropertyModel<Date>(data, "endDate"),
        (DateTimePanelSettings) DateTimePanelSettings.get().withSelectStartStopTime(true).withTargetType(java.sql.Timestamp.class)
        .withRequired(true), DatePrecision.MINUTE_15);
    endDateField.add(endDateTimePanel);
    dateFieldToolTip(endDateTimePanel);

    if (access == false) {
      endDateField.setEnabled(false);
      startDateField.setEnabled(false);
    }
  }

  /**
   * add tooltip to datefield.
   */
  private void dateFieldToolTip(final DateTimePanel component)
  {
    WicketUtils.addTooltip(component.getDateField(), new Model<String>() {
      private static final long serialVersionUID = 3878115580425103805L;

      @Override
      public String getObject()
      {
        final StringBuffer buf = new StringBuffer();
        if (data.getStartDate() != null) {
          buf.append(DateHelper.TECHNICAL_ISO_UTC.get().format(data.getStartDate()));
          if (data.getEndDate() != null) {
            buf.append(" - ");
          }
        }
        if (data.getEndDate() != null) {
          buf.append(DateHelper.TECHNICAL_ISO_UTC.get().format(data.getEndDate()));
        }
        return buf.toString();
      }
    });
  }

  private LabelValueChoiceRenderer<TeamCalDO> getLabeledList(final List<TeamCalDO> list)
  {
    final LabelValueChoiceRenderer<TeamCalDO> templateNamesChoiceRenderer = new LabelValueChoiceRenderer<TeamCalDO>();
    for (final TeamCalDO t : list) {
      templateNamesChoiceRenderer.addValue(t, t.getTitle());
    }
    return templateNamesChoiceRenderer;
  }

  @Override
  public void onBeforeRender()
  {
    super.onBeforeRender();
  }

  /**
   * @see org.projectforge.web.wicket.AbstractEditForm#getLogger()
   */
  @Override
  protected Logger getLogger()
  {
    return log;
  }

}
