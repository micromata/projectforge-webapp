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

package org.projectforge.web;

import org.apache.wicket.markup.html.IHeaderResponse;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.projectforge.web.wicket.AbstractSecuredPage;

public class CustomizeMenuPage extends AbstractSecuredPage
{
  private static final long serialVersionUID = 8587252641914110851L;

  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(CustomizeMenuPage.class);

  @SpringBean(name = "menuBuilder")
  private MenuBuilder menuBuilder;

  public CustomizeMenuPage(final PageParameters parameters)
  {
    super(parameters);
    // body.add(new Label("totalNumberOfHistoryEntries", NumberFormatter.format(totalNumberOfHistoryEntries)));
    final RepeatingView origMenuRepeater = new RepeatingView("parentMenu");
    body.add(origMenuRepeater);
    final Menu menu = menuBuilder.getMenu(getUser());
    for (final MenuEntry parentMenuEntry : menu.getMenuEntries()) {
      if (parentMenuEntry.hasSubMenuEntries() == false) {
        continue;
      }
      final WebMarkupContainer parentItem = new WebMarkupContainer(origMenuRepeater.newChildId());
      origMenuRepeater.add(parentItem);
      parentItem.add(new Label("label", getString(parentMenuEntry.getI18nKey())));
      final RepeatingView subMenuRepeater = new RepeatingView("menu");
      parentItem.add(subMenuRepeater);
      for (final MenuEntry menuEntry : parentMenuEntry.getSubMenuEntries()) {
        final WebMarkupContainer item = new WebMarkupContainer(subMenuRepeater.newChildId());
        subMenuRepeater.add(item);
        item.add(new Label("label", getString(menuEntry.getI18nKey())));
      }
    }
  }

  @Override
  public void renderHead(final IHeaderResponse response)
  {
    super.renderHead(response);
    response.renderJavaScriptReference("scripts/jquery.jstree/jquery.jstree.js");
    response.renderJavaScriptReference("scripts/jquery.jstree/jquery.hotkeys.js");
  }

  @Override
  protected String getTitle()
  {
    return getString("menu.customize.title");
  }

}
