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

import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;

import javax.naming.Context;
import javax.naming.NameNotFoundException;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.BasicAttributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;

import org.apache.commons.lang.StringUtils;

/**
 * @author Kai Reinhard (k.reinhard@micromata.de)
 */
public class PersonDao
{
  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(PersonDao.class);

  private String userName;

  private String password;

  private String url;

  private String base;

  /*
   * @see PersonDao#create(Person)
   */
  public void create(final Person person)
  {
    final DirContext ctx = createAuthenticatedContext();
    final String dn = buildDn(person);
    try {
      final Attributes attrs = getAttributesToBind(person);
      ctx.bind(dn, null, attrs);
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
   * @see PersonDao#update(Person)
   */
  public void update(final Person person)
  {
    final DirContext ctx = createAuthenticatedContext();
    final String dn = buildDn(person);
    try {
      ctx.rebind(dn, null, getAttributesToBind(person));
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
   * @see PersonDao#delete(Person)
   */
  public void delete(final Person person)
  {
    final DirContext ctx = createAuthenticatedContext();
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
    final DirContext ctx = createAnonymousContext();

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
  public List<Person> findAll()
  {
    final DirContext ctx = createAnonymousContext();

    final LinkedList<Person> list = new LinkedList<Person>();
    NamingEnumeration< ? > results = null;
    try {
      final SearchControls controls = new SearchControls();
      controls.setSearchScope(SearchControls.SUBTREE_SCOPE);
      results = ctx.search("", "(objectclass=person)", controls);

      while (results.hasMore()) {
        final SearchResult searchResult = (SearchResult) results.next();
        final String dn = searchResult.getName();
        final Attributes attributes = searchResult.getAttributes();
        list.add(mapToPerson(dn, attributes));
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
   * @see PersonDao#findByPrimaryKey(java.lang.String, java.lang.String, java.lang.String)
   */
  public Person findByPrimaryKey(final String country, final String company, final String fullname)
  {

    final DirContext ctx = createAnonymousContext();
    final String dn = buildDn(country, company, fullname);
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
    return buildDn(person.getCountry(), person.getCompany(), person.getFullName());
  }

  private String buildDn(final String country, final String company, final String fullname)
  {
    final StringBuffer sb = new StringBuffer();
    sb.append("cn=");
    sb.append(fullname);
    sb.append(", ");
    sb.append("ou=");
    sb.append(company);
    sb.append(", ");
    sb.append("c=");
    sb.append(country);
    final String dn = sb.toString();
    return dn;
  }

  private DirContext createContext(final Hashtable<String, String> env)
  {
    env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
    final String tempUrl = createUrl();
    env.put(Context.PROVIDER_URL, tempUrl);
    DirContext ctx;
    try {
      ctx = new InitialDirContext(env);
    } catch (final NamingException e) {
      throw new RuntimeException(e);
    }
    return ctx;
  }

  private DirContext createAnonymousContext()
  {
    final Hashtable<String, String> hashtable = new Hashtable<String, String>();
    final Hashtable<String, String> env = hashtable;
    return createContext(env);
  }

  private DirContext createAuthenticatedContext()
  {
    final Hashtable<String, String> env = new Hashtable<String, String>();
    env.put(Context.SECURITY_AUTHENTICATION, "simple");
    env.put(Context.SECURITY_PRINCIPAL, userName);
    env.put(Context.SECURITY_CREDENTIALS, password);
    return createContext(env);
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
    person.setFullName((String) attributes.get("cn").get());
    person.setLastName((String) attributes.get("sn").get());
    person.setDescription((String) attributes.get("description").get());

    // Remove any trailing spaces after comma
    final String cleanedDn = dn.replaceAll(", *", ",");

    final String countryMarker = ",c=";
    final int countryIndex = cleanedDn.lastIndexOf(countryMarker);

    final String companyMarker = ",ou=";
    final int companyIndex = cleanedDn.lastIndexOf(companyMarker);

    final String country = cleanedDn.substring(countryIndex + countryMarker.length());
    person.setCountry(country);
    final String company = cleanedDn.substring(companyIndex + companyMarker.length(), countryIndex);
    person.setCompany(company);
    return person;
  }

  private String createUrl()
  {
    String tempUrl = url;
    if (!tempUrl.endsWith("/")) {
      tempUrl += "/";
    }
    if (StringUtils.isNotEmpty(base)) {
      tempUrl += base;
    }
    return tempUrl;
  }

  public void setUrl(final String url)
  {
    this.url = url;
  }

  public void setBase(final String base)
  {
    this.base = base;
  }

  public void setPassword(final String credentials)
  {
    this.password = credentials;
  }

  public void setUserDn(final String principal)
  {
    this.userName = principal;
  }

  // public boolean authenticate(final String userDn, final String credentials)
  // {
  // final LdapContextSource contextSource = new LdapContextSource();
  // contextSource.setUrl("ldap://localhost:389");
  // contextSource.setBase("dc=example,dc=com");
  // DirContext ctx = null;
  // try {
  // ctx = contextSource.getContext(userDn, credentials);
  // return true;
  // } catch (final Exception e) {
  // // Context creation failed - authentication did not succeed
  // log.error("LDAP login failed for user: " + userDn, e);
  // return false;
  // } finally {
  // // It is imperative that the created DirContext instance is always closed
  // LdapUtils.closeContext(ctx);
  // }
  // }
  //
  // private String getDnForUser(final String uid)
  // {
  // final Filter f = new EqualsFilter("uid", uid);
  // final List result = ldapTemplate.search(DistinguishedName.EMPTY_PATH, f.toString(), new AbstractContextMapper() {
  // @Override
  // protected Object doMapFromContext(final DirContextOperations ctx)
  // {
  // return ctx.getNameInNamespace();
  // }
  // });
  //
  // if (result.size() != 1) {
  // throw new RuntimeException("User not found or not unique");
  // }
  //
  // return (String) result.get(0);
  // }
}
