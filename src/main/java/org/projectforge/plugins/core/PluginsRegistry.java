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

package org.projectforge.plugins.core;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.settings.IResourceSettings;
import org.projectforge.admin.SystemUpdater;
import org.projectforge.core.ConfigXml;
import org.projectforge.plugins.memo.MemoPlugin;
import org.projectforge.plugins.todo.ToDoPlugin;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;

/**
 * @author Kai Reinhard (k.reinhard@micromata.de)
 */
public class PluginsRegistry
{
  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(PluginsRegistry.class);

  /**
   * Defines some built-in plugins. Don't forget to add the pluginContext.xml files to TestConfiguration.
   */
  private final AbstractPlugin[] builtinPlugins = new AbstractPlugin[] { new ToDoPlugin(), new MemoPlugin()};

  private static PluginsRegistry instance;

  private IResourceSettings resourceSettings;

  private ConfigurableListableBeanFactory beanFactory;

  private final List<AbstractPlugin> plugins = new ArrayList<AbstractPlugin>();

  public synchronized static PluginsRegistry instance()
  {
    if (instance == null) {
      instance = new PluginsRegistry();
    }
    return instance;
  }

  public void register(final AbstractPlugin plugin)
  {
    plugins.add(plugin);
  }

  public List<AbstractPlugin> getPlugins()
  {
    return plugins;
  }

  public PluginsRegistry()
  {
  }

  public void set(final SystemUpdater systemUpdater)
  {
    for (final AbstractPlugin plugin : plugins) {
      systemUpdater.register(plugin.getInitializationUpdateEntry());
      systemUpdater.register(plugin.getUpdateEntries());
    }
  }

  public void set(final ConfigurableListableBeanFactory beanFactory, final IResourceSettings resourceSettings)
  {
    this.beanFactory = beanFactory;
    this.resourceSettings = resourceSettings;
  }

  /**
   * Load built-in plugins and plugins which are configured in config.xml.
   */
  public void loadPlugins() {
    for (final AbstractPlugin plugin : builtinPlugins) {
      plugins.add(plugin);
    }
    final ConfigXml xmlConfiguration = ConfigXml.getInstance();
    final String[] pluginMainClasses = xmlConfiguration.getPluginMainClasses();
    if (pluginMainClasses != null) {
      for (final String pluginMainClassName : pluginMainClasses) {
        try {
          final Class< ? > pluginMainClass = Class.forName(pluginMainClassName);
          try {
            final AbstractPlugin plugin = (AbstractPlugin) pluginMainClass.newInstance();
            plugins.add(plugin);
          } catch (final ClassCastException ex) {
            log.error("Couldn't load plugin, class '" + pluginMainClassName + "' isn't of type AbstractPlugin.");
          } catch (final InstantiationException ex) {
            log.error("Couldn't load plugin, class '" + pluginMainClassName + "' can't be instantiated: " + ex);
          } catch (final IllegalAccessException ex) {
            log.error("Couldn't load plugin, class '" + pluginMainClassName + "' can't be instantiated: " + ex);
          }
        } catch (final ClassNotFoundException ex) {
          log.error("Couldn't load plugin, class '" + pluginMainClassName + "' not found");
        }
      }
    }
  }

  public void initialize()
  {
    for (final AbstractPlugin plugin : plugins) {
      plugin.setResourceSettings(resourceSettings);
      beanFactory.autowireBeanProperties(plugin, AutowireCapableBeanFactory.AUTOWIRE_BY_NAME, false);
      plugin.init();
    }
  }
}
