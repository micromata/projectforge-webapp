/////////////////////////////////////////////////////////////////////////////
//
// Project   ProjectForge
//
// Copyright 2001-2009, Micromata GmbH, Kai Reinhard
//           All rights reserved.
//
/////////////////////////////////////////////////////////////////////////////

package org.projectforge.plugins.teamcal.event;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.annotations.IndexedEmbedded;
import org.projectforge.core.DefaultBaseDO;
import org.projectforge.user.PFUserDO;

/**
 * @author Werner Feder (w.feder-extern@micromata.de)
 *
 */
@Entity
@Indexed
@Table(name = "T_PLUGIN_CALENDAR_LOCAL_INVITATION")
public class LocalInvitationDO extends DefaultBaseDO
{
  private static final long serialVersionUID = 125482422641232693L;

  @IndexedEmbedded(depth = 1)
  private PFUserDO user;

  @IndexedEmbedded(depth = 1)
  private TeamEventDO event;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_fk", nullable = false)
  public PFUserDO getUser()
  {
    return user;
  }

  public LocalInvitationDO setUser(final PFUserDO user)
  {
    this.user = user;
    return this;
  }

  @Transient
  public Integer getUserId()
  {
    return user != null ? user.getId() : null;
  }

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "event_fk", nullable = false)
  public TeamEventDO getEvent()
  {
    return event;
  }

  public LocalInvitationDO setEvent(final TeamEventDO event)
  {
    this.event = event;
    return this;
  }

  @Transient
  public Integer getEventId()
  {
    return event != null ? event.getId() : null;
  }
}
