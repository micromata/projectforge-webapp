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

package org.projectforge.web.wicket;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.projectforge.web.wicket.WicketUtils;


public class WicketUtilsTest
{

  @Test
  public void testRemoveSessionId()
  {
    assertEquals("https://localhost:8443/ProjectForge/wa/timesheetEdit/id/325295", WicketUtils
        .removeSessionId("https://localhost:8443/ProjectForge/wa/timesheetEdit/id/325295;jsessionid=DF6F216F10DC6A27EBA0EB60A7254EAA"));
    assertEquals("https://localhost:8443/ProjectForge/wa/timesheetEdit/id/325295", WicketUtils
        .removeSessionId("https://localhost:8443/ProjectForge/wa/timesheetEdit/id/325295"));
    assertEquals("https://localhost:8443/?hurzel", WicketUtils.removeSessionId("https://localhost:8443/;jsessionid=376kjKJ224?hurzel"));
    assertEquals(
        "https://localhost:8443/ProjectForge/wa/?wicket:bookmarkablePage=:org.projectforge.web.timesheet.TimesheetMassUpdatePage",
        WicketUtils
            .removeSessionId("https://localhost:8443/ProjectForge/wa/;jsessionid=B2BE03E5838FDAFCE3ED1AE235A78878?wicket:bookmarkablePage=:org.projectforge.web.timesheet.TimesheetMassUpdatePage"));
    assertEquals("https://localhost:8443/timesheetList/date=6546576/store=false?hurzel", WicketUtils
        .removeSessionId("https://localhost:8443/timesheetList/date=6546576/store=false;jsessionid=376kjKJ224?hurzel"));
    assertEquals("https://localhost:8443/timesheetList/?hurzel", WicketUtils
        .removeSessionId("https://localhost:8443/timesheetList/;jsessionid=376kjKJ224?hurzel"));
    assertEquals("https://localhost:8443/timesheetList/", WicketUtils
        .removeSessionId("https://localhost:8443/timesheetList/;jsessionid=376kjKJ224"));
  }
  
  @Test
  public void createTooltip()
  {
    assertEquals("", WicketUtils.createTooltip(null, null));
    assertEquals("", WicketUtils.createTooltip("  ", null));
    assertEquals("The title", WicketUtils.createTooltip("The title", null));
    assertEquals(" - The text", WicketUtils.createTooltip("", "The text"));
    assertEquals("The title - The text", WicketUtils.createTooltip("The title", "The text"));
  }
}
