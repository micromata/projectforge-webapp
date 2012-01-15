/////////////////////////////////////////////////////////////////////////////
//
// Project ProjectForge Community Edition
//         www.projectforge.org
//
// Copyright (C) 2001-2012 Kai Reinhard (k.reinhard@micromata.com)
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

package org.projectforge.fibu.kost;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.IntRange;
import org.projectforge.core.CurrencyFormatter;
import org.projectforge.core.Priority;

/**
 * Used in config.xml for the definition of the used business assessment schema. This object represents a single row of the business
 * assessment.
 * 
 * @author Kai Reinhard (k.reinhard@micromata.de)
 * 
 */
public class BusinessAssessmentRow implements Serializable
{
  private static final long serialVersionUID = -5192131633290561520L;

  private final BusinessAssessment bussinessAssessment;

  private final BusinessAssessmentRowConfig config;

  private List<BuchungssatzDO> accountRecords;

  private BigDecimal amount;

  public BusinessAssessmentRow(final BusinessAssessment bussinessAssessment, final BusinessAssessmentRowConfig config)
  {
    this.bussinessAssessment = bussinessAssessment;
    this.config = config;
  }

  /**
   * @param accountNumber
   * @return true if the given account number matches the account number ranges of this row.
   */
  public boolean doesMatch(final int accountNumber)
  {
    if (config.getAccountNumbers() != null) {
      for (final Integer no : config.accountNumbers) {
        if (no != null && no.intValue() == accountNumber) {
          return true;
        }
      }
    }
    if (config.getAccountNumberRanges() != null) {
      for (final IntRange range : config.accountNumberRanges) {
        if (range != null && range.containsInteger(accountNumber) == true) {
          return true;
        }
      }
    }
    return false;
  }

  /**
   * Removes any previous existing Buchungssatz.
   * @param value If true then all account records will be stored, if added via addAccountRecord. Otherwise no records are stored.
   */
  public void setStoreAccountRecords(final boolean value)
  {
    if (value == true) {
      this.accountRecords = new ArrayList<BuchungssatzDO>();
    } else {
      this.accountRecords = null;
    }
  }

  /**
   * Addiert den Kontoumsatz und falls setStoreBuchungsaetze(true) gesetzt wurde, wird der Buchungssatz intern hinzugefügt.
   * @param satz
   */
  public void addAccountRecord(final BuchungssatzDO record)
  {
    if (amount == null) {
      amount = BigDecimal.ZERO;
    }
    amount = amount.add(record.getBetrag());
    if (this.accountRecords != null) {
      this.accountRecords.add(record);
    }
  }

  /**
   * @return the amount
   */
  public BigDecimal getAmount()
  {
    return amount;
  }

  /**
   * @return the accountRecords if stored otherwise null.
   */
  public List<BuchungssatzDO> getAccountRecords()
  {
    return accountRecords;
  }

  /**
   * @return the bussinessAssessment of which this row is part of.
   */
  public BusinessAssessment getBussinessAssessment()
  {
    return bussinessAssessment;
  }

  /**
   * The number has no other functionality than to be displayed.
   * @return Number to display (e. g. 1051).
   */
  public String getNo()
  {
    return config.getNo();
  }

  /**
   * The id can be used for referring the row e. g. inside scripts or for calculating values (see {@link #getValue()}).
   * @see #getValue()
   */
  public String getId()
  {
    return config.getId();
  }

  /**
   * Priority to display. If a short business assessment is displayed only rows with high priority are shown.
   * @return
   */
  public Priority getPriority()
  {
    return config.getPriority();
  }

  /**
   * The title will be displayed.
   */
  public String getTitle()
  {
    return config.getTitle();
  }

  /**
   * /** Only for indention when displaying this row.
   * @return the indent
   */
  public int getIndent()
  {
    return config.getIndent();
  }

  /**
   * @return the accountNumberRanges
   */
  public List<IntRange> getAccountNumberRanges()
  {
    return config.getAccountNumberRanges();
  }

  /**
   * @return the accountNumbers
   */
  public List<Integer> getAccountNumbers()
  {
    return config.getAccountNumbers();
  }

  @Override
  public String toString()
  {
    return StringUtils.leftPad(getNo(), 4)
        + " "
        + StringUtils.rightPad(getTitle(), 20)
        + " "
        + StringUtils.leftPad(CurrencyFormatter.format(getAmount()), 18);
    /*
     * StringBuffer buf = new StringBuffer(); buf.append(row); for (KontoUmsatz umsatz : kontoUmsaetze) { buf.append("\n ");
     * buf.append(umsatz.toString()); } return buf.toString();
     */
  }

}
