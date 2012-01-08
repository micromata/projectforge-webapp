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

package org.projectforge.web.address;

import org.apache.wicket.PageParameters;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.projectforge.address.AddressDO;
import org.projectforge.address.AddressDao;
import org.projectforge.address.PersonalAddressDO;
import org.projectforge.address.PersonalAddressDao;
import org.projectforge.web.mobile.AbstractMobileViewPage;
import org.projectforge.web.wicket.layout.AbstractFormRenderer;
import org.projectforge.web.wicket.layout.LayoutContext;

public class AddressMobileViewPage extends AbstractMobileViewPage
{
  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(AddressMobileViewPage.class);

  @SpringBean(name = "addressDao")
  private AddressDao addressDao;

  @SpringBean(name = "personalAddressDao")
  private PersonalAddressDao personalAddressDao;

  public AddressMobileViewPage(final PageParameters parameters)
  {
    super(parameters, AddressMobileEditPage.class, "address");
  }

  @Override
  protected AbstractFormRenderer createRenderer(final LayoutContext layoutContext, final Integer objectId)
  {
    final AddressDO address = addressDao.getById(objectId);
    if (address == null) {
      log.error("Oups, address with id " + objectId + " not found.");
      setResponsePage(AddressMobileListPage.class);
      return null;
    }
    PersonalAddressDO personalAddress = personalAddressDao.getByAddressId(address.getId());
    if (personalAddress == null) {
      personalAddress = new PersonalAddressDO();
    }
    return new AddressFormRenderer(this, layoutContext, addressDao, address, personalAddress);
  }
}
