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

package org.projectforge.web.wicket.flowlayout;

/**
 * Used by IconPanels.
 * @author Kai Reinhard (k.reinhard@micromata.de)
 */
public enum IconType
{
  ALERT("alert"), ARROW_REFRESH("arrowrefresh-1-e"), CIRCLE_MINUS("circle-minus"), CIRCLE_ARROW_EAST("circle-arrow-e"), CIRCLE_ARROW_WEST("circle-arrow-w"), CIRCLE_CLOSE(
      "circle-close"), CIRCLE_PLUS("circle-plus"), CLIPBOARD("clipboard"), CLOCK("clock"), DOCUMENT("document"), FOLDER_OPEN("folder-open"), HELP(
          "help"), JIRA_SUPPORT("star"), KEYBOARD("calculator"), MINUS_THICK("minusthick"), MODIFIED("alert"), PLUS_THICK("plusthick"), TRASH(
              "trash");

  private String cssIdentifier;

  public String getClassAttrValue()
  {
    return "ui-icon-" + cssIdentifier;
  }

  private IconType(final String classAttrValue)
  {
    this.cssIdentifier = classAttrValue;
  }
}
