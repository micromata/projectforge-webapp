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

package org.projectforge.plugins.teamcal.dialog;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.extensions.markup.html.form.select.IOptionRenderer;
import org.apache.wicket.extensions.markup.html.form.select.Select;
import org.apache.wicket.extensions.markup.html.form.select.SelectOptions;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.IChoiceRenderer;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.projectforge.plugins.teamcal.admin.TeamCalDO;
import org.projectforge.plugins.teamcal.admin.TeamCalDao;
import org.projectforge.plugins.teamcal.event.TeamEventDao;
import org.projectforge.plugins.teamcal.event.TeamEventRight;
import org.projectforge.plugins.teamcal.integration.TeamCalCalendarFilter;
import org.projectforge.plugins.teamcal.integration.TemplateCalendarProperties;
import org.projectforge.plugins.teamcal.integration.TemplateEntry;
import org.projectforge.user.PFUserContext;
import org.projectforge.user.PFUserDO;
import org.projectforge.user.UserRights;
import org.projectforge.web.common.ColorPickerPanel;
import org.projectforge.web.dialog.PFDialog;
import org.projectforge.web.wicket.components.SingleButtonPanel;
import org.projectforge.web.wicket.flowlayout.AjaxIconButtonPanel;
import org.projectforge.web.wicket.flowlayout.CheckBoxPanel;
import org.projectforge.web.wicket.flowlayout.DropDownChoicePanel;
import org.projectforge.web.wicket.flowlayout.IconButtonPanel;
import org.projectforge.web.wicket.flowlayout.IconType;

import com.vaynberg.wicket.select2.Select2MultiChoice;

import de.micromata.wicket.ajax.AjaxCallback;

/**
 * @author M. Lauterbach (m.lauterbach@micromata.de)
 * 
 */
public class TeamCalDialog extends PFDialog
{
  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(TeamCalDialog.class);

  private static final long serialVersionUID = 8687197318833240410L;

  private final List<TeamCalDO> selectedCalendars;

  private DropDownChoicePanel<TemplateEntry> templateChoice;

  // Adaption (fake) to display "Time Sheets" as selection option
  public static final TeamCalDO TIMESHEET_CALENDAR = new TeamCalDO();

  private final TeamCalCalendarFilter filter;

  private TeamCalCalendarFilter backupFilter;

  public static final Integer TIMESHEET_CALENDAR_ID = -1;

  @SpringBean
  private TeamCalDao teamCalDao;

  private final TeamEventRight teamEventRight;

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
    teamEventRight = (TeamEventRight) UserRights.instance().getRight(TeamEventDao.USER_RIGHT_ID);
    setOnCloseCallback(new AjaxCallback() {
      private static final long serialVersionUID = -8154276568761839693L;

      @Override
      public void callback(final AjaxRequestTarget target)
      {
        myClose(target);
      }
    });
  }

  /**
   * @see org.projectforge.web.dialog.PFDialog#open(org.apache.wicket.ajax.AjaxRequestTarget)
   */
  @Override
  public void open(final AjaxRequestTarget target)
  {
    backupFilter = new TeamCalCalendarFilter().copyValuesFrom(filter);
    super.open(target);
  }

  /**
   * @see org.projectforge.web.dialog.PFDialog#isRefreshedOnOpen()
   */
  @Override
  protected boolean isRefreshedOnOpen()
  {
    return true;
  }

  /**
   * @see org.projectforge.web.dialog.PFDialog#onInitialize()
   */
  @Override
  protected void onInitialize()
  {
    super.onInitialize();
    TIMESHEET_CALENDAR.setTitle(getString("plugins.teamcal.timeSheetCalendar"));
    TIMESHEET_CALENDAR.setId(TIMESHEET_CALENDAR_ID);

    appendNewAjaxActionButton(new AjaxCallback() {
      private static final long serialVersionUID = -8154276568761839693L;

      @Override
      public void callback(final AjaxRequestTarget target)
      {
        // Restore values (valid at opening time of this dialog):
        filter.copyValuesFrom(backupFilter);
        TeamCalDialog.this.close(target);
      }
    }, getString("cancel"), SingleButtonPanel.CANCEL);

    // confirm
    // TODO: Tooltip über schließen: Kannst final auch irgendwo hinneklicken
    appendNewAjaxActionButton(new AjaxCallback() {
      private static final long serialVersionUID = -8741086877308855477L;

      @Override
      public void callback(final AjaxRequestTarget target)
      {
        myClose(target);
        TeamCalDialog.this.close(target);
      }
    }, getString("close"), SingleButtonPanel.DEFAULT_SUBMIT);
  }

  private void myClose(final AjaxRequestTarget target)
  {
    if (filter.isModified(backupFilter) == false) {
      // Do nothing.
      return;
    }
    final TemplateEntry activeTemplateEntry = filter.getActiveTemplateEntry();
    if (activeTemplateEntry != null) {
      activeTemplateEntry.setDirty();
    }
    setResponsePage(getPage().getClass(), getPage().getPageParameters());
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

    private Select<Integer> select;

    private Select2MultiChoice<TeamCalDO> teamCalChoice;

    private TeamCalNameDialog nameDialog;

    private AjaxCallback currentAjaxCallback;

    private String currentName;

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

      final WebMarkupContainer bottomContainer = new WebMarkupContainer("bottomContainer");
      bottomContainer.setOutputMarkupId(true);
      bottomContainer.setOutputMarkupPlaceholderTag(true);
      add(bottomContainer);

      // hide if no templates available
      bottomContainer.setVisible(filter.getTemplateEntries().isEmpty() == false);

      // COLOR TABLE
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
          final TemplateEntry activeTemplateEntry = filter.getActiveTemplateEntry();
          for (final TeamCalDO calendar : selectedCalendars) {
            final WebMarkupContainer container = new WebMarkupContainer(calendarRepeater.newChildId());
            calendarRepeater.add(container);
            final IModel<Boolean> model = Model.of(activeTemplateEntry.isVisible(calendar.getId()) == true);
            final CheckBoxPanel checkBoxPanel = new CheckBoxPanel("isVisible", model, "");
            checkBoxPanel.getCheckBox().add(new AjaxFormComponentUpdatingBehavior("onChange") {
              private static final long serialVersionUID = 3523446385818267608L;

              @Override
              protected void onUpdate(final AjaxRequestTarget target)
              {
                final Boolean newSelection = checkBoxPanel.getCheckBox().getConvertedInput();
                final TemplateCalendarProperties properties = activeTemplateEntry.getCalendarProperties(calendar.getId());
                if (newSelection != properties.isVisible()) {
                  properties.setVisible(newSelection);
                  activeTemplateEntry.setDirty();
                }
              }
            });
            container.add(checkBoxPanel);
            container.add(new Label("name", calendar.getTitle()));
            final ColorPickerPanel picker = new ColorPickerPanel("colorPicker", activeTemplateEntry.getColorCode(calendar.getId())) {
              private static final long serialVersionUID = 7221351523462540744L;

              @Override
              protected void onColorUpdate(final String selectedColor)
              {
                final TemplateCalendarProperties props = activeTemplateEntry.getCalendarProperties(calendar.getId());
                if (props != null) {
                  props.setColorCode(selectedColor);
                } else {
                  log.warn("TeamCalendarProperties not found: calendar.id='"
                      + calendar.getId()
                      + "' + for active template '"
                      + activeTemplateEntry.getName()
                      + "'.");
                }
              }
            };
            container.add(picker);
          }
        }
      };
      repeaterContainer.setOutputMarkupId(true);
      repeaterContainer.add(calendarRepeater);
      bottomContainer.add(repeaterContainer);

      final IChoiceRenderer<TemplateEntry> teamCalCollectionRenderer = new IChoiceRenderer<TemplateEntry>() {
        private static final long serialVersionUID = 613794318110089990L;

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

      // TEMPLATEENTRY DROPDOWN
      final IModel<List<TemplateEntry>> choicesModel = new PropertyModel<List<TemplateEntry>>(filter, "templateEntries");
      final IModel<TemplateEntry> currentModel = new PropertyModel<TemplateEntry>(filter, "activeTemplateEntry");
      templateChoice = new DropDownChoicePanel<TemplateEntry>("templateList", currentModel, choicesModel, teamCalCollectionRenderer, false);
      add(templateChoice);
      templateChoice.getDropDownChoice().setOutputMarkupId(true);

      templateChoice.getDropDownChoice().add(new AjaxFormComponentUpdatingBehavior("onChange") {
        private static final long serialVersionUID = 8999698636114154230L;

        /**
         * @see org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior#onUpdate(org.apache.wicket.ajax.AjaxRequestTarget)
         */
        @Override
        protected void onUpdate(final AjaxRequestTarget target)
        {
          selectedCalendars.clear();
          final TemplateEntry activeTemplateEntry = filter.getActiveTemplateEntry();
          selectedCalendars.addAll(activeTemplateEntry.getCalendars());
          addToTarget(target, templateChoice.getDropDownChoice(), repeaterContainer, select, teamCalChoice);
        }
      });

      // ADD BUTTON FOR Template
      final IconButtonPanel addTemplateButton = new AjaxIconButtonPanel("addTemplate", IconType.PLUS_THICK, getString("add")) {
        private static final long serialVersionUID = -8572571785540159369L;

        /**
         * @see org.projectforge.web.wicket.flowlayout.AjaxIconButtonPanel#onSubmit(org.apache.wicket.ajax.AjaxRequestTarget)
         */
        @Override
        protected void onSubmit(final AjaxRequestTarget target)
        {
          currentName = "";
          // this callback is evaluated when the name dialog was entered!
          currentAjaxCallback = new AjaxCallback() {
            private static final long serialVersionUID = -6959790939627419710L;

            @Override
            public void callback(final AjaxRequestTarget target)
            {
              final TemplateEntry newTemplate = new TemplateEntry();
              newTemplate.setName(currentName);
              filter.add(newTemplate);
              selectedCalendars.clear();
              selectedCalendars.addAll(newTemplate.getCalendars());
            }
          };
          nameDialog.open(target);
        }
      };
      addTemplateButton.setDefaultFormProcessing(false);
      addTemplateButton.setLight();
      add(addTemplateButton);

      // EDIT BUTTON
      final IconButtonPanel editTemplateButton = new AjaxIconButtonPanel("editTemplate", IconType.WRENCH, getString("edit")) {
        private static final long serialVersionUID = -8572571785540159369L;

        /**
         * @see org.projectforge.web.wicket.flowlayout.AjaxIconButtonPanel#onSubmit(org.apache.wicket.ajax.AjaxRequestTarget)
         */
        @Override
        protected void onSubmit(final AjaxRequestTarget target)
        {
          final TemplateEntry activeTemplateEntry = filter.getActiveTemplateEntry();
          if (activeTemplateEntry != null) {
            currentName = activeTemplateEntry.getName();
            nameDialog.open(target);
            // this callback is evaluated when the name dialog was entered!
            currentAjaxCallback = new AjaxCallback() {
              private static final long serialVersionUID = -6959790939627419710L;

              @Override
              public void callback(final AjaxRequestTarget target)
              {
                if (currentName.equals(filter.getActiveTemplateEntry().getName()) == false) {
                  filter.getActiveTemplateEntry().setName(currentName);
                }
              }
            };
          }
        }
      };
      editTemplateButton.setDefaultFormProcessing(false);
      editTemplateButton.setLight();
      add(editTemplateButton);

      //
      // DELETE BUTTON
      final IconButtonPanel deleteTemplateButton = new AjaxIconButtonPanel("deleteTemplate", IconType.TRASH, getString("delete")) {
        private static final long serialVersionUID = -8572571785540159369L;

        /**
         * @see org.projectforge.web.wicket.flowlayout.AjaxIconButtonPanel#onSubmit(org.apache.wicket.ajax.AjaxRequestTarget)
         */
        @Override
        protected void onSubmit(final AjaxRequestTarget target)
        {
          filter.remove(filter.getActiveTemplateEntry());
          selectedCalendars.clear();
          selectedCalendars.addAll(filter.getActiveTemplateEntry().getCalendars());
          addToTarget(target, templateChoice.getDropDownChoice(), repeaterContainer, select, teamCalChoice);
        }
      };
      deleteTemplateButton.setDefaultFormProcessing(false);
      deleteTemplateButton.setLight();
      add(deleteTemplateButton);

      // hide if no templates available
      editTemplateButton.setVisible(filter.getTemplateEntries().isEmpty() == false);

      final TemplateEntry activeTemplateEntry = filter.getActiveTemplateEntry();
      selectedCalendars.clear();
      if (activeTemplateEntry != null) {
        selectedCalendars.addAll(activeTemplateEntry.getCalendars());
      }
      // TEAMCAL CHOICE FIELD
      final TeamCalChoiceProvider teamProvider = new TeamCalChoiceProvider();
      teamCalChoice = new Select2MultiChoice<TeamCalDO>("choices", new PropertyModel<Collection<TeamCalDO>>(TeamCalDialog.this,
          "selectedCalendars"), teamProvider);
      teamCalChoice.setOutputMarkupId(true);
      teamCalChoice.add(new AjaxFormComponentUpdatingBehavior("onChange") {
        private static final long serialVersionUID = 1L;

        @Override
        protected void onUpdate(final AjaxRequestTarget target)
        {
          final TemplateEntry activeTemplateEntry = filter.getActiveTemplateEntry();
          final Set<Integer> oldCalIds = activeTemplateEntry.getCalendarIds();
          final List<Integer> newIds = new LinkedList<Integer>();
          // add new keys
          for (final TeamCalDO calendar : selectedCalendars) {
            if (oldCalIds.contains(calendar.getId()) == false) {
              activeTemplateEntry.addNewCalendarProperties(filter, calendar.getId());
            }
            newIds.add(calendar.getId());
          }
          // delete removed keys
          for (final Integer key : oldCalIds) {
            if (newIds.contains(key) == false) {
              activeTemplateEntry.removeCalendarProperties(key);
            }
          }
          // because onBeforeRender is overwritten, just add the components
          addToTarget(target, templateChoice.getDropDownChoice(), repeaterContainer, select, teamCalChoice);
        }
      });
      bottomContainer.add(teamCalChoice);

      final IOptionRenderer<Integer> renderer = new IOptionRenderer<Integer>() {
        private static final long serialVersionUID = 4233157357375064338L;

        @Override
        public String getDisplayValue(final Integer object)
        {
          if (TIMESHEET_CALENDAR_ID.equals(object)) {
            return TIMESHEET_CALENDAR.getTitle();
          }
          return teamCalDao.getById(object).getTitle();
        }

        @Override
        public IModel<Integer> getModel(final Integer value)
        {
          return Model.of(value);
        }
      };

      // TEAMCAL DROPDOWN
      final Form<Void> defaultForm = new Form<Void>("defaultForm");
      add(defaultForm);
      final IModel<TemplateEntry> activeTemplateEntryModel = new PropertyModel<TemplateEntry>(filter, "activeTemplateEntry");
      select = new Select<Integer>("defaultSelect", new PropertyModel<Integer>(activeTemplateEntryModel, "defaultCalendarId")) {
        private static final long serialVersionUID = -1826120411566623945L;

        /**
         * @see org.apache.wicket.Component#onBeforeRender()
         */
        @Override
        protected void onBeforeRender()
        {
          super.onBeforeRender();
          final TemplateEntry activeTemplateEntry = filter.getActiveTemplateEntry();
          final List<TeamCalDO> result = new ArrayList<TeamCalDO>();
          if (activeTemplateEntry != null) {
            for (final TeamCalDO cal : activeTemplateEntry.getCalendars()) {
              if (teamEventRight.hasUpdateAccess(PFUserContext.getUser(), cal) == true) {
                result.add(cal);
              }
            }
          }
          final PFUserDO user = PFUserContext.getUser();
          final List<Integer> filteredList = new ArrayList<Integer>();
          filteredList.add(0, TIMESHEET_CALENDAR_ID);
          if (result != null) {
            // remove teamCals where user has less than full access or is not owner.
            final Iterator<TeamCalDO> it = result.iterator();
            while (it.hasNext()) {
              final TeamCalDO teamCal = it.next();
              if (teamCalDao.hasUpdateAccess(user, teamCal, null, false) == true) {
                filteredList.add(teamCal.getId());
              }
            }
          }
          final SelectOptions<Integer> options = new SelectOptions<Integer>("options", filteredList, renderer);
          select.addOrReplace(options);
        }

      };
      select.add(new AjaxFormComponentUpdatingBehavior("onChange") {
        private static final long serialVersionUID = -5677673442072775133L;

        @Override
        protected void onUpdate(final AjaxRequestTarget target)
        {
          filter.setSelectedCalendar(TemplateEntry.calcCalendarStringForCalendar(filter.getActiveTemplateEntry().getDefaultCalendarId()));
        }
      });
      select.setOutputMarkupId(true);
      defaultForm.add(select);

      nameDialog = new TeamCalNameDialog("nameDialog", new ResourceModel("plugins.teamcal.title.list"), new PropertyModel<String>(
          Content.this, "currentName")) {
        private static final long serialVersionUID = 95566184649574010L;

        @Override
        protected void onConfirm(final AjaxRequestTarget target)
        {
          nameDialog.close(target);
          currentAjaxCallback.callback(target);
          templateChoice.getDropDownChoice().setChoices(filter.getTemplateEntries());
          templateChoice.getDropDownChoice().setDefaultModel(new PropertyModel<TemplateEntry>(filter, "activeTemplateEntry"));
          bottomContainer.setVisible(filter.getTemplateEntries().isEmpty() == false);
          editTemplateButton.setVisible(filter.getTemplateEntries().isEmpty() == false);
          addToTarget(target, templateChoice.getDropDownChoice(), bottomContainer, select);
        }

        /**
         * @see org.projectforge.plugins.teamcal.dialog.TeamCalNameDialog#onError(org.apache.wicket.ajax.AjaxRequestTarget)
         */
        @Override
        protected void onError(final AjaxRequestTarget target)
        {
          target.add(dialogContainer);
        }
      };
      add(nameDialog);
    }

    private void addToTarget(final AjaxRequestTarget target, final Component... components)
    {
      for (final Component c : components) {
        target.add(c);
      }
    }
  }

}
