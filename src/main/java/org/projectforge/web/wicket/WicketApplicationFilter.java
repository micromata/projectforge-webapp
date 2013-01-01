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

package org.projectforge.web.wicket;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.apache.wicket.Application;

/**
 * Holt die Wicket-Application und setzt sie in das passende Wicket-ThreadLocal.
 */
public class WicketApplicationFilter implements Filter
{

  private Application application;

  public Application getApplication()
  {
    return application;
  }

  public void setApplication(final Application application)
  {
    this.application = application;
  }

  public void destroy()
  {
    // blank
  }

  public void init(final FilterConfig filterConfig) throws ServletException
  {
    // blank
  }

  public void doFilter(final ServletRequest request, final ServletResponse response, final FilterChain chain) throws IOException, ServletException
  {
    // Sollte eigentlich immer NULL ergeben, aber man weiss nie ...
    final Application previousOne = (Application.exists() == true) ? Application.get() : null;
    org.apache.wicket.ThreadContext.setApplication(this.application);
    try {
      chain.doFilter(request, response);
    } finally {
      if (previousOne != null) {
        org.apache.wicket.ThreadContext.setApplication(previousOne);
      } else {
        org.apache.wicket.ThreadContext.setApplication(null);
      }
    }
  }

}
