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

package org.projectforge.plugins.teamcal.event.abo;

import java.io.Serializable;
import java.net.URL;
import java.security.MessageDigest;
import java.util.List;

import net.fortuna.ical4j.data.CalendarBuilder;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.projectforge.plugins.teamcal.admin.TeamCalDO;
import org.projectforge.plugins.teamcal.admin.TeamCalDao;
import org.projectforge.plugins.teamcal.event.TeamEventDO;
import org.projectforge.plugins.teamcal.event.TeamEventDao;

import com.google.common.collect.RangeMap;

/**
 * @author Johannes Unterstein (j.unterstein@micromata.de)
 */
public class TeamEventAbo implements Serializable
{
  private Integer teamCalId;

  private List<TeamEventDO> events;

  private RangeMap<Long, TeamEventDO> eventDuractionAccess;

  private TeamEventDao teamEventDao;

  private TeamCalDao teamCalDao;

  public TeamEventAbo(TeamEventDao teamEventDao, TeamCalDao teamCalDao, TeamCalDO teamCalDo)
  {
    this.teamCalId = teamCalDo.getId();
    this.teamEventDao = teamEventDao;
    this.teamCalDao = teamCalDao;
    if (teamCalDo.isAbo() == true && StringUtils.isNotEmpty(teamCalDo.getAboUrl())) {

      final CalendarBuilder builder = new CalendarBuilder();
      try {
        final byte[] bytes = IOUtils.toByteArray(new URL(teamCalDo.getAboUrl()));
        MessageDigest md = MessageDigest.getInstance("MD5");
        String md5 = md.digest(bytes).toString();
        if(StringUtils.equals(md5, teamCalDo.getAboHash()) == false) {
            // TODO update do
        }
      } catch (Exception e) {
        e.printStackTrace(); // To change body of catch statement use File | Settings | File Templates.
      }
      // final Calendar calendar = builder.build();
    }
  }
}
