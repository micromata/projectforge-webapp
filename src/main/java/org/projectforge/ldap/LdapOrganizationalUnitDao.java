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

package org.projectforge.ldap;

import javax.naming.NameNotFoundException;
import javax.naming.NamingEnumeration;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.BasicAttributes;
import javax.naming.directory.SearchControls;

/**
 * @author Kai Reinhard (k.reinhard@micromata.de)
 */
public class LdapOrganizationalUnitDao
{
  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(LdapOrganizationalUnitDao.class);

  private static final String OBJECT_CLASS = "organizationalUnit";

  LdapConnector ldapConnector;

  public void createIfNotExist(final String ou, final String description, final String... organizationalUnits)
  {
    new LdapTemplate(ldapConnector) {
      @Override
      protected Object call() throws NameNotFoundException, Exception
      {
        final StringBuffer buf = new StringBuffer();
        buf.append("ou=").append(ou);
        if (organizationalUnits != null) {
          buf.append(',');
        }
        LdapUtils.buildOu(buf, organizationalUnits);
        final String path = buf.toString();

        NamingEnumeration< ? > results = null;
        final SearchControls controls = new SearchControls();
        controls.setSearchScope(SearchControls.ONELEVEL_SCOPE);
        final String searchBase = LdapUtils.getOu(organizationalUnits);
        results = ctx.search(searchBase, "(&(objectClass=" + OBJECT_CLASS + ")(ou=" + ou + "))", controls);
        if (results.hasMore() == true) {
          log.info(OBJECT_CLASS + " does already exist (OK): " + path);
          return null;
        }
        log.info("Create " + OBJECT_CLASS + ": " + path);
        final Attributes attrs = new BasicAttributes();
        final BasicAttribute ocattr = new BasicAttribute("objectclass");
        ocattr.add("top");
        ocattr.add(OBJECT_CLASS);
        attrs.put(ocattr);
        LdapUtils.putAttribute(attrs, "ou", ou);
        LdapUtils.putAttribute(attrs, "description", description);
        ctx.bind(path, null, attrs);
        return null;
      }
    }.excecute();
  }

  public void setLdapConnector(final LdapConnector ldapConnector)
  {
    this.ldapConnector = ldapConnector;
  }
}
