/////////////////////////////////////////////////////////////////////////////
//
// Project   ProjectForge
//
// Copyright 2001-2009, Micromata GmbH, Kai Reinhard
//           All rights reserved.
//
/////////////////////////////////////////////////////////////////////////////

package org.projectforge.plugins.skillmatrix;

import org.projectforge.core.BaseDao;
import org.projectforge.user.UserDao;
import org.projectforge.user.UserRightId;

/**
 * This is the base data access object class. Most functionality such as access checking, select, insert, update, save, delete etc. is
 * implemented by the super class.
 * @author Werner Feder (werner.feder@t-online.de)
 */
public class AttendeeDao extends BaseDao<AttendeeDO>
{

  public static final String UNIQUE_PLUGIN_ID = "PLUGIN_SKILL_MATRIX_TRAINING_ATTENDEE";

  public static final String I18N_KEY_SKILL_PREFIX = "plugins.skillmatrix.training";

  public static final UserRightId USER_RIGHT_ID = new UserRightId(UNIQUE_PLUGIN_ID, "plugin20", I18N_KEY_SKILL_PREFIX);

  private static final String[] ADDITIONAL_SEARCH_FIELDS = new String[] { "attendee.firstname", "attendee.lastname","training.title",
    "training.skill.title", "rating", "certificate" };

  // private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(AttendeeDao.class);

  private TrainingDao trainingDao;
  private UserDao userDao;

  public AttendeeDao()
  {
    super(AttendeeDO.class);
    userRightId = USER_RIGHT_ID;
  }

  @Override
  protected String[] getAdditionalSearchFields()
  {
    return ADDITIONAL_SEARCH_FIELDS;
  }

  @Override
  public AttendeeDO newInstance()
  {
    return new AttendeeDO();
  }

  public TrainingDao getTraingDao()
  {
    return trainingDao;
  }

  public AttendeeDao setTraingDao(final TrainingDao trainingDao)
  {
    this.trainingDao = trainingDao;
    return this;
  }

  public UserDao getUserDao()
  {
    return userDao;
  }

  public AttendeeDao setUserDao(final UserDao userDao)
  {
    this.userDao = userDao;
    return this;
  }
}
