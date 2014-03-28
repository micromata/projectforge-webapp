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

package org.projectforge.plugins.skillmatrix;

import org.projectforge.continuousdb.UpdateEntry;
import org.projectforge.plugins.core.AbstractPlugin;
import org.projectforge.user.UserPrefArea;
import org.projectforge.web.MenuItemDef;
import org.projectforge.web.MenuItemDefId;

/**
 * @author Billy Duong (b.duong@micromata.de)
 */
public class SkillMatrixPlugin extends AbstractPlugin
{

  public static final String ID_SKILL_RATING = "skillRating";

  public static final String ID_SKILL = "skill";

  public static final String ID_SKILL_TREE = "skillTree";

  public static final String ID_SKILL_TRAINING = "skillTraining";

  public static final String ID_SKILL_TRAINING_ATTENDEE = "skillTrainingAttendee";

  public static final String RESOURCE_BUNDLE_NAME = SkillMatrixPlugin.class.getPackage().getName() + ".SkillMatrixI18nResources";

  static UserPrefArea USER_PREF_AREA;

  private static final Class< ? >[] PERSISTENT_ENTITIES = new Class< ? >[] { SkillDO.class, SkillRatingDO.class, TrainingDO.class, AttendeeDO.class};

  public static final String I18N_KEY_SKILLMATRIX_PREFIX = "plugins.skillmatrix";

  public static final String I18N_KEY_SKILLRATING_MENU_ENTRY = "plugins.skillmatrix.skillrating.menu";

  public static final String I18N_KEY_SKILL_MENU_ENTRY = "plugins.skillmatrix.skill.menu";

  public static final String I18N_KEY_SKILLTREE_MENU_ENTRY = "plugins.skillmatrix.skilltree.menu";

  public static final String I18N_KEY_SKILLTRAINING_MENU_ENTRY = "plugins.skillmatrix.skilltraining.menu";

  public static final String I18N_KEY_SKILLTRAINING_ATTENDEE_MENU_ENTRY = "plugins.skillmatrix.skilltraining.attendee.menu";

  /**
   * This dao should be defined in pluginContext.xml (as resources) for proper initialization.
   */
  private SkillDao skillDao;
  private SkillRatingDao skillRatingDao;
  private TrainingDao trainingDao;
  private AttendeeDao attendeeDao;


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
    SkillMatrixPluginUpdates.dao = getDatabaseUpdateDao();
    register(ID_SKILL_RATING, SkillRatingDao.class, skillRatingDao, I18N_KEY_SKILLMATRIX_PREFIX);
    register(ID_SKILL, SkillDao.class, skillDao, I18N_KEY_SKILLMATRIX_PREFIX);
    register(ID_SKILL_TRAINING, TrainingDao.class, trainingDao, I18N_KEY_SKILLMATRIX_PREFIX);
    register(ID_SKILL_TRAINING_ATTENDEE, AttendeeDao.class, attendeeDao, I18N_KEY_SKILLMATRIX_PREFIX);

    // Register the web part:
    registerWeb(ID_SKILL_RATING, SkillRatingListPage.class, SkillRatingEditPage.class);
    registerWeb(ID_SKILL, SkillListPage.class, SkillEditPage.class);
    registerWeb(ID_SKILL_TRAINING, TrainingListPage.class, TrainingEditPage.class);
    registerWeb(ID_SKILL_TRAINING_ATTENDEE, AttendeeListPage.class, AttendeeEditPage.class);

    // Register the menu entry as sub menu entry of the misc menu:
    final MenuItemDef parentMenu = getMenuItemDef(MenuItemDefId.MISC);

    registerMenuItem(new MenuItemDef(parentMenu, ID_SKILL_TREE, 5, I18N_KEY_SKILLTREE_MENU_ENTRY, SkillTreePage.class));
    registerMenuItem(new MenuItemDef(parentMenu, ID_SKILL_RATING, 5, I18N_KEY_SKILLRATING_MENU_ENTRY, SkillRatingListPage.class));
    registerMenuItem(new MenuItemDef(parentMenu, ID_SKILL, 5, I18N_KEY_SKILL_MENU_ENTRY, SkillListPage.class));
    registerMenuItem(new MenuItemDef(parentMenu, ID_SKILL_TRAINING, 5, I18N_KEY_SKILLTRAINING_MENU_ENTRY, TrainingListPage.class));
    registerMenuItem(new MenuItemDef(parentMenu, ID_SKILL_TRAINING_ATTENDEE, 5, I18N_KEY_SKILLTRAINING_ATTENDEE_MENU_ENTRY, AttendeeListPage.class));

    // .setMobileMenu(SkillRatingMobileListPage.class, 10));

    // Define the access management:
    registerRight(new SkillRight());
    registerRight(new SkillRatingRight());
    registerRight(new TrainingRight());
    registerRight(new AttendeeRight());

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

  public void setTrainingDao(final TrainingDao trainingDao)
  {
    this.trainingDao = trainingDao;
  }

  public void setAttendeeDao(final AttendeeDao attendeeDao)
  {
    this.attendeeDao = attendeeDao;
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
