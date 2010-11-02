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

package org.projectforge.user;

import org.projectforge.core.I18nEnum;

/**
 * @author Kai Reinhard (k.reinhard@micromata.de)
 * 
 */
public enum UserRightId implements I18nEnum
{
  FIBU_EINGANGSRECHNUNGEN("fibu.eingangsrechnungen"), //
  FIBU_AUSGANGSRECHNUNGEN("fibu.ausgangsrechnungen"), //
  FIBU_EMPLOYEE_SALARY("fibu.employeeSalaries"), //
  FIBU_DATEV_IMPORT("fibu.datevImport"), //
  FIBU_COST_UNIT("fibu.costUnit"), //

  MISC_MEB("misc.meb"), //

  PM_GANTT("pm.gantt"), //
  PM_ORDER_BOOK("pm.orderbook"), //
  PM_HR_PLANNING("pm.hrPlanning"), //
  PM_PROJECT("pm.project"), //

  ORGA_CONTRACTS("orga.contracts"), //
  ORGA_INCOMING_MAIL("orga.incomingmail"), //
  ORGA_OUTGOING_MAIL("orga.outgoingmail");

  private String key;

  /**
   * The key will be used e. g. for i18n.
   * @return
   */
  public String getKey()
  {
    return key;
  }

  public String getI18nKey()
  {
    return "access.right." + key;
  }

  UserRightId(String key)
  {
    this.key = key;
  }
}
