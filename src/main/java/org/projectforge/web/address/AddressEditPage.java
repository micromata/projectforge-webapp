/////////////////////////////////////////////////////////////////////////////
//
// Project ProjectForge Community Edition
//         www.projectforge.org
//
// Copyright (C) 2001-2011 Kai Reinhard (k.reinhard@me.com)
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

import org.apache.log4j.Logger;
import org.apache.wicket.PageParameters;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.projectforge.address.AddressDO;
import org.projectforge.address.AddressDao;
import org.projectforge.address.PersonalAddressDO;
import org.projectforge.address.PersonalAddressDao;
import org.projectforge.web.wicket.AbstractBasePage;
import org.projectforge.web.wicket.AbstractEditPage;
import org.projectforge.web.wicket.EditPage;

@EditPage(defaultReturnPage = AddressListPage.class)
public class AddressEditPage extends AbstractEditPage<AddressDO, AddressEditForm, AddressDao>
{
  private static final long serialVersionUID = 7091721062661400435L;

  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(AddressEditPage.class);

  @SpringBean(name = "addressDao")
  private AddressDao addressDao;

  @SpringBean(name = "personalAddressDao")
  private PersonalAddressDao personalAddressDao;

  public AddressEditPage(PageParameters parameters)
  {
    super(parameters, "address");
    init();
  }

  @Override
  protected AbstractBasePage afterSaveOrUpdate()
  {
    final AddressDO address = addressDao.getOrLoad(getData().getId());
    final PersonalAddressDO personalAddress = form.personalAddress;
    personalAddress.setAddress(address);
    personalAddressDao.setOwner(personalAddress, getUserId()); // Set current logged in user as owner.
    personalAddressDao.saveOrUpdate(personalAddress);
    return null;
  }

  @Override
  protected AddressDao getBaseDao()
  {
    return addressDao;
  }

  @Override
  protected AddressEditForm newEditForm(AbstractEditPage< ? , ? , ? > parentPage, AddressDO data)
  {
    return new AddressEditForm(this, data);
  }

  @Override
  protected Logger getLogger()
  {
    return log;
  }
}
