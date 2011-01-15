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
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.projectforge.address.AddressDO;
import org.projectforge.address.PersonalAddressDO;
import org.projectforge.address.PersonalAddressDao;
import org.projectforge.web.wicket.AbstractEditForm;
import org.projectforge.web.wicket.layout.LayoutContext;

public class AddressEditForm extends AbstractEditForm<AddressDO, AddressEditPage>
{
  private static final long serialVersionUID = 3881031215413525517L;

  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(AddressEditForm.class);

  @SpringBean(name = "personalAddressDao")
  private PersonalAddressDao personalAddressDao;

  protected AddressFormRenderer renderer;

  protected PersonalAddressDO personalAddress;

  public AddressEditForm(AddressEditPage parentPage, AddressDO data)
  {
    super(parentPage, data);
    personalAddress = null;
    if (isNew() == false) {
      personalAddress = personalAddressDao.getByAddressId(getData().getId());
    }
    if (personalAddress == null) {
      personalAddress = new PersonalAddressDO();
    }
    renderer = new AddressFormRenderer(this, new LayoutContext(this), parentPage.getBaseDao(), data, personalAddress);
  }

  @Override
  protected void init()
  {
    super.init();
    renderer.add();
  }
  
  @Override
  protected void validation()
  {
    super.validation();
    renderer.validation();
  }

  @Override
  protected Logger getLogger()
  {
    return log;
  }
}
