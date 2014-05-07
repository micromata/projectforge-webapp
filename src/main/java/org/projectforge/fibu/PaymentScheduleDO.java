/////////////////////////////////////////////////////////////////////////////
//
// Project   ProjectForge
//
// Copyright 2001-2009, Micromata GmbH, Kai Reinhard
//           All rights reserved.
//
/////////////////////////////////////////////////////////////////////////////

package org.projectforge.fibu;

import java.math.BigDecimal;
import java.sql.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.hibernate.search.annotations.DateBridge;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Index;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.annotations.Resolution;
import org.projectforge.core.DefaultBaseDO;
import org.projectforge.core.PropertyInfo;

/**
 * @author Werner Feder (werner.feder@t-online.de)
 *
 */
@Entity
@Indexed
@Table(name = "T_PAYMENTSCHEDULE", uniqueConstraints = { @UniqueConstraint(columnNames = { "auftrag_id", "number"})})
public class PaymentScheduleDO extends DefaultBaseDO
{
  private static final long serialVersionUID = -8024212050762584171L;

  private AuftragDO auftrag;

  private short number;

  @PropertyInfo(i18nKey = "date")
  @Field(index = Index.UN_TOKENIZED)
  @DateBridge(resolution = Resolution.DAY)
  private Date scheduleDate;

  @PropertyInfo(i18nKey = "fibu.common.betrag")
  private BigDecimal amount = null;

  @PropertyInfo(i18nKey = "comment")
  private String comment;

  @PropertyInfo(i18nKey = "fibu.common.reached")
  private boolean reached;

  @PropertyInfo(i18nKey = "fibu.auftrag.vollstaendigFakturiert")
  private boolean vollstaendigFakturiert;

  /**
   * Not used as object due to performance reasons.
   * @return AuftragDO
   */
  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "auftrag_id", nullable = false)
  public AuftragDO getAuftrag()
  {
    return auftrag;
  }

  public PaymentScheduleDO setAuftrag(final AuftragDO auftrag)
  {
    this.auftrag = auftrag;
    return this;
  }

  @Transient
  public Integer getAuftragId()
  {
    if (this.auftrag == null)
      return null;
    return auftrag.getId();
  }

  @Column
  public short getNumber()
  {
    return number;
  }

  public PaymentScheduleDO setNumber(final short number)
  {
    this.number = number;
    return this;
  }
  @Column
  public Date getScheduleDate()
  {
    return scheduleDate;
  }

  public PaymentScheduleDO setScheduleDate(final Date scheduleDate)
  {
    this.scheduleDate = scheduleDate;
    return this;
  }

  @Column(scale = 2, precision = 12)
  public BigDecimal getAmount()
  {
    return amount;
  }

  public PaymentScheduleDO setAmount(final BigDecimal amount)
  {
    this.amount = amount;
    return this;
  }

  @Column
  public String getComment()
  {
    return comment;
  }

  public PaymentScheduleDO setComment(final String comment)
  {
    this.comment = comment;
    return this;
  }

  @Column
  public boolean isReached()
  {
    return reached;
  }

  public PaymentScheduleDO setReached(final boolean reached)
  {
    this.reached = reached;
    return this;
  }

  /**
   * Dieses Flag wird manuell von der FiBu gesetzt und kann nur für abgeschlossene Aufträge gesetzt werden.
   */
  @Column(name = "vollstaendig_fakturiert", nullable = false)
  public boolean isVollstaendigFakturiert()
  {
    return vollstaendigFakturiert;
  }

  public PaymentScheduleDO setVollstaendigFakturiert(final boolean vollstaendigFakturiert)
  {
    this.vollstaendigFakturiert = vollstaendigFakturiert;
    return this;
  }

  @Override
  public boolean equals(final Object o)
  {
    if (o instanceof PaymentScheduleDO) {
      final PaymentScheduleDO other = (PaymentScheduleDO) o;
      if (ObjectUtils.equals(this.getNumber(), other.getNumber()) == false) {
        return false;
      }
      if (ObjectUtils.equals(this.getAuftragId(), other.getAuftragId()) == false) {
        return false;
      }
      return true;
    }
    return false;
  }

  @Override
  public int hashCode()
  {
    final HashCodeBuilder hcb = new HashCodeBuilder();
    hcb.append(getNumber());
    if (getAuftrag() != null) {
      hcb.append(getAuftrag().getId());
    }
    return hcb.toHashCode();
  }

  @Transient
  public boolean isEmpty()
  {
    if (StringUtils.isBlank(comment) == false) {
      return false;
    }
    if (amount != null && amount.compareTo(BigDecimal.ZERO) != 0) {
      return false;
    }
    return (scheduleDate == null);
  }

}
