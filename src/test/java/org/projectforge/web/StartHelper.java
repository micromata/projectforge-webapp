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
import java.io.InputStream;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.bio.SocketConnector;
import org.eclipse.jetty.webapp.WebAppContext;
import org.eclipse.jetty.xml.XmlConfiguration;
import org.projectforge.common.DateHelper;

/**
 * Helper for starting ProjectForge via Jetty web server.
 * @author Kai Reinhard (k.reinhard@micromata.de)
 * 
 */
public class StartHelper
{
  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(StartHelper.class);

  /**
   * Helper method for starting ProjectForge via Jetty web server. If any key is pressed in the console the web server will be stopped.
   * @param settings Configuration of the web server and web application.
   */
  public static void start(final StartSettings settings)
  {
    final int timeout = (int) DateHelper.MILLIS_HOUR;
    setProperty("base.dir", settings.getBaseDir());
    setProperty("hibernate.dialect", settings.getDialect());
    setProperty("jettyEnv.driverClassName", settings.getJdbcDriverClass());
    setProperty("jettyEnv.jdbcUrl", settings.getJdbcUrl());
    setProperty("hibernate.schemaUpdate", settings.isSchemaUpdate());
    setProperty("jetty.home", settings.getBaseDir());
    setProperty("jettyEnv.jdbcUser", settings.getJdbcUser());
    setProperty("jettyEnv.jdbcPassword", settings.getJdbcPassword());

    final Server server = new Server();
    final SocketConnector connector = new SocketConnector();

    // Set some timeout options to make debugging easier.
    connector.setMaxIdleTime(timeout);
    connector.setSoLingerTime(-1);
    connector.setPort(settings.getPort());
    server.addConnector(connector);

    // check if a keystore for a SSL certificate is available, and
    // if so, start a SSL connector on port 8443. By default, the
    // quickstart comes with a Apache Wicket Quickstart Certificate
    // that expires about half way september 2021. Do not use this
    // certificate anywhere important as the passwords are available
    // in the source.

    // Resource keystore = Resource.newClassPathResource("/keystore");
    // if (keystore != null && keystore.exists()) {
    // connector.setConfidentialPort(8443);
    //
    // SslContextFactory factory = new SslContextFactory();
    // factory.setKeyStoreResource(keystore);
    // factory.setKeyStorePassword("wicket");
    // factory.setTrustStore(keystore);
    // factory.setKeyManagerPassword("wicket");
    // SslSocketConnector sslConnector = new SslSocketConnector(factory);
    // sslConnector.setMaxIdleTime(timeout);
    // sslConnector.setPort(8443);
    // sslConnector.setAcceptors(4);
    // server.addConnector(sslConnector);
    //
    // System.out.println("SSL access to the quickstart has been enabled on port 8443");
    // System.out.println("You can access the application using SSL on https://localhost:8443");
    // System.out.println();
    // }

    final WebAppContext webAppContext = new WebAppContext();
    webAppContext.setServer(server);
    webAppContext.setContextPath("/ProjectForge");
    webAppContext.setWar("src/main/webapp");
    webAppContext.setDescriptor("src/main/webapp/WEB-INF/web.xml");
    webAppContext.setConfigurationClasses(CONFIGURATION_CLASSES);
    webAppContext.setExtraClasspath("target/classes");
    webAppContext.setInitParameter("development", String.valueOf(settings.isDevelopment()));
    webAppContext.setInitParameter("stripWicketTargets", String.valueOf(settings.isStripWicketTargets()));
    if (StringUtils.isNotBlank(settings.getJdbcJar()) == true) {
      final File jdbcJarFile = new File(settings.getJdbcJar());
      if (jdbcJarFile.canRead() == true) {
        // If started with other jdbc driver:
        log.info("Using jdbc jar file: " + jdbcJarFile.getAbsolutePath());
        webAppContext.setExtraClasspath(jdbcJarFile.getAbsolutePath());
      } else {
        log.error("Can't load jdbc jar file: " + jdbcJarFile.getAbsolutePath());
      }
    }

    // START JMX SERVER
    // MBeanServer mBeanServer = ManagementFactory.getPlatformMBeanServer();
    // MBeanContainer mBeanContainer = new MBeanContainer(mBeanServer);
    // server.getContainer().addEventListener(mBeanContainer);
    // mBeanContainer.start();

    server.setHandler(webAppContext);

    try {
      final ClassLoader classLoader = StartHelper.class.getClassLoader();
      final InputStream is = classLoader.getResourceAsStream("jetty.xml");
      final XmlConfiguration configuration = new XmlConfiguration(is);
      configuration.configure(server);
      System.out.println(">>> STARTING EMBEDDED JETTY SERVER, PRESS ANY KEY TO STOP");
      server.start();
      System.in.read();
      System.out.println(">>> STOPPING EMBEDDED JETTY SERVER");
      server.stop();
      server.join();
    } catch (final Exception e) {
      e.printStackTrace();
      System.exit(1);
    }
  }

  private static void setProperty(final String key, final String value)
  {
    log.info(key + "=" + value);
    System.setProperty(key, value);
  }

  private static void setProperty(final String key, final Object value)
  {
    if (value == null) {
      System.setProperty(key, null);
    } else {
      System.setProperty(key, String.valueOf(value));
    }
  }

  private static final String[] CONFIGURATION_CLASSES = { //
    org.eclipse.jetty.webapp.WebInfConfiguration.class.getName(), //
    org.eclipse.jetty.webapp.WebXmlConfiguration.class.getName(), //
    org.eclipse.jetty.webapp.MetaInfConfiguration.class.getName(), //
    org.eclipse.jetty.webapp.FragmentConfiguration.class.getName(), //
    org.eclipse.jetty.plus.webapp.EnvConfiguration.class.getName(), //
    org.eclipse.jetty.plus.webapp.PlusConfiguration.class.getName(), //
    org.eclipse.jetty.annotations.AnnotationConfiguration.class.getName(), //
    org.eclipse.jetty.webapp.JettyWebXmlConfiguration.class.getName(), //
    org.eclipse.jetty.webapp.TagLibConfiguration.class.getName()};
}
