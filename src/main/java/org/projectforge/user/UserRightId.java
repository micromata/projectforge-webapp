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

package org.projectforge.user;

import java.io.Serializable;

import org.apache.commons.lang.builder.HashCodeBuilder;
import org.hibernate.search.annotations.ClassBridge;
import org.hibernate.search.annotations.Index;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.annotations.Store;
import org.projectforge.core.I18nEnum;

/**
 * @author Kai Reinhard (k.reinhard@micromata.de)
 * 
 */
@Indexed
@ClassBridge(index = Index.TOKENIZED, store = Store.NO, impl = HibernateSearchUserRightIdBridge.class)
public class UserRightId implements I18nEnum, Comparable<UserRightId>, Serializable
{
  private static final long serialVersionUID = 2328022474754212904L;

  public static UserRightId FIBU_EINGANGSRECHNUNGEN = new UserRightId("FIBU_EINGANGSRECHNUNGEN", "fibu1",
      "access.right.fibu.eingangsrechnungen");

  public static UserRightId FIBU_AUSGANGSRECHNUNGEN = new UserRightId("FIBU_AUSGANGSRECHNUNGEN", "fibu2",
      "access.right.fibu.ausgangsrechnungen");

  public static UserRightId FIBU_EMPLOYEE = new UserRightId("FIBU_EMPLOYEE", "fibu3", "access.right.fibu.employee");

  public static UserRightId FIBU_EMPLOYEE_SALARY = new UserRightId("FIBU_EMPLOYEE_SALARY", "fibu4", "access.right.fibu.employeeSalaries");

  public static UserRightId FIBU_DATEV_IMPORT = new UserRightId("FIBU_DATEV_IMPORT", "fibu5", "access.right.fibu.datevImport");

  public static UserRightId FIBU_COST_UNIT = new UserRightId("FIBU_COST_UNIT", "fibu6", "access.right.fibu.costUnit");

  public static UserRightId FIBU_ACCOUNTS = new UserRightId("FIBU_ACCOUNTS", "fibu7", "access.right.fibu.accounts");

  public static UserRightId MISC_MEB = new UserRightId("MISC_MEB", "misc1", "access.right.misc.meb");

  public static UserRightId PM_GANTT = new UserRightId("PM_GANTT", "pm1", "access.right.pm.gantt");

  public static UserRightId PM_ORDER_BOOK = new UserRightId("PM_ORDER_BOOK", "pm2", "access.right.pm.orderbook");

  public static UserRightId PM_HR_PLANNING = new UserRightId("PM_HR_PLANNING", "pm3", "access.right.pm.hrPlanning");

  public static UserRightId PM_PROJECT = new UserRightId("PM_PROJECT", "pm4", "access.right.pm.project");

  public static UserRightId ORGA_CONTRACTS = new UserRightId("ORGA_CONTRACTS", "orga1", "access.right.orga.contracts");

  public static UserRightId ORGA_INCOMING_MAIL = new UserRightId("ORGA_INCOMING_MAIL", "orga2", "access.right.orga.incomingmail");

  public static UserRightId ORGA_OUTGOING_MAIL = new UserRightId("ORGA_OUTGOING_MAIL", "orga3", "access.right.orga.outgoingmail");

  private final String id;

  private final String orderString;

  private final String i18nKey;

  /**
   * @param id Must be unique (including all plugins).
   * @param orderString For displaying the rights in e. g. UserEditPage in the correct order.
   * @param i18nKey
   */
  public UserRightId(final String id, final String orderString, final String i18nKey)
  {
    this.id = id;
    this.orderString = orderString;
    this.i18nKey = i18nKey;
  }

  public String getId()
  {
    return id;
  }

  @Override
  public String getI18nKey()
  {
    return i18nKey;
  }

  public String getOrderString()
  {
    return orderString;
  }

  @Override
  public String toString()
  {
    return String.valueOf(id);
  }

  @Override
  public int hashCode()
  {
    return new HashCodeBuilder().append(id).hashCode();
  }

  @Override
  public boolean equals(final Object obj)
  {
    if (obj == null || obj instanceof UserRightId == false) {
      return false;
    }
    return id.equals(((UserRightId) obj).id);
  }

  @Override
  public int compareTo(final UserRightId o)
  {
    final int res = orderString.compareTo(o.orderString);
    if (res != 0) {
      return res;
    }
    return id.compareTo(o.id);
  }
}
