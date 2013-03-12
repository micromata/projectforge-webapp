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
import java.util.HashSet;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.projectforge.core.BaseDO;
import org.projectforge.core.ModificationStatus;
import org.projectforge.user.PFUserDO;
import org.projectforge.user.UserRights;

import de.micromata.hibernate.history.ExtendedHistorizable;

/**
 * @author Kai Reinhard (k.reinhard@micromata.de)
 */
@Entity
@Table(name = "T_PLUGIN_CALENDAR_EVENT_ATTENDEE")
public class TeamEventAttendeeDO implements Serializable, Comparable<TeamEventAttendeeDO>, BaseDO<Integer>, ExtendedHistorizable
{
  private static final long serialVersionUID = -3293247578185393730L;

  private String url;

  private Integer userId;

  private String loginToken;

  private TeamAttendeeStatus status;

  private String comment;

  private Integer id;

  private static final Set<String> NON_HISTORIZABLE_ATTRIBUTES;

  public static final int URL_MAX_LENGTH = 255;

  static {
    NON_HISTORIZABLE_ATTRIBUTES = new HashSet<String>();
    NON_HISTORIZABLE_ATTRIBUTES.add("loginToken");
  }

  @Override
  @Id
  @GeneratedValue
  @Column(name = "pk")
  public Integer getId()
  {
    return id;
  }

  @Override
  public void setId(final Integer id)
  {
    this.id = id;
  }


  /**
   * Is set if the attendee is a ProjectForge user.
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
   * Is used if the attendee isn't a ProjectForge user for authentication.
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
   * The url (mail) of the attendee. Isn't used if the attendee is a ProjectForge user.
   * @return the url
   */
  @Column(length = URL_MAX_LENGTH)
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
    if (this.id != null && ObjectUtils.equals(this.id, arg0.id) == true) {
      return 0;
    }
    return this.toString().toLowerCase().compareTo(arg0.toString().toLowerCase());
  }

  /**
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode()
  {
    final HashCodeBuilder hcb = new HashCodeBuilder();
    if (this.id != null) {
      hcb.append(this.id);
      return hcb.toHashCode();
    }
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
      if (this.id != null && ObjectUtils.equals(this.id, ((TeamEventAttendeeDO) o).id) == true) {
        return true;
      }
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
    } else if (StringUtils.isBlank(url) == true) {
      return String.valueOf(id);
    }
    return StringUtils.defaultString(this.url);
  }

  /**
   * @see org.projectforge.core.BaseDO#isMinorChange()
   */
  @Transient
  @Override
  public boolean isMinorChange()
  {
    return false;
  }

  /**
   * @see org.projectforge.core.BaseDO#setMinorChange(boolean)
   */
  @Override
  public void setMinorChange(final boolean value)
  {
    throw new UnsupportedOperationException();
  }

  /**
   * @see org.projectforge.core.BaseDO#getAttribute(java.lang.String)
   */
  @Transient
  @Override
  public Object getAttribute(final String key)
  {
    throw new UnsupportedOperationException();
  }

  /**
   * @see org.projectforge.core.BaseDO#setAttribute(java.lang.String, java.lang.Object)
   */
  @Override
  public void setAttribute(final String key, final Object value)
  {
    throw new UnsupportedOperationException();
  }

  /**
   * @see org.projectforge.core.BaseDO#copyValuesFrom(org.projectforge.core.BaseDO, java.lang.String[])
   */
  @Override
  public ModificationStatus copyValuesFrom(final BaseDO< ? extends Serializable> src, final String... ignoreFields)
  {
    if (src instanceof TeamEventAttendeeDO == false) {
      throw new UnsupportedOperationException();
    }
    final TeamEventAttendeeDO source = (TeamEventAttendeeDO)src;
    ModificationStatus modStatus = ModificationStatus.NONE;
    if (ObjectUtils.equals(this.id, source.id) == false) {
      modStatus = ModificationStatus.MAJOR;
      this.id = source.id;
    }
    if (ObjectUtils.equals(this.url, source.url) == false) {
      modStatus = ModificationStatus.MAJOR;
      this.url = source.url;
    }
    if (ObjectUtils.equals(this.userId, source.userId) == false) {
      modStatus = ModificationStatus.MAJOR;
      this.userId = source.userId;
    }
    if (ObjectUtils.equals(this.loginToken, source.loginToken) == false) {
      modStatus = ModificationStatus.MAJOR;
      this.loginToken = source.loginToken;
    }
    if (this.status != source.status) {
      modStatus = ModificationStatus.MAJOR;
      this.status = source.status;
    }
    if (this.comment != source.comment) {
      modStatus = ModificationStatus.MAJOR;
      this.comment = source.comment;
    }
    return modStatus;
  }

  /**
   * @see de.micromata.hibernate.history.ExtendedHistorizable#getHistorizableAttributes()
   */
  @Transient
  @Override
  public Set<String> getHistorizableAttributes()
  {
    // All attributes are historizable.
    return null;
  }

  /**
   * @see de.micromata.hibernate.history.ExtendedHistorizable#getNonHistorizableAttributes()
   */
  @Transient
  @Override
  public Set<String> getNonHistorizableAttributes()
  {
    return NON_HISTORIZABLE_ATTRIBUTES;
  }
}
