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

package org.projectforge.test;

import java.io.File;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.Validate;
import org.apache.log4j.Logger;
import org.projectforge.common.StringHelper;
import org.projectforge.core.ConfigXmlTest;
import org.projectforge.core.Configuration;
import org.projectforge.database.HibernateUtils;
import org.projectforge.jdbc.PropertyDataSource;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.orm.hibernate3.LocalSessionFactoryBean;

/**
 * Only needed for test configuration
 */
public class TestConfiguration
{
  private static final String WORK_DIR = "work";

  private static final Logger log = Logger.getLogger(TestConfiguration.class);

  private static final String[] TEST_CONTEXT_FILES = new String[] { "test-applicationContext-main.xml", "applicationContext-hibernate.xml",
    "applicationContext-business.xml", "applicationContext-web.xml", "applicationContext-ldap.xml",
    "org/projectforge/plugins/memo/pluginContext.xml", "org/projectforge/plugins/todo/pluginContext.xml"};

  private static final String[] CMD_CONTEXT_FILES = new String[] { "cmd-applicationContext-main.xml", "applicationContext-hibernate.xml",
    "applicationContext-business.xml", "applicationContext-ldap.xml"};

  protected ClassPathXmlApplicationContext ctx = null;

  protected String[] contextFiles;

  protected String databaseUrl;

  protected boolean isInitialized = false;

  public static TestConfiguration testConfiguration = null;

  public static synchronized void initAsTestConfiguration()
  {
    initAsTestConfiguration((String[]) null);
  }

  public static synchronized void initAsTestConfiguration(final String... additionalContextFiles)
  {
    System.setProperty("base.dir", new File("./tmp").getAbsoluteFile().toString());
    ConfigXmlTest.createTestConfiguration();
    final String[] contextFiles;
    if (additionalContextFiles != null && additionalContextFiles.length > 0) {
      contextFiles = (String[]) ArrayUtils.addAll(TEST_CONTEXT_FILES, additionalContextFiles);
    } else {
      contextFiles = TEST_CONTEXT_FILES;
    }
    init(contextFiles);
  }

  public static synchronized void initAsCmdLineConfiguration()
  {
    init(CMD_CONTEXT_FILES);
  }

  /**
   * Get the file from the working directory. If the working directory doesn't exist then it'll be created.
   * @param filename
   * @return
   */
  public static File getWorkFile(final String filename)
  {
    final File workDir = new File(WORK_DIR);
    if (workDir.exists() == false) {
      log.info("Create working directory: " + workDir.getAbsolutePath());
      workDir.mkdir();
    }
    return new File(workDir, filename);
  }

  private static synchronized void init(final String[] contextFiles)
  {
    if (testConfiguration == null) {
      testConfiguration = new TestConfiguration(contextFiles);
    } else if (ArrayUtils.isEquals(testConfiguration.contextFiles, contextFiles) == false) {
      final String msg = "Already initialized with incompatible context files: "
          + StringHelper.listToString(", ",
              testConfiguration.contextFiles
              + ". New context files: "
              + StringHelper.listToString(",", contextFiles)
              + ". This is OK if the test case is started in fork-mode.");
      log.warn(msg);
      testConfiguration = new TestConfiguration(contextFiles);
    }
  }

  public static TestConfiguration getConfiguration()
  {
    if (testConfiguration == null) {
      final String msg = "Not initialized.";
      log.fatal(msg);
      throw new RuntimeException(msg);
    }
    return testConfiguration;
  }

  public ConfigurableListableBeanFactory getBeanFactory()
  {
    return ctx.getBeanFactory();
  }

  public boolean isInitialized()
  {
    return isInitialized;
  }

  public void setInitialized(final boolean isInitialized)
  {
    this.isInitialized = isInitialized;
  }

  public ClassPathXmlApplicationContext getApplicationContext()
  {
    return this.ctx;
  }

  public <T> T getBean(final String name, final Class<T> requiredType)
  {
    final T obj = ctx.getBean(name, requiredType);
    return obj;
  }

  public <T> T getAndAutowireBean(final String name, final Class<T> requiredType)
  {
    final T obj = getBean(name, requiredType);
    autowire(obj, AutowireCapableBeanFactory.AUTOWIRE_BY_NAME);
    return obj;
  }

  public String getDatabaseUrl()
  {
    return databaseUrl;
  }

  /**
   * Convenience. Bean-Properties "spring-like" setzen.
   * @param existingBean .
   * @param autowireMode Analog <code>AutowireCapableBeanFactory.AUTOWIRE_BY_NAME</code> etc.
   */
  public void autowire(final Object existingBean, final int autowireMode)
  {
    Validate.notNull(existingBean, "Bean to wire is null");
    ctx.getBeanFactory().autowireBeanProperties(existingBean, autowireMode, false);
  }

  /**
   * Init and reinitialise context for each run
   */
  protected TestConfiguration(final String[] contextFiles) throws BeansException
  {
    this.contextFiles = contextFiles;
    initCtx();
  }

  /**
   * Init and reinitialise context for each run
   */
  protected void initCtx() throws BeansException
  {
    if (ctx == null) {
      log.info("Initializing context: " + org.projectforge.common.StringHelper.listToString(", ", contextFiles));
      try {
        // Build spring context
        ctx = new ClassPathXmlApplicationContext(contextFiles);
        ctx.getBeanFactory().autowireBeanProperties(this, AutowireCapableBeanFactory.AUTOWIRE_BY_NAME, false);

        final PropertyDataSource ds = ctx.getBean("dataSource", PropertyDataSource.class);
        this.databaseUrl = ds.getUrl();
        final JdbcTemplate jdbc = new JdbcTemplate(ds);
        try {
          jdbc.execute("CHECKPOINT DEFRAG");
        } catch (final org.springframework.jdbc.BadSqlGrammarException ex) {
          // ignore
        }
        final LocalSessionFactoryBean localSessionFactoryBean = (LocalSessionFactoryBean) ctx.getBean("&sessionFactory");
        HibernateUtils.setConfiguration(localSessionFactoryBean.getConfiguration());
      } catch (final Throwable ex) {
        log.error(ex.getMessage(), ex);
        throw new RuntimeException(ex);
      }
    } else {
      // Get a new HibernateTemplate each time
      ctx.getBeanFactory().autowireBeanProperties(this, AutowireCapableBeanFactory.AUTOWIRE_BY_NAME, false);
    }
    final Configuration cfg = ctx.getBean("configuration", Configuration.class);
    cfg.setBeanFactory(ctx.getBeanFactory()); // Bean factory need to be set.
  }
}
