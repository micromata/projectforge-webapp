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

import java.lang.reflect.Field;

import org.apache.commons.lang.ClassUtils;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.validation.validator.StringValidator.MaximumLengthValidator;
import org.projectforge.database.HibernateUtils;

public class MaxLengthTextArea extends TextArea<String>
{
  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(MaxLengthTextArea.class);

  private static final long serialVersionUID = 1507157818607697767L;

  /**
   * Tries to get the length definition of the Hibernate configuration. If not available then a warning will be logged. <br/>
   * Example:
   * 
   * @param id
   * @param label needed for validation error messages. Is also used for setting label via wicket id [label]Label.
   * @param model
   * @see org.apache.wicket.Component#Component(String, IModel)
   * @see FormComponent#setLabel(IModel)
   */
  public MaxLengthTextArea(final String id, final IModel<String> model)
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
   * @param id
   * @param label needed for validation error messages.
   * @param model
   * @param maxLength
   * @see org.apache.wicket.Component#Component(String, IModel)
   * @see FormComponent#setLabel(IModel)
   */
  public MaxLengthTextArea(final String id, final IModel<String> model, final int maxLength)
  {
    super(id, model);
    if (ClassUtils.isAssignable(model.getClass(), PropertyModel.class)) {
      final PropertyModel< ? > propertyModel = (PropertyModel< ? >) model;
      final Field propertyField = propertyModel.getPropertyField();
      if (propertyField != null) {
        final Integer dbMaxLength = HibernateUtils.getPropertyLength(propertyModel.getTarget().getClass().getName(),
            propertyField.getName());
        if (dbMaxLength != null && dbMaxLength < maxLength) {
          log.warn("Data base length of given property is less than given maxLength: " + model);
        }
      }
    }
    init(id, maxLength);
  }

  private void init(final String id, final Integer maxLength)
  {
    if (maxLength != null) {
      add(new MaximumLengthValidator(maxLength));
      // add(AttributeModifier.replace("maxlength", String.valueOf(maxLength))); // Not supported by html textarea!
    }
  }
}
