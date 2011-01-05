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

import org.apache.wicket.behavior.SimpleAttributeModifier;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.PropertyModel;
import org.projectforge.web.wicket.components.MaxLengthTextField;

/**
 * Represents a text field panel for a mobile device.
 * @author Kai Reinhard (k.reinhard@micromata.de)
 * 
 */
public class TextFieldMobileLPanel extends TextFieldLPanel
{
  private static final long serialVersionUID = 3591742530399992033L;

  /**
   * Wicket id.
   */
  public static final String INPUT_ID = "input";

  private String label;

  private String size;

  private WebMarkupContainer item = new WebMarkupContainer("item");

  /**
   * @see AbstractDOFormRenderer#createTextFieldPanel(String, LayoutLength, Object, String)
   */
  TextFieldMobileLPanel(final String id, final LayoutLength length, final Object dataObject, final String property)
  {
    this(id, length, new MaxLengthTextField(INPUT_ID, new PropertyModel<String>(dataObject, property)));
  }

  /**
   * @see AbstractDOFormRenderer#createTextFieldPanel(String, LayoutLength, TextField)
   */
  TextFieldMobileLPanel(final String id, final LayoutLength length, final TextField< ? > textField)
  {
    super(id, length);
    this.textField = textField;
    this.classAttributeAppender = "text";
    add(item);
    item.add(textField);
  }

  public TextFieldMobileLPanel setLabel(final String label)
  {
    this.label = label;
    item.add(new Label("label", label));
    return this;
  }

  public TextFieldMobileLPanel setStrong()
  {
    this.classAttributeAppender = "text strong";
    return this;
  }

  /**
   * Does nothing.
   * @see org.projectforge.web.wicket.layout.TextFieldLPanel#setFocus()
   */
  public TextFieldMobileLPanel setFocus()
  {
    return this;
  }

  @Override
  protected void onBeforeRender()
  {
    super.onBeforeRender();
    if (label != null) {
      textField.add(new SimpleAttributeModifier("placeholder", label));
    }
    if (size == null) {
      item.add(new SimpleAttributeModifier("class", "smallfield"));
    }
  }
}
