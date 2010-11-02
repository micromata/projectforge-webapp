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

package org.projectforge.web.task;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.SubmitLink;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.hibernate.Hibernate;
import org.projectforge.task.TaskDO;
import org.projectforge.task.TaskFavorite;
import org.projectforge.task.TaskTree;
import org.projectforge.user.UserPrefArea;
import org.projectforge.web.fibu.ISelectCallerPage;
import org.projectforge.web.wicket.AbstractSelectPanel;
import org.projectforge.web.wicket.WebConstants;
import org.projectforge.web.wicket.WicketLocalizerAndUrlBuilder;
import org.projectforge.web.wicket.components.FavoritesChoicePanel;
import org.projectforge.web.wicket.components.TooltipImage;

/**
 * Panel for showing and selecting one task.
 * @author Kai Reinhard (k.reinhard@micromata.de)
 * 
 */
public class TaskSelectPanel extends AbstractSelectPanel<TaskDO>
{
  private static final long serialVersionUID = -7231190025292695850L;

  @SpringBean(name = "taskFormatter")
  private TaskFormatter taskFormatter;

  @SpringBean(name = "taskTree")
  private TaskTree taskTree;

  private boolean enableLinks;

  private boolean showPath = true;

  public TaskSelectPanel(final String id, final IModel<TaskDO> model, final ISelectCallerPage caller, final String selectProperty)
  {
    super(id, model, caller, selectProperty);
    TaskDO task = model.getObject();
    if (Hibernate.isInitialized(task) == false) {
      task = taskTree.getTaskById(task.getId());
      model.setObject(task);
    }
  }

  @SuppressWarnings("serial")
  public TaskSelectPanel init()
  {
    super.init();
    // Todo: replace taskAsString with Wicket mechanism
    final Label taskAsStringLabel = new Label("taskAsString", new Model<String>() {
      @Override
      public String getObject()
      {
        final TaskDO task = getModelObject();
        if (task == null) {
          return "";
        } else if (showPath == true) {
          return taskFormatter.getTaskPath(new WicketLocalizerAndUrlBuilder(getResponse()), task.getId(), null, enableLinks, true);
        } else {
          return task.getTitle();
        }
      }
    });
    taskAsStringLabel.setEscapeModelStrings(false);
    add(taskAsStringLabel);
    final SubmitLink selectButton = new SubmitLink("select") {
      public void onSubmit()
      {
        final TaskTreePage taskTreePage = new TaskTreePage(caller, selectProperty);
        if (getModelObject() != null) {
          taskTreePage.setEventNode(getModelObject().getId()); // Preselect node for highlighting.
        }
        setResponsePage(taskTreePage);
      };
    };
    selectButton.setDefaultFormProcessing(false);
    add(selectButton);
    selectButton.add(new TooltipImage("selectHelp", getResponse(), WebConstants.IMAGE_TASK_SELECT, getString("tooltip.selectTask")));
    final SubmitLink unselectButton = new SubmitLink("unselect") {
      @Override
      public void onSubmit()
      {
        caller.unselect(selectProperty);
      }

      @Override
      public boolean isVisible()
      {
        return isRequired() == false && getModelObject() != null;
      }
    };
    unselectButton.setDefaultFormProcessing(false);
    add(unselectButton);
    unselectButton
        .add(new TooltipImage("unselectHelp", getResponse(), WebConstants.IMAGE_TASK_UNSELECT, getString("tooltip.unselectTask")));
    // DropDownChoice favorites
    final FavoritesChoicePanel<TaskDO, TaskFavorite> favoritesPanel = new FavoritesChoicePanel<TaskDO, TaskFavorite>("favorites",
        UserPrefArea.TASK_FAVORITE, tabIndex) {
      @Override
      protected void select(final TaskFavorite favorite)
      {
        if (favorite.getTask() != null) {
          TaskSelectPanel.this.selectTask(favorite.getTask());
        }
      }

      @Override
      protected TaskDO getCurrentObject()
      {
        return TaskSelectPanel.this.getModelObject();
      }

      @Override
      protected TaskFavorite newFavoriteInstance(final TaskDO currentObject)
      {
        final TaskFavorite favorite = new TaskFavorite();
        favorite.setTask(currentObject);
        return favorite;
      }
    };
    add(favoritesPanel);
    favoritesPanel.init();
    if (showFavorites == false) {
      favoritesPanel.setVisible(false);
    }
    return this;
  }

  /**
   * Will be called if the user has chosen an entry of the task favorites drop down choice.
   * @param task
   */
  protected void selectTask(final TaskDO task)
  {
    setModelObject(task);
    caller.select(selectProperty, task.getId());
  }

  @Override
  protected void convertInput()
  {
    setConvertedInput(getModelObject());
  }

  /**
   * @param enableLinks true for showing click-able ancestor tasks, false for no links.
   */
  public TaskSelectPanel setEnableLinks(boolean enableLinks)
  {
    this.enableLinks = enableLinks;
    return this;
  }

  /**
   * If true (default) then the path from the root task to the currently selected will be shown, otherwise only the name of the task is
   * displayed.
   * @param showPath
   */
  public void setShowPath(boolean showPath)
  {
    this.showPath = showPath;
  }
}
