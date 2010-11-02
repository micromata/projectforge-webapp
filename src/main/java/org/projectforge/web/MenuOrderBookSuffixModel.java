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

package org.projectforge.web;

import org.apache.wicket.injection.web.InjectorHolder;
import org.apache.wicket.model.Model;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.projectforge.common.NumberHelper;
import org.projectforge.fibu.AuftragDao;


public class MenuOrderBookSuffixModel extends Model<String>
{
  private static final long serialVersionUID = -8989618708152002379L;

  @SpringBean(name="auftragDao")
  private AuftragDao auftragDao;

  @Override
  public String getObject()
  {
    if (auftragDao == null) {
      InjectorHolder.getInjector().inject(this);
    }
    final Integer counter = getAuftragDao().getAbgeschlossenNichtFakturiertAnzahl();
    if (NumberHelper.greaterZero(counter) == false) {
      return "";
    }
    return MenuBuilder.getNewCounterForAsMenuEntrySuffix(counter, "menu.fibu.orderbook.htmlSuffixTooltip");
  }

  public void setAuftragDao(AuftragDao auftragDao)
  {
    this.auftragDao = auftragDao;
  }

  public AuftragDao getAuftragDao()
  {
    return auftragDao;
  }
}
