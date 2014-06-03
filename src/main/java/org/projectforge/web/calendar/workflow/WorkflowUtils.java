package org.projectforge.web.calendar.workflow;

import org.apache.wicket.Component;
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
public class WorkflowUtils implements Serializable
{

  public static void addWorkflowPanel(FieldsetPanel fieldsetPanel)
  {
    // workflow SubmitButton
    final AjaxButton workflowSubmitButton = new AjaxButton(ButtonPanel.BUTTON_ID, Model.of(PFUserContext
        .getLocalizedString("workflow.toggle.start"))) {

      @Override
      protected void onSubmit(final AjaxRequestTarget target, final Form< ? > form)
      {
        if (oldTime() != null) {
          submitWorkflow(this);
          MySession.get().setLastWorkflowSubmit(newTime());
        }
      }

      @Override
      public boolean isVisible()
      {
        return MySession.get().getLastWorkflowSubmit() != null;
      }
    };
    workflowSubmitButton.setOutputMarkupId(true);
    workflowSubmitButton.setOutputMarkupPlaceholderTag(true);

    // workflow ToggleButton
    final AjaxButton workflowToggleButton = new AjaxButton(ButtonPanel.BUTTON_ID) {
      @Override
      protected void onSubmit(final AjaxRequestTarget target, final Form< ? > form)
      {
        if (oldTime() == null) {// Start Workflow
          MySession.get().setLastWorkflowSubmit(newTime());
        } else {// Stop Workflow
          submitWorkflow(this);
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
            submitWorkflow(workflowToggleButton);
            MySession.get().setLastWorkflowSubmit(newTime());
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
        return timePeriodString(MySession.get().getLastWorkflowSubmit(), newTime());
      }
    };
    fieldsetPanel.add(new ButtonPanel(fieldsetPanel.newChildId(), submitButtonModel, workflowSubmitButton, ButtonType.GREEN));
  }

  /**
   * @return The last time the User saved his time. Might be null.
   */
  private static Calendar oldTime()
  {
    return MySession.get().getLastWorkflowSubmit();
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
   * @return the string
   * @result String representing the Period
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
   * Submits the current workflow-settings. Also opens a responsePage for further proceeding.
   * 
   * @param component the according component
   */
  private static void submitWorkflow(Component component)
  {
    final TimesheetDO timesheetDO = new TimesheetDO();
    timesheetDO.setStartDate(oldTime().getTimeInMillis());
    timesheetDO.setStopTime(newTime().getTimeInMillis());
    // timesheetDO.setDescription("Created by Workflow");
    final TimesheetEditPage responsePage = new TimesheetEditPage(timesheetDO);
    if (component.getPage() instanceof WebPage) {
      responsePage.setReturnToPage((WebPage) component.getPage());
    }
    component.setResponsePage(responsePage);
    MySession.get().setLastWorkflowSubmit(null);
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
