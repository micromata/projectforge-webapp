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

package org.projectforge.web;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

public class UrlHelperTest
{
  @Test
  public void removeJSessionId()
  {
    assertNull(URLHelper.removeJSessionId(null));
    assertEquals("", URLHelper.removeJSessionId(""));
    assertEquals("http://localhost:8080/ProjectForge/wa/addressEdit",
        URLHelper.removeJSessionId("http://localhost:8080/ProjectForge/wa/addressEdit"));
    assertEquals("http://localhost:8080/ProjectForge/wa/addressEdit",
        URLHelper.removeJSessionId("http://localhost:8080/ProjectForge/wa/addressEdit;jsessionid=hji8ysdreqlz19bipa3ccm2jj"));
    assertEquals("http://localhost:8080/ProjectForge/wa/addressEdit?4",
        URLHelper.removeJSessionId("http://localhost:8080/ProjectForge/wa/addressEdit;jsessionid=hji8ysdreqlz19bipa3ccm2jj?4"));
  }
}
