/////////////////////////////////////////////////////////////////////////////
//
// Project   ProjectForge
//
// Copyright 2001-2009, Micromata GmbH, Kai Reinhard
//           All rights reserved.
//
/////////////////////////////////////////////////////////////////////////////

package org.projectforge.plugins.poll;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.annotations.IndexedEmbedded;
import org.projectforge.core.DefaultBaseDO;

/**
 * @author M. Lauterbach (m.lauterbach@micromata.de)
 * 
 */
@Entity
@Indexed
@Table(name = "T_PLUGIN_POLL_RESULT")
public class PollResultDO extends DefaultBaseDO
{
  private static final long serialVersionUID = -8378182859274204836L;

  @IndexedEmbedded(depth = 1)
  private PollEventDO pollEvent;

  @IndexedEmbedded(depth = 1)
  private PollAttendeeDO pollAttendee;

  private boolean result;

  public PollResultDO()
  {

  }

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "poll_event_fk")
  /**
   * @return the pollEvent
   */
  public PollEventDO getPollEvent()
  {
    return pollEvent;
  }

  /**
   * @param pollEvent the pollEvent to set
   * @return this for chaining.
   */
  public PollResultDO setPollEvent(final PollEventDO pollEvent)
  {
    this.pollEvent = pollEvent;
    return this;
  }

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "poll_attendee_fk")
  /**
   * @return the pollAttendee
   */
  public PollAttendeeDO getPollAttendee()
  {
    return pollAttendee;
  }

  /**
   * @param pollAttendee the pollAttendee to set
   * @return this for chaining.
   */
  public PollResultDO setPollAttendee(final PollAttendeeDO pollAttendee)
  {
    this.pollAttendee = pollAttendee;
    return this;
  }

  @Column
  /**
   * @return the result
   */
  public boolean isResult()
  {
    return result;
  }

  /**
   * @param result the result to set
   * @return this for chaining.
   */
  public PollResultDO setResult(final boolean result)
  {
    this.result = result;
    return this;
  }
}
