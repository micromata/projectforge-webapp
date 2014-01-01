/////////////////////////////////////////////////////////////////////////////
//
// Project ProjectForge Community Edition
//         www.projectforge.org
//
// Copyright (C) 2001-2014 Kai Reinhard (k.reinhard@micromata.de)
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

package org.projectforge.plugins.teamcal;

import java.util.Map;

import junit.framework.Assert;

import org.junit.BeforeClass;
import org.junit.Test;
import org.projectforge.core.Configuration;
import org.projectforge.core.ConfigurationParam;

public class TeamCalConfigTest
{
  @BeforeClass
  public static void setUp() throws Exception
  {
    final String domain = "projectforge.org";
    final Map<ConfigurationParam, Object> map = Configuration.init4TestMode();
    map.put(ConfigurationParam.CALENDAR_DOMAIN, domain);
  }

  @Test
  public void extractUid()
  {
    final TeamCalConfig config = new TeamCalConfig();
    final String domain = Configuration.getInstance().getCalendarDomain();
    Assert.assertNull(config.extractEventId(null));
    Assert.assertNull(config.extractEventId(""));
    Assert.assertNull(config.extractEventId("unkown-123@" + domain));
    Assert.assertEquals(new Integer("123"), config.extractEventId(TeamCalConfig.EVENT_UID_PREFIX + "-123@" + domain));
    Assert.assertEquals(new Integer("1"), config.extractEventId(TeamCalConfig.EVENT_UID_PREFIX + "-1@" + domain));
  }
}
