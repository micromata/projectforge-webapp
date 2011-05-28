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

import org.apache.wicket.PageParameters;
import org.apache.wicket.extensions.markup.html.repeater.data.table.DataTable;
import org.apache.wicket.util.tester.FormTester;
import org.junit.Assert;
import org.junit.Test;
import org.projectforge.address.AddressDO;
import org.projectforge.address.AddressStatus;
import org.projectforge.address.ContactStatus;
import org.projectforge.address.FormOfAddress;
import org.projectforge.web.wicket.WicketPageTestBase;

public class AddressPagesTest extends WicketPageTestBase
{
  @SuppressWarnings("unchecked")
  @Test
  public void testRenderPages()
  {
    loginTestAdmin();
    tester.startPage(new AddressListPage(new PageParameters()));
    tester.assertRenderedPage(AddressListPage.class);
    DataTable<AddressDO> table = (DataTable<AddressDO>) tester.getComponentFromLastRenderedPage("body:form:table");
    Assert.assertEquals(0, table.getRowCount());
    // Now, add a new address:
    tester.clickLink("body:contentMenuArea:menu:1:link");
    tester.assertRenderedPage(AddressEditPage.class);
    // Need new page to initialize model:
    AddressEditPage editPage = new AddressEditPage(new PageParameters());
    final AddressDO data = editPage.getForm().getData();
    data.setName("Reinhard").setFirstName("Kai").setForm(FormOfAddress.MISTER).setContactStatus(ContactStatus.ACTIVE).setAddressStatus(
        AddressStatus.UPTODATE).setTask(getTask("1.1"));
    tester.startPage(editPage);
    FormTester form = tester.newFormTester("body:form");
    form.submit("create:button");
    tester.assertRenderedPage(AddressListPage.class);
    table = (DataTable<AddressDO>) tester.getComponentFromLastRenderedPage("body:form:table");
    Assert.assertEquals(1, table.getRowCount());
    tester.clickLink("body:form:table:body:rows:1:cells:1:cell:1:select"); // Edit page
    tester.assertRenderedPage(AddressEditPage.class);
    editPage = (AddressEditPage) tester.getLastRenderedPage();
    Assert.assertEquals("Kai", editPage.getForm().getData().getFirstName());
    form = tester.newFormTester("body:form");
    form.submit("cancel:button");
    tester.assertRenderedPage(AddressListPage.class);
    tester.clickLink("body:form:table:body:rows:2:cells:1:cell:2:link"); // View page
    tester.assertRenderedPage(AddressViewPage.class);
  }
}
