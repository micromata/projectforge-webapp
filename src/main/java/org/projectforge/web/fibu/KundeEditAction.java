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
import net.sourceforge.stripes.validation.EnumeratedTypeConverter;
import net.sourceforge.stripes.validation.IntegerTypeConverter;
import net.sourceforge.stripes.validation.Validate;
import net.sourceforge.stripes.validation.ValidateNestedProperties;

import org.apache.log4j.Logger;
import org.projectforge.common.LabelValueBean;
import org.projectforge.fibu.KundeDO;
import org.projectforge.fibu.KundeDao;
import org.projectforge.fibu.KundeStatus;
import org.projectforge.web.core.BaseAction;
import org.projectforge.web.core.BaseEditAction;
import org.projectforge.web.core.BaseEditActionBean;


/**
 */
@UrlBinding("/secure/fibu/KundeEdit.action")
@BaseAction(jspUrl = "/WEB-INF/jsp/fibu/kundeEdit.jsp")
@BaseEditAction(listAction = KundeListAction.class)
public class KundeEditAction extends BaseEditActionBean<KundeDao, KundeDO>
{
  private static final Logger log = Logger.getLogger(KundeEditAction.class);

  public List<LabelValueBean<String, KundeStatus>> getStatusList()
  {
    KundeStatus[] values = KundeStatus.LIST;
    List<LabelValueBean<String, KundeStatus>> list = new ArrayList<LabelValueBean<String, KundeStatus>>();
    for (KundeStatus status : values) {
      list.add(new LabelValueBean<String, KundeStatus>(getLocalizedString("fibu.kunde.status." + status.getKey()), status));
    }
    return list;
  }

  public void setKundeDao(KundeDao addressDao)
  {
    this.baseDao = addressDao;
  }

  @ValidateNestedProperties( { @Validate(field = "name", required = true, maxlength = 255),
      @Validate(field = "identifier", maxlength = 20),
      @Validate(field = "id", required = true, minvalue = 1, maxvalue = 999, converter = IntegerTypeConverter.class),
      @Validate(field = "status", converter = EnumeratedTypeConverter.class), @Validate(field = "description", maxlength = 4000)})
  public KundeDO getKunde()
  {
    return getData();
  }

  public void setKunde(KundeDO data)
  {
    setData(data);
  }

  @Override
  protected Logger getLogger()
  {
    return log;
  }

  @Override
  protected KundeDO createDataInstance()
  {
    return new KundeDO();
  }
}
