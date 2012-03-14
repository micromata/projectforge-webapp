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
import org.projectforge.database.HibernateDialect;

public class AbstractStart
{
  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(AbstractStart.class);

  private static int PORT = 8181;

  private static final String BASE_DIR = System.getProperty("user.home") + "/ProjectForge";

  private static final HibernateDialect DATABASE = HibernateDialect.PostgreSQL;

  private static final boolean SCHEMA_UPDATE = false;

  private static final String JDBC_URL_HSQL = "jdbc:hsqldb:" + new File(BASE_DIR).getAbsolutePath() + "ProjectForgeDB";

  private static final String JDBC_URL_POSTGRESQL = "jdbc:postgresql://localhost:5432/projectforge";

  private static final String JDBC_JAR = System.getProperty("user.home") + "/workspace/Java/apache-tomcat/lib/postgresql-8.3-603.jdbc3.jar";

  private static final String JDBC_USERNAME = "sa";

  private static final String JDBC_PASSWORD = "";

  private static final boolean STRIP_WICKET_TARGETS = true;

  public static void main(final String[] args) throws Exception
  {
    final int timeout = (int) DateHelper.MILLIS_HOUR;

    setProperty("base.dir", BASE_DIR);
    if (DATABASE == HibernateDialect.PostgreSQL) {
      setProperty("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect");
      setProperty("jettyEnv.driverClassName", "org.postgresql.Driver");
      setProperty("jettyEnv.jdbcUrl", JDBC_URL_POSTGRESQL);
    } else {
      setProperty("hibernate.dialect", "org.hibernate.dialect.HSQLDialect");
      setProperty("jettyEnv.driverClassName", "org.hsqldb.jdbcDriver");
      setProperty("jettyEnv.jdbcUrl", JDBC_URL_HSQL);
    }
    setProperty("hibernate.schemaUpdate", SCHEMA_UPDATE);
    setProperty("development", true);
    setProperty("stripWicketTargets", STRIP_WICKET_TARGETS);
    setProperty("jetty.home", BASE_DIR);
    setProperty("jettyEnv.jdbcUser", JDBC_USERNAME);
    setProperty("jettyEnv.jdbcPassword", JDBC_PASSWORD);

    final Server server = new Server();
    final SocketConnector connector = new SocketConnector();

    // Set some timeout options to make debugging easier.
    connector.setMaxIdleTime(timeout);
    connector.setSoLingerTime(-1);
    connector.setPort(PORT);
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
    webAppContext.setWar("target/ProjectForge/");
    webAppContext.setDescriptor("target/ProjectForge/WEB-INF/web.xml");
    webAppContext.setConfigurationClasses(CONFIGURATION_CLASSES);
    if (StringUtils.isNotBlank(JDBC_JAR) == true) {
      final File jdbcJarFile = new File(JDBC_JAR);
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

    final ClassLoader classLoader = AbstractStart.class.getClassLoader();
    final InputStream is = classLoader.getResourceAsStream("jetty.xml");
    final XmlConfiguration configuration = new XmlConfiguration(is);
    configuration.configure(server);
    try {
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
