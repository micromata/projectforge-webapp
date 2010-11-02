/////////////////////////////////////////////////////////////////////////////
//
// Project ProjectForge Community Edition
//         www.projectforge.org
//
// Copyright (C) 2001-2010 Kai Reinhard (k.reinhard@me.com)
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

import java.util.Date;
import java.util.List;
import java.util.StringTokenizer;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.apache.commons.lang.Validate;
import org.projectforge.access.OperationType;
import org.projectforge.core.BaseDO;
import org.projectforge.core.BaseDao;
import org.projectforge.core.BaseSearchFilter;
import org.projectforge.core.QueryFilter;
import org.projectforge.fibu.kost.Kost1DO;
import org.projectforge.fibu.kost.Kost1Dao;
import org.projectforge.user.PFUserDO;
import org.projectforge.user.ProjectForgeGroup;
import org.projectforge.user.UserDao;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;


/**
 * Ein Mitarbeiter ist einem ProjectForge-Benutzer zugeordnet und trägt einige buchhalterische Angaben.
 * @author Kai Reinhard (k.reinhard@micromata.de)
 * 
 */
public class EmployeeDao extends BaseDao<EmployeeDO>
{
  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(EmployeeDao.class);

  private static final String[] ADDITIONAL_SEARCH_FIELDS = new String[] { "user.firstname", "user.lastname", "user.description",
      "user.organization"};

  private UserDao userDao;

  private Kost1Dao kost1Dao;

  public EmployeeDao()
  {
    super(EmployeeDO.class);
    baseDaoReindexRegistry.registerDependent(PFUserDO.class, this);
    baseDaoReindexRegistry.registerDependent(Kost1DO.class, this);
  }

  @Override
  protected String[] getAdditionalSearchFields()
  {
    return ADDITIONAL_SEARCH_FIELDS;
  }

  @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
  public EmployeeDO getByUserId(Integer userId)
  {
    @SuppressWarnings("unchecked")
    final List<EmployeeDO> list = getHibernateTemplate().find("from EmployeeDO e where e.user.id = ?", userId);
    if (list != null && list.size() > 0) {
      return list.get(0);
    }
    return null;
  }

  /**
   * If more than one employee is found, null will be returned.
   * @param fullname Format: &lt;last name&gt;, &lt;first name&gt;
   */
  @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
  public EmployeeDO getByName(String fullname)
  {
    StringTokenizer tokenizer = new StringTokenizer(fullname, ",");
    if (tokenizer.countTokens() != 2) {
      log.error("EmployeeDao.getByName: Token '" + fullname + "' not supported.");
    }
    Validate.isTrue(tokenizer.countTokens() == 2);
    String lastname = tokenizer.nextToken().trim();
    String firstname = tokenizer.nextToken().trim();
    @SuppressWarnings("unchecked")
    final List<EmployeeDO> list = getHibernateTemplate().find("from EmployeeDO e where e.user.lastname = ? and e.user.firstname = ?",
        new Object[] { lastname, firstname});
    // final List<EmployeeDO> list = getHibernateTemplate().find("from EmployeeDO e where e.user.lastname = ?", lastname);
    if (list != null && list.size() == 1) {
      return list.get(0);
    }
    return null;
  }

  /**
   * @param employee
   * @param userId If null, then user will be set to null;
   * @see BaseDao#getOrLoad(Integer)
   */
  public void setUser(final EmployeeDO employee, Integer userId)
  {
    PFUserDO user = userDao.getOrLoad(userId);
    employee.setUser(user);
  }

  /**
   * @param employee
   * @param kost1Id If null, then kost1 will be set to null;
   * @see BaseDao#getOrLoad(Integer)
   */
  public void setKost1(final EmployeeDO employee, Integer kost1Id)
  {
    Kost1DO kost1 = kost1Dao.getOrLoad(kost1Id);
    employee.setKost1(kost1);
  }

  @Override
  public List<EmployeeDO> getList(final BaseSearchFilter filter)
  {
    final EmployeeFilter myFilter = (EmployeeFilter) filter;
    QueryFilter queryFilter = new QueryFilter(filter);
    final List<EmployeeDO> list = getList(queryFilter);
    final Date now = new Date();
    if (myFilter.isShowOnlyActiveEntries() == true) {
      CollectionUtils.filter(list, new Predicate() {
        public boolean evaluate(final Object object)
        {
          final EmployeeDO employee = (EmployeeDO) object;
          if (employee.getEintrittsDatum() != null && now.before(employee.getEintrittsDatum()) == true) {
            return false;
          } else if (employee.getAustrittsDatum() != null && now.after(employee.getAustrittsDatum()) == true) {
            return false;
          }
          return true;
        }
      });
    }
    return list;
  }

  /**
   * @see org.projectforge.core.BaseDao#hasSelectAccess()
   */
  @Override
  public boolean hasSelectAccess(boolean throwException)
  {
    return accessChecker.isUserMemberOfGroup(throwException, ProjectForgeGroup.FINANCE_GROUP, ProjectForgeGroup.CONTROLLING_GROUP);
  }

  @Override
  public boolean hasSelectAccess(EmployeeDO obj, boolean throwException)
  {
    return hasSelectAccess(throwException);
  }

  /**
   * @see org.projectforge.core.BaseDao#hasAccess(Object, OperationType)
   */
  @Override
  public boolean hasAccess(EmployeeDO obj, EmployeeDO oldObj, OperationType operationType, boolean throwException)
  {
    return accessChecker.isUserMemberOfGroup(throwException, ProjectForgeGroup.FINANCE_GROUP);
  }

  @Override
  protected void afterSaveOrModify(EmployeeDO employee)
  {
    super.afterSaveOrModify(employee);
    userGroupCache.refreshEmployee(employee.getUserId());
  }

  @Override
  public List<EmployeeDO> getDependentObjectsToReindex(BaseDO< ? > obj)
  {
    if (obj instanceof PFUserDO) {
      @SuppressWarnings("unchecked")
      final List<EmployeeDO> list = getHibernateTemplate().find("from EmployeeDO e where e.user.id=?", ((PFUserDO) obj).getId());
      return list;
    } else if (obj instanceof Kost1DO) {
      @SuppressWarnings("unchecked")
      final List<EmployeeDO> list = getHibernateTemplate().find("from EmployeeDO e where e.kost1.id=?", ((Kost1DO) obj).getId());
      return list;
    } else {
      return dependencyNotSupportedOf(obj);
    }
  }

  public void setUserDao(UserDao userDao)
  {
    this.userDao = userDao;
  }

  public void setKost1Dao(Kost1Dao kost1Dao)
  {
    this.kost1Dao = kost1Dao;
  }

  @Override
  public EmployeeDO newInstance()
  {
    return new EmployeeDO();
  }
}
