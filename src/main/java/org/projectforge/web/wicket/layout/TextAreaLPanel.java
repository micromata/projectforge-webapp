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

package org.projectforge.web.wicket.layout;

import org.apache.wicket.Component;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.model.PropertyModel;
import org.projectforge.web.wicket.FocusOnLoadBehavior;
import org.projectforge.web.wicket.components.MaxLengthTextArea;

/**
 * Represents a field set panel. A form or page can contain multiple field sets.
 * @author Kai Reinhard (k.reinhard@micromata.de)
 * 
 */
public class TextAreaLPanel extends AbstractLPanel
{
  private static final long serialVersionUID = 4625762736195250291L;

  /**
   * Wicket id.
   */
  public static final String TEXT_AREA_ID = "textarea";

  private TextArea< ? > textArea;

  public TextAreaLPanel(final String id, final LayoutLength length, final Object dataObject, final String property)
  {
    this(id, length, new MaxLengthTextArea(TEXT_AREA_ID, new PropertyModel<String>(dataObject, property)));
  }

  public TextAreaLPanel(final String id, final LayoutLength length, final TextArea< ? > textArea)
  {
    super(id, length);
    this.textArea = textArea;
    this.classAttributeAppender = "textarea";
    add(textArea);
  }

  public TextAreaLPanel setFocus()
  {
    textArea.add(new FocusOnLoadBehavior());
    return this;
  }

  public TextArea< ? > getTextArea()
  {
    return textArea;
  }

  @Override
  protected Component getClassModifierComponent()
  {
    return textArea;
  }
}
