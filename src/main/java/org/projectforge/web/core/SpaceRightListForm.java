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
import org.apache.wicket.model.PropertyModel;
import org.projectforge.core.SpaceFilter;
import org.projectforge.web.wicket.AbstractListForm;
import org.projectforge.web.wicket.components.CoolCheckBoxPanel;

public class SpaceRightListForm extends AbstractListForm<SpaceFilter, SpaceRightListPage>
{
  private static final long serialVersionUID = 458516732043405087L;

  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(SpaceRightListForm.class);

  public SpaceRightListForm(SpaceRightListPage parentPage)
  {
    super(parentPage);
  }
  
  @Override
  protected void init()
  {
    super.init();
    filterContainer.add(new CoolCheckBoxPanel("activeCheckBox", new PropertyModel<Boolean>(getSearchFilter(), "active"),
        getString("space.status.active"), true));
    filterContainer.add(new CoolCheckBoxPanel("closedCheckBox", new PropertyModel<Boolean>(getSearchFilter(), "closed"),
        getString("space.status.closed"), true));
    filterContainer.add(new CoolCheckBoxPanel("deletedCheckBox", new PropertyModel<Boolean>(getSearchFilter(), "deleted"),
        getString("onlyDeleted"), true).setTooltip(getString("onlyDeleted.tooltip")));
  }

  @Override
  protected Logger getLogger()
  {
    return log;
  }

  @Override
  protected SpaceFilter newSearchFilterInstance()
  {
    return new SpaceFilter();
  }
}
