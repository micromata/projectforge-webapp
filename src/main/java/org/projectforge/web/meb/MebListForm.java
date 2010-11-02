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

package org.projectforge.web.meb;

import org.apache.log4j.Logger;
import org.apache.wicket.markup.html.basic.Label;
import org.projectforge.core.Configuration;
import org.projectforge.core.ConfigurationParam;
import org.projectforge.user.PFUserContext;
import org.projectforge.web.wicket.AbstractListForm;

public class MebListForm extends AbstractListForm<MebListFilter, MebListPage>
{
  private static final long serialVersionUID = -2464526434762283874L;

  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(MebListForm.class);

  @Override
  protected void init()
  {
    super.init();
    filterContainer.add(new Label("intro", PFUserContext.getLocalizedMessage("meb.intro", Configuration.getInstance().getStringValue(
        ConfigurationParam.MEB_SMS_RECEIVING_PHONE_NUMBER))));
  }

  public MebListForm(MebListPage parentPage)
  {
    super(parentPage);
  }

  @Override
  protected MebListFilter newSearchFilterInstance()
  {
    return new MebListFilter();
  }

  @Override
  protected Logger getLogger()
  {
    return log;
  }
}
