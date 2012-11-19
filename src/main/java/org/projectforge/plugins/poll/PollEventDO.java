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
import org.joda.time.DateTime;
import org.projectforge.core.DefaultBaseDO;

/**
 * @author M. Lauterbach (m.lauterbach@micromata.de)
 * 
 */
public class PollEventDO extends DefaultBaseDO
{
  private static final long serialVersionUID = 1L;

  @IndexedEmbedded(depth = 1)
  private PollDO poll;

  private DateTime startDate;

  private DateTime endDate;

  public PollEventDO()
  {

  }

  /**
   * @return the pollId
   */
  public PollDO getPoll()
  {
    return poll;
  }

  /**
   * @param poll the pollId to set
   * @return this for chaining.
   */
  public PollEventDO setPoll(PollDO poll)
  {
    this.poll = poll;
    return this;
  }

  /**
   * @return the startDate
   */
  public DateTime getStartDate()
  {
    return startDate;
  }

  /**
   * @param startDate the startDate to set
   * @return this for chaining.
   */
  public PollEventDO setStartDate(DateTime startDate)
  {
    this.startDate = startDate;
    return this;
  }

  /**
   * @return the endDate
   */
  public DateTime getEndDate()
  {
    return endDate;
  }

  /**
   * @param endDate the endDate to set
   * @return this for chaining.
   */
  public PollEventDO setEndDate(DateTime endDate)
  {
    this.endDate = endDate;
    return this;
  }
}
