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
import org.projectforge.user.UserRightId;

/**
 * @author Billy Duong (duong.billy@yahoo.de)
 *
 */
public class SkillRatingDao extends BaseDao<SkillRatingDO>
{
  public static final UserRightId USER_RIGHT_ID = new UserRightId("PLUGIN_SKILL_MATRIX_SKILL_RATING", "plugin20", "plugins.skillmatrix.skillrating");

  static final String I18N_KEY_ERROR_CYCLIC_REFERENCE = "plugins.skillmatrix.error.cyclicReference";

  public SkillRatingDao(){
    super(SkillRatingDO.class);
    userRightId = USER_RIGHT_ID;
  }

  @Override
  public SkillRatingDO newInstance()
  {
    return new SkillRatingDO();
  }

}
