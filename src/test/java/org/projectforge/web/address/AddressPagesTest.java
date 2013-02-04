/////////////////////////////////////////////////////////////////////////////
//
// Project ProjectForge Community Edition
//         www.projectforge.org
//
// Copyright (C) 2001-2013 Kai Reinhard (k.reinhard@micromata.de)
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

import org.apache.wicket.Component;
import org.apache.wicket.extensions.markup.html.repeater.data.table.DataTable;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.util.tester.FormTester;
import org.junit.Assert;
import org.junit.Test;
import org.projectforge.address.AddressDO;
import org.projectforge.address.AddressDao;
import org.projectforge.address.AddressStatus;
import org.projectforge.address.ContactStatus;
import org.projectforge.address.FormOfAddress;
import org.projectforge.web.wicket.AbstractEditPage;
import org.projectforge.web.wicket.AbstractListPage;
import org.projectforge.web.wicket.ListAndEditPagesTestBase;

public class AddressPagesTest extends ListAndEditPagesTestBase
{
  AddressDao addressDao;

  @SuppressWarnings("unchecked")
  @Test
  public void testViewPages()
  {
    loginTestAdmin();
    tester.startPage(AddressListPage.class);
    tester.assertRenderedPage(AddressListPage.class);

    // Now, add a new address:
    tester.clickLink(findComponentByAccessKey(tester, PATH_CONTENT_MENU_REPEATER, 'n'));
    tester.assertRenderedPage(AddressEditPage.class);
    // Need new page to initialize model:
    final AddressEditPage editPage = new AddressEditPage(new PageParameters());
    final AddressDO data = editPage.getForm().getData();
    data.setName("Reinhard").setFirstName("Kai").setForm(FormOfAddress.MISTER).setContactStatus(ContactStatus.ACTIVE)
    .setAddressStatus(AddressStatus.UPTODATE).setTask(getTask("1.1"));
    tester.startPage(editPage);
    FormTester form = tester.newFormTester(PATH_EDITPAGE_FORM);
    form.submit(findComponentByLabel(form, KEY_EDITPAGE_BUTTON_CREATE));
    tester.assertRenderedPage(AddressListPage.class);
    final DataTable<AddressDO, String> table = (DataTable<AddressDO, String>) tester.getComponentFromLastRenderedPage(PATH_LISTPAGE_TABLE);
    Assert.assertEquals(1, table.getRowCount());

    // Check view page
    tester.clickLink("body:form:table:body:rows:1:cells:1:cell:2:link"); // View page
    tester.assertRenderedPage(AddressViewPage.class);

    // Check mobile view page
    final Integer id = addressDao.internalLoadAll().get(0).getId();
    final PageParameters params = new PageParameters().add(AbstractEditPage.PARAMETER_KEY_ID, id);
    tester.startPage(AddressMobileViewPage.class, params);
    tester.assertRenderedPage(AddressMobileViewPage.class);

    // Delete entry
    tester.startPage(AddressListPage.class);
    tester.assertRenderedPage(AddressListPage.class);
    final Component link = findComponentByLabel(tester, PATH_LISTPAGE_TABLE, "select");
    tester.clickLink(link); // Edit page
    tester.assertRenderedPage(AddressEditPage.class);
    form = tester.newFormTester(PATH_EDITPAGE_FORM);
    form.submit(findComponentByLabel(form, KEY_EDITPAGE_BUTTON_MARK_AS_DELETED));
  }

  @Override
  protected AbstractEditPage< ? , ? , ? > getEditPageWithPrefilledData()
  {
    final AddressEditPage editPage = new AddressEditPage(new PageParameters());
    final AddressDO data = editPage.getForm().getData();
    data.setName("Reinhard").setFirstName("Kai").setForm(FormOfAddress.MISTER).setContactStatus(ContactStatus.ACTIVE)
    .setAddressStatus(AddressStatus.UPTODATE).setTask(getTask("1.1"));
    return editPage;
  }

  @Override
  protected Class< ? extends AbstractEditPage< ? , ? , ? >> getEditPageClass()
      {
    return AddressEditPage.class;
      }

  @Override
  protected Class< ? extends AbstractListPage< ? , ? , ? >> getListPageClass()
      {
    return AddressListPage.class;
      }

  /**
   * @param addressDao the addressDao to set
   * @return this for chaining.
   */
  public void setAddressDao(final AddressDao addressDao)
  {
    this.addressDao = addressDao;
  }
}
