/////////////////////////////////////////////////////////////////////////////
//
// Project   ProjectForge
//
// Copyright 2001-2009, Micromata GmbH, Kai Reinhard
//           All rights reserved.
//
/////////////////////////////////////////////////////////////////////////////

package org.projectforge.plugins.teamcal.event.importics;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.LinkedList;
import java.util.List;

import net.fortuna.ical4j.model.component.VEvent;

import org.apache.commons.lang.time.DateFormatUtils;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.ChoiceRenderer;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.projectforge.plugins.teamcal.admin.TeamCalDO;
import org.projectforge.plugins.teamcal.admin.TeamCalDao;
import org.projectforge.plugins.teamcal.event.TeamEventDO;
import org.projectforge.plugins.teamcal.event.TeamEventDao;
import org.projectforge.web.dialog.ModalDialog;
import org.projectforge.web.wicket.flowlayout.DivPanel;
import org.projectforge.web.wicket.flowlayout.FieldsetPanel;

/**
 * @author M. Lauterbach (m.lauterbach@micromata.de)
 * 
 */
public class ImportIcsDialog extends ModalDialog
{
  private static final long serialVersionUID = 4882579653256365873L;

  @SpringBean(name = "teamCalDao")
  private TeamCalDao teamCalDao;

  @SpringBean(name = "teamEventDao")
  private TeamEventDao teamEventDao;

  /**
   * @param id
   */
  public ImportIcsDialog(final String id, final IModel<String> title)
  {
    super(id);
    setTitle(title);
  }

  public void open(final AjaxRequestTarget target, final List<VEvent> newEvents, final List<VEvent> existingEvents)
  {
    super.open(target);
    clearContent();
    final List<EventCalendarPair> calendarPairs = new LinkedList<ImportIcsDialog.EventCalendarPair>();
    final List<TeamCalDO> allCalendars = teamCalDao.getListByUser();

    for (final VEvent event : newEvents) {
      final FieldsetPanel fs = gridBuilder.newFieldset(event.getSummary().getValue());
      final EventCalendarPair referencePair = new EventCalendarPair();
      referencePair.event = event;
      calendarPairs.add(referencePair);
      if (event.getDescription() != null) {
        fs.add(new Label(fs.newChildId(), "Description: " + event.getDescription().getValue()));
      }
      fs.add(new Label(fs.newChildId(), " - "));
      fs.add(new Label(fs.newChildId(), new DateFormatUtils().format(event.getStartDate().getDate(), "dd.MM.yyyy HH:mm"))); // TODO richtiges Format!
      fs.add(new Label(fs.newChildId(), " bis "));
      final ChoiceRenderer<TeamCalDO> choice = new ChoiceRenderer<TeamCalDO>(){
        private static final long serialVersionUID = 424880918380768972L;

        /**
         * @see org.apache.wicket.markup.html.form.ChoiceRenderer#getDisplayValue(java.lang.Object)
         */
        @Override
        public Object getDisplayValue(final TeamCalDO object)
        {
          return object.getTitle();
        }
      };
      final FieldsetPanel fss = gridBuilder.newFieldset(getString("plugins.teamcal.title"));
      final DropDownChoice<TeamCalDO> dropDown = new DropDownChoice<TeamCalDO>(fss.getDropDownChoiceId(), new PropertyModel<TeamCalDO>(referencePair, "calendar"), allCalendars, choice);
      fss.add(dropDown);
    }
    final DivPanel buttonParent = gridBuilder.getPanel();
    // add SingleButton Panel
    final AjaxButton button = new AjaxButton("TODO") {
      /**
       * @see org.apache.wicket.ajax.markup.html.form.AjaxButton#onSubmit(org.apache.wicket.ajax.AjaxRequestTarget, org.apache.wicket.markup.html.form.Form)
       */
      @Override
      protected void onSubmit(final AjaxRequestTarget target, final Form< ? > form)
      {
        onSave(calendarPairs); // TODO add this
      }
    };
    buttonParent.add(button);
    addContent(target);
  }

  /**
   * @see org.projectforge.web.dialog.ModalDialog#init()
   */
  @Override
  public void init()
  {
    setCloseButtonLabel(getString("cancel"));
    final Form<Void> form = new Form<Void>(getFormId());
    init(form);
  }

  private void onSave(final List<EventCalendarPair> pairs)
  {
    TeamEventDO teamEvent;
    for (final EventCalendarPair pair : pairs) {
      final VEvent e = pair.event;
      teamEvent = new TeamEventDO();
      teamEvent.setCalendar(pair.calendar);
      teamEvent.setCreated(e.getCreated().getDate());
      teamEvent.setStartDate(new Timestamp(e.getStartDate().getDate().getTime()));
      teamEvent.setStartDate(new Timestamp(e.getEndDate().getDate().getTime()));
      if (e.getLocation() != null) {
        teamEvent.setLocation(e.getLocation().getValue());
      }
      if (e.getDescription() != null) {
        teamEvent.setNote(e.getDescription().getValue());
      }
      if (e.getSummary() != null) {
        teamEvent.setSubject(e.getSummary().getValue());
      }
      teamEventDao.saveOrUpdate(teamEvent);
    }

  }

  private class EventCalendarPair implements Serializable
  {
    private static final long serialVersionUID = -688491447235814904L;

    private VEvent event;

    private TeamCalDO calendar;

    public EventCalendarPair()
    {
    }

  }
}
