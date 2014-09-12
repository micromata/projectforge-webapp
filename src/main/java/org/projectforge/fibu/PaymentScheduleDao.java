/////////////////////////////////////////////////////////////////////////////
//
// Project ProjectForge Community Edition
//         www.projectforge.org
//
// Copyright (C) 2001-2014 Kai Reinhard (k.reinhard@micromata.de)
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
