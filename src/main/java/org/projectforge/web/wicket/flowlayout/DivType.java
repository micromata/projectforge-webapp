/////////////////////////////////////////////////////////////////////////////
//
// Project ProjectForge Community Edition
//         www.projectforge.org
//
// Copyright (C) 2001-2012 Kai Reinhard (k.reinhard@micromata.de)
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

package org.projectforge.web.wicket.flowlayout;

/**
 * Used for defining class attribute value for elements.
 * @author Kai Reinhard (k.reinhard@micromata.de)
 */
public enum DivType
{
  @Deprecated ACCORDION_BOX("accordion_box"),@Deprecated BLOCK("block"), @Deprecated BLOCK_LINES("block lines"),@Deprecated BOX("box"),@Deprecated CLEARFIX("clearfix mm_clear"),@Deprecated COL_25("span3"),@Deprecated COL_33(
      "span4"),@Deprecated COL_40("span4"),@Deprecated COL_50("span6"),@Deprecated COL_60("span8"),@Deprecated COL_66("span8"),@Deprecated COL_75("span9"),@Deprecated COL_100("span12"),@Deprecated COLUMNS("columns"), CHECKBOX(
          "jqui_checkbox"), FIELD_DIV("field_div"), GRID4("span4"), GRID6("span6"), GRID12("span12"), MARGIN_TOP_10("margin_top_10"), RADIOBOX(
              "radio-jquery-ui"),@Deprecated ROUND_ALL("round_all"), @Deprecated SECTION("section"), @Deprecated
              TOGGLE_CONTAINER("toggle_container");

  private String classAttrValue;

  public String getClassAttrValue()
  {
    return classAttrValue;
  }

  private DivType(final String classAttrValue)
  {
    this.classAttrValue = classAttrValue;
  }
}
