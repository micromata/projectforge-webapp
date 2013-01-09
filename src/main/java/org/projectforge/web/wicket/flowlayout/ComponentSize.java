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
public enum ComponentSize
{
  LENGTH_2("length-2"), LENGTH_4("length-4"), LENGTH_10("length-10"), MINI("mini"), SMALL("small"), MEDIUM("medium"), LARGE("large"), XLARGE("xlarge"), XXLARGE("xxlarge");

  private String classAttrValue;

  public String getClassAttrValue()
  {
    return "input-" + classAttrValue;
  }

  private ComponentSize(final String classAttrValue)
  {
    this.classAttrValue = classAttrValue;
  }
}
