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

import org.projectforge.user.PFUserContext;

/**
 * @author Kai Reinhard (k.reinhard@micromata.de)
 *
 */
public class NotificationController implements Serializable
{
  private static final long serialVersionUID = -5256549570640682669L;

  private final TeamEventDiff orgData;

  public NotificationController(final TeamEventDO data) {
    orgData = new TeamEventDiff(data);
  }

  public void onDelete(final TeamEventDO data) {
    if (data.getAttendees() != null && data.getAttendees().isEmpty() == false) {
      final TeamEventMailer mailer = TeamEventMailer.getInstance();
      final TeamEventMailValue value = new TeamEventMailValue(data.getId(), TeamEventMailType.REJECTION);
      mailer.getQueue().offer(value);
      mailer.send(orgData);
    }
  }

  public void afterUpdate(final TeamEventDO data, final TeamEventDao teamEventDao) {
    if (data.getAttendees() != null && data.getAttendees().isEmpty() == false) {
      orgData.computeChanges(data, teamEventDao.getDisplayHistoryEntries(data));
      final TeamEventMailer mailer = TeamEventMailer.getInstance();
      final TeamEventMailValue value = new TeamEventMailValue(data.getId(), TeamEventMailType.UPDATE);
      mailer.getQueue().offer(value);
      mailer.send(orgData);
    }
  }

  public void afterSaveOrUpdate(final TeamEventDO data, final TeamEventDao teamEventDao, final LocalInvitationDao localInvitationDao, final boolean wasNew) {
    if (data.getAttendees() != null && data.getAttendees().isEmpty() == false) {
      final TeamEventMailer mailer = TeamEventMailer.getInstance();
      TeamEventMailValue value = null;
      if (wasNew == true) {
        value = new TeamEventMailValue(data.getId(), TeamEventMailType.INVITATION);
        for (final TeamEventAttendeeDO attendee: data.getAttendees()) {
          if (attendee.getUser() != null) {
            if (attendee.getUser().equals(PFUserContext.getUser())== true) {
              continue;
            }
            if (attendee.getUserId() != null) {
              final LocalInvitationDO invitation = new LocalInvitationDO();
              invitation.setEvent(data).setUser(attendee.getUser());
              localInvitationDao.saveOrUpdate(invitation);
            }
          }
        }
      } else {
        orgData.computeChanges(data, teamEventDao.getDisplayHistoryEntries(data));
        value = new TeamEventMailValue(data.getId(), TeamEventMailType.UPDATE);
      }
      mailer.getQueue().offer(value);
      mailer.send(orgData);
    }
  }
}
