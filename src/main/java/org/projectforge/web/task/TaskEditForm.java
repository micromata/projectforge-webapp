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

package org.projectforge.web.task;

import java.math.BigDecimal;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.projectforge.task.TaskDO;
import org.projectforge.user.UserGroupCache;
import org.projectforge.web.wicket.AbstractEditForm;
import org.projectforge.web.wicket.layout.LayoutContext;

public class TaskEditForm extends AbstractEditForm<TaskDO, TaskEditPage>
{
  private static final long serialVersionUID = -3784956996856970327L;

  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(TaskEditForm.class);

  public static final BigDecimal MAX_DURATION_DAYS = new BigDecimal(10000);

  @SpringBean(name = "userGroupCache")
  private UserGroupCache userGroupCache;

  protected TaskFormRenderer renderer;

  public TaskEditForm(TaskEditPage parentPage, TaskDO data)
  {
    super(parentPage, data);
    renderer = new TaskFormRenderer(parentPage, this, new LayoutContext(this), parentPage.getBaseDao(), data);
    renderer.userGroupCache = this.userGroupCache;
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
    if (StringUtils.isNotBlank(renderer.durationField.getInput()) == true
        && StringUtils.isNotBlank(renderer.endDatePanel.getInput()) == true) {
      addError("gantt.error.durationAndEndDateAreMutuallyExclusive");
    }
  }

  @Override
  protected Logger getLogger()
  {
    return log;
  }
}
