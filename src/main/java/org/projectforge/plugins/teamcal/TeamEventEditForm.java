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
import org.projectforge.web.wicket.flowlayout.DivTextPanel;
import org.projectforge.web.wicket.flowlayout.FieldsetPanel;

import com.google.code.jqwicket.ui.colorpicker.ColorPickerOptions;
import com.google.code.jqwicket.ui.colorpicker.ColorPickerTextField;

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

  @SpringBean(name = "userGroupCache")
  private UserGroupCache userGroupCache;

  @SuppressWarnings("unused")
  private int stopHourOfDay, stopMinute;

  private DateTimePanel startDateTimePanel;

  private DropDownChoice<Integer> stopHourOfDayDropDownChoice;

  private DropDownChoice<Integer> stopMinuteDropDownChoice;

  private String colorCode;

  private boolean access;

  private PropertyModel<FieldsetPanel> dateFieldsetModel;

  private FieldsetPanel dateFieldSet;

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

    final TeamCalDO teamCal = data.getCalendar();
    final TeamCalRight right = new TeamCalRight();
    if (isNew() == true || teamCal.getOwner() == null) {
      access = true;
    } else {
      if (right.hasUpdateAccess(getUser(), teamCal, teamCal) == true)
        access = true;
      else
        if (right.hasAccessGroup(teamCal.getReadOnlyAccessGroup(), userGroupCache, getUser()) == true)
          access = false;
        else
          if (right.hasAccessGroup(teamCal.getMinimalAccessGroup(), userGroupCache, getUser()) == true) {
            final TeamEventDO newTeamEventDO = new TeamEventDO();
            newTeamEventDO.setId(data.getId());
            newTeamEventDO.setStartDate(data.getStartDate());
            newTeamEventDO.setEndDate(data.getEndDate());
            data = newTeamEventDO;
            access = false;
          } else
            access = false;
    }

    initTeamCalPicker(gridBuilder.newFieldset(getString("plugins.teamevent.teamCal"), true));

    {
      final FieldsetPanel fieldSet = gridBuilder.newFieldset(getString("plugins.teamevent.subject"));
      final MaxLengthTextField subjectField = new MaxLengthTextField(fieldSet.getTextFieldId(), new PropertyModel<String>(data, "subject"));
      subjectField.setRequired(true);
      fieldSet.add(subjectField);
      if (access == false)
        fieldSet.setEnabled(false);
    }
    {
      final FieldsetPanel fieldSet = gridBuilder.newFieldset(getString("plugins.teamevent.note"));
      final MaxLengthTextArea noteField = new MaxLengthTextArea(fieldSet.getTextAreaId(), new PropertyModel<String>(data, "note"));
      fieldSet.add(noteField);
      if (access == false)
        fieldSet.setEnabled(false);
    }

    dateFieldSet = gridBuilder.newFieldset(getString("plugins.teamevent.duration"), true);
    dateFieldsetModel = new PropertyModel<FieldsetPanel>(this, "dateFieldSet");
    initPeriodPanel();

    {
      final FieldsetPanel fieldSet = gridBuilder.newFieldset(getString("plugins.teamevent.location"));
      @SuppressWarnings("serial")
      final PFAutoCompleteMaxLengthTextField locationTextField = new PFAutoCompleteMaxLengthTextField(fieldSet.getTextFieldId(),
          new PropertyModel<String>(data, "location")){

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

    {
      final FieldsetPanel fieldSet = gridBuilder.newFieldset("", true);
      final DivPanel checkBoxPanel = fieldSet.addNewCheckBoxDiv();
      checkBoxPanel.add(new CheckBoxPanel(checkBoxPanel.newChildId(), new PropertyModel<Boolean>(data, "allDay"), getString("plugins.teamevent.allDay")){
        /**
         * 
         */
        private static final long serialVersionUID = 930499365286491752L;

        /**
         * @see org.projectforge.web.wicket.flowlayout.CheckBoxPanel#onSelectionChanged(java.lang.Boolean)
         */
        @Override
        protected void onSelectionChanged(final Boolean newSelection)
        {
          // TODO
          super.onSelectionChanged(newSelection);
          if (newSelection == true) {
            final FieldsetPanel panel = dateFieldsetModel.getObject();
            panel.setEnabled(false);
            dateFieldsetModel.setObject(panel);
            dateFieldsetModel.detach();
            dateFieldSet.modelChanged();
          } else {
            final FieldsetPanel panel = dateFieldsetModel.getObject();
            panel.setEnabled(false);
            dateFieldsetModel.setObject(panel);
            dateFieldsetModel.detach();
            dateFieldSet.modelChanged();
          }
        }
      });
      fieldSet.add(checkBoxPanel);
      if (access == false)
        fieldSet.setEnabled(false);
    }

    initColorPicker(gridBuilder.newFieldset(getString("plugins.teamevent.duration"), true));
  }

  /**
   * @return the dateFieldSet
   */
  public FieldsetPanel getDateFieldSet()
  {
    return dateFieldSet;
  }

  /**
   * @param dateFieldSet the dateFieldSet to set
   * @return this for chaining.
   */
  public void setDateFieldSet(final FieldsetPanel dateFieldSet)
  {
    this.dateFieldSet = dateFieldSet;
  }

  /**
   * @param newFieldset
   */
  private void initColorPicker(final FieldsetPanel newFieldset)
  {
    final ColorPickerOptions options = new ColorPickerOptions();
    options.livePreview(true);
    options.flat(true);
    @SuppressWarnings("serial")
    final ColorPickerTextField<String> picker =
    new ColorPickerTextField<String>(newFieldset.getTextFieldId(), new PropertyModel<String>(this, "colorCode"), options){
    };
    picker.show();
    newFieldset.add(picker);
  }

  /**
   * @param fieldSet
   */
  @SuppressWarnings("serial")
  private void initTeamCalPicker(final FieldsetPanel fieldSet)
  {
    final List<TeamCalDO> list = teamCalDao.getTeamCalsByAccess(getUser(), TeamCalDao.FULL_ACCESS_GROUP);
    final PropertyModel<TeamCalDO> selectModel = new PropertyModel<TeamCalDO>(data, "calendar");
    final DropDownChoice<TeamCalDO> teamCalDrop = new DropDownChoice<TeamCalDO>(fieldSet.getDropDownChoiceId(),
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
    fieldSet.add(teamCalDrop);
    if (access == false)
      fieldSet.setEnabled(false);
  }

  /**
   * @param dateFieldSet
   */
  @SuppressWarnings("serial")
  private void initPeriodPanel()
  {
    if (data.isAllDay() == false) {
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
            data.setStartDate(startDate.getTimestamp());
            data.setEndDate(stopDate.getTimestamp());
            //            if (data.getDuration() < 60000) {
            //              // Duration is less than 60 seconds.
            //              stopMinuteDropDownChoice.error(getString("timesheet.error.zeroDuration"));
            //            }
            //            else
            //              if (data.getDuration() > TimesheetDao.MAXIMUM_DURATION) {
            //                stopMinuteDropDownChoice.error(getString("timesheet.error.maximumDurationExceeded"));
            //              }
          }
      });
    }
    startDateTimePanel = new DateTimePanel(dateFieldSet.newChildId(), new PropertyModel<Date>(data, "startDate"),
        (DateTimePanelSettings) DateTimePanelSettings.get().withSelectStartStopTime(true).withTargetType(java.sql.Timestamp.class)
        .withRequired(true), DatePrecision.MINUTE_15);
    dateFieldSet.add(startDateTimePanel);
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
    dateFieldSet.add(new DivTextPanel(dateFieldSet.newChildId(), getString("until")));
    // Stop time
    stopHourOfDayDropDownChoice = new DropDownChoice<Integer>(dateFieldSet.getDropDownChoiceId(),
        new PropertyModel<Integer>(this, "stopHourOfDay"), DateTimePanel.getHourOfDayRenderer().getValues(),
        DateTimePanel.getHourOfDayRenderer());
    stopHourOfDayDropDownChoice.setNullValid(false);
    stopHourOfDayDropDownChoice.setRequired(true);
    dateFieldSet.add(stopHourOfDayDropDownChoice);
    stopMinuteDropDownChoice = new DropDownChoice<Integer>(dateFieldSet.getDropDownChoiceId(),
        new PropertyModel<Integer>(this, "stopMinute"), DateTimePanel.getMinutesRenderer(DatePrecision.MINUTE_15).getValues(),
        DateTimePanel.getMinutesRenderer(DatePrecision.MINUTE_15));
    stopMinuteDropDownChoice.setNullValid(false);
    stopMinuteDropDownChoice.setRequired(true);
    dateFieldSet.add(stopMinuteDropDownChoice);
    if (access == false || data.isAllDay())
      dateFieldSet.setEnabled(false);
  }

  private LabelValueChoiceRenderer<TeamCalDO> getLabeledList(final List<TeamCalDO> list) {
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
    // TODO continue color picker
    //    final JQContributionConfig config = new JQContributionConfig().withDefaultJQueryUi();
    //    {
    //      @Override
    //      public JavaScriptResourceReference getJqueryCoreJsResourceReference(){
    //        return new JavaScriptResourceReference(WicketApplication.class, "jquery.js");
    //      }
    //    };
    //    getApplication().getComponentPreOnBeforeRenderListeners().add(new JQComponentOnBeforeRenderListener(config));


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

  /**
   * @return the colorCode
   */
  public String getColorCode()
  {
    return colorCode;
  }

  /**
   * @param colorCode the colorCode to set
   * @return this for chaining.
   */
  public void setColorCode(final String colorCode)
  {
    this.colorCode = colorCode;
  }

}
