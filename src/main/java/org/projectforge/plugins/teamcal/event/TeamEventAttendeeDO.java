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

package org.projectforge.plugins.teamcal.event;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.projectforge.user.PFUserDO;
import org.projectforge.user.UserRights;

/**
 * @author Kai Reinhard (k.reinhard@micromata.de)
 */
@Entity
@Table(name = "T_PLUGIN_CALENDAR_EVENT_ATTENDEE")
public class TeamEventAttendeeDO implements Serializable, Comparable<TeamEventAttendeeDO>
{
  private static final long serialVersionUID = -3293247578185393730L;

  private String url;

  private Integer userId;

  private String loginToken;

  private TeamAttendeeStatus status;

  private String comment;

  private Integer id;

  @Id
  @GeneratedValue
  @Column(name = "pk")
  public Integer getId()
  {
    return id;
  }

  public void setId(final Integer id)
  {
    this.id = id;
  }


  /**
   * @return the userId
   */
  public Integer getUserId()
  {
    return userId;
  }

  /**
   * @param userId the userId to set
   * @return this for chaining.
   */
  public TeamEventAttendeeDO setUserId(final Integer userId)
  {
    this.userId = userId;
    return this;
  }

  /**
   * @return the loginToken
   */
  @Column(name = "login_token", length = 255)
  public String getLoginToken()
  {
    return loginToken;
  }

  /**
   * @param loginToken the loginToken to set
   * @return this for chaining.
   */
  public TeamEventAttendeeDO setLoginToken(final String loginToken)
  {
    this.loginToken = loginToken;
    return this;
  }

  /**
   * @return the url
   */
  public String getUrl()
  {
    return url;
  }

  /**
   * @param url the url to set
   * @return this for chaining.
   */
  public TeamEventAttendeeDO setUrl(final String url)
  {
    this.url = url;
    return this;
  }

  /**
   * @return the status
   */
  public TeamAttendeeStatus getStatus()
  {
    return status;
  }

  /**
   * @param status the status to set
   * @return this for chaining.
   */
  public TeamEventAttendeeDO setStatus(final TeamAttendeeStatus status)
  {
    this.status = status;
    return this;
  }

  /**
   * @return the comment
   */
  public String getComment()
  {
    return comment;
  }

  /**
   * @param comment the comment to set
   * @return this for chaining.
   */
  public TeamEventAttendeeDO setComment(final String comment)
  {
    this.comment = comment;
    return this;
  }

  /**
   * @see java.lang.Comparable#compareTo(java.lang.Object)
   */
  @Override
  public int compareTo(final TeamEventAttendeeDO arg0)
  {
    return this.toString().toLowerCase().compareTo(arg0.toString().toLowerCase());
  }

  /**
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode()
  {
    final HashCodeBuilder hcb = new HashCodeBuilder();
    if (this.userId != null) {
      hcb.append(this.userId);
    } else {
      hcb.append(this.url);
    }
    return hcb.toHashCode();
  }

  /**
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals(final Object o)
  {
    if (o instanceof TeamEventAttendeeDO) {
      final TeamEventAttendeeDO other = (TeamEventAttendeeDO) o;
      if (ObjectUtils.equals(this.getUserId(), other.getUserId()) == false)
        return false;
      if (StringUtils.equals(this.getUrl(), other.getUrl()) == false)
        return false;
      return true;
    }
    return false;
  }

  /**
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString()
  {
    if (this.userId != null) {
      final PFUserDO user = UserRights.getUserGroupCache().getUser(this.userId);
      if (user != null) {
        return user.getFullname() + " (id=" + this.userId + ")";
      } else {
        return "id=" + this.userId + " (not found)";
      }
    }
    return StringUtils.defaultString(this.url);
  }
}
