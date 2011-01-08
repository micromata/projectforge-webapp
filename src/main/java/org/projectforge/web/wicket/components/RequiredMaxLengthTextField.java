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

import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;

/**
 * MaxLengthTextField with required-validation.
 * @author Kai Reinhard (k.reinhard@micromata.de)
 * 
 */
public class RequiredMaxLengthTextField extends MaxLengthTextField
{
  private static final long serialVersionUID = 7610655476354134456L;

  /**
   * Use constructor with parent and/or label params instead.
   */
  @Deprecated
  public RequiredMaxLengthTextField(final String id, final PropertyModel<String> model)
  {
    this(null, id, null, model);
  }

  /**
   * @see MaxLengthTextField#MaxLengthTextField(WebMarkupContainer, String, String, PropertyModel)
   */
  public RequiredMaxLengthTextField(final String id, final String label, final IModel<String> model)
  {
    this(null, id, label, model);
  }

  /**
   * @see MaxLengthTextField#MaxLengthTextField(WebMarkupContainer, String, String, PropertyModel)
   */
  public RequiredMaxLengthTextField(final WebMarkupContainer parent, final String id, final String label, final IModel<String> model)
  {
    super(parent, id, label, model);
    setRequired(true);
  }

  /**
   * @see MaxLengthTextField#MaxLengthTextField(WebMarkupContainer, String, String, PropertyModel)
   */
  public RequiredMaxLengthTextField(final String id, final String label, final IModel<String> model, final int maxLength)
  {
    this(null, id, label, model, maxLength);
  }

  /**
   * @see MaxLengthTextField#MaxLengthTextField(WebMarkupContainer, String, String, PropertyModel)
   */
  public RequiredMaxLengthTextField(final WebMarkupContainer parent, final String id, final String label, final IModel<String> model,
      final int maxLength)
  {
    super(parent, id, label, model, maxLength);
    setRequired(true);
  }

  /**
   * Use constructor with parent and/or label params instead.
   */
  @Deprecated
  public RequiredMaxLengthTextField(final String id, final IModel<String> model, final int maxLength)
  {
    this(null, id, null, model, maxLength);
  }
}
