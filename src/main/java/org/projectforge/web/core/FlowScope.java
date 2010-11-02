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

package org.projectforge.web.core;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;

/**
 */
public class FlowScope implements Serializable
{
  private static final long serialVersionUID = 7083583841619862173L;

  public static final int DEFAULT_TIMEOUT_IN_SECONDS = 600;

  public static final String FLOW_SCOPE_ID = "__flowid";

  private static final Logger log = Logger.getLogger(FlowScope.class);

  private static int lastId = 42;

  private static long lastCleanTime = 0;

  private Map<String, Object> map = new HashMap<String, Object>();

  private long lastAccess;

  private int timeout = DEFAULT_TIMEOUT_IN_SECONDS;

  private String key;

  protected static synchronized String getUniqueId()
  {
    return String.valueOf(++lastId);
  }

  protected FlowScope()
  {
    this.key = getUniqueId();
    touch();
  }

  public int getTimeout()
  {
    return timeout;
  }

  public void setTimeout(int timeout)
  {
    this.timeout = timeout;
  }

  public String key()
  {
    return this.key;
  }

  /**
   * Stores the provided value <b>both</b> in the flow scope a under the specified name, and in a request attribute with the
   * specified name. Allows flow scope attributes to be accessed seamlessly as request attributes during both the current request and
   * the subsequent request.
   * 
   * @param name the name of the attribute to add to flow scope
   * @param value the value to be added
   * @return the previous object stored with the same name (possibly null)
   */
  public Object put(String key, Object value)
  {
    return map.put(key, value);
  }

  public Object get(String key)
  {
    return map.get(key);
  }
  
  public Object remove(String key) {
    return map.remove(key);
  }

  public String getKey()
  {
    return key;
  }

  public void touch()
  {
    this.lastAccess = System.currentTimeMillis() / 1000;
  }

  public void closeFlowScope(HttpServletRequest request)
  {
    Map<String, FlowScope> scopes = getContainer(request, false);
    scopes.remove(this.getKey());
  }

  /**
   * Gets the current flow scope into which items can be stored temporarily.
   * 
   * @param request the current request
   * @param create if true then the FlowScope will be created when it does not exist already
   * @return the current FlowScope, or null if it does not exist and create is false
   */
  public static FlowScope getCurrent(HttpServletRequest request, ExtendedActionBean actionBean, boolean create)
  {
    Map<String, FlowScope> scopes = getContainer(request, create);

    if (scopes == null) {
      return null;
    }
    FlowScope scope = null;
    String key = actionBean.getFlowKey();
    if (key == null) {
      key = request.getParameter(FLOW_SCOPE_ID);
      if (key == null) {
        key = (String) request.getAttribute(FLOW_SCOPE_ID);
      }
    }
    if (key != null) {
      scope = scopes.get(key);
      if (scope != null) {
        scope.touch();
        actionBean.setFlowScope(scope);
      }
    }
    if (scope == null && create == true) {
      scope = new FlowScope();
      scopes.put(scope.getKey(), scope);
      actionBean.setFlowScope(scope);
    }
    return scope;
  }

  /**
   * Internal helper method to retreive (and selectively create) the container for all the flow scopes. Will return null if the
   * container does not exist and <i>create</i> is false. Will also return null if the current session has been invalidated, regardless of
   * the value of <i>create</i>.
   * 
   * @param request the current request
   * @param create if true, create the container when it doesn't exist.
   * @return a Map of integer keys to FlowScope objects
   */
  @SuppressWarnings("unchecked")
  private static Map<String, FlowScope> getContainer(HttpServletRequest request, boolean create)
  {
    HttpSession session = request.getSession(create);
    Map<String, FlowScope> scopes = null;
    if (session != null) {
      scopes = (Map<String, FlowScope>) session.getAttribute(FLOW_SCOPE_ID);
      if (scopes != null) {
        clean(scopes);
      } else if (create == true) {
        scopes = new HashMap<String, FlowScope>();
        session.setAttribute(FLOW_SCOPE_ID, scopes);
      }
    }
    return scopes;
  }

  private static void clean(Map<String, FlowScope> scopes)
  {
    long currentTime = System.currentTimeMillis() / 1000;
    if (currentTime - lastCleanTime > DEFAULT_TIMEOUT_IN_SECONDS) {
      log.debug("Cleaning up flow scopes ...");
      // Clean up any old-age flash scopes
      Collection<FlowScope> flashes = scopes.values();
      Iterator<FlowScope> iterator = flashes.iterator();
      while (iterator.hasNext()) {
        FlowScope scope = iterator.next();
        if (scope.isExpired(currentTime) == true) {
          iterator.remove();
          log.debug("Removing expired flow scope: " + scope.getKey());
        }
      }
      lastCleanTime = currentTime;
    }
  }

  /**
   * 
   * @param scope
   * @param currentTime current time in seconds
   * @return
   */
  private boolean isExpired(long currentTime)
  {
    long age = currentTime - this.lastAccess;
    return age > this.timeout;
  }

}
