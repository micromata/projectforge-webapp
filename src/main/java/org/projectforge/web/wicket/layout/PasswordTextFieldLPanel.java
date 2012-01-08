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

package org.projectforge.web.wicket.layout;

import org.apache.wicket.markup.html.form.TextField;

/**
 * Represents a field set panel. A form or page can contain multiple field sets.
 * @author Kai Reinhard (k.reinhard@micromata.de)
 * 
 */
public class PasswordTextFieldLPanel extends TextFieldLPanel
{
  private static final long serialVersionUID = -8656967306681480177L;

  /**
   * @see AbstractFormRenderer#createTextFieldPanel(String, LayoutLength, TextField)
   */
  PasswordTextFieldLPanel(final String id, final TextField< ? > textField, final PanelContext ctx)
  {
    super(id, textField, ctx);
  }

  /**
   * Only used by TextFieldMobileLPanel.
   * @param id
   * @param length
   */
  protected PasswordTextFieldLPanel(final String id, final PanelContext ctx)
  {
    super(id, ctx);
  }
}
