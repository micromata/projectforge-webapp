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
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.projectforge.address.AddressDO;
import org.projectforge.address.AddressDao;
import org.projectforge.common.NumberHelper;
import org.projectforge.web.mobile.AbstractSecuredMobilePage;
import org.projectforge.web.wicket.AbstractEditPage;
import org.projectforge.web.wicket.components.LabelBookmarkablePageLinkPanel;

public class AddressViewMobilePage extends AbstractSecuredMobilePage
{
  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(AddressViewMobilePage.class);

  @SpringBean(name = "addressDao")
  private AddressDao addressDao;

  private AddressDO address;

  public AddressViewMobilePage(final PageParameters parameters)
  {
    super(parameters);
    final Integer id = getPageParameters().getAsInteger(AbstractEditPage.PARAMETER_KEY_ID);
    if (NumberHelper.greaterZero(id) == true) {
      address = addressDao.getById(id);
    }
    if (address == null) {
      log.error("Oups, address with id " + id + " not found.");
      setResponsePage(AddressListMobilePage.class);
      return;
    }
    leftnavRepeater.add(new LabelBookmarkablePageLinkPanel(leftnavRepeater.newChildId(), AddressListMobilePage.class, getString("list")));
    add(new Label("name", address.getTitle() + " " + address.getFirstName() + " " + address.getName()));
  }

  @Override
  protected String getTitle()
  {
    return getString("address.title.heading");
  }
}
