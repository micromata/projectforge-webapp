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
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.PasswordTextField;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.Model;
import org.projectforge.web.wicket.WicketUtils;

/**
 * Represents a group panel. A field set, form or page can contain multiple group panels. A group panel groups fields.
 * @author Kai Reinhard (k.reinhard@micromata.de)
 * 
 */
public class GroupLPanel extends Panel
{
  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(GroupLPanel.class);

  private static final long serialVersionUID = -8760386387270114082L;

  /**
   * The markup wicket id of the heading label.
   */
  public static final String HEADING_ID = "heading";

  protected Label headingLabel;

  private RepeatingView entriesRepeater;

  private boolean hasChildren;

  GroupLPanel(final String id)
  {
    super(id);
  }

  GroupLPanel(final String id, final String heading)
  {
    this(id);
    if (heading != null) {
      setHeading(heading);
    }
  }

  public TextFieldLPanel addTextField(final PanelContext ctx)
  {
    final TextFieldLPanel textFieldPanel = new TextFieldLPanel(newChildId(), ctx);
    addTextField(textFieldPanel, ctx);
    return textFieldPanel;
  }

  public TextFieldLPanel addTextField(final TextField< ? > textField, final PanelContext ctx)
  {
    final TextFieldLPanel textFieldPanel = new TextFieldLPanel(newChildId(), textField, ctx);
    addTextField(textFieldPanel, ctx);
    return textFieldPanel;
  }

  public TextFieldLPanel addPasswordTextField(final PasswordTextField textField, final PanelContext ctx)
  {
    final TextFieldLPanel textFieldPanel = new PasswordTextFieldLPanel(newChildId(), textField, ctx);
    addTextField(textFieldPanel, ctx);
    return textFieldPanel;
  }

  private void addTextField(final TextFieldLPanel textFieldPanel, final PanelContext ctx)
  {
    ctx.internalSetValueField(textFieldPanel);
    if (ctx.getLabelLength() != null) {
      final LabelLPanel labelPanel = new LabelLPanel(newChildId(), ctx).setLabelFor(textFieldPanel.getTextField());
      ctx.internalSetLabelPanel(labelPanel);
      add(labelPanel);
    }
    if (ctx.getLabel() != null) {
      textFieldPanel.getTextField().setLabel(new Model<String>(ctx.getLabel()));
    } else {
      log.warn("No label given for text field component.");
    }
    if (ctx.getTooltip() != null) {
      WicketUtils.addTooltip(textFieldPanel.getTextField(), ctx.getTooltip());
    }
    if (ctx.getLabelPanel() != null && ctx.getLabelTooltip() != null) {
      WicketUtils.addTooltip(ctx.getLabelPanel().getWrappedComponent(), ctx.getLabelTooltip());
    }
    add(textFieldPanel);
  }

  public TextAreaLPanel addTextArea(final PanelContext ctx)
  {
    final TextAreaLPanel textAreaPanel = new TextAreaLPanel(newChildId(), ctx);
    return addTextArea(textAreaPanel, ctx);
  }

  public TextAreaLPanel addTextArea(final TextArea< ? > textArea, final PanelContext ctx)
  {
    final TextAreaLPanel textAreaPanel = new TextAreaLPanel(newChildId(), textArea, ctx);
    return addTextArea(textAreaPanel, ctx);
  }

  private TextAreaLPanel addTextArea(final TextAreaLPanel textAreaPanel, final PanelContext ctx)
  {
    if (ctx.getLabelLength() != null) {
      add(new LabelLPanel(newChildId(), ctx).setLabelFor(textAreaPanel.getTextArea()));
    }
    if (ctx.getLabel() != null) {
      textAreaPanel.getTextArea().setLabel(new Model<String>(ctx.getLabel()));
    }
    if (ctx.getTooltip() != null) {
      WicketUtils.addTooltip(textAreaPanel.getTextArea(), ctx.getTooltip());
    }
    if (ctx.getLabelPanel() != null && ctx.getLabelTooltip() != null) {
      WicketUtils.addTooltip(ctx.getLabelPanel().getWrappedComponent(), ctx.getLabelTooltip());
    }
    add(textAreaPanel);
    return textAreaPanel;
  }

  public GroupLPanel add(final AbstractLPanel layoutPanel)
  {
    hasChildren = true;
    entriesRepeater.add(layoutPanel);
    return this;
  }

  public GroupLPanel add(final IField field)
  {
    hasChildren = true;
    entriesRepeater.add((Component) field);
    return this;
  }

  public boolean hasChildren()
  {
    return hasChildren;
  }

  public String newChildId()
  {
    if (entriesRepeater == null) {
      init();
    }
    return entriesRepeater.newChildId();
  }

  /**
   * Should only be called manually if no children are added to this field set. Otherwise it'll be initialized at the first call of
   * newChildId().
   */
  public GroupLPanel init()
  {
    if (entriesRepeater != null) {
      return this;
    }
    if (this.headingLabel != null) {
      add(this.headingLabel);
    } else {
      add(new Label(HEADING_ID, "[invisible]").setVisible(false));
    }
    entriesRepeater = new RepeatingView("entriesRepeater");
    add(entriesRepeater);
    return this;
  }

  public GroupLPanel setHeading(final Label headingLabel)
  {
    this.headingLabel = headingLabel;
    return this;
  }

  /**
   * @param heading
   * @return this for chaining.
   */
  public GroupLPanel setHeading(final String heading)
  {
    if (heading != null) {
      this.headingLabel = new Label(HEADING_ID, heading);
    }
    return this;
  }
}
