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
import org.apache.wicket.markup.html.panel.Panel;

/**
 * Represents a entry of a group panel. This can be a label, text field or other form components.
 * @author Kai Reinhard (k.reinhard@micromata.de)
 * 
 */
public abstract class AbstractLayoutPanel extends Panel implements ComponentWrapper
{
  private static final long serialVersionUID = -6769384502876947092L;

  protected LayoutLength length;

  protected LayoutLength indent;

  protected boolean breakBefore;

  private boolean rendered;

  protected String classAttributeAppender;

  public AbstractLayoutPanel(final String id, final LayoutLength length)
  {
    super(id);
    this.length = length;
  }

  /**
   * Render new line before this component.
   */
  public boolean isBreakBefore()
  {
    return breakBefore;
  }

  public AbstractLayoutPanel setBreakBefore()
  {
    this.breakBefore = true;
    return this;
  }

  public AbstractLayoutPanel setBreakBefore(final boolean breakBefore)
  {
    this.breakBefore = breakBefore;
    return this;
  }

  /**
   * Indent this compontent.
   */
  public LayoutLength getIndent()
  {
    return indent;
  }

  public AbstractLayoutPanel setIndent(final LayoutLength indent)
  {
    this.indent = indent;
    return this;
  }

  @Override
  protected void onBeforeRender()
  {
    super.onBeforeRender();
    if (rendered == true) {
      return;
    }
    rendered = true;
    getClassModifierComponent().add(getClassAttributeModifier());
  }

  @Override
  public String getMarkupId()
  {
    return getClassModifierComponent().getMarkupId();
  }

  /**
   * The length, break before and intend will be added as class attribute to this component.
   * @return
   */
  protected abstract Component getClassModifierComponent();

  @Override
  public Component getWrappedComponent()
  {
    return getClassModifierComponent();
  }

  private SimpleAttributeModifier getClassAttributeModifier()
  {
    final StringBuffer buf = new StringBuffer();
    if (breakBefore == true) {
      buf.append(LayoutConstants.CLASS_BREAK_BEFORE).append(" ");
    }
    if (indent != null) {
      buf.append("put").append(indent.getClassAttrValue()).append(" ");
    }
    buf.append(length.getClassAttrValue());
    if (this.classAttributeAppender != null) {
      buf.append(" ").append(this.classAttributeAppender);
    }
    return new SimpleAttributeModifier("class", buf.toString());
  }
}
