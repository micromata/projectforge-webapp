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

package org.projectforge.web.wicket.components;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.wicket.Component;
import org.apache.wicket.markup.html.form.IChoiceRenderer;
import org.projectforge.common.ILabelValueBean;
import org.projectforge.core.I18nEnum;

/**
 * @author Kai Reinhard (k.reinhard@micromata.de)
 */
public class LabelValueChoiceRenderer<T> implements IChoiceRenderer<T>
{
  private static final long serialVersionUID = -5832080496659840226L;

  private List<T> values;

  private Map<T, String> displayValues;

  /**
   */
  public LabelValueChoiceRenderer()
  {
    this.values = new ArrayList<T>();
    this.displayValues = new HashMap<T, String>();
  }

  /**
   * Creates already entries from the given enum.
   * @param parent Only needed for internationalization.
   * @param i18nEnum if not enum and not from type T a class cast exception will be thrown.
   * @see Component#getString(String)
   */
  public LabelValueChoiceRenderer(final Component parent, final I18nEnum[] values)
  {
    this(parent, values, 0);
  }

  /**
   * Creates already entries from the given enum.
   * @param parent Only needed for internationalization.
   * @param i18nEnum if not enum and not from type T a class cast exception will be thrown.
   * @param dummy Only for avoiding ambiguous constructors.
   * @see Component#getString(String)
   */
  @SuppressWarnings("unchecked")
  public LabelValueChoiceRenderer(final Component parent, final I18nEnum[] values, final int dummy)
  {
    this();
    for (final I18nEnum value : values) {
      addValue((T) value, parent.getString(value.getI18nKey()));
    }
  }

  /**
   * Creates already entries from the given enum.
   * @param parent Only needed for internationalization.
   * @param values if the elements are not from type ILabelValueBean<String, T> a class cast exception will be thrown.
   * @see Component#getString(String)
   */
  public LabelValueChoiceRenderer(final Component parent, final List<T> values)
  {
    this();
    for (final Object value : values) {
      @SuppressWarnings("unchecked")
      final ILabelValueBean<String, T> labelValue = (ILabelValueBean<String, T>)value;
      addValue(labelValue.getValue(), labelValue.getLabel());
    }
  }

  /**
   * Creates already entries from the given enum.
   * @param parent Only needed for internationalization.
   * @param values if the elements are not from type ILabelValueBean<String, T> a class cast exception will be thrown.
   * @see Component#getString(String)
   */
  public LabelValueChoiceRenderer(final Component parent, final T... values)
  {
    this();
    for (final Object value : values) {
      @SuppressWarnings("unchecked")
      final ILabelValueBean<String, T> labelValue = (ILabelValueBean<String, T>)value;
      addValue(labelValue.getValue(), labelValue.getLabel());
    }
  }

  public LabelValueChoiceRenderer<T> addValue(final T value, final String displayValue)
  {
    this.values.add(value);
    this.displayValues.put(value, displayValue);
    return this;
  }

  public List<T> getValues()
  {
    return values;
  }

  /**
   * Please note: This method does not check wether the given object is an entry of the year list or not.
   * @return given integer as String or "[minYear]-[maxYear]" if value is -1.
   * @see org.apache.wicket.markup.html.form.IChoiceRenderer#getDisplayValue(java.lang.Object)
   */
  public Object getDisplayValue(final T object)
  {
    return this.displayValues.get(object);
  }

  public String getIdValue(final T object, final int index)
  {
    return object.toString();
  }

}
