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

package org.projectforge.plugins.liquidityplanning;

import java.math.BigDecimal;
import java.sql.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Table;

import org.hibernate.search.annotations.DateBridge;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Index;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.annotations.Resolution;
import org.hibernate.search.annotations.Store;
import org.projectforge.common.RecurrenceFrequency;
import org.projectforge.core.DefaultBaseDO;
import org.projectforge.database.Constants;

/**
 * Beside entries of debitors and creditors invoices additional entries (for accommodation, taxes, planned salaries, assurance etc.) are
 * important for a complete liquidity planning.
 * @author Kai Reinhard (k.reinhard@micromata.de)
 * 
 */
@Entity
@Indexed
@Table(name = "T_PLUGIN_LIQUI_ENTRY")
public class LiquidityEntryDO extends DefaultBaseDO
{
  private static final long serialVersionUID = 6006883617791360816L;

  @Field(index = Index.TOKENIZED, store = Store.NO)
  private String subject;

  @Field(index = Index.TOKENIZED, store = Store.NO)
  private String description;

  @Field(index = Index.TOKENIZED, store = Store.NO)
  private String comment;

  @Field(index = Index.UN_TOKENIZED)
  @DateBridge(resolution = Resolution.DAY)
  private Date dueDate;

  @Field(index = Index.UN_TOKENIZED)
  private BigDecimal ammount;

  private RecurrenceFrequency recurranceInterval;

  @Column(length = Constants.LENGTH_TITLE)
  public String getSubject()
  {
    return subject;
  }

  /**
   * @param title
   * @return this for chaining.
   */
  public LiquidityEntryDO setSubject(final String subject)
  {
    this.subject = subject;
    return this;
  }

  @Enumerated(EnumType.STRING)
  @Column(length = 20)
  public RecurrenceFrequency getRecurranceInterval()
  {
    return recurranceInterval;
  }

  /**
   * @return this for chaining.
   */
  public LiquidityEntryDO setRecurranceInterval(final RecurrenceFrequency recurranceInterval)
  {
    this.recurranceInterval = recurranceInterval;
    return this;
  }

  @Column(name = "due_date")
  public Date getDueDate()
  {
    return dueDate;
  }

  /**
   * @return this for chaining.
   */
  public LiquidityEntryDO setDueDate(final Date dueDate)
  {
    this.dueDate = dueDate;
    return this;
  }

  public BigDecimal getAmmount()
  {
    return ammount;
  }

  public LiquidityEntryDO setAmmount(final BigDecimal ammount)
  {
    this.ammount = ammount;
    return this;
  }

  public String getComment()
  {
    return comment;
  }

  @Column(length = Constants.LENGTH_TEXT)
  public LiquidityEntryDO setComment(final String comment)
  {
    this.comment = comment;
    return this;
  }

  public String getDescription()
  {
    return description;
  }

  @Column(length = Constants.LENGTH_TEXT)
  public LiquidityEntryDO setDescription(final String description)
  {
    this.description = description;
    return this;
  }
}
