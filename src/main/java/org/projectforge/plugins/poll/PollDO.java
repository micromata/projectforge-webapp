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
import org.projectforge.user.PFUserDO;

/**
 * @author M. Lauterbach (m.lauterbach@micromata.de)
 * 
 */
public class PollDO extends DefaultBaseDO
{
  private static final long serialVersionUID = 1L;

  @IndexedEmbedded(depth = 1)
  private PFUserDO owner;

  private String title;

  private String location;

  private String description;

  private boolean active;

  public PollDO()
  {

  }

  /**
   * @return the owner
   */
  public PFUserDO getOwner()
  {
    return owner;
  }

  /**
   * @param owner the owner to set
   * @return this for chaining.
   */
  public PollDO setOwner(PFUserDO owner)
  {
    this.owner = owner;
    return this;
  }

  /**
   * @return the title
   */
  public String getTitle()
  {
    return title;
  }

  /**
   * @param title the title to set
   * @return this for chaining.
   */
  public PollDO setTitle(String title)
  {
    this.title = title;
    return this;
  }

  /**
   * @return the location
   */
  public String getLocation()
  {
    return location;
  }

  /**
   * @param location the location to set
   * @return this for chaining.
   */
  public PollDO setLocation(String location)
  {
    this.location = location;
    return this;
  }

  /**
   * @return the description
   */
  public String getDescription()
  {
    return description;
  }

  /**
   * @param description the description to set
   * @return this for chaining.
   */
  public PollDO setDescription(String description)
  {
    this.description = description;
    return this;
  }

  /**
   * @return the active
   */
  public boolean isActive()
  {
    return active;
  }

  /**
   * @param active the active to set
   * @return this for chaining.
   */
  public PollDO setActive(boolean active)
  {
    this.active = active;
    return this;
  }
}
