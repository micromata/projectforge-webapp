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

import net.sourceforge.stripes.action.StrictBinding;
import net.sourceforge.stripes.action.UrlBinding;
import net.sourceforge.stripes.validation.Validate;

import org.apache.log4j.Logger;
import org.projectforge.common.LabelValueBean;
import org.projectforge.fibu.ProjektDO;
import org.projectforge.fibu.ProjektDao;
import org.projectforge.fibu.kost.KostCache;
import org.projectforge.reporting.Kost2Art;
import org.projectforge.reporting.Projekt;
import org.projectforge.reporting.impl.ProjektImpl;
import org.projectforge.web.core.BaseAction;
import org.projectforge.web.core.BaseListActionBean;


@StrictBinding
@UrlBinding("/secure/fibu/ProjektList.action")
@BaseAction(jspUrl = "/WEB-INF/jsp/fibu/projektList.jsp")
public class ProjektListAction extends BaseListActionBean<ProjektListFilter, ProjektDao, ProjektDO>
{
  private static final Logger log = Logger.getLogger(ProjektListAction.class);

  private KostCache kostCache;

  private List<Projekt> projektList;

  public void setProjektDao(ProjektDao projektDao)
  {
    this.baseDao = projektDao;
  }

  public void setKostCache(KostCache kostCache)
  {
    this.kostCache = kostCache;
  }

  public List<LabelValueBean<String, String>> getFilterTypeList()
  {
    List<LabelValueBean<String, String>> list = new ArrayList<LabelValueBean<String, String>>();
    list.add(new LabelValueBean<String, String>(getLocalizedString("filter.all"), "all"));
    list.add(new LabelValueBean<String, String>(getLocalizedString("notEnded"), "notEnded"));
    list.add(new LabelValueBean<String, String>(getLocalizedString("ended"), "ended"));
    list.add(new LabelValueBean<String, String>(getLocalizedString("deleted"), "deleted"));
    return list;
  }

  @Override
  protected String getSingleEntryValue()
  {
    List<Projekt> list = getProjektList();
    if (list != null && list.size() == 1) {
      return String.valueOf(list.get(0).getId()); // return the pk.
    }
    return null;
  }

  @Validate
  public String getListType()
  {
    return getActionFilter().getListType();
  }

  public void setListType(String listType)
  {
    getActionFilter().setListType(listType);
  }

  public List<Projekt> getProjektList()
  {
    if (projektList == null) {
      List<ProjektDO> list = super.buildList();
      projektList = new ArrayList<Projekt>();
      if (list != null) {
        for (ProjektDO projektDO : list) {
          ProjektImpl projekt = new ProjektImpl(projektDO);
          List<Kost2Art> kost2Arts = kostCache.getAllKost2Arts(projektDO.getId());
          projekt.setKost2Arts(kost2Arts);
          projektList.add(projekt);
        }
      }
      refreshResultList = false;
    }
    return projektList;
  }

  @Override
  protected List<ProjektDO> buildList()
  {
    return null;
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
  protected ProjektListFilter createFilterInstance()
  {
    return new ProjektListFilter();
  }

  @Override
  protected Logger getLogger()
  {
    return log;
  }
}
