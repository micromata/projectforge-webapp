/////////////////////////////////////////////////////////////////////////////
//
// Project   ProjectForge
//
// Copyright 2001-2009, Micromata GmbH, Kai Reinhard
//           All rights reserved.
//
/////////////////////////////////////////////////////////////////////////////

package org.projectforge.addresses;

import java.util.List;

import org.apache.commons.lang.Validate;
//import org.apache.poi.ss.formula.functions.T;
import org.projectforge.access.AccessType;
import org.projectforge.access.OperationType;
import org.projectforge.core.BaseDao;
import org.projectforge.core.Configuration;
import org.projectforge.core.ConfigurationParam;
import org.projectforge.task.TaskDO;
import org.projectforge.task.TaskDao;
import org.projectforge.user.PFUserDO;

/**
 * @author Werner Feder (werner.feder@t-online.de)
 *
 */
public class Address2Dao extends BaseDao<Address2DO>
{

  private Configuration configuration;

  private TaskDao taskDao;

  private final XmlConverter<InstantMessagingValue> imConverter;
  private final XmlConverter<PhoneValue> phoneConverter;
  private final XmlConverter<EmailValue> emailConverter;

  public Address2Dao()
  {
    super(Address2DO.class);
    final InstantMessagingValue im = new InstantMessagingValue();
    final PhoneValue phone = new PhoneValue();
    final EmailValue email = new EmailValue();
    imConverter = new XmlConverter<InstantMessagingValue>(im);
    phoneConverter = new XmlConverter<PhoneValue>(phone);
    emailConverter = new XmlConverter<EmailValue>(email);
  }

  public void setConfiguration(final Configuration configuration)
  {
    this.configuration = configuration;
  }

  public void setTaskDao(final TaskDao taskDao)
  {
    this.taskDao = taskDao;
  }

  //  private String getNormalizedFullname(final Address2DO address)
  //  {
  //    final StringBuilder builder = new StringBuilder();
  //    if (address.getFirstName() != null) {
  //      builder.append(address.getFirstName().toLowerCase().trim());
  //    }
  //    if (address.getName() != null) {
  //      builder.append(address.getName().toLowerCase().trim());
  //    }
  //    return builder.toString();
  //  }

  /**
   * @param address
   * @param taskId If null, then task will be set to null;
   * @see BaseDao#getOrLoad(Integer)
   */
  public void setTask(final Address2DO address, final Integer taskId)
  {
    final TaskDO task = taskDao.getOrLoad(taskId);
    address.setTask(task);
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

  private void beforeUpdateOrSave(final Address2DO address)
  {
    if (address != null && address.getTaskId() == null) {
      setTask(address, getDefaultTaskId());
    }
  }

  /**
   * @see org.projectforge.core.BaseDao#hasAccess(Object, OperationType)
   */
  @Override
  public boolean hasAccess(final PFUserDO user, final Address2DO obj, final Address2DO oldObj, final OperationType operationType,
      final boolean throwException)
  {
    beforeUpdateOrSave(obj);
    return accessChecker.hasPermission(user, obj.getTaskId(), AccessType.TASKS, operationType, throwException);
  }

  /**
   * @see org.projectforge.core.BaseDao#hasUpdateAccess(Object, Object)
   */
  @Override
  public boolean hasUpdateAccess(final PFUserDO user, final Address2DO obj, final Address2DO dbObj, final boolean throwException)
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

  /**
   * @see org.projectforge.core.BaseDao#newInstance()
   */
  @Override
  public Address2DO newInstance()
  {
    return new Address2DO();
  }

  /**
   * Exports xml string as List of Instant Messaging values.
   * @param InstantMessagingValue values
   */
  public List<InstantMessagingValue> readImValues(final String valuesAsXml)
  {
    return imConverter.readValues(valuesAsXml);
  }

  /**
   * Exports the Instant Messaging values as xml string.
   * @param InstantMessagingValue values
   */
  public String getImValuesAsXml(final InstantMessagingValue... values)
  {
    return imConverter.getValuesAsXml(values);
  }


  public List<EmailValue> readEmailValues(final String valuesAsXml)
  {
    return emailConverter.readValues(valuesAsXml);
  }

  public String getEmailValuesAsXml(final EmailValue... values)
  {
    return emailConverter.getValuesAsXml(values);
  }

  public List<PhoneValue> readPhoneValues(final String valuesAsXml)
  {
    return phoneConverter.readValues(valuesAsXml);
  }

  public String getPhoneValuesAsXml(final PhoneValue... values)
  {
    return phoneConverter.getValuesAsXml(values);
  }

}
