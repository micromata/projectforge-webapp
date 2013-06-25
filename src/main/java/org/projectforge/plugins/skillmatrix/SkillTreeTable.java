/////////////////////////////////////////////////////////////////////////////
//
// Project   ProjectForge
//
// Copyright 2001-2009, Micromata GmbH, Kai Reinhard
//           All rights reserved.
//
/////////////////////////////////////////////////////////////////////////////

package org.projectforge.plugins.skillmatrix;

import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.projectforge.web.tree.TreeTable;
import org.projectforge.web.tree.TreeTableFilter;
import org.projectforge.web.tree.TreeTableNode;

/**
 * The implementation of TreeTable for skills. Used for browsing the skills (tree view).
 * @author Billy Duong (b.duong@micromata.de)
 * 
 */
public class SkillTreeTable extends TreeTable<SkillTreeTableNode>
{

  private static final long serialVersionUID = 2448799447532237904L;

  private static final Logger log = Logger.getLogger(SkillTreeTable.class);

  @SpringBean(name = "skillDao")
  private SkillDao skillDao;

  private final SkillNode rootNode;

  /** Time of last modification in milliseconds from 1970-01-01. */
  private long timeOfLastModification = 0;

  public SkillTreeTable(final SkillNode root)
  {
    this.rootNode = root;
  }

  public TreeTableNode setOpenedStatusOfNode(final String eventKey, final Integer hashId)
  {
    return super.setOpenedStatusOfNode(eventKey, hashId);
  }

  @Override
  public List<SkillTreeTableNode> getNodeList(final TreeTableFilter<TreeTableNode> filter)
  {
    if (getTimeOfLastModification() < getSkillTree().getTimeOfLastModification()) {
      reload();
    }
    return super.getNodeList(filter);
  }

  protected void addDescendantNodes(final SkillTreeTableNode parent)
  {
    final SkillNode skill = parent.getSkillNode();
    if (skill.getChilds() != null) {
      for (final SkillNode node : skill.getChilds()) {
        if (getSkillTree().hasSelectAccess(node) == true) {
          // The logged in user has select access, so add this skill node
          // to this tree table:
          final SkillTreeTableNode child = new SkillTreeTableNode(parent, node);
          addTreeTableNode(child);
          addDescendantNodes(child);
        }
      }
    }
  }

  protected synchronized void reload()
  {
    log.debug("Reloading skill tree.");
    allNodes.clear();
    if (rootNode != null) {
      root = new SkillTreeTableNode(null, rootNode);
    } else {
      root = new SkillTreeTableNode(null, getSkillTree().getRootSkillNode());
    }
    addDescendantNodes(root);
    updateOpenStatus();
    updateTimeOfLastModification();
  }

  /**
   * Has the current logged in user select access to the given skill?
   * @param node
   * @return
   */
  boolean hasSelectAccess(final SkillNode node)
  {
    return getSkillTree().hasSelectAccess(node);
  }

  /**
   * @return the timeOfLastModification
   */
  public long getTimeOfLastModification()
  {
    return timeOfLastModification;
  }

  private void updateTimeOfLastModification()
  {
    this.timeOfLastModification = new Date().getTime();
  }

  private SkillTree getSkillTree()
  {
    return skillDao.getSkillTree();
  }
}
