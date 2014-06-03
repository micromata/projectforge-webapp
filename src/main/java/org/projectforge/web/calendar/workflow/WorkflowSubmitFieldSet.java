package org.projectforge.web.calendar.workflow;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.AjaxSelfUpdatingTimerBehavior;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.time.Duration;
import org.projectforge.timesheet.TimesheetDO;
import org.projectforge.user.PFUserContext;
import org.projectforge.user.UserXmlPreferencesCache;
import org.projectforge.web.calendar.DateTimeFormatter;
import org.projectforge.web.timesheet.TimesheetEditPage;
import org.projectforge.web.wicket.MySession;
import org.projectforge.web.wicket.flowlayout.ButtonPanel;
import org.projectforge.web.wicket.flowlayout.ButtonType;
import org.projectforge.web.wicket.flowlayout.FieldsetPanel;

import java.io.Serializable;
import java.util.Calendar;

/**
 * Utils around the workflow handling of time sheet creation.
 * 
 * @author Julius Hege (julheg@gmx.de)
 * @author Johannes Unterstein (j.unterstein@micromata.de)
 */
public class WorkflowSubmitFieldSet implements Serializable
{
  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(WorkflowSubmitFieldSet.class);

  private static final String WORKFLOW_LAST_SUBMIT_KEY = "WORKFLOW_LAST_SUBMIT_KEY";

  private final UserXmlPreferencesCache userXmlPreferencesCache;

  private final WebPage accordingWebPage;

  public WorkflowSubmitFieldSet(FieldsetPanel fieldsetPanel, final UserXmlPreferencesCache userXmlPreferencesCache, WebPage accordingWebPage)
  {
    this.userXmlPreferencesCache = userXmlPreferencesCache;
    this.accordingWebPage = accordingWebPage;
    // workflow SubmitButton
    final AjaxButton workflowSubmitButton = new AjaxButton(ButtonPanel.BUTTON_ID, Model.of(PFUserContext
        .getLocalizedString("workflow.toggle.start"))) {

      @Override
      protected void onSubmit(final AjaxRequestTarget target, final Form< ? > form)
      {
        if (oldTime() != null) {
          submitWorkflow();
          setLastWorkflowSubmitToCurrentTime();
        }
      }

      @Override
      public boolean isVisible()
      {
        return oldTime() != null;
      }
    };
    workflowSubmitButton.setOutputMarkupId(true);
    workflowSubmitButton.setOutputMarkupPlaceholderTag(true);

    // workflow ToggleButton
    final AjaxButton workflowToggleButton = new AjaxButton(ButtonPanel.BUTTON_ID) {
      @Override
      protected void onSubmit(final AjaxRequestTarget target, final Form< ? > form)
      {
        if (oldTime() == null) {
          // Start Workflow
          setLastWorkflowSubmitToCurrentTime();
        } else {
          // Stop Workflow
          submitWorkflow();
        }
        target.add(this);
        target.add(workflowSubmitButton);
      }
    };
    workflowToggleButton.setOutputMarkupId(true);
    workflowToggleButton.setOutputMarkupPlaceholderTag(true);
    // Auto-updating of the Time on the button and similar
    final AjaxSelfUpdatingTimerBehavior ajaxSelfUpdatingTimerBehavior = new AjaxSelfUpdatingTimerBehavior(Duration.seconds(30)) {

      @Override
      protected void onPostProcessTarget(final AjaxRequestTarget target)
      {
        updateLabeling();
        target.add(workflowSubmitButton);
      }

      /**
       * Updates time on the button
       */
      void updateLabeling()
      {
        if (oldTime() != null) {
          ((AjaxButton) getComponent()).replace(new Label("title", timePeriodString(oldTime(), newTime())));
          if (oldTime().get(Calendar.DAY_OF_YEAR) != newTime().get(Calendar.DAY_OF_YEAR)
              || oldTime().get(Calendar.YEAR) != newTime().get(Calendar.YEAR)) {
            // There are no entries over 2 days or more
            submitWorkflow();
            setLastWorkflowSubmitToCurrentTime();
          }
        }
      }
    };
    workflowSubmitButton.add(ajaxSelfUpdatingTimerBehavior);

    IModel<String> toggleButtonModel = new AbstractReadOnlyModel<String>() {

      @Override
      public String getObject()
      {
        return PFUserContext.getLocalizedString(oldTime() == null ? "workflow.toggle.start" : "workflow.toggle.stop");
      }
    };
    fieldsetPanel.add(new ButtonPanel(fieldsetPanel.newChildId(), toggleButtonModel, workflowToggleButton, ButtonType.DARK));

    IModel<String> submitButtonModel = new AbstractReadOnlyModel<String>() {

      @Override
      public String getObject()
      {
        return timePeriodString(oldTime(), newTime());
      }
    };
    fieldsetPanel.add(new ButtonPanel(fieldsetPanel.newChildId(), submitButtonModel, workflowSubmitButton, ButtonType.GREEN));
  }

  /**
   * Sets last workflow submit.
   */
  private void setLastWorkflowSubmitToCurrentTime()
  {
    userXmlPreferencesCache.putEntry(MySession.get().getUserId(), WORKFLOW_LAST_SUBMIT_KEY, newTime().getTimeInMillis(), true);
  }

  /**
   * Submits the current workflow-settings. Also opens a responsePage for further proceeding.
   */
  private void submitWorkflow()
  {
    final TimesheetDO timesheetDO = new TimesheetDO();
    timesheetDO.setStartDate(oldTime().getTimeInMillis());
    timesheetDO.setStopTime(newTime().getTimeInMillis());
    // timesheetDO.setDescription("Created by Workflow");
    final TimesheetEditPage responsePage = new TimesheetEditPage(timesheetDO);
    responsePage.setReturnToPage(accordingWebPage);
    accordingWebPage.setResponsePage(responsePage);
    userXmlPreferencesCache.removeEntry(MySession.get().getUserId(), WORKFLOW_LAST_SUBMIT_KEY);
  }

  /**
   * @return The last time the User saved his time. Might be null.
   */
  private Calendar oldTime()
  {
    Object fromCache = userXmlPreferencesCache.getEntry(MySession.get().getUserId(), WORKFLOW_LAST_SUBMIT_KEY);
    if (fromCache == null) {
      return null;
    } else {
      try {
        Calendar calendar = Calendar.getInstance(PFUserContext.getTimeZone());
        calendar.setTimeInMillis(Long.parseLong(fromCache.toString()));
        return calendar;
      } catch (Exception e) {
        log.warn("Unable to read stored value 'lastWorkflowSubmit' from userXmlPreferencesCache", e);
        return null;
      }
    }
  }

  /**
   * @return The current time, rounded to 5-minute steps
   */
  private static Calendar newTime()
  {
    return roundTo5Minutes(Calendar.getInstance(PFUserContext.getTimeZone()));
  }

  /**
   * Time period string.
   * 
   * @param start the start
   * @param stop the stop
   * @return the String representing the Period
   */
  private static String timePeriodString(Calendar start, Calendar stop)
  {
    final DateTimeFormatter dtf = new DateTimeFormatter();
    final boolean timeStored = (start != null);
    if (timeStored) {
      return dtf.getFormattedTime(start.getTime())
          + " "
          + PFUserContext.getLocalizedString("workflow.timeTo")
          + " "
          + dtf.getFormattedTime(stop.getTime());
    }
    return "";
  }

  /**
   * Rounds a date up or down to the next 5-minute mark
   * 
   * @param date that should be rounded
   */
  private static Calendar roundTo5Minutes(final Calendar date)
  {
    if (date == null) {
      return null;
    }
    int minutes = date.get(Calendar.MINUTE);
    final int seconds = date.get(Calendar.SECOND);
    if ((minutes % 5 == 2 && seconds >= 20) || minutes % 5 > 2) {
      minutes += 5;
    }
    minutes -= minutes % 5;
    date.set(Calendar.MINUTE, minutes);
    date.set(Calendar.SECOND, 0);
    date.set(Calendar.MILLISECOND, 0);
    return date;
  }
}
