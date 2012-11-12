/////////////////////////////////////////////////////////////////////////////
//
// Project   ProjectForge
//
// Copyright 2001-2009, Micromata GmbH, Kai Reinhard
//           All rights reserved.
//
/////////////////////////////////////////////////////////////////////////////

package org.projectforge.plugins.teamcal.dialog;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
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
import org.projectforge.plugins.teamcal.admin.TeamCalRight;
import org.projectforge.plugins.teamcal.event.TeamCalEventProvider;
import org.projectforge.plugins.teamcal.integration.TeamCalCalendarCollection;
import org.projectforge.plugins.teamcal.integration.TeamCalCalendarFilter;
import org.projectforge.user.PFUserContext;
import org.projectforge.user.PFUserDO;
import org.projectforge.web.common.ColorPickerPanel;
import org.projectforge.web.dialog.PFDialog;
import org.projectforge.web.timesheet.TimesheetEventsProvider;
import org.projectforge.web.wicket.components.SingleButtonPanel;
import org.projectforge.web.wicket.flowlayout.AjaxIconButtonPanel;
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
  private static final long serialVersionUID = 8687197318833240410L;

  private final List<TeamCalDO> selectedCalendars;

  private TeamCalDO selectedDefaultCalendar;

  private final TeamCalCalendarFilter newFilter;

  private final TeamCalCalendarFilter oldFilter;

  private DropDownChoicePanel<TeamCalCalendarCollection> collectionChoice;

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
    this.oldFilter = filter;
    this.newFilter = new TeamCalCalendarFilter(filter);
    selectedCalendars = new LinkedList<TeamCalDO>();
    timeSheetCalendar = new TeamCalDO();
  }

  /**
   * @see org.projectforge.web.dialog.PFDialog#open(org.apache.wicket.ajax.AjaxRequestTarget)
   */
  @Override
  public void open(final AjaxRequestTarget target)
  {
    newFilter.updateTeamCalendarFilter(oldFilter);
    // this assignment is wanted to prevent auto save "final" action
    if (newFilter.getSelectedCalendar() != null
        || TimesheetEventsProvider.EVENT_CLASS_NAME.equals(newFilter.getSelectedCalendar())) {
      selectedDefaultCalendar = timeSheetCalendar;
    } else {
      // get teamCal
      selectedDefaultCalendar = TeamCalEventProvider.getTeamCalForEncodedId(teamCalDao, newFilter.getSelectedCalendar());
      if (selectedDefaultCalendar == null) {
        selectedDefaultCalendar = timeSheetCalendar;
      }
    }
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
    timeSheetCalendar.setTitle(getString("plugins.teamcal.timeSheetCalendar"));
    timeSheetCalendar.setId(TIMESHEET_CALENDAR_ID);

    // close button
    appendNewAjaxActionButton(new AjaxCallback() {
      private static final long serialVersionUID = -8154276568761839693L;

      @Override
      public void callback(final AjaxRequestTarget target)
      {
        TeamCalDialog.this.close(target);
      }
    }, getString("cancel"), SingleButtonPanel.CANCEL);

    // confirm
    appendNewAjaxActionButton(new AjaxCallback() {
      private static final long serialVersionUID = -8741086877308855477L;

      @Override
      public void callback(final AjaxRequestTarget target)
      {
        oldFilter.updateTeamCalendarFilter(newFilter);
        setResponsePage(getPage().getClass(), getPage().getPageParameters());
      }
    }, getString("change"), SingleButtonPanel.DEFAULT_SUBMIT);
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
            final ColorPickerPanel picker = new ColorPickerPanel("colorPicker", newFilter.getColor(calendar.getId(),
                newFilter.getCurrentCollection())) {
              private static final long serialVersionUID = 7221351523462540744L;

              @Override
              protected void onColorUpdate(final String selectedColor)
              {
                newFilter.updateCalendarColor(calendar.getId(), selectedColor, newFilter.getCurrentCollection());
              }
            };
            container.add(picker);
          }
        }
      };
      repeaterContainer.setOutputMarkupId(true);
      add(repeaterContainer);
      repeaterContainer.add(calendarRepeater);

      final IChoiceRenderer<TeamCalCalendarCollection> teamCalCollectionRenderer = new IChoiceRenderer<TeamCalCalendarCollection>() {
        private static final long serialVersionUID = 613794318110089990L;

        @Override
        public String getIdValue(final TeamCalCalendarCollection object, final int index)
        {
          return object.getTeamCalCalendarColletionName();
        }

        @Override
        public Object getDisplayValue(final TeamCalCalendarCollection object)
        {
          return object.getTeamCalCalendarColletionName();
        }
      };

      // TEAMCALCALENDARCOLLECTION DROPDOWN
      final IModel<List<TeamCalCalendarCollection>> choicesModel = new PropertyModel<List<TeamCalCalendarCollection>>(newFilter, "teamCalCalendarCollection");
      final IModel<TeamCalCalendarCollection> currentModel = new PropertyModel<TeamCalCalendarCollection>(newFilter, "currentCollection");
      collectionChoice = new DropDownChoicePanel<TeamCalCalendarCollection>(
          "collectionList", currentModel, choicesModel, teamCalCollectionRenderer, false);
      add(collectionChoice);
      collectionChoice.getDropDownChoice().setOutputMarkupId(true);

      collectionChoice.getDropDownChoice().add(new AjaxFormComponentUpdatingBehavior("onChange") {
        private static final long serialVersionUID = 8999698636114154230L;

        /**
         * @see org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior#onUpdate(org.apache.wicket.ajax.AjaxRequestTarget)
         */
        @Override
        protected void onUpdate(final AjaxRequestTarget target)
        {
          selectedCalendars.clear();
          selectedCalendars.addAll(newFilter.calcAssignedtItems(teamCalDao, newFilter.getCurrentCollection()));
          addToTarget(target, collectionChoice.getDropDownChoice(), repeaterContainer, select, teamCalChoice);
        }
      });

      // ADD BUTTON FOR TCCC
      final IconButtonPanel addCollectionButton = new AjaxIconButtonPanel("addCollection", IconType.PLUS_THICK,
          getString("add")) {
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
              final TeamCalCalendarCollection addedCollection = new TeamCalCalendarCollection();
              addedCollection.setTeamCalCalendarCollectionName(currentName);
              addedCollection.setCalendarMap(new HashMap<Integer, String>());
              newFilter.getTeamCalCalendarCollection().add(addedCollection);
              newFilter.setCurrentCollection(addedCollection);
              selectedCalendars.clear();
              selectedCalendars.addAll(newFilter.calcAssignedtItems(teamCalDao, addedCollection));
              addToTarget(target, collectionChoice.getDropDownChoice(), repeaterContainer, teamCalChoice);
            }
          };
          nameDialog.open(target);
        }
      };
      addCollectionButton.setDefaultFormProcessing(false);
      addCollectionButton.setLight();
      add(addCollectionButton);

      // EDIT BUTTON FOR TCCC
      final IconButtonPanel editCollectionButton = new AjaxIconButtonPanel("editCollection", IconType.WRENCH,
          getString("edit")) {
        private static final long serialVersionUID = -8572571785540159369L;

        /**
         * @see org.projectforge.web.wicket.flowlayout.AjaxIconButtonPanel#onSubmit(org.apache.wicket.ajax.AjaxRequestTarget)
         */
        @Override
        protected void onSubmit(final AjaxRequestTarget target)
        {
          if (newFilter.getCurrentCollection() != null) {
            // this callback is evaluated when the name dialog was entered!
            currentAjaxCallback = new AjaxCallback() {
              private static final long serialVersionUID = -6959790939627419710L;

              @Override
              public void callback(final AjaxRequestTarget target)
              {
                if (currentName.equals(newFilter.getCurrentCollection().getTeamCalCalendarColletionName()) == false) {
                  newFilter.getTeamCalCalendarCollection().remove(newFilter.getCurrentCollection());
                  newFilter.getCurrentCollection().setTeamCalCalendarCollectionName(currentName);
                  newFilter.getTeamCalCalendarCollection().add(newFilter.getCurrentCollection());
                  addToTarget(target, collectionChoice.getDropDownChoice(), repeaterContainer, teamCalChoice);
                }
              }
            };
            currentName = newFilter.getCurrentCollection().getTeamCalCalendarColletionName();
            nameDialog.open(target);
          }
        }
      };
      editCollectionButton.setDefaultFormProcessing(false);
      editCollectionButton.setLight();
      add(editCollectionButton);

      selectedCalendars.clear();
      selectedCalendars.addAll(newFilter.calcAssignedtItems(teamCalDao, newFilter.getCurrentCollection()));

      // TEAMCAL CHOICE FIELD
      final TeamCalChoiceProvider teamProvider = new TeamCalChoiceProvider();
      teamCalChoice = new Select2MultiChoice<TeamCalDO>("choices",
          new PropertyModel<Collection<TeamCalDO>>(TeamCalDialog.this, "selectedCalendars"), teamProvider);
      teamCalChoice.setOutputMarkupId(true);
      teamCalChoice.add(new AjaxFormComponentUpdatingBehavior("onChange") {
        private static final long serialVersionUID = 1L;

        @Override
        protected void onUpdate(final AjaxRequestTarget target)
        {
          final Set<Integer> oldKeys = new HashSet<Integer>(newFilter.getCalendarPk(newFilter.getCurrentCollection()));
          final List<Integer> newKeys = new LinkedList<Integer>();
          // add new keys
          for (final TeamCalDO calendar : selectedCalendars) {
            if (oldKeys.contains(calendar.getId()) == false) {
              newFilter.addCalendarPk(calendar.getId(), newFilter.getCurrentCollection());
            }
            newKeys.add(calendar.getId());
          }
          // delete removed keys
          for (final Integer key : oldKeys) {
            if (newKeys.contains(key) == false) {
              newFilter.removeCalendarPk(key, newFilter.getCurrentCollection());
            }
          }
          // because onBeforeRender is overwritten, just add the components
          addToTarget(target, collectionChoice.getDropDownChoice(), repeaterContainer, select, teamCalChoice);
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

      // TEAMCAL DROPDOWN
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
          final List<TeamCalDO> result = newFilter.calcAssignedtItems(teamCalDao, newFilter.getCurrentCollection());
          final TeamCalRight teamCalRight = new TeamCalRight();
          final PFUserDO user = PFUserContext.getUser();
          final List<TeamCalDO> filteredList = new ArrayList<TeamCalDO>();
          filteredList.add(0, timeSheetCalendar);
          if (result != null) {
            // remove teamCals where user has less than full access or is not owner.
            final Iterator<TeamCalDO> it = result.iterator();
            while (it.hasNext()) {
              final TeamCalDO teamCal = it.next();
              if (teamCalRight.isOwner(user, teamCal) == true) {
                filteredList.add(teamCal);
              } else {
                if (teamCal.getFullAccessGroup() != null) {
                  if (teamCalRight.isMemberOfAtLeastOneGroup(user, teamCal.getFullAccessGroupId()) == true) {
                    filteredList.add(teamCal);
                  }
                }
              }
            }
          }
          final SelectOptions<TeamCalDO> options = new SelectOptions<TeamCalDO>("options", filteredList, renderer);
          select.addOrReplace(options);
        }

        /**
         * @see org.apache.wicket.Component#onModelChanged()
         */
        @Override
        protected void onModelChanged()
        {
          super.onModelChanged();
          if (timeSheetCalendar.equals(selectedDefaultCalendar) || selectedDefaultCalendar == null) {
            newFilter.setSelectedCalendar(TimesheetEventsProvider.EVENT_CLASS_NAME);
          } else {
            newFilter.setSelectedCalendar(TeamCalEventProvider.EVENT_CLASS_NAME + "-" + selectedDefaultCalendar.getId());
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

      nameDialog = new TeamCalNameDialog("nameDialog", new ResourceModel("plugins.teamcal.title.list"), new PropertyModel<String>(Content.this, "currentName")){
        private static final long serialVersionUID = 95566184649574010L;

        @Override
        protected void onConfirm(final AjaxRequestTarget target)
        {
          nameDialog.close(target);
          currentAjaxCallback.callback(target);
          collectionChoice.getDropDownChoice().setChoices(newFilter.getTeamCalCalendarCollection());
          collectionChoice.getDropDownChoice().setDefaultModel(new PropertyModel<TeamCalCalendarCollection>(newFilter, "currentCollection"));
          addToTarget(target, collectionChoice.getDropDownChoice(), repeaterContainer, select, teamCalChoice);
        }
      };
      add(nameDialog);
    }

    private void addToTarget(final AjaxRequestTarget target, final Component... components) {
      for (final Component c: components) {
        target.add(c);
      }
    }
  }

}
