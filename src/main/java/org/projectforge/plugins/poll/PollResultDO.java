/////////////////////////////////////////////////////////////////////////////
//
// Project   ProjectForge
//
// Copyright 2001-2009, Micromata GmbH, Kai Reinhard
//           All rights reserved.
//
/////////////////////////////////////////////////////////////////////////////

package org.projectforge.plugins.poll;

import org.hibernate.search.annotations.IndexedEmbedded;
import org.projectforge.core.DefaultBaseDO;

/**
 * @author M. Lauterbach (m.lauterbach@micromata.de)
 * 
 */
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
  public PollResultDO setPollEvent(PollEventDO pollEvent)
  {
    this.pollEvent = pollEvent;
    return this;
  }

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
  public PollResultDO setPollAttendee(PollAttendeeDO pollAttendee)
  {
    this.pollAttendee = pollAttendee;
    return this;
  }

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
  public PollResultDO setResult(boolean result)
  {
    this.result = result;
    return this;
  }
}
