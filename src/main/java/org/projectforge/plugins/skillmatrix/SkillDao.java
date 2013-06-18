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

import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.projectforge.core.BaseDao;
import org.projectforge.core.UserException;
import org.projectforge.user.UserRightId;

/**
 * 
 * @author Kai Reinhard (k.reinhard@micromata.de)
 * 
 */
public class SkillDao extends BaseDao<SkillDO>
{
  public static final UserRightId USER_RIGHT_ID = new UserRightId("PLUGIN_SKILL_MATRIX_SKILL", "plugin20", "plugins.skillmatrix.skill");

  static final String I18N_KEY_ERROR_CYCLIC_REFERENCE = "plugins.skillmatrix.error.cyclicReference";

  public static final String I18N_KEY_ERROR_DUPLICATE_CHILD_SKILL = "plugins.skillmatrix.error.duplicateChildSkill";

  private static final String[] ADDITIONAL_SEARCH_FIELDS = new String[] { "parent.title" };

  private final SkillTree skillTree = new SkillTree(this);

  // private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(SkillDao.class);

  public SkillDao()
  {
    super(SkillDO.class);
    userRightId = USER_RIGHT_ID;
  }

  @Override
  public SkillDO newInstance()
  {
    return new SkillDO();
  }

  /**
   * @see org.projectforge.core.BaseDao#onSaveOrModify(org.projectforge.core.ExtendedBaseDO)
   */
  @Override
  protected void onSaveOrModify(final SkillDO obj)
  {
    synchronized (this) {
      checkConstraintViolation(obj);
    }
  }

  @Override
  protected void afterSaveOrModify(final SkillDO obj)
  {
    skillTree.setExpired();
  }

  public SkillTree getSkillTree() {
    return skillTree;
  }

  @SuppressWarnings("unchecked")
  public void checkConstraintViolation(final SkillDO skill) throws UserException
  {
    List<SkillDO> list;
    // TODO: Check if Skill already exists. -> example TaskDao.checkConstraintViolation
    if(skill.getId() != null) {
      list = getHibernateTemplate().find("from SkillDO s where s.parent.id = ? and s.title = ? and s.id != ?",
          new Object[]{skill.getParentId(), skill.getTitle(), skill.getId()});
    } else {
      list = getHibernateTemplate().find("from SkillDO s where s.parent.id = ? and s.title = ?", new Object[]{skill.getParentId(), skill.getTitle()});
    }
    if(CollectionUtils.isNotEmpty(list)) {
      throw new UserException(I18N_KEY_ERROR_DUPLICATE_CHILD_SKILL);
    }
    // TODO: Check for valid Tree structure (root) -> example TaskDao.checkConstraintVilation
  }

  /**
   * @see org.projectforge.core.BaseDao#getAdditionalSearchFields()
   */
  @Override
  protected String[] getAdditionalSearchFields()
  {
    return ADDITIONAL_SEARCH_FIELDS;
  }
}
