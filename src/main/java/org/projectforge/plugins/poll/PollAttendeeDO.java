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
import org.projectforge.user.PFUserDO;

/**
 * @author M. Lauterbach (m.lauterbach@micromata.de)
 * 
 */
@Entity
@Indexed
@Table(name = "T_PLUGIN_POLL_ATTENDEE")
public class PollAttendeeDO extends DefaultBaseDO
{
  private static final long serialVersionUID = -7792408128536643950L;

  @IndexedEmbedded(depth = 1)
  private PFUserDO user;

  private String email;

  @IndexedEmbedded(depth = 1)
  private PollDO poll;

  private String secureKey;

  public PollAttendeeDO()
  {

  }

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_fk")
  /**
   * @return the user
   */
  public PFUserDO getUser()
  {
    return user;
  }

  /**
   * @param user the user to set
   * @return this for chaining.
   */
  public PollAttendeeDO setUser(final PFUserDO user)
  {
    this.user = user;
    return this;
  }

  @Column
  /**
   * @return the email
   */
  public String getEmail()
  {
    return email;
  }

  /**
   * @param email the email to set
   * @return this for chaining.
   */
  public PollAttendeeDO setEmail(final String email)
  {
    this.email = email;
    return this;
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
  public PollAttendeeDO setPoll(final PollDO poll)
  {
    this.poll = poll;
    return this;
  }

  @Column
  /**
   * @return the secureKey
   */
  public String getSecureKey()
  {
    return secureKey;
  }

  /**
   * @param secureKey the secureKey to set
   * @return this for chaining.
   */
  public PollAttendeeDO setSecureKey(final String secureKey)
  {
    this.secureKey = secureKey;
    return this;
  }
}
