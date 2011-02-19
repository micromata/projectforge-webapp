/////////////////////////////////////////////////////////////////////////////
//
// Project ProjectForge Community Edition
//         www.projectforge.org
//
// Copyright (C) 2001-2011 Kai Reinhard (k.reinhard@me.com)
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

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang.Validate;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.resource.loader.BundleStringResourceLoader;
import org.apache.wicket.settings.IResourceSettings;
import org.hibernate.cfg.AnnotationConfiguration;
import org.projectforge.core.BaseDao;
import org.projectforge.plugins.todo.ToDoDO;
import org.projectforge.registry.Registry;
import org.projectforge.registry.RegistryEntry;
import org.projectforge.web.MenuItemDef;
import org.projectforge.web.MenuItemDefId;
import org.projectforge.web.MenuItemRegistry;
import org.projectforge.web.registry.WebRegistry;
import org.projectforge.web.wicket.IListPageColumnsCreator;

/**
 * @author Kai Reinhard (k.reinhard@micromata.de)
 */
public abstract class AbstractPlugin
{
  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(AbstractPlugin.class);

  private AnnotationConfiguration annotationConfiguration;

  private IResourceSettings resourceSettings;

  private boolean initialized;
  
  private static Set<Class<?>> initializedPlugins = new HashSet<Class<?>>();

  public void setAnnotationConfiguration(final AnnotationConfiguration annotationConfiguration)
  {
    this.annotationConfiguration = annotationConfiguration;
  }

  public void setResourceSettings(final IResourceSettings resourceSettings)
  {
    this.resourceSettings = resourceSettings;
  }

  public final void init()
  {
    synchronized (initializedPlugins) {
      if (initializedPlugins.contains(this.getClass()) == true || initialized == true) {
        log.warn("Ignoring multiple initialization of plugin.");
        return;
      }
      initialized = true;
      initializedPlugins.add(this.getClass());
      log.info("Initializing plugin: " + getClass());
      initialize();
    }
  }

  protected abstract void initialize();

  protected MenuItemDef getMenuItemDef(final MenuItemDefId menuItemDefId)
  {
    return MenuItemRegistry.instance().get(menuItemDefId);
  }

  protected void getMenuItemDef(final String id)
  {
    MenuItemRegistry.instance().get(id);
  }

  protected void registerMenuItem(final MenuItemDef menuItemDef)
  {
    MenuItemRegistry.instance().register(menuItemDef);
  }

  /**
   * 
   * @param resourceBundle
   * @return
   */
  protected AbstractPlugin addResourceBundle(final String resourceBundle)
  {
    resourceSettings.addStringResourceLoader(0, new BundleStringResourceLoader(resourceBundle));
    return this;
  }

  /**
   * Register dao and i18nPrefix for i18n.
   * @param id
   * @param daoClass
   * @param dao
   * @param i18nPrefix
   * @return this for chaining.
   */
  protected AbstractPlugin register(final String id, final Class< ? extends BaseDao< ? >> daoClass, final BaseDao< ? > dao,
      final String i18nPrefix)
  {
    Validate.notNull(daoClass);
    Validate.notNull(dao);
    Registry.instance().register(new RegistryEntry(id, daoClass, dao, i18nPrefix));
    return this;
  }

  /**
   * Register given class at the hibernate container.
   * @param doClass Data object which is JPA annotated.
   * @return this for chaining.
   */
  protected AbstractPlugin registerDataObject(final Class< ? > doClass)
  {
    annotationConfiguration.addAnnotatedClass(ToDoDO.class);
    return this;
  }

  /**
   * @param id
   * @param listPageColumnsCreatorClass
   * @return this for chaining.
   * @see WebRegistry#register(String, Class)
   */
  protected AbstractPlugin registerListPageColumnsCreator(final String id,
      final Class< ? extends IListPageColumnsCreator< ? >> listPageColumnsCreatorClass)
  {
    WebRegistry.instance().register(id, listPageColumnsCreatorClass);
    return this;
  }

  /**
   * @param mountPage
   * @param pageClass
   * @return this for chaining.
   * @see WebRegistry#addMountPages(String, Class)
   */
  protected AbstractPlugin addMountPages(final String mountPage, final Class< ? extends WebPage> pageClass)
  {
    WebRegistry.instance().addMountPage(mountPage, pageClass);
    return this;
  }

  /**
   * @param mountPageBasename
   * @param pageListClass
   * @param pageEditClass
   * @return this for chaining.
   * @see WebRegistry#addMountPages(String, Class, Class)
   */
  protected AbstractPlugin addMountPages(final String mountPageBasename, final Class< ? extends WebPage> pageListClass,
      final Class< ? extends WebPage> pageEditClass)
  {
    WebRegistry.instance().addMountPages(mountPageBasename, pageListClass, pageEditClass);
    return this;
  }
}
