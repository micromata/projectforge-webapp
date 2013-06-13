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

import java.lang.reflect.Field;

/**
 * @author Kai Reinhard (k.reinhard@micromata.de)
 */
public class PropUtils
{
  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(PropUtils.class);

  public static PropertyInfo get(final Class< ? > clazz, final String property)
  {
    try {
      final Field field = clazz.getDeclaredField(property);
      return field.getAnnotation(PropertyInfo.class);
    } catch (final NoSuchFieldException ex) {
      log.error("Field '" + clazz.getName() + "." + property + "' not found: " + ex.getMessage());
      return null;
    }
  }

  public static String getI18nKey(final Class< ? > clazz, final String property)
  {
    final PropertyInfo info = get(clazz, property);
    if (info == null) {
      log.error("PropertyInfo not found for field '" + clazz.getName() + "." + property + "' not found.");
      return null;
    }
    return info.i18nKey();
  }
}
