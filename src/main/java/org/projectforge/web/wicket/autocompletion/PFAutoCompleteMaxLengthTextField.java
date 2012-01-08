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

package org.projectforge.web.wicket.autocompletion;

import org.apache.wicket.behavior.SimpleAttributeModifier;
import org.apache.wicket.extensions.ajax.markup.html.autocomplete.IAutoCompleteRenderer;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.validation.validator.StringValidator.MaximumLengthValidator;
import org.projectforge.database.HibernateUtils;


public abstract class PFAutoCompleteMaxLengthTextField extends PFAutoCompleteTextField<String>
{
  private static final long serialVersionUID = -1269934405480896598L;

  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(PFAutoCompleteMaxLengthTextField.class);

  public PFAutoCompleteMaxLengthTextField(final String id, final PropertyModel<String> model)
  {
    this(id, model, PFAutoCompleteRenderer.INSTANCE, new PFAutoCompleteSettings());// , type, StringAutoCompleteRenderer.INSTANCE,
    // settings);
  }

  public PFAutoCompleteMaxLengthTextField(final String id, final PropertyModel<String> model, final int maxLength)
  {
    this(id, model, PFAutoCompleteRenderer.INSTANCE, new PFAutoCompleteSettings(), maxLength);// , type,
    // StringAutoCompleteRenderer.INSTANCE,
    // settings);
  }

  public PFAutoCompleteMaxLengthTextField(final String id, final PropertyModel<String> model, final IAutoCompleteRenderer<String> renderer,
      final PFAutoCompleteSettings settings)
  {
    this(id, model, renderer, settings, null);
  }

  public PFAutoCompleteMaxLengthTextField(final String id, final PropertyModel<String> model, final IAutoCompleteRenderer<String> renderer,
      final PFAutoCompleteSettings settings, final Integer maxLength)
  {
    super(id, model, renderer, settings);
    final Integer length = maxLength != null ? maxLength : HibernateUtils.getPropertyLength(model.getTarget().getClass().getName(), model
        .getPropertyField().getName());
    if (length == null) {
      log.warn("No length validation for: " + model);
    } else {
      init(length);
    }
  }

  private void init(final Integer maxLength)
  {
    if (maxLength != null) {
      add(new MaximumLengthValidator(maxLength));
      add(new SimpleAttributeModifier("maxlength", String.valueOf(maxLength)));
    }
  }
}
