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

package org.projectforge.plugins.teamcal.admin;

import java.util.Collection;

import org.apache.lucene.document.Document;
import org.hibernate.search.bridge.FieldBridge;
import org.hibernate.search.bridge.LuceneOptions;
import org.projectforge.registry.Registry;
import org.projectforge.user.GroupDO;
import org.projectforge.user.PFUserDO;

/**
 * Users and groups bridge for hibernate search.
 * @author Kai Reinhard (k.reinhard@micromata.de)
 * 
 */
public class HibernateSearchUsersGroupsBridge implements FieldBridge
{
  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(HibernateSearchUsersGroupsBridge.class);

  /**
   * Get all names of groups and users and creates an index containing all user and group names separated by '|'. <br/>
   * @see org.hibernate.search.bridge.FieldBridge#set(java.lang.String, java.lang.Object, org.apache.lucene.document.Document,
   *      org.hibernate.search.bridge.LuceneOptions)
   */
  public void set(final String name, final Object value, final Document document, final LuceneOptions luceneOptions)
  {
    final TeamCalDO calendar = (TeamCalDO) value;
    final TeamCalDao teamCalDao = (TeamCalDao) Registry.instance().getDao(TeamCalDao.class);
    final StringBuffer buf = new StringBuffer();
    appendGroups(teamCalDao.getSortedFullAccessGroups(calendar), buf);
    appendGroups(teamCalDao.getSortedReadonlyAccessGroups(calendar), buf);
    appendGroups(teamCalDao.getSortedMinimalAccessGroups(calendar), buf);
    appendUsers(teamCalDao.getSortedFullAccessUsers(calendar), buf);
    appendUsers(teamCalDao.getSortedReadonlyAccessUsers(calendar), buf);
    appendUsers(teamCalDao.getSortedMinimalAccessUsers(calendar), buf);
    if (log.isDebugEnabled() == true) {
      log.debug(buf.toString());
    }
    luceneOptions.addFieldToDocument(name, buf.toString(), document);
  }

  private void appendGroups(final Collection<GroupDO> groups, final StringBuffer buf)
  {
    if (groups == null) {
      return;
    }
    for (final GroupDO group : groups) {
      buf.append(group.getName()).append("|");
    }
  }

  private void appendUsers(final Collection<PFUserDO> users, final StringBuffer buf)
  {
    if (users == null) {
      return;
    }
    for (final PFUserDO user : users) {
      buf.append(user.getFullname()).append(user.getUsername()).append("|");
    }
  }
}
