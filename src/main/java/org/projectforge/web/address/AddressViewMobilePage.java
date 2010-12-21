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

package org.projectforge.web.address;

import org.apache.wicket.PageParameters;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.projectforge.address.AddressDao;
import org.projectforge.web.mobile.AbstractSecuredMobilePage;
import org.projectforge.web.wicket.components.LabelBookmarkablePageLinkPanel;

public class AddressViewMobilePage extends AbstractSecuredMobilePage
{
  @SpringBean(name = "addressDao")
  private AddressDao addressDao;

  public AddressViewMobilePage(final PageParameters parameters)
  {
    super(parameters);
    leftnavRepeater.add(new LabelBookmarkablePageLinkPanel(leftnavRepeater.newChildId(), AddressListMobilePage.class, getString("list")));
  }

  @Override
  protected String getTitle()
  {
    return getString("address.title.heading");
  }
}
