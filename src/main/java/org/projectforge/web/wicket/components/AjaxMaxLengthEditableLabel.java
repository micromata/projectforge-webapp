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

package org.projectforge.web.wicket.components;

import org.apache.commons.lang.ClassUtils;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.extensions.ajax.markup.html.AjaxEditableLabel;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.util.convert.IConverter;
import org.apache.wicket.validation.validator.StringValidator.MaximumLengthValidator;
import org.projectforge.database.HibernateUtils;

@SuppressWarnings("serial")
public class AjaxMaxLengthEditableLabel extends AjaxEditableLabel<String>
{
  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(AjaxMaxLengthEditableLabel.class);

  @SuppressWarnings("rawtypes")
  private IConverter converter;

  /**
   * Tries to get the length definition of the Hibernate configuration. If not available then a warning will be logged. Example:
   * 
   * <pre>
   * &lt;label wicket:id="streetLabel"&gt;[street]&lt;/&gt;&lt;input type="text" wicket:id="street" /&gt;<br/>
   * add(new MaxLengthTextField(this, "street", "address.street", model);
   * </pre>
   * @param id
   * @param model
   * @see org.apache.wicket.Component#Component(String, IModel)
   * @see FormComponent#setLabel(IModel)
   */
  public AjaxMaxLengthEditableLabel(final String id, final IModel<String> model)
  {
    super(id, model);
    Integer length = null;
    if (ClassUtils.isAssignable(model.getClass(), PropertyModel.class)) {
      final PropertyModel< ? > propertyModel = (PropertyModel< ? >) model;
      length = HibernateUtils.getPropertyLength(propertyModel.getTarget().getClass().getName(), propertyModel.getPropertyField().getName());
      if (length == null) {
        log.warn("No length validation for: " + model);
      }
    }
    init(id, length);
  }

  /**
   * Example:
   * 
   * <pre>
   * &lt;label wicket:id="streetLabel"&gt;[street]&lt;/&gt;&lt;input type="text" wicket:id="street" /&gt;<br/>
   * add(new MaxLengthTextField(this, "street", "address.street", model);
   * </pre>
   * @param parent if not null and label is not null than a label with wicket id [id]Label is added.
   * @param id
   * @param model
   * @param maxLength
   * @see org.apache.wicket.Component#Component(String, IModel)
   * @see FormComponent#setLabel(IModel)
   */
  public AjaxMaxLengthEditableLabel(final String id, final IModel<String> model, final int maxLength)
  {
    super(id, model);
    if (ClassUtils.isAssignable(model.getClass(), PropertyModel.class)) {
      final PropertyModel< ? > propertyModel = (PropertyModel< ? >) model;
      final Integer dbMaxLength = HibernateUtils.getPropertyLength(propertyModel.getTarget().getClass().getName(), propertyModel
          .getPropertyField().getName());
      if (dbMaxLength != null && dbMaxLength < maxLength) {
        log.warn("Data base length of given property is less than given maxLength: " + model);
      }
    }
    init(id, maxLength);
  }

  private void init(final String id, final Integer maxLength)
  {
    if (maxLength != null) {
      add(new MaximumLengthValidator(maxLength));
      add(AttributeModifier.replace("maxlength", String.valueOf(maxLength)));
    }
  }

  /**
   * @see org.apache.wicket.Component#getConverter(java.lang.Class)
   */
  @SuppressWarnings("unchecked")
  @Override
  public <C> IConverter<C> getConverter(final Class<C> type)
  {
    if (converter != null) {
      return converter;
    } else {
      return super.getConverter(type);
    }
  }

  /**
   * Setting a converter is more convenient instead of overriding method getConverter(Class).
   * @param converter
   * @return This for chaining.
   */
  public <C> AjaxMaxLengthEditableLabel setConverter(final IConverter<C> converter)
  {
    this.converter = converter;
    return this;
  }
}
