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

package org.projectforge.plugins.todo;

import org.apache.log4j.Logger;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.projectforge.user.PFUserContext;
import org.projectforge.user.UserGroupCache;
import org.projectforge.web.wicket.AbstractEditForm;
import org.projectforge.web.wicket.layout.LayoutContext;

public class ToDoEditForm extends AbstractEditForm<ToDoDO, ToDoEditPage>
{
  private static final long serialVersionUID = -6208809585214296635L;

  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(ToDoEditForm.class);

  @SpringBean(name = "userGroupCache")
  private UserGroupCache userGroupCache;

  protected ToDoFormRenderer renderer;

  public ToDoEditForm(ToDoEditPage parentPage, ToDoDO data)
  {
    super(parentPage, data);
    if (isNew() == true) {
      data.setAssignee(PFUserContext.getUser());
      data.setReporter(PFUserContext.getUser());
    }
    renderer = new ToDoFormRenderer(parentPage, this, new LayoutContext(this), data, userGroupCache);
  }

  @Override
  protected void init()
  {
    super.init();
    renderer.add();
  }

  @Override
  protected Logger getLogger()
  {
    return log;
  }
}
