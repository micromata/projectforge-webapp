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

package org.projectforge.web.core;

import org.apache.log4j.Logger;
import org.apache.wicket.PageParameters;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.projectforge.core.SpaceDO;
import org.projectforge.core.SpaceDao;
import org.projectforge.core.SpaceStatus;
import org.projectforge.web.wicket.AbstractAutoLayoutEditPage;
import org.projectforge.web.wicket.AbstractEditPage;
import org.projectforge.web.wicket.EditPage;

@EditPage(defaultReturnPage = SpaceListPage.class)
public class SpaceEditPage extends AbstractAutoLayoutEditPage<SpaceDO, SpaceEditForm, SpaceDao>
{
  private static final long serialVersionUID = 941381326473678282L;

  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(SpaceEditPage.class);

  @SpringBean(name = "spaceDao")
  private SpaceDao spaceDao;

  public SpaceEditPage(PageParameters parameters)
  {
    super(parameters, "space");
    init();
    if (isNew() == true) {
      getData().setStatus(SpaceStatus.ACTIVE);
    }
  }

  @Override
  protected SpaceDao getBaseDao()
  {
    return spaceDao;
  }

  @Override
  protected SpaceEditForm newEditForm(AbstractEditPage< ? , ? , ? > parentPage, SpaceDO data)
  {
    return new SpaceEditForm(this, data);
  }

  @Override
  protected Logger getLogger()
  {
    return log;
  }
}
