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

package org.projectforge.web.wicket;

import org.apache.wicket.extensions.markup.html.repeater.data.table.DataTable;
import org.apache.wicket.util.tester.FormTester;
import org.junit.Assert;
import org.junit.Test;

/**
 * Use-full super class for testing standard list pages (derived from AbstractListPage).
 * @author Kai Reinhard (k.reinhard@micromata.de)
 * 
 */
public abstract class ListAndEditPagesTestBase extends WicketPageTestBase
{
  @Test
  public void baseTests()
  {
    loginTestAdmin();
    tester.startPage(getListPageClass());
    tester.assertRenderedPage(getListPageClass());
    DataTable< ? > table = (DataTable< ? >) tester.getComponentFromLastRenderedPage("body:form:table");
    Assert.assertEquals(getNumberOfExistingListElements(), table.getRowCount());

    // Now, add a new element:
    tester.clickLink("body:contentMenuArea:menu:1:link");
    tester.assertRenderedPage(getEditPageClass());

    // Need new page to initialize model:
    final AbstractEditPage< ? , ? , ? > editPage = getEditPageWithPrefilledData();
    tester.startPage(editPage);
    FormTester form = tester.newFormTester("body:form");
    form.submit("create:button");

    // Now check list page
    tester.assertRenderedPage(getListPageClass());
    table = (DataTable<?>) tester.getComponentFromLastRenderedPage("body:form:table");
    Assert.assertEquals(getNumberOfExistingListElements() + 1, table.getRowCount());

    // Now re-enter edit page
    tester.clickLink("body:form:table:body:rows:1:cells:1:cell:1:select"); // Edit page
    tester.assertRenderedPage(getEditPageClass());
    checkEditPage();
    form = tester.newFormTester("body:form");
    form.submit("markAsDeleted:button");

    // Now check list page again after object was deleted:
    tester.assertRenderedPage(getListPageClass());
    table = (DataTable<?>) tester.getComponentFromLastRenderedPage("body:form:table");
    Assert.assertEquals(getNumberOfExistingListElements(), table.getRowCount());
  }

  protected abstract Class< ? extends AbstractListPage< ? , ? , ? >> getListPageClass();

  protected abstract Class< ? extends AbstractEditPage< ? , ? , ? >> getEditPageClass();

  /**
   * Creates a new edit page and pre-fills all fields of the data model which are at least required to create a new data-base entry.<br>
   * Example:<br>
   * 
   * <pre>
   * AddressEditPage editPage = new AddressEditPage(new PageParameters());
   * final AddressDO data = editPage.getForm().getData();
   * data.setName(&quot;Reinhard&quot;).setFirstName(&quot;Kai&quot;))....;
   * return editPage.
   * </pre>
   * @return
   */
  protected abstract AbstractEditPage< ? , ? , ? > getEditPageWithPrefilledData();

  /**
   * Optional checks of the fields of the edit page after an object was inserted and clicked in the list page.<br>
   * Example:<br>
   * 
   * <pre>
   * AddressEditPage editPage = (AddressEditPage) tester.getLastRenderedPage();
   * AddressDO address = editPage.getForm().getData();
   * Assert.assertEquals(&quot;Kai&quot;, address.getFirstName());
   * </pre>
   */
  protected void checkEditPage()
  {

  }

  /**
   * Override this method if any test object entries do exist in the list (default is 0).
   * @return
   */
  protected int getNumberOfExistingListElements()
  {
    return 0;
  }
}
