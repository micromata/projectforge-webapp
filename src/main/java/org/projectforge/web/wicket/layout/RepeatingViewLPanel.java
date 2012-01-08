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

package org.projectforge.web.wicket.layout;

import org.apache.wicket.Component;
import org.apache.wicket.behavior.SimpleAttributeModifier;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.projectforge.web.wicket.components.MyRepeatingView;

/**
 * Represents a field set panel. A form or page can contain multiple field sets.
 * @author Kai Reinhard (k.reinhard@micromata.de)
 * 
 */
public class RepeatingViewLPanel extends AbstractLPanel
{
  private static final long serialVersionUID = -8623727080968164967L;

  private RepeatingView repeatingView;

  private WebMarkupContainer parentContainer;

  RepeatingViewLPanel(final String id, final PanelContext ctx)
  {
    super(id, ctx);
    parentContainer = new WebMarkupContainer("parent");
    add(parentContainer);
    this.repeatingView = new MyRepeatingView("repeatingView");
    parentContainer.add(this.repeatingView);
  }

  public RepeatingView getRepeatingView()
  {
    return repeatingView;
  }

  @Override
  protected Component getClassModifierComponent()
  {
    return parentContainer;
  }

  public RepeatingViewLPanel setAlignment(final LayoutAlignment alignment)
  {
    final String align;
    if (alignment == LayoutAlignment.RIGHT) {
      align = "right";
    } else if (alignment == LayoutAlignment.MIDDLE) {
      align = "middle";
    } else {
      align = null;
    }
    if (align != null) {
      parentContainer.add(new SimpleAttributeModifier("style", "text-align: " + align + ";"));
    }
    return this;
  }
}
