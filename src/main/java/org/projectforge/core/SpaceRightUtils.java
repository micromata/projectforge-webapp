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

import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import org.apache.commons.lang.Validate;

/**
 * Some helper methods for handle SpaceRightDO's.
 * @author Kai Reinhard (k.reinhard@micromata.de)
 * 
 */
public class SpaceRightUtils
{
  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(SpaceRightUtils.class);

  /**
   * The value is from form "access=rw,notification=true,key=value,...".
   * @param right
   * @return A key-value-map of the key-values stored as value string of the given right.
   */
  public static Map<String, String> getValues(final SpaceRightDO right)
  {
    Validate.notNull(right);
    final String rightValue = right.getValue();
    final Map<String, String> result = new HashMap<String, String>();
    if (rightValue == null || rightValue.indexOf('=') <= -1) {
      return result;
    }
    final StringTokenizer tokenizer = new StringTokenizer(rightValue, ",");
    while (tokenizer.hasMoreTokens() == true) {
      final String keyValue = tokenizer.nextToken();
      String key = "", value = "";
      final int pos = keyValue.indexOf('=');
      if (pos > 0) {
        key = keyValue.substring(0, pos).trim();
        value = (keyValue.length() > pos + 1) ? keyValue.substring(pos + 1).trim() : null;
        result.put(key, value);
      } else {
        log.warn("Ignore unknown right value entry (isn't from type key=value): " + keyValue);
      }
    }
    return result;
  }
}
