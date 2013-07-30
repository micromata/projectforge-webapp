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

package org.projectforge.timesheet;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import junit.framework.Assert;

import org.junit.Test;
import org.projectforge.common.DateHelper;
import org.projectforge.user.PFUserContext;
import org.projectforge.user.PFUserDO;

public class TimesheetUtilsTest
{
  private TimeZone timeZone;

  private Locale locale;

  @Test
  public void testBeginOfTimesheets()
  {
    timeZone = DateHelper.EUROPE_BERLIN;
    locale = new Locale("DE_de");
    PFUserContext.setUser(new PFUserDO().setTimeZone(timeZone).setLocale(locale));
    final PFUserDO user1 = new PFUserDO();
    user1.setId(1);
    final PFUserDO user2 = new PFUserDO();
    user1.setId(2);
    final List<TimesheetDO> list = new LinkedList<TimesheetDO>();
    add(list, user1, "2013-07-20 12:15", "2013-07-20 13:00");
    add(list, user2, "2013-07-20 10:00", "2013-07-20 16:00");
    add(list, user2, "2013-07-20 08:00", "2013-07-20 09:00");
    add(list, user2, "2013-07-20 17:00", "2013-07-20 19:00");

    add(list, user1, "2013-07-22 12:00", "2013-07-22 13:00");
    add(list, user1, "2013-07-21 23:00", "2013-07-22 06:00");

    add(list, user1, "2013-07-25 12:00", "2013-07-25 13:00");
    add(list, user1, "2013-07-24 23:00", "2013-07-25 06:00");
    add(list, user1, "2013-07-25 23:00", "2013-07-26 06:00");

    assertTimestamp("2013-07-20 12:15", TimesheetUtils.getBegin(list, parseDate("2013-07-20"), user1.getId()));
    assertTimestamp("2013-07-20 13:00", TimesheetUtils.getEnd(list, parseDate("2013-07-20"), user1.getId()));
    assertTimestamp("2013-07-20 08:00", TimesheetUtils.getBegin(list, parseDate("2013-07-20"), user2.getId()));
    assertTimestamp("2013-07-20 19:00", TimesheetUtils.getEnd(list, parseDate("2013-07-20"), user2.getId()));

    assertTimestamp("2013-07-22 00:00", TimesheetUtils.getBegin(list, parseDate("2013-07-22"), user1.getId()));
    assertTimestamp("2013-07-22 13:00", TimesheetUtils.getEnd(list, parseDate("2013-07-22"), user1.getId()));

    assertTimestamp("2013-07-25 00:00", TimesheetUtils.getBegin(list, parseDate("2013-07-25"), user1.getId()));
    assertEndTimestamp("2013-07-25 23:59", TimesheetUtils.getEnd(list, parseDate("2013-07-25"), user1.getId()));
    Assert.assertNull(TimesheetUtils.getBegin(list, parseDate("2013-07-19"), user1.getId()));
  }

  private void add(final List<TimesheetDO> list, final PFUserDO user, final String start, final String stop)
  {
    final Date startDate = parseTimestamp(start);
    final Date stopDate = parseTimestamp(stop);
    final TimesheetDO ts = new TimesheetDO().setStartDate(startDate).setStopDate(stopDate).setUser(user);
    list.add(ts);
  }

  private void assertTimestamp(final String expected, final Date date)
  {
    Assert.assertNotNull(date);
    Assert.assertEquals(expected + ":00.000", DateHelper.formatIsoTimestamp(date, timeZone));
  }

  private void assertEndTimestamp(final String expected, final Date date)
  {
    Assert.assertNotNull(date);
    Assert.assertEquals(expected + ":59.999", DateHelper.formatIsoTimestamp(date, timeZone));
  }

  private Date parseTimestamp(final String dateString)
  {
    final Date result = DateHelper.parseIsoTimestamp(dateString + ":00.000", timeZone);
    Assert.assertNotNull(result);
    return result;
  }

  private Date parseDate(final String dateString)
  {
    final Date result = DateHelper.parseIsoTimestamp(dateString + " 08:00:00.000", timeZone);
    Assert.assertNotNull(result);
    return result;
  }
}
