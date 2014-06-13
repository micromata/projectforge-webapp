/////////////////////////////////////////////////////////////////////////////
//
// Project   ProjectForge
//
// Copyright 2001-2009, Micromata GmbH, Kai Reinhard
//           All rights reserved.
//
/////////////////////////////////////////////////////////////////////////////

package org.projectforge.plugins.teamcal.event;

import java.io.Serializable;

import org.projectforge.plugins.teamcal.admin.TeamCalDO;
import org.projectforge.user.PFUserContext;

/**
 * @author Kai Reinhard (k.reinhard@micromata.de)
 *
 */
public class NotificationController implements Serializable
{
  private static final long serialVersionUID = -5256549570640682669L;

  private final TeamCalDO[] calendarsWithFullAccess;

  public NotificationController(final TeamCalDO[] calendarsWithFullAccess) {
    this.calendarsWithFullAccess = calendarsWithFullAccess;
  }

  public void onDelete(final TeamEventDO data) {
    if (data.getAttendees() != null && data.getAttendees().isEmpty() == false) {
      final TeamEventMailer mailer = TeamEventMailer.getInstance();
      final TeamEventMailValue value = new TeamEventMailValue(data.getId(), TeamEventMailType.REJECTION, null);
      mailer.getQueue().offer(value);
      mailer.send();
    }
  }

  public void afterUpdate(final TeamEventDO data, final Integer orgId) {
    if (data.getAttendees() != null && data.getAttendees().isEmpty() == false) {
      //        for (final TeamEventAttendeeDO attendee: newEvent.getAttendees()) {
      //          if (attendee.getUser() != null) {
      //            if (attendee.getUser().equals(PFUserContext.getUser())== true) {
      //              continue;
      //            }
      //            for (int i=0; i < form.calendarsWithFullAccess.length; i++) {
      //              final Integer id = form.calendarsWithFullAccess[i].getOwnerId();
      //              if (id.equals(attendee.getUser().getId()) == true) {
      //                final TeamEventDO temp = getData();
      //                temp.setCalendar(form.calendarsWithFullAccess[i]);
      //                this.teamEventDao.saveOrUpdate(temp);
      //              }
      //            }
      //          }
      //        }
      final TeamEventMailer mailer = TeamEventMailer.getInstance();
      final TeamEventMailValue value = new TeamEventMailValue(data.getId(), TeamEventMailType.UPDATE, orgId);
      mailer.getQueue().offer(value);
      mailer.send();
    }
  }

  public void afterSaveOrUpdate(final TeamEventDO data, final TeamEventDao teamEventDao, final boolean wasNew) {
    if (data.getAttendees() != null && data.getAttendees().isEmpty() == false) {
      final TeamEventMailer mailer = TeamEventMailer.getInstance();
      TeamEventMailValue value = null;
      if (wasNew == true) {
        value = new TeamEventMailValue(data.getId(), TeamEventMailType.INVITATION, null);
        for (final TeamEventAttendeeDO attendee: data.getAttendees()) {
          if (attendee.getUser() != null) {
            if (attendee.getUser().equals(PFUserContext.getUser())== true) {
              continue;
            }
            for (int i=0; i < calendarsWithFullAccess.length; i++) {
              final Integer id = calendarsWithFullAccess[i].getOwnerId();
              if (id.equals(attendee.getUser().getId()) == true) {
                final TeamEventDO temp = data.clone();
                temp.setId(null);
                temp.setCalendar(calendarsWithFullAccess[i]);
                teamEventDao.save(temp);
              }
            }
          }
        }
      } else {
        value = new TeamEventMailValue(data.getId(), TeamEventMailType.UPDATE, null);
      }
      mailer.getQueue().offer(value);
      mailer.send();
    }
  }
}
