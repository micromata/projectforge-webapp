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

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.naming.NameNotFoundException;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.BasicAttributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.ModificationItem;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;

import org.apache.commons.lang.StringUtils;
import org.projectforge.common.StringHelper;

/**
 * @author Kai Reinhard (k.reinhard@micromata.de)
 */
public abstract class LdapDao<I extends Serializable, T extends LdapObject<I>>
{
  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(LdapDao.class);

  protected LdapConnector ldapConnector;

  protected LdapConfig ldapConfig;

  protected abstract String getObjectClass();

  protected abstract String[] getAdditionalObjectClasses();

  public abstract String getIdAttrId();

  public abstract I getId(T obj);

  public void create(final String ouBase, final T obj, final Object... args)
  {
    new LdapTemplate(ldapConnector) {
      @Override
      protected Object call() throws NameNotFoundException, Exception
      {
        create(ctx, ouBase, obj, args);
        return null;
      }
    }.excecute();
  }

  /**
   * @param ctx
   * @param ouBase If organizational units are given by the given obj then this parameter will be ignored, otherwise this is the ou where
   *          the new object will be inserted.
   * @param obj
   * @param args
   * @throws NamingException
   */
  public void create(final DirContext ctx, final String ouBase, final T obj, final Object... args) throws NamingException
  {
    final String dn = buildDn(ouBase, obj);
    log.info("Create " + getObjectClass() + ": " + dn + ": " + getLogInfo(obj));
    final Attributes attrs = new BasicAttributes();
    final BasicAttribute ocattr = new BasicAttribute("objectclass");
    ocattr.add(getObjectClass());
    ocattr.add("top");
    final String[] additionalObjectClasses = getAdditionalObjectClasses();
    if (additionalObjectClasses != null) {
      for (final String additionalObjectClass : additionalObjectClasses) {
        ocattr.add(additionalObjectClass);
      }
    }
    attrs.put(ocattr);
    final ModificationItem[] modificationItems = getModificationItems(obj);
    for (final ModificationItem modItem : modificationItems) {
      final Attribute attr = modItem.getAttribute();
      LdapUtils.putAttribute(attrs, attr.getID(), (String) attr.get());
    }
    LdapUtils.putAttribute(attrs, "cn", LdapUtils.escapeCommonName(obj.getCommonName()));
    onBeforeBind(dn, attrs, args);
    ctx.bind(dn, null, attrs);
  }

  protected void onBeforeBind(final String dn, final Attributes attrs, final Object... args)
  {
    // Do nothing at default.
  }

  /**
   * Please do not use this method for bulk updates, use {@link #createOrUpdate(Set, Object, Object...)} instead! Calls
   * {@link #getSetOfAllObjects()} before creation or update.
   * @param obj
   * @see #createOrUpdate(Set, Object, Object...)
   */
  public void createOrUpdate(final String ouBase, final T obj, final Object... args)
  {
    createOrUpdate(getSetOfAllObjects(ouBase), ouBase, obj, args);
  }

  /**
   * Please do not use this method for bulk updates, use {@link #createOrUpdate(Set, Object, Object...)} instead! Calls
   * {@link #getSetOfAllObjects()} before creation or update.
   * @param obj
   * @throws NamingException
   * @see #createOrUpdate(Set, Object, Object...)
   */
  public void createOrUpdate(final DirContext ctx, final String ouBase, final T obj, final Object... args) throws NamingException
  {
    createOrUpdate(ctx, getSetOfAllObjects(ctx, ouBase), ouBase, obj, args);
  }

  /**
   * Calls {@link #create(Object)} if the object isn't part of the given set, otherwise {@link #update(Object)}.
   * @param setOfAllLdapObjects List generated before via {@link #getSetOfAllObjects()}.
   * @param obj
   */
  public void createOrUpdate(final SetOfAllLdapObjects setOfAllLdapObjects, final String ouBase, final T obj, final Object... args)
  {
    if (setOfAllLdapObjects.contains(obj, buildDn(ouBase, obj)) == true) {
      update(ouBase, obj, args);
    } else {
      create(ouBase, obj, args);
    }
  }

  /**
   * Calls {@link #create(Object)} if the object isn't part of the given set, otherwise {@link #update(Object)}.
   * @param setOfAllLdapObjects List generated before via {@link #getSetOfAllObjects()}.
   * @param obj
   * @throws NamingException
   */
  public void createOrUpdate(final DirContext ctx, final SetOfAllLdapObjects setOfAllLdapObjects, final String ouBase, final T obj,
      final Object... args) throws NamingException
      {
    if (setOfAllLdapObjects.contains(obj, buildDn(ouBase, obj)) == true) {
      update(ctx, ouBase, obj, args);
    } else {
      create(ctx, ouBase, obj, args);
    }
      }

  public void update(final String ouBase, final T obj, final Object... objs)
  {
    new LdapTemplate(ldapConnector) {
      @Override
      protected Object call() throws NameNotFoundException, Exception
      {
        update(ctx, ouBase, obj, objs);
        return null;
      }
    }.excecute();
  }

  public void update(final DirContext ctx, final String ouBase, final T obj, final Object... objs) throws NamingException
  {
    modify(ctx, obj, getModificationItems(obj));
  }

  protected abstract ModificationItem[] getModificationItems(final T obj);

  /**
   * Helper method.
   * @param attrId
   * @param attrValue
   * @return
   */
  protected ModificationItem createModificationItem(final String attrId, final String attrValue)
  {
    return createModificationItem(DirContext.REPLACE_ATTRIBUTE, attrId, attrValue);
  }

  /**
   * Helper method.
   * @param {@link DirContext#REPLACE_ATTRIBUTE}, {@link DirContext#ADD_ATTRIBUTE} or {@link DirContext#REMOVE_ATTRIBUTE}.
   * @param attrId
   * @param attrValue
   * @return
   */
  protected ModificationItem createModificationItem(final int mode, final String attrId, final String attrValue)
  {
    return new ModificationItem(mode, new BasicAttribute(attrId, attrValue));
  }

  /**
   * Helper method for appending modification item(s) to a given list. At least one entry will be added if no attrValue is given.
   * @param list
   * @param attrId
   * @param attrValues If null then a null-value will be assumed. If more than one string is given, multiple modification items will be
   *          added.
   * @return
   */
  protected void createAndAddModificationItems(final List<ModificationItem> list, final String attrId, final String... attrValues)
  {
    if (attrValues == null) {
      list.add(createModificationItem(attrId, null));
      return;
    }
    boolean added = false;
    for (final String attrValue : attrValues) {
      if (StringUtils.isEmpty(attrValue) == true && added == true) {
        continue;
      }
      final String val = StringUtils.isEmpty(attrValue) == true ? null : attrValue;
      if (added == false) {
        list.add(createModificationItem(DirContext.REPLACE_ATTRIBUTE, attrId, val));
        added = true;
      } else {
        list.add(createModificationItem(DirContext.ADD_ATTRIBUTE, attrId, val));
      }
    }
  }

  /**
   * Helper method for appending modification item(s) to a given list. At least one entry will be added if no attrValue is given.
   * @param list
   * @param attrId
   * @param attrValues If null then a null-value will be assumed. If more than one string is given, multiple modification items will be
   *          added.
   * @return
   */
  protected void createAndAddModificationItems(final List<ModificationItem> list, final String attrId, final Set<String> attrValues)
  {
    if (attrValues == null) {
      list.add(createModificationItem(attrId, null));
      return;
    }
    boolean added = false;
    for (final String attrValue : attrValues) {
      if (StringUtils.isEmpty(attrValue) == true && added == true) {
        continue;
      }
      final String val = StringUtils.isEmpty(attrValue) == true ? null : attrValue;
      if (added == false) {
        list.add(createModificationItem(DirContext.REPLACE_ATTRIBUTE, attrId, val));
        added = true;
      } else {
        list.add(createModificationItem(DirContext.ADD_ATTRIBUTE, attrId, val));
      }
    }
  }

  public void modify(final T obj, final ModificationItem[] modificationItems)
  {
    new LdapTemplate(ldapConnector) {
      @Override
      protected Object call() throws NameNotFoundException, Exception
      {
        modify(ctx, obj, modificationItems);
        return null;
      }
    }.excecute();
  }

  public void modify(final DirContext ctx, final T obj, final ModificationItem[] modificationItems) throws NamingException
  {
    final Object id = getId(obj);
    // The dn is may-be changed, so find the original dn by id:
    final T origObject = findById(ctx, id, obj.getOrganizationalUnit());
    if (origObject == null) {
      throw new RuntimeException("Object with id "
          + id
          + " not found in search base '"
          + StringHelper.listToString(",", obj.getOrganizationalUnit())
          + "'. Can't modify the object: "
          + obj);
    }
    final String dn = origObject.getDn();
    log.info("Modify attributes of " + getObjectClass() + ": " + dn + ": " + getLogInfo(obj));
    ctx.modifyAttributes(dn, modificationItems);
    // Don't move object.
    // if (obj.getDn() != null && StringUtils.equals(dn, obj.getDn()) == false) {
    // log.info("DN of object is changed from '" + dn + "' to '" + obj.getDn());
    // ctx.rename(dn, obj.getDn());
    // }
  }

  public void move(final T obj, final String newOrganizationalUnit)
  {
    new LdapTemplate(ldapConnector) {
      @Override
      protected Object call() throws NameNotFoundException, Exception
      {
        move(ctx, obj, newOrganizationalUnit);
        return null;
      }
    }.excecute();
  }

  public void move(final DirContext ctx, final T obj, final String newOrganizationalUnit) throws NamingException
  {
    final Object id = getId(obj);
    // The dn is may-be changed, so find the original dn by id:
    final T origObject = findById(id, obj.getOrganizationalUnit());
    if (origObject == null) {
      throw new RuntimeException("Object with id "
          + id
          + " not found in search base '"
          + StringHelper.listToString(",", obj.getOrganizationalUnit())
          + "'. Can't modify the object: "
          + obj);
    }
    final String ou = LdapUtils.getOu(newOrganizationalUnit);
    final String origOu = LdapUtils.getOu(origObject.getOrganizationalUnit());
    if (StringUtils.equals(origOu, ou) == false) {
      log.info("Move object with id '" + obj.getId() + "' from '" + origOu + "' to '" + ou);
      ctx.rename("cn=" + LdapUtils.escapeCommonName(obj.getCommonName()) + "," + origOu,
          "cn=" + LdapUtils.escapeCommonName(obj.getCommonName()) + "," + ou);
    }
  }

  protected String getLogInfo(final T obj)
  {
    return String.valueOf(obj);
  }

  protected void onBeforeRebind(final String dn, final Attributes attrs, final Object... objs)
  {
    // Do nothing at default;
  }

  public void delete(final T obj)
  {
    new LdapTemplate(ldapConnector) {
      @Override
      protected Object call() throws NameNotFoundException, Exception
      {
        delete(ctx, obj);
        return null;
      }
    }.excecute();
  }

  public void delete(final DirContext ctx, final T obj) throws NamingException
  {
    final String dn = buildDn(null, obj);
    log.info("Delete " + getObjectClass() + ": " + dn + ": " + getLogInfo(obj));
    ctx.unbind(dn);
  }

  @SuppressWarnings("unchecked")
  public List<T> findAll(final String organizationalUnit)
  {
    return (List<T>) new LdapTemplate(ldapConnector) {
      @Override
      protected Object call() throws NameNotFoundException, Exception
      {
        return findAll(ctx, organizationalUnit);
      }
    }.excecute();
  }

  public List<T> findAll(final DirContext ctx, final String organizationalUnit) throws NamingException
  {
    final LinkedList<T> list = new LinkedList<T>();
    NamingEnumeration< ? > results = null;
    final SearchControls controls = new SearchControls();
    controls.setSearchScope(SearchControls.SUBTREE_SCOPE);
    final String searchBase = LdapUtils.getOu(organizationalUnit);
    results = ctx.search(searchBase, "(objectclass=" + getObjectClass() + ")", controls);
    while (results.hasMore()) {
      final SearchResult searchResult = (SearchResult) results.next();
      final String dn = searchResult.getName();
      final Attributes attributes = searchResult.getAttributes();
      list.add(mapToObject(dn, searchBase, attributes));
    }
    return list;
  }

  @SuppressWarnings("unchecked")
  public T findById(final Object id, final String... organizationalUnits)
  {
    return (T) new LdapTemplate(ldapConnector) {
      @Override
      protected Object call() throws NameNotFoundException, Exception
      {
        return findById(ctx, id, organizationalUnits);
      }
    }.excecute();
  }

  public T findById(final DirContext ctx, final Object id, final String... organizationalUnits) throws NamingException
  {
    NamingEnumeration< ? > results = null;
    final SearchControls controls = new SearchControls();
    controls.setSearchScope(SearchControls.SUBTREE_SCOPE);
    final String searchBase = LdapUtils.getOu(organizationalUnits);
    results = ctx.search(searchBase, "(&(objectClass=" + getObjectClass() + ")(" + getIdAttrId() + "=" + id + "))", controls);
    if (results.hasMore() == false) {
      return null;
    }
    final SearchResult searchResult = (SearchResult) results.next();
    final String dn = searchResult.getName();
    final Attributes attributes = searchResult.getAttributes();
    if (results.hasMore() == true) {
      log.error("Oups, found entries with multiple id's: " + getObjectClass() + "." + id);
    }
    return mapToObject(dn, searchBase, attributes);
  }

  /**
   * Set of all objects (the string is built from the method {@link #buildDn(Object)}).
   */
  public SetOfAllLdapObjects getSetOfAllObjects(final String organizationalUnit)
  {
    final SetOfAllLdapObjects set = new SetOfAllLdapObjects();
    final List<T> all = findAll(organizationalUnit);
    for (final T obj : all) {
      if (log.isDebugEnabled() == true) {
        log.debug("Adding: " + obj.getDn());
      }
      set.add(obj);
    }
    return set;
  }

  /**
   * Set of all objects (the string is built from the method {@link #buildDn(Object)}).
   * @throws NamingException
   */
  public SetOfAllLdapObjects getSetOfAllObjects(final DirContext ctx, final String organizationalUnit) throws NamingException
  {
    final SetOfAllLdapObjects set = new SetOfAllLdapObjects();
    final List<T> all = findAll(ctx, organizationalUnit);
    for (final T obj : all) {
      if (log.isDebugEnabled() == true) {
        log.debug("Adding: " + obj.getDn());
      }
      set.add(obj);
    }
    return set;
  }

  /**
   * Sets dn of object and organizationalUnit if not already given.
   * @param ouBase If {@link T#getOrganizationalUnit()} is not given, ouBase is used for building dn, otherwise ouBase is ignored.
   * @param obj
   * @return
   */
  protected String buildDn(final String ouBase, final T obj)
  {
    final StringBuffer buf = new StringBuffer();
    buf.append("cn=").append(LdapUtils.escapeCommonName(obj.getCommonName()));
    if (obj.getOrganizationalUnit() != null) {
      buf.append(',');
      LdapUtils.buildOu(buf, obj.getOrganizationalUnit());
    } else if (ouBase != null) {
      buf.append(',');
      LdapUtils.buildOu(buf, ouBase);
      obj.setOrganizationalUnit(ouBase);
    }
    obj.setDn(buf.toString());
    return obj.getDn();
  }

  protected T mapToObject(final String dn, final String ouBase, final Attributes attributes) throws NamingException
  {
    String fullDn;
    if (StringUtils.isNotBlank(ouBase) == true) {
      fullDn = dn + "," + ouBase;
    } else {
      fullDn = dn;
    }
    final T obj = mapToObject(fullDn, attributes);
    obj.setDn(fullDn);
    obj.setOrganizationalUnit(LdapUtils.getOrganizationalUnit(dn, ouBase));
    obj.setCommonName(LdapUtils.getAttributeStringValue(attributes, "cn"));
    return obj;
  }

  /**
   * 
   * @param dn
   * @param attributes
   * @return
   * @throws NamingException
   */
  protected abstract T mapToObject(final String dn, final Attributes attributes) throws NamingException;

  public void setLdapConnector(final LdapConnector ldapConnector)
  {
    this.ldapConnector = ldapConnector;
    this.ldapConfig = ldapConnector.getLdapConfig();
  }
}
