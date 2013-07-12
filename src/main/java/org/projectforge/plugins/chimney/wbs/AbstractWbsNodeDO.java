/////////////////////////////////////////////////////////////////////////////
//
// Project   ProjectForge
//
// Copyright 2001-2009, Micromata GmbH, Kai Reinhard
//           All rights reserved.
//
/////////////////////////////////////////////////////////////////////////////

package org.projectforge.plugins.chimney.wbs;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.OrderColumn;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.Validate;
import org.hibernate.Hibernate;
import org.hibernate.search.annotations.Indexed;
import org.projectforge.core.BaseDO;
import org.projectforge.core.ModificationStatus;
import org.projectforge.core.Priority;
import org.projectforge.plugins.chimney.wbs.visitors.IWbsNodeVisitor;
import org.projectforge.task.TaskDO;
import org.projectforge.task.TaskStatus;
import org.projectforge.user.PFUserDO;

/**
 * @author Sweeps <pf@byte-storm.com>
 * 
 */
@Entity
@org.hibernate.annotations.Entity(dynamicUpdate = true)
// enables dirty checking on UPDATE
@Indexed
@Table(name = "T_CHIMNEY_WBS_NODE")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "node_type", discriminatorType = DiscriminatorType.STRING)
public abstract class AbstractWbsNodeDO extends AbstractVisitableBaseDO<IWbsNodeVisitor> implements IWbsNodeReadOnly
{
  private static final long serialVersionUID = -3762621542515259774L;

  protected TaskDO structureElementDO = new TaskDO();

  protected String wbsCode;

  protected AbstractWbsNodeDO parent;

  protected List<AbstractWbsNodeDO> children = new ArrayList<AbstractWbsNodeDO>();

  protected int autoIncrementChildren = 0;

  protected PhaseDO phaseDO;

  protected PlanningStatus planningStatus = PlanningStatus.PLANNING;

  /**
   * Checks whether newNode is a valid child node of this node.
   * @param newNode The node that is about to be added as child
   * @return true is newNode is a valid child
   */
  protected abstract boolean childIsOfValidType(AbstractWbsNodeDO newNode);

  /**
   * Adds a WbsNodeDO as child. Throws an IllegalArgumentException if childIsOfValidType(newNode) validates to false.
   * @param newNode
   * @throws IllegalArgumentException if newNode=null, node type cannot be added to this node or the node is already a child of this node
   */
  public void addChild(final AbstractWbsNodeDO newNode)
  {
    Validate.notNull(newNode);
    Validate.isTrue(newNode.getParent() == null, "Node already has a parent");
    ensureChildInsertible(newNode);
    ensureChildIsNotInList(newNode);
    children.add(newNode);
    newNode.setNewParent(this);
  }

  protected void setNewParent(final AbstractWbsNodeDO newParent)
  {
    parent = newParent;
    getTaskDo().setParentTask(newParent.getTaskDo());
  }

  /**
   * Removes the given node from this node's children list and sets the node's parent reference to null
   * @param oldNode The node to remove as child
   * @return true on success, false if the given node was not a child of this node
   */
  public boolean removeChild(final AbstractWbsNodeDO oldNode)
  {
    Validate.notNull(oldNode);
    final boolean success = children.remove(oldNode);
    if (success)
      oldNode.parent = null;
    return success;
  }

  @Override
  public int childrenCount()
  {
    return children.size();
  }

  @Override
  public AbstractWbsNodeDO getChild(final int index)
  {
    return children.get(index);
  }

  @Override
  public int getChildIndex(final AbstractWbsNodeDO node)
  {
    return children.indexOf(node);
  }

  @Override
  public boolean hasChildren()
  {
    return childrenCount() > 0;
  }

  @Override
  public boolean isChild(final AbstractWbsNodeDO node)
  {
    return children.contains(node);
  }

  /**
   * Swaps the positions of the two given nodes in this node's children list. If one of the given nodes is not a child of this node, a
   * {@link WbsNodeIsNotAChildException} is thrown.
   * @param childA First child
   * @param childB Second child
   * @throws WbsNodeIsNotAChildException
   */
  public void swapChildren(final AbstractWbsNodeDO childA, final AbstractWbsNodeDO childB)
  {
    final int childAIndex = getChildIndex(childA);
    final int childBIndex = getChildIndex(childB);

    if (childAIndex < 0)
      throw new WbsNodeIsNotAChildException(childA, this);
    if (childBIndex < 0)
      throw new WbsNodeIsNotAChildException(childB, this);
    if (childAIndex == childBIndex)
      return;

    final AbstractWbsNodeDO tmp = children.get(childAIndex);
    children.set(childAIndex, children.get(childBIndex));
    children.set(childBIndex, tmp);
  }

  private void ensureChildIsNotInList(final AbstractWbsNodeDO newNode)
  {
    if (children.contains(newNode))
      throw new IllegalArgumentException("Cannot add child. Node is already contained in list.");
  }

  private void ensureChildInsertible(final AbstractWbsNodeDO newNode)
  {
    if (!childIsOfValidType(newNode))
      throw new IllegalArgumentException("Cannot add child. Illegal class: " + Hibernate.getClass(newNode));
  }

  /**
   * Returns a filtered list of children. Either, only children that match or do not match the specified classes are included in the list.
   * The returned list is a filtered copy of the internal list. List operations on the returned list therefore have no effect on children of
   * this node.
   * @param filter A {@link WbsNodeFilter} specifying the filtering behavior.
   * @return The list of filtered children
   */
  public List<AbstractWbsNodeDO> getFilteredChildren(final WbsNodeFilter filter)
  {
    final ArrayList<AbstractWbsNodeDO> filteredList = new ArrayList<AbstractWbsNodeDO>();
    for (final AbstractWbsNodeDO child : children) {
      if (filter.isAllowed(child))
        filteredList.add(child);
    }
    return filteredList;
  }

  @OneToOne
  @JoinColumn(name = "struct_el_fk")
  public TaskDO getTaskDo()
  {
    return structureElementDO;
  }

  @Column(name = "wbs_code", length = 1000)
  @Override
  public String getWbsCode()
  {
    return wbsCode;
  }

  public void setWbsCode(final String wbsCode)
  {
    this.wbsCode = wbsCode;
  }

  @Override
  @Transient
  public String getTitle()
  {
    checkTaskDoPresence();
    return structureElementDO.getTitle();
  }

  protected void checkTaskDoPresence()
  {
    if (structureElementDO == null) {
      throw new IllegalStateException("taskDo must be set at this state");
    }
  }

  public void setTitle(final String title)
  {
    checkTaskDoPresence();
    structureElementDO.setTitle(title);
  }

  public void setShortDescription(final String shortDescription)
  {
    checkTaskDoPresence();
    structureElementDO.setShortDescription(shortDescription);
  }

  @Transient
  public String getShortDescription()
  {
    checkTaskDoPresence();
    return structureElementDO.getShortDescription();
  }

  public void setDescription(final String description)
  {
    checkTaskDoPresence();
    structureElementDO.setDescription(description);
  }

  @Transient
  public String getDescription()
  {
    checkTaskDoPresence();
    return structureElementDO.getDescription();
  }

  @Transient
  public TaskStatus getStatus()
  {
    checkTaskDoPresence();
    return structureElementDO.getStatus();
  }

  public void setStatus(final TaskStatus status)
  {
    checkTaskDoPresence();
    structureElementDO.setStatus(status);
  }

  @Transient
  public Priority getPriority()
  {
    checkTaskDoPresence();
    return structureElementDO.getPriority();
  }

  public void setPriority(final Priority priority)
  {
    checkTaskDoPresence();
    structureElementDO.setPriority(priority);
  }

  @SuppressWarnings("deprecation")
  @Override
  @Transient
  public int getProgress()
  {
    checkTaskDoPresence();
    final Integer progress = structureElementDO.getProgress();
    if (progress == null)
      return 0;
    return progress;
  }

  @SuppressWarnings("deprecation")
  public void setProgress(final int progress)
  {
    checkTaskDoPresence();
    structureElementDO.setProgress(progress);
  }

  @Override
  @Transient
  public boolean isDeleted()
  {
    // update the deleted status of this WBSNodeDO automagically if it changed in TaskDO (i.e. user deleted it through task view)
    if (structureElementDO != null && structureElementDO.isDeleted() != super.isDeleted())
      super.setDeleted(structureElementDO.isDeleted());
    return super.isDeleted();
  }

  @Override
  public void setDeleted(final boolean deleted)
  {
    super.setDeleted(deleted);
    if (structureElementDO != null) // must not throw exception here, otherwise, Hibernate cannot create objects
      structureElementDO.setDeleted(deleted);
  }

  /**
   * @return true, if any node on the path to the root is marked as deleted
   */
  @Transient
  public boolean isTransitivelyDeleted()
  {
    AbstractWbsNodeDO node = this;
    while (node != null) {
      if (node.isDeleted())
        return true;
      node = node.getParent();
    }
    return false;
  }

  @Override
  @ManyToOne
  @JoinColumn(name = "parent_id", updatable = false, insertable = false, nullable = true)
  public AbstractWbsNodeDO getParent()
  {
    return parent;
  }

  /**
   * @return The user responsible for the project
   * @throws IllegalStateException if no TaskDO object is set
   */
  @Transient
  public PFUserDO getResponsibleUser()
  {
    checkTaskDoPresence();
    return this.getTaskDo().getResponsibleUser();
  }

  /**
   * Set a new user as responsible user for the project
   * @param newUser The new responsible user
   * @throws IllegalStateException if no TaskDO object is set
   */
  public void setResponsibleUser(final PFUserDO newUser)
  {
    checkTaskDoPresence();
    getTaskDo().setResponsibleUser(newUser);
  }

  @Override
  public String toString()
  {
    // return getTitle() + ", Code: " + getWbsCode()+
    // ", Parent id: "+(parent==null?"null":parent.getId())+"\nchildren:\n"+children.toString()+"\n";
    return getTitle() + ", Code: " + getWbsCode() + ", Parent id: " + (parent == null ? "null" : parent.getId()); // "+"\nchildren:\n"+children.toString()+"\n";
  }

  @Override
  public void accept(final IWbsNodeVisitor visitor)
  {
    visitor.visit(this);
  }

  @Override
  public boolean equals(final Object obj)
  {
    if (obj instanceof AbstractWbsNodeDO) {
      if (getId() != null)
        return getId().equals(((AbstractWbsNodeDO) obj).getId()) && this.getClass().equals(obj.getClass());
    }
    return super.equals(obj);
  }

  public int autoIncrementAndGet()
  {
    autoIncrementChildren++;
    return autoIncrementChildren;
  }

  @JoinColumn(name = "phase_fk", nullable = true)
  public PhaseDO getPhase()
  {
    AbstractWbsNodeDO maybeParent;
    if (phaseDO == null) {
      maybeParent = getParent();
      return maybeParent == null ? null : maybeParent.getPhase();
    }
    return phaseDO;
  }

  public void setPhase(final PhaseDO phaseDO)
  {
    this.phaseDO = phaseDO;
  }

  // -----------------------------------------------------------
  // below are some private setters/getters needed by Hibernate
  // -----------------------------------------------------------

  void setTaskDo(final TaskDO taskDO)
  {
    this.structureElementDO = taskDO;
  }

  void setParent(final AbstractWbsNodeDO parent)
  {
    this.parent = parent;
  }

  @OneToMany
  @JoinColumn(name = "parent_id", nullable = true)
  @OrderColumn(name = "parent_index")
  private List<AbstractWbsNodeDO> getChildren()
  {
    return children;
  }

  @SuppressWarnings("unused")
  private void setChildren(final List<AbstractWbsNodeDO> children)
  {
    this.children = children;
  }

  @Column(name = "auto_increment_children", nullable = false)
  public int getAutoIncrementChildren()
  {
    return autoIncrementChildren;
  }

  public void setAutoIncrementChildren(final int autoIncrementChildren)
  {
    this.autoIncrementChildren = autoIncrementChildren;
  }

  public void setPlanningStatus(final PlanningStatus planningStatus)
  {
    this.planningStatus = planningStatus;
  }

  @Column(name = "planning_status", nullable = false, columnDefinition = "varchar(255) default 'PLANNING'")
  @Enumerated(EnumType.STRING)
  public PlanningStatus getPlanningStatus()
  {
    return planningStatus;
  }

  @Override
  public ModificationStatus copyValuesFrom(final BaseDO< ? extends Serializable> src, final String... ignoreFields)
  {
    ModificationStatus status = super.copyValuesFrom(src, ignoreFields);
    // ProjectForge's super method does not handle insert and swap operations of Lists correctly (order is not maintained)
    // put entries in the children list into their correct order
    if (!ArrayUtils.contains(ignoreFields, "children") && src instanceof AbstractWbsNodeDO) {
      final AbstractWbsNodeDO srcNode = (AbstractWbsNodeDO) src;
      if (srcNode.children.size() != children.size())
        throw new RuntimeException(
            "Error in method super.copyValuesFrom(). Children lists of Src and this should already have the same length at this point.");

      for (int i = 0; i < children.size(); i++) {
        if (!children.get(i).equals(srcNode.children.get(i))) {
          status = ModificationStatus.MAJOR;
          children.set(i, srcNode.children.get(i));
        }
      }
    }
    return status;
  }

}
