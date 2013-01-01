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

package org.projectforge.plugins.skillmatrix;

import org.projectforge.admin.UpdateEntry;
import org.projectforge.plugins.core.AbstractPlugin;
import org.projectforge.user.UserPrefArea;
import org.projectforge.web.MenuItemDef;
import org.projectforge.web.MenuItemDefId;

/**
 * @author Kai Reinhard (k.reinhard@micromata.de)
 */
public class SkillMatrixPlugin extends AbstractPlugin
{
  public static final String ID_SKILL_RATING = "skillRating";
  public static final String ID_SKILL = "skill";

  public static final String RESOURCE_BUNDLE_NAME = SkillMatrixPlugin.class.getPackage().getName() + ".SkillMatrixI18nResources";

  static UserPrefArea USER_PREF_AREA;

  private static final Class< ? >[] PERSISTENT_ENTITIES = new Class< ? >[] { SkillDO.class, SkillRatingDO.class };

  /**
   * This dao should be defined in pluginContext.xml (as resources) for proper initialization.
   */
  private SkillDao skillDao;

  private SkillRatingDao skillRatingDao;

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
    SkillMatrixPluginUpdates.dao = databaseUpdateDao;
    register(ID_SKILL_RATING, SkillRatingDao.class, skillRatingDao, "plugins.skillmatrix");
    register(ID_SKILL, SkillDao.class, skillDao, "plugins.skillmatrix");

    // Register the web part:
    registerWeb(ID_SKILL_RATING, SkillRatingListPage.class, SkillRatingEditPage.class);
    registerWeb(ID_SKILL, SkillListPage.class, SkillEditPage.class);

    // Register the menu entry as sub menu entry of the misc menu:
    final MenuItemDef parentMenu = getMenuItemDef(MenuItemDefId.MISC);

    registerMenuItem(new MenuItemDef(parentMenu, ID_SKILL_RATING, 5, "plugins.skillmatrix.skillrating.menu", SkillRatingListPage.class));
    registerMenuItem(new MenuItemDef(parentMenu, ID_SKILL, 5, "plugins.skillmatrix.skill.menu", SkillListPage.class));

    // .setMobileMenu(SkillRatingMobileListPage.class, 10));

    // Define the access management:
    registerRight(new SkillRight());
    registerRight(new SkillRatingRight());

    // All the i18n stuff:
    addResourceBundle(RESOURCE_BUNDLE_NAME);
  }

  /**
   * Setter is called by the Spring framework with a proper initialized data access object (defined in pluginContext.xml).
   * @param skillDao
   */
  public void setSkillDao(final SkillDao skillDao)
  {
    this.skillDao = skillDao;
  }

  public void setSkillRatingDao(final SkillRatingDao skillRatingDao)
  {
    this.skillRatingDao = skillRatingDao;
  }

  /**
   * @see org.projectforge.plugins.core.AbstractPlugin#getInitializationUpdateEntry()
   */
  @Override
  public UpdateEntry getInitializationUpdateEntry()
  {
    return SkillMatrixPluginUpdates.getInitializationUpdateEntry();
  }
}
