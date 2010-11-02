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

package org.projectforge.web.fibu;

import java.util.ArrayList;
import java.util.List;

import net.sourceforge.stripes.action.UrlBinding;

import org.apache.log4j.Logger;
import org.projectforge.common.LabelValueBean;
import org.projectforge.fibu.kost.Kost1DO;
import org.projectforge.fibu.kost.Kost1Dao;
import org.projectforge.web.core.BaseAction;
import org.projectforge.web.core.BaseListActionBean;


@Deprecated
@UrlBinding("/secure/fibu/Kost1List.action")
@BaseAction(jspUrl = "/WEB-INF/jsp/fibu/kost1List.jsp")
public class Kost1ListAction extends BaseListActionBean<Kost1ListFilter, Kost1Dao, Kost1DO>
{
  private static final Logger log = Logger.getLogger(Kost1ListAction.class);

  public void setKost1Dao(Kost1Dao kost1Dao)
  {
    this.baseDao = kost1Dao;
  }

  public List<LabelValueBean<String, String>> getFilterTypeList()
  {
    List<LabelValueBean<String, String>> list = new ArrayList<LabelValueBean<String, String>>();
    list.add(new LabelValueBean<String, String>(getLocalizedString("filter.all"), "all"));
    list.add(new LabelValueBean<String, String>(getLocalizedString("fibu.kost.status.active"), "active"));
    list.add(new LabelValueBean<String, String>(getLocalizedString("fibu.kost.status.nonactive"), "nonactive"));
    list.add(new LabelValueBean<String, String>(getLocalizedString("notEnded"), "notEnded"));
    list.add(new LabelValueBean<String, String>(getLocalizedString("ended"), "ended"));
    list.add(new LabelValueBean<String, String>(getLocalizedString("deleted"), "deleted"));
    return list;
  }

  /**
   * @return always true.
   * @see org.projectforge.web.core.BaseListActionBean#isShowResultInstantly()
   */
  @Override
  protected boolean isShowResultInstantly()
  {
    return true;
  }

  @Override
  protected Kost1ListFilter createFilterInstance()
  {
    return new Kost1ListFilter();
  }

  @Override
  protected Logger getLogger()
  {
    return log;
  }
}
