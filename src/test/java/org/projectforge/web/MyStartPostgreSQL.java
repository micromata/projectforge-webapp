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

import org.projectforge.common.DatabaseDialect;
import org.projectforge.shared.storage.StorageConstants;
import org.projectforge.webserver.StartSettings;

/**
 * For larger installations please increase memory by giving the following start VM parameters: -Xmx1024m -Xms512m -XX:PermSize=96m
 * -XX:MaxPermSize=192m <br/>
 * Please note: you need to add the PostgreSQL jdbc driver (edit run configuration and add the jar file as external jar to the classpath).
 * @author Kai Reinhard (k.reinhard@micromata.de)
 * @see Start
 */
public class MyStartPostgreSQL
{
  /**
   * Should be set to true only at first start:
   */
  private static final boolean SCHEMA_UPDATE = false;

  private static final boolean DEVELOPMENT_MODE = true;

  private static final String BASE_DIR = System.getProperty("user.home") + "/ProjectForge";

  private static final String JDBC_USER = "projectforge";

  public static void main(final String[] args) throws Exception
  {
    // Please don't forget to add your Postgresql driver, such as postgresql-9.0-802.jdbc3.jar
    // Open Run/Debug configurations dialog (Eclipse context menu of this class) and add external jar to classpath.
    final StartSettings settings = new StartSettings(DatabaseDialect.PostgreSQL, BASE_DIR);
    settings.setJdbcUser(JDBC_USER);
    settings.setSchemaUpdate(SCHEMA_UPDATE);
    //settings.setUsingCookies(false);
    settings.setDevelopment(DEVELOPMENT_MODE);
    settings.setLaunchBrowserAfterStartup(true);
    // Set the url of ProjectForge's storage web server:
    System.setProperty(StorageConstants.SYSTEM_PROPERTY_URL, "http://localhost:8081/");
    final StartHelper startHelper = new StartHelper(settings);
    startHelper.start();
  }
}
