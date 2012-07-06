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

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.naming.NameNotFoundException;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;

/**
 * @author Kai Reinhard (k.reinhard@micromata.de)
 */
public abstract class LdapDao<T>
{
  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(LdapDao.class);

  LdapConnector ldapConnector;

  protected abstract String getObjectClass();

  public void create(final T obj)
  {
    new LdapTemplate(ldapConnector) {
      @Override
      protected Object call() throws NameNotFoundException, Exception
      {
        final String dn = buildDn(obj);
        log.info("Create " + getObjectClass() + ": " + dn);
        final Attributes attrs = getAttributesToBind(obj);
        ctx.bind(dn, null, attrs);
        return null;
      }
    }.excecute();
  }

  /**
   * Calls {@link #create(Object)} if the object isn't part of the given set, otherwise {@link #update(Object)}.
   * @param setOfAllLdapObjects List generated before via {@link #getSetOfAllObjects()}.
   * @param obj
   */
  public void createOrUpdate(final Set<String> setOfAllLdapObjects, final T obj)
  {
    final String dn = buildDn(obj);
    if (setOfAllLdapObjects.contains(dn) == true) {
      update(obj);
    } else {
      create(obj);
    }
  }

  public void update(final T obj)
  {
    new LdapTemplate(ldapConnector) {
      @Override
      protected Object call() throws NameNotFoundException, Exception
      {
        final String dn = buildDn(obj);
        log.info("Update " + getObjectClass() + ": " + dn);
        final Attributes attrs = getAttributesToBind(obj);
        ctx.rebind(dn, null, attrs);
        return null;
      }
    }.excecute();
  }

  public void delete(final T obj)
  {
    new LdapTemplate(ldapConnector) {
      @Override
      protected Object call() throws NameNotFoundException, Exception
      {
        final String dn = buildDn(obj);
        log.info("Delete " + getObjectClass() + ": " + dn);
        ctx.unbind(dn);
        return null;
      }
    }.excecute();
  }

  @SuppressWarnings("unchecked")
  public List<T> findAll()
  {
    return (List<T>) new LdapTemplate(ldapConnector) {
      @Override
      protected Object call() throws NameNotFoundException, Exception
      {
        final LinkedList<T> list = new LinkedList<T>();
        NamingEnumeration< ? > results = null;
        final SearchControls controls = new SearchControls();
        controls.setSearchScope(SearchControls.SUBTREE_SCOPE);
        results = ctx.search("", "(objectclass=" + getObjectClass() + ")", controls);
        while (results.hasMore()) {
          final SearchResult searchResult = (SearchResult) results.next();
          final String dn = searchResult.getName();
          final Attributes attributes = searchResult.getAttributes();
          list.add(mapToObject(dn, attributes));
        }
        return list;
      }
    }.excecute();
  }

  /**
   * Set of all objects (the string is built from the method {@link #buildDn(Object)}).
   */
  public Set<String> getSetOfAllObjects()
  {
    final List<T> all = findAll();
    final Set<String> set = new HashSet<String>();
    for (final T obj : all) {
      set.add(buildDn(obj));
    }
    return set;
  }

  protected abstract String buildDn(final T obj);

  protected abstract Attributes getAttributesToBind(final T obj);

  protected abstract T mapToObject(final String dn, final Attributes attributes) throws NamingException;

  public void setLdapConnector(final LdapConnector ldapConnector)
  {
    this.ldapConnector = ldapConnector;
  }
}
