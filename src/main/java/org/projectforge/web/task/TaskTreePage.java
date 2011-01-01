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

import org.apache.commons.lang.StringUtils;
import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.projectforge.common.StringHelper;
import org.projectforge.task.TaskDao;
import org.projectforge.task.TaskFilter;
import org.projectforge.user.ProjectForgeGroup;
import org.projectforge.web.fibu.ISelectCallerPage;
import org.projectforge.web.wicket.AbstractEditPage;
import org.projectforge.web.wicket.AbstractListPage;
import org.projectforge.web.wicket.AbstractReindexTopRightMenu;
import org.projectforge.web.wicket.AbstractSecuredPage;
import org.projectforge.web.wicket.WicketUtils;
import org.projectforge.web.wicket.components.ContentMenuEntryPanel;

/**
 * Shows the task tree for selection.
 * @author Kai Reinhard (k.reinhard@micromata.de)
 * 
 */
public class TaskTreePage extends AbstractSecuredPage
{
  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(TaskTreePage.class);

  private static final long serialVersionUID = -8406452960003792763L;

  static final String USER_PREFS_KEY_OPEN_TASKS = "openTasks";

  private static final String I18N_PREFIX = "task";

  @SpringBean(name = "taskDao")
  private TaskDao taskDao;

  protected ISelectCallerPage caller;

  protected String selectProperty;

  TaskTreeForm form;

  private TaskTreeTablePanel taskTreeTablePanel;

  public TaskTreePage(PageParameters parameters)
  {
    super(parameters);
    taskTreeTablePanel = new TaskTreeTablePanel("taskTree", this);
    if (parameters.containsKey(AbstractListPage.PARAMETER_HIGHLIGHTED_ROW) == true) {
      taskTreeTablePanel.setHighlightedRowId(parameters.getAsInteger(AbstractListPage.PARAMETER_HIGHLIGHTED_ROW));
    }
    init();
  }

  public TaskTreePage(final ISelectCallerPage caller, final String selectProperty)
  {
    super(new PageParameters());
    this.caller = caller;
    this.selectProperty = selectProperty;
    taskTreeTablePanel = new TaskTreeTablePanel("taskTree", caller, selectProperty, this);
    init();
  }

  public void setHighlightedRowId(final Integer highlightedRowId)
  {
    taskTreeTablePanel.setHighlightedRowId(highlightedRowId);
  }

  @SuppressWarnings("serial")
  private void init()
  {
    if (isSelectMode() == false) {
      final ContentMenuEntryPanel newItemMenuEntry = new ContentMenuEntryPanel(getNewContentMenuChildId(), new Link<Object>("link") {
        @Override
        public void onClick()
        {
          final PageParameters params = new PageParameters();
          final AbstractEditPage< ? , ? , ? > editPage = new TaskEditPage(params);
          editPage.setReturnToPage(TaskTreePage.this);
          setResponsePage(editPage);
        };
      }, getString("add"));
      contentMenuEntries.add(newItemMenuEntry);
      dropDownMenu.setVisible(true);
      new AbstractReindexTopRightMenu(this, accessChecker.isUserMemberOfAdminGroup()) {
        @Override
        protected void rebuildDatabaseIndex(boolean onlyNewest)
        {
          if (onlyNewest == true) {
            taskDao.rebuildDatabaseIndex4NewestEntries();
          } else {
            taskDao.rebuildDatabaseIndex();
          }
        }

        @Override
        protected String getString(String i18nKey)
        {
          return TaskTreePage.this.getString(i18nKey);
        }
      };
    }
    body.add(taskTreeTablePanel);
    taskTreeTablePanel.init();
    form = new TaskTreeForm(this);
    body.add(form);
    form.init();
    body.add(new FeedbackPanel("feedback").setOutputMarkupId(true));
  }

  public void refresh()
  {
    taskTreeTablePanel.refresh();
  }

  @Override
  protected void onBodyTag(ComponentTag bodyTag)
  {
    if (taskTreeTablePanel.getEventNode() != null) {
      // Show the selected task entry on top:
      bodyTag.put("onload", "javascript:self.location.href='#clickedEntry'");
    }
  }

  /**
   * The root node will only be shown in select mode and for admin users.
   */
  public boolean isShowRootNode()
  {
    return (accessChecker.isUserMemberOfAdminGroup()) || accessChecker.isUserMemberOfGroup(ProjectForgeGroup.FINANCE_GROUP);
  }

  void persistOpenNodes()
  {
    // final Set<Serializable> openedNodes = menu.getOpenNodes();
    ((AbstractSecuredPage) getPage()).putUserPrefEntry(USER_PREFS_KEY_OPEN_TASKS, taskTreeTablePanel.getTreeTable().getOpenNodes(), true);
    if (log.isDebugEnabled() == true) {
      log.debug("Opened task nodes sucessfully persisted in the user's preferences.");
    }
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
    if (StringUtils.isNotBlank(getTaskFilter().getSearchString()) == true) {
      setResponsePage(new TaskListPage(this, getPageParameters()));
    } else {
      refresh();
    }
  }

  protected void onListViewSubmit()
  {
    setResponsePage(new TaskListPage(this, getPageParameters()));
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

  public void setEventNode(Integer id)
  {
    taskTreeTablePanel.setEventNode(id);
  }
}
