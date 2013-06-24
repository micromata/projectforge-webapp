/////////////////////////////////////////////////////////////////////////////
//
// Project   ProjectForge
//
// Copyright 2001-2009, Micromata GmbH, Kai Reinhard
//           All rights reserved.
//
/////////////////////////////////////////////////////////////////////////////

package org.projectforge.plugins.skillmatrix;

import org.projectforge.web.tree.TreeTableNode;

/**
 * Represents a single node as part of a TreeTable.
 * @author Billy Duong (b.duong@micromata.de)
 *
 */
public class SkillTreeTableNode extends TreeTableNode
{
  private static final long serialVersionUID = 6436021236494684413L;

  private final SkillNode skillNode;

  public SkillTreeTableNode(final SkillTreeTableNode parent, final SkillNode skillNode) {
    super(parent, parent.getHashId());
    this.skillNode = skillNode;
  }

  /**
   * @return the skillNode
   */
  public SkillNode getSkillNode()
  {
    return skillNode;
  }

  public SkillDO getSkill() {
    return skillNode.getSkill();
  }

  public String getSkillTitle() {
    return getSkill().getTitle();
  }

  public Integer getId() {
    return getSkill().getId();
  }

  /**
   * Return a String representation of this object.
   */
  @Override
  public String toString()
  {
    final StringBuffer sb = new StringBuffer("SkillTreeTableNode[skillName=");
    sb.append(getSkillTitle());
    sb.append(",id=");
    sb.append(getId());
    sb.append("]");
    return (sb.toString());
  }

  /** Should be overwrite by derived classes. */
  @Override
  public int compareTo(final TreeTableNode obj)
  {
    final SkillTreeTableNode node = (SkillTreeTableNode) obj;
    return skillNode.getSkill().getTitle().compareTo(node.getSkill().getTitle());
  }

}
