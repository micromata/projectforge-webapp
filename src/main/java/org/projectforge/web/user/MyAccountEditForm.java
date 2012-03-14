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

package org.projectforge.web.user;

import org.apache.log4j.Logger;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.projectforge.access.AccessChecker;
import org.projectforge.user.GroupDao;
import org.projectforge.user.PFUserDO;
import org.projectforge.user.UserRightDao;
import org.projectforge.web.wicket.AbstractEditForm;

public class MyAccountEditForm extends AbstractEditForm<PFUserDO, MyAccountEditPage>
{
  private static final long serialVersionUID = 4137560623244324454L;

  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(MyAccountEditForm.class);

  @SpringBean(name = "accessChecker")
  private AccessChecker accessChecker;

  @SpringBean(name = "userRightDao")
  private UserRightDao userRightDao;

  @SpringBean(name = "groupDao")
  private GroupDao groupDao;


  public MyAccountEditForm(final MyAccountEditPage parentPage, final PFUserDO data)
  {
    super(parentPage, data);
  }

  @Override
  protected void init()
  {
    super.init();
  }

  @Override
  public void updateButtonVisibility()
  {
    super.updateButtonVisibility();
    createButtonPanel.setVisible(false);
    updateButtonPanel.setVisible(true);
    deleteButtonPanel.setVisible(false);
    markAsDeletedButtonPanel.setVisible(false);
    undeleteButtonPanel.setVisible(false);
  }

  @Override
  protected Logger getLogger()
  {
    return log;
  }
}
