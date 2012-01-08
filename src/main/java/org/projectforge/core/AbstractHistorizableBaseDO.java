/////////////////////////////////////////////////////////////////////////////
//
// Project ProjectForge Community Edition
//         www.projectforge.org
//
// Copyright (C) 2001-2012 Kai Reinhard (k.reinhard@micromata.com)
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

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.MappedSuperclass;
import javax.persistence.Transient;

import de.micromata.hibernate.history.ExtendedHistorizable;

/**
 * Declares lastUpdate and created as invalidHistorizableProperties.
 * @author Kai Reinhard (k.reinhard@micromata.de)
 */
@MappedSuperclass
public abstract class AbstractHistorizableBaseDO<I extends Serializable> extends AbstractBaseDO<I> implements ExtendedHistorizable
{
  private static final long serialVersionUID = -5980671510045450615L;

  protected static final Set<String> invalidHistorizableProperties;
  
  static {
    invalidHistorizableProperties = new HashSet<String>();
    invalidHistorizableProperties.add("lastUpdate");
    invalidHistorizableProperties.add("created");
  }
  
  public static Set<String> getInvalidHistorizableProperties()
  {
    return invalidHistorizableProperties;
  }


  @Transient
  public Set<String> getHistorizableAttributes()
  {
    return null;
  }

  @Transient
  public Set<String> getNonHistorizableAttributes()
  {
    return invalidHistorizableProperties;
  }
}
