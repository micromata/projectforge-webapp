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

package org.projectforge.fibu;

import java.math.BigDecimal;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;

import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Index;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.annotations.IndexedEmbedded;
import org.hibernate.search.annotations.Store;
import org.projectforge.common.StringHelper;
import org.projectforge.core.Constants;
import org.projectforge.core.DefaultBaseDO;

/**
 * Das monatliche Gehalt eines festangestellten Mitarbeiters.
 * @author Kai Reinhard (k.reinhard@micromata.de)
 */
@Entity
@Indexed
@Table(name = "T_FIBU_EMPLOYEE_SALARY", uniqueConstraints = { @UniqueConstraint(columnNames = { "employee_id", "year", "month"})})
public class EmployeeSalaryDO extends DefaultBaseDO
{
  private static final long serialVersionUID = -6854150096887750382L;

  @IndexedEmbedded(depth = 2)
  private EmployeeDO employee;

  @Field(index = Index.UN_TOKENIZED, store = Store.NO)
  private Integer year;

  @Field(index = Index.UN_TOKENIZED, store = Store.NO)
  private Integer month;

  @Field(index = Index.UN_TOKENIZED, store = Store.NO)
  private BigDecimal bruttoMitAgAnteil;

  @Field(index = Index.TOKENIZED, store = Store.NO)
  private String comment;

  private EmployeeSalaryType type;

  /**
   * @return Zugehöriger Mitarbeiter.
   */
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "employee_id", nullable = false)
  public EmployeeDO getEmployee()
  {
    return employee;
  }

  public void setEmployee(EmployeeDO employee)
  {
    this.employee = employee;
  }

  @Transient
  public Integer getEmployeeId()
  {
    if (this.employee == null)
      return null;
    return employee.getId();
  }

  /**
   * @return Abrechnungsjahr.
   */
  @Column
  public Integer getYear()
  {
    return year;
  }

  public void setYear(Integer year)
  {
    this.year = year;
  }

  /**
   * @return Abrechnungsmonat.
   */
  @Column
  public Integer getMonth()
  {
    return month;
  }

  public void setMonth(Integer month)
  {
    this.month = month;
  }

  @Transient
  public String getFormattedMonth()
  {
    return StringHelper.format2DigitNumber(month + 1);
  }

  @Transient
  public String getFormattedYearAndMonth()
  {
    return String.valueOf(year) + "-" + StringHelper.format2DigitNumber(month + 1);
  }

  /**
   * Die Bruttoauszahlung an den Arbeitnehmer (inklusive AG-Anteil Sozialversicherungen).
   */
  @Column(name = "brutto_mit_ag_anteil")
  public BigDecimal getBruttoMitAgAnteil()
  {
    return bruttoMitAgAnteil;
  }

  public void setBruttoMitAgAnteil(BigDecimal bruttoMitAgAnteil)
  {
    this.bruttoMitAgAnteil = bruttoMitAgAnteil;
  }

  @Column(length = Constants.COMMENT_LENGTH)
  public String getComment()
  {
    return comment;
  }

  public void setComment(String comment)
  {
    this.comment = comment;
  }

  @Enumerated(EnumType.STRING)
  @Column(length = 20)
  public EmployeeSalaryType getType()
  {
    return type;
  }

  public void setType(EmployeeSalaryType type)
  {
    this.type = type;
  }
}
