/////////////////////////////////////////////////////////////////////////////
//
// Project ProjectForge Community Edition
//         www.projectforge.org
//
// Copyright (C) 2001-2012 Kai Reinhard (k.reinhard@micromata.de)
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

package org.projectforge.plugins.teamcal.event;

import org.apache.log4j.Logger;
import org.apache.wicket.model.PropertyModel;
import org.projectforge.web.wicket.AbstractListForm;
import org.projectforge.web.wicket.flowlayout.DivPanel;
import org.projectforge.web.wicket.flowlayout.DivType;
import org.projectforge.web.wicket.flowlayout.FieldsetPanel;

/**
 * @author Kai Reinhard (k.reinhard@micromata.de)
 *
 */
public class TeamEventListForm extends AbstractListForm<TeamEventFilter, TeamEventListPage>
{
  private static final long serialVersionUID = 3659495003810851072L;

  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(TeamEventListForm.class);

  public TeamEventListForm(final TeamEventListPage parentPage){
    super(parentPage);
  }

  /**
   * @see org.projectforge.web.wicket.AbstractListForm#newSearchFilterInstance()
   */
  @Override
  protected TeamEventFilter newSearchFilterInstance()
  {
    return new TeamEventFilter();
  }

  /**
   * @see org.projectforge.web.wicket.AbstractListForm#init()
   */
  @Override
  protected void init()
  {
    super.init();
    gridBuilder.newColumnsPanel();
    gridBuilder.newColumnPanel(DivType.COL_66);
    //        getSearchFilter().setOwnerId(getUserId());
    {
      final FieldsetPanel fs = gridBuilder.newFieldset(getString("label.options"), true).setNoLabelFor();
      fs.setOutputMarkupId(true);

      final DivPanel checkBoxPanel = fs.addNewCheckBoxDiv();
      checkBoxPanel.add(createAutoRefreshCheckBoxPanel(checkBoxPanel.newChildId(),
          new PropertyModel<Boolean>(getSearchFilter(), "own"), getString("plugins.teamcal.own")));
      checkBoxPanel.add(createAutoRefreshCheckBoxPanel(checkBoxPanel.newChildId(),
          new PropertyModel<Boolean>(getSearchFilter(), "fullAccess"), getString("plugins.teamcal.fullAccess")));
      checkBoxPanel.add(createAutoRefreshCheckBoxPanel(checkBoxPanel.newChildId(),
          new PropertyModel<Boolean>(getSearchFilter(), "readonlyAccess"), getString("plugins.teamcal.readonlyAccess")));
      checkBoxPanel.add(createAutoRefreshCheckBoxPanel(checkBoxPanel.newChildId(),
          new PropertyModel<Boolean>(getSearchFilter(), "minimalAccess"), getString("plugins.teamcal.minimalAccess")));
      fs.add(checkBoxPanel);
    }
    {
      // DropDownChoice page size
      gridBuilder.newColumnPanel(DivType.COL_33);
      addPageSizeFieldset();
    }
  }

  /**
   * @see org.projectforge.web.wicket.AbstractListForm#getLogger()
   */
  @Override
  protected Logger getLogger()
  {
    return log;
  }

  /**
   * @return the filter
   */
  public TeamEventFilter getFilter()
  {
    return getSearchFilter();
  }
}
