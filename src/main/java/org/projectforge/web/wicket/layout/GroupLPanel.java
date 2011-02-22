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
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.PasswordTextField;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.Model;

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

  /**
   * @deprecated Use addTextField(PanelContext) instead.
   */
  public TextFieldLPanel addTextField(final Object dataObject, final String property, final String label, final LayoutLength labelLength,
      final LayoutLength valueLength)
  {
    return addTextField(dataObject, property, label, labelLength, valueLength, null, false);
  }

  /**
   * @deprecated Use addTextField(PanelContext) instead.
   */
  public TextFieldLPanel addTextField(final Object dataObject, final String property, final String label, final LayoutLength labelLength,
      final LayoutLength valueLength, final boolean newLineBetweenLabelAndTextField)
  {
    return addTextField(dataObject, property, label, labelLength, valueLength, null, newLineBetweenLabelAndTextField);
  }

  /**
   * @deprecated Use addTextField(PanelContext) instead.
   */
  public TextFieldLPanel addTextField(final Object dataObject, final String property, final String label, final LayoutLength labelLength,
      final LayoutLength valueLength, final FieldType fieldType, final boolean newLineBetweenLabelAndTextField)
  {
    return addTextField(new PanelContext(dataObject, property, valueLength, label, labelLength) //
        .setFieldType(fieldType) //
        .setBreakBetweenLabelAndField(newLineBetweenLabelAndTextField));
  }

  public TextFieldLPanel addTextField(final PanelContext ctx)
  {
    final TextFieldLPanel textFieldPanel = new TextFieldLPanel(newChildId(), ctx.getValueLength(), ctx.getData(), ctx.getProperty());
    addTextField(textFieldPanel, ctx);
    return textFieldPanel;
  }

  public TextFieldLPanel addTextField(final TextField< ? > textField, final PanelContext ctx)
  {
    final TextFieldLPanel textFieldPanel = new TextFieldLPanel(newChildId(), ctx.getValueLength(), textField);
    addTextField(textFieldPanel, ctx);
    return textFieldPanel;
  }

  public TextFieldLPanel addPasswordTextField(final PasswordTextField textField, final PanelContext ctx)
  {
    final TextFieldLPanel textFieldPanel = new PasswordTextFieldLPanel(newChildId(), ctx.getValueLength(), textField);
    addTextField(textFieldPanel, ctx);
    return textFieldPanel;
  }

  private void addTextField(final TextFieldLPanel textFieldPanel, final PanelContext ctx)
  {
    ctx.internalSetValueField(textFieldPanel);
    if (ctx.getLabel() != null) {
      textFieldPanel.getTextField().setLabel(new Model<String>(ctx.getLabel()));
    }
    if (ctx.getTooltip() != null) {
      textFieldPanel.setTooltip(ctx.getTooltip());
    }
    if (ctx.isRequired() == true) {
      textFieldPanel.setRequired();
    }
    if (ctx.isStrong() == true) {
      textFieldPanel.setStrong();
    }
    if (ctx.isReadonly() == true) {
      log.error("Field read-only isn't yet supported by this method. If needed, please implement this.");
    }
    if (ctx.getLabelLength() != null) {
      final LabelLPanel labelPanel = new LabelLPanel(newChildId(), ctx.getLabelLength(), ctx.getLabel()).setLabelFor(textFieldPanel
          .getTextField());
      ctx.internalSetLabelPanel(labelPanel);
      if (ctx.isBreakBefore() == true) {
        labelPanel.setBreakBefore();
      }
      if (ctx.getTooltip() != null) {
        labelPanel.setTooltip(ctx.getTooltip());
      }
      add(labelPanel);
    }
    if (ctx.isBreakBetweenLabelAndField() == true) {
      textFieldPanel.setBreakBefore();
    }
    add(textFieldPanel);
  }

  /**
   * @deprecated Use addTextField(PanelContext) instead.
   */
  public TextFieldLPanel addTextField(final Object dataObject, final String property, final LayoutLength valueLength)
  {
    final TextFieldLPanel textFieldPanel = new TextFieldLPanel(newChildId(), valueLength, dataObject, property);
    add(textFieldPanel);
    return textFieldPanel;
  }

  @SuppressWarnings("serial")
  public TextAreaLPanel addTextArea(final Object dataObject, final String property, final String label, final LayoutLength labelLength,
      final LayoutLength valueLength)
  {
    final TextAreaLPanel textAreaPanel = new TextAreaLPanel(newChildId(), valueLength, dataObject, property);
    add(new LabelLPanel(newChildId(), labelLength, label).setLabelFor(textAreaPanel.getTextArea()).setBreakBefore());
    textAreaPanel.getTextArea().setLabel(new Model<String>() {
      @Override
      public String getObject()
      {
        return label;
      }
    });
    add(textAreaPanel);
    return textAreaPanel;
  }

  @SuppressWarnings("serial")
  public TextAreaLPanel addTextArea(final Object dataObject, final String property, final String label, final LayoutLength labelLength,
      final LayoutLength valueLength, final boolean newLineBetweenLabelAndTextarea)
  {
    final TextAreaLPanel textAreaPanel = new TextAreaLPanel(newChildId(), valueLength, dataObject, property);
    add(new LabelLPanel(newChildId(), labelLength, label).setLabelFor(textAreaPanel.getTextArea()).setBreakBefore());
    if (newLineBetweenLabelAndTextarea == true) {
      textAreaPanel.setBreakBefore();
    }
    textAreaPanel.getTextArea().setLabel(new Model<String>() {
      @Override
      public String getObject()
      {
        return label;
      }
    });
    add(textAreaPanel);
    return textAreaPanel;
  }

  public TextAreaLPanel addTextArea(final Object dataObject, final String property, final LayoutLength valueLength)
  {
    final TextAreaLPanel textAreaPanel = new TextAreaLPanel(newChildId(), valueLength, dataObject, property);
    add(textAreaPanel);
    return textAreaPanel;
  }

  public TextAreaLPanel addTextArea(final PanelContext ctx)
  {
    final TextAreaLPanel textAreaPanel = new TextAreaLPanel(newChildId(), ctx.getValueLength(), ctx.getData(), ctx.getProperty());
    if (ctx.getLabelLength() != null) {
      final LabelLPanel labelPanel = new LabelLPanel(newChildId(), ctx.getLabelLength(), ctx.getLabel()).setLabelFor(textAreaPanel.getTextArea());
      labelPanel.setBreakBefore();
      ctx.internalSetLabelPanel(labelPanel);
      add(labelPanel);
    }
    if (ctx.getCssStyle() != null) {
      textAreaPanel.setCssStyle(ctx.getCssStyle());
    }
    if (ctx.getLabel() != null) {
      textAreaPanel.getTextArea().setLabel(new Model<String>(ctx.getLabel()));
    }
    if (ctx.isBreakBetweenLabelAndField() == true) {
      textAreaPanel.setBreakBefore();
    }
    ctx.internalSetValueField(textAreaPanel);
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
