/////////////////////////////////////////////////////////////////////////////
//
// Project   ProjectForge
//
// Copyright 2001-2009, Micromata GmbH, Kai Reinhard
//           All rights reserved.
//
/////////////////////////////////////////////////////////////////////////////

package org.projectforge.plugins.teamcal.dialog;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.extensions.markup.html.form.select.IOptionRenderer;
import org.apache.wicket.extensions.markup.html.form.select.Select;
import org.apache.wicket.extensions.markup.html.form.select.SelectOptions;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.projectforge.plugins.teamcal.admin.TeamCalDO;
import org.projectforge.plugins.teamcal.admin.TeamCalDao;
import org.projectforge.plugins.teamcal.event.TeamCalEventProvider;
import org.projectforge.plugins.teamcal.integration.TeamCalCalendarFilter;
import org.projectforge.web.common.ColorPickerPanel;
import org.projectforge.web.dialog.PFDialog;
import org.projectforge.web.timesheet.TimesheetEventsProvider;

import com.vaynberg.wicket.select2.Select2MultiChoice;

/**
 * @author M. Lauterbach (m.lauterbach@micromata.de)
 * 
 */
public class TeamCalDialog extends PFDialog
{
  private static final long serialVersionUID = 8687197318833240410L;

  private final List<TeamCalDO> selectedCalendars;

  private TeamCalDO selectedDefaultCalendar;

  private final TeamCalCalendarFilter filter;

  // Adaption (fake) to display "Time Sheets" as selection option
  private final TeamCalDO timeSheetCalendar;

  private static final int TIMESHEET_CALENDAR_ID = -1;

  @SpringBean
  private TeamCalDao teamCalDao;

  /**
   * @param id
   * @param titleModel
   * @param filter
   */
  public TeamCalDialog(final String id, final IModel<String> titleModel, final TeamCalCalendarFilter filter)
  {
    super(id, titleModel);
    this.filter = filter;
    selectedCalendars = new LinkedList<TeamCalDO>();
    timeSheetCalendar = new TeamCalDO();
  }

  /**
   * @see org.projectforge.web.dialog.PFDialog#onInitialize()
   */
  @Override
  protected void onInitialize()
  {
    super.onInitialize();
    timeSheetCalendar.setTitle(getString("plugins.teamcal.timeSheetCalendar"));
    timeSheetCalendar.setId(TIMESHEET_CALENDAR_ID);
    // this assignment is wanted to prevent auto save "final" action
    if(StringUtils.isBlank(filter.getSelectedCalendar()) || TimesheetEventsProvider.EVENT_CLASS_NAME.equals(filter.getSelectedCalendar())) {
      selectedDefaultCalendar = timeSheetCalendar;
    } else {
      // get teamCal
      selectedDefaultCalendar = TeamCalEventProvider.getTeamCalForEncodedId(teamCalDao, filter.getSelectedCalendar());
      if(selectedDefaultCalendar == null) {
        selectedDefaultCalendar = timeSheetCalendar;
      }
    }
  }

  /**
   * @see org.projectforge.web.dialog.PFDialog#getDialogContent(java.lang.String)
   */
  @Override
  protected Component getDialogContent(final String wicketId)
  {
    return new Content(wicketId);
  }

  /**
   * Inner class to represent the actual dialog content
   *
   */
  private class Content extends Panel
  {
    private static final long serialVersionUID = -135497846745050310L;
    private Select<TeamCalDO> select;

    /**
     * @param id
     */
    public Content(final String id)
    {
      super(id);
    }

    /**
     * @see org.apache.wicket.Component#onInitialize()
     */
    @Override
    protected void onInitialize()
    {
      super.onInitialize();
      final RepeatingView calendarRepeater = new RepeatingView("repeater");
      final WebMarkupContainer repeaterContainer = new WebMarkupContainer("repeaterContainer") {
        private static final long serialVersionUID = 7750294984025761480L;

        /**
         * @see org.apache.wicket.Component#onBeforeRender()
         */
        @Override
        protected void onBeforeRender()
        {
          super.onBeforeRender();
          calendarRepeater.removeAll();
          for (final TeamCalDO calendar : selectedCalendars) {
            final WebMarkupContainer container = new WebMarkupContainer(calendarRepeater.newChildId());
            calendarRepeater.add(container);
            container.add(new Label("name", calendar.getTitle()));
            final ColorPickerPanel picker = new ColorPickerPanel("colorPicker", filter.getColor(calendar.getId())) {
              private static final long serialVersionUID = 7221351523462540744L;

              @Override
              protected void onColorUpdate(final String selectedColor)
              {
                filter.updateCalendarColor(calendar.getId(), selectedColor);
              }
            };
            container.add(picker);
          }
        }
      };
      repeaterContainer.setOutputMarkupId(true);
      add(repeaterContainer);
      repeaterContainer.add(calendarRepeater);

      selectedCalendars.addAll(filter.calcAssignedtItems(teamCalDao));

      final TeamCalChoiceProvider teamProvider = new TeamCalChoiceProvider();
      final Select2MultiChoice<TeamCalDO> teamCalChoice = new Select2MultiChoice<TeamCalDO>("choices",
          new PropertyModel<Collection<TeamCalDO>>(TeamCalDialog.this, "selectedCalendars"), teamProvider);
      teamCalChoice.add(new AjaxFormComponentUpdatingBehavior("onChange") {
        private static final long serialVersionUID = 1L;

        @Override
        protected void onUpdate(final AjaxRequestTarget target)
        {
          final Set<Integer> oldKeys = new HashSet<Integer>(filter.getCalendarPk());
          final List<Integer> newKeys = new LinkedList<Integer>();
          // add new keys
          for (final TeamCalDO calendar : selectedCalendars) {
            if (oldKeys.contains(calendar.getId()) == false) {
              filter.addCalendarPk(calendar.getId());
            }
            newKeys.add(calendar.getId());
          }
          // delete removed keys
          for (final Integer key : oldKeys) {
            if (newKeys.contains(key) == false) {
              filter.removeCalendarPk(key);
            }
          }
          // because onBeforeRender is overwritten, just add the components
          target.add(repeaterContainer);
          target.add(select);
        }
      });
      add(teamCalChoice);


      final IOptionRenderer<TeamCalDO> renderer = new IOptionRenderer<TeamCalDO>() {
        private static final long serialVersionUID = 4233157357375064338L;

        @Override
        public String getDisplayValue(final TeamCalDO object)
        {
          return object.getTitle();
        }

        @Override
        public IModel<TeamCalDO> getModel(final TeamCalDO value)
        {
          return Model.of(value);
        }
      };
      final Form<Void> defaultForm = new Form<Void>("defaultForm");
      add(defaultForm);
      select = new Select<TeamCalDO>("defaultSelect", new PropertyModel<TeamCalDO>(TeamCalDialog.this, "selectedDefaultCalendar")) {
        private static final long serialVersionUID = -1826120411566623945L;

        /**
         * @see org.apache.wicket.Component#onBeforeRender()
         */
        @Override
        protected void onBeforeRender()
        {
          super.onBeforeRender();
          final List<TeamCalDO> result = filter.calcAssignedtItems(teamCalDao);
          result.add(0, timeSheetCalendar);
          final SelectOptions<TeamCalDO> options = new SelectOptions<TeamCalDO>("options", result, renderer);
          select.addOrReplace(options);
        }

        /**
         * @see org.apache.wicket.Component#onModelChanged()
         */
        @Override
        protected void onModelChanged()
        {
          super.onModelChanged();
          if(timeSheetCalendar.equals(selectedDefaultCalendar) || selectedDefaultCalendar == null) {
            filter.setSelectedCalendar(TimesheetEventsProvider.EVENT_CLASS_NAME);
          }else{
            filter.setSelectedCalendar(TeamCalEventProvider.EVENT_CLASS_NAME + "-" + selectedDefaultCalendar.getId());
          }
        }
      };
      select.add(new AjaxFormComponentUpdatingBehavior("onChange") {
        private static final long serialVersionUID = -5677673442072775133L;

        @Override
        protected void onUpdate(final AjaxRequestTarget target)
        {
          // just update the model
        }
      });
      add(select);
      select.setOutputMarkupId(true);
      defaultForm.add(select);
    }

  }

}
