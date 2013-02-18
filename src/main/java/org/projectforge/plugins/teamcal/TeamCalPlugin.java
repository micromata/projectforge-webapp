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

package org.projectforge.plugins.teamcal;

import org.projectforge.admin.UpdateEntry;
import org.projectforge.plugins.core.AbstractPlugin;
import org.projectforge.plugins.teamcal.admin.TeamCalDO;
import org.projectforge.plugins.teamcal.admin.TeamCalDao;
import org.projectforge.plugins.teamcal.admin.TeamCalEditPage;
import org.projectforge.plugins.teamcal.admin.TeamCalListPage;
import org.projectforge.plugins.teamcal.admin.TeamCalRight;
import org.projectforge.plugins.teamcal.event.TeamEventAttendeeDO;
import org.projectforge.plugins.teamcal.event.TeamEventDO;
import org.projectforge.plugins.teamcal.event.TeamEventDao;
import org.projectforge.plugins.teamcal.event.TeamEventEditPage;
import org.projectforge.plugins.teamcal.event.TeamEventListPage;
import org.projectforge.plugins.teamcal.event.TeamEventRight;
import org.projectforge.plugins.teamcal.integration.TeamCalCalendarFeedHook;
import org.projectforge.plugins.teamcal.integration.TeamCalCalendarPage;
import org.projectforge.plugins.teamcal.integration.TeamcalTimesheetPluginComponentHook;
import org.projectforge.registry.DaoRegistry;
import org.projectforge.registry.RegistryEntry;
import org.projectforge.web.MenuItemDef;
import org.projectforge.web.MenuItemDefId;
import org.projectforge.web.MenuItemRegistry;
import org.projectforge.web.calendar.CalendarFeed;
import org.projectforge.web.timesheet.TimesheetEditPage;
import org.projectforge.web.wicket.WicketApplication;

/**
 * @author Kai Reinhard (k.reinhard@micromata.de)
 * @author M. Lauterbach (m.lauterbach@micromata.de)
 */
public class TeamCalPlugin extends AbstractPlugin
{
  public static final String ID = "teamCal";

  public static final String RESOURCE_BUNDLE_NAME = TeamCalPlugin.class.getPackage().getName() + ".TeamCalI18nResources";

  private static final Class< ? >[] PERSISTENT_ENTITIES = new Class< ? >[] { TeamCalDO.class, TeamEventDO.class, TeamEventAttendeeDO.class};

  /**
   * This dao should be defined in pluginContext.xml (as resources) for proper initialization.
   */
  private TeamCalDao teamCalDao;

  private TeamEventDao teamEventDao;

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
    TeamCalPluginUpdates.dao = databaseUpdateDao;
    final RegistryEntry entry = new RegistryEntry(ID, TeamCalDao.class, teamCalDao, "plugins.teamcal");
    final RegistryEntry eventEntry = new RegistryEntry("teamEvent", TeamEventDao.class, teamEventDao, "plugins.teamcal.event");

    // The CalendarDao is automatically available by the scripting engine!
    register(entry);
    register(eventEntry);

    // Register the web part:
    registerWeb(ID, TeamCalListPage.class, TeamCalEditPage.class);
    registerWeb(ID, TeamCalListPage.class, TeamCalEditPage.class, DaoRegistry.ADDRESS, false); // At second position (after Address entry)
    // for SearchPage.
    registerWeb("teamEvent", TeamEventListPage.class, TeamEventEditPage.class, ID, false); // At position after entry.

    addMountPage("teamCalendar", TeamCalCalendarPage.class);
    // Register the menu entry as sub menu entry of the misc menu:
    final MenuItemDef parentMenu = getMenuItemDef(MenuItemDefId.COMMON);
    // registerMenuItem(new MenuItemDef(parentMenu, ID, 7, "plugins.teamcal.menu", TeamCalCalendarPage.class));
    registerMenuItem(new MenuItemDef(parentMenu, ID + "List", 11, "plugins.teamcal.title.list", TeamCalListPage.class));
    final MenuItemDef menuItemDef = MenuItemRegistry.instance().get(MenuItemDefId.CALENDAR);
    menuItemDef.setPageClass(TeamCalCalendarPage.class);
    WicketApplication.setDefaultPage(TeamCalCalendarPage.class);
    // .setMobileMenu(ToDoMobileListPage.class, 10));

    // Define the access management:
    registerRight(new TeamCalRight());
    registerRight(new TeamEventRight());

    // All the i18n stuff:
    addResourceBundle(RESOURCE_BUNDLE_NAME);

    CalendarFeed.registerFeedHook(new TeamCalCalendarFeedHook());

    TimesheetEditPage.addPluginHook(new TeamcalTimesheetPluginComponentHook());
  }

  /**
   * @param teamCalDao the calendarDao to set
   * @return this for chaining.
   */
  public void setTeamCalDao(final TeamCalDao teamCalDao)
  {
    this.teamCalDao = teamCalDao;
  }

  public void setTeamEventDao(final TeamEventDao teamEventDao)
  {
    this.teamEventDao = teamEventDao;
  }

  /**
   * @see org.projectforge.plugins.core.AbstractPlugin#getInitializationUpdateEntry()
   */
  @Override
  public UpdateEntry getInitializationUpdateEntry()
  {
    return TeamCalPluginUpdates.getInitializationUpdateEntry();
  }
}
