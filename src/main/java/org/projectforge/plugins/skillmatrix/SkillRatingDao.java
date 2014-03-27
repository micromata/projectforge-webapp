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

import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.hibernate.criterion.Restrictions;
import org.projectforge.core.BaseDao;
import org.projectforge.core.BaseSearchFilter;
import org.projectforge.core.QueryFilter;
import org.projectforge.core.UserException;
import org.projectforge.registry.Registry;
import org.projectforge.user.PFUserDO;
import org.projectforge.user.UserRightId;

/**
 * DAO for SkillRatingDO. Handles constraint validation and database access.
 * @author Billy Duong (b.duong@micromata.de)
 * 
 */
public class SkillRatingDao extends BaseDao<SkillRatingDO>
{
  public static final String UNIQUE_PLUGIN_ID = "PLUGIN_SKILL_MATRIX_SKILL_RATING";

  public static final String I18N_KEY_SKILLRATING_PREFIX = "plugins.skillmatrix.skillrating";

  public static final UserRightId USER_RIGHT_ID = new UserRightId(
      UNIQUE_PLUGIN_ID, "plugin20",
      I18N_KEY_SKILLRATING_PREFIX);

  static final String I18N_KEY_ERROR_CYCLIC_REFERENCE = "plugins.skillmatrix.error.cyclicReference";

  public static final String I18N_KEY_ERROR_DUPLICATE_RATING = "plugins.skillmatrix.error.duplicateRating";

  public static final String I18N_KEY_ERROR_RATEABLE_SKILL_WITH_NULL_RATING = "plugins.skillmatrix.error.rateableSkillWithNullRating";

  public static final String I18N_KEY_ERROR_UNRATEABLE_SKILL_WITH_RATING = "plugins.skillmatrix.error.unrateableSkillWithRating";

  private static final String[] ADDITIONAL_SEARCH_FIELDS = new String[] { "skill.title" };

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
   * @see org.projectforge.core.BaseDao#onSaveOrModify(org.projectforge.core.ExtendedBaseDO)
   */
  @Override
  protected void onSaveOrModify(final SkillRatingDO obj)
  {
    synchronized (this) {
      checkConstraintViolation(obj);
    }
  }

  /**
   * 
   * @param skillRating that needs to be validated.
   * @throws UserException is thrown when the user wants to create a duplicate.
   */
  @SuppressWarnings("unchecked")
  private void checkConstraintViolation(final SkillRatingDO skillRating) throws UserException
  {
    List<SkillRatingDO> list;
    if(skillRating.getId() != null) {
      list = getHibernateTemplate().find("from SkillRatingDO s where s.user.id = ? and s.skill.id = ? and s.id != ?", new Object[] { skillRating.getUserId(), skillRating.getSkillId(), skillRating.getId()});
    } else {
      list = getHibernateTemplate().find("from SkillRatingDO s where s.user.id = ? and s.skill.id = ?", new Object[] { skillRating.getUserId(), skillRating.getSkillId()});
    }
    if(CollectionUtils.isNotEmpty(list) == true) {
      throw new UserException(I18N_KEY_ERROR_DUPLICATE_RATING);
    }

    if(skillRating.getSkill().isRateable() == false && skillRating.getSkillRating() != null) {
      throw new UserException(I18N_KEY_ERROR_UNRATEABLE_SKILL_WITH_RATING);
    } else if(skillRating.getSkill().isRateable() == true && skillRating.getSkillRating() == null) {
      throw new UserException(I18N_KEY_ERROR_RATEABLE_SKILL_WITH_NULL_RATING);
    }
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
    final String searchString = myFilter.getSearchString();

    if (myFilter.getSkillRating() != null) {
      final Object[] values = SkillRating.getRequiredExperienceValues(myFilter
          .getSkillRating());
      queryFilter.add(Restrictions.in("skillRating", values));
    }

    if (myFilter.getSkillId() != null) {
      final SkillDO skill = new SkillDO();
      skill.setId(myFilter.getSkillId());
      queryFilter.add(Restrictions.eq("skill", skill));
    }

    if (myFilter.getUserId() != null) {
      final PFUserDO user = new PFUserDO();
      user.setId(myFilter.getUserId());
      queryFilter.add(Restrictions.eq("user", user));
    }

    myFilter.setSearchString(searchString); // Restore search string.
    return getList(queryFilter);
  }

  public SkillRatingDO setSkill(final SkillRatingDO rating, final Integer id)
  {
    final SkillDO skill = Registry.instance().getDao(SkillDao.class).getSkillTree().getSkillById(id);
    rating.setSkill(skill);
    return rating;
  }

}
