/////////////////////////////////////////////////////////////////////////////
//
// Project   ProjectForge
//
// Copyright 2001-2009, Micromata GmbH, Kai Reinhard
//           All rights reserved.
//
/////////////////////////////////////////////////////////////////////////////

package org.projectforge.plugins.chimney.wbs.visitors;

import org.projectforge.plugins.chimney.wbs.AbstractWbsNodeDO;
import org.projectforge.plugins.chimney.wbs.MilestoneDO;
import org.projectforge.plugins.chimney.wbs.PhaseDO;
import org.projectforge.plugins.chimney.wbs.ProjectDO;
import org.projectforge.plugins.chimney.wbs.SubtaskDO;
import org.projectforge.plugins.chimney.wbs.WorkpackageDO;

/**
 * Visitor that checks if childNode can become a child of thisNode. There are two modes for using this visitor that depends on the used
 * constructor:
 * <ul>
 * <li>a) Call any of the WbsNodeChildValidationVisitor(*DO) constructors to set the child holder node directly. This is useful when the
 * runtime type of the child holder is known.</li>
 * <li>b) If the runtime type of the child holder is unknown, use the WbsNodeChildValidationVisitor(). Then, the visitor must be called at
 * least twice. The first time, it must be called by the child holder.</li>
 * </ul>
 * The following rules apply:
 * <ul>
 * <li>Milestones can be added to Projects</li>
 * <li>Subtasks can be added to Projects and Subtasks</li>
 * <li>Workpackages can be added to Projects and Subtasks</li>
 * <li>Phases can be added to Projects</li>
 * <li>Projects are only permitted as root</li>
 * </ul>
 */
public class WbsNodeChildValidationVisitor implements IWbsNodeVisitor
{

  enum WbsType
  {
    MILESTONE, PROJECT, SUBTASK, WORKPACKAGE, PHASE
  };

  private WbsType childHolderType;

  private AbstractWbsNodeDO childHolder;

  private boolean validChild = false;

  private InvalidChildErrorType errorReason;

  /**
   * This constructor turns the visitor into a little state machine. The first time the created visitor is called, it must be called by the
   * child holder to set the node type to compare against.
   */
  public WbsNodeChildValidationVisitor()
  {
    childHolderType = null;
  }

  /**
   * @param thisNode The wbs node that a child is supposed to be added to
   */
  public WbsNodeChildValidationVisitor(final MilestoneDO thisNode)
  {
    setChildHolder(thisNode, WbsType.MILESTONE);
  }

  public WbsNodeChildValidationVisitor(final ProjectDO thisNode)
  {
    setChildHolder(thisNode, WbsType.PROJECT);
  }

  public WbsNodeChildValidationVisitor(final SubtaskDO thisNode)
  {
    setChildHolder(thisNode, WbsType.SUBTASK);
  }

  public WbsNodeChildValidationVisitor(final WorkpackageDO thisNode)
  {
    setChildHolder(thisNode, WbsType.WORKPACKAGE);
  }

  public WbsNodeChildValidationVisitor(final PhaseDO thisNode)
  {
    setChildHolder(thisNode, WbsType.PHASE);
  }

  @Override
  public void visit(final MilestoneDO childNode)
  {
    // set the child holder's type if not set and return
    if (childHolderType == null) {
      setChildHolder(childNode, WbsType.MILESTONE);
      return;
    }
    if (childHolderEquals(childNode) || wouldCreateLoopWith(childNode))
      return;

    // Milestones may be added to projects and phases
    switch (childHolderType) {
      case PROJECT:
      case PHASE:
        setChildValid();
        break;
      default:
        setChildInvalid(InvalidChildErrorType.MILESTONE_CHILD_ILLEGAL);
    }
  }

  @Override
  public void visit(final ProjectDO childNode)
  {
    // set the child holder's type if not set and return
    if (childHolderType == null) {
      setChildHolder(childNode, WbsType.PROJECT);
      return;
    }
    if (childHolderEquals(childNode) || wouldCreateLoopWith(childNode))
      return;

    // Projects may be added to nothing, they are always root elements
    setChildInvalid(InvalidChildErrorType.PROJECT_CHILD_ILLEGAL);
  }

  @Override
  public void visit(final SubtaskDO childNode)
  {
    // set the child holder's type if not set and return
    if (childHolderType == null) {
      setChildHolder(childNode, WbsType.SUBTASK);
      return;
    }
    if (childHolderEquals(childNode) || wouldCreateLoopWith(childNode))
      return;

    // Subtasks may be added to projects, other subtasks and phases
    switch (childHolderType) {
      case PROJECT:
      case SUBTASK:
      case PHASE:
        setChildValid();
        break;
      default:
        setChildInvalid(InvalidChildErrorType.SUBTASK_CHILD_ILLEGAL);
    }
  }

  @Override
  public void visit(final WorkpackageDO childNode)
  {
    // set the child holder's type if not set and return
    if (childHolderType == null) {
      setChildHolder(childNode, WbsType.WORKPACKAGE);
      return;
    }
    if (childHolderEquals(childNode) || wouldCreateLoopWith(childNode))
      return;

    // Workpackage may be added to projects and subtasks
    switch (childHolderType) {
      case PROJECT:
      case SUBTASK:
        setChildValid();
        break;
      default:
        setChildInvalid(InvalidChildErrorType.WORKPACKAGE_CHILD_ILLEGAL);
    }
  }

  @Override
  public void visit(final AbstractWbsNodeDO childNode)
  {
    if (childHolderType == null) {
      throw new UnsupportedOperationException("Node is not supported as a child holder by this visitor");
    }

    // unsupported WbsNodeDOs cannot be added using this visitor
    setChildInvalid(InvalidChildErrorType.UNSUPPORTED);
  }

  @Override
  public void visit(final PhaseDO childNode)
  {
    // set the child holder's type if not set and return
    if (childHolderType == null) {
      setChildHolder(childNode, WbsType.PHASE);
      return;
    }
    if (childHolderEquals(childNode) || wouldCreateLoopWith(childNode))
      return;

    // Phase may be added to projects
    switch (childHolderType) {
      case PROJECT:
        setChildValid();
        break;
      default:
        setChildInvalid(InvalidChildErrorType.PHASE_CHILD_ILLEGAL);
    }
  }

  public boolean isValidChild()
  {
    return validChild;
  }

  public InvalidChildErrorType getErrorReason()
  {
    return errorReason;
  }

  private void setChildHolder(final AbstractWbsNodeDO childHolder, final WbsType childHolderType)
  {
    this.childHolder = childHolder;
    this.childHolderType = childHolderType;
  }

  private void setChildValid()
  {
    validChild = true;
    errorReason = null;
  }

  private void setChildInvalid(final InvalidChildErrorType reason)
  {
    validChild = false;
    errorReason = reason;
  }

  private boolean childHolderEquals(final AbstractWbsNodeDO childNode)
  {
    final boolean nodesEqual = childHolder.equals(childNode);
    if (nodesEqual) {
      setChildInvalid(InvalidChildErrorType.CHILD_TO_ITSELF_ILLEGAL);
    }
    return nodesEqual;
  }

  private boolean wouldCreateLoopWith(final AbstractWbsNodeDO childNode)
  {
    // check if the child holder has childNode in its path to the root
    // in this case, it is illegal to add the node because a loop would be created
    AbstractWbsNodeDO parent = childHolder.getParent();
    while (parent != null) {
      if (parent.equals(childNode)) {
        setChildInvalid(InvalidChildErrorType.LOOP_DETECTED);
        return true;
      }
      parent = parent.getParent();
    }
    return false;
  }

}
