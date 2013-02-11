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
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.ChoiceRenderer;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.projectforge.common.TimeNotation;
import org.projectforge.plugins.teamcal.admin.TeamCalDO;
import org.projectforge.plugins.teamcal.admin.TeamCalDao;
import org.projectforge.plugins.teamcal.event.TeamEventDO;
import org.projectforge.plugins.teamcal.event.TeamEventDao;
import org.projectforge.user.PFUserContext;
import org.projectforge.web.dialog.ModalDialog;
import org.projectforge.web.wicket.bootstrap.GridSize;
import org.projectforge.web.wicket.bootstrap.GridType;
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

  private List<EventCalendarPair> calendarPairs;

  private String userDateFormat;

  // needed to convert TimeNotation to suitable date format
  private static final String H12 = " hh:mm";

  private static final String H24 = " HH:mm";

  // needed if user has no date settings
  private static final String DEFAULT_DATE_FORMAT = "dd.MM.yyyy HH:mm";

  /**
   * @param id
   */
  public ImportIcsDialog(final String id, final IModel<String> title)
  {
    super(id);
    setTitle(title);

    // set date format
    final String timeFormat = PFUserContext.getUser().getTimeNotation().equals(TimeNotation.H12) ? H12 : H24;
    userDateFormat = PFUserContext.getUser().getDateFormat() + timeFormat;
    if (userDateFormat == null){
      userDateFormat = DEFAULT_DATE_FORMAT;
    }
  }

  public void open(final AjaxRequestTarget target, final List<VEvent> newEvents, final List<VEvent> existingEvents)
  {
    super.open(target);
    clearContent();
    calendarPairs = new LinkedList<ImportIcsDialog.EventCalendarPair>();

    // TODO Fall behandeln: "wenn noch keine kalender angelegt , z.b. neuen erzeugen."
    final List<TeamCalDO> allCalendars = teamCalDao.getAllCalendarsWithFullAccess();

    for (final VEvent event : newEvents) {
      gridBuilder.newSplitPanel(GridSize.COL100, GridType.ROW_FLUID);
      final FieldsetPanel fsDesc = gridBuilder.newFieldset(event.getSummary().getValue()).supressLabelForWarning();
      final EventCalendarPair referencePair = new EventCalendarPair();
      referencePair.event = event;
      calendarPairs.add(referencePair);
      String desc = "";
      if (event.getDescription() != null) {
        desc = event.getDescription().getValue();
      }
      fsDesc.add(new Label(fsDesc.newChildId(), getString("plugins.teamcal.description") + ": " + desc));

      final FieldsetPanel fsDuration = gridBuilder.newFieldset("");
      fsDuration.add(new Label(fsDuration.newChildId(), DateFormatUtils.format(event.getStartDate().getDate(), userDateFormat)));
      fsDuration.add(new Label(fsDuration.newChildId(), " " + getString("until") + " "));
      fsDuration.add(new Label(fsDuration.newChildId(), DateFormatUtils.format(event.getEndDate().getDate(), userDateFormat)));
      final ChoiceRenderer<TeamCalDO> choice = new ChoiceRenderer<TeamCalDO>() {
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
      final FieldsetPanel fsCalChoose = gridBuilder.newFieldset(getString("plugins.teamcal.title.heading"));
      final DropDownChoice<TeamCalDO> dropDown = new DropDownChoice<TeamCalDO>(fsCalChoose.getDropDownChoiceId(),
          new PropertyModel<TeamCalDO>(referencePair, "calendar"), allCalendars, choice);
      fsCalChoose.add(dropDown);
    }
    addContent(target);
  }

  @Override
  protected boolean onCloseButtonSubmit(final AjaxRequestTarget target)
  {
    TeamEventDO teamEvent;
    if (calendarPairs != null) {
      for (final EventCalendarPair pair : calendarPairs) {
        if (pair.calendar != null) {
          final VEvent e = pair.event;
          teamEvent = new TeamEventDO();
          teamEvent.setCalendar(pair.calendar);
          teamEvent.setStartDate(new Timestamp(e.getStartDate().getDate().getTime()));
          teamEvent.setEndDate(new Timestamp(e.getEndDate().getDate().getTime()));
          if (e.getCreated() != null) {
            teamEvent.setCreated(e.getCreated().getDate());
          }
          if (e.getUid() != null) {
            teamEvent.setExternalUid(e.getUid().getValue());
          }
          if (e.getLocation() != null) {
            teamEvent.setLocation(e.getLocation().getValue());
          }
          if (e.getDescription() != null) {
            teamEvent.setNote(e.getDescription().getValue());
          }
          if (e.getSummary() != null) {
            teamEvent.setSubject(e.getSummary().getValue());
          } else {
            teamEvent.setSubject(getString(""));
          }
          if (teamEvent.getStartDate().getTime() < teamEvent.getEndDate().getTime()) {
            teamEventDao.saveOrUpdate(teamEvent);
          }
        } else {
          ajaxError(getString("plugins.teamcal.import.ics.noCalError"), target);
          return false;
        }
      }
    }
    return true;
  }

  /**
   * @see org.projectforge.web.dialog.ModalDialog#init()
   */
  @Override
  public void init()
  {
    setCloseButtonLabel(getString("save"));
    setShowCancelButton();
    final Form<Void> form = new Form<Void>(getFormId());
    init(form);
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
