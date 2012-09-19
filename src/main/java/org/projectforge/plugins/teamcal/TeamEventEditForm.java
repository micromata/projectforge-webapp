/////////////////////////////////////////////////////////////////////////////
//
// Project   ProjectForge
//
// Copyright 2001-2009, Micromata GmbH, Kai Reinhard
//           All rights reserved.
//
/////////////////////////////////////////////////////////////////////////////

package org.projectforge.plugins.teamcal;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.ObjectUtils;
import org.apache.log4j.Logger;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.validation.IFormValidator;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.projectforge.common.DateHelper;
import org.projectforge.common.DateHolder;
import org.projectforge.common.DatePrecision;
import org.projectforge.timesheet.TimesheetDao;
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
import org.projectforge.web.wicket.flowlayout.DivTextPanel;
import org.projectforge.web.wicket.flowlayout.FieldsetPanel;

/**
 * @author Maximilian Lauterbach (m.lauterbach@micromata.de)
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

  @SuppressWarnings("unused")
  private int stopHourOfDay, stopMinute;

  private boolean allDay;

  private DateTimePanel startDateTimePanel;

  private DropDownChoice<Integer> stopHourOfDayDropDownChoice;

  private DropDownChoice<Integer> stopMinuteDropDownChoice;

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

    gridBuilder.newGrid16();
    parentPage.preInit();

    setTeamCalPicker(gridBuilder.newFieldset(getString("plugins.teamevent.teamCal"), true));

    {
      final FieldsetPanel set = gridBuilder.newFieldset(getString("plugins.teamevent.subject"));
      final MaxLengthTextField subjectField = new MaxLengthTextField(set.getTextFieldId(), new PropertyModel<String>(data, "subject"));
      subjectField.setRequired(true);
      set.add(subjectField);
    }
    {
      final FieldsetPanel set = gridBuilder.newFieldset(getString("plugins.teamevent.note"));
      final MaxLengthTextArea noteField = new MaxLengthTextArea(set.getTextAreaId(), new PropertyModel<String>(data, "note"));
      set.add(noteField);
    }

    setPeriodPanel(gridBuilder.newFieldset(getString("plugins.teamevent.duration"), true));

    {
      final FieldsetPanel set = gridBuilder.newFieldset(getString("plugins.teamevent.location"));
      @SuppressWarnings("serial")
      final PFAutoCompleteMaxLengthTextField locationTextField = new PFAutoCompleteMaxLengthTextField(set.getTextFieldId(),
          new PropertyModel<String>(data, "location")){

        @Override
        protected List<String> getChoices(final String input)
        {
          return teamEventDao.getAutocompletion("location", input);
        }

      };
      set.add(locationTextField);
    }

    {
      final FieldsetPanel set = gridBuilder.newFieldset("", true);
      final DivPanel checkBoxPanel = set.addNewCheckBoxDiv();
      checkBoxPanel.add(new CheckBoxPanel(checkBoxPanel.newChildId(), new PropertyModel<Boolean>(data, "allDay"), getString("plugins.teamevent.allDay")));
      set.add(checkBoxPanel);
    }
  }

  /**
   * @param newFieldset
   */
  @SuppressWarnings("serial")
  private void setTeamCalPicker(final FieldsetPanel newFieldset)
  {
    final List<TeamCalDO> list = teamCalDao.getTeamCalsByAccess(getUser(), TeamCalDao.FULL_ACCESS_GROUP);
    final PropertyModel<TeamCalDO> selectModel = new PropertyModel<TeamCalDO>(data, "calendar");
    final DropDownChoice<TeamCalDO> teamCalDrop = new DropDownChoice<TeamCalDO>(newFieldset.getDropDownChoiceId(),
        selectModel, list, getLabeledList(list)){
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
        else
          return check;
      }
    };
    teamCalDrop.setNullValid(false);
    teamCalDrop.setRequired(true);
    newFieldset.add(teamCalDrop);
  }

  /**
   * @param newFieldset
   */
  @SuppressWarnings("serial")
  private void setPeriodPanel(final FieldsetPanel newFieldset)
  {
    add(new IFormValidator() {

      public org.apache.wicket.markup.html.form.FormComponent<?>[] getDependentFormComponents() {
        return null;};

        @Override
        public void validate(final Form< ? > form)
        {
          final DateHolder startDate = new DateHolder(startDateTimePanel.getConvertedInput());
          final DateHolder stopDate = new DateHolder(startDate.getTimestamp());
          stopDate.setHourOfDay(stopHourOfDayDropDownChoice.getConvertedInput());
          stopDate.setMinute(stopMinuteDropDownChoice.getConvertedInput());
          if (stopDate.getTimeOfDay() < startDate.getTimeOfDay()) { // Stop time is
            // before start time. Assuming next day for stop time:
            stopDate.add(Calendar.DAY_OF_MONTH, 1);
          }
          final TeamEventDO e = data;
          data.setStartDate(startDate.getTimestamp());
          data.setEndDate(stopDate.getTimestamp());
          if (data.getDuration() < 60000) {
            // Duration is less than 60 seconds.
            stopMinuteDropDownChoice.error(getString("timesheet.error.zeroDuration"));
          } else if (data.getDuration() > TimesheetDao.MAXIMUM_DURATION) {
            stopMinuteDropDownChoice.error(getString("timesheet.error.maximumDurationExceeded"));
          }
        }
    });
    startDateTimePanel = new DateTimePanel(newFieldset.newChildId(), new PropertyModel<Date>(data, "startDate"),
        (DateTimePanelSettings) DateTimePanelSettings.get().withSelectStartStopTime(true).withTargetType(java.sql.Timestamp.class)
        .withRequired(true), DatePrecision.MINUTE_15);
    newFieldset.add(startDateTimePanel);
    WicketUtils.addTooltip(startDateTimePanel.getDateField(), new Model<String>() {
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
    newFieldset.add(new DivTextPanel(newFieldset.newChildId(), getString("until")));
    // Stop time
    stopHourOfDayDropDownChoice = new DropDownChoice<Integer>(newFieldset.getDropDownChoiceId(),
        new PropertyModel<Integer>(this, "stopHourOfDay"), DateTimePanel.getHourOfDayRenderer().getValues(),
        DateTimePanel.getHourOfDayRenderer());
    stopHourOfDayDropDownChoice.setNullValid(false);
    stopHourOfDayDropDownChoice.setRequired(true);
    newFieldset.add(stopHourOfDayDropDownChoice);
    stopMinuteDropDownChoice = new DropDownChoice<Integer>(newFieldset.getDropDownChoiceId(),
        new PropertyModel<Integer>(this, "stopMinute"), DateTimePanel.getMinutesRenderer(DatePrecision.MINUTE_15).getValues(),
        DateTimePanel.getMinutesRenderer(DatePrecision.MINUTE_15));
    stopMinuteDropDownChoice.setNullValid(false);
    stopMinuteDropDownChoice.setRequired(true);
    newFieldset.add(stopMinuteDropDownChoice);
  }

  private LabelValueChoiceRenderer<TeamCalDO> getLabeledList(final List<TeamCalDO> list) {
    final LabelValueChoiceRenderer<TeamCalDO> templateNamesChoiceRenderer = new LabelValueChoiceRenderer<TeamCalDO>();
    for (final TeamCalDO t : list) {
      templateNamesChoiceRenderer.addValue(t, t.getTitle());
    }
    return templateNamesChoiceRenderer;
  }

  /**
   * @return the allDay
   */
  public boolean isAllDay()
  {
    return allDay;
  }

  /**
   * @param allDay the allDay to set
   * @return this for chaining.
   */
  public void setAllDay(final boolean allDay)
  {
    this.allDay = allDay;
  }

  @Override
  public void onBeforeRender()
  {
    super.onBeforeRender();
    final DateHolder stopDateHolder = new DateHolder(data.getEndDate(), DatePrecision.MINUTE_15);
    stopHourOfDay = stopDateHolder.getHourOfDay();
    stopMinute = stopDateHolder.getMinute();
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
