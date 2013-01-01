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

package org.projectforge.plugins.poll;

import org.projectforge.admin.UpdateEntry;
import org.projectforge.plugins.core.AbstractPlugin;
import org.projectforge.plugins.poll.attendee.PollAttendeeDO;
import org.projectforge.plugins.poll.attendee.PollAttendeeDao;
import org.projectforge.plugins.poll.attendee.PollAttendeePage;
import org.projectforge.plugins.poll.attendee.PollAttendeeRight;
import org.projectforge.plugins.poll.event.PollEventDO;
import org.projectforge.plugins.poll.event.PollEventDao;
import org.projectforge.plugins.poll.event.PollEventEditPage;
import org.projectforge.plugins.poll.event.PollEventRight;
import org.projectforge.plugins.poll.result.PollResultDO;
import org.projectforge.plugins.poll.result.PollResultDao;
import org.projectforge.plugins.poll.result.PollResultPage;
import org.projectforge.registry.RegistryEntry;
import org.projectforge.user.UserPrefArea;
import org.projectforge.web.MenuItemDef;
import org.projectforge.web.MenuItemDefId;

/**
 * @author M. Lauterbach (m.lauterbach@micromata.de)
 * 
 */
public class PollPlugin extends AbstractPlugin
{
  public static final String ID = "poll";

  public static final String RESOURCE_BUNDLE_NAME = PollPlugin.class.getPackage().getName() + ".PollI18nResources";

  static UserPrefArea USER_PREF_AREA;

  private static final Class< ? >[] PERSISTENT_ENTITIES = new Class< ? >[] { PollDO.class, PollEventDO.class, PollAttendeeDO.class,
    PollResultDO.class};

  /**
   * This dao should be defined in pluginContext.xml (as resources) for proper initialization.
   */
  private PollDao pollDao;

  private PollEventDao pollEventDao;

  private PollAttendeeDao pollAttendeeDao;

  private PollResultDao pollResultDao;

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
    PollPluginUpdates.dao = databaseUpdateDao;
    final RegistryEntry entry = new RegistryEntry(ID, PollDao.class, pollDao, "plugins.poll");
    final RegistryEntry eventEntry = new RegistryEntry("pollEvent", PollEventDao.class, pollEventDao, "plugins.poll");
    final RegistryEntry attendeeEntry = new RegistryEntry("pollAttendee", PollAttendeeDao.class, pollAttendeeDao, "plugins.poll");
    final RegistryEntry resultEntry = new RegistryEntry("pollResult", PollResultDao.class, pollResultDao, "plugins.poll");

    // The CalendarDao is automatically available by the scripting engine!
    register(entry);
    register(eventEntry);
    register(attendeeEntry);
    register(resultEntry);

    // Register the web part:
    registerWeb("poll", PollListPage.class, PollEditPage.class);

    addMountPage("newPoll", NewPollPage.class);
    addMountPage("pollEvent", PollEventEditPage.class);
    addMountPage("pollAttendees", PollAttendeePage.class);
    addMountPage("pollResult", PollResultPage.class);
    addMountPage("newPollOverview", NewPollOverviewPage.class);
    // Register the menu entry as sub menu entry of the misc menu:
    final MenuItemDef parentMenu = getMenuItemDef(MenuItemDefId.MISC);
    registerMenuItem(new MenuItemDef(parentMenu, ID, 30, "plugins.poll.menu", PollListPage.class));
    // registerMenuItem(new MenuItemDef(parentMenu, ID + "List", 8, "plugins.teamcal.title.list", TeamCalListPage.class));

    // Define the access management:
    registerRight(new PollRight());
    registerRight(new PollAttendeeRight());
    registerRight(new PollEventRight());

    // All the i18n stuff:
    addResourceBundle(RESOURCE_BUNDLE_NAME);

    // Register favorite entries (the user can modify these templates/favorites via 'own settings'):
    USER_PREF_AREA = registerUserPrefArea("POLL_FAVORITE", PollDO.class, "poll.favorite");
    // CalendarFeed.registerFeedHook(new TeamCalCalendarFeedHook());
  }

  /**
   * @param pollDao the calendarDao to set
   * @return this for chaining.
   */
  public void setPollDao(final PollDao pollDao)
  {
    this.pollDao = pollDao;
  }

  public void setPollEventDao(final PollEventDao pollEventDao)
  {
    this.pollEventDao = pollEventDao;
  }

  public void setPollAttendeeDao(final PollAttendeeDao pollAttendeeDao)
  {
    this.pollAttendeeDao = pollAttendeeDao;
  }

  public void setPollResultDao(final PollResultDao pollResultDao)
  {
    this.pollResultDao = pollResultDao;
  }

  /**
   * @see org.projectforge.plugins.core.AbstractPlugin#getInitializationUpdateEntry()
   */
  @Override
  public UpdateEntry getInitializationUpdateEntry()
  {
    return PollPluginUpdates.getInitializationUpdateEntry();
  }
}
