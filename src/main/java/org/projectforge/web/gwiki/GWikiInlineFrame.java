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

package org.projectforge.web.gwiki;

import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.protocol.http.WebApplication;

/**
 * Component to render an iframe with a specific target.
 * 
 * @author Kai Reinhard (k.reinhard@micromata.de)
 * 
 */
public class GWikiInlineFrame extends WebMarkupContainer
{
  private static final long serialVersionUID = 3903792810732132670L;

  private String target = null;

  public GWikiInlineFrame(final String id, final String target)
  {
    super(id);

    this.target = target;
  }

  private String getTargetURL()
  {
    final String url = WebApplication.get().getServletContext().getContextPath() + "/gwiki/" + target;
    return url;
  }

  @Override
  protected final void onComponentTag(final ComponentTag tag)
  {
    checkComponentTag(tag, "iframe");

    tag.put("src", getTargetURL());

    super.onComponentTag(tag);
  }

}
