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

import org.apache.wicket.Page;
import org.apache.wicket.PageParameters;
import org.apache.wicket.Request;
import org.apache.wicket.Response;
import org.apache.wicket.Session;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.resource.loader.BundleStringResourceLoader;
import org.apache.wicket.spring.injection.annot.SpringComponentInjector;
import org.apache.wicket.util.tester.FormTester;
import org.apache.wicket.util.tester.WicketTester;
import org.junit.Before;
import org.projectforge.test.TestBase;
import org.projectforge.web.LoginPage;

public class WicketPageTestBase extends TestBase
{
  protected WicketTester tester;

  @Before
  public void setUpWicketApplication()
  {
    tester = new WicketTester(new WebApplication() {
      @Override
      protected void init()
      {
        super.init();
        addComponentInstantiationListener(new SpringComponentInjector(this, getTestConfiguration().getApplicationContext(), true));
        getResourceSettings().setResourceStreamLocator(new MyResourceStreamLocator());
        getResourceSettings().addStringResourceLoader(0, new BundleStringResourceLoader(WicketApplication.RESOURCE_BUNDLE_NAME));
        getResourceSettings().setThrowExceptionOnMissingResource(false); // Don't throw MissingResourceException for missing i18n keys.
      }

      @Override
      public Class< ? extends Page> getHomePage()
      {
        return WicketApplication.DEFAULT_PAGE;
      }

      @Override
      public Session newSession(final Request request, final Response response)
      {
        return new MySession(request);
      }
    });
  }

  /**
   * 
   * @param username
   * @param password not encrypted.
   */
  protected void login(final String username, final String password)
  {
    final LoginPage loginPage = new LoginPage(new PageParameters());
    // start and render the test page
    tester.startPage(loginPage);
    // assert rendered page class
    tester.assertRenderedPage(LoginPage.class);
    final FormTester form = tester.newFormTester("body:form");
    form.setValue("username", username);
    form.setValue("password", password);
    form.submit("login:button");
    tester.assertRenderedPage(WicketApplication.DEFAULT_PAGE);
  }

  protected void loginTestAdmin()
  {
    login(TestBase.TEST_ADMIN_USER, TestBase.TEST_ADMIN_USER_PASSWORD);
  }
}
