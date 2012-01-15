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

import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.projectforge.core.Priority;
import org.projectforge.xml.stream.XmlField;
import org.projectforge.xml.stream.XmlObject;

/**
 * Used in config.xml for the definition of the used business assessment schema. This object represents a single row of the business
 * assessment.
 * 
 * @author Kai Reinhard (k.reinhard@micromata.de)
 * 
 */
@XmlObject(alias = "row")
public class BusinessAssessmentRowConfig
{
  // <row no="1051" id="gesamtleistung" value="umsatzErloese+bestVerdg+aktEigenleistungen" priority="high" title="Gesamtleistung" />

  @XmlField(asAttribute = true)
  private String no;

  @XmlField(asAttribute = true)
  private String id;

  @XmlField(asAttribute = true)
  private String accountRange;

  private String value;

  private Priority priority;

  @XmlField(asAttribute = true)
  private String title;

  private int indent;

  public BusinessAssessmentRowConfig()
  {
  }

  /**
   * The number has no other functionality than to be displayed.
   * @return Number to display (e. g. 1051).
   */
  public String getNo()
  {
    return no;
  }

  /**
   * @param no the no to set
   * @return this for chaining.
   */
  public BusinessAssessmentRowConfig setNo(final String no)
  {
    this.no = no;
    return this;
  }

  /**
   * The id can be used for referring the row e. g. inside scripts or for calculating values (see {@link #getValue()}).
   * @see #getValue()
   */
  public String getId()
  {
    return id;
  }

  /**
   * @param id the id to set
   * @return this for chaining.
   */
  public BusinessAssessmentRowConfig setId(final String id)
  {
    this.id = id;
    return this;
  }

  /**
   * The amount is calculated by adding all account records of the given account range. The account range is a coma separated list of
   * accounts and account ranges (DATEV accounts) such as "4830,4947", "4000-4799" or "6300,6800-6855".
   * @return the accountRange
   */
  public String getAccountRange()
  {
    return accountRange;
  }

  /**
   * @param accountRange the accountRange to set
   * @return this for chaining.
   */
  public BusinessAssessmentRowConfig setAccountRange(final String accountRange)
  {
    this.accountRange = accountRange;
    return this;
  }

  /**
   * The value is optional and used if the amount of this row has to be calculated. If the string starts with '=' then the value is
   * calculated, e. g. "= resultBeforeTaxes + taxesAndOtherIncomes". resultBeforeTaxes and taxesAndOtherIncomes are id's of rows available
   * as variables. <br/>
   * If the string doesn't start with a '=' the value will be taken as Groovy script and the returned value of this script is taken as
   * amount of this row.
   */
  public String getValue()
  {
    return value;
  }

  /**
   * @param value the value to set
   * @return this for chaining.
   */
  public BusinessAssessmentRowConfig setValue(final String value)
  {
    this.value = value;
    return this;
  }

  /**
   * Priority to display. If a short business assessment is displayed only rows with high priority are shown.
   * @return
   */
  public Priority getPriority()
  {
    return priority;
  }

  /**
   * @param priority the priority to set
   * @return this for chaining.
   */
  public BusinessAssessmentRowConfig setPriority(final Priority priority)
  {
    this.priority = priority;
    return this;
  }

  /**
   * The title will be displayed.
   */
  public String getTitle()
  {
    return title;
  }

  /**
   * @param title the title to set
   * @return this for chaining.
   */
  public BusinessAssessmentRowConfig setTitle(final String title)
  {
    this.title = title;
    return this;
  }

  /**
   * Only for indention when displaying this row.
   * @return the indent
   */
  public int getIndent()
  {
    return indent;
  }

  /**
   * @param indent the indent to set
   * @return this for chaining.
   */
  public BusinessAssessmentRowConfig setIndent(final int indent)
  {
    this.indent = indent;
    return this;
  }

  @Override
  public String toString()
  {
    final ReflectionToStringBuilder builder = new ReflectionToStringBuilder(this);
    return builder.toString();
  }
}
