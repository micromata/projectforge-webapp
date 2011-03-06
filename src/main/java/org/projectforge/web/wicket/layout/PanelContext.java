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

import java.io.Serializable;

/**
 * Set properties to the add methods which are supported by the panels.
 * @author Kai Reinhard (k.reinhard@micromata.de)
 * 
 */
public class PanelContext implements Serializable
{
  private static final long serialVersionUID = -4958909833492022321L;

  private String label;

  private String tooltip;

  private LayoutLength labelLength;

  private LayoutLength valueLength;

  private Object data;

  private String property;

  private FieldType fieldType;

  private LayoutAlignment aligment;

  private LabelLPanel labelPanel;

  private IField valueField;

  private boolean breakBefore = true, breakBetweenLabelAndField, strong, required, focus, readonly;

  private String cssStyle;

  /**
   * Should only be used for DropDownChoices.
   * @param data
   * @param property
   */
  public PanelContext(final Object data, final String property)
  {
    this.data = data;
    this.property = property;
  }

  /**
   * Should only be used for DropDownChoices.
   * @param data
   * @param property
   */
  public PanelContext(final Object data, final String property, final LayoutLength valueLength)
  {
    this.data = data;
    this.property = property;
    this.valueLength = valueLength;
  }

  /**
   * @param data
   * @param property
   * @param label No label is printed. Only for displaying validation messages.
   */
  public PanelContext(final Object data, final String property, final LayoutLength valueLength, final String label)
  {
    this.data = data;
    this.property = property;
    this.valueLength = valueLength;
    this.label = label;
  }

  public PanelContext(final Object data, final String property, final LayoutLength valueLength, final String label,
      final LayoutLength labelLength)
  {
    this.data = data;
    this.property = property;
    this.valueLength = valueLength;
    this.label = label;
    this.labelLength = labelLength;
  }

  public PanelContext(final LayoutLength valueLength, final String label, final LayoutLength labelLength)
  {
    this.valueLength = valueLength;
    this.label = label;
    this.labelLength = labelLength;
  }

  public PanelContext(final LayoutLength valueLength)
  {
    this.valueLength = valueLength;
  }

  public String getLabel()
  {
    return label;
  }

  public LayoutLength getLabelLength()
  {
    return labelLength;
  }

  public LayoutLength getValueLength()
  {
    return valueLength;
  }

  public Object getData()
  {
    return data;
  }

  public String getProperty()
  {
    return property;
  }

  public FieldType getFieldType()
  {
    return fieldType;
  }

  /**
   * @param fieldType
   * @return this for chaining.
   */
  public PanelContext setFieldType(final FieldType fieldType)
  {
    this.fieldType = fieldType;
    return this;
  }

  /**
   * 
   * @param tooltip
   * @return this for chaining.
   */
  public PanelContext setTooltip(final String tooltip)
  {
    this.tooltip = tooltip;
    return this;
  }

  public String getTooltip()
  {
    return tooltip;
  }

  public boolean isBreakBefore()
  {
    return breakBefore;
  }

  /**
   * Break before this panel (containing field and label as optional). Default is true.
   * @param breakBefore
   * @return this for chaining.
   */
  public PanelContext setBreakBefore(final boolean breakBefore)
  {
    this.breakBefore = breakBefore;
    return this;
  }

  public boolean isBreakBetweenLabelAndField()
  {
    return breakBetweenLabelAndField;
  }

  /**
   * @param breakBetweenLabelAndProperty
   * @return this for chaining.
   */
  public PanelContext setBreakBetweenLabelAndField(final boolean breakBetweenLabelAndProperty)
  {
    this.breakBetweenLabelAndField = breakBetweenLabelAndProperty;
    return this;
  }

  public String getCssStyle()
  {
    return cssStyle;
  }

  /**
   * @param cssStyle
   * @return this for chaining.
   */
  public PanelContext setCssStyle(final String cssStyle)
  {
    this.cssStyle = cssStyle;
    return this;
  }

  public LayoutAlignment getAligment()
  {
    return aligment;
  }

  /**
   * @param aligment
   * @return this for chaining.
   */
  public PanelContext setAligment(LayoutAlignment aligment)
  {
    this.aligment = aligment;
    return this;
  }

  public boolean isRequired()
  {
    return required;
  }

  /**
   * @return this for chaining.
   */
  public PanelContext setRequired()
  {
    this.required = true;
    return this;
  }

  public boolean isFocus()
  {
    return focus;
  }

  /**
   * @return this for chaining.
   */
  public PanelContext setFocus()
  {
    this.focus = true;
    return this;
  }

  public boolean isStrong()
  {
    return strong;
  }

  /**
   * @return this for chaining.
   */
  public PanelContext setStrong()
  {
    this.strong = true;
    return this;
  }

  public boolean isReadonly()
  {
    return readonly;
  }

  /**
   * @return this for chaining.
   */
  public PanelContext setReadonly()
  {
    this.readonly = true;
    return this;
  }

  public IField getValueField()
  {
    return valueField;
  }

  public LabelLPanel getLabelPanel()
  {
    return labelPanel;
  }

  /**
   * Don't use this method. This method doesn't set a new value field! This method does only register a previous added value field.
   * @param valueField
   */
  public void internalSetValueField(final IField valueField)
  {
    this.valueField = valueField;
  }

  /**
   * Don't use this method. This method doesn't set a new label panel! This method does only register a previous added label panel.
   * @param valueField
   */
  public void internalSetLabelPanel(final LabelLPanel labelPanel)
  {
    this.labelPanel = labelPanel;
  }
}
