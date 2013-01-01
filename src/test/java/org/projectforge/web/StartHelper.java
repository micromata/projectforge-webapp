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

import org.eclipse.jetty.webapp.WebAppContext;
import org.projectforge.webserver.AbstractStartHelper;
import org.projectforge.webserver.StartSettings;

/**
 * @author Kai Reinhard (k.reinhard@micromata.de)
 */
public class StartHelper extends AbstractStartHelper
{

  /**
   * @param startSettings
   */
  public StartHelper(final StartSettings startSettings)
  {
    super(startSettings);
  }

  /**
   * @see org.projectforge.webserver.AbstractStartHelper#getWebAppContext()
   */
  @Override
  protected WebAppContext getWebAppContext()
  {
    final WebAppContext webAppContext = new WebAppContext();
    webAppContext.setClassLoader(this.getClass().getClassLoader());
    webAppContext.setConfigurationClasses(CONFIGURATION_CLASSES);
    webAppContext.setContextPath("/ProjectForge");
    webAppContext.setWar("src/main/webapp");
    webAppContext.setDescriptor("src/main/webapp/WEB-INF/web.xml");
    webAppContext.setExtraClasspath("target/classes");
    webAppContext.setInitParameter("development", String.valueOf(startSettings.isDevelopment()));
    webAppContext.setInitParameter("stripWicketTags", String.valueOf(startSettings.isStripWicketTags()));
    return webAppContext;
  }
}
