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

import org.apache.wicket.Component;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.model.PropertyModel;

/**
 * Represents a check box panel.
 * @author Kai Reinhard (k.reinhard@micromata.de)
 * 
 */
public class CheckBoxLPanel extends AbstractLPanel
{
  private static final long serialVersionUID = -1765716960784280118L;

  /**
   * Wicket id.
   */
  public static final String CHECKBOX_ID = "checkbox";

  private CheckBox checkBox;

  /**
   * @see AbstractDOFormRenderer#createCheckBoxPanel(String, Object, String)
   */
  CheckBoxLPanel(final String id, final Object dataObject, final String property)
  {
    this(id, new CheckBox(CHECKBOX_ID, new PropertyModel<Boolean>(dataObject, property)));
  }

  /**
   * @see AbstractDOFormRenderer#createCheckBoxPanel(String, CheckBox)
   */
  CheckBoxLPanel(final String id, final CheckBox checkBox)
  {
    super(id, null);
    this.checkBox = checkBox;
    this.classAttributeAppender = "checkbox";
    add(checkBox);
  }

  public CheckBox getCheckBox()
  {
    return checkBox;
  }

  @Override
  protected Component getClassModifierComponent()
  {
    return checkBox;
  }
}
