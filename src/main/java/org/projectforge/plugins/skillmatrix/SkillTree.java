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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.Validate;
import org.apache.log4j.Logger;
import org.projectforge.common.AbstractCache;
import org.projectforge.task.TaskNode;

/**
 * Holds the complete skill list as a tree. It will be initialized by the values read from the database. Any changes will be written to this
 * tree and to the database.
 * @author Kai Reinhard (k.reinhard@micromata.de)
 */
public class SkillTree extends AbstractCache implements Serializable
{
  private static final long serialVersionUID = 8331566269626909676L;

  private static final Logger log = Logger.getLogger(SkillTree.class);

  private static final List<SkillNode> EMPTY_LIST = new ArrayList<SkillNode>();

  private final SkillDao skillDao;

  /** For faster searching of entries. */
  private Map<Integer, SkillNode> skillMap;

  /** The root node of all skills. The only node with parent null. */
  private SkillNode root = null;

  public SkillNode getRootTaskNode()
  {
    checkRefresh();
    return this.root;
  }

  /** Adds the given node as child of the given parent. */
  private synchronized SkillNode addTaskNode(final SkillNode node, final SkillNode parent)
  {
    checkRefresh();
    if (parent != null) {
      node.setParent(parent);
      parent.addChild(node);
    }
    return node;
  }

  /**
   * Adds a new node with the given data. The given skill holds all data and the information (id) of the parent node of the node to add. Will
   * be called by SkillDao after inserting a new skill.
   */
  SkillNode addSkillNode(final SkillDO skill)
  {
    checkRefresh();
    final SkillNode node = new SkillNode();
    node.setSkill(skill);
    final SkillNode parent = getSkillNodeById(skill.getParentId());
    if (parent != null) {
      node.setParent(parent);
    } else if (node.getId().equals(root.getId()) == false) {
      // This node is not the root node:
      node.setParent(root);
    }
    skillMap.put(node.getId(), node);
    return addTaskNode(node, parent);
  }

  /**
   * @param skillId
   * @param ancestorTaskId
   * @return
   * @see TaskNode#getPathToAncestor(Integer)
   */
  public List<SkillNode> getPath(final Integer skillId, final Integer ancestorSkillId)
  {
    checkRefresh();
    if (skillId == null) {
      return EMPTY_LIST;
    }
    final SkillNode skillNode = getSkillNodeById(skillId);
    if (skillNode == null) {
      return EMPTY_LIST;
    }
    return skillNode.getPathToAncestor(ancestorSkillId);
  }

  /**
   * Returns the path to the root node in an ArrayList.
   * @see #getPath(Integer, Integer)
   */
  public List<SkillNode> getPathToRoot(final Integer skillId)
  {
    return getPath(skillId, null);
  }

  /** All skill nodes are stored in an HashMap for faster searching. */
  public SkillNode getSkillNodeById(final Integer id)
  {
    if (id == null) {
      return null;
    }
    checkRefresh();
    return skillMap.get(id);
  }

  public SkillDO getSkill(final String title) {
    if(title == null) {
      return null;
    }
    checkRefresh();
    for(final SkillNode skill : skillMap.values()) {
      if(title.equals(skill.getSkill().getTitle())) {
        return skill.getSkill();
      }
    }
    return null;
  }

  public SkillDO getSkillById(final Integer id)
  {
    checkRefresh();
    final SkillNode node = getSkillNodeById(id);
    if (node != null) {
      return node.getSkill();
    }
    return null;
  }

  /**
   * After changing a skill this method will be called by SkillDao for updating the skill and the skill tree.
   * @param skill Updating the existing skill in the SkillTree. If not exist, a new skill will be added.
   */
  SkillNode addOrUpdateSkillNode(final SkillDO skill)
  {
    checkRefresh();
    Validate.notNull(skill);
    Validate.notNull(skill.getId());
    final SkillNode node = getSkillNodeById(skill.getId());
    if (node == null) {
      return addSkillNode(skill);
    }
    node.setSkill(skill);
    if (skill.getParentId() != null && skill.getParentId().equals(node.getParent().getId()) == false) {
      if (log.isDebugEnabled() == true) {
        log.debug("Skill hierarchy was changed for skill: " + skill);
      }
      final SkillNode oldParent = node.getParent();
      Validate.notNull(oldParent);
      oldParent.removeChild(node);
      final SkillNode newParent = getSkillNodeById(skill.getParentId());
      node.setParent(newParent);
      newParent.addChild(node);
    }
    return node;
  }

  public SkillTree(final SkillDao skillDao)
  {
    this.skillDao = skillDao;
  }

  /**
   * @see #isRootNode(SkillDO)
   */
  public boolean isRootNode(final SkillNode node)
  {
    Validate.notNull(node);
    return isRootNode(node.getSkill());
  }

  /**
   * @param node
   * @return true, if the given skill has the same id as the Skill tree's root node, otherwise false;
   */
  public boolean isRootNode(final SkillDO skill)
  {
    Validate.notNull(skill);
    if (root == null && skill.getParentId() == null) {
      // First skill, so it should be the root node.
      return true;
    }
    checkRefresh();
    if (skill.getId() == null) {
      // Node has no id, so it can't be the root node.
      return false;
    }
    return root.getId().equals(skill.getId());
  }

  /**
   * Should only called by test suite!
   */
  public void clear()
  {
    this.root = null;
    this.setExpired();
  }

  /**
   * All skills from database will be read and cached into this SkillTree. Also all explicit group skill access' will be read from database and
   * will be cached in this tree (implicit access' will be created too).<br/>
   * The generation of the skill tree will be done manually, not by Hibernate because the skill hierarchy is very sensible. Manipulations of
   * the skill tree should be done carefully for single skill nodes.
   * 
   * @see org.projectforge.common.AbstractCache#refresh()
   */
  @Override
  protected void refresh()
  {
    log.info("Initializing skill tree ...");
    SkillNode newRoot = null;
    skillMap = new HashMap<Integer, SkillNode>();
    final List<SkillDO> skillList = skillDao.internalLoadAll();
    SkillNode node;
    log.debug("Loading list of skills ...");
    for (final SkillDO skill : skillList) {
      node = new SkillNode();
      node.setSkill(skill);
      skillMap.put(node.getId(), node);
      if (node.isRootNode() == true) {
        if (newRoot != null) {
          log.error("Duplicate root node found: " + newRoot.getId() + " and " + node.getId());
          node.setParent(newRoot); // Set the second root skill as child skill of first read root skill.
        } else {
          if (log.isDebugEnabled() == true) {
            log.debug("Root note found: " + node);
          }
          newRoot = node;
        }
      }
    }

    if (newRoot == null) {
      log.fatal("OUPS, no skill found (ProjectForge database not initialized?) OK, initialize it ...");
      final SkillDO rootSkill = new SkillDO();
      rootSkill.setTitle("root");
      skillDao.internalSave(rootSkill);
      newRoot = new SkillNode();
      newRoot.setSkill(rootSkill);
      skillMap.put(newRoot.getId(), newRoot);
    }
    this.root = newRoot;
    if (log.isDebugEnabled() == true) {
      log.debug("Creating tree for " + skillList.size() + " skills ...");
    }
    for (final SkillDO skill : skillList) {
      SkillNode parentNode = null;
      node = skillMap.get(skill.getId());
      final Integer parentId = skill.getParentId();
      if (parentId != null) {
        parentNode = skillMap.get(parentId);
      }
      // log.debug("Processing node: " + node.getId() + ", parent: " + parentId);
      if (parentNode != null) {
        node.setParent(parentNode);
        parentNode.addChild(node);
      } else {
        log.debug("Processing root node:" + node);
      }
    }

    if (log.isDebugEnabled() == true) {
      log.debug(this.root);
    }
    if (log.isDebugEnabled() == true) {
      log.debug(this.toString());
    }
    log.info("Initializing skill tree done.");
  }
}
