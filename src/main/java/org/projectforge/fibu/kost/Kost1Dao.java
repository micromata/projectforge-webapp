/////////////////////////////////////////////////////////////////////////////
//
// Project ProjectForge Community Edition
//         www.projectforge.org
//
// Copyright (C) 2001-2013 Kai Reinhard (k.reinhard@micromata.de)
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

package org.projectforge.fibu.kost;

import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.projectforge.core.BaseDao;
import org.projectforge.core.BaseSearchFilter;
import org.projectforge.core.QueryFilter;
import org.projectforge.core.UserException;
import org.projectforge.fibu.EmployeeDO;
import org.projectforge.fibu.KostFormatter;
import org.projectforge.fibu.ProjektStatus;
import org.projectforge.user.PFUserContext;
import org.projectforge.user.UserRightId;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;


public class Kost1Dao extends BaseDao<Kost1DO>
{
  public static final UserRightId USER_RIGHT_ID = UserRightId.FIBU_COST_UNIT;

  private static final String[] ADDITIONAL_SEARCH_FIELDS = new String[] { "nummer"};

  @Override
  protected String[] getAdditionalSearchFields()
  {
    return ADDITIONAL_SEARCH_FIELDS;
  }

  private KostCache kostCache;

  public Kost1Dao()
  {
    super(Kost1DO.class);
    userRightId = USER_RIGHT_ID;
  }

  /**
   * Gets kost1 as string. Extends access: Users have read access to the number of their own kost1.
   * @param id
   * @return
   */
  @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
  public String getKostString(final Integer id)
  {
    if (id == null) {
      return "";
    }
    final Kost1DO kost1 = internalGetById(id);
    if (kost1 == null) {
      return "";
    }
    if (hasLoggedInUserSelectAccess(kost1, false) == true) {
      return KostFormatter.format(kost1);
    } else {
      final EmployeeDO employee = userGroupCache.getEmployee(PFUserContext.getUserId());
      if (employee != null && employee.getKost1Id() != null && employee.getKost1Id().compareTo(id) == 0) {
        kost1.setDescription(""); // Paranoia (if KostFormatter shows description in future times and Kost1DO is not visible for the user).
        return KostFormatter.format(kost1);
      }
    }
    return null;
  }

  /**
   * @param kostString Format ######## or #.###.##.## is supported.
   * @see #getKost1(int, int, int, int)
   */
  @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
  public Kost1DO getKost1(final String kostString)
  {
    final int[] kost = KostHelper.parseKostString(kostString);
    if (kost == null) {
      return null;
    }
    return getKost1(kost[0], kost[1], kost[2], kost[3]);
  }

  @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
  public Kost1DO getKost1(final int nummernkreis, final int bereich, final int teilbereich, final int endziffer)
  {
    @SuppressWarnings("unchecked")
    final
    List<Kost1DO> list = getHibernateTemplate().find(
        "from Kost1DO k where k.nummernkreis=? and k.bereich=? and k.teilbereich=? and k.endziffer=?",
        new Object[] { nummernkreis, bereich, teilbereich, endziffer});
    if (CollectionUtils.isEmpty(list) == true) {
      return null;
    }
    return list.get(0);
  }

  @Override
  @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
  public List<Kost1DO> getList(final BaseSearchFilter filter)
  {
    final KostFilter myFilter;
    if (filter instanceof KostFilter) {
      myFilter = (KostFilter) filter;
    } else {
      myFilter = new KostFilter(filter);
    }
    final QueryFilter queryFilter = new QueryFilter(myFilter);
    if (myFilter.isActive() == true) {
      queryFilter.add(Restrictions.eq("kostentraegerStatus", KostentraegerStatus.ACTIVE));
    } else if (myFilter.isNonActive() == true) {
      queryFilter.add(Restrictions.eq("kostentraegerStatus", KostentraegerStatus.NONACTIVE));
    } else if (myFilter.isEnded() == true) {
      queryFilter.add(Restrictions.eq("kostentraegerStatus", KostentraegerStatus.ENDED));
    } else if (myFilter.isNotEnded() == true) {
      queryFilter.add(Restrictions.or(Restrictions.ne("kostentraegerStatus", ProjektStatus.ENDED), Restrictions.isNull("kostentraegerStatus")));
    }
    queryFilter.addOrder(Order.asc("nummernkreis")).addOrder(Order.asc("bereich")).addOrder(Order.asc("teilbereich")).addOrder(Order.asc("endziffer"));
    return getList(queryFilter);
  }

  @SuppressWarnings("unchecked")
  @Override
  protected void onSaveOrModify(final Kost1DO obj)
  {
    List<Kost2DO> list = null;
    final String sql = "from Kost1DO k where k.nummernkreis = ? and k.bereich = ? and k.teilbereich = ? and k.endziffer = ?";
    if (obj.getId() == null) {
      // New entry
      list = getHibernateTemplate().find(sql,
          new Object[] { obj.getNummernkreis(), obj.getBereich(), obj.getTeilbereich(), obj.getEndziffer()});
    } else {
      // entry already exists. Check maybe changed:
      list = getHibernateTemplate().find(sql + " and pk <> ?",
          new Object[] { obj.getNummernkreis(), obj.getBereich(), obj.getTeilbereich(), obj.getEndziffer(), obj.getId()});
    }
    if (CollectionUtils.isNotEmpty(list) == true) {
      throw new UserException("fibu.kost.error.collision");
    }
  }

  @Override
  protected void afterSaveOrModify(final Kost1DO kost1)
  {
    super.afterSaveOrModify(kost1);
    kostCache.updateKost1(kost1);
  }

  @Override
  public Kost1DO newInstance()
  {
    return new Kost1DO();
  }

  public void setKostCache(final KostCache kostCache)
  {
    this.kostCache = kostCache;
  }
}
