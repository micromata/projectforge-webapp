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
 * @author Werner Feder (werner.feder@t-online.de)
 * 
 */
public class InviteeDao extends BaseDao<InviteeDO>
{

  public static final String UNIQUE_PLUGIN_ID = "PLUGIN_SKILL_MATRIX_TRAINING_INVITEE";

  public static final String I18N_KEY_SKILL_PREFIX = "plugins.skillmatrix.training.invitee";

  public static final UserRightId USER_RIGHT_ID = new UserRightId(UNIQUE_PLUGIN_ID, "plugin20", I18N_KEY_SKILL_PREFIX);

  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(InviteeDao.class);

  private TrainingDao trainingDao;
  private UserDao userDao;

  public InviteeDao()
  {
    super(InviteeDO.class);
    userRightId = USER_RIGHT_ID;
  }

  @Override
  public InviteeDO newInstance()
  {
    return new InviteeDO();
  }

  public TrainingDao getTraingDao() {
    return trainingDao;
  }

  public InviteeDao setTraingDao(final TrainingDao trainingDao) {
    this.trainingDao = trainingDao;
    return this;
  }

  public UserDao getUserDao() {
    return userDao;
  }

  public InviteeDao setUserDao(final UserDao userDao) {
    this.userDao = userDao;
    return this;
  }
}
