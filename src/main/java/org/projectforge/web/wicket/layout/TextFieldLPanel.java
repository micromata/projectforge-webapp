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

import org.apache.wicket.Component;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.PropertyModel;
import org.projectforge.web.wicket.FocusOnLoadBehavior;
import org.projectforge.web.wicket.components.MaxLengthTextField;

/**
 * Represents a field set panel. A form or page can contain multiple field sets.
 * @author Kai Reinhard (k.reinhard@micromata.de)
 * 
 */
public class TextFieldLPanel extends AbstractLPanel implements IField
{
  private static final long serialVersionUID = 5771712946605166500L;

  /**
   * Wicket id.
   */
  public static final String INPUT_ID = "input";

  protected TextField< ? > textField;

  /**
   * @see AbstractRenderer#createTextFieldPanel(String, LayoutLength, Object, String)
   */
  TextFieldLPanel(final String id, final LayoutLength length, final Object dataObject, final String property)
  {
    this(id, length, new MaxLengthTextField(INPUT_ID, new PropertyModel<String>(dataObject, property)));
  }

  /**
   * @see AbstractRenderer#createTextFieldPanel(String, LayoutLength, TextField)
   */
  TextFieldLPanel(final String id, final LayoutLength length, final TextField< ? > textField)
  {
    super(id, length);
    this.textField = textField;
    this.classAttributeAppender = "text";
    add(textField);
  }

  /**
   * Only used by TextFieldMobileLPanel.
   * @param id
   * @param length
   */
  protected TextFieldLPanel(final String id, final LayoutLength length)
  {
    super(id, length);
  }

  public TextFieldLPanel setStrong()
  {
    this.classAttributeAppender = "text strong";
    return this;
  }

  public TextFieldLPanel setRequired()
  {
    textField.setRequired(true);
    return this;
  }

  public TextFieldLPanel setFocus()
  {
    textField.add(new FocusOnLoadBehavior());
    return this;
  }

  public TextField< ? > getTextField()
  {
    return textField;
  }

  @Override
  protected Component getClassModifierComponent()
  {
    return textField;
  }
}
