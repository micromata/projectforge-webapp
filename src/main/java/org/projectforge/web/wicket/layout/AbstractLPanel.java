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
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;
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

  protected PanelContext ctx;

  private boolean rendered;

  protected String classAttributeAppender;

  public AbstractLPanel(final String id)
  {
    super(id);
  }

  public AbstractLPanel(final String id, final PanelContext ctx)
  {
    super(id);
    this.ctx = ctx;
  }

  /**
   * @param tooltip
   * @return this for chaining.
   */
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
    if (ctx == null) {
      return;
    }
    final AbstractBehavior classAttributeModifier = getClassAttributeModifier();
    if (classAttributeModifier != null && getClassModifierComponent() != null) {
      getClassModifierComponent().add(classAttributeModifier);
    }
    final Component wrappedComponent = getWrappedComponent();
    if (wrappedComponent != null) {
      String align = null;
      if (ctx.getAlignment() != null) {
        if (ctx.getAlignment() == LayoutAlignment.MIDDLE) {
          align = "middle";
        } else if (ctx.getAlignment() == LayoutAlignment.RIGHT) {
          align = "right";
        }
      }
      if (align != null) {
        wrappedComponent.add(new AttributeAppendModifier("style", "text-align: " + align + ";"));
      }
      final String attr = getCssStyle();
      if (attr != null) {
        wrappedComponent.add(new SimpleAttributeModifier("style", attr));
      }
      if (wrappedComponent instanceof FormComponent< ? >) {
        final FormComponent< ? > formComponent = (FormComponent< ? >) wrappedComponent;
        if (ctx.getLabel() != null) {
          formComponent.setLabel(new Model<String>(ctx.getLabel()));
        }
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
   * The length, break before and intend will be added as class attribute to this component.
   * @return
   */
  protected abstract Component getClassModifierComponent();

  @Override
  public Component getWrappedComponent()
  {
    return getClassModifierComponent();
  }

  protected String getValueLength()
  {
    if (ctx.getValueLength() != null) {
      return ctx.getValueLength().getClassAttrValue();
    }
    return null;
  }

  protected String getStrong()
  {
    if (ctx.isStrong() == true) {
      return LayoutConstants.CLASS_STRONG;
    } else {
      return null;
    }
  }

  protected String getBreakBefore()
  {
    if (ctx.isBreakBefore() == true || ctx.isBreakBetweenLabelAndField() == true) {
      return LayoutConstants.CLASS_BREAK_BEFORE;
    } else {
      return null;
    }
  }

  protected String getCssStyle()
  {
    return ctx.getCssStyle();
  }

  private AbstractBehavior getClassAttributeModifier()
  {
    final StringBuffer buf = new StringBuffer();
    boolean first = true;
    String attr = getBreakBefore();
    if (attr != null) {
      first = StringHelper.append(buf, first, attr, " ");
    }
    if (ctx.getIndent() != null) {
      first = StringHelper.append(buf, first, "put", " ");
      buf.append(ctx.getIndent().getClassAttrValue());
    }
    attr = getValueLength();
    if (attr != null) {
      first = StringHelper.append(buf, first, attr, " ");
    }
    if (this.classAttributeAppender != null) {
      first = StringHelper.append(buf, first, this.classAttributeAppender, " ");
    }
    attr = getStrong();
    if (attr != null) {
      first = StringHelper.append(buf, first, attr, " ");
    }
    if (first == false) {
      return new AttributeAppendModifier("class", buf.toString());
    } else {
      return null;
    }
  }
}
