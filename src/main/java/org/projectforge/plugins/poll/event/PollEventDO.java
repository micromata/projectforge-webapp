/////////////////////////////////////////////////////////////////////////////
//
// Project ProjectForge Community Edition
//         www.projectforge.org
//
// Copyright (C) 2001-2013 Kai Reinhard (k.reinhard@micromata.de)
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

import java.sql.Timestamp;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.apache.commons.lang.time.DateFormatUtils;
import org.hibernate.search.annotations.DateBridge;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Index;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.annotations.IndexedEmbedded;
import org.hibernate.search.annotations.Resolution;
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

  @Field(index = Index.UN_TOKENIZED)
  @DateBridge(resolution = Resolution.MINUTE)
  private Timestamp startDate;

  @Field(index = Index.UN_TOKENIZED)
  @DateBridge(resolution = Resolution.MINUTE)
  private Timestamp endDate;

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
  public Timestamp getStartDate()
  {
    return startDate;
  }

  /**
   * @param startDate the startDate to set
   * @return this for chaining.
   */
  public PollEventDO setStartDate(final Timestamp startDate)
  {
    this.startDate = startDate;
    return this;
  }

  @Column
  /**
   * @return the endDate
   */
  public Timestamp getEndDate()
  {
    return endDate;
  }

  /**
   * @param endDate the endDate to set
   * @return this for chaining.
   */
  public PollEventDO setEndDate(final Timestamp endDate)
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
    return DateFormatUtils.format(startDate.getTime(), pattern) + " - " + DateFormatUtils.format(endDate.getTime(), pattern);
  }

  /**
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode()
  {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((endDate == null) ? 0 : endDate.hashCode());
    result = prime * result + ((poll == null) ? 0 : poll.hashCode());
    result = prime * result + ((startDate == null) ? 0 : startDate.hashCode());
    return result;
  }

  /**
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals(final Object obj)
  {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    final PollEventDO other = (PollEventDO) obj;
    if (endDate == null) {
      if (other.endDate != null)
        return false;
    } else if (!endDate.equals(other.endDate))
      return false;
    if (poll == null) {
      if (other.poll != null)
        return false;
    } else if (!poll.equals(other.poll))
      return false;
    if (startDate == null) {
      if (other.startDate != null)
        return false;
    } else if (!startDate.equals(other.startDate))
      return false;
    return true;
  }
}
