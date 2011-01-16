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
import org.apache.wicket.behavior.AbstractBehavior;
import org.apache.wicket.behavior.SimpleAttributeModifier;
import org.apache.wicket.markup.html.panel.Panel;
import org.projectforge.common.StringHelper;
import org.projectforge.web.wicket.AttributeAppendModifier;
import org.projectforge.web.wicket.WicketUtils;

/**
 * Represents a entry of a group panel. This can be a label, text field or other form components.
 * @author Kai Reinhard (k.reinhard@micromata.de)
 * 
 */
public abstract class AbstractLPanel extends Panel implements ComponentWrapper, IField
{
  private static final long serialVersionUID = -6769384502876947092L;

  protected LayoutLength length;

  protected LayoutLength indent;

  protected boolean breakBefore;

  private boolean rendered;

  private boolean strong;

  protected String classAttributeAppender;

  private LayoutAlignment alignment;

  public AbstractLPanel(final String id)
  {
    super(id);
  }

  public AbstractLPanel(final String id, final LayoutLength length)
  {
    super(id);
    this.length = length;
  }

  /**
   * @param length
   * @return this for chaining.
   */
  public AbstractLPanel setLength(LayoutLength length)
  {
    this.length = length;
    return this;
  }

  @Override
  public IField setAlignment(LayoutAlignment aligment)
  {
    this.alignment = aligment;
    return this;
  }

  /**
   * Render new line before this component.
   */
  public boolean isBreakBefore()
  {
    return breakBefore;
  }

  /**
   * @return this for chaining.
   */
  public AbstractLPanel setBreakBefore()
  {
    this.breakBefore = true;
    return this;
  }

  /**
   * 
   * @param breakBefore
   * @return this for chaining.
   */
  public AbstractLPanel setBreakBefore(final boolean breakBefore)
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

  /**
   * @param indent
   * @return this for chaining.
   */
  public AbstractLPanel setIndent(final LayoutLength indent)
  {
    this.indent = indent;
    return this;
  }

  @Override
  public AbstractLPanel setStrong()
  {
    this.strong = true;
    return this;
  }

  @Override
  public IField setCssStyle(String cssStyle)
  {
    getWrappedComponent().add(new SimpleAttributeModifier("style", cssStyle));
    return this;
  }

  /**
   * @param tooltip
   * @return this for chaining.
   */
  @Override
  public AbstractLPanel setTooltip(final String tooltip)
  {
    WicketUtils.addTooltip(getWrappedComponent(), tooltip);
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
    final AbstractBehavior classAttributeModifier = getClassAttributeModifier();
    if (classAttributeModifier != null && getClassModifierComponent() != null) {
      getClassModifierComponent().add(classAttributeModifier);
    }
    final Component wrappedComponent = getWrappedComponent();
    if (wrappedComponent != null) {
      String align = null;
      if (this.alignment != null) {
        if (this.alignment == LayoutAlignment.MIDDLE) {
          align = "middle";
        } else if (this.alignment == LayoutAlignment.RIGHT) {
          align = "right";
        }
      }
      if (align != null) {
        wrappedComponent.add(new AttributeAppendModifier("style", "text-align: " + align + ";"));
      }
    }
  }

  @Override
  public String getMarkupId()
  {
    if (getClassModifierComponent() != null && getClassModifierComponent() != this) {
      return getClassModifierComponent().getMarkupId();
    } else {
      return super.getMarkupId();
    }
  }

  /**
   * Sets style="..." for the wrapped component.
   * @param css
   */
  public AbstractLPanel setStyle(final String css)
  {
    if (getClassModifierComponent() != null) {
      getClassModifierComponent().add(new SimpleAttributeModifier("style", css));
    }
    return this;
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

  private AbstractBehavior getClassAttributeModifier()
  {
    final StringBuffer buf = new StringBuffer();
    boolean first = true;
    if (breakBefore == true) {
      first = StringHelper.append(buf, first, LayoutConstants.CLASS_BREAK_BEFORE, " ");
    }
    if (indent != null) {
      first = StringHelper.append(buf, first, "put", " ");
      buf.append(indent.getClassAttrValue());
    }
    if (length != null) {
      first = StringHelper.append(buf, first, length.getClassAttrValue(), " ");
    }
    if (this.classAttributeAppender != null) {
      first = StringHelper.append(buf, first, this.classAttributeAppender, " ");
    }
    if (this.strong == true) {
      first = StringHelper.append(buf, first, "strong", " ");
    }
    if (first == false) {
      return new AttributeAppendModifier("class", buf.toString());
    } else {
      return null;
    }
  }

  /**
   * Does nothing at default.
   * @return this for chaining.
   * @see org.projectforge.web.wicket.layout.IField#setFocus()
   */
  @Override
  public AbstractLPanel setFocus()
  {
    return this;
  }

  /**
   * Does nothing at default.
   * @return this for chaining.
   * @see org.projectforge.web.wicket.layout.IField#setRequired()
   */
  @Override
  public IField setRequired()
  {
    return this;
  }
}
