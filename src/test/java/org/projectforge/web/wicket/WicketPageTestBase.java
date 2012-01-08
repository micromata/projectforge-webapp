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

package org.projectforge.web.wicket;

import org.apache.commons.lang.ClassUtils;
import org.apache.wicket.Page;
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
import org.projectforge.user.UserXmlPreferencesCache;
import org.projectforge.web.LoginPage;
import org.projectforge.web.MenuBuilder;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * Your wicket tester class must extends this or any derived class from TestBase for correct initialization of Spring, data-base, resource
 * locator etc. Before your tests a new data-base is initialized and set-up with test data.
 * @author Kai Reinhard (k.reinhard@micromata.de)
 * 
 */
public class WicketPageTestBase extends TestBase
{
  protected WicketTester tester;

  private MenuBuilder menuBuilder;

  private UserXmlPreferencesCache userXmlPreferencesCache;

  @Before
  public void setUpWicketApplication()
  {
    tester = new WicketTester(new WebApplication() {
      @Override
      protected void init()
      {
        super.init();
        final ClassPathXmlApplicationContext context = getTestConfiguration().getApplicationContext();
        addComponentInstantiationListener(new SpringComponentInjector(this, context, true));
        getResourceSettings().setResourceStreamLocator(new MyResourceStreamLocator());
        getResourceSettings().addStringResourceLoader(0, new BundleStringResourceLoader(WicketApplication.RESOURCE_BUNDLE_NAME));
        final ConfigurableListableBeanFactory beanFactory = context.getBeanFactory();
        beanFactory.autowireBeanProperties(this, AutowireCapableBeanFactory.AUTOWIRE_BY_NAME, false);
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
   * Logs the user in, if not already logged-in. If an user is already logged in then nothing is done. Therefore you must log-out an user
   * before any new login.
   * @param username
   * @param password not encrypted.
   */
  public void login(final String username, final String password)
  {
    // start and render the test page
    tester.startPage(LoginPage.class);
    if (ClassUtils.isAssignable(tester.getLastRenderedPage().getClass(), WicketApplication.DEFAULT_PAGE) == true) {
      // Already logged-in.
      return;
    }
    // assert rendered page class
    tester.assertRenderedPage(LoginPage.class);
    final FormTester form = tester.newFormTester("body:form");
    form.setValue("username", username);
    form.setValue("password", password);
    form.submit("login:button");
    tester.assertRenderedPage(WicketApplication.DEFAULT_PAGE);
  }

  public void loginTestAdmin()
  {
    login(TestBase.TEST_ADMIN_USER, TestBase.TEST_ADMIN_USER_PASSWORD);
  }

  /**
   * Logs any current logged-in user out and calls log-in page.
   */
  protected void logout()
  {
    LoginPage.logout((MySession) tester.getWicketSession(), tester.getWicketRequest(), tester
        .getWicketResponse(), userXmlPreferencesCache, menuBuilder);
    tester.startPage(LoginPage.class);
    tester.assertRenderedPage(LoginPage.class);
  }

  public void setUserXmlPreferencesCache(final UserXmlPreferencesCache userXmlPreferencesCache)
  {
    this.userXmlPreferencesCache = userXmlPreferencesCache;
  }

  public void setMenuBuilder(final MenuBuilder menuBuilder)
  {
    this.menuBuilder = menuBuilder;
  }
}
