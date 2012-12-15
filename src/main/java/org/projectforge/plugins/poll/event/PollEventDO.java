/////////////////////////////////////////////////////////////////////////////
//
// Project ProjectForge Community Edition
//         www.projectforge.org
//
// Copyright (C) 2001-2012 Kai Reinhard (k.reinhard@micromata.de)
//
// ProjectForge is dual-licensed.
//
// This community edition is free software; you can redistribute it and/or
// modify it under the terms of the GNU General Public License as published
// by the Free Software Foundation; version 3 of the License.
//
// This community edition is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
// Public License for more details.
//
// You should have received a copy of the GNU General Public License along
// with this program; if not, see http://www.gnu.org/licenses/.
//
/////////////////////////////////////////////////////////////////////////////

package org.projectforge.plugins.poll.event;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.apache.commons.lang.time.DateFormatUtils;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.annotations.IndexedEmbedded;
import org.joda.time.DateTime;
import org.projectforge.common.DateFormatType;
import org.projectforge.common.DateFormats;
import org.projectforge.core.DefaultBaseDO;
import org.projectforge.plugins.poll.PollDO;

/**
 * @author M. Lauterbach (m.lauterbach@micromata.de)
 * 
 */
@Entity
@Indexed
@Table(name = "T_PLUGIN_POLL_EVENT")
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

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "poll_fk")
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
  public PollEventDO setPoll(final PollDO poll)
  {
    this.poll = poll;
    return this;
  }

  @Column
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
  public PollEventDO setStartDate(final DateTime startDate)
  {
    this.startDate = startDate;
    return this;
  }

  @Column
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
  public PollEventDO setEndDate(final DateTime endDate)
  {
    this.endDate = endDate;
    return this;
  }

  /**
   * @see org.projectforge.core.AbstractBaseDO#toString()
   */
  @Override
  public String toString()
  {
    final String pattern = DateFormats.getFormatString(DateFormatType.TIMESTAMP_MINUTES);
    return DateFormatUtils.format(startDate.getMillis(), pattern) + " - " + DateFormatUtils.format(endDate.getMillis(), pattern);
  }
}
