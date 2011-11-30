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

package org.projectforge.plugins.marketing;

import org.projectforge.admin.UpdateEntry;
import org.projectforge.plugins.core.AbstractPlugin;
import org.projectforge.web.MenuItemDef;
import org.projectforge.web.MenuItemDefId;

/**
 * Your plugin initialization. Register all your components such as i18n files, data-access object etc.
 * @author Kai Reinhard (k.reinhard@micromata.de)
 */
public class MarketingPlugin extends AbstractPlugin
{
  public static final String ADDRESS_CAMPAIGN_ID = "addressCampaign";

  public static final String ADDRESS_CAMPAIGN_VALUE_ID = "addressCampaignValues";

  public static final String RESOURCE_BUNDLE_NAME = MarketingPlugin.class.getPackage().getName() + ".MarketingI18nResources";

  private static final Class< ? >[] PERSISTENT_ENTITIES = new Class< ? >[] { AddressCampaignDO.class, AddressCampaignValueDO.class};

  private AddressCampaignDao campaignDao;

  private AddressCampaignValueDao addressCampaignValueDao;

  @Override
  public Class< ? >[] getPersistentEntities()
  {
    return PERSISTENT_ENTITIES;
  }

  @Override
  protected void initialize()
  {
    // DatabaseUpdateDao is needed by the updater:
    MarketingPluginUpdates.dao = databaseUpdateDao;
    // Register it:
    register(ADDRESS_CAMPAIGN_ID, AddressCampaignDao.class, campaignDao, "plugins.marketing.addressCampaign");
    register(ADDRESS_CAMPAIGN_VALUE_ID, AddressCampaignValueDao.class, addressCampaignValueDao, "plugins.marketing.addressCampaignValue");

    // Register the web part:
    registerWeb(ADDRESS_CAMPAIGN_ID, AddressCampaignListPage.class, AddressCampaignEditPage.class);
    registerWeb(ADDRESS_CAMPAIGN_VALUE_ID, AddressCampaignValueListPage.class, AddressCampaignValueEditPage.class);

    // Register the menu entry as sub menu entry of the misc menu:
    final MenuItemDef parentMenu = getMenuItemDef(MenuItemDefId.MISC);
    registerMenuItem(new MenuItemDef(parentMenu, ADDRESS_CAMPAIGN_ID, 30, "plugins.marketing.addressCampaign.menu", AddressCampaignListPage.class));
    registerMenuItem(new MenuItemDef(parentMenu, ADDRESS_CAMPAIGN_VALUE_ID, 30, "plugins.marketing.addressCampaignValue.menu", AddressCampaignValueListPage.class));

    // Define the access management:
    registerRight(new AddressCampaignRight());
    registerRight(new AddressCampaignValueRight());

    // All the i18n stuff:
    addResourceBundle(RESOURCE_BUNDLE_NAME);
  }

  public void setCampaignDao(final AddressCampaignDao campaignDao)
  {
    this.campaignDao = campaignDao;
  }

  @Override
  public UpdateEntry getInitializationUpdateEntry()
  {
    return MarketingPluginUpdates.getInitializationUpdateEntry();
  }
}
