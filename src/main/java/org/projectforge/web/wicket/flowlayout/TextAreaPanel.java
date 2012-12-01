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

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.markup.html.panel.Panel;

/**
 * Represents an icon.
 * @author Kai Reinhard (k.reinhard@micromata.de)
 * 
 */
public class TextAreaPanel extends Panel implements ComponentWrapperPanel
{
  public static final String WICKET_ID = "textarea";

  private static final long serialVersionUID = -4126462093466172226L;

  private Component field;

  public TextAreaPanel(final String id, final Component field)
  {
    super(id);
    field.add(AttributeModifier.append("class", "textarea"));
    add(this.field = field);
  }

  /**
   * class="autogrow"
   * @return this for chaining.
   */
  public TextAreaPanel setAutogrow()
  {
    return setAutogrow(0, 500);
  }

  /**
   * class="autogrow"
   * @return this for chaining.
   */
  public TextAreaPanel setAutogrow(final int minHeight, final int maxHeight)
  {
    field.add(AttributeModifier.append("class", "expand" + minHeight + "-" + maxHeight));
    return this;
  }

  /**
   * @see org.projectforge.web.wicket.flowlayout.ComponentWrapperPanel#getComponentOutputId()
   */
  @Override
  public String getComponentOutputId()
  {
    field.setOutputMarkupId(true);
    return field.getMarkupId();
  }
}
