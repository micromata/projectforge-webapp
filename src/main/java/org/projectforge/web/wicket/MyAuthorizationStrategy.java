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

package org.projectforge.web.wicket;

import org.apache.wicket.Component;
import org.apache.wicket.authorization.Action;
import org.apache.wicket.authorization.IAuthorizationStrategy;
import org.apache.wicket.markup.html.WebPage;
import org.projectforge.web.LoginPage;
import org.projectforge.web.mobile.LoginMobilePage;

/**
 * @author Kai Reinhard (k.reinhard@micromata.de)
 * 
 */
public class MyAuthorizationStrategy implements IAuthorizationStrategy// , IUnauthorizedComponentInstantiationListener
{
  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(MyAuthorizationStrategy.class);

  public boolean isActionAuthorized(Component component, Action action)
  {
    return true;
  }

  public <T extends Component> boolean isInstantiationAuthorized(Class<T> componentClass)
  {
    if (WebPage.class.isAssignableFrom(componentClass) == true) {
      if (componentClass.equals(LoginPage.class) == true || componentClass.equals(LoginMobilePage.class) == true) {
        return true;
      }
      boolean isAuthenticated = MySession.get().isAuthenticated();
      if (isAuthenticated == false) {
        log.fatal("This should not occur, because LoginFilter should avoid this!");
        throw new RuntimeException("Fatal security error!");
      }
      return isAuthenticated;
    }
    return true;
  }

  // public void onUnauthorizedInstantiation(Component component)
  // {
  // if (AbstractSecuredPage.class.isAssignableFrom(component.getClass()) == false) {
  // throw new RestartResponseAtInterceptPageException(new RedirectPage("../secure/Login.action"));
  // }
  // final AbstractSecuredPage page = (AbstractSecuredPage) component;
  // final String url = page.getUrl("/secure/Login.action");
  // throw new RestartResponseAtInterceptPageException(new RedirectPage(url));
  // }
}
