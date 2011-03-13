/////////////////////////////////////////////////////////////////////////////
//
// Project ProjectForge Community Edition
//         www.projectforge.org
//
// Copyright (C) 2001-2011 Kai Reinhard (k.reinhard@me.com)
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

package org.projectforge.web.wicket.layout;

import org.apache.wicket.markup.html.form.DropDownChoice;

/**
 * Represents a combo box field for mobile devices.
 * @author Kai Reinhard (k.reinhard@micromata.de)
 * 
 */
public class DropDownChoiceMobileLPanel extends DropDownChoiceLPanel
{
  private static final long serialVersionUID = 3970575700933910729L;

  /**
   * @see AbstractFormRenderer#createDropDownChoicePanel(String, LayoutLength, DropDownChoice)
   */
  DropDownChoiceMobileLPanel(final String id,  final DropDownChoice< ? > dropDownChoice, final PanelContext ctx)
  {
    super(id, ctx);
    this.dropDownChoice = dropDownChoice;
    this.classAttributeAppender = "select";
    add(dropDownChoice);
  }

  public DropDownChoiceMobileLPanel setLabel(final String label)
  {
    return this;
  }
}
