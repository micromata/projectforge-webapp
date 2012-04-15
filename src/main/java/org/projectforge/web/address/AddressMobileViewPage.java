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

import org.apache.log4j.Logger;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.projectforge.address.AddressDO;
import org.projectforge.address.AddressDao;
import org.projectforge.web.mobile.AbstractMobileViewPage;
import org.projectforge.web.mobile.AbstractSecuredMobilePage;
import org.projectforge.web.wicket.mobileflowlayout.LabelValueDataTablePanel;

public class AddressMobileViewPage extends AbstractMobileViewPage<AddressDO, AddressDao>
{
  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(AddressMobileViewPage.class);

  private static final long serialVersionUID = 4478785262257939098L;

  @SpringBean(name = "addressDao")
  private AddressDao addressDao;

  public AddressMobileViewPage(final PageParameters parameters)
  {
    super(parameters);
    gridBuilder.newCollapsiblePanel(data.getFullNameWithTitleAndForm());
    final AddressPageSupport pageSupport = new AddressPageSupport(data);
    final LabelValueDataTablePanel table = gridBuilder.newLabelValueDataTable();
    table.addRow(pageSupport.getOrganizationProperties());
    table.addRow(pageSupport.getPositionTextProperties());
    table.addRow(pageSupport.getAddressStatusProperties());
    table.addRow(pageSupport.getWebsiteProperties());
    gridBuilder.newCollapsiblePanel(getString("address.business"));
    table.addRow(pageSupport.getWebsiteProperties());
  }

  @Override
  protected AddressDao getBaseDao()
  {
    return addressDao;
  }

  /**
   * @see org.projectforge.web.mobile.AbstractMobileViewPage#getListPageClass()
   */
  @Override
  protected Class< ? extends AbstractSecuredMobilePage> getListPageClass()
  {
    return AddressMobileListPage.class;
  }

  @Override
  protected Logger getLogger()
  {
    return log;
  }

  /**
   * @see org.projectforge.web.mobile.AbstractMobilePage#getTitle()
   */
  @Override
  protected String getTitle()
  {
    return getString("address.title.view");
  }
}
