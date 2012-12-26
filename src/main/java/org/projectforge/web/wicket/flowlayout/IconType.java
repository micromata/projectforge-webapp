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
 * Used by IconPanels.
 * @author Kai Reinhard (k.reinhard@micromata.de)
 */
public enum IconType
{
  ALERT("question-sign"), REFRESH("refresh"), CIRCLE_MINUS("question-sign"), CIRCLE_ARROW_EAST("question-sign"), CIRCLE_ARROW_WEST(
      "question-sign"), CIRCLE_CLOSE("question-sign"), CIRCLE_PLUS("question-sign"), CLIPBOARD("question-sign"), CLOCK("question-sign"), DOCUMENT(
          "question-sign"), DOWNLOAD("download"), FOLDER_OPEN("folder-open"), HELP("info-sign"), JIRA_SUPPORT("star"), KEYBOARD("question-sign"), MINUS_THICK(
              "question-sign"), MODIFIED("question-sign"), PLUS_THICK("question-sign"), SEARCH("search"), //
              /** RSS feed symbol. */
              SUBSCRIPTION("question-sign"), TRASH("trash"), CALENDAR("calendar"), WRENCH("wrench");

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
