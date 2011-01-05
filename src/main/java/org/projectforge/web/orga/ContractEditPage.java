/////////////////////////////////////////////////////////////////////////////
//
// Project ProjectForge Community Edition
//         www.projectforge.org
//
// Copyright (C) 2001-2011 Kai Reinhard (k.reinhard@me.com)
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

package org.projectforge.web.orga;

import java.util.Date;

import org.apache.log4j.Logger;
import org.apache.wicket.PageParameters;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.projectforge.calendar.DayHolder;
import org.projectforge.orga.ContractDO;
import org.projectforge.orga.ContractDao;
import org.projectforge.web.fibu.ISelectCallerPage;
import org.projectforge.web.wicket.AbstractBasePage;
import org.projectforge.web.wicket.AbstractEditPage;
import org.projectforge.web.wicket.EditPage;
import org.projectforge.web.wicket.components.DatePanel;

@EditPage(defaultReturnPage = ContractListPage.class)
public class ContractEditPage extends AbstractEditPage<ContractDO, ContractEditForm, ContractDao> implements ISelectCallerPage
{
  private static final long serialVersionUID = 4375220914096256551L;

  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(ContractEditPage.class);

  @SpringBean(name = "contractDao")
  private ContractDao contractDao;

  public ContractEditPage(PageParameters parameters)
  {
    super(parameters, "legalAffaires.contract");
    init();
    if (isNew() == true) {
      getData().setDate(new DayHolder().getSQLDate());
    }
  }
  
  @Override
  public AbstractBasePage onSaveOrUpdate()
  {
    if (isNew() == true && getData().getNumber() == null) {
      getData().setNumber(contractDao.getNextNumber(getData()));
    }
    return null;
  }


  /**
   * @see org.projectforge.web.fibu.ISelectCallerPage#select(java.lang.String, java.lang.Integer)
   */
  public void select(String property, Object selectedValue)
  {
    if ("date".equals(property) == true) {
      getData().setDate(setDate(form.datePanel, selectedValue));
    } else if ("validFrom".equals(property) == true) {
      getData().setValidFrom(setDate(form.validFromDatePanel, selectedValue));
    } else if ("validUntil".equals(property) == true) {
      getData().setValidUntil(setDate(form.validUntilDatePanel, selectedValue));
    } else if ("signingDate".equals(property) == true) {
      getData().setSigningDate(setDate(form.signingDatePanel, selectedValue));
    } else if ("resubmissionOnDate".equals(property) == true) {
      getData().setResubmissionOnDate(setDate(form.resubmissionDatePanel, selectedValue));
    } else if ("dueDate".equals(property) == true) {
      getData().setDueDate(setDate(form.dueDatePanel, selectedValue));
    } else {
      log.error("Property '" + property + "' not supported for selection.");
    }
  }
  
  private java.sql.Date setDate(final DatePanel panel, final Object selectedValue) {
    final Date date = (Date) selectedValue;
    final java.sql.Date sqlDate = new java.sql.Date(date.getTime());
    panel.markModelAsChanged();
    return sqlDate;
  }

  /**
   * @see org.projectforge.web.fibu.ISelectCallerPage#unselect(java.lang.String)
   */
  public void unselect(String property)
  {
    log.error("Property '" + property + "' not supported for selection.");
  }

  /**
   * @see org.projectforge.web.fibu.ISelectCallerPage#cancelSelection(java.lang.String)
   */
  public void cancelSelection(String property)
  {
    // Do nothing.
  }

  @Override
  protected ContractDao getBaseDao()
  {
    return contractDao;
  }

  @Override
  protected ContractEditForm newEditForm(AbstractEditPage< ? , ? , ? > parentPage, ContractDO data)
  {
    return new ContractEditForm(this, data);
  }

  @Override
  protected Logger getLogger()
  {
    return log;
  }
}
