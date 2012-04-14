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

import org.apache.wicket.spring.injection.annot.SpringBean;
import org.projectforge.address.AddressDO;
import org.projectforge.address.AddressDao;
import org.projectforge.address.PersonalAddressDao;
import org.projectforge.web.mobile.AbstractMobileEditForm;

public class AddressMobileEditForm extends AbstractMobileEditForm<AddressDO, AddressMobileEditPage>
{
  private static final long serialVersionUID = -8781593985402346929L;

  @SpringBean(name = "addressDao")
  private AddressDao addressDao;

  @SpringBean(name = "personalAddressDao")
  private PersonalAddressDao personalAddressDao;

  protected AddressEditSupport addressEditSupport;

  public AddressMobileEditForm(final AddressMobileEditPage parentPage, final AddressDO data)
  {
    super(parentPage, data);
  }

  @Override
  protected void init()
  {
    super.init();
    addressEditSupport = new AddressEditSupport(this, gridBuilder, addressDao, personalAddressDao, data);
    gridBuilder.newCollapsiblePanel(data.getFullNameWithTitleAndForm());
    addressEditSupport.addName();
  }
}
