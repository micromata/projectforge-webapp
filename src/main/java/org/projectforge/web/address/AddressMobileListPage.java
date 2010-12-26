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
import org.projectforge.address.AddressDO;
import org.projectforge.address.AddressDao;
import org.projectforge.web.mobile.AbstractMobileListPage;

public class AddressMobileListPage extends AbstractMobileListPage<AddressMobileListForm, AddressDao, AddressDO>
{
  private static final long serialVersionUID = -5138249821780619306L;

  @SpringBean(name = "addressDao")
  private AddressDao addressDao;

  public AddressMobileListPage(final PageParameters parameters)
  {
    super("address", parameters);
  }

  @Override
  protected AddressDao getBaseDao()
  {
    return addressDao;
  }

  @Override
  protected AddressMobileListForm newListForm(AbstractMobileListPage< ? , ? , ? > parentPage)
  {
    return new AddressMobileListForm(this);
  }

  @Override
  protected String getEntryName(final AddressDO entry)
  {
    return entry.getFirstName() + " " + entry.getName();
  }

  @Override
  protected String getEntryComment(AddressDO entry)
  {
    return entry.getOrganization();
  }
}
