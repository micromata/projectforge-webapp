/////////////////////////////////////////////////////////////////////////////
//
// Project ProjectForge Community Edition
//         www.projectforge.org
//
// Copyright (C) 2001-2013 Kai Reinhard (k.reinhard@micromata.de)
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

package org.projectforge.web.admin;

import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.projectforge.access.AccessChecker;
import org.projectforge.access.AccessException;
import org.projectforge.admin.SystemUpdater;
import org.projectforge.admin.UpdateEntry;
import org.projectforge.user.Login;
import org.projectforge.user.PFUserContext;
import org.projectforge.user.ProjectForgeGroup;
import org.projectforge.web.wicket.AbstractSecuredPage;

public class SystemUpdatePage extends AbstractSecuredPage
{
  private static final long serialVersionUID = -7624191773850329338L;

  @SpringBean(name = "systemUpdater")
  protected SystemUpdater systemUpdater;

  private final SystemUpdateForm form;

  public SystemUpdatePage(final PageParameters parameters)
  {
    super(parameters);
    form = new SystemUpdateForm(this);
    body.add(form);
    form.init();
    refresh();
  }

  protected void update(final UpdateEntry updateEntry)
  {
    checkAdminUser();
    accessChecker.checkRestrictedOrDemoUser();
    systemUpdater.update(updateEntry);
    refresh();
  }

  protected void refresh()
  {
    checkAdminUser();
    systemUpdater.runAllPreChecks();
    form.updateEntryRows();
  }

  private void checkAdminUser()
  {
    if (Login.getInstance().isAdminUser(PFUserContext.getUser()) == false) {
      throw new AccessException(AccessChecker.I18N_KEY_VIOLATION_USER_NOT_MEMBER_OF, ProjectForgeGroup.ADMIN_GROUP.getKey());
    }
  }

  @Override
  protected String getTitle()
  {
    return getString("system.update.title");
  }
}
