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

package org.projectforge.web.filter;

import org.springframework.web.context.WebApplicationContext;

/**
 * @author wolle
 */
public class SpringContext
{
  private static ThreadLocal<WebApplicationContext> CTX = new ThreadLocal<WebApplicationContext>();

  public static WebApplicationContext getWebApplicationContext()
  {
    return CTX.get();
  }

  static void setWebApplicationContext(WebApplicationContext ctx)
  {
    CTX.set(ctx);
  }

  @SuppressWarnings("unchecked")
  public static <T> T getBean(Class<T> clazz)
  {
    String[] beanNamesForType = getWebApplicationContext().getBeanNamesForType(clazz);
    return (T) getWebApplicationContext().getBean(beanNamesForType[0], clazz);
  }
}
