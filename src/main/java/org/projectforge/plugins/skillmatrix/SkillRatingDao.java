/////////////////////////////////////////////////////////////////////////////
//
// Project   ProjectForge
//
// Copyright 2001-2009, Micromata GmbH, Kai Reinhard
//           All rights reserved.
//
/////////////////////////////////////////////////////////////////////////////

package org.projectforge.plugins.skillmatrix;

import java.util.List;

import org.hibernate.criterion.Restrictions;
import org.projectforge.core.BaseDao;
import org.projectforge.core.BaseSearchFilter;
import org.projectforge.core.QueryFilter;
import org.projectforge.user.UserRightId;

/**
 * @author Billy Duong (duong.billy@yahoo.de)
 * 
 */
public class SkillRatingDao extends BaseDao<SkillRatingDO>
{
  public static final UserRightId USER_RIGHT_ID = new UserRightId("PLUGIN_SKILL_MATRIX_SKILL_RATING", "plugin20",
      "plugins.skillmatrix.skillrating");

  static final String I18N_KEY_ERROR_CYCLIC_REFERENCE = "plugins.skillmatrix.error.cyclicReference";

  private static final String[] ADDITIONAL_SEARCH_FIELDS = new String[] { "skill.title"};

  public SkillRatingDao()
  {
    super(SkillRatingDO.class);
    userRightId = USER_RIGHT_ID;
  }

  @Override
  public SkillRatingDO newInstance()
  {
    return new SkillRatingDO();
  }

  /**
   * @see org.projectforge.core.BaseDao#getAdditionalSearchFields()
   */
  @Override
  protected String[] getAdditionalSearchFields()
  {
    return ADDITIONAL_SEARCH_FIELDS;
  }

  /**
   * @see org.projectforge.core.BaseDao#getList(org.projectforge.core.BaseSearchFilter)
   */
  @Override
  public List<SkillRatingDO> getList(final BaseSearchFilter filter)
  {
    final SkillRatingFilter myFilter;
    if (filter instanceof SkillRatingFilter) {
      myFilter = (SkillRatingFilter) filter;
    } else {
      myFilter = new SkillRatingFilter(filter);
    }
    final QueryFilter queryFilter = new QueryFilter(myFilter);

    if (myFilter.getSkillRating() != null) {
      final Object[] values = SkillRating.getRequiredExperienceValues(myFilter.getSkillRating());
      queryFilter.add(Restrictions.in("skillRating", values));
    }
    return getList(queryFilter);
  }

}
