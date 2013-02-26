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

package org.projectforge.plugins.teamcal;

import org.projectforge.common.NumberHelper;
import org.projectforge.core.ConfigXml;
import org.projectforge.core.ConfigurationData;

public class TeamCalConfig implements ConfigurationData
{
  // Don't change this, otherwise the synchronization with older entries may fail.
  static final String EVENT_UID_PREFIX = "pf-event";

  // Don't change this, otherwise the synchronization with older entries may fail.
  private static final String TIMESHEET_UID_PREFIX = "pf-ts";

  private static TeamCalConfig config;

  public static TeamCalConfig get()
  {
    if (config == null) {
      config = (TeamCalConfig) ConfigXml.getInstance().getPluginConfig(TeamCalConfig.class);
    }
    return config;
  }

  String domain = "projectforge.acme.priv";

  /**
   * The domain is needed for having world wide unique uids. Please note: If you change this domain after working with this plugin,
   * synchronisation errors between ProjectForge and your iCal clients may occur.
   * @return the domain
   */
  public String getDomain()
  {
    return domain;
  }

  /**
   * @param domain the domain to set
   * @return this for chaining.
   */
  public TeamCalConfig setDomain(final String domain)
  {
    this.domain = domain;
    return this;
  }

  /**
   * @param prefix
   * @param id
   * @see #createUid(String, String)
   */
  public String createUid(final String prefix, final Integer id)
  {
    return createUid(prefix, id != null ? id.toString() : "");
  }

  /**
   * Creates a world wide unique event id for ical events for better synchronization.
   * @param prefix
   * @param id
   * @return uid of the format: "${prefix}-${id}@${domain}", e. g. "pf-event-1234@projectforge.org".
   */
  public String createUid(final String prefix, final String id)
  {
    return prefix + "-" + id + "@" + getDomain();
  }

  public Integer extractEventId(final String uid)
  {
    if (uid == null) {
      return null;
    }
    final String prefix = EVENT_UID_PREFIX + "-";
    final String suffix = "@" + getDomain();
    if (uid.endsWith(suffix) == false || uid.startsWith(prefix) == false) {
      return null;
    }
    final String idString = uid.substring(prefix.length(), uid.length() - suffix.length());
    return NumberHelper.parseInteger(idString);
  }

  /**
   * @param id
   * @return
   */
  public String createEventUid(final Integer id)
  {
    return createUid(EVENT_UID_PREFIX, id);
  }

  /**
   * @param id
   * @return
   */
  public String createTimesheetUid(final Integer id)
  {
    return createUid(TIMESHEET_UID_PREFIX, id);
  }

  /**
   * Only for internal test purposes.
   * @param config
   */
  public static void __internalSetConfig(final TeamCalConfig newConfig)
  {
    config = newConfig;
  }
}
