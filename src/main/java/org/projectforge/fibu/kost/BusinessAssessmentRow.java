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

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.apache.commons.lang.math.IntRange;
import org.projectforge.core.Priority;
import org.projectforge.xml.stream.XmlField;
import org.projectforge.xml.stream.XmlObject;
import org.projectforge.xml.stream.XmlOmitField;

/**
 * Used in config.xml for the definition of the used business assessment schema. This object represents a single row of the business
 * assessment.
 * 
 * @author Kai Reinhard (k.reinhard@micromata.de)
 * 
 */
@XmlObject(alias = "row")
public class BusinessAssessmentRow
{
  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(BusinessAssessmentRow.class);

  // <row no="1051" id="gesamtleistung" value="umsatzErloese+bestVerdg+aktEigenleistungen" priority="high" title="Gesamtleistung" />

  @XmlOmitField
  List<IntRange> accountNumberRanges;

  @XmlOmitField
  List<Integer> accountNumbers;

  @XmlField(asAttribute = true)
  private String no;

  @XmlField(asAttribute = true)
  private String id;

  @XmlField(asAttribute = true, alias = "accountRange")
  private String accountRangeConfig;

  @XmlField(alias = "value")
  private String valueConfig;

  private Priority priority;

  @XmlField(asAttribute = true)
  private String title;

  private int indent;

  @XmlOmitField
  private boolean initialized;

  public BusinessAssessmentRow()
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
   * The id can be used for referring the row e. g. inside scripts or for calculating values (see {@link #getValue()}).
   * @see #getValue()
   */
  public String getId()
  {
    return id;
  }

  /**
   * The amount is calculated by adding all account records of the given account range. The account range is a coma separated list of
   * accounts and account ranges (DATEV accounts) such as "4830,4947", "4000-4799" or "6300,6800-6855".
   * @return the accountRange
   */
  public String getAccountRangeConfig()
  {
    return accountRangeConfig;
  }

  /**
   * The value is optional and used if the amount of this row has to be calculated. If the string starts with '=' then the value is
   * calculated, e. g. "= resultBeforeTaxes + taxesAndOtherIncomes". resultBeforeTaxes and taxesAndOtherIncomes are id's of rows available
   * as variables. <br/>
   * If the string doesn't start with a '=' the value will be taken as Groovy script and the returned value of this script is taken as
   * amount of this row.
   */
  public String getValueConfig()
  {
    return valueConfig;
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
   * The title will be displayed.
   */
  public String getTitle()
  {
    return title;
  }

  /**
   * /** Only for indention when displaying this row.
   * @return the indent
   */
  public int getIndent()
  {
    return indent;
  }

  /**
   * @return the accountNumberRanges
   */
  public List<IntRange> getAccountNumberRanges()
  {
    initialize();
    return accountNumberRanges;
  }

  /**
   * @return the accountNumbers
   */
  public List<Integer> getAccountNumbers()
  {
    initialize();
    return accountNumbers;
  }

  @Override
  public String toString()
  {
    final ReflectionToStringBuilder builder = new ReflectionToStringBuilder(this);
    return builder.toString();
  }

  /**
   * Extract the account ranges of the configured accountRage at set the ranges. Examples: "4830,4947", "4000-4799" or "6300,6800-6855"
   */
  private synchronized void initialize()
  {
    if (initialized == true) {
      return;
    }
    accountNumberRanges = new ArrayList<IntRange>();
    accountNumbers = new ArrayList<Integer>();
    if (StringUtils.isBlank(accountRangeConfig) == true) {
      // No account ranges given.
      return;
    }
    final String[] ranges = StringUtils.split(accountRangeConfig, ";,");
    for (final String range : ranges) {
      if (StringUtils.isBlank(range) == true) {
        // No account range given.
        continue;
      }
      final String str = range.trim();
      if (str.indexOf('-') >= 0) {
        final String[] numbers = StringUtils.split(str, "-");
        if (numbers == null || numbers.length != 2) {
          log.warn("Couldn't parse number range of businessAssessmentRow '" + accountRangeConfig + "'.");
        } else {
          try {
            accountNumberRanges.add(new IntRange(new Integer(numbers[0].trim()), new Integer(numbers[1].trim())));
          } catch (final NumberFormatException ex) {
            log.warn("Couldn't parse number range of businessAssessmentRow '" + accountRangeConfig + "':" + ex.getMessage(), ex);
          }
        }
      } else {
        try {
          accountNumbers.add(new Integer(str));
        } catch (final NumberFormatException ex) {
          log.warn("Couldn't parse number range of businessAssessmentRow '" + accountRangeConfig + "':" + ex.getMessage(), ex);
        }
      }
    }
    initialized = true;
  }
}
