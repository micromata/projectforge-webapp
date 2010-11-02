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

package org.projectforge.web.user;

import net.sourceforge.stripes.validation.Validate;

import org.apache.log4j.Logger;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.model.PropertyModel;
import org.projectforge.user.PFUserDO;
import org.projectforge.web.wicket.WebConstants;
import org.projectforge.web.wicket.components.TooltipImage;

public class MyAccountEditForm extends UserBaseEditForm<MyAccountEditPage>
{
  private static final long serialVersionUID = 4137560623244324454L;

  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(MyAccountEditForm.class);

  boolean invalidateAllStayLoggedInSessions;

  public MyAccountEditForm(MyAccountEditPage parentPage, PFUserDO data)
  {
    super(parentPage, data);
  }

  @Override
  protected void init()
  {
    super.init();
    add(new Label("username", getData().getUsername()));
    add(new CheckBox("invalidateAllStayLoggedInSessions", new PropertyModel<Boolean>(this, "invalidateAllStayLoggedInSessions")));
    add(new TooltipImage("invalidateAllStayLoggedInSessionsTooltipImage", getResponse(), WebConstants.IMAGE_HELP,
        getString("login.stayLoggedIn.invalidateAllStayLoggedInSessions.tooltip")));
    add(new Label("groupnames", userDao.getGroupnames(getData())));
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

  /**
   * If true then update button results in generating a new stay-logged-in key, therefore any existing stay-logged-in session will be
   * invalid.
   * @return
   */
  @Validate
  public boolean isInvalidateAllStayLoggedInSessions()
  {
    return invalidateAllStayLoggedInSessions;
  }

  public void setInvalidateAllStayLoggedInSessions(boolean invalidateAllStayLoggedInSessions)
  {
    this.invalidateAllStayLoggedInSessions = invalidateAllStayLoggedInSessions;
  }

  @Override
  protected Logger getLogger()
  {
    return log;
  }
}
