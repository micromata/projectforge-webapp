/////////////////////////////////////////////////////////////////////////////
//
// Project ProjectForge Community Edition
//         www.projectforge.org
//
// Copyright (C) 2001-2012 Kai Reinhard (k.reinhard@micromata.com)
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

package org.projectforge.plugins.banking;


import org.apache.log4j.Logger;
import org.apache.wicket.PageParameters;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.projectforge.web.wicket.AbstractEditPage;
import org.projectforge.web.wicket.EditPage;


@EditPage(defaultReturnPage = BankAccountListPage.class)
public class BankAccountEditPage extends AbstractEditPage<BankAccountDO, BankAccountEditForm, BankAccountDao>
{
  private static final long serialVersionUID = -7707234514303512825L;

  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(BankAccountEditPage.class);
  
  @SpringBean(name = "bankAccountDao")
  private BankAccountDao bankAccountDao;

  @SuppressWarnings("serial")
  public BankAccountEditPage(PageParameters parameters)
  {
    super(parameters, "fibu.bankAccount");
    init();
  }

  @Override
  protected BankAccountDao getBaseDao()
  {
    return bankAccountDao;
  }

  @Override
  protected BankAccountEditForm newEditForm(AbstractEditPage< ? , ? , ? > parentPage, BankAccountDO data)
  {
    return new BankAccountEditForm(this, data);
  }

  @Override
  protected Logger getLogger()
  {
    return log;
  }
}
