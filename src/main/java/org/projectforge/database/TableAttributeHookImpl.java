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

package org.projectforge.database;

import java.lang.reflect.AccessibleObject;

import org.projectforge.updater.TableAttributeHook;
import org.projectforge.updater.TableAttributeType;

/**
 * Represents one attribute of a table (e. g. for creation).
 * 
 * @author Kai Reinhard (k.reinhard@micromata.de)
 */
public class TableAttributeHookImpl implements TableAttributeHook
{
  /**
   * @see org.projectforge.updater.TableAttributeHook#determineType(java.lang.reflect.AccessibleObject)
   */
  @Override
  public TableAttributeType determineType(final AccessibleObject annotatedFieldOrMethod)
  {
    if (annotatedFieldOrMethod.isAnnotationPresent(org.hibernate.annotations.Type.class)) {
      final org.hibernate.annotations.Type annotation = annotatedFieldOrMethod.getAnnotation(org.hibernate.annotations.Type.class);
      final String typePropertyValue = annotation.type();
      if ("binary".equals(typePropertyValue) == true) {
        return TableAttributeType.BINARY;
      }
    }
    return null;
  }
}
