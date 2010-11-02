/////////////////////////////////////////////////////////////////////////////
//
// Project ProjectForge Community Edition
//         www.projectforge.org
//
// Copyright (C) 2001-2010 Kai Reinhard (k.reinhard@me.com)
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

package org.projectforge.web.common;

import java.io.IOException;
import java.util.Date;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;

import org.projectforge.common.DateHelper;
import org.projectforge.web.calendar.DateTimeFormatter;
import org.projectforge.web.core.JspTag;


/**
 * Displays a date with the user's locale and time zone.
 * @author Kai Reinhard (k.reinhard@micromata.de)
 * 
 */
public class DateTag extends JspTag
{
  private static final long serialVersionUID = 2613178021108575277L;

  private Date date;

  private String year;

  private String precision = "second";

  private String type = "timestamp";

  private DateTimeFormatter dateTimeFormatter;

  private String var;

  @Override
  public int doStartTag() throws JspException
  {
    init();
    if (date == null) {
      return SKIP_BODY;
    }
    String str = null;
    try {
      String pattern = null;
      if ("date".equals(type) == true) {
        if ("short".equals(year) == true) {
          pattern = DateTimeFormatter.I18N_KEY_SHORT_DATE_FORMAT;
        } else if ("none".equals(year) == true) {
          pattern = DateTimeFormatter.I18N_KEY_DATE_NONE_YEAR_FORMAT;
        } else {
          pattern = DateTimeFormatter.I18N_KEY_DATE_FORMAT;
        }
      } else if ("timestamp".equals(type) == true) {
        if ("short".equals(year) == true) {
          if ("second".equals(precision) == true) {
            pattern = DateTimeFormatter.I18N_KEY_SHORT_TIMESTAMP_FORMAT;
          } else if ("minute".equals(precision) == true) {
            pattern = DateTimeFormatter.I18N_KEY_SHORT_TIMESTAMP_FORMAT_WITH_MINUTES;
          } else {
            throwUnsupportedPrecisionException();
          }
        } else {
          if ("second".equals(precision) == true) {
            pattern = DateTimeFormatter.I18N_KEY_TIMESTAMP_FORMAT;
          } else if ("minute".equals(precision) == true) {
            pattern = DateTimeFormatter.I18N_KEY_TIMESTAMP_FORMAT_WITH_MINUTES;
          } else {
            throwUnsupportedPrecisionException();
          }
        }
      } else if ("timeOfDay".equals(type) == true) {
        pattern = DateTimeFormatter.I18N_KEY_TIMEOFDAY_FORMAT;
      } else if ("utc".equals(type) == true) {
        str = DateHelper.TECHNICAL_ISO_UTC.get().format(date);
      } else {
        throw new UnsupportedOperationException("type '" + type + "' not supported by tag pf:date");
      }
      if (str == null) {
        str = dateTimeFormatter.getFormattedDateTime(date, pattern);
      }
      if (var != null) {
        pageContext.setAttribute(var, str, PageContext.PAGE_SCOPE);
      } else {
        pageContext.getOut().write(str);
      }
    } catch (IOException ex) {
      throw new JspException(ex);
    }
    return SKIP_BODY;
  }

  private String throwUnsupportedPrecisionException()
  {
    throw new UnsupportedOperationException("precision '"
        + precision
        + "' not supported by tag pf:date. Supported values are: \"minute\" and \"second\".");
  }

  @Override
  public void release()
  {
    super.release();
    date = null;
    precision = "seconds";
    type = "timestamp";
    year = null;
    var = null;
  }

  /**
   * If set then the date is written without time of day.
   * @param date
   */
  public void setDate(Date date)
  {
    this.date = date;
  }

  /**
   * Precision of date for type "timestamp" and "timeOfDay": "minute" or "second".
   * @param precision
   */
  public void setPrecision(String precision)
  {
    this.precision = precision;
  }

  /**
   * Supported types: "date", "timestamp", "timeOfDay".
   * @param type
   */
  public void setType(String type)
  {
    this.type = type;
  }

  /**
   * Supports:<br/>
   * <ul>
   * <li> short: only 2 digits will be shown.</li>
   * <li> none: no output of year.</li>
   * </ul>
   * If not given then the year will be shown with 4 digits.
   * @param year
   */
  public void setYear(String year)
  {
    this.year = year;
  }

  /**
   * No output. Assign the formatted date to the given scripting variable.
   * @param var
   */
  public void setVar(String var)
  {
    this.var = var;
  }

  public void setDateTimeFormatter(DateTimeFormatter dateTimeFormatter)
  {
    this.dateTimeFormatter = dateTimeFormatter;
  }
}
