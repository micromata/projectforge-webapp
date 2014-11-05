/////////////////////////////////////////////////////////////////////////////
//
// Project ProjectForge Community Edition
//         www.projectforge.org
//
// Copyright (C) 2001-2014 Kai Reinhard (k.reinhard@micromata.de)
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

package org.projectforge.database;

import java.io.IOException;
import java.lang.annotation.ElementType;

import org.hibernate.search.annotations.Analyze;
import org.hibernate.search.annotations.Resolution;
import org.hibernate.search.annotations.Store;
import org.hibernate.search.cfg.SearchMapping;
import org.projectforge.core.ConfigXml;
import org.projectforge.plugins.core.AbstractPlugin;
import org.projectforge.plugins.core.PluginsRegistry;
import org.springframework.orm.hibernate4.LocalSessionFactoryBean;

import de.micromata.hibernate.history.HistoryEntry;
import de.micromata.hibernate.history.delta.PropertyDelta;

/**
 * @author Kai Reinhard (k.reinhard@micromata.de)
 */
// @Configuration
// @EnableTransactionManagement
// @PropertySource({ "classpath:persistence-mysql.properties" })
// @ComponentScan({ "org.baeldung.spring.persistence" })
public class PersistenceConfig extends LocalSessionFactoryBean
{
  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(PersistenceConfig.class);

  // @Autowired
  // private Environment env;

  private boolean schemaUpdate;

  /**
   * @see org.springframework.orm.hibernate4.LocalSessionFactoryBean#afterPropertiesSet()
   */
  @Override
  public void afterPropertiesSet() throws IOException
  {
    super.afterPropertiesSet();
    final org.hibernate.cfg.Configuration config = getConfiguration();
    config.setProperty("hibernate.hbm2ddl.auto", String.valueOf(this.schemaUpdate));
    // sessionFactory.setDataSource(restDataSource());
    // sessionFactory.setPackagesToScan(new String[] { "org.baeldung.spring.persistence.model"});
    for (final Class< ? > entityClass : HibernateEntities.CORE_ENTITIES) {
      log.debug("Adding class " + entityClass.getName());
      config.addAnnotatedClass(entityClass);
    }
    for (final Class< ? > entityClass : HibernateEntities.HISTORY_ENTITIES) {
      log.debug("Adding class " + entityClass.getName());
      config.addAnnotatedClass(entityClass);
    }
    final PluginsRegistry pluginsRegistry = PluginsRegistry.instance();
    pluginsRegistry.loadPlugins();
    for (final AbstractPlugin plugin : pluginsRegistry.getPlugins()) {
      final Class< ? >[] persistentEntities = plugin.getPersistentEntities();
      if (persistentEntities != null) {
        for (final Class< ? > entity : persistentEntities) {
          log.debug("Adding class " + entity.getName());
          config.addAnnotatedClass(entity);
          HibernateEntities.instance().addEntity(entity);
        }
      }
    }

    // Add the hibernate history entities programmatically:
    final SearchMapping mapping = new SearchMapping();
    mapping.entity(HistoryEntry.class).indexed() //
    .property("id", ElementType.METHOD).documentId().name("id")//
    .property("userName", ElementType.METHOD).field().analyze(Analyze.NO).store(Store.NO) //
    // Must be tokenized for using lower case (MultiFieldQueryParser uses lower case strings):
    .property("className", ElementType.METHOD).field().store(Store.NO) //
    .property("timestamp", ElementType.METHOD).field().store(Store.NO).dateBridge(Resolution.MINUTE) //
    // Needed in BaseDao for FullTextQuery.setProjection("entityId"):
    .property("entityId", ElementType.METHOD).field().store(Store.YES) //
    .property("delta", ElementType.METHOD).indexEmbedded() //
    // PropertyDelta:
    .entity(PropertyDelta.class) //
    .property("id", ElementType.METHOD).documentId().name("id")//
    .property("oldValue", ElementType.METHOD).field().store(Store.NO) //
    .property("newValue", ElementType.METHOD).field().store(Store.NO); //
    config.getProperties().put("hibernate.search.model_mapping", mapping);
  }

  // @Bean
  // public DataSource restDataSource()
  // {
  // final BasicDataSource dataSource = new BasicDataSource();
  // dataSource.setDriverClassName(env.getProperty("jdbc.driverClassName"));
  // dataSource.setUrl(env.getProperty("jdbc.url"));
  // dataSource.setUsername(env.getProperty("jdbc.user"));
  // dataSource.setPassword(env.getProperty("jdbc.pass"));
  //
  // return dataSource;
  // }

  // @Bean
  // @Autowired
  // public HibernateTransactionManager transactionManager(final SessionFactory sessionFactory)
  // {
  // final HibernateTransactionManager txManager = new HibernateTransactionManager();
  // txManager.setSessionFactory(sessionFactory);
  //
  // return txManager;
  // }

  // @Bean
  // public PersistenceExceptionTranslationPostProcessor exceptionTranslation()
  // {
  // return new PersistenceExceptionTranslationPostProcessor();
  // }

  // @Bean
  // @SuppressWarnings("serial")
  // Properties hibernateProperties()
  // {
  // return new Properties() {
  // {
  // setProperty("hibernate.hbm2ddl.auto", env.getProperty("hibernate.hbm2ddl.auto"));
  // setProperty("hibernate.dialect", env.getProperty("hibernate.dialect"));
  // setProperty("hibernate.globally_quoted_identifiers", "true");
  // }
  // };
  // }

  public void setSchemaUpdate(final boolean schemaUpdate)
  {
    this.schemaUpdate = schemaUpdate;
  }

  /**
   * Needed for ensuring that configuration is initialized.
   * @param configXml
   */
  // @Bean
  // @Autowired
  public void setConfigXml(final ConfigXml configXml)
  {
    // Do nothing. Ensure only that configuration is initialized.
  }
}