/////////////////////////////////////////////////////////////////////////////
//
// Project ProjectForge Community Edition
//         www.projectforge.org
//
// Copyright (C) 2001-2010 Kai Reinhard (k.reinhard@me.com)
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
import org.apache.wicket.markup.html.basic.Label;

/**
 * Represents a field set panel. A form or page can contain multiple field sets.
 * @author Kai Reinhard (k.reinhard@micromata.de)
 * 
 */
public class LabelPanel extends AbstractLayoutPanel
{
  private static final long serialVersionUID = -8760386387270114082L;

  /**
   * Wicket id.
   */
  public static final String LABEL_ID = "label";

  private Label label;

  public LabelPanel(final String id, final LayoutLength length, final String label)
  {
    this(id, length, new Label(LABEL_ID, label));
  }

  public LabelPanel(final String id, final LayoutLength length, final Label label)
  {
    super(id, length);
    this.label = label;
    add(label);
  }

  @Override
  protected Component getClassModifierComponent()
  {
    return label;
  }

  public LabelPanel setLabelFor(final Component component)
  {
    if (component instanceof ComponentWrapper) {
      label.add(new SimpleAttributeModifier("for", ((ComponentWrapper)component).getWrappedComponent().getMarkupId()));
      ((ComponentWrapper) component).getWrappedComponent().setOutputMarkupId(true);
    } else {
      label.add(new SimpleAttributeModifier("for", component.getMarkupId()));
      component.setOutputMarkupId(true);
    }
    return this;
  }
}
