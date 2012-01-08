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

import java.io.IOException;
import java.lang.reflect.Constructor;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.wicket.Component;
import org.apache.wicket.protocol.http.BufferedWebResponse;
import org.apache.wicket.protocol.http.MockHttpServletRequest;
import org.apache.wicket.protocol.http.MockHttpServletResponse;
import org.apache.wicket.protocol.http.MockHttpSession;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.protocol.http.WebRequest;
import org.apache.wicket.protocol.http.WebRequestCycle;
import org.apache.wicket.protocol.http.servlet.ServletWebRequest;
import org.projectforge.web.core.MenuPanel;
import org.springframework.beans.BeanUtils;


/**
 * Rendern einer Wicket-Component innerhalb einer jsp-Page.
 */
public class JspRenderer
{
  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(JspRenderer.class);

  public static String renderComponent(HttpServletRequest request, HttpServletResponse response, ServletContext application,
      Class< ? extends Component> componentClass) throws IOException
  {
    String s = "???";
    s = renderComponentInternal(componentClass);
    return s;
  }

  protected static String renderComponentInternal(Class< ? extends Component> componentClass)
  {

    // get the servlet context
    WebApplication application = WebApplication.get();

    ServletContext context = application.getServletContext();

    // fake a request/response cycle
    MockHttpSession servletSession = new MockHttpSession(context);
    servletSession.setTemporary(true);

    MockHttpServletRequest servletRequest = new MockHttpServletRequest(application, servletSession, context);
    MockHttpServletResponse servletResponse = new MockHttpServletResponse(servletRequest);

    // initialize request and response
    servletRequest.initialize();
    servletResponse.initialize();

    WebRequest webRequest = new ServletWebRequest(servletRequest);

    BufferedWebResponse webResponse = new BufferedWebResponse(servletResponse);
    webResponse.setAjax(true);

    WebRequestCycle requestCycle = new WebRequestCycle(application, webRequest, webResponse);

    final Component component = buildInstance(componentClass);
    // final Component component = new Label("theOne", "Test");
    final OneComponentPage page = new OneComponentPage();
    page.setVersioned(false);
    page.add(component);

    if (component instanceof MenuPanel) {
      ((MenuPanel) component).init();
    }
    try {
      component.renderComponent();

      if (requestCycle.wasHandled() == false) {
        log.warn("Component was not rendered in renderComponentInstance()");
      }
      requestCycle.detach();

    } finally {
      requestCycle.getResponse().close();
    }

    return webResponse.toString();
  }

  /**
   * Instanz der Komponente erzeugen
   * @param componentClass Nie <code>null</code>
   * @return Immer gesetzt.
   */
  protected static Component buildInstance(Class< ? extends Component> componentClass)
  {

    try {
      final Constructor< ? extends Component> ctor = componentClass.getDeclaredConstructor(String.class);
      final Component comp = (Component) BeanUtils.instantiateClass(ctor, new Object[] { "theOne"});
      return comp;
    } catch (NoSuchMethodException e) {
      throw new RuntimeException("Component has no ctor(String)", e);
    }
  }
}
