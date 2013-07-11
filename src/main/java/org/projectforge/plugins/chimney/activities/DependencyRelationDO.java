/////////////////////////////////////////////////////////////////////////////
//
// Project   ProjectForge - Chimney Plugin
//
// Copyright 2012, Micromata GmbH
//           All rights reserved.
//
/////////////////////////////////////////////////////////////////////////////

package org.projectforge.plugins.chimney.activities;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;

import org.apache.commons.lang.Validate;
import org.hibernate.annotations.Index;
import org.hibernate.annotations.Type;
import org.hibernate.search.annotations.Indexed;
import org.joda.time.DateTime;
import org.joda.time.Period;
import org.projectforge.core.DefaultBaseDO;
import org.projectforge.plugins.chimney.utils.date.StandardWorkdayNormalizer;

@Entity
@Indexed
@Table(name = "T_DEPENDENCY_RELATION", uniqueConstraints = { @UniqueConstraint(columnNames = { "predecessor_id", "successor_id",
"deletedDateTime"}, name = "pred_succ_unique")})
public class DependencyRelationDO extends DefaultBaseDO implements IDependencyRelationReadOnly
{
  private static final long serialVersionUID = 3502200437838799123L;

  private static final Period DEFAULT_OFFSET = new Period(0);

  private static final DependencyRelationType DEFAULT_TYPE = DependencyRelationType.END_BEGIN;

  private Period offset = DEFAULT_OFFSET;

  private DependencyRelationType type = DEFAULT_TYPE;

  private WbsActivityDO predecessor;

  private WbsActivityDO successor;

  // is set to current date/time when dependency gets deleted to allow re-creation of dependency with same predecessor/successor
  private DateTime deletedDateTime = null;

  public DependencyRelationDO()
  {
  }

  public DependencyRelationDO(final WbsActivityDO predecessor, final WbsActivityDO successor)
  {
    this(predecessor, successor, DEFAULT_OFFSET, DEFAULT_TYPE);
  }

  public DependencyRelationDO(final WbsActivityDO predecessor, final WbsActivityDO successor, final Period offset)
  {
    this(predecessor, successor, offset, DEFAULT_TYPE);
  }

  public DependencyRelationDO(final WbsActivityDO predecessor, final WbsActivityDO successor, final DependencyRelationType type)
  {
    this(predecessor, successor, DEFAULT_OFFSET, type);
  }

  public DependencyRelationDO(final WbsActivityDO predecessor, final WbsActivityDO successor, final Period offset,
      final DependencyRelationType type)
  {
    setAndPropagatePredecessor(predecessor);
    setAndPropagateSuccessor(successor);
    setOffset(offset);
    setType(type);
  }

  @Override
  public void setId(final Integer id)
  {
    // remove me from predecessor and successor depRel sets because hashcode() output will change and
    // thus the relation sets in successor/predecessor would incorrectly let contains(this) be false.
    boolean predHadMe = false;
    boolean succHadMe = false;
    if (predecessor != null)
      predHadMe = predecessor.getSuccessorRelations().remove(this);
    if (successor != null)
      succHadMe = successor.getPredecessorRelations().remove(this);

    // change the id
    super.setId(id);

    // readd me if i had been in successor/predecessor before
    if (predHadMe && predecessor != null)
      predecessor.getSuccessorRelations().add(this);
    if (succHadMe && successor != null)
      successor.getPredecessorRelations().add(this);
  }

  @Column
  @Type(type = "org.jadira.usertype.dateandtime.joda.PersistentPeriodAsString")
  public Period getOffset()
  {
    return StandardWorkdayNormalizer.toNormalizedPeriod(offset);
  }

  public void setOffset(final Period offset)
  {
    this.offset = offset;
  }

  @Override
  public DependencyRelationType getType()
  {
    return type;
  }

  public void setType(final DependencyRelationType relationType)
  {
    this.type = relationType;
  }

  public void setAndPropagatePredecessor(final WbsActivityDO newPredecessor)
  {
    Validate.notNull(newPredecessor);
    // remove this relation from old predecessor's successor relation set
    if (predecessor != null)
      predecessor.removeSuccessorRelation(this);
    // remove ourself from the successor's predecessor relation set before changing the predecessor
    // and later readd it because setting a new predessor changes the hashcode() result of this class.
    if (successor != null)
      successor.removePredecessorRelation(this);
    // set new predecessor
    predecessor = newPredecessor;
    // add this relation to new predecessor's successor relation set
    newPredecessor.addSuccessorRelation(this);
    // readd relation to the successor
    if (successor != null)
      successor.addPredecessorRelation(this);
  }

  public void setAndPropagateSuccessor(final WbsActivityDO newSuccessor)
  {
    Validate.notNull(newSuccessor);
    // remove this relation from old successor's predecessor relation set
    if (successor != null)
      successor.removePredecessorRelation(this);
    // remove ourself from the predecessor's successor relation set before changing the successor
    // and later readd it because setting a new successor changes the hashcode() result of this class.
    if (predecessor != null)
      predecessor.removeSuccessorRelation(this);
    // set new successor
    successor = newSuccessor;
    // add this relation to new successor's predecessor relation set
    newSuccessor.addPredecessorRelation(this);
    // readd relation to the predecessor
    if (predecessor != null)
      predecessor.addSuccessorRelation(this);
  }

  @Override
  @ManyToOne(optional = false)
  @JoinColumn(name = "predecessor_id", nullable = false)
  @Index(name = "predecessor_index")
  public WbsActivityDO getPredecessor()
  {
    return predecessor;
  }

  @Override
  @ManyToOne(optional = false)
  @JoinColumn(name = "successor_id", nullable = false)
  @Index(name = "successor_index")
  public WbsActivityDO getSuccessor()
  {
    return successor;
  }

  @Override
  public boolean equals(final Object obj)
  {
    if (this == obj) {
      return true;
    }

    if ((obj == null) || !(obj instanceof DependencyRelationDO)) {
      return false;
    }

    final DependencyRelationDO other = (DependencyRelationDO) obj;
    if (getId() != null && other.getId() != null) {
      return getId().equals(other.getId());
    }

    return false;
  }

  @Override
  public int hashCode()
  {
    if (getId() != null) {
      return getId();
    }
    return super.hashCode();
  }

  public void ensureValid()
  {
    if (isDeleted())
      return;

    if (getPredecessor() == null) {
      throw new PredecessorNullException(this);
    }

    if (getSuccessor() == null) {
      throw new SuccessorNullException(this);
    }

    if (isSelfDependend()) {
      throw new SelfDependencyRelationException(getPredecessor());
    }

    if (isCyclic()) {
      throw new CyclicDependencyRelationException(getPredecessor(), getSuccessor());
    }
  }

  @Transient
  private boolean isCyclic()
  {
    return getPredecessor().hasTransitivePredecessor(getSuccessor());
  }

  @Transient
  private boolean isSelfDependend()
  {
    return getPredecessor().equals(getSuccessor());
  }

  @SuppressWarnings("unused")
  private void setPredecessor(final WbsActivityDO newPredecessor)
  {
    predecessor = newPredecessor;
  }

  @SuppressWarnings("unused")
  private void setSuccessor(final WbsActivityDO newSuccessor)
  {
    successor = newSuccessor;
  }

  @Column(name = "deletedDateTime", nullable = true)
  @Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
  DateTime getDeletedDateTime()
  {
    return deletedDateTime;
  }

  void setDeletedDateTime(final DateTime deletedDateTime)
  {
    this.deletedDateTime = deletedDateTime;
  }

  @Override
  public String toString()
  {
    return predecessor.getWbsNode().getWbsCode() + " - " + successor.getWbsNode().getWbsCode();
  }
}
