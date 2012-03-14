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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.apache.wicket.Page;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.util.tester.WicketTester;
import org.junit.Test;

public class WicketUtilsTest
{

  @Test
  public void testRemoveSessionId()
  {
    assertEquals("https://localhost:8443/ProjectForge/wa/timesheetEdit/id/325295",
        WicketUtils
        .removeSessionId("https://localhost:8443/ProjectForge/wa/timesheetEdit/id/325295;jsessionid=DF6F216F10DC6A27EBA0EB60A7254EAA"));
    assertEquals("https://localhost:8443/ProjectForge/wa/timesheetEdit/id/325295",
        WicketUtils.removeSessionId("https://localhost:8443/ProjectForge/wa/timesheetEdit/id/325295"));
    assertEquals("https://localhost:8443/?hurzel", WicketUtils.removeSessionId("https://localhost:8443/;jsessionid=376kjKJ224?hurzel"));
    assertEquals(
        "https://localhost:8443/ProjectForge/wa/?wicket:bookmarkablePage=:org.projectforge.web.timesheet.TimesheetMassUpdatePage",
        WicketUtils
        .removeSessionId("https://localhost:8443/ProjectForge/wa/;jsessionid=B2BE03E5838FDAFCE3ED1AE235A78878?wicket:bookmarkablePage=:org.projectforge.web.timesheet.TimesheetMassUpdatePage"));
    assertEquals("https://localhost:8443/timesheetList/date=6546576/store=false?hurzel",
        WicketUtils.removeSessionId("https://localhost:8443/timesheetList/date=6546576/store=false;jsessionid=376kjKJ224?hurzel"));
    assertEquals("https://localhost:8443/timesheetList/?hurzel",
        WicketUtils.removeSessionId("https://localhost:8443/timesheetList/;jsessionid=376kjKJ224?hurzel"));
    assertEquals("https://localhost:8443/timesheetList/",
        WicketUtils.removeSessionId("https://localhost:8443/timesheetList/;jsessionid=376kjKJ224"));
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

  @Test
  public void isParent()
  {
    new WicketTester(new WebApplication() {
      @Override
      public Class< ? extends Page> getHomePage()
      {
        return null;
      }
    });
    final WebMarkupContainer c1 = new WebMarkupContainer("c1");
    final WebMarkupContainer c2 = new WebMarkupContainer("c2");
    c1.add(c2);
    final WebMarkupContainer c3 = new WebMarkupContainer("c3");
    c2.add(c3);
    final Label l1 = new Label("l1", "l1");
    c3.add(l1);
    final Label l2 = new Label("l2", "l2");
    c1.add(l2);
    final Label l3 = new Label("l3", "l3");
    assertFalse(WicketUtils.isParent(c1, c1));
    assertTrue(WicketUtils.isParent(c1, c2));
    assertFalse(WicketUtils.isParent(c2, c1));
    assertTrue(WicketUtils.isParent(c1, c3));
    assertTrue(WicketUtils.isParent(c1, l1));
    assertTrue(WicketUtils.isParent(c2, l1));
    assertTrue(WicketUtils.isParent(c3, l1));
    assertTrue(WicketUtils.isParent(c1, l2));
    assertFalse(WicketUtils.isParent(c2, l2));
    assertFalse(WicketUtils.isParent(c1, l3));
  }
}
