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

package org.projectforge.web;

import java.io.File;
import java.util.TimeZone;

import org.projectforge.database.HibernateDialect;

/**
 * The settings for starting ProjectForge web server Jetty.
 * @author Kai Reinhard (k.reinhard@micromata.de)
 * 
 */
public class StartSettings
{
  private int port = 8080;

  private String baseDir;

  private boolean development = true;

  private boolean schemaUpdate = true;

  private String jdbcUrl;

  private String jdbcDriverClass;

  private String jdbcUser;

  private String jdbcPassword = "";

  private String dialect;

  private boolean stripWicketTags = true;

  private boolean usingCookies = true;

  private Integer jdbcMaxActive;

  private boolean launchBrowserAfterStartup = true;

  private static final String DEFAULT_JDBC_URL_HSQL_DB = "database/ProjectForgeDB";

  private static final String DEFAULT_JDBC_URL_POSTGRESQL = "jdbc:postgresql://localhost:5432/projectforge";

  public StartSettings(final HibernateDialect hibernateDialect, final String baseDir)
  {
    TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
    System.setProperty("user.timezone", TimeZone.getDefault().getID());
    this.baseDir = baseDir;
    if (hibernateDialect == HibernateDialect.PostgreSQL) {
      dialect = "org.hibernate.dialect.PostgreSQLDialect";
      jdbcDriverClass = "org.postgresql.Driver";
      jdbcUrl = DEFAULT_JDBC_URL_POSTGRESQL;
    } else {
      dialect = "org.hibernate.dialect.HSQLDialect";
      jdbcDriverClass = "org.hsqldb.jdbcDriver";
      jdbcUrl = "jdbc:hsqldb:" + new File(baseDir).getAbsolutePath() + File.separatorChar + DEFAULT_JDBC_URL_HSQL_DB;
      jdbcUser = "sa";
    }
  }

  /**
   * The port to run ProjectForge.
   */
  public int getPort()
  {
    return port;
  }

  public StartSettings setPort(final int port)
  {
    this.port = port;
    return this;
  }

  /**
   * The base dir of ProjectForge, the directory for the config.xml, data-base files etc.
   * @return
   */
  public String getBaseDir()
  {
    return baseDir;
  }

  public StartSettings setBaseDir(final String baseDir)
  {
    this.baseDir = baseDir;
    return this;
  }

  /**
   * You should never run ProjectForge in development in productive environments. The development mode is used by Wicket and a header is
   * displayed on every web page.
   * @return the development
   */
  public boolean isDevelopment()
  {
    return development;
  }

  /**
   * @param development the development to set
   * @return this for chaining.
   */
  public StartSettings setDevelopment(final boolean development)
  {
    this.development = development;
    return this;
  }

  /**
   * @return the Hibernate dialect (of the data-base in use).
   */
  public String getDialect()
  {
    return dialect;
  }

  /**
   * If true, than any missing table or column in the data-base is created automatically on start-up by Hibernate. This feature should be
   * used only on first start of ProjectForge especially in productive environments this value should be always false.
   */
  public boolean isSchemaUpdate()
  {
    return schemaUpdate;
  }

  public StartSettings setSchemaUpdate(final boolean schemaUpdate)
  {
    this.schemaUpdate = schemaUpdate;
    return this;
  }

  /**
   * @return The jdbc url to be used for the data-base connection.
   */
  public String getJdbcUrl()
  {
    return jdbcUrl;
  }

  /**
   * @return the jdbcDriverClass to be used
   */
  public String getJdbcDriverClass()
  {
    return jdbcDriverClass;
  }

  /**
   * @param jdbcDriverClass the jdbcDriverClass to set
   * @return this for chaining.
   */
  public StartSettings setJdbcDriverClass(final String jdbcDriverClass)
  {
    this.jdbcDriverClass = jdbcDriverClass;
    return this;
  }

  public StartSettings setJdbcUrl(final String jdbcUrl)
  {
    this.jdbcUrl = jdbcUrl;
    return this;
  }

  /**
   * @return The jdbc user used for the data-base connection.
   */
  public String getJdbcUser()
  {
    return jdbcUser;
  }

  public StartSettings setJdbcUser(final String jdbcUser)
  {
    this.jdbcUser = jdbcUser;
    return this;
  }

  /**
   * @return The jdbc user password used for the data-base connection.
   */
  public String getJdbcPassword()
  {
    return jdbcPassword;
  }

  public StartSettings setJdbcPassword(final String jdbcPassword)
  {
    this.jdbcPassword = jdbcPassword;
    return this;
  }

  /**
   * This feature is useful for developing for testing the functionality of ProjectForge if cookies are disabled.
   * @return the usingCookies
   */
  public boolean isUsingCookies()
  {
    return usingCookies;
  }

  /**
   * @param usingCookies the usingCookies to set
   * @return this for chaining.
   */
  public StartSettings setUsingCookies(final boolean usingCookies)
  {
    this.usingCookies = usingCookies;
    return this;
  }

  /**
   * If true then the wicket specific html tags are removed from the HTML output. This is default for the productive mode and can be set
   * optional for the development mode for increasing the readability of the html markup. If you have to debug the Wicket output itself,
   * this value should be set to false.
   */
  public boolean isStripWicketTags()
  {
    return stripWicketTags;
  }

  public StartSettings setStripWicketTags(final boolean stripWicketTags)
  {
    this.stripWicketTags = stripWicketTags;
    return this;
  }

  /**
   * Max active data base connections to configure.
   * @return the jdbcMaxActive
   */
  public Integer getJdbcMaxActive()
  {
    return jdbcMaxActive;
  }

  /**
   * @param jdbcMaxActive the jdbcMaxActive to set
   * @return this for chaining.
   */
  public StartSettings setJdbcMaxActive(final Integer jdbcMaxActive)
  {
    this.jdbcMaxActive = jdbcMaxActive;
    return this;
  }

  /**
   * Should the browser be started with ProjectForge automatically after start-up?
   * @return the launchBrowserAfterStartup
   */
  public boolean isLaunchBrowserAfterStartup()
  {
    return launchBrowserAfterStartup;
  }

  /**
   * Should the browser be started with ProjectForge automatically after start-up?
   * @param launchBrowserAfterStartup the launchBrowserAfterStartup to set
   * @return this for chaining.
   */
  public StartSettings setLaunchBrowserAfterStartup(final boolean launchBrowserAfterStartup)
  {
    this.launchBrowserAfterStartup = launchBrowserAfterStartup;
    return this;
  }
}
