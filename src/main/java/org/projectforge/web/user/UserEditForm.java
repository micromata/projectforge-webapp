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

package org.projectforge.web.user;

import org.apache.log4j.Logger;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.projectforge.user.GroupDao;
import org.projectforge.user.PFUserDO;
import org.projectforge.user.UserRightDao;
import org.projectforge.web.wicket.AbstractEditForm;
import org.projectforge.web.wicket.layout.LayoutContext;

public class UserEditForm extends AbstractEditForm<PFUserDO, UserEditPage>
{
  private static final long serialVersionUID = 7872294377838461659L;

  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(UserEditForm.class);

  @SpringBean(name = "userRightDao")
  private UserRightDao userRightDao;

  @SpringBean(name = "groupDao")
  private GroupDao groupDao;

  protected UserFormRenderer renderer;

  public UserEditForm(final UserEditPage parentPage, final PFUserDO data)
  {
    super(parentPage, data);
    renderer = new UserFormRenderer(this, parentPage, new LayoutContext(this), parentPage.getBaseDao(), userRightDao, groupDao, data);
  }

  @Override
  protected void init()
  {
    super.init();
    renderer.add();
  }
  
  @Override
  protected void validation()
  {
    super.validation();
    renderer.validation();
  }

  @Override
  protected Logger getLogger()
  {
    return log;
  }
}
