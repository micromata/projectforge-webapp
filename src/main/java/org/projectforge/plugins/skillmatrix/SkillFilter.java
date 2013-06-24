/////////////////////////////////////////////////////////////////////////////
//
// Project   ProjectForge
//
// Copyright 2001-2009, Micromata GmbH, Kai Reinhard
//           All rights reserved.
//
/////////////////////////////////////////////////////////////////////////////

package org.projectforge.plugins.skillmatrix;

import java.util.HashMap;
import java.util.HashSet;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.projectforge.core.BaseSearchFilter;
import org.projectforge.user.PFUserDO;

/**
 * @author Billy Duong (b.duong@micromata.de)
 *
 */
public class SkillFilter extends BaseSearchFilter
{

  private static final long serialVersionUID = -8140231682294892497L;

  /**
   * Used by match filter for avoiding multiple traversing of the tree. Should be empty before building a skill node list!
   */
  private transient HashMap<Integer, Boolean> skillVisibility;

  /**
   * Used by match filter for storing those skills which matches the search string. Should be empty before building a skill node list!
   */
  private transient HashSet<Integer> skillsMatched;

  public SkillFilter() {
  }

  public SkillFilter(final SkillFilter baseSearchFilter)
  {
    super(baseSearchFilter);
  }

  public void resetMatch()
  {
    skillVisibility = new HashMap<Integer, Boolean>();
    skillsMatched = new HashSet<Integer>();
  }

  /**
   * Needed by SkillTreeTable to show and hide nodes.<br/>
   * Don't forget to call resetMatch before!
   * @param node Node to check.
   * @param skillDao Needed for access checking.
   * @param user Needed for access checking.
   * @see org.projectforge.web.tree.TreeTableFilter#match(org.projectforge.web.tree.TreeTableNode)
   */
  public boolean match(final SkillNode node, final SkillDao skillDao, final PFUserDO user)
  {
    Validate.notNull(node);
    Validate.notNull(node.getSkill());
    if (skillVisibility == null) {
      resetMatch();
    }
    final SkillDO skill = node.getSkill();
    if (StringUtils.isBlank(this.searchString) == true) {
      return node.isRootNode() == true;
    } else {
      if (isVisibleBySearchString(node, skill, skillDao, user) == true) {
        return node.isRootNode() == true;
      } else {
        if (node.getParent() != null && node.getParent().isRootNode() == false && isAncestorVisibleBySearchString(node.getParent()) == true) {
          // Otherwise the node is only visible by his status if the parent node is visible:
          //return isVisibleByStatus(node, skill);
          return false;
        } else {
          return false;
        }
      }
    }
  }

  private boolean isAncestorVisibleBySearchString(final SkillNode node)
  {
    if (skillsMatched.contains(node.getId()) == true) {
      return true;
    } else if (node.getParent() != null) {
      return isAncestorVisibleBySearchString(node.getParent());
    }
    return false;
  }

  /**
   * @param node
   * @param skill
   * @return true if the search string matches at least one field of the skill of if this methods returns true for any descendant.
   */
  private boolean isVisibleBySearchString(final SkillNode node, final SkillDO skill, final SkillDao skillDao, final PFUserDO user)
  {
    final Boolean cachedVisibility = skillVisibility.get(skill.getId());
    if (cachedVisibility != null) {
      return cachedVisibility;
    }
    if (node.isRootNode() == false) {
      skillVisibility.put(skill.getId(), false);
      return false;
    }
    if (skillDao != null && skillDao.hasSelectAccess(user, node.getSkill(), false) == false) {
      return false;
    }
    //    final PFUserDO responsibleUser = Registry.instance().getUserGroupCache().getUser(skill.getResponsibleUserId());
    //    final String username = responsibleUser != null ? responsibleUser.getFullname() + " " + responsibleUser.getUsername() : null;
    if (StringUtils.containsIgnoreCase(skill.getTitle(), this.searchString) == true
        || StringUtils.containsIgnoreCase(skill.getDescription(), this.searchString) == true) {
      skillVisibility.put(skill.getId(), true);
      skillsMatched.add(skill.getId());
      return true;
    } else if (node.hasChilds() == true && node.isRootNode() == false) {
      for (final SkillNode childNode : node.getChilds()) {
        final SkillDO childSkill = childNode.getSkill();
        if (isVisibleBySearchString(childNode, childSkill, skillDao, user) == true) {
          skillVisibility.put(childSkill.getId(), true);
          return true;
        }
      }
    }
    skillVisibility.put(skill.getId(), false);
    return false;
  }

}
