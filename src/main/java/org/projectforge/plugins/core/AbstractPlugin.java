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

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.Validate;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.resource.loader.BundleStringResourceLoader;
import org.apache.wicket.settings.IResourceSettings;
import org.projectforge.admin.UpdateEntry;
import org.projectforge.core.BaseDO;
import org.projectforge.core.BaseDao;
import org.projectforge.database.DatabaseUpdateDao;
import org.projectforge.plugins.todo.ToDoPlugin;
import org.projectforge.registry.Registry;
import org.projectforge.registry.RegistryEntry;
import org.projectforge.user.UserPrefArea;
import org.projectforge.user.UserPrefAreaRegistry;
import org.projectforge.user.UserRight;
import org.projectforge.user.UserRights;
import org.projectforge.user.UserXmlPreferencesBaseDOSingleValueConverter;
import org.projectforge.user.UserXmlPreferencesDao;
import org.projectforge.web.MenuItemDef;
import org.projectforge.web.MenuItemDefId;
import org.projectforge.web.MenuItemRegistry;
import org.projectforge.web.registry.WebRegistry;
import org.projectforge.web.registry.WebRegistryEntry;
import org.projectforge.web.wicket.IListPageColumnsCreator;

import de.micromata.hibernate.history.Historizable;

/**
 * @author Kai Reinhard (k.reinhard@micromata.de)
 */
public abstract class AbstractPlugin
{
  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(AbstractPlugin.class);

  protected DatabaseUpdateDao databaseUpdateDao;

  private UserXmlPreferencesDao userXmlPreferencesDao;

  private IResourceSettings resourceSettings;

  private String resourceBundleName;

  private boolean initialized;

  private static Set<Class< ? >> initializedPlugins = new HashSet<Class< ? >>();

  public void setDatabaseUpdateDao(final DatabaseUpdateDao databaseUpdateDao)
  {
    this.databaseUpdateDao = databaseUpdateDao;
  }

  public void setUserXmlPreferencesDao(final UserXmlPreferencesDao userXmlPreferencesDao)
  {
    this.userXmlPreferencesDao = userXmlPreferencesDao;
  }

  public void setResourceSettings(final IResourceSettings resourceSettings)
  {
    this.resourceSettings = resourceSettings;
  }

  public String getResourceBundleName()
  {
    return resourceBundleName;
  }

  /**
   * Override this method if persistent entities should be added (JPA annotated classes which will be registered at Hibernate).
   */
  public Class< ? >[] getPersistentEntities()
  {
    return null;
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

  /**
   * Is called on initialization of the plugin by the method {@link #init()}.
   */
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
   * @param resourceBundleName
   * @return this for chaining.
   */
  protected AbstractPlugin addResourceBundle(final String resourceBundleName)
  {
    this.resourceBundleName = resourceBundleName;
    resourceSettings.getStringResourceLoaders().add(new BundleStringResourceLoader(resourceBundleName));
    return this;
  }

  /**
   * @param id The unique plugin id.
   * @param daoClassType The dao object type.
   * @param baseDao The dao itself.
   * @param i18nPrefix The prefix for i18n keys.
   * @return New RegistryEntry.
   */
  protected RegistryEntry register(final String id, final Class< ? extends BaseDao< ? >> daoClassType, final BaseDao< ? > baseDao,
      final String i18nPrefix)
  {
    if (baseDao == null) {
      throw new IllegalArgumentException(
          id
          + ": Dao object is null. May-be the developer forgots to initialize it in pluginContext.xml or the setter method is not given in the main plugin class!");
    }
    final RegistryEntry entry = new RegistryEntry(id, daoClassType, baseDao, i18nPrefix);
    register(entry);
    return entry;
  }

  /**
   * Registers the given entry.
   * @param entry
   * @return The registered registry entry for chaining.
   * @see Registry#register(RegistryEntry)
   */
  protected RegistryEntry register(final RegistryEntry entry)
  {
    Validate.notNull(entry);
    Registry.instance().register(entry);
    return entry;
  }

  /**
   * Use this method if your entities don't support the general search page (e. g. if you have no data-base entities which implements
   * {@link Historizable}).
   * @param id
   * @return this for chaining.
   * @see WebRegistry#register(String)
   */
  protected AbstractPlugin registerWeb(final String id)
  {
    WebRegistry.instance().register(id);
    return this;
  }

  /**
   * 
   * @param id
   * @param existingEntryId
   * @param insertBefore
   * @return this for chaining.
   * @see WebRegistry#register(WebRegistryEntry, boolean, WebRegistryEntry)
   */
  protected AbstractPlugin registerWeb(final String id, final String existingEntryId, final boolean insertBefore)
  {
    final WebRegistryEntry existingEntry = WebRegistry.instance().getEntry(id);
    WebRegistry.instance().register(existingEntry, insertBefore, new WebRegistryEntry(id));
    return this;
  }

  /**
   * @param id
   * @param pageListClass list page to mount. Needed for displaying the result-sets by the general search page if the list page implements
   *          {@link IListPageColumnsCreator}.
   * @param pageEditClass edit page to mount.
   * @return this for chaining.
   * @see WebRegistry#register(String, Class)
   * @see WebRegistry#addMountPages(String, Class, Class)
   */
  @SuppressWarnings("unchecked")
  protected AbstractPlugin registerWeb(final String id, final Class< ? extends WebPage> pageListClass,
      final Class< ? extends WebPage> pageEditClass)
  {
    registerWeb(id, pageEditClass, pageEditClass, null, false);
    return this;
  }

  /**
   * @param id
   * @param pageListClass list page to mount. Needed for displaying the result-sets by the general search page if the list page implements
   *          {@link IListPageColumnsCreator}.
   * @param pageEditClass edit page to mount.
   * @return this for chaining.
   * @see WebRegistry#register(String, Class)
   * @see WebRegistry#addMountPages(String, Class, Class)
   */
  @SuppressWarnings("unchecked")
  protected AbstractPlugin registerWeb(final String id, final Class< ? extends WebPage> pageListClass,
      final Class< ? extends WebPage> pageEditClass, final String existingEntryId, final boolean insertBefore)
  {
    WebRegistryEntry entry;
    if (IListPageColumnsCreator.class.isAssignableFrom(pageListClass) == true) {
      entry = new WebRegistryEntry(id, (Class< ? extends IListPageColumnsCreator< ? >>) pageListClass);
    } else {
      entry = new WebRegistryEntry(id);
    }
    if (existingEntryId != null) {
      final WebRegistryEntry existingEntry = WebRegistry.instance().getEntry(existingEntryId);
      WebRegistry.instance().register(existingEntry, insertBefore, entry);
    } else {
      WebRegistry.instance().register(entry);
    }
    WebRegistry.instance().addMountPages(id, pageListClass, pageEditClass);
    return this;
  }

  /**
   * @param mountPage
   * @param pageClass
   * @return this for chaining.
   * @see WebRegistry#addMountPages(String, Class)
   */
  protected AbstractPlugin addMountPage(final String mountPage, final Class< ? extends WebPage> pageClass)
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

  /**
   * Registers a right which is responsible for the access management.
   * @param right
   * @return this for chaining.
   */
  protected AbstractPlugin registerRight(final UserRight right)
  {
    UserRights.instance().addRight(right);
    return this;
  }

  /**
   * Registers a new user preferences areas (shown in the list of 'own settings' of each user).
   * @param areaId
   * @param cls
   * @param i18nSuffix
   * @return Created and registered UserPrefArea.
   * @see UserPrefArea#UserPrefArea(String, Class, String)
   */
  protected UserPrefArea registerUserPrefArea(final String areaId, final Class< ? > cls, final String i18nSuffix)
  {
    final UserPrefArea userPrefArea = new UserPrefArea(areaId, cls, i18nSuffix);
    UserPrefAreaRegistry.instance().register(userPrefArea);
    return userPrefArea;
  }

  /**
   * The annotations of the given classes will be processed by xstream which is used for marshalling and unmarshalling user xml preferences.
   * @param classes
   * @return this for chaining.
   * @see UserXmlPreferencesDao#processAnnotations(Class...)
   */
  protected AbstractPlugin processUserXmlPreferencesAnnotations(final Class< ? >... classes)
  {
    userXmlPreferencesDao.processAnnotations(classes);
    return this;
  }

  /**
   * Register converters before marshaling and unmarshaling by XStream.
   * @param daoClass Class of the dao.
   * @param doClass Class of the DO which will be converted.
   * @see UserXmlPreferencesBaseDOSingleValueConverter#UserXmlPreferencesBaseDOSingleValueConverter(Class, Class)
   */
  public void registerUserXmlPreferencesConverter(final Class< ? extends BaseDao< ? >> daoClass, final Class< ? extends BaseDO< ? >> doClass)
  {
    userXmlPreferencesDao.registerConverter(daoClass, doClass, 10);
  }

  /**
   * Register converters before marshaling and unmarshaling by XStream.
   * @param daoClass Class of the dao.
   * @param doClass Class of the DO which will be converted.
   * @param priority The priority needed by xtream for using converters in the demanded order.
   * @see UserXmlPreferencesBaseDOSingleValueConverter#UserXmlPreferencesBaseDOSingleValueConverter(Class, Class)
   */
  public void registerUserXmlPreferencesConverter(final Class< ? extends BaseDao< ? >> daoClass,
      final Class< ? extends BaseDO< ? >> doClass, final int priority)
  {
    userXmlPreferencesDao.registerConverter(daoClass, doClass, priority);
  }

  /**
   * Override this method if an update entry for initialization does exist. This will be called, if the plugin runs the first time.
   * @return null at default.
   * @see ToDoPlugin
   */
  public UpdateEntry getInitializationUpdateEntry()
  {
    return null;
  }

  /**
   * Override this method if update entries does exist for this plugin.
   * @return null at default.
   */
  public List<UpdateEntry> getUpdateEntries()
  {
    return null;
  }
}
