/////////////////////////////////////////////////////////////////////////////
//
// Project   ProjectForge
//
// Copyright 2001-2009, Micromata GmbH, Kai Reinhard
//           All rights reserved.
//
/////////////////////////////////////////////////////////////////////////////

package org.projectforge.plugins.skillmatrix;

import java.util.Collection;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.projectforge.core.BaseDao;
import org.projectforge.user.GroupDO;
import org.projectforge.user.UserRightId;
import org.projectforge.web.user.GroupsProvider;

/**
 * This is the base data access object class. Most functionality such as access checking, select, insert, update, save, delete etc. is implemented by the super class.
 * @author Werner Feder (werner.feder@t-online.de)
 */
public class TrainingDao extends BaseDao<TrainingDO>
{

  public static final String UNIQUE_PLUGIN_ID = "PLUGIN_SKILL_MATRIX_TRAINING";

  public static final String I18N_KEY_SKILL_PREFIX = "plugins.skillmatrix.training";

  public static final UserRightId USER_RIGHT_ID = new UserRightId(UNIQUE_PLUGIN_ID, "plugin20", I18N_KEY_SKILL_PREFIX);

  private static final String[] ADDITIONAL_SEARCH_FIELDS = new String[] { "skill.title" };

  private SkillDao skillDao;

  public TrainingDao()
  {
    super(TrainingDO.class);
    userRightId = USER_RIGHT_ID;
  }

  @Override
  protected String[] getAdditionalSearchFields()
  {
    return ADDITIONAL_SEARCH_FIELDS;
  }

  @Override
  public TrainingDO newInstance()
  {
    return new TrainingDO();
  }

  public TrainingDao setSkillDao(final SkillDao skillDao)
  {
    this.skillDao = skillDao;
    return this;
  }

  public SkillDao getSkillDao()
  {
    return skillDao;
  }

  /**
   * @param skill
   * @param skillId If null, then skill will be set to null;
   * @see BaseDao#getOrLoad(Integer)
   */
  public TrainingDO setSkill(final TrainingDO training, final Integer skillId)
  {
    final SkillDO skill = skillDao.getOrLoad(skillId);
    training.setSkill(skill);
    return training;
  }

  @SuppressWarnings("unchecked")
  public TrainingDO getTraining(final String title)
  {
    if (title == null) {
      return null;
    }
    final List<TrainingDO> list = getHibernateTemplate().find("from TrainingDO u where u.title = ?", title);
    if (CollectionUtils.isEmpty(list) == true) {
      return null;
    }
    return list.get(0);
  }

  /**
   * Please note: Only the string group.fullAccessGroupIds will be modified (but not be saved)!
   * @param training
   * @param fullAccessGroups
   */
  public void setFullAccessGroups(final TrainingDO training, final Collection<GroupDO> fullAccessGroups)
  {
    training.setFullAccessGroupIds(new GroupsProvider().getGroupIds(fullAccessGroups));
  }

  public Collection<GroupDO> getSortedFullAccessGroups(final TrainingDO training)
  {
    return new GroupsProvider().getSortedGroups(training.getFullAccessGroupIds());
  }

  /**
   * Please note: Only the string group.readonlyAccessGroupIds will be modified (but not be saved)!
   * @param training
   * @param readonlyAccessGroups
   */
  public void setReadonlyAccessGroups(final TrainingDO training, final Collection<GroupDO> readonlyAccessGroups)
  {
    training.setReadonlyAccessGroupIds(new GroupsProvider().getGroupIds(readonlyAccessGroups));
  }

  public Collection<GroupDO> getSortedReadonlyAccessGroups(final TrainingDO training)
  {
    return new GroupsProvider().getSortedGroups(training.getReadonlyAccessGroupIds());
  }
}
