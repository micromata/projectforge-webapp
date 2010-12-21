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

package org.projectforge.web.mobile;

import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.image.ContextImage;
import org.apache.wicket.markup.html.link.AbstractLink;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.Link;
import org.projectforge.web.LoginPage;

/**
 * Menu entry of a pageItem area.
 * @author Kai Reinhard (k.reinhard@micromata.de)
 * 
 */
public class PageItemEntryMenuPanel extends PageItemEntryPanel
{
  private static final long serialVersionUID = -7707209103924086843L;

  @SuppressWarnings("serial")
  public PageItemEntryMenuPanel(final String id, final ContextImage image, final String name, final String comment)
  {
    super(id);
    final PageParameters params = new PageParameters();
    params.add(LoginPage.REQUEST_PARAM_LOGOUT, "true");
    final Link<String> link = new Link<String>("link") {
      public void onClick()
      {
        PageItemEntryMenuPanel.this.onClick();
      };
    };
    add(link);
    init(link, image, name, comment);
  }

  public PageItemEntryMenuPanel(final String id, final Class< ? extends WebPage> pageClass, final ContextImage image, final String name,
      final String comment)
  {
    super(id);
    final BookmarkablePageLink<String> link = new BookmarkablePageLink<String>("link", pageClass);
    add(link);
    init(link, image, name, comment);
  }

  public PageItemEntryMenuPanel(final String id, final Class< ? extends WebPage> pageClass, final PageParameters params, final ContextImage image, final String name,
      final String comment)
  {
    super(id);
    final BookmarkablePageLink<String> link = new BookmarkablePageLink<String>("link", pageClass, params);
    add(link);
    init(link, image, name, comment);
  }

  private void init(final AbstractLink link, final ContextImage image, final String name, final String comment)
  {
    if (image == null) {
      link.add(new Label("image", "[invisible]").setVisible(false));
    } else {
      link.add(image);
    }
    link.add(new Label("name", name));
    if (comment == null) {
      link.add(new Label("comment", "[invisible]").setVisible(false));
    } else {
      link.add(new Label("comment", comment));
    }
  }

  protected void onClick()
  {
  }
}
