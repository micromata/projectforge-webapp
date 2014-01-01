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

package org.projectforge.plugins.liquidityplanning;

import org.projectforge.continuousdb.UpdateEntry;
import org.projectforge.plugins.core.AbstractPlugin;
import org.projectforge.registry.DaoRegistry;
import org.projectforge.registry.RegistryEntry;
import org.projectforge.user.UserPrefArea;
import org.projectforge.user.UserRightValue;
import org.projectforge.web.MenuItemDef;
import org.projectforge.web.MenuItemDefId;

/**
 * @author Kai Reinhard (k.reinhard@micromata.de)
 */
public class LiquidityPlanningPlugin extends AbstractPlugin
{
  public static final String ID = "liquididityplanning";

  public static final String RESOURCE_BUNDLE_NAME = LiquidityPlanningPlugin.class.getPackage().getName()
      + ".LiquidityPlanningI18nResources";

  static UserPrefArea USER_PREF_AREA;

  private static final Class< ? >[] PERSISTENT_ENTITIES = new Class< ? >[] { LiquidityEntryDO.class};

  /**
   * This dao should be defined in pluginContext.xml (as resources) for proper initialization.
   */
  private LiquidityEntryDao liquidityEntryDao;

  @Override
  public Class< ? >[] getPersistentEntities()
  {
    return PERSISTENT_ENTITIES;
  }

  /**
   * @see org.projectforge.plugins.core.AbstractPlugin#initialize()
   */
  @Override
  protected void initialize()
  {
    // DatabaseUpdateDao is needed by the updater:
    LiquidityPlanningPluginUpdates.dao = getDatabaseUpdateDao();
    final RegistryEntry entry = new RegistryEntry(ID, LiquidityEntryDao.class, liquidityEntryDao, "plugins.liquidityplanning");
    register(entry);

    // Register the web part:
    // Insert at first position before accounting-record entry (for SearchPage).
    registerWeb(ID, LiquidityEntryListPage.class, LiquidityEntryEditPage.class, DaoRegistry.ACCOUNTING_RECORD, true);

    addMountPage("liquidityForecast", LiquidityForecastPage.class);

    // Register the menu entry as sub menu entry of the reporting menu:
    final MenuItemDef parentMenu = getMenuItemDef(MenuItemDefId.REPORTING);
    registerMenuItem(new MenuItemDef(parentMenu, ID, 10, "plugins.liquidityplanning.menu", LiquidityEntryListPage.class,
        LiquidityEntryDao.USER_RIGHT_ID, UserRightValue.READONLY, UserRightValue.READWRITE));

    // Define the access management:
    registerRight(new LiquidityPlanningRight());

    // All the i18n stuff:
    addResourceBundle(RESOURCE_BUNDLE_NAME);
  }

  public void setLiquidityEntryDao(final LiquidityEntryDao liquidityEntryDao)
  {
    this.liquidityEntryDao = liquidityEntryDao;
  }

  /**
   * @see org.projectforge.plugins.core.AbstractPlugin#getInitializationUpdateEntry()
   */
  @Override
  public UpdateEntry getInitializationUpdateEntry()
  {
    return LiquidityPlanningPluginUpdates.getInitializationUpdateEntry();
  }
}
