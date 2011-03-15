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

package org.projectforge.web.admin;

import static org.projectforge.web.wicket.layout.SelectLPanel.WICKET_ID_SELECT_PANEL;

import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.projectforge.task.TaskDO;
import org.projectforge.task.TaskTree;
import org.projectforge.user.GroupDO;
import org.projectforge.web.task.TaskEditPage;
import org.projectforge.web.task.TaskSelectPanel;
import org.projectforge.web.user.GroupEditPage;
import org.projectforge.web.user.GroupSelectPanel;
import org.projectforge.web.wicket.WebConstants;
import org.projectforge.web.wicket.WicketUtils;
import org.projectforge.web.wicket.components.SingleButtonPanel;
import org.projectforge.web.wicket.layout.AbstractFormRenderer;
import org.projectforge.web.wicket.layout.LabelLPanel;
import org.projectforge.web.wicket.layout.LayoutContext;
import org.projectforge.web.wicket.layout.LayoutLength;
import org.projectforge.web.wicket.layout.PanelContext;

/**
 * @author Kai Reinhard (k.reinhard@micromata.de)
 * 
 */
public class TaskWizardFormRenderer extends AbstractFormRenderer
{
  private static final long serialVersionUID = 4515982116827709004L;

  final static LayoutLength LABEL_LENGTH = LayoutLength.HALF;

  final static LayoutLength VALUE_LENGTH = LayoutLength.ONEHALF;

  final static LayoutLength FULL_LENGTH = LayoutLength.DOUBLE;

  private TaskWizardPage wizardPage;

  private TaskWizardForm form;

  private TaskTree taskTree;

  public TaskWizardFormRenderer(final TaskWizardForm container, final LayoutContext layoutContext, final TaskWizardPage wizardPage,
      final TaskTree taskTree)
  {
    super(container, layoutContext);
    this.form = container;
    this.wizardPage = wizardPage;
    this.taskTree = taskTree;
  }

  @SuppressWarnings("serial")
  @Override
  public void add()
  {
    doPanel.newFieldSetPanel(getString("wizard"));
    doPanel.addHelpLabel(getString("task.wizard.intro"), new PanelContext(FULL_LENGTH));
    int number = 1;
    doPanel.newGroupPanel(String.valueOf(number++) + ". " + getString("task"));
    {
      final TaskSelectPanel taskSelectPanel = new TaskSelectPanel(WICKET_ID_SELECT_PANEL, new PropertyModel<TaskDO>(form, "task"),
          wizardPage, "taskId");
      doPanel.addSelectPanel(taskSelectPanel, new PanelContext(VALUE_LENGTH, getString("task"), LABEL_LENGTH));
      taskSelectPanel.setShowFavorites(false).init();
      taskSelectPanel.setRequired(true);
    }
    {
      doPanel.addLabel("", new PanelContext(LABEL_LENGTH));
      final RepeatingView repeatingView = doPanel.addRepeater(new PanelContext(VALUE_LENGTH)).getRepeatingView();
      final Button button = new Button("button", new Model<String>(getString("task.wizard.button.createTask"))) {
        @Override
        public final void onSubmit()
        {
          final PageParameters params = new PageParameters();
          params.put(TaskEditPage.PARAM_PARENT_TASK_ID, taskTree.getRootTaskNode().getId());
          final TaskEditPage editPage = new TaskEditPage(params);
          editPage.setReturnToPage(wizardPage);
          setResponsePage(editPage);
        }
      };
      button.setDefaultFormProcessing(false);
      button.add(WebConstants.BUTTON_CLASS);
      SingleButtonPanel buttonPanel = new SingleButtonPanel(repeatingView.newChildId(), button);
      WicketUtils.addTooltip(button, getString("task.wizard.button.createTask.tooltip"));
      repeatingView.add(buttonPanel);
    }

    // Manager group
    createGroupComponents(number++, "managerGroup");

    // Team
    createGroupComponents(number++, "team");

    doPanel.newGroupPanel(getString("task.wizard.action"));
    doPanel.addLabel(new Label(LabelLPanel.LABEL_ID, new Model<String>() {
      @Override
      public String getObject()
      {
        if (wizardPage.actionRequired() == true) {
          return getString("task.wizard.action.taskAndgroupsGiven");
        } else {
          return getString("task.wizard.action.noactionRequired");
        }
      }
    }), new PanelContext(FULL_LENGTH));
  }

  @SuppressWarnings("serial")
  private void createGroupComponents(final int number, final String key)
  {
    doPanel.newGroupPanel(number + ". " + getString("task.wizard." + key));
    doPanel.addHelpLabel(getString("task.wizard." + key + ".intro"), new PanelContext(FULL_LENGTH));
    {
      final GroupSelectPanel groupSelectPanel = new GroupSelectPanel(WICKET_ID_SELECT_PANEL, new PropertyModel<GroupDO>(form, key),
          wizardPage, key + "Id");
      doPanel.addSelectPanel(groupSelectPanel, new PanelContext(VALUE_LENGTH, getString("group"), LABEL_LENGTH));
      groupSelectPanel.setShowFavorites(false).init();
    }
    {
      doPanel.addLabel("", new PanelContext(LABEL_LENGTH));
      final RepeatingView repeatingView = doPanel.addRepeater(new PanelContext(VALUE_LENGTH)).getRepeatingView();
      final Button button = new Button("button", new Model<String>(getString("task.wizard.button.createGroup"))) {
        @Override
        public final void onSubmit()
        {
          wizardPage.managerGroupCreated = "managerGroup".equals(key);
          final PageParameters params = new PageParameters();
          final StringBuffer buf = new StringBuffer();
          if (form.task != null) {
            buf.append(form.task.getTitle());
          }
          if (wizardPage.managerGroupCreated == true) {
            if (form.task != null) {
              buf.append(", ");
            }
            buf.append(getString("task.wizard.managerGroup.groupNameSuffix"));
          }
          params.put(GroupEditPage.PARAM_GROUP_NAME, buf.toString());
          final GroupEditPage editPage = new GroupEditPage(params);
          editPage.setReturnToPage(wizardPage);
          setResponsePage(editPage);
        }

        @Override
        public boolean isVisible()
        {
          return form.task != null;
        }
      };
      button.setDefaultFormProcessing(false);
      button.add(WebConstants.BUTTON_CLASS);
      SingleButtonPanel buttonPanel = new SingleButtonPanel(repeatingView.newChildId(), button);
      WicketUtils.addTooltip(button, getString("task.wizard.button.createGroup.tooltip"));
      repeatingView.add(buttonPanel);
    }
  }
}
