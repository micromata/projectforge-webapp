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
import org.apache.wicket.behavior.SimpleAttributeModifier;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.util.convert.IConverter;
import org.apache.wicket.validation.validator.StringValidator.MaximumLengthValidator;
import org.projectforge.database.HibernateUtils;

public class MaxLengthTextField extends TextField<String>
{
  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(MaxLengthTextField.class);

  private static final long serialVersionUID = -6577192527741433068L;

  private IConverter converter;

  /**
   * Use constructor with parent and/or label params instead.
   */
  @Deprecated
  public MaxLengthTextField(final String id, final IModel<String> model)
  {
    this(null, id, null, model);
  }

  public MaxLengthTextField(final String id, final String label, final IModel<String> model)
  {
    this(null, id, label, model);
  }

  /**
   * Tries to get the length definition of the Hibernate configuration. If not available then a warning will be logged. Example:
   * 
   * <pre>
   * &lt;label wicket:id="streetLabel"&gt;[street]&lt;/&gt;&lt;input type="text" wicket:id="street" /&gt;<br/>
   * add(new MaxLengthTextField(this, "street", "address.street", model);
   * </pre>
   * @param parent if not null and label is not null than a label with wicket id [id]Label is added.
   * @param id
   * @param label needed for validation error messages. Is also used for setting label via wicket id [label]Label.
   * @param model
   * @see org.apache.wicket.Component#Component(String, IModel)
   * @see FormComponent#setLabel(IModel)
   */
  public MaxLengthTextField(final WebMarkupContainer parent, final String id, final String label, final IModel<String> model)
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
    init(parent, id, label, length);
  }

  /**
   * Use constructor with parent and/or label params instead.
   */
  @Deprecated
  public MaxLengthTextField(final String id, final IModel<String> model, final int maxLength)
  {
    this(null, id, null, model, maxLength);
  }

  public MaxLengthTextField(final String id, final String label, final IModel<String> model, final int maxLength)
  {
    this(null, id, label, model, maxLength);
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
   * @param label needed for validation error messages. Is also used for setting label via wicket id [label]Label.
   * @param model
   * @param maxLength
   * @see org.apache.wicket.Component#Component(String, IModel)
   * @see FormComponent#setLabel(IModel)
   */
  public MaxLengthTextField(final WebMarkupContainer parent, final String id, final String label, final IModel<String> model,
      final int maxLength)
  {
    super(id, model);
    if (ClassUtils.isAssignable(model.getClass(), PropertyModel.class)) {
      PropertyModel< ? > propertyModel = (PropertyModel< ? >) model;
      final Integer dbMaxLength = HibernateUtils.getPropertyLength(propertyModel.getTarget().getClass().getName(), propertyModel
          .getPropertyField().getName());
      if (dbMaxLength != null && dbMaxLength < maxLength) {
        log.warn("Data base length of given property is less than given maxLength: " + model);
      }
    }
    init(parent, id, label, maxLength);
  }

  private void init(final WebMarkupContainer parent, final String id, final String label, final Integer maxLength)
  {
    if (label != null) {
      setLabel(new Model<String>(label));
      if (parent != null) {
        parent.add(new LabelForPanel(id + "Label", this, label));
      }
    }
    if (maxLength != null) {
      add(new MaximumLengthValidator(maxLength));
      add(new SimpleAttributeModifier("maxlength", String.valueOf(maxLength)));
    }
  }

  @Override
  public IConverter getConverter(Class< ? > type)
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
  public MaxLengthTextField setConverter(final IConverter converter)
  {
    this.converter = converter;
    return this;
  }
}
