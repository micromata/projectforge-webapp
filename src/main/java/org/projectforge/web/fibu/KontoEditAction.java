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

import net.sourceforge.stripes.action.UrlBinding;
import net.sourceforge.stripes.validation.IntegerTypeConverter;
import net.sourceforge.stripes.validation.Validate;
import net.sourceforge.stripes.validation.ValidateNestedProperties;

import org.apache.log4j.Logger;
import org.projectforge.fibu.KontoDO;
import org.projectforge.fibu.KontoDao;
import org.projectforge.web.core.BaseAction;
import org.projectforge.web.core.BaseEditAction;
import org.projectforge.web.core.BaseEditActionBean;


/**
 */
@UrlBinding("/secure/fibu/KontoEdit.action")
@BaseAction(jspUrl = "/WEB-INF/jsp/fibu/kontoEdit.jsp")
@BaseEditAction(listAction = KontoListAction.class)
public class KontoEditAction extends BaseEditActionBean<KontoDao, KontoDO>
{
  private static final Logger log = Logger.getLogger(KontoEditAction.class);

  public void setKontoDao(KontoDao kontoDao)
  {
    this.baseDao = kontoDao;
  }

  @ValidateNestedProperties( { @Validate(field = "nummer", required = true, converter = IntegerTypeConverter.class),
      @Validate(field = "bezeichnung", maxlength = 255), @Validate(field = "description", maxlength = 4000)})
  public KontoDO getKonto()
  {
    return getData();
  }

  public void setKonto(KontoDO data)
  {
    setData(data);
  }

  @Override
  protected Logger getLogger()
  {
    return log;
  }

  @Override
  protected KontoDO createDataInstance()
  {
    return new KontoDO();
  }
}
