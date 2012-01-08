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

package org.projectforge.address;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.Validate;
import org.hibernate.LockMode;
import org.projectforge.access.AccessChecker;
import org.projectforge.access.AccessException;
import org.projectforge.core.BaseDao;
import org.projectforge.user.PFUserContext;
import org.projectforge.user.PFUserDO;
import org.projectforge.user.UserDao;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * 
 * @author Kai Reinhard (k.reinhard@micromata.de)
 * 
 */
public class PersonalAddressDao extends HibernateDaoSupport
{
  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(PersonalAddressDao.class);

  private AccessChecker accessChecker;

  private UserDao userDao;

  public void setAccessChecker(AccessChecker accessChecker)
  {
    this.accessChecker = accessChecker;
  }

  public void setUserDao(UserDao userDao)
  {
    this.userDao = userDao;
  }

  /**
   * @param personalAddress
   * @param ownerId If null, then task will be set to null;
   * @see BaseDao#getOrLoad(Integer)
   */
  public void setOwner(final PersonalAddressDO personalAddress, Integer ownerId)
  {
    PFUserDO user = userDao.getOrLoad(ownerId);
    personalAddress.setOwner(user);
  }

  /**
   * @param obj
   * @return the generated identifier.
   */
  @Transactional(readOnly = false, propagation = Propagation.REQUIRES_NEW, isolation = Isolation.REPEATABLE_READ)
  public Serializable saveOrUpdate(PersonalAddressDO obj)
  {
    if (internalUpdate(obj) == true) {
      return obj.getId();
    }
    return internalSave(obj);
  }

  private void checkAccess(PersonalAddressDO obj)
  {
    Validate.notNull(obj);
    Validate.notNull(obj.getOwnerId());
    Validate.notNull(obj.getAddressId());
    final PFUserDO owner = PFUserContext.getUser();
    if (owner == null || owner.getId().equals(obj.getOwnerId()) == false) {
      throw new AccessException("address.accessException.userIsNotOwnerOfPersonalAddress");
    }
    accessChecker.checkDemoUser();
  }

  private Serializable internalSave(PersonalAddressDO obj)
  {
    if (isEmpty(obj) == true) {
      // No entry, so we do not need to save this entry.
      return null;
    }
    checkAccess(obj);
    obj.setCreated();
    obj.setLastUpdate();
    Serializable id = getHibernateTemplate().save(obj);
    log.info("New object added (" + id + "): " + obj.toString());
    return id;
  }

  private boolean isEmpty(PersonalAddressDO obj)
  {
    return (obj.isFavoriteCard() == false)
        && (obj.isFavoriteBusinessPhone() == false)
        && (obj.isFavoriteMobilePhone() == false)
        && (obj.isFavoriteFax() == false)
        && (obj.isFavoritePrivatePhone() == false)
        && (obj.isFavoritePrivateMobilePhone() == false);
  }

  /**
   * @param obj
   * @return true, if already existing entry was updated, otherwise false (e. g. if no entry exists for update).
   */
  private boolean internalUpdate(PersonalAddressDO obj)
  {
    PersonalAddressDO dbObj = null;
    if (obj.getId() != null) {
      dbObj = (PersonalAddressDO) getHibernateTemplate().load(PersonalAddressDO.class, obj.getId(), LockMode.PESSIMISTIC_WRITE);
    }
    if (dbObj == null) {
      dbObj = getByAddressId(obj.getAddressId());
    }
    if (dbObj == null) {
      return false;
    }
    checkAccess(dbObj);
    Validate.isTrue(ObjectUtils.equals(dbObj.getAddressId(), obj.getAddressId()));
    obj.setId(dbObj.getId());
    if (isEmpty(obj) == true) {
      // Is empty, so delete this entry:
      getHibernateTemplate().delete(dbObj);
      log.info("Empty object deleted: " + obj.toString());
      return true;
    }
    // Copy all values of modified user to database object.
    boolean modified = dbObj.copyValuesFrom(obj, "owner", "address", "id");
    if (modified == true) {
      dbObj.setLastUpdate();
      log.info("Object updated: " + dbObj.toString());
    }
    return true;
  }

  /**
   * @return the PersonalAddressDO entry assigned to the given address for the context user or null, if not exist.
   */
  @SuppressWarnings("unchecked")
  @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
  public PersonalAddressDO getByAddressId(Integer addressId)
  {
    final PFUserDO owner = PFUserContext.getUser();
    Validate.notNull(owner);
    Validate.notNull(owner.getId());
    List<PersonalAddressDO> list = getHibernateTemplate().find(
        "from " + PersonalAddressDO.class.getSimpleName() + " t where t.owner.id = ? and t.address.id = ?",
        new Object[] { owner.getId(), addressId});
    if (list != null) {
      if (list.size() == 0) {
        return null;
      }
      if (list.size() > 1) {
        log.error("Multiple personal address book entries for same user ("
            + owner.getId()
            + " and same address ("
            + addressId
            + "). Should not occur?!");
      }
      return list.get(0);
    }
    return null;
  }

  /**
   * @return the list of all PersonalAddressDO entries for the context user.
   */
  @SuppressWarnings("unchecked")
  @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
  public List<PersonalAddressDO> getList()
  {
    final PFUserDO owner = PFUserContext.getUser();
    Validate.notNull(owner);
    Validate.notNull(owner.getId());
    List<PersonalAddressDO> list = getHibernateTemplate().find(
        "from "
            + PersonalAddressDO.class.getSimpleName()
            + " t join fetch t.address where t.owner.id = ? and t.address.deleted = false order by t.address.name, t.address.firstName", owner.getId());
    return list;
  }

  /**
   * @return the list of all address ids of personal address book for the context user (isFavorite() must be true).
   * @see PersonalAddressDO#isFavorite()
   */
  @SuppressWarnings("unchecked")
  @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
  public Map<Integer, PersonalAddressDO> getPersonalAddressByAddressId()
  {
    final PFUserDO owner = PFUserContext.getUser();
    Validate.notNull(owner);
    Validate.notNull(owner.getId());
    List<PersonalAddressDO> list = getHibernateTemplate().find(
        "from " + PersonalAddressDO.class.getSimpleName() + " t where t.owner.id = ?", owner.getId());
    Map<Integer, PersonalAddressDO> result = new HashMap<Integer, PersonalAddressDO>();
    for (PersonalAddressDO entry : list) {
      if (entry.isFavorite() == true) {
        result.put(entry.getAddressId(), entry);
      }
    }
    return result;
  }

  /**
   * @return the list of all address entries for the context user (isFavorite() must be true).
   * @see PersonalAddressDO#isFavorite()
   */
  @SuppressWarnings("unchecked")
  @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
  public List<AddressDO> getMyAddresses()
  {
    final PFUserDO owner = PFUserContext.getUser();
    Validate.notNull(owner);
    Validate.notNull(owner.getId());
    List<PersonalAddressDO> list = getHibernateTemplate().find(
        "from "
            + PersonalAddressDO.class.getSimpleName()
            + " t join fetch t.address where t.owner.id = ? and t.address.deleted = false order by t.address.name, t.address.firstName", owner.getId());
    List<AddressDO> result = new ArrayList<AddressDO>();
    for (PersonalAddressDO entry : list) {
      if (entry.isFavorite() == true) {
        result.add(entry.getAddress());
      }
    }
    return result;
  }
}
