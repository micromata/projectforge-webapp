/////////////////////////////////////////////////////////////////////////////
//
// Project   ProjectForge
//
// Copyright 2001-2009, Micromata GmbH, Kai Reinhard
//           All rights reserved.
//
/////////////////////////////////////////////////////////////////////////////

package org.projectforge.addresses;

import org.apache.commons.lang.Validate;
import org.projectforge.access.AccessType;
import org.projectforge.access.OperationType;
import org.projectforge.core.BaseDao;
import org.projectforge.core.Configuration;
import org.projectforge.core.ConfigurationParam;
import org.projectforge.task.TaskDO;
import org.projectforge.task.TaskDao;
import org.projectforge.user.PFUserDO;

/**
 * @author Kai Reinhard (k.reinhard@micromata.de)
 *
 */
public class AddressEntryDao extends BaseDao<AddressEntryDO>
{

  private Configuration configuration;
  private TaskDao taskDao;

  public AddressEntryDao()
  {
    super(AddressEntryDO.class);
  }

  public void setConfiguration(final Configuration configuration)
  {
    this.configuration = configuration;
  }

  public void setTaskDao(final TaskDao taskDao)
  {
    this.taskDao = taskDao;
  }

  private String getNormalizedFullname(final AddressEntryDO addressEntry)
  {
    final StringBuilder builder = new StringBuilder();
    if (addressEntry.getFirstName() != null) {
      builder.append(addressEntry.getFirstName().toLowerCase().trim());
    }
    if (addressEntry.getName() != null) {
      builder.append(addressEntry.getName().toLowerCase().trim());
    }
    return builder.toString();
  }

  /**
   * @param address
   * @param taskId If null, then task will be set to null;
   * @see BaseDao#getOrLoad(Integer)
   */
  public void setTask(final AddressEntryDO addressEntry, final Integer taskId)
  {
    final TaskDO task = taskDao.getOrLoad(taskId);
    addressEntry.setTask(task);
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
   * Addresses will be assigned to a default task.
   */
  public Integer getDefaultTaskId()
  {
    return configuration.getTaskIdValue(ConfigurationParam.DEFAULT_TASK_ID_4_ADDRESSES);
  }

  private void beforeUpdateOrSave(final AddressEntryDO addressEntry)
  {
    if (addressEntry != null && addressEntry.getTaskId() == null) {
      setTask(addressEntry, getDefaultTaskId());
    }
  }

  /**
   * @see org.projectforge.core.BaseDao#newInstance()
   */
  @Override
  public AddressEntryDO newInstance()
  {
    return new AddressEntryDO();
  }

  /**
   * @see org.projectforge.core.BaseDao#hasAccess(Object, OperationType)
   */
  @Override
  public boolean hasAccess(final PFUserDO user, final AddressEntryDO obj, final AddressEntryDO oldObj, final OperationType operationType,
      final boolean throwException)
  {
    beforeUpdateOrSave(obj);
    return accessChecker.hasPermission(user, obj.getTaskId(), AccessType.TASKS, operationType, throwException);
  }

  /**
   * @see org.projectforge.core.BaseDao#hasUpdateAccess(Object, Object)
   */
  @Override
  public boolean hasUpdateAccess(final PFUserDO user, final AddressEntryDO obj, final AddressEntryDO dbObj, final boolean throwException)
  {
    Validate.notNull(dbObj);
    Validate.notNull(obj);
    beforeUpdateOrSave(obj);
    Validate.notNull(dbObj.getTaskId());
    Validate.notNull(obj.getTaskId());
    if (accessChecker.hasPermission(user, obj.getTaskId(), AccessType.TASKS, OperationType.UPDATE, throwException) == false) {
      return false;
    }
    if (dbObj.getTaskId().equals(obj.getTaskId()) == false) {
      // User moves the object to another task:
      if (accessChecker.hasPermission(user, obj.getTaskId(), AccessType.TASKS, OperationType.INSERT, throwException) == false) {
        // Inserting of object under new task not allowed.
        return false;
      }
      if (accessChecker.hasPermission(user, dbObj.getTaskId(), AccessType.TASKS, OperationType.DELETE, throwException) == false) {
        // Deleting of object under old task not allowed.
        return false;
      }
    }
    return true;
  }
}
