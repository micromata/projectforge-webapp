/////////////////////////////////////////////////////////////////////////////
//
// Project   ProjectForge
//
// Copyright 2001-2009, Micromata GmbH, Kai Reinhard
//           All rights reserved.
//
/////////////////////////////////////////////////////////////////////////////

package org.projectforge.fibu;

import org.apache.commons.lang.Validate;
import org.projectforge.access.AccessType;
import org.projectforge.access.OperationType;
import org.projectforge.core.BaseDao;
import org.projectforge.user.PFUserDO;

/**
 * @author Werner Feder (werner.feder@t-online.de)
 * 
 */
public class PaymentScheduleDao extends BaseDao<PaymentScheduleDO>
{

  private AuftragDao auftragDao;

  public PaymentScheduleDao()
  {
    super(PaymentScheduleDO.class);
  }

  public PaymentScheduleDao setAuftragDao(final AuftragDao auftragDao)
  {
    this.auftragDao = auftragDao;
    return this;
  }

  /**
   * @param paymentSchedule
   * @param auftragId If null, then auftrag will be set to null;
   * @see BaseDao#getOrLoad(Integer)
   */
  public void setContact(final PaymentScheduleDO paymentSchedule, final Integer auftragId)
  {
    final AuftragDO auftrag = auftragDao.getOrLoad(auftragId);
    paymentSchedule.setAuftrag(auftrag);
  }

  /**
   * return Always true, no generic select access needed for paymentSchedule objects.
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
  public boolean hasAccess(final PFUserDO user, final PaymentScheduleDO obj, final PaymentScheduleDO oldObj, final OperationType operationType,
      final boolean throwException)
  {
    return accessChecker.hasPermission(user, obj.getAuftragId(), AccessType.TASKS, operationType, throwException);
  }

  /**
   * @see org.projectforge.core.BaseDao#hasUpdateAccess(Object, Object)
   */
  @Override
  public boolean hasUpdateAccess(final PFUserDO user, final PaymentScheduleDO obj, final PaymentScheduleDO dbObj, final boolean throwException)
  {
    Validate.notNull(dbObj);
    Validate.notNull(obj);
    Validate.notNull(dbObj.getAuftragId());
    Validate.notNull(obj.getAuftragId());
    if (accessChecker.hasPermission(user, obj.getAuftragId(), AccessType.TASKS, OperationType.UPDATE, throwException) == false) {
      return false;
    }
    return true;
  }

  /**
   * @see org.projectforge.core.BaseDao#newInstance()
   */
  @Override
  public PaymentScheduleDO newInstance()
  {
    return new PaymentScheduleDO();
  }

}
