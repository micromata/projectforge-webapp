/////////////////////////////////////////////////////////////////////////////
//
// Project   ProjectForge
//
// Copyright 2001-2009, Micromata GmbH, Kai Reinhard
//           All rights reserved.
//
/////////////////////////////////////////////////////////////////////////////

package org.projectforge.address.contact;

import org.apache.commons.lang.Validate;
import org.projectforge.access.AccessType;
import org.projectforge.access.OperationType;
import org.projectforge.core.BaseDao;
import org.projectforge.user.PFUserDO;

/**
 * @author Werner Feder (werner.feder@t-online.de)
 *
 */
public class ContactEntryDao extends BaseDao<ContactEntryDO>
{

  private ContactDao contactDao;

  public ContactEntryDao()
  {
    super(ContactEntryDO.class);
  }


  public void setContactDao(final ContactDao contactDao)
  {
    this.contactDao = contactDao;
  }

  /**
   * @param addressEntry
   * @param address2Id If null, then address will be set to null;
   * @see BaseDao#getOrLoad(Integer)
   */
  public void setAddress(final ContactEntryDO addressEntry, final Integer address2Id)
  {
    final ContactDO adress2 = contactDao.getOrLoad(address2Id);
    addressEntry.setAddress(adress2);
  }
  /**
   * return Always true, no generic select access needed for address objects.
   * @see org.projectforge.core.BaseDao#hasSelectAccess()
   */
  @Override
  public boolean hasSelectAccess(final PFUserDO user, final boolean throwException)
  {
    return true;
  }


  /**
   * @see org.projectforge.core.BaseDao#hasAccess(Object, OperationType)
   */
  @Override
  public boolean hasAccess(final PFUserDO user, final ContactEntryDO obj, final ContactEntryDO oldObj, final OperationType operationType,
      final boolean throwException)
  {
    return accessChecker.hasPermission(user, obj.getAddressId(), AccessType.TASKS, operationType, throwException);
  }

  /**
   * @see org.projectforge.core.BaseDao#hasUpdateAccess(Object, Object)
   */
  @Override
  public boolean hasUpdateAccess(final PFUserDO user, final ContactEntryDO obj, final ContactEntryDO dbObj, final boolean throwException)
  {
    Validate.notNull(dbObj);
    Validate.notNull(obj);
    Validate.notNull(dbObj.getAddressId());
    Validate.notNull(obj.getAddressId());
    if (accessChecker.hasPermission(user, obj.getAddressId(), AccessType.TASKS, OperationType.UPDATE, throwException) == false) {
      return false;
    }
    //    if (dbObj.getAddressId().equals(obj.getAddressId()) == false) {
    //      // User moves the object to another task:
    //      if (accessChecker.hasPermission(user, obj.getTaskId(), AccessType.TASKS, OperationType.INSERT, throwException) == false) {
    //        // Inserting of object under new task not allowed.
    //        return false;
    //      }
    //      if (accessChecker.hasPermission(user, dbObj.getTaskId(), AccessType.TASKS, OperationType.DELETE, throwException) == false) {
    //        // Deleting of object under old task not allowed.
    //        return false;
    //      }
    //    }
    return true;
  }

  /**
   * @see org.projectforge.core.BaseDao#newInstance()
   */
  @Override
  public ContactEntryDO newInstance()
  {
    return new ContactEntryDO();
  }
}
