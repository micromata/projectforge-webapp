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

package org.projectforge.web.wicket.flowlayout;

/**
 * Used by IconPanels.
 * @author Kai Reinhard (k.reinhard@micromata.de)
 */
public enum IconType
{
  ALERT("icon-warning-sign"), //
  REFRESH("refresh"), //
  //CIRCLE_ARROW_EAST("question-sign"), //
  //CIRCLE_ARROW_WEST("question-sign"), //
  //CLIPBOARD("question-sign"), //
  //CLOCK("question-sign"), //
  DOCUMENT("icon-file"), //
  DOWNLOAD("download"), //
  EDIT("pencil"), //
  FOLDER_OPEN("folder-open"), //
  GOTO("icon-hand-right"), //
  HELP("info-sign"), //
  JIRA_SUPPORT("star"), //
  KEYBOARD("icon-th"), //
  MINUS_SIGN("minus-sign"), //
  //MINUS_THICK("question-sign"), //
  MODIFIED("icon-star-empty"), //
  PLUS("plus"), //
  PLUS_SIGN("plus-sign"), //
  REMOVE("remove"), //
  SEARCH("search"), // //
  /** RSS feed symbol. */
  SUBSCRIPTION("globe"), //
  TRASH("trash"), //
  CALENDAR("calendar"), //
  WRENCH("wrench"), //
  ZOOM_IN("zoom-in"), //
  ZOOM_OUT("zoom-out");

  private String cssIdentifier;

  public String getClassAttrValue()
  {
    return "icon-" + cssIdentifier;
  }

  private IconType(final String classAttrValue)
  {
    this.cssIdentifier = classAttrValue;
  }
}
