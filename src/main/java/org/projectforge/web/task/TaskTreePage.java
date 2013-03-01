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

package org.projectforge.web.task;

import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.projectforge.access.AccessChecker;
import org.projectforge.common.StringHelper;
import org.projectforge.fibu.kost.KostCache;
import org.projectforge.task.TaskDao;
import org.projectforge.task.TaskFilter;
import org.projectforge.user.ProjectForgeGroup;
import org.projectforge.user.UserGroupCache;
import org.projectforge.user.UserPrefArea;
import org.projectforge.web.admin.TaskWizardPage;
import org.projectforge.web.calendar.DateTimeFormatter;
import org.projectforge.web.core.PriorityFormatter;
import org.projectforge.web.fibu.ISelectCallerPage;
import org.projectforge.web.user.UserFormatter;
import org.projectforge.web.user.UserPrefListPage;
import org.projectforge.web.wicket.AbstractEditPage;
import org.projectforge.web.wicket.AbstractListPage;
import org.projectforge.web.wicket.AbstractReindexTopRightMenu;
import org.projectforge.web.wicket.AbstractSecuredPage;
import org.projectforge.web.wicket.WebConstants;
import org.projectforge.web.wicket.WicketUtils;
import org.projectforge.web.wicket.components.ContentMenuEntryPanel;
import org.projectforge.web.wicket.flowlayout.IconType;

/**
 * Shows the task tree for selection.
 * @author Kai Reinhard (k.reinhard@micromata.de)
 * 
 */
public class TaskTreePage extends AbstractSecuredPage
{
  private static final long serialVersionUID = -8406452960003792763L;

  static final String USER_PREFS_KEY_OPEN_TASKS = "openTasks";

  private static final String I18N_PREFIX = "task";

  @SpringBean(name = "accessChecker")
  private AccessChecker accessChecker;

  @SpringBean(name = "userFormatter")
  private UserFormatter userFormatter;

  @SpringBean(name = "dateTimeFormatter")
  private DateTimeFormatter dateTimeFormatter;

  @SpringBean(name = "kostCache")
  private KostCache kostCache;

  @SpringBean(name = "priorityFormatter")
  private PriorityFormatter priorityFormatter;

  @SpringBean(name = "taskFormatter")
  private TaskFormatter taskFormatter;

  @SpringBean(name = "taskDao")
  private TaskDao taskDao;

  @SpringBean(name = "userGroupCache")
  private UserGroupCache userGroupCache;

  protected ISelectCallerPage caller;

  protected String selectProperty;

  TaskTreeForm form;

  private TaskTreeBuilder taskTreeBuilder;

  /**
   * Sibling page (if the user switches between tree and list view.
   */
  private TaskListPage taskListPage;

  public TaskTreePage(final PageParameters parameters)
  {
    super(parameters);
    init();
    if (WicketUtils.contains(parameters, AbstractListPage.PARAMETER_HIGHLIGHTED_ROW) == true) {
      taskTreeBuilder.setHighlightedTaskNodeId(WicketUtils.getAsInteger(parameters, AbstractListPage.PARAMETER_HIGHLIGHTED_ROW));
    }
  }

  /**
   * Called if the user clicks on button "tree view".
   * @param taskTreePage
   * @param parameters
   */
  TaskTreePage(final TaskListPage taskListPage, final PageParameters parameters)
  {
    this(taskListPage.getCaller(), taskListPage.getSelectProperty());
    this.taskListPage = taskListPage;
  }

  public TaskTreePage(final ISelectCallerPage caller, final String selectProperty)
  {
    super(new PageParameters());
    this.caller = caller;
    this.selectProperty = selectProperty;
    init();
  }

  public void setHighlightedRowId(final Integer highlightedRowId)
  {
    taskTreeBuilder.setHighlightedTaskNodeId(highlightedRowId);
  }

  @SuppressWarnings("serial")
  private void init()
  {

    if (isSelectMode() == false) {
      ContentMenuEntryPanel menuEntry = new ContentMenuEntryPanel(getNewContentMenuChildId(), new Link<Object>("link") {
        @Override
        public void onClick()
        {
          final PageParameters params = new PageParameters();
          final AbstractEditPage< ? , ? , ? > editPage = new TaskEditPage(params);
          editPage.setReturnToPage(TaskTreePage.this);
          setResponsePage(editPage);
        };
      }, IconType.PLUS);
      menuEntry.setAccessKey(WebConstants.ACCESS_KEY_ADD).setTooltip(getString(WebConstants.ACCESS_KEY_ADD_TOOLTIP_TITLE),
          getString(WebConstants.ACCESS_KEY_ADD_TOOLTIP));
      addContentMenuEntry(menuEntry);

      final BookmarkablePageLink<Void> addTemplatesLink = UserPrefListPage.createLink("link", UserPrefArea.TASK_FAVORITE);
      menuEntry = new ContentMenuEntryPanel(getNewContentMenuChildId(), addTemplatesLink, getString("favorites"));
      addContentMenuEntry(menuEntry);
      if (accessChecker.isLoggedInUserMemberOfAdminGroup() == true) {
        menuEntry = new ContentMenuEntryPanel(getNewContentMenuChildId(), new Link<Object>("link") {
          @Override
          public void onClick()
          {
            final PageParameters params = new PageParameters();
            final TaskWizardPage wizardPage = new TaskWizardPage(params);
            wizardPage.setReturnToPage(TaskTreePage.this);
            setResponsePage(wizardPage);
          };
        }, getString("wizard"));
        addContentMenuEntry(menuEntry);
      }
      new AbstractReindexTopRightMenu(contentMenuBarPanel, accessChecker.isLoggedInUserMemberOfAdminGroup()) {
        @Override
        protected void rebuildDatabaseIndex(final boolean onlyNewest)
        {
          if (onlyNewest == true) {
            taskDao.rebuildDatabaseIndex4NewestEntries();
          } else {
            taskDao.rebuildDatabaseIndex();
          }
        }

        @Override
        protected String getString(final String i18nKey)
        {
          return TaskTreePage.this.getString(i18nKey);
        }
      };
    }
    form = new TaskTreeForm(this);
    body.add(form);
    form.init();
    taskTreeBuilder = new TaskTreeBuilder().setSelectMode(isSelectMode()).setShowRootNode(isShowRootNode())
        .setShowCost(kostCache.isKost2EntriesExists());
    if (accessChecker.isLoggedInUserMemberOfGroup(ProjectForgeGroup.FINANCE_GROUP, ProjectForgeGroup.CONTROLLING_GROUP,
        ProjectForgeGroup.PROJECT_ASSISTANT, ProjectForgeGroup.PROJECT_MANAGER) == true) {
      taskTreeBuilder.setShowOrders(true);
    }
    taskTreeBuilder.set(accessChecker, taskDao, taskFormatter, priorityFormatter, userFormatter, dateTimeFormatter, userGroupCache);
    taskTreeBuilder.setCaller(caller).setSelectProperty(selectProperty);
    form.add(taskTreeBuilder.createTree("tree", this, form.getSearchFilter()));

    body.add(new Label("info", new Model<String>(getString("task.tree.info"))));

  }

  public void refresh()
  {
    form.getSearchFilter().resetMatch();
  }

  @Override
  protected void onBodyTag(final ComponentTag bodyTag)
  {
    // if (taskTreeTablePanel.getEventNode() != null) {
    // // Show the selected task entry on top:
    // bodyTag.put("onload", "javascript:self.location.href='#clickedEntry'");
    // }
  }

  /**
   * The root node will only be shown for admin users and financial staff.
   */
  boolean isShowRootNode()
  {
    return (accessChecker.isLoggedInUserMemberOfAdminGroup()) || accessChecker.isLoggedInUserMemberOfGroup(ProjectForgeGroup.FINANCE_GROUP);
  }

  protected String getSearchToolTip()
  {
    return getLocalizedMessage("search.string.info", getSearchFields());
  }

  public String getSearchFields()
  {
    return StringHelper.listToString(", ", taskDao.getSearchFields());
  }

  /**
   * @return true, if this page is called for selection by a caller otherwise false.
   */
  public boolean isSelectMode()
  {
    return this.caller != null;
  }

  @Override
  protected String getTitle()
  {
    if (isSelectMode() == true) {
      return getString(I18N_PREFIX + ".title.list.select");
    } else {
      return getString(I18N_PREFIX + ".title.list");
    }
  }

  protected void onSearchSubmit()
  {
    refresh();
  }

  void onListViewSubmit()
  {
    if (taskListPage != null) {
      setResponsePage(taskListPage);
    } else {
      setResponsePage(new TaskListPage(this, getPageParameters()));
    }
  }

  protected void onResetSubmit()
  {
    form.getSearchFilter().reset();
    refresh();
    form.clearInput();
  }

  protected void onCancelSubmit()
  {
    if (isSelectMode() == true) {
      WicketUtils.setResponsePage(this, caller);
      caller.cancelSelection(selectProperty);
    }
  }

  TaskFilter getTaskFilter()
  {
    return form.getSearchFilter();
  }
}
