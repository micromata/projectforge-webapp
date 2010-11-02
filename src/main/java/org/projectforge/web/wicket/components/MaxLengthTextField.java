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

package org.projectforge.web.wicket.components;

import org.apache.commons.lang.ClassUtils;
import org.apache.wicket.behavior.SimpleAttributeModifier;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.validation.validator.StringValidator.MaximumLengthValidator;
import org.projectforge.database.HibernateUtils;


public class MaxLengthTextField extends TextField<String>
{
  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(MaxLengthTextField.class);

  private static final long serialVersionUID = -6577192527741433068L;

  /**
   * Tries to get the length definition of the Hibernate configuration. If not available then a warning will be logged.
   * @param id
   * @param model
   * @see org.apache.wicket.Component#Component(String, IModel)
   */
  public MaxLengthTextField(final String id, final PropertyModel<String> model)
  {
    super(id, model);
    final Integer length = HibernateUtils.getPropertyLength(model.getTarget().getClass().getName(), model.getPropertyField().getName());
    if (length == null) {
      log.warn("No length validation for: " + model);
    }
    init(length);
  }

  /**
   * @param id
   * @param model
   * @param maxLength
   * @see org.apache.wicket.Component#Component(String, IModel)
   */
  public MaxLengthTextField(final String id, final IModel<String> model, int maxLength)
  {
    super(id, model);
    if (ClassUtils.isAssignable(model.getClass(), PropertyModel.class)) {
      PropertyModel<?> propertyModel = (PropertyModel<?>)model;
      Integer dbMaxLength = HibernateUtils.getPropertyLength(propertyModel.getTarget().getClass().getName(), propertyModel.getPropertyField().getName());
      if (dbMaxLength != null && dbMaxLength < maxLength) {
        log.warn("Data base length of given property is less than given maxLength: " + model);
      }
    }
    init(maxLength);
  }

  private void init(final Integer maxLength)
  {
    if (maxLength != null) {
      add(new MaximumLengthValidator(maxLength));
      add(new SimpleAttributeModifier("maxlength", String.valueOf(maxLength)));
    }
  }
}
