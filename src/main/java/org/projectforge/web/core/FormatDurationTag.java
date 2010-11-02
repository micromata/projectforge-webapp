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

package org.projectforge.web.core;

import java.io.IOException;

import javax.servlet.jsp.JspException;

import org.projectforge.calendar.TimePeriod;
import org.projectforge.web.calendar.DateTimeFormatter;


/**
 * Displays a info icon with a tooltipp.
 * @author Kai Reinhard (k.reinhard@micromata.de)
 * 
 * @see DateTimeFormatter#getPrettyFormattedDuration(long, int, int)
 * 
 */
public class FormatDurationTag extends JspTag
{
  private static final long serialVersionUID = 4276610851104596402L;

  private long millis;

  private int hoursOfDay = DateTimeFormatter.DEFAULT_HOURS_OF_DAY;
  
  private int minHours4DaySeparation = DateTimeFormatter.DEFAULT_MIN_HOURS4DAY_SEPARATION;
  
  private DateTimeFormatter dateTimeFormatter;

  public void setDateTimeFormatter(DateTimeFormatter dateTimeFormatter)
  {
    this.dateTimeFormatter = dateTimeFormatter;
  }

  // private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(TaskPathTag.class);
  @Override
  public int doStartTag() throws JspException
  {
    init();
    try {
      pageContext.getOut().write(dateTimeFormatter.getPrettyFormattedDuration(millis, hoursOfDay, minHours4DaySeparation));
    } catch (IOException ex) {
      throw new JspException(ex);
    }
    return SKIP_BODY;
  }

  @Override
  public void release()
  {
    super.release();
  }

  /**
   * The i18n key of the tooltipp to show.
   * @param key
   */
  public void setMillis(long millis)
  {
    this.millis = millis;
  }
  
  /**
   * Default is 8 (working day)
   * @param hoursOfDay
   * @see TimePeriod#getDurationFields(long, int, int)
   */
  public void setHoursOfDay(int hoursOfDay)
  {
    this.hoursOfDay = hoursOfDay;
  }
  
  /**
   * Default is 24
   * @param DEFAULT_HOURS_OF_DAY
   * @see TimePeriod#getDurationFields(long, int, int)
   */
  public void setMinHours4DaySeparation(int minHours4DaySeparation)
  {
    this.minHours4DaySeparation = minHours4DaySeparation;
  }
}
