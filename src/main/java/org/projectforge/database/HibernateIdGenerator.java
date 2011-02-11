/////////////////////////////////////////////////////////////////////////////
//
// Project ProjectForge Community Edition
//         www.projectforge.org
//
// Copyright (C) 2001-2011 Kai Reinhard (k.reinhard@me.com)
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

package org.projectforge.database;

import java.io.Serializable;
import java.lang.reflect.Field;

import javax.persistence.GeneratedValue;
import javax.persistence.Id;

import org.hibernate.HibernateException;
import org.hibernate.engine.SessionImplementor;
import org.hibernate.id.IdentityGenerator;
import org.projectforge.core.BaseDO;

/**
 * Using own id generator is only needed for importing xml dumps while preserving ids.
 * @author Kai Reinhard (k.reinhard@micromata.de)
 * 
 */
public class HibernateIdGenerator extends IdentityGenerator
{
  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(HibernateIdGenerator.class);

  private static boolean preserveIds = false;
  
  /**
   * Important: preserveIds should only be set to false temporarily e. g. for importing a xml dump into the data-base. Please don't forget to increase the id sequencer afterwards!
   * Otherwise duplicate id's are possible (resulting in errors)!
   * @param preserveIds
   */
  public static void setPreserveIds(final boolean preserveIds)
  {
    if (preserveIds == true) {
      log.warn("PreserveIds is set temporarily to false!");
    }
    HibernateIdGenerator.preserveIds = preserveIds;
  }

  @Override
  public Serializable generate(final SessionImplementor session, final Object obj) throws HibernateException
  {
    if (preserveIds == false) {
      return super.generate(session, obj);
    }
    if (obj == null) {
      throw new HibernateException(new NullPointerException());
    }
    if (obj instanceof BaseDO< ? >) {
      final Serializable id = ((BaseDO< ? >) obj).getId();
      return getId(session, obj, id);
    }
    for (final Field field : obj.getClass().getDeclaredFields()) {
      if (field.isAnnotationPresent(Id.class) == true && field.isAnnotationPresent(GeneratedValue.class) == true) {
        final boolean isAccessible = field.isAccessible();
        try {
          field.setAccessible(true);
          final Object idObject = field.get(obj);
          field.setAccessible(isAccessible);
          if (idObject != null && Serializable.class.isAssignableFrom(idObject.getClass()) == true) {
            return (Serializable) idObject;
          }
        } catch (IllegalArgumentException e) {
          e.printStackTrace();
        } catch (IllegalAccessException e) {
          e.printStackTrace();
        }
      }
    }
    return super.generate(session, obj);
  }

  private Serializable getId(final SessionImplementor session, final Object obj, final Serializable id)
  {
    if (id != null) {
      return id;
    } else {
      return super.generate(session, obj);
    }
  }

}
