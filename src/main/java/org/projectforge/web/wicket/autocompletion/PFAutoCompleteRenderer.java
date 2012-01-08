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

import org.apache.wicket.Response;
import org.apache.wicket.extensions.ajax.markup.html.autocomplete.IAutoCompleteRenderer;

public class PFAutoCompleteRenderer implements IAutoCompleteRenderer<String>
{
  private static final long serialVersionUID = -6217378174956288038L;

  /**
   * A singleton instance
   */
  public static final PFAutoCompleteRenderer INSTANCE = new PFAutoCompleteRenderer();

  /**
   * @param <T>
   * @return
   */
  public static final PFAutoCompleteRenderer instance()
  {
    return INSTANCE;
  }

  public void render(final String value, final Response response, final String criteria)
  {
    response.write(value);
  }

  public void renderFooter(final Response response)
  {
  }

  public void renderHeader(final Response response)
  {
  }
}
