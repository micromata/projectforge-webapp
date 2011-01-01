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

import java.util.Date;

import org.apache.commons.lang.StringUtils;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.projectforge.common.BeanHelper;
import org.projectforge.common.DatePrecision;
import org.projectforge.core.I18nEnum;
import org.projectforge.web.HtmlHelper;
import org.projectforge.web.calendar.DateTimeFormatter;
import org.projectforge.web.mobile.ActionLinkPanel;
import org.projectforge.web.mobile.ActionLinkType;
import org.projectforge.web.wicket.ImageDef;
import org.projectforge.web.wicket.components.DatePanel;

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

  private LayoutContext layoutContext;

  private RepeatingView fieldSetRepeater;

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

  public LabelLPanel addLabel(final String label, final LayoutLength labelLength)
  {
    final LabelLPanel labelPanel;
    if (layoutContext.isMobile() == true) {
      labelPanel = new LabelValueHeadingMobileLPanel(groupPanel.newChildId(), label);
    } else {
      labelPanel = new LabelLPanel(groupPanel.newChildId(), labelLength, label);
    }
    groupPanel.add(labelPanel);
    return labelPanel;
  }

  /**
   * @return the created field.
   */
  public IField addMaxLengthTextField(final Object data, final String property, final String label, final LayoutLength labelLength,
      final LayoutLength valueLength)
  {
    return addMaxLengthTextField(data, property, label, labelLength, valueLength, null);
  }

  /**
   * @return the created field or a dummy IField if the field is e. g. empty in read-only mode.
   */
  public IField addMaxLengthTextField(final Object data, final String property, final String label, final LayoutLength labelLength,
      final LayoutLength valueLength, final FieldType fieldType)
  {
    IField field;
    if (layoutContext.isReadonly() == true) {
      final Object value = BeanHelper.getNestedProperty(data, property);
      if (isBlank(value) == true) {
        field = new DummyField();
      } else {
        ensureLabelValueTablePanel();
        if (fieldType == FieldType.E_MAIL) {
          field = new ActionLinkPanel(LabelValueTableLPanel.WICKET_ID_VALUE, ActionLinkType.MAIL, String.valueOf(value));
        } else {
          field = new LabelLPanel(LabelValueTableLPanel.WICKET_ID_VALUE, labelLength, String.valueOf(value));
        }
        labelValueTablePanel.add(label, (WebMarkupContainer) field);
      }
    } else {
      field = groupPanel.addMaxLengthTextField(data, property, label, labelLength, valueLength);
    }
    return field;
  }

  /**
   * @return the created field or a dummy IField if the field is e. g. empty in read-only mode.
   */
  public IField addMaxLengthTextArea(final Object data, final String property, final String label, final LayoutLength labelLength,
      final LayoutLength valueLength)
  {
    IField field;
    if (layoutContext.isReadonly() == true) {
      final Object value = BeanHelper.getNestedProperty(data, property);
      if (isBlank(value) == true) {
        field = new DummyField();
      } else {
        ensureLabelValueTablePanel();
        final String displayValue;
        if (value instanceof String) {
          displayValue = HtmlHelper.formatText((String) value, true);
        } else {
          displayValue = HtmlHelper.formatText(String.valueOf(value), true);
        }
        final LabelLPanel labelPanel = new LabelLPanel(LabelValueTableLPanel.WICKET_ID_VALUE, valueLength, displayValue);
        labelPanel.getWrappedComponent().setEscapeModelStrings(false);
        field = labelPanel;
        labelValueTablePanel.add(label, (WebMarkupContainer) field);
      }
    } else {
      field = groupPanel.addMaxLengthTextArea(data, property, label, labelLength, valueLength);
    }
    return field;
  }

  /**
   * @return the created field or a dummy IField if the field is e. g. empty in read-only mode.
   */
  public IField addMaxLengthTextArea(final Object data, final String property, final LayoutLength valueLength)
  {
    IField field;
    if (layoutContext.isReadonly() == true) {
      final Object value = BeanHelper.getNestedProperty(data, property);
      if (isBlank(value) == true) {
        field = new DummyField();
      } else {
        final String displayValue;
        if (value instanceof String) {
          displayValue = HtmlHelper.formatText((String) value, true);
        } else {
          displayValue = HtmlHelper.formatText(String.valueOf(value), true);
        }
        final LabelLPanel labelPanel = new LabelLPanel(groupPanel.newChildId(), valueLength, displayValue);
        labelPanel.getWrappedComponent().setEscapeModelStrings(false);
        field = labelPanel;
        groupPanel.add(field);
      }
    } else {
      field = groupPanel.addMaxLengthTextArea(data, property, valueLength);
    }
    return field;
  }

  /**
   * If the value is type of I18Enum then the localized string is shown in read-only mode.
   */
  public IField addDropDownChoice(final Object data, final String property, final String label, final LayoutLength labelLength,
      final DropDownChoice< ? > dropDownChoice, final LayoutLength valueLength)
  {
    IField field;
    if (layoutContext.isReadonly() == true) {
      final Object value = BeanHelper.getNestedProperty(data, property);
      if (isBlank(value) == true) {
        field = new DummyField();
      } else {
        ensureLabelValueTablePanel();
        final String displayValue;
        if (value instanceof I18nEnum) {
          displayValue = getString(((I18nEnum) value).getI18nKey());
        } else {
          displayValue = String.valueOf(value);
        }
        field = new LabelLPanel(LabelValueTableLPanel.WICKET_ID_VALUE, labelLength, displayValue);
        labelValueTablePanel.add(label, (WebMarkupContainer) field);
      }
    } else {
      if (layoutContext.isMobile() == true) {
        field = new DropDownChoiceMobileLPanel(groupPanel.newChildId(), valueLength, dropDownChoice);
      } else {
        field = new DropDownChoiceLPanel(groupPanel.newChildId(), valueLength, dropDownChoice);
      }
      groupPanel.add(new LabelLPanel(groupPanel.newChildId(), labelLength, label, (AbstractLPanel) field, true));
      groupPanel.add(field);
    }
    return field;
  }

  public IField addDateFieldPanel(final Object data, final String property, final String label, final LayoutLength labelLength,
      final DatePrecision precision, final LayoutLength valueLength)
  {
    IField field;
    final Object value = BeanHelper.getNestedProperty(data, property);
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
      field = new LabelLPanel(LabelValueTableLPanel.WICKET_ID_VALUE, labelLength, displayValue);
      labelValueTablePanel.add(label, (WebMarkupContainer) field);
    }
    return field;
  }

  public IField addDateFieldPanel(final Object data, final String property, final String label, final LayoutLength labelLength,
      final DatePanel datePanel, final LayoutLength valueLength)
  {
    IField field = new DateFieldLPanel(groupPanel.newChildId(), valueLength, datePanel);
    groupPanel.add(new LabelLPanel(groupPanel.newChildId(), labelLength, label, (AbstractLPanel) field, true));
    groupPanel.add(field);
    return field;
  }

  /**
   * property must be of type boolean.
   * @param data
   * @param property
   * @return
   */
  public IField addCheckBox(final Object data, final String property)
  {
    IField field;
    if (layoutContext.isReadonly() == true) {
      final Object value = BeanHelper.getNestedProperty(data, property);
      if (value != null && value instanceof Boolean && ((Boolean) value) == true) {
        field = addImage(ImageDef.ACCEPT);
      } else {
        field = addImage(ImageDef.DENY);
      }
    } else {
      field = new CheckBoxLPanel(groupPanel.newChildId(), data, property);
      groupPanel.add(field);
    }
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
