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
import org.projectforge.web.address.AddressListPage;
import org.projectforge.web.wicket.PresizedImage;

public class MenuMobilePage extends AbstractSecuredMobilePage
{
  public MenuMobilePage(final PageParameters parameters)
  {
    super(parameters);
    final PageItemPanel pageItemPanel = new PageItemPanel("menu");
    add(pageItemPanel);
    final String optimized = getString("mobile.optimized");
    PresizedImage image = new PresizedImage("image", getResponse(), MobileWebConstants.THUMB_IMAGE_ADDRESS_BOOK);
    pageItemPanel.add(new PageItemEntryMenuPanel(pageItemPanel.newChildId(), AddressListPage.class, image,
        getString("address.title.heading"), null));
    image = new PresizedImage("image", getResponse(), MobileWebConstants.THUMB_IMAGE_ADDRESS_BOOK);
    pageItemPanel.add(new PageItemEntryMenuPanel(pageItemPanel.newChildId(), AddressListPage.class, image,
        getString("address.title.heading"), optimized));
  }

  @Override
  protected String getTitle()
  {
    return getString("menu.main.title");
  }
}
