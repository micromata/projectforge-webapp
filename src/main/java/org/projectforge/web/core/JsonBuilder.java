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

package org.projectforge.web.core;

import java.util.Collection;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;

public class JsonBuilder
{
  final private StringBuffer buf = new StringBuffer();

  /**
   * Creates Json result string from the given list.<br/>
   * [["Horst"], ["Klaus"], ...]] // For single property<br/>
   * [["Klein", "Horst"],["Schmidt", "Klaus"], ...] // For two Properties (e. g. name, first name) [["id:37", "Klein", "Horst"],["id:42",
   * "Schmidt", "Klaus"], ...] // For two Properties (e. g. name, first name) with id. <br/>
   * Uses ObjectUtils.toString(Object) for formatting each value.
   * @param prependId If true, the first column will be returned as "id:<value>".
   * @param col The array representation: List<Object> or List<Object[]>. If null then "[]" is returned.
   * @return
   */
  public static String buildToStringRows(final Collection< ? > col, final boolean prependId)
  {
    if (col == null) {
      return "[]";
    }
    final JsonBuilder builder = new JsonBuilder();
    return builder.append(col, prependId).getAsString();
  }

  public static String buildToStringRows(final Collection< ? > col)
  {
    return buildToStringRows(col, false);
  }

  public String getAsString()
  {
    return buf.toString();
  }

  /**
   * Appends objects to buffer, e. g.: ["Horst"], ["Klaus"], ... Uses formatValue(Object) to render the values.
   * @param oArray
   * @param prependId If true then id will be prepended at the first column, e. g.: ["id:37", "Klein", "Horst"].
   * @return This (fluent)
   */
  public JsonBuilder append(final Object[] oArray, final boolean prependId)
  {
    buf.append(" ["); // begin array
    boolean firstCell = true;
    for (Object obj : oArray) {
      if (firstCell == true) {
        firstCell = false;
        buf.append("\"");
        if (prependId == true) {
          buf.append("id:");
        }
      } else {
        buf.append(",\"");
      }
      // " must be quoted as \":
      buf.append(StringUtils.replace(formatValue(obj), "\"", "\\\"")).append("\"");
    }
    buf.append("]"); // end array
    return this;
  }

  public JsonBuilder append(final Object[] oArray)
  {
    return append(oArray, false);
  }

  /**
   * Creates Json result string from the given list.<br/>
   * [["Horst"], ["Klaus"], ...]] // For single property<br/>
   * [["Klein", "Horst"],["Schmidt", "Klaus"], ...] // For two Properties (e. g. name, first name) [["id:37", "Klein", "Horst"],["id:42",
   * "Schmidt", "Klaus"], ...] // For two Properties (e. g. name, first name) with id. <br/>
   * Uses formatValue(Object) for formatting each value.
   * @param prependId If true, the first column will be returned as "id:<value>".
   * @param col The array representation: List<Object> or List<Object[]>. If null then "[]" is returned.
   * @return
   */
  public JsonBuilder append(final Collection< ? > col, final boolean prependId)
  {
    if (col == null) {
      buf.append("[]");
      return this;
    }
    // Format: [["1.1", "1.2", ...],["2.1", "2.2", ...]]
    buf.append("[\n");
    boolean firstRow = true;
    for (final Object os : col) {
      if (firstRow == true)
        firstRow = false;
      else buf.append(",\n");
      if (os instanceof Object[]) { // Multiple properties
        append((Object[]) os, prependId);
      } else { // Only one property
        append(transform(os));
      }
    }
    buf.append("]"); // end data
    return this;
  }

  public JsonBuilder append(final Collection< ? > col)
  {
    return append(col, false);
  }

  /**
   * @param obj
   * @return
   * @see ObjectUtils#toString(Object)
   */
  protected String formatValue(Object obj)
  {
    return ObjectUtils.toString(obj);
  }

  protected JsonBuilder append(final Object obj)
  {
    if (obj instanceof Object[]) {
      return append((Object[])obj);
    }
    buf.append(" ["); // begin row
    // " must be quoted as \":
    buf.append("\"").append(StringUtils.replace(formatValue(obj), "\"", "\\\"")).append("\"");
    buf.append("]"); // end row
    return this;
  }

  /**
   * Before rendering a obj of e. g. a collection the obj can be transformed e. g. in an Object array of dimension 2 containing label and
   * value.
   * @param obj
   * @return obj (identity function) if not overload.
   */
  protected Object transform(final Object obj)
  {
    return obj;
  }
}
