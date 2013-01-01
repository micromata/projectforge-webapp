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

package org.projectforge.web;

import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.util.tester.FormTester;
import org.junit.Assert;
import org.junit.Test;
import org.projectforge.test.TestBase;
import org.projectforge.web.address.AddressListPage;
import org.projectforge.web.admin.SystemUpdatePage;
import org.projectforge.web.calendar.CalendarPage;
import org.projectforge.web.wicket.WicketPageTestBase;
import org.projectforge.web.wicket.WicketUtils;

public class LoginPageTest extends WicketPageTestBase
{
  @Test
  public void testLoginAndLogout()
  {
    final LoginPage loginPage = new LoginPage(new PageParameters());
    // start and render the test page
    tester.startPage(loginPage);
    // assert rendered page class
    tester.assertRenderedPage(LoginPage.class);
    // assert rendered label component
    tester.assertVisible("body:form:username");
    FormTester form = tester.newFormTester("body:form");
    form.setValue(findComponentByLabel(form, "username"), "demo");
    form.setValue(findComponentByLabel(form,"password"), "wrong");
    form.submit(KEY_LOGINPAGE_BUTTON_LOGIN);
    tester.assertRenderedPage(LoginPage.class);
    form = tester.newFormTester("body:form");
    form.setValue(findComponentByLabel(form,"username"), TestBase.TEST_ADMIN_USER);
    form.setValue(findComponentByLabel(form,"password"), TestBase.TEST_ADMIN_USER_PASSWORD);
    form.submit(KEY_LOGINPAGE_BUTTON_LOGIN);
    tester.assertRenderedPage(CalendarPage.class);
    tester.startPage(AddressListPage.class);
    tester.assertRenderedPage(AddressListPage.class);

    loginTestAdmin(); // login should be ignored.
    tester.assertRenderedPage(WicketUtils.getDefaultPage());

    logout();
    try {
      tester.startPage(AddressListPage.class);
      Assert.fail("Page must not be available, user not logged-in.");
    } catch (final WicketRuntimeException ex) {
      // Everything fine.
    }
  }

  @Test
  public void testInternalCheckLogin()
  {
    login(TestBase.TEST_USER, TestBase.TEST_USER_PASSWORD);
    logout();
    login(TestBase.TEST_DELETED_USER, TestBase.TEST_DELETED_USER_PASSWORD, false);
    tester.assertRenderedPage(LoginPage.class);

    // Update required
    UserFilter.setUpdateRequiredFirst(true);
    login(TestBase.TEST_USER, TestBase.TEST_USER_PASSWORD, false);
    tester.assertRenderedPage(LoginPage.class);
    login(TestBase.TEST_DELETED_USER, TestBase.TEST_DELETED_USER_PASSWORD, false);
    tester.assertRenderedPage(LoginPage.class);
    login(TestBase.TEST_ADMIN_USER, TestBase.TEST_ADMIN_USER_PASSWORD, false);
    tester.assertRenderedPage(SystemUpdatePage.class);
    logout();
    UserFilter.setUpdateRequiredFirst(false);
    login(TestBase.TEST_USER, TestBase.TEST_USER_PASSWORD);
    logout();
  }
}
