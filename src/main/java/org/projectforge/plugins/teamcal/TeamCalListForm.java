/////////////////////////////////////////////////////////////////////////////
//
// Project   ProjectForge
//
// Copyright 2001-2009, Micromata GmbH, Kai Reinhard
//           All rights reserved.
//
/////////////////////////////////////////////////////////////////////////////

package org.projectforge.plugins.teamcal;

import org.apache.log4j.Logger;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.projectforge.user.PFUserContext;
import org.projectforge.user.PFUserDO;
import org.projectforge.web.user.UserSelectPanel;
import org.projectforge.web.wicket.AbstractListForm;
import org.projectforge.web.wicket.flowlayout.DivPanel;
import org.projectforge.web.wicket.flowlayout.DivType;
import org.projectforge.web.wicket.flowlayout.FieldsetPanel;

/**
 * @author M. Lauterbach (m.lauterbach@micromata.de)
 *
 */
public class TeamCalListForm extends AbstractListForm<TeamCalFilter, TeamCalListPage>
{
  private static final long serialVersionUID = 3659495003810851072L;

  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(TeamCalListForm.class);

  private TeamCalFilter filter;

  public TeamCalListForm(final TeamCalListPage parentPage){
    super(parentPage);
    filter = newSearchFilterInstance();
    final boolean access = false;
    filter.setOwnerId(getUserId());
    filter.setFullAccess(access);
    filter.setReadOnlyAccess(access);
    filter.setMinimalAccess(access);
  }

  /**
   * @see org.projectforge.web.wicket.AbstractListForm#newSearchFilterInstance()
   */
  @Override
  protected TeamCalFilter newSearchFilterInstance()
  {
    return new TeamCalFilter();
  }

  /**
   * @see org.projectforge.web.wicket.AbstractListForm#init()
   */
  @SuppressWarnings("serial")
  @Override
  protected void init()
  {
    super.init();
    gridBuilder.newColumnsPanel();
    gridBuilder.newColumnPanel(DivType.COL_66);
    {
      final FieldsetPanel fs = gridBuilder.newFieldset(getString("label.options"), true).setNoLabelFor();
      fs.setOutputMarkupId(true);
      final Model<PFUserDO> model = new Model<PFUserDO>() {
        @Override
        public PFUserDO getObject()
        {
          //          if (getSearchFilter().getOwnerId() == null)
          //            getSearchFilter().setOwnerId(PFUserContext.getUserId());
          return userGroupCache.getUser(getSearchFilter().getOwnerId());
        }

        @Override
        public void setObject(final PFUserDO object)
        {
          if (object == null) {
            getSearchFilter().setOwnerId(PFUserContext.getUserId());
          } else {
            getSearchFilter().setOwnerId(object.getId());
          }
          parentPage.refresh();
        }
      };
      final UserSelectPanel userSelectPanel = new UserSelectPanel(fs.newChildId(), model, parentPage, "ownerId");
      fs.add(userSelectPanel);
      userSelectPanel.setDefaultFormProcessing(false);
      userSelectPanel.init().withAutoSubmit(true);

      final DivPanel checkBoxPanel = fs.addNewCheckBoxDiv();
      checkBoxPanel.add(createAutoRefreshCheckBoxPanel(checkBoxPanel.newChildId(),
          new PropertyModel<Boolean>(getSearchFilter(), "fullAccess"), getString("plugins.teamcal.fullAccess")));
      checkBoxPanel.add(createAutoRefreshCheckBoxPanel(checkBoxPanel.newChildId(),
          new PropertyModel<Boolean>(getSearchFilter(), "readOnlyAccess"), getString("plugins.teamcal.readOnlyAccess")));
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
  public TeamCalFilter getFilter()
  {
    return getSearchFilter();
  }

  /**
   * @param filter the filter to set
   */
  public void setFilter(final TeamCalFilter filter)
  {
    this.filter = filter;
  }
}
