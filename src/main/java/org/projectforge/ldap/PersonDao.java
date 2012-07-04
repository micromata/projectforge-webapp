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

import java.util.LinkedList;
import java.util.List;

import javax.naming.NameNotFoundException;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.BasicAttributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;

import org.projectforge.common.StringHelper;

/**
 * @author Kai Reinhard (k.reinhard@micromata.de)
 */
public class PersonDao
{
  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(PersonDao.class);

  LdapConnector ldapConnector;

  /*
   * @see PersonDao#create(Person)
   */
  public void create(final Person person)
  {
    new LdapTemplate(ldapConnector) {
      @Override
      protected Object call() throws NameNotFoundException, Exception
      {
        final String dn = buildDn(person);
        log.info("Create person: " + dn);
        final Attributes attrs = getAttributesToBind(person);
        ctx.bind(dn, null, attrs);
        return null;
      }
    }.excecute();
  }

  /*
   * @see PersonDao#update(Person)
   */
  public void update(final Person person)
  {
    new LdapTemplate(ldapConnector) {
      @Override
      protected Object call() throws NameNotFoundException, Exception
      {
        final String dn = buildDn(person);
        log.info("Update person: " + dn);
        final Attributes attrs = getAttributesToBind(person);
        ctx.rebind(dn, null, attrs);
        return null;
      }
    }.excecute();
  }

  /*
   * @see PersonDao#delete(Person)
   */
  public void delete(final Person person)
  {
    final DirContext ctx = null;// createAuthenticatedContext();
    final String dn = buildDn(person);
    try {
      ctx.unbind(dn);
    } catch (final NamingException e) {
      throw new RuntimeException(e);
    } finally {
      if (ctx != null) {
        try {
          ctx.close();
        } catch (final Exception e) {
          // Never mind this.
        }
      }
    }
  }

  /*
   * @see PersonDao#getAllPersonNames()
   */
  public List<String> getAllPersonNames()
  {
    final DirContext ctx = null;// createAuthenticatedContext();

    final LinkedList<String> list = new LinkedList<String>();
    NamingEnumeration< ? > results = null;
    try {
      final SearchControls controls = new SearchControls();
      controls.setSearchScope(SearchControls.SUBTREE_SCOPE);
      results = ctx.search("", "(objectclass=person)", controls);

      while (results.hasMore()) {
        final SearchResult searchResult = (SearchResult) results.next();
        final Attributes attributes = searchResult.getAttributes();
        final Attribute attr = attributes.get("cn");
        final String cn = (String) attr.get();
        list.add(cn);
      }
    } catch (final NamingException e) {
      throw new RuntimeException(e);
    } finally {
      if (results != null) {
        try {
          results.close();
        } catch (final Exception e) {
          // Never mind this.
        }
      }
      if (ctx != null) {
        try {
          ctx.close();
        } catch (final Exception e) {
          // Never mind this.
        }
      }
    }
    return list;
  }

  /*
   * @see PersonDao#findAll()
   */
  @SuppressWarnings("unchecked")
  public List<Person> findAll()
  {
    return (List<Person>) new LdapTemplate(ldapConnector) {
      @Override
      protected Object call() throws NameNotFoundException, Exception
      {
        final LinkedList<Person> list = new LinkedList<Person>();
        NamingEnumeration< ? > results = null;
        final SearchControls controls = new SearchControls();
        controls.setSearchScope(SearchControls.SUBTREE_SCOPE);
        results = ctx.search("", "(objectclass=person)", controls);
        while (results.hasMore()) {
          final SearchResult searchResult = (SearchResult) results.next();
          final String dn = searchResult.getName();
          final Attributes attributes = searchResult.getAttributes();
          list.add(mapToPerson(dn, attributes));
        }
        return list;
      }
    }.excecute();
  }

  /*
   * @see PersonDao#findByPrimaryKey(java.lang.String, java.lang.String, java.lang.String)
   */
  public Person findByPrimaryKey(final String company, final String fullname)
  {

    final DirContext ctx = null;// createAuthenticatedContext();
    final String dn = buildDn(company, fullname);
    try {
      final Attributes attributes = ctx.getAttributes(dn);
      return mapToPerson(dn, attributes);
    } catch (final NameNotFoundException e) {
      throw new RuntimeException("Did not find entry with primary key '" + dn + "'", e);
    } catch (final NamingException e) {
      throw new RuntimeException(e);
    } finally {
      if (ctx != null) {
        try {
          ctx.close();
        } catch (final Exception e) {
          // Never mind this.
        }
      }
    }
  }

  private String buildDn(final Person person)
  {
    return buildDn(person.getOu(), person.getFullName());
  }

  private String buildDn(final String ou, final String fullname)
  {
    final StringBuffer sb = new StringBuffer();
    sb.append("cn=");
    sb.append(fullname);
    final int pos = ou != null ? ou.indexOf(',') : -1;
    if (pos < 0) {
      sb.append(", ou=");
      sb.append(ou);
    } else {
      final String[] strs = StringHelper.splitAndTrim(ou, ",");
      for (final String str : strs) {
        sb.append(", ou=");
        sb.append(str);
      }
    }
    final String dn = sb.toString();
    return dn;
  }

  private Attributes getAttributesToBind(final Person person)
  {
    final Attributes attrs = new BasicAttributes();
    final BasicAttribute ocattr = new BasicAttribute("objectclass");
    ocattr.add("top");
    ocattr.add("person");
    attrs.put(ocattr);
    attrs.put("cn", person.getFullName());
    attrs.put("sn", person.getLastName());
    attrs.put("description", person.getDescription());
    return attrs;
  }

  private Person mapToPerson(final String dn, final Attributes attributes) throws NamingException
  {
    final Person person = new Person();
    person.setFullName(getAttribute(attributes, "cn"));
    person.setLastName(getAttribute(attributes, "sn"));
    person.setDescription(getAttribute(attributes, "description"));
    person.setOu(getAttribute(attributes, "ou"));
    return person;
  }

  private String getAttribute(final Attributes attributes, final String attrId) throws NamingException
  {
    final Attribute attr = attributes.get(attrId);
    if (attr == null) {
      return null;
    }
    return (String) attr.get();
  }

  public void setLdapConnector(final LdapConnector ldapConnector)
  {
    this.ldapConnector = ldapConnector;
  }
}
