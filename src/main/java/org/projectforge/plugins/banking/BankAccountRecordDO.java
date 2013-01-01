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

package org.projectforge.plugins.banking;

import java.math.BigDecimal;
import java.sql.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.search.annotations.DateBridge;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Index;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.annotations.IndexedEmbedded;
import org.hibernate.search.annotations.Resolution;
import org.hibernate.search.annotations.Store;
import org.projectforge.core.DefaultBaseDO;

import de.micromata.hibernate.history.Historizable;

@Entity
@Indexed
@Table(name = "T_PLUGIN_BANK_ACCOUNT_RECORD")
public class BankAccountRecordDO extends DefaultBaseDO implements Historizable
{
  private static final long serialVersionUID = 2440829270676298481L;

  @IndexedEmbedded(depth = 1)
  private BankAccountDO account;

  @DateBridge(resolution = Resolution.DAY)
  private Date date;

  @Field(index = Index.UN_TOKENIZED)
  private BigDecimal amount;

  @Field(index = Index.TOKENIZED, store = Store.NO)
  private String text;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "account_fk", nullable = false)
  public BankAccountDO getAccount()
  {
    return account;
  }

  public void setAccount(final BankAccountDO account)
  {
    this.account = account;
  }

  @Column(name = "date_col", nullable = false)
  public Date getDate()
  {
    return date;
  }

  public void setDate(final Date date)
  {
    this.date = date;
  }

  @Column(nullable = false, scale = 5, precision = 18)
  public BigDecimal getAmount()
  {
    return amount;
  }

  public void setAmount(final BigDecimal amount)
  {
    this.amount = amount;
  }

  @Column(length = 255)
  public String getText()
  {
    return text;
  }

  public void setText(final String text)
  {
    this.text = text;
  }
}
