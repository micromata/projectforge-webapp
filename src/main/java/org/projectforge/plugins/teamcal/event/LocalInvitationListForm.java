/////////////////////////////////////////////////////////////////////////////
//
// Project ProjectForge Community Edition
//         www.projectforge.org
//
// Copyright (C) 2001-2014 Kai Reinhard (k.reinhard@micromata.de)
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
import org.projectforge.web.wicket.AbstractListForm;
import org.projectforge.web.wicket.bootstrap.GridSize;

/**
 * @author Werner Feder (w.feder.extern@micromata.de)
 *
 */
public class LocalInvitationListForm extends AbstractListForm<LocalInvitationFilter, LocalInvitationListPage>
{
  private static final long serialVersionUID = 6564726713786425742L;

  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(LocalInvitationListForm.class);

  //  @SuppressWarnings("serial")
  @Override
  protected void init()
  {
    super.init();
    gridBuilder.newSplitPanel(GridSize.COL66);
    //    {
    //      // User
    //      final FieldsetPanel fs = gridBuilder.newFieldset(getString("plugins.teamcal.event.user"));
    //      final UserSelectPanel assigneeSelectPanel = new UserSelectPanel(fs.newChildId(), new Model<PFUserDO>() {
    //        @Override
    //        public PFUserDO getObject()
    //        {
    //          return userGroupCache.getUser(getSearchFilter().getUserId());
    //        }
    //
    //        @Override
    //        public void setObject(final PFUserDO object)
    //        {
    //          if (object == null) {
    //            getSearchFilter().setUserId(null);
    //          } else {
    //            getSearchFilter().setUserId(object.getId());
    //          }
    //        }
    //      }, parentPage, "userId");
    //      fs.add(assigneeSelectPanel);
    //      assigneeSelectPanel.setDefaultFormProcessing(false);
    //      assigneeSelectPanel.init().withAutoSubmit(true);
    //    }
    //    gridBuilder.newSplitPanel(GridSize.COL33);
    //    {
    //      // TeamEvent
    //      final FieldsetPanel fs = gridBuilder.newFieldset(getString("plugins.teamcal.event.event"));
    //      final UserSelectPanel reporterSelectPanel = new UserSelectPanel(fs.newChildId(), new Model<PFUserDO>() {
    //
    //        @Override
    //        public PFUserDO getObject()
    //        {
    //          return userGroupCache.getUser(getSearchFilter().getTeamEventId());
    //        }
    //
    //        @Override
    //        public void setObject(final PFUserDO object)
    //        {
    //          if (object == null) {
    //            getSearchFilter().setTeamEventId(null);
    //          } else {
    //            getSearchFilter().setTeamEventId(object.getId());
    //          }
    //        }
    //      }, parentPage, "teamEventId");
    //      fs.add(reporterSelectPanel);
    //      reporterSelectPanel.setDefaultFormProcessing(false);
    //      reporterSelectPanel.init().withAutoSubmit(true);
    //    }
  }

  public LocalInvitationListForm(final LocalInvitationListPage parentPage)
  {
    super(parentPage);
  }

  /**
   * @see org.projectforge.web.wicket.AbstractListForm#onOptionsPanelCreate(org.projectforge.web.wicket.flowlayout.FieldsetPanel,
   *      org.projectforge.web.wicket.flowlayout.DivPanel)
   */
  //  @Override
  //  protected void onOptionsPanelCreate(final FieldsetPanel optionsFieldsetPanel, final DivPanel optionsCheckBoxesPanel)
  //  {
  //    optionsCheckBoxesPanel.add(createAutoRefreshCheckBoxButton(optionsCheckBoxesPanel.newChildId(), new PropertyModel<Boolean>(
  //        getSearchFilter(), "opened"), getString(ToDoStatus.OPENED.getI18nKey())));
  //    optionsCheckBoxesPanel.add(createAutoRefreshCheckBoxButton(optionsCheckBoxesPanel.newChildId(), new PropertyModel<Boolean>(
  //        getSearchFilter(), "reopened"), getString(ToDoStatus.RE_OPENED.getI18nKey())));
  //    optionsCheckBoxesPanel.add(createAutoRefreshCheckBoxButton(optionsCheckBoxesPanel.newChildId(), new PropertyModel<Boolean>(
  //        getSearchFilter(), "inprogress"), getString(ToDoStatus.IN_PROGRESS.getI18nKey())));
  //    optionsCheckBoxesPanel.add(createAutoRefreshCheckBoxButton(optionsCheckBoxesPanel.newChildId(), new PropertyModel<Boolean>(
  //        getSearchFilter(), "closed"), getString(ToDoStatus.CLOSED.getI18nKey())));
  //    optionsCheckBoxesPanel.add(createAutoRefreshCheckBoxButton(optionsCheckBoxesPanel.newChildId(), new PropertyModel<Boolean>(
  //        getSearchFilter(), "postponed"), getString(ToDoStatus.POSTPONED.getI18nKey())));
  //    optionsCheckBoxesPanel
  //    .add(createAutoRefreshCheckBoxButton(optionsCheckBoxesPanel.newChildId(),
  //        new PropertyModel<Boolean>(getSearchFilter(), "onlyRecent"), getString("plugins.todo.status.onlyRecent"),
  //        getString("plugins.todo.status.onlyRecent.tooltip")));
  //  }

  @Override
  protected LocalInvitationFilter newSearchFilterInstance()
  {
    return new LocalInvitationFilter();
  }

  @Override
  protected Logger getLogger()
  {
    return log;
  }
}
