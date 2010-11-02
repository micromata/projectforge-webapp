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

package org.projectforge.web;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.projectforge.web.HtmlHelper;


public class HtmlHelperTest
{
  @Test
  public void testAttribute()
  {
    HtmlHelper htmlHelper = new HtmlHelper();
    assertEquals(" hallo=\"test\"", htmlHelper.attribute("hallo", "test"));
  }
  
  @Test
  public void testAppendAncorOnClickSubmitEventStartTag() {
    HtmlHelper htmlHelper = new HtmlHelper();
    StringBuffer buf = new StringBuffer();
    htmlHelper.appendAncorOnClickSubmitEventStartTag(buf, "submitEvent", "select.task");
    assertEquals("<a href=\"#\" onclick=\"javascript:submitEvent('select.task')\">", buf.toString());    
    buf = new StringBuffer();
    htmlHelper.appendAncorOnClickSubmitEventStartTag(buf, "submitSelectedEvent", "selectTask", "4");
    assertEquals("<a href=\"#\" onclick=\"javascript:submitSelectedEvent('selectTask', '4')\">", buf.toString());    
  }
}
