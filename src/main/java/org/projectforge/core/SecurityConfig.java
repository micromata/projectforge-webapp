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

package org.projectforge.core;

import org.projectforge.user.PFUserDO;


/**
 * Bean used by ConfigXML (config.xml) for configuring security stuff.
 * @author Kai Reinhard (k.reinhard@micromata.de)
 */
public class SecurityConfig
{
  private String pepperString;

  /**
   * If configured passwords will be hashed by using this salt.
   * @return the pepperString which should be used for hashing passwords (with salt and pepper).
   * @see PFUserDO#getPasswordSalt()
   */
  public String getPepperString()
  {
    return pepperString;
  }

  /**
   * @param pepperString the pepperString to set
   * @return this for chaining.
   */
  public SecurityConfig setPepperString(final String pepperString)
  {
    this.pepperString = pepperString;
    return this;
  }
}
