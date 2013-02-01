/////////////////////////////////////////////////////////////////////////////
//
// Project   ProjectForge
//
// Copyright 2001-2009, Micromata GmbH, Kai Reinhard
//           All rights reserved.
//
/////////////////////////////////////////////////////////////////////////////

package org.projectforge.plugins.teamcal.integration;

import java.sql.Timestamp;
import java.util.List;

import net.fortuna.ical4j.model.component.VEvent;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.model.IModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.projectforge.plugins.teamcal.admin.TeamCalDao;
import org.projectforge.plugins.teamcal.event.TeamEventDO;
import org.projectforge.plugins.teamcal.event.TeamEventDao;
import org.projectforge.web.dialog.ModalDialog;
import org.projectforge.web.wicket.flowlayout.DivPanel;

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
    final DivPanel content = gridBuilder.getPanel();
    content.add(new Label(content.newChildId(), "hurzel")); // TODO add import ics repeater
  }

  private void onSave(final List<VEvent> newEvents, final List<VEvent> existingEvents)
  {
    TeamEventDO teamEvent;
    for (final VEvent e : newEvents) {
      teamEvent = new TeamEventDO();
      // teamEvent.setCalendar(teamCal); TODO dialog
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
}
