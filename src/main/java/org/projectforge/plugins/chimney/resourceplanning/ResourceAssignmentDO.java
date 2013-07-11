/////////////////////////////////////////////////////////////////////////////
//
// Project   ProjectForge - Chimney Plugin
//
// Copyright 2012, Micromata GmbH
//           All rights reserved.
//
/////////////////////////////////////////////////////////////////////////////

package org.projectforge.plugins.chimney.resourceplanning;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
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
import org.projectforge.plugins.chimney.web.components.ChimneyJodaPeriodConverter;
import org.projectforge.user.PFUserContext;
import org.projectforge.user.PFUserDO;

@Entity
@Indexed
@Table(name = "T_CHIMNEY_RESOURCE_ASSIGNMENT")
public class ResourceAssignmentDO extends DefaultBaseDO
{
  private static final long serialVersionUID = -8536706247102061808L;

  private AbstractWbsNodeDO wbsNode;
  private PFUserDO user;
  private DateTime beginDate;
  private DateTime endDate;
  private Period plannedEffort;

  public ResourceAssignmentDO() {
  }

  public ResourceAssignmentDO(final AbstractWbsNodeDO wbsNode) {
    Validate.notNull(wbsNode);
    this.wbsNode = wbsNode;
  }

  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(nullable = false, name = "wbsNode", unique = false)
  public AbstractWbsNodeDO getWbsNode()
  {
    return wbsNode;
  }

  public void setWbsNode(final AbstractWbsNodeDO wbsNode)
  {
    this.wbsNode = wbsNode;
  }

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  public PFUserDO getUser()
  {
    return user;
  }

  public void setUser(final PFUserDO user)
  {
    this.user = user;
  }

  @Deprecated
  @Column(name = "beginDate")
  @Type(type="org.jadira.usertype.dateandtime.joda.PersistentDateTime")
  public DateTime getBeginDate()
  {
    return beginDate;
  }

  @Deprecated
  public void setBeginDate(final DateTime beginDate) throws InconsistentFixedDatesException
  {
    if (!isEndDateGreaterOrEqualBeginDate(beginDate, endDate)) {
      throw new InconsistentFixedDatesException(beginDate, endDate);
    }
    this.beginDate = beginDate;
  }

  @Deprecated
  @Column(name = "fixedEndDate")
  @Type(type="org.jadira.usertype.dateandtime.joda.PersistentDateTime")
  public DateTime getEndDate()
  {
    return endDate;
  }

  @Deprecated
  public void setEndDate(final DateTime endDate) throws InconsistentFixedDatesException
  {
    if (!isEndDateGreaterOrEqualBeginDate(beginDate, endDate)) {
      throw new InconsistentFixedDatesException(beginDate, endDate);
    }
    this.endDate = endDate;
  }

  public boolean isEndDateGreaterOrEqualBeginDate(final DateTime beginDate, final DateTime endDate)
  {
    return beginDate == null || endDate == null || endDate.compareTo(beginDate) >= 0;
  }

  @Column(name = "effortEstimation")
  @Type(type="org.jadira.usertype.dateandtime.joda.PersistentPeriodAsString")
  public Period getPlannedEffort()
  {
    return StandardWorkdayNormalizer.toNormalizedPeriod(plannedEffort);
  }

  public void setPlannedEffort(final Period plannedEffort)
  {
    this.plannedEffort = plannedEffort;
  }

  @Override
  public String toString()
  {
    final PeriodFormatter formatter = ChimneyJodaPeriodConverter.getFormatter();
    String pattern = DateFormats.ISO_DATE;
    if (PFUserContext.getUser() != null)
      pattern = DateFormats.getFormatString(DateFormatType.DATE);

    final String blank = "N/A";
    final String username = (user != null) ? user.getShortDisplayName() : blank;
    final String wbsCodeString = (wbsNode != null) ? wbsNode.getWbsCode() : blank;
    final String beginString = (beginDate != null) ? beginDate.toString(pattern) : blank;
    final String endString = (endDate != null) ? endDate.toString(pattern) : blank;
    final String effortEstimationString = (plannedEffort != null) ? plannedEffort.toString(formatter) : blank;

    return String.format("%s@%s: %s - %s (%s)", username, wbsCodeString, beginString, endString, effortEstimationString);
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }

    if (o == null || !(o instanceof ResourceAssignmentDO)) {
      return false;
    }

    final ResourceAssignmentDO other = (ResourceAssignmentDO) o;
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

}
