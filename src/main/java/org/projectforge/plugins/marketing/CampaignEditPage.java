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

package org.projectforge.plugins.marketing;

import org.apache.log4j.Logger;
import org.apache.wicket.PageParameters;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.projectforge.web.wicket.AbstractAutoLayoutEditPage;
import org.projectforge.web.wicket.AbstractEditPage;
import org.projectforge.web.wicket.EditPage;

/**
 * The controler of the edit formular page. Most functionality such as insert, update, delete etc. is done by the super class.
 * @author Kai Reinhard (k.reinhard@micromata.de)
 */
@EditPage(defaultReturnPage = CampaignListPage.class)
public class CampaignEditPage extends AbstractAutoLayoutEditPage<CampaignDO, CampaignEditForm, CampaignDao>
{
  private static final long serialVersionUID = -5058143025817192156L;

  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(CampaignEditPage.class);

  @SpringBean(name = "campaignDao")
  private CampaignDao campaignDao;

  public CampaignEditPage(final PageParameters parameters)
  {
    super(parameters, "plugins.marketing.campaign");
    init();
  }

  @Override
  protected CampaignDao getBaseDao()
  {
    return campaignDao;
  }

  @Override
  protected CampaignEditForm newEditForm(final AbstractEditPage< ? , ? , ? > parentPage, final CampaignDO data)
  {
    return new CampaignEditForm(this, data);
  }

  @Override
  protected Logger getLogger()
  {
    return log;
  }
}
