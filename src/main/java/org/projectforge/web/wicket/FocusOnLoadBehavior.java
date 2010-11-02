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

package org.projectforge.web.wicket;

import org.apache.wicket.Component;
import org.apache.wicket.behavior.AbstractBehavior;
import org.apache.wicket.markup.html.IHeaderResponse;

public class FocusOnLoadBehavior extends AbstractBehavior
{
  private static final long serialVersionUID = 2698344455870507074L;

  private Component component;

  private boolean isTemporary;

  public FocusOnLoadBehavior()
  {
    this(false);
  }

  /**
   * @param isTemporary If true, remove the behavior after component has been rendered
   */
  public FocusOnLoadBehavior(boolean isTemporary)
  {
    super();
    this.isTemporary = isTemporary;
  }

  public void bind(Component component)
  {
    this.component = component;
    component.setOutputMarkupId(true);
  }

  public void renderHead(IHeaderResponse iHeaderResponse)
  {
    super.renderHead(iHeaderResponse);
    iHeaderResponse.renderOnLoadJavascript("document.getElementById('" + component.getMarkupId() + "').focus()");
  }

  @Override
  public boolean isTemporary()
  {
    return isTemporary;
  }
}
