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

package org.projectforge.web.admin;

import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.Model;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.projectforge.task.TaskDO;
import org.projectforge.task.TaskTree;
import org.projectforge.user.GroupDO;
import org.projectforge.web.wicket.AbstractForm;
import org.projectforge.web.wicket.WebConstants;
import org.projectforge.web.wicket.components.SingleButtonPanel;
import org.projectforge.web.wicket.layout.LayoutContext;

public class TaskWizardForm extends AbstractForm<TaskWizardForm, TaskWizardPage>
{
  private static final long serialVersionUID = -2450673501083584299L;

  private TaskWizardFormRenderer renderer;

  @SpringBean(name = "taskTree")
  private TaskTree taskTree;

  protected TaskDO task;

  protected GroupDO managerGroup, team;

  public TaskWizardForm(final TaskWizardPage parentPage)
  {
    super(parentPage);
    renderer = new TaskWizardFormRenderer(this, new LayoutContext(false), parentPage, taskTree);
  }

  @SuppressWarnings("serial")
  protected void init()
  {
    add(new FeedbackPanel("feedback").setOutputMarkupId(true));
    renderer.add();
    final Button createButton = new Button("button", new Model<String>(getString("task.wizard.finish"))) {
      @Override
      public final void onSubmit()
      {
        parentPage.create();
      }

      @Override
      public boolean isVisible()
      {
        return parentPage.actionRequired();
      }
    };
    createButton.add(WebConstants.BUTTON_CLASS_DEFAULT);
    setDefaultButton(createButton);
    final SingleButtonPanel createButtonPanel = new SingleButtonPanel("finish", createButton);
    add(createButtonPanel);
  }
}
