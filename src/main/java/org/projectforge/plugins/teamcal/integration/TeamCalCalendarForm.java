/////////////////////////////////////////////////////////////////////////////
//
// Project ProjectForge Community Edition
//         www.projectforge.org
//
// Copyright (C) 2001-2013 Kai Reinhard (k.reinhard@micromata.de)
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

package org.projectforge.plugins.teamcal.integration;

import java.util.List;
import java.util.Set;

import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.Component;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.IChoiceRenderer;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.projectforge.common.StringHelper;
import org.projectforge.plugins.teamcal.dialog.TeamCalFilterDialog;
import org.projectforge.plugins.teamcal.event.TeamEventListPage;
import org.projectforge.plugins.teamcal.event.importics.DropIcsPanel;
import org.projectforge.web.calendar.CalendarForm;
import org.projectforge.web.calendar.CalendarPage;
import org.projectforge.web.calendar.CalendarPageSupport;
import org.projectforge.web.calendar.ICalendarFilter;
import org.projectforge.web.dialog.ModalDialog;
import org.projectforge.web.wicket.flowlayout.AjaxIconButtonPanel;
import org.projectforge.web.wicket.flowlayout.DropDownChoicePanel;
import org.projectforge.web.wicket.flowlayout.IconButtonPanel;
import org.projectforge.web.wicket.flowlayout.IconType;

/**
 * @author Johannes Unterstein (j.unterstein@micromata.de)
 * @author M. Lauterbach (m.lauterbach@micromata.de)
 */
public class TeamCalCalendarForm extends CalendarForm
{

  private static final long serialVersionUID = -5838203593605203398L;
  private MyErrorDialog errorDialog;

  /**
   * @param parentPage
   */
  public TeamCalCalendarForm(final CalendarPage parentPage)
  {
    super(parentPage);
  }

  /**
   * @see org.projectforge.web.calendar.CalendarForm#createCalendarPageSupport()
   */
  @Override
  protected CalendarPageSupport createCalendarPageSupport()
  {
    return new CalendarPageSupport(parentPage).setShowOptions(false).setShowTimsheetsSelectors(false);
  }

  /**
   * @see org.projectforge.web.calendar.CalendarForm#init()
   */
  @SuppressWarnings("serial")
  @Override
  protected void init()
  {
    super.init();
    {
      final IconButtonPanel searchButtonPanel = new IconButtonPanel(buttonGroupPanel.newChildId(), IconType.SEARCH, new ResourceModel(
          "search")) {
        /**
         * @see org.projectforge.web.wicket.flowlayout.IconButtonPanel#onSubmit()
         */
        @Override
        protected void onSubmit()
        {
          final Set<Integer> visibleCalsSet = ((TeamCalCalendarFilter) filter).getActiveVisibleCalendarIds();
          final String calendars = StringHelper.objectColToString(visibleCalsSet, ",");
          final TeamEventListPage teamEventListPage = new TeamEventListPage(new PageParameters().add(TeamEventListPage.PARAM_CALENDARS,
              calendars));
          setResponsePage(teamEventListPage);
        }
      };
      searchButtonPanel.setDefaultFormProcessing(false);
      buttonGroupPanel.addButton(searchButtonPanel);
    }

    if (((TeamCalCalendarFilter) filter).getActiveTemplateEntry() != null) {
      final IChoiceRenderer<TemplateEntry> templateEntriesRenderer = new IChoiceRenderer<TemplateEntry>() {
        private static final long serialVersionUID = 4804134958242438331L;

        @Override
        public String getIdValue(final TemplateEntry object, final int index)
        {
          return object.getName();
        }

        @Override
        public Object getDisplayValue(final TemplateEntry object)
        {
          return object.getName();
        }
      };

      final IModel<List<TemplateEntry>> choicesModel = new PropertyModel<List<TemplateEntry>>(filter, "templateEntries");
      final IModel<TemplateEntry> activeModel = new PropertyModel<TemplateEntry>(filter, "activeTemplateEntry");
      final DropDownChoicePanel<TemplateEntry> templateChoice = new DropDownChoicePanel<TemplateEntry>(fieldset.newChildId(), activeModel,
          choicesModel, templateEntriesRenderer, false);
      fieldset.add(templateChoice);
      templateChoice.getDropDownChoice().setOutputMarkupId(true);

      templateChoice.getDropDownChoice().add(new AjaxFormComponentUpdatingBehavior("onChange") {
        private static final long serialVersionUID = 8999698636114154230L;

        /**
         * @see org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior#onUpdate(org.apache.wicket.ajax.AjaxRequestTarget)
         */
        @Override
        protected void onUpdate(final AjaxRequestTarget target)
        {
          filter.setSelectedCalendar(TemplateEntry.calcCalendarStringForCalendar(activeModel.getObject().getDefaultCalendarId()));
          setResponsePage(getParentPage().getClass());
        }
      });

      fieldset.add(new DropIcsPanel(fieldset.newChildId()) {

        @Override
        protected void onIcsImport(final AjaxRequestTarget target, final Calendar calendar)
        {
          // TODO kai: do fancy stuff with the calendar

          @SuppressWarnings("unchecked")
          final List<Component> list = calendar.getComponents("VEVENT");
          // if (calendar.getComponent(name))
          if (list == null || list.size() == 0) {
            errorDialog.open(target, getString("plugins.teamcal.import.noEventsGiven"));
            return;
          }

          // Temporary not used, because multiple events are not supported.
          //          final List<VEvent> events = new ArrayList<VEvent>();
          //          for (final Component c : list) {
          //            final VEvent event = new VEvent(c.getProperties());
          //
          //            if (StringUtils.equals(event.getSummary().getValue(), CalendarFeed.SETUP_EVENT) == true) {
          //              // skip setup event!
          //              continue;
          //            }
          //            events.add(event);
          //          }

          // TODO change to events.size() if multiple events are supported.
          if (list.size() > 1) {
            errorDialog.open(target, getString("plugins.teamcal.import.multipleEventsNotYetSupported"));
            return;
          }
          // 1. Check id/external id. If not yet given, create new entry and ask for calendar to add: Redirect to TeamEventEditPage.
          // 2. If already exists open edit dialog with DiffAcceptDiscardPanels.
        }
      });
    }
  }

  /**
   * @see org.apache.wicket.Component#onInitialize()
   */
  @SuppressWarnings("serial")
  @Override
  protected void onInitialize()
  {
    final TeamCalFilterDialog dialog = new TeamCalFilterDialog(parentPage.newModalDialogId(), (TeamCalCalendarFilter) filter);
    parentPage.add(dialog);
    dialog.init();
    final IconButtonPanel calendarButtonPanel = new AjaxIconButtonPanel(buttonGroupPanel.newChildId(), IconType.CALENDAR,
        new ResourceModel("plugins.teamcal.calendar.edit")) {
      /**
       * @see org.projectforge.web.wicket.flowlayout.AjaxIconButtonPanel#onSubmit(org.apache.wicket.ajax.AjaxRequestTarget)
       */
      @Override
      protected void onSubmit(final AjaxRequestTarget target)
      {
        dialog.open(target);
        // Redraw the content:
        dialog.redraw().addContent(target);
      }
    };
    errorDialog = new MyErrorDialog(parentPage.newModalDialogId());
    parentPage.add(errorDialog);
    errorDialog.init();
    calendarButtonPanel.setDefaultFormProcessing(false);
    buttonGroupPanel.addButton(calendarButtonPanel);
    super.onInitialize();
  }

  @Override
  public TeamCalCalendarFilter getFilter()
  {
    return (TeamCalCalendarFilter) filter;
  }

  @Override
  protected void setFilter(final ICalendarFilter filter)
  {
    this.filter = filter;
  }

  /**
   * @return the selectedCalendars
   */
  public Set<Integer> getSelectedCalendars()
  {
    return ((TeamCalCalendarFilter) filter).getActiveVisibleCalendarIds();
  }

  private class MyErrorDialog extends ModalDialog {
    private static final long serialVersionUID = -5934755033487813064L;

    /**
     * @param id
     */
    public MyErrorDialog(final String id)
    {
      super(id);
    }

    /**
     * @see org.projectforge.web.dialog.ModalDialog#onInitialize()
     */
    @Override
    protected void onInitialize()
    {
      super.onInitialize();
      setTitle(getString("plugins.teamcal.import.ics.error"));
    }

    @Override
    public void init()
    {
      final Form<Void> form = new Form<Void>(getFormId());
      init(form);
    }

    public MyErrorDialog open(final AjaxRequestTarget target, final String s) {
      super.open(target);
      ajaxError(s, target);
      return this;
    }
  }
}
