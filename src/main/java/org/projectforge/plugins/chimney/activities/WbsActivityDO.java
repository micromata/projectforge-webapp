/////////////////////////////////////////////////////////////////////////////
//
// Project   ProjectForge - Chimney Plugin
//
// Copyright 2012, Micromata GmbH
//           All rights reserved.
//
/////////////////////////////////////////////////////////////////////////////

package org.projectforge.plugins.chimney.activities;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.apache.commons.lang.Validate;
import org.hibernate.annotations.Type;
import org.hibernate.search.annotations.Indexed;
import org.joda.time.DateTime;
import org.joda.time.Period;
import org.joda.time.format.PeriodFormatter;
import org.projectforge.common.DateFormatType;
import org.projectforge.common.DateFormats;
import org.projectforge.core.DefaultBaseDO;
import org.projectforge.plugins.chimney.utils.date.InconsistentFixedDatesException;
import org.projectforge.plugins.chimney.utils.date.StandardWorkdayNormalizer;
import org.projectforge.plugins.chimney.wbs.AbstractWbsNodeDO;
import org.projectforge.plugins.chimney.wbs.MilestoneDO;
import org.projectforge.plugins.chimney.web.components.ChimneyJodaPeriodConverter;

@Entity
@Indexed
@Table(name = "T_WBS_ACTIVITY")
public class WbsActivityDO extends DefaultBaseDO implements IActivityReadOnly<DependencyRelationDO>
{
  private static final long serialVersionUID = -8536706247102061808L;

  private AbstractWbsNodeDO wbsNode;
  private DateTime fixedBeginDate;
  private DateTime fixedEndDate;
  private Period effortEstimation;
  private Set<DependencyRelationDO> predecessorRelations = new HashSet<DependencyRelationDO>();
  private Set<DependencyRelationDO> successorRelations = new HashSet<DependencyRelationDO>();

  public WbsActivityDO() {
  }

  public WbsActivityDO(final AbstractWbsNodeDO wbsNode) {
    Validate.notNull(wbsNode);
    this.wbsNode = wbsNode;
  }

  @OneToOne
  @JoinColumn(nullable = false, name = "wbsNode", unique = true)
  public AbstractWbsNodeDO getWbsNode()
  {
    return wbsNode;
  }

  public void setWbsNode(final AbstractWbsNodeDO wbsNode)
  {
    this.wbsNode = wbsNode;
  }

  @Override
  @Column(name = "fixedBeginDate")
  @Type(type="org.jadira.usertype.dateandtime.joda.PersistentDateTime")
  public DateTime getFixedBeginDate()
  {
    return fixedBeginDate;
  }


  public void setFixedBeginDate(final DateTime beginDate) throws InconsistentFixedDatesException
  {
    if (!isEndDateGreaterOrEqualBeginDate(beginDate, fixedEndDate)) {
      throw new InconsistentFixedDatesException(beginDate, fixedEndDate);
    }
    this.fixedBeginDate = beginDate;
  }

  @Override
  @Column(name = "fixedEndDate")
  @Type(type="org.jadira.usertype.dateandtime.joda.PersistentDateTime")
  public DateTime getFixedEndDate()
  {
    return fixedEndDate;
  }

  public void setFixedEndDate(final DateTime endDate) throws InconsistentFixedDatesException
  {
    if (!isEndDateGreaterOrEqualBeginDate(fixedBeginDate, endDate)) {
      throw new InconsistentFixedDatesException(fixedBeginDate, endDate);
    }
    fixedEndDate = endDate;
  }

  public boolean isEndDateGreaterOrEqualBeginDate(final DateTime beginDate, final DateTime endDate)
  {
    return beginDate == null || endDate == null || endDate.compareTo(beginDate) >= 0;
  }

  @Override
  @Column(name = "effortEstimation")
  @Type(type="org.jadira.usertype.dateandtime.joda.PersistentPeriodAsString")
  public Period getEffortEstimation()
  {
    final AbstractWbsNodeDO maybeWbsNode = getWbsNode();
    if (maybeWbsNode != null && maybeWbsNode instanceof MilestoneDO) {
      return Period.ZERO;
    }
    return StandardWorkdayNormalizer.toNormalizedPeriod(effortEstimation);
  }

  public void setEffortEstimation(final Period effortEstimation)
  {
    this.effortEstimation = effortEstimation;
  }

  @Override
  public Iterator<DependencyRelationDO> predecessorRelationsIterator() {
    return getPredecessorRelations().iterator();
  }
  @Override
  public Iterator<DependencyRelationDO> successorRelationsIterator() {
    return getSuccessorRelations().iterator();
  }

  @OneToMany(mappedBy = "successor")
  public Set<DependencyRelationDO> getPredecessorRelations()
  {
    Iterator<DependencyRelationDO> dependencyIt;
    for (dependencyIt=predecessorRelations.iterator(); dependencyIt.hasNext();  ) {
      if (dependencyIt.next().isDeleted())
        dependencyIt.remove();
    }
    return predecessorRelations;
  }


  @OneToMany(mappedBy = "predecessor")
  public Set<DependencyRelationDO> getSuccessorRelations()
  {
    Iterator<DependencyRelationDO> dependencyIt;
    for (dependencyIt=successorRelations.iterator(); dependencyIt.hasNext();  ) {
      if (dependencyIt.next().isDeleted())
        dependencyIt.remove();
    }
    return successorRelations;
  }

  /**
   * Called by DependencyRelationDO to update the predecessor list. Do not call directly.
   */
  protected void addPredecessorRelation(final DependencyRelationDO predecessorRelation) {
    if (!predecessorRelations.add(predecessorRelation))
      throw new IllegalStateException("Tried to add a predecessor dependency relation that has been added before. Looks like you hit a bug.");
  }

  /**
   * Called by DependencyRelationDao to update the predecessor list. Do not call directly.
   */
  protected void removePredecessorRelation(final DependencyRelationDO predecessorRelation) {
    if (!predecessorRelations.remove(predecessorRelation))
      throw new IllegalStateException("Tried to remove a predecessor dependency relation that does not exist. Looks like you hit a bug.");
  }

  /**
   * Called by DependencyRelationDO to update the successor list. Do not call directly.
   */
  protected void addSuccessorRelation(final DependencyRelationDO successorRelation) {
    if (!successorRelations.add(successorRelation))
      throw new IllegalStateException("Tried to add a successor dependency relation that has been added before. Looks like you hit a bug.");
  }

  /**
   * Called by DependencyRelationDao to update the predecessor list. Do not call directly.
   */
  protected void removeSuccessorRelation(final DependencyRelationDO successorRelation) {
    if (!successorRelations.remove(successorRelation))
      throw new IllegalStateException("Tried to remove a successor dependency relation that does not exist. Looks like you hit a bug.");
  }

  @Override
  public String toString()
  {
    final PeriodFormatter formatter = ChimneyJodaPeriodConverter.getFormatter();
    final String pattern = DateFormats.getFormatString(DateFormatType.DATE);

    final String blank = "_";
    final String wbsCodeString = (wbsNode != null) ? wbsNode.getWbsCode() : blank;
    final String fixedBeginString = (fixedBeginDate != null) ? fixedBeginDate.toString(pattern) : blank;
    final String fixedEndString = (fixedEndDate != null) ? fixedEndDate.toString(pattern) : blank;
    final String effortEstimationString = (effortEstimation != null) ? effortEstimation.toString(formatter) : blank;

    return String.format("[%s]: %s - %s (%s)", wbsCodeString, fixedBeginString, fixedEndString, effortEstimationString);
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }

    if (o == null || !(o instanceof WbsActivityDO)) {
      return false;
    }

    final WbsActivityDO other = (WbsActivityDO) o;
    final Integer thisId = getId();
    final Integer otherId = other.getId();
    if (thisId != null && otherId != null) {
      return thisId.equals(otherId);
    }

    return false;
  }

  @Override
  public int hashCode() {
    if (getId() != null) {
      return getId();
    }
    return super.hashCode();
  }


  // ---- Hibernate setters -----

  @SuppressWarnings("unused")
  private void setPredecessorRelations(final Set<DependencyRelationDO> predecessorRelations)
  {
    this.predecessorRelations = predecessorRelations;
  }

  @SuppressWarnings("unused")
  private void setSuccessorRelations(final Set<DependencyRelationDO> successorRelations)
  {
    this.successorRelations = successorRelations;
  }

  public boolean hasTransitiveSuccessor(final WbsActivityDO activity)
  {
    if (hasDirectSuccessor(activity)) {
      return true;
    }

    final Set<DependencyRelationDO> successorRelations = getSuccessorRelations();
    for (final DependencyRelationDO dependencyRelation: successorRelations) {
      if (dependencyRelation.getSuccessor().hasTransitiveSuccessor(activity)) {
        return true;
      }
    }

    return false;
  }

  public boolean hasDirectSuccessor(final WbsActivityDO activity)
  {
    final Set<DependencyRelationDO> successorRelations = getSuccessorRelations();
    for (final DependencyRelationDO dependencyRelation: successorRelations) {
      if (dependencyRelation.getSuccessor().equals(activity)) {
        return true;
      }
    }
    return false;
  }

  public boolean hasDirectPredecessor(final WbsActivityDO activity)
  {
    final Set<DependencyRelationDO> predecessorRelations = getPredecessorRelations();
    for (final DependencyRelationDO dependencyRelation: predecessorRelations) {
      if (dependencyRelation.getPredecessor().equals(activity)) {
        return true;
      }
    }
    return false;
  }

  public boolean hasTransitivePredecessor(final WbsActivityDO activity)
  {
    if (hasDirectPredecessor(activity)) {
      return true;
    }

    final Set<DependencyRelationDO> predecessorRelations = getPredecessorRelations();
    for (final DependencyRelationDO dependencyRelation: predecessorRelations) {
      if (dependencyRelation.getPredecessor().hasTransitivePredecessor(activity)) {
        return true;
      }
    }

    return false;
  }
}
