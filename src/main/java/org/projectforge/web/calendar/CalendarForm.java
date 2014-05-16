/////////////////////////////////////////////////////////////////////////////
//
// Project ProjectForge Community Edition
//         www.projectforge.org
//
// Copyright (C) 2001-2014 Kai Reinhard (k.reinhard@micromata.de)
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

package org.projectforge.web.calendar;

import java.util.Calendar;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.AjaxSelfUpdatingTimerBehavior;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.apache.wicket.util.time.Duration;
import org.joda.time.DateMidnight;
import org.projectforge.timesheet.TimesheetDO;
import org.projectforge.user.PFUserContext;
import org.projectforge.user.PFUserDO;
import org.projectforge.user.UserDao;
import org.projectforge.user.UserGroupCache;
import org.projectforge.web.timesheet.TimesheetEditPage;
import org.projectforge.web.wicket.AbstractStandardForm;
import org.projectforge.web.wicket.WicketUtils;
import org.projectforge.web.wicket.bootstrap.GridBuilder;
import org.projectforge.web.wicket.bootstrap.GridSize;
import org.projectforge.web.wicket.components.DateTimePanel;
import org.projectforge.web.wicket.components.JodaDatePanel;
import org.projectforge.web.wicket.flowlayout.ButtonGroupPanel;
import org.projectforge.web.wicket.flowlayout.ButtonPanel;
import org.projectforge.web.wicket.flowlayout.ButtonType;
import org.projectforge.web.wicket.flowlayout.CheckBoxButton;
import org.projectforge.web.wicket.flowlayout.DivPanel;
import org.projectforge.web.wicket.flowlayout.DivTextPanel;
import org.projectforge.web.wicket.flowlayout.FieldsetPanel;
import org.projectforge.web.wicket.flowlayout.IconButtonPanel;
import org.projectforge.web.wicket.flowlayout.IconType;

public class CalendarForm extends AbstractStandardForm<CalendarFilter, CalendarPage>
{
  private static final long serialVersionUID = -145923669780937370L;

  @SpringBean(name = "userGroupCache")
  private UserGroupCache userGroupCache;

  @SpringBean(name = "userDao")
  protected UserDao userDao;

  protected ICalendarFilter filter;

  private JodaDatePanel currentDatePanel;

  private Label durationLabel;

  protected ButtonGroupPanel buttonGroupPanel;

  protected FieldsetPanel fieldset;

  protected CalendarPageSupport createCalendarPageSupport()
  {
    final CalendarPageSupport calendarPageSupport = new CalendarPageSupport(parentPage);
    return calendarPageSupport;
  }

  @SuppressWarnings("serial")
  @Override
  protected void init()
  {
    super.init();
    gridBuilder.newSplitPanel(GridSize.SPAN6);
    fieldset = gridBuilder.newFieldset(getString("label.options"));
    final CalendarPageSupport calendarPageSupport = createCalendarPageSupport();
    calendarPageSupport.addUserSelectPanel(fieldset, new PropertyModel<PFUserDO>(this, "timesheetsUser"), true);
    currentDatePanel = new JodaDatePanel(fieldset.newChildId(), new PropertyModel<DateMidnight>(filter, "startDate")).setAutosubmit(true);
    currentDatePanel.getDateField().setOutputMarkupId(true);
    fieldset.add(currentDatePanel);

    final DropDownChoice<Integer> firstHourDropDownChoice = new DropDownChoice<Integer>(fieldset.getDropDownChoiceId(),
        new PropertyModel<Integer>(filter, "firstHour"), DateTimePanel.getHourOfDayRenderer().getValues(),
        DateTimePanel.getHourOfDayRenderer()) {
      /**
       * @see org.apache.wicket.markup.html.form.DropDownChoice#wantOnSelectionChangedNotifications()
       */
      @Override
      protected boolean wantOnSelectionChangedNotifications()
      {
        return true;
      }
    };
    firstHourDropDownChoice.setNullValid(false);
    firstHourDropDownChoice.setRequired(true);
    WicketUtils.addTooltip(firstHourDropDownChoice, getString("calendar.option.firstHour.tooltip"));
    fieldset.add(firstHourDropDownChoice);

    final DivPanel checkBoxPanel = fieldset.addNewCheckBoxButtonDiv();

    calendarPageSupport.addOptions(checkBoxPanel, true, filter);
    checkBoxPanel.add(new CheckBoxButton(checkBoxPanel.newChildId(), new PropertyModel<Boolean>(filter, "slot30"),
        getString("calendar.option.slot30"), true).setTooltip(getString("calendar.option.slot30.tooltip")));

    buttonGroupPanel = new ButtonGroupPanel(fieldset.newChildId());
    fieldset.add(buttonGroupPanel);
    {
      final IconButtonPanel refreshButtonPanel = new IconButtonPanel(buttonGroupPanel.newChildId(), IconType.REFRESH,
          getRefreshIconTooltip()) {
        /**
         * @see org.projectforge.web.wicket.flowlayout.IconButtonPanel#onSubmit()
         */
        @Override
        protected void onSubmit()
        {
          parentPage.refresh();
          setResponsePage(getPage().getClass(), getPage().getPageParameters());
        }
      };
      buttonGroupPanel.addButton(refreshButtonPanel);
      setDefaultButton(refreshButtonPanel.getButton());
    }
    gridBuilder.newSplitPanel(GridSize.SPAN3);
    final FieldsetPanel fs = gridBuilder.newFieldset(getString("timesheet.duration")).suppressLabelForWarning();
    final DivTextPanel durationPanel = new DivTextPanel(fs.newChildId(), new Label(DivTextPanel.WICKET_ID, new Model<String>() {
      @Override
      public String getObject()
      {
        return parentPage.calendarPanel.getTotalTimesheetDuration();
      }
    }));
    durationLabel = durationPanel.getLabel4Ajax();
    fs.add(durationPanel);

    gridBuilder.newSplitPanel(GridSize.SPAN3);


    // workflow SubmitButton
    final FieldsetPanel workflowFs = gridBuilder.newFieldset("Workflow");
    workflowFs.setLabelFor(new Label("a","b"));
    final AjaxButton workflowSubmitButton = new AjaxButton(ButtonPanel.BUTTON_ID,new Model<String>(PFUserContext.getLocalizedString("workflow.toggle.submit"))){
      @Override
      protected void onSubmit(final AjaxRequestTarget target, final Form< ? > form)
      {
        if(oldTime()!=null){
          SubmitWorkflow(PFUserContext.getUser().getLastWorkflowSubmit(), newTime());
          PFUserContext.getUser().setLastWorkflowSubmit(newTime());
        }
      }
    };
    workflowSubmitButton.setVisible(PFUserContext.getUser().getLastWorkflowSubmit()!=null);
    workflowSubmitButton.setOutputMarkupId(true);
    workflowSubmitButton.setOutputMarkupPlaceholderTag(true);


    // workflow ToggleButton
    final AjaxButton workflowToggleButton = new AjaxButton(ButtonPanel.BUTTON_ID,new Model<String>("TEST3")) {
      @Override
      protected void onSubmit(final AjaxRequestTarget target, final Form< ? > form)
      {
        if(oldTime()==null){//Start Workflow
          PFUserContext.getUser().setLastWorkflowSubmit(newTime());
          updateButtonSubmit(workflowSubmitButton, true);
        }
        else{//Stop Workflow
          SubmitWorkflow(newTime(),oldTime());
        }
        updateButtonToggle(this);
        target.add(this);
        target.add(workflowSubmitButton);
      }
    };
    workflowToggleButton.setOutputMarkupId(true);
    workflowToggleButton.setOutputMarkupPlaceholderTag(true);
    //Auto-updating of the Time on the button and similar
    final AjaxSelfUpdatingTimerBehavior ajaxSelfUpdatingTimerBehavior=new AjaxSelfUpdatingTimerBehavior(Duration.seconds(30)){

      @Override
      protected void onPostProcessTarget(final AjaxRequestTarget target)
      {
        updateLabeling();
        target.add(workflowSubmitButton);
      }
      /*
       * Updates time on the button
       */
      void updateLabeling(){
        if(oldTime()!=null){
          ((AjaxButton)getComponent()).replace(new Label("title", TimePeriodString(oldTime(),newTime())));
          if(oldTime().get(Calendar.DAY_OF_YEAR)!=newTime().get(Calendar.DAY_OF_YEAR)||oldTime().get(Calendar.YEAR)!=newTime().get(Calendar.YEAR)){
            //There are no entries over 2 days or more
            SubmitWorkflow(oldTime(), newTime());
            PFUserContext.getUser().setLastWorkflowSubmit(newTime());
          }
        }
      }
    };
    workflowSubmitButton.add(ajaxSelfUpdatingTimerBehavior);

    workflowFs.add(new ButtonPanel(workflowFs.newChildId(), "This Text get overwritten", workflowToggleButton, ButtonType.DARK));
    workflowFs.add(new ButtonPanel(workflowFs.newChildId(), "This Text get overwritten", workflowSubmitButton, ButtonType.GREEN));
    updateButtonToggle(workflowToggleButton);
    updateButtonSubmit(workflowSubmitButton, oldTime()!=null);

    onAfterInit(gridBuilder);
  }

  /*
   * @return The last time the User saved his time. Might be null.
   */
  Calendar oldTime(){
    return PFUserContext.getUser().getLastWorkflowSubmit();
  }

  /*
   * @return The current time, rounded to 5-minute steps
   */
  Calendar newTime(){
    return RoundTo5Minutes(Calendar.getInstance(PFUserContext.getTimeZone()));
  }

  /*
   * @param calendar The Calendar that has to be changed
   * @return The calendar, set to the last midnight
   */
  Calendar CalendarToMidnight(final Calendar calendar){
    calendar.set(Calendar.HOUR, 0);
    calendar.set(Calendar.MINUTE, 0);
    calendar.set(Calendar.SECOND, 0);
    calendar.set(Calendar.MILLISECOND, 0);
    return calendar;
  }

  /*
   * @param workflowSubmitButton The AjaxButton that should be updated.
   * @param visibility The new visibility.
   */
  void updateButtonSubmit(final AjaxButton workflowSubmitButton, final boolean visibility){
    workflowSubmitButton.setVisible(visibility);
    workflowSubmitButton.replace(new Label("title", TimePeriodString(PFUserContext.getUser().getLastWorkflowSubmit(),newTime())));
  }

  /*
   * @param workflowToggleButton The AjaxButton that should be updated.
   * @return The new text on the button.
   */
  String updateButtonToggle(final AjaxButton workflowToggleButton){
    final String result = PFUserContext.getLocalizedString(oldTime()==null?"workflow.toggle.start":"workflow.toggle.stop");
    workflowToggleButton.replace(new Label("title", result));
    return result;
  }

  /*
   * @param start The Start
   * @param stop The Stop
   * @result String representing the Period
   */
  String TimePeriodString(final Calendar start,final Calendar stop){
    final DateTimeFormatter dtf = new DateTimeFormatter();
    final boolean timeStored = (start!=null);
    if(timeStored){
      return dtf.getFormattedTime(start.getTime())+" "+PFUserContext.getLocalizedString("workflow.timeTo")+" "+dtf.getFormattedTime(stop.getTime());
    }
    return "";
  }

  /*
   * Submits the current workflow-settings. Also opens a responsePage for further proceeding.
   * @param start The Start
   * @param stop The Stop
   */
  void SubmitWorkflow(final Calendar start,final Calendar stop){
    final TimesheetDO timesheetDO = new TimesheetDO();
    timesheetDO.setStartDate(start.getTimeInMillis());
    timesheetDO.setStopTime(stop.getTimeInMillis());
    timesheetDO.setDescription("Created by Workflow");
    final TimesheetEditPage responsePage = new TimesheetEditPage(timesheetDO);
    responsePage.setReturnToPage((WebPage) getPage());
    setResponsePage(responsePage);
    PFUserContext.getUser().removeLastWorkflowSubmit();
  }

  /*
   *Rounds a date up or down to the next 5-minute mark
   *@param Date that should be rounded
   */
  public static Calendar RoundTo5Minutes(final Calendar date){
    if(date==null){
      return null;
    }
    int minutes = date.get(Calendar.MINUTE);
    final int seconds = date.get(Calendar.SECOND);
    if((minutes%5==2&&seconds>=20)||minutes%5>2){
      minutes+=5;
    }
    minutes-=minutes%5;
    date.set(Calendar.MINUTE,minutes);
    date.set(Calendar.SECOND, 0);
    date.set(Calendar.MILLISECOND, 0);
    return date;
  }

  protected String getRefreshIconTooltip()
  {
    return getString("refresh");
  }

  /**
   * Hook method where child implementations could place their logic
   * 
   * @param gridBuilder
   */
  protected void onAfterInit(final GridBuilder gridBuilder)
  {
    // by default nothing happens here
  }

  public CalendarForm(final CalendarPage parentPage)
  {
    super(parentPage);
  }

  public ICalendarFilter getFilter()
  {
    return filter;
  }

  protected void setFilter(final ICalendarFilter filter)
  {
    this.filter = filter;
  }

  @Override
  public PFUserDO getUser()
  {
    final Integer userId = getFilter().getTimesheetUserId();
    return userId != null ? userGroupCache.getUser(userId) : null;
  }

  public void setTimesheetsUser(final PFUserDO user)
  {
    if (user == null) {
      getFilter().setTimesheetUserId(null);
    } else {
      getFilter().setTimesheetUserId(user.getId());
    }
  }

  /**
   * @return the currentDatePanel
   */
  public JodaDatePanel getCurrentDatePanel()
  {
    return currentDatePanel;
  }

  /**
   * @return the durationLabel
   */
  public Label getDurationLabel()
  {
    return durationLabel;
  }

}
