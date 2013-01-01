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
 * Used for defining class attribute value for elements.
 * @author Kai Reinhard (k.reinhard@micromata.de)
 */
public enum FieldSetIconPosition
{
  BOTTOM_RIGHT("position: absolute; left: 125px; bottom: 0px;"), //
  TOP_LEFT("position: absolute; left: 0px; top: 0px;"), //
  TOP_RIGHT("position: absolute; left: 125px; top: 0px;");

  private String styleAttrValue;

  public String getStyleAttrValue()
  {
    return styleAttrValue;
  }

  private FieldSetIconPosition(final String styleAttrValue)
  {
    this.styleAttrValue = styleAttrValue;
  }
}
