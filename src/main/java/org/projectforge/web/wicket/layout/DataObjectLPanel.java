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

import java.util.Date;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.markup.html.form.ListMultipleChoice;
import org.apache.wicket.markup.html.form.PasswordTextField;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.Model;
import org.projectforge.common.BeanHelper;
import org.projectforge.common.DatePrecision;
import org.projectforge.core.I18nEnum;
import org.projectforge.web.HtmlHelper;
import org.projectforge.web.calendar.DateTimeFormatter;
import org.projectforge.web.mobile.ActionLinkPanel;
import org.projectforge.web.mobile.ActionLinkType;
import org.projectforge.web.wicket.AbstractSelectPanel;
import org.projectforge.web.wicket.ImageDef;
import org.projectforge.web.wicket.WebConstants;
import org.projectforge.web.wicket.WicketUtils;
import org.projectforge.web.wicket.components.DatePanel;
import org.projectforge.web.wicket.components.DateTimePanel;

/**
 * This panel contains one or more field sets and is a convenient class for rendering data objects as forms or read-only views for different
 * target devices (such as deskop computers or mobile devices). <br/>
 * Every field set contains group panels for grouping data object fields.
 * @author Kai Reinhard (k.reinhard@micromata.de)
 * 
 */
public class DataObjectLPanel extends Panel
{
  private static final long serialVersionUID = 6302571306175282690L;

  private final LayoutContext layoutContext;

  private final RepeatingView fieldSetRepeater;

  private FieldSetLPanel fieldSetPanel;

  private GroupLPanel groupPanel;

  private LabelValueTableLPanel labelValueTablePanel;

  /**
   * Creates and add a new FieldSetLPanel (for normal or mobile version) depending on the layout context. If a previous field set panel
   * exist then it would be tagged as invisible, if that field set doesn't contain any children.
   * @param id
   * @param heading
   */
  public FieldSetLPanel newFieldSetPanel(final String heading)
  {
    closeFieldSetPanel();
    final String id = fieldSetRepeater.newChildId();
    if (layoutContext.isMobile() == true) {
      fieldSetPanel = new FieldSetMobileLPanel(id, heading);
    } else {
      fieldSetPanel = new FieldSetLPanel(id, heading);
    }
    fieldSetRepeater.add(fieldSetPanel);
    return fieldSetPanel;
  }

  public GroupLPanel newGroupPanel()
  {
    return newGroupPanel(null);
  }

  public GroupLPanel newGroupPanel(final String heading)
  {
    closeGroupPanel();
    ensureFieldSetPanel();
    final String id = fieldSetPanel.newChildId();
    if (layoutContext.isMobile() == true) {
      groupPanel = new GroupMobileLPanel(id, heading);
    } else {
      groupPanel = new GroupLPanel(id, heading);
    }
    fieldSetPanel.add(groupPanel);
    return groupPanel;
  }

  public LabelValueTableLPanel newLabelValueTablePanel()
  {
    closeLabelValueTablePanel();
    ensureGroupPanel();
    final String id = groupPanel.newChildId();
    labelValueTablePanel = new LabelValueTableLPanel(id);
    groupPanel.add(labelValueTablePanel);
    return labelValueTablePanel;
  }

  private DataObjectLPanel closeFieldSetPanel()
  {
    closeGroupPanel();
    if (fieldSetPanel != null && fieldSetPanel.hasChildren() == false) {
      fieldSetPanel.setVisible(false);
    }
    fieldSetPanel = null;
    return this;
  }

  private DataObjectLPanel closeGroupPanel()
  {
    closeLabelValueTablePanel();
    if (groupPanel != null && groupPanel.hasChildren() == false) {
      groupPanel.setVisible(false);
    }
    groupPanel = null;
    return this;
  }

  private DataObjectLPanel closeLabelValueTablePanel()
  {
    if (labelValueTablePanel != null && labelValueTablePanel.hasChildren() == false) {
      labelValueTablePanel.setVisible(false);
    }
    labelValueTablePanel = null;
    return this;
  }

  @Override
  protected void onBeforeRender()
  {
    super.onBeforeRender();
    closeFieldSetPanel();
  }

  public LabelLPanel addLabel(final PanelContext ctx)
  {
    return addLabel(ctx.getLabel(), ctx);
  }

  public LabelLPanel addLabel(final String label, final PanelContext ctx)
  {
    ensureGroupPanel();
    final LabelLPanel labelPanel;
    labelPanel = new LabelLPanel(groupPanel.newChildId(), label, ctx);
    groupPanel.add(labelPanel);
    return labelPanel;
  }

  public LabelLPanel addLabel(final Label label, final PanelContext ctx)
  {
    ensureGroupPanel();
    final LabelLPanel labelPanel;
    labelPanel = new LabelLPanel(groupPanel.newChildId(), label, ctx);
    groupPanel.add(labelPanel);
    return labelPanel;
  }

  public LabelLPanel addHelpLabel(final String label, final PanelContext ctx)
  {
    ensureGroupPanel();
    final LabelLPanel labelPanel;
    labelPanel = new LabelLPanel(groupPanel.newChildId(), label, ctx);
    labelPanel.getClassModifierComponent().add(WebConstants.HELP_CLASS);
    groupPanel.add(labelPanel);
    return labelPanel;
  }

  public RepeatingViewLPanel addRepeater(final PanelContext ctx)
  {
    ensureGroupPanel();
    final RepeatingViewLPanel repeatingViewPanel;
    repeatingViewPanel = new RepeatingViewLPanel(groupPanel.newChildId(), ctx);
    groupPanel.add(repeatingViewPanel);
    return repeatingViewPanel;
  }

  /**
   * @param ctx with data and property.
   * @return
   */
  public IField addReadonlyTextField(final PanelContext ctx)
  {
    Validate.notNull(ctx.getData());
    Validate.notNull(ctx.getProperty());
    ensureGroupPanel();
    final Object value = BeanHelper.getNestedProperty(ctx.getData(), ctx.getProperty());
    if (isBlank(value) == true) {
      return new DummyField();
    }
    return addReadonlyTextField(String.valueOf(value), ctx);
  }

  public IField addReadonlyTextField(final String value, final PanelContext ctx)
  {
    ensureGroupPanel();
    IField field;
    if (isBlank(value) == true) {
      field = new DummyField();
    } else {
      ensureLabelValueTablePanel();
      final String wicketId;
      if (ctx.isBreakBetweenLabelAndField() == true) {
        newLabelValueTablePanel();
        wicketId = LabelValueTableLPanel.WICKET_ID_LABEL;
      } else {
        wicketId = LabelValueTableLPanel.WICKET_ID_VALUE;
      }
      if (ctx.getFieldType() == FieldType.E_MAIL) {
        field = new ActionLinkPanel(wicketId, ActionLinkType.MAIL, value, ctx);
      } else if (ctx.getFieldType() == FieldType.PHONE_NO) {
        field = new ActionLinkPanel(wicketId, ActionLinkType.CALL, value, ctx);
      } else if (ctx.getFieldType() == FieldType.MOBILE_PHONE_NO) {
        field = new ActionLinkPanel(wicketId, ActionLinkType.CALL_AND_SMS, value, ctx);
      } else if (ctx.getFieldType() == FieldType.WEB_PAGE) {
        field = new ActionLinkPanel(wicketId, ActionLinkType.EXTERNAL_URL, value, ctx);
      } else {
        field = new LabelLPanel(wicketId, value, ctx);
      }
      labelValueTablePanel.add(ctx.getLabel(), (WebMarkupContainer) field, ctx.isBreakBetweenLabelAndField());
    }
    return field;
  }

  /**
   * @return the created field or a dummy IField if the field is e. g. empty in read-only mode.
   */
  public IField addTextArea(final PanelContext ctx)
  {
    ensureGroupPanel();
    IField field;
    if (layoutContext.isMobileReadonly() == true) {
      final String wicketId;
      if (ctx.isBreakBetweenLabelAndField() == true) {
        newLabelValueTablePanel();
        wicketId = LabelValueTableLPanel.WICKET_ID_LABEL;
      } else {
        wicketId = LabelValueTableLPanel.WICKET_ID_VALUE;
      }
      final Object value = BeanHelper.getNestedProperty(ctx.getData(), ctx.getProperty());
      if (isBlank(value) == true) {
        return new DummyField();
      }
      final String displayValue;
      if (value instanceof String) {
        displayValue = HtmlHelper.formatText((String) value, true);
      } else {
        displayValue = HtmlHelper.formatText(String.valueOf(value), true);
      }
      ensureLabelValueTablePanel();
      final LabelLPanel labelPanel = new LabelLPanel(wicketId, displayValue, ctx);
      labelPanel.getWrappedComponent().setEscapeModelStrings(false);
      field = labelValueTablePanel.add(ctx.getLabel(), labelPanel, ctx.isBreakBetweenLabelAndField());
    } else {
      field = groupPanel.addTextArea(ctx);
    }
    return field;
  }

  public IField addTextArea(final TextArea< ? > textArea, final PanelContext ctx)
  {
    return groupPanel.addTextArea(textArea, ctx);
  }

  /**
   * If the value is type of I18Enum then the localized string is shown in read-only mode.
   * @param ctx
   * @param dropDownChoice value field to add.
   */
  public IField addDropDownChoice(final DropDownChoice< ? > dropDownChoice, final PanelContext ctx)
  {
    ensureGroupPanel();
    IField field;
    LabelLPanel labelPanel = null;
    if (layoutContext.isMobileReadonly() == true) {
      final Object value = BeanHelper.getNestedProperty(ctx.getData(), ctx.getProperty());
      if (isBlank(value) == true) {
        return new DummyField();
      }
      ensureLabelValueTablePanel();
      final String displayValue;
      if (value instanceof I18nEnum) {
        displayValue = getString(((I18nEnum) value).getI18nKey());
      } else {
        displayValue = String.valueOf(value);
      }
      labelPanel = new LabelLPanel(LabelValueTableLPanel.WICKET_ID_VALUE, displayValue, ctx);
      field = labelValueTablePanel.add(ctx.getLabel(), labelPanel);
    } else {
      field = new DropDownChoiceLPanel(groupPanel.newChildId(), dropDownChoice, ctx);
      if (ctx.getLabelLength() != null) {
        labelPanel = new LabelLPanel(groupPanel.newChildId(), (AbstractLPanel) field, ctx);
        groupPanel.add(labelPanel);
      }
      if (ctx.getLabel() != null && ((DropDownChoiceLPanel) field).getDropDownChoice() != null) {
        ((DropDownChoiceLPanel) field).getDropDownChoice().setLabel(new Model<String>(ctx.getLabel()));
      }
      groupPanel.add(field);
    }
    ctx.internalSetValueField(field);
    ctx.internalSetLabelPanel(labelPanel);
    return field;
  }

  public IField addListMultipleChoice(final ListMultipleChoice< ? > listMultipleChoice, final PanelContext ctx)
  {
    ensureGroupPanel();
    IField field;
    LabelLPanel labelPanel = null;
    field = new ListMultipleChoiceLPanel(groupPanel.newChildId(), listMultipleChoice, ctx);
    if (ctx.getLabelLength() != null) {
      labelPanel = new LabelLPanel(groupPanel.newChildId(), (AbstractLPanel) field, ctx);
      groupPanel.add(labelPanel);
    }
    groupPanel.add(field);
    ctx.internalSetValueField(field);
    ctx.internalSetLabelPanel(labelPanel);
    return field;
  }

  /**
   * @param data
   * @param property
   * @param precision
   * @param ctx with data and property.
   */
  public IField addDateFieldPanel(final DatePrecision precision, final PanelContext ctx)
  {
    Validate.notNull(ctx.getData());
    Validate.notNull(ctx.getProperty());
    ensureGroupPanel();
    IField field;
    final Object value = BeanHelper.getNestedProperty(ctx.getData(), ctx.getProperty());
    if (isBlank(value) == true) {
      field = new DummyField();
    } else {
      ensureLabelValueTablePanel();
      final String displayValue;
      if (precision == DatePrecision.DAY) {
        displayValue = DateTimeFormatter.instance().getFormattedDate(value);
      } else {
        displayValue = DateTimeFormatter.instance().getFormattedDateTime((Date) value);
      }
      field = new LabelLPanel(LabelValueTableLPanel.WICKET_ID_VALUE, displayValue, ctx);
      labelValueTablePanel.add(ctx.getLabel(), (WebMarkupContainer) field);
    }
    return field;
  }

  @SuppressWarnings("serial")
  public IField addDateFieldPanel(final DatePanel datePanel, final PanelContext ctx)
  {
    ensureGroupPanel();
    final IField field = new DateFieldLPanel(groupPanel.newChildId(), datePanel, ctx);
    groupPanel.add(new LabelLPanel(groupPanel.newChildId(), (AbstractLPanel) field, ctx));
    ((DateFieldLPanel) field).getDatePanel().setLabel(new Model<String>() {
      @Override
      public String getObject()
      {
        return ctx.getLabel();
      }
    });
    groupPanel.add(field);
    return field;
  }

  @SuppressWarnings("serial")
  public IField addDateTimePanel(final Object data, final String property, final DateTimePanel dateTimePanel, final PanelContext ctx)
  {
    ensureGroupPanel();
    final IField field = new DateTimeFieldLPanel(groupPanel.newChildId(), dateTimePanel, ctx);
    groupPanel.add(new LabelLPanel(groupPanel.newChildId(), (AbstractLPanel) field, ctx));
    ((DateTimeFieldLPanel) field).getDatePanel().setLabel(new Model<String>() {
      @Override
      public String getObject()
      {
        return ctx.getLabel();
      }
    });
    groupPanel.add(field);
    return field;
  }

  public IField addSelectPanel(final AbstractSelectPanel< ? > selectPanel, final PanelContext ctx)
  {
    ensureGroupPanel();
    final SelectLPanel field = new SelectLPanel(groupPanel.newChildId(), selectPanel, ctx);
    if (ctx.getLabelLength() != null) {
      final LabelLPanel labelPanel = new LabelLPanel(groupPanel.newChildId(), field, ctx);
      field.getSelectPanel().setLabel(new Model<String>(ctx.getLabel()));
      groupPanel.add(labelPanel);
      labelPanel.setLabelFor(field.getWrappedComponent());
      ctx.internalSetLabelPanel(labelPanel);
    }
    groupPanel.add(field);
    ctx.internalSetValueField(field);
    if (ctx.getTooltip() != null) {
      field.setTooltip(ctx.getTooltip());
    }
    if (ctx.getLabelPanel() != null && ctx.getLabelTooltip() != null) {
      WicketUtils.addTooltip(ctx.getLabelPanel().getWrappedComponent(), ctx.getLabelTooltip());
    }
    return field;
  }

  public ContainerLPanel addContainer(final WebMarkupContainer container, final PanelContext ctx)
  {
    ensureGroupPanel();
    final ContainerLPanel containerPanel = new ContainerLPanel(groupPanel.newChildId(), container, ctx);
    ctx.internalSetValueField(containerPanel);
    if (ctx.getLabelLength() != null) {
      final LabelLPanel labelPanel = new LabelLPanel(groupPanel.newChildId(), container, ctx);
      groupPanel.add(labelPanel);
    }
    if (container instanceof FormComponent< ? > && ctx.getLabel() != null) {
      ((FormComponent< ? >) container).setLabel(new Model<String>(ctx.getLabel()));
    }
    groupPanel.add(containerPanel);
    return containerPanel;
  }

  /**
   * @param textField
   * @param valueLength
   * @return
   */
  public IField addTextField(final PanelContext ctx)
  {
    ensureGroupPanel();
    IField field;
    if (layoutContext.isMobileReadonly() == true) {
      field = addReadonlyTextField(ctx);
    } else {
      field = groupPanel.addTextField(ctx);
    }
    return field;
  }

  /**
   * @param textField
   * @param valueLength
   * @return
   */
  public void addTextField(final TextField< ? > textField, final PanelContext ctx)
  {
    ensureGroupPanel();
    groupPanel.addTextField(textField, ctx);
  }

  /**
   * @param textField
   * @param valueLength
   * @return
   */
  public void addPasswordTextField(final PasswordTextField textField, final PanelContext ctx)
  {
    ensureGroupPanel();
    groupPanel.addPasswordTextField(textField, ctx);
  }

  public IField addJiraIssuesPanel(final String text, final PanelContext ctx)
  {
    final IField field = new JiraIssuesLPanel(groupPanel.newChildId(), text, ctx);
    groupPanel.add(field);
    return field;
  }

  /**
   * property must be of type boolean.
   * @param ctx data and property should be given.
   */
  public IField addCheckBox(final PanelContext ctx)
  {
    Validate.notNull(ctx.getData());
    Validate.notNull(ctx.getProperty());
    ensureGroupPanel();
    IField field;
    if (layoutContext.isReadonly() == true) {
      final Object value = BeanHelper.getNestedProperty(ctx.getData(), ctx.getProperty());
      if (value != null && value instanceof Boolean && ((Boolean) value) == true) {
        field = new ImageLPanel(groupPanel.newChildId(), ImageDef.ACCEPT);
      } else {
        field = new ImageLPanel(groupPanel.newChildId(), ImageDef.DENY);
      }
    } else {
      field = new CheckBoxLPanel(groupPanel.newChildId(), ctx);
    }
    if (ctx.getLabelLength() != null) {
      final LabelLPanel labelPanel = new LabelLPanel(groupPanel.newChildId(), (AbstractLPanel) field, ctx);
      ctx.internalSetLabelPanel(labelPanel);
      groupPanel.add(labelPanel);
      ((CheckBoxLPanel) field).getCheckBox().setLabel(new Model<String>(ctx.getLabel()));
      if (ctx.getTooltip() != null) {
        labelPanel.setTooltip(ctx.getTooltip());
      }
    }
    if (ctx.getTooltip() != null) {
      ((CheckBoxLPanel) field).setTooltip(ctx.getTooltip());
    }
    groupPanel.add(field);
    ctx.internalSetValueField(field);
    return field;
  }

  /**
   * property must be of type boolean.
   * @param data
   * @param property
   * @return
   */
  public IField addImage(final ImageDef imageDef)
  {
    final IField field = new ImageLPanel(groupPanel.newChildId(), imageDef);
    groupPanel.add(field);
    return field;
  }

  private boolean isBlank(final Object value)
  {
    if (value == null) {
      return true;
    } else if (value instanceof String == true) {
      return StringUtils.isBlank((String) value);
    }
    return false;
  }

  /**
   * @param container The parent container.
   * @param layoutContext The current used layout (ro/rw, mobile or desktop).
   */
  public DataObjectLPanel(final String id, final LayoutContext layoutContext)
  {
    super(id);
    this.layoutContext = layoutContext;
    this.fieldSetRepeater = new RepeatingView("fieldSetRepeater");
    add(fieldSetRepeater);
  }

  private void ensureFieldSetPanel()
  {
    if (fieldSetPanel == null) {
      newFieldSetPanel(null);
    }
  }

  private void ensureGroupPanel()
  {
    if (groupPanel == null) {
      newGroupPanel();
    }
  }

  private void ensureLabelValueTablePanel()
  {
    if (labelValueTablePanel == null) {
      newLabelValueTablePanel();
    }
  }
}
