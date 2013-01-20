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

import java.io.Serializable;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.Model;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.projectforge.access.AccessChecker;
import org.projectforge.fibu.AuftragsPositionVO;
import org.projectforge.fibu.kost.KostCache;
import org.projectforge.task.TaskDO;
import org.projectforge.task.TaskFilter;
import org.projectforge.task.TaskNode;
import org.projectforge.task.TaskTree;
import org.projectforge.user.ProjectForgeGroup;
import org.projectforge.web.calendar.DateTimeFormatter;
import org.projectforge.web.core.PriorityFormatter;
import org.projectforge.web.fibu.ISelectCallerPage;
import org.projectforge.web.fibu.OrderPositionsPanel;
import org.projectforge.web.tree.DefaultTreeTablePanel;
import org.projectforge.web.tree.TreeIconsActionPanel;
import org.projectforge.web.tree.TreeTable;
import org.projectforge.web.tree.TreeTableEvent;
import org.projectforge.web.tree.TreeTableNode;
import org.projectforge.web.user.UserFormatter;
import org.projectforge.web.wicket.ListSelectActionPanel;

@Deprecated
class TaskTreeTablePanel extends DefaultTreeTablePanel<TaskTreeTableNode>
{
  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(TaskTreeTablePanel.class);

  private static final long serialVersionUID = 5350030698494851813L;

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

  @SpringBean(name = "taskTree")
  private TaskTree taskTree;

  private final TaskTreePage parentPage;

  TaskTreeTablePanel(final String id, final TaskTreePage parentPage)
  {
    super(id);
    this.parentPage = parentPage;
  }

  TaskTreeTablePanel(final String id, final ISelectCallerPage caller, final String selectProperty, final TaskTreePage parentPage)
  {
    super(id, caller, selectProperty);
    this.parentPage = parentPage;
  }

  @Override
  protected void initializeColumnHeads()
  {
    colHeadRepeater = new RepeatingView("cols");
    treeTableHead.add(colHeadRepeater);
    colHeadRepeater.add(createColHead("task"));
    colHeadRepeater.add(createColHead("task.consumption"));
    if (kostCache.isKost2EntriesExists() == true) {
      colHeadRepeater.add(createColHead("fibu.kost2"));
    }
    if (taskTree.hasOrderPositionsEntries() == true
        && accessChecker.isLoggedInUserMemberOfGroup(ProjectForgeGroup.FINANCE_GROUP, ProjectForgeGroup.CONTROLLING_GROUP,
            ProjectForgeGroup.PROJECT_ASSISTANT, ProjectForgeGroup.PROJECT_MANAGER) == true) {
      colHeadRepeater.add(createColHead("fibu.auftrag.auftraege"));
    }
    colHeadRepeater.add(createColHead("shortDescription"));
    if (accessChecker.isLoggedInUserMemberOfGroup(ProjectForgeGroup.FINANCE_GROUP) == true) {
      colHeadRepeater.add(createColHead("task.protectTimesheetsUntil.short"));
    }
    colHeadRepeater.add(createColHead("task.reference"));
    colHeadRepeater.add(createColHead("priority"));
    colHeadRepeater.add(createColHead("task.status"));
    colHeadRepeater.add(createColHead("task.assignedUser"));
  }

  @Override
  protected String getCssStyle(final TaskTreeTableNode node)
  {
    return TaskListPage.getCssStyle(node.getTask(), highlightedRowId);
  }

  public String getImageUrl(final String image)
  {
    return parentPage.getImageUrl(image);
  }

  @Override
  protected boolean onSetEvent(final AjaxRequestTarget target, final TreeTableEvent event, final TreeTableNode node)
  {
    if (target == null) {
      // User has clicked an icon for opening in new browser window.
      // Need to redirect for getting a new task tree instance.
      setResponsePage(TaskTreePage.class);
      return true;
    }
    parentPage.persistOpenNodes();
    return super.onSetEvent(target, event, node);
  }

  @Override
  protected void onSetEventNode(final Serializable hashId)
  {
    super.onSetEventNode(hashId);
    parentPage.persistOpenNodes();
  }

  @Override
  protected TreeTable<TaskTreeTableNode> buildTreeTable()
  {
    final TaskTreeTable taskTreeTable = new TaskTreeTable(taskTree);
    @SuppressWarnings("unchecked")
    final Set<Serializable> openTaskNodes = (Set<Serializable>) parentPage.getUserPrefEntry(TaskTreePage.USER_PREFS_KEY_OPEN_TASKS);
    if (openTaskNodes != null) {
      final Set<Serializable> set = new HashSet<Serializable>();
      set.addAll(openTaskNodes);
      taskTreeTable.setOpenNodes(set);
      if (log.isDebugEnabled() == true) {
        log.debug("openedNodes sucessfully get from user preferences, opened=" + taskTreeTable.getOpenNodes());
      }
    }
    return taskTreeTable;
  }

  @Override
  protected List<TaskTreeTableNode> buildTreeList()
  {
    final TaskFilter taskFilter = parentPage.form.getSearchFilter();
    taskFilter.resetMatch();
    final List<TaskTreeTableNode> treeList = new LinkedList<TaskTreeTableNode>();//.getNodeList(taskFilter);
    if (parentPage.isShowRootNode() == true && StringUtils.isBlank(parentPage.getTaskFilter().getSearchString()) == true) {
      treeList.add(new TaskTreeTableNode(null, taskTree.getRootTaskNode()));
    }
    return treeList;
  }

  @Override
  protected void addColumns(final RepeatingView colBodyRepeater, final String cssStyle, final TaskTreeTableNode node)
  {
    final TaskNode taskNode = node.getTaskNode();
    final TaskDO task = node.getTask();
    Component col = TaskListPage.getConsumptionBarPanel(this, colBodyRepeater.newChildId(), taskTree, isSelectMode(), taskNode);
    addColumn(colBodyRepeater, col, cssStyle);
    if (kostCache.isKost2EntriesExists() == true) {
      col = TaskListPage.getKostLabel(colBodyRepeater.newChildId(), taskTree, task);
      addColumn(colBodyRepeater, col, cssStyle);
    }
    if (taskTree.hasOrderPositionsEntries() == true
        && accessChecker.isLoggedInUserMemberOfGroup(ProjectForgeGroup.FINANCE_GROUP, ProjectForgeGroup.CONTROLLING_GROUP,
            ProjectForgeGroup.PROJECT_ASSISTANT, ProjectForgeGroup.PROJECT_MANAGER) == true) {
      final Set<AuftragsPositionVO> orderPositions = taskTree.getOrderPositionEntries(task.getId());
      if (CollectionUtils.isEmpty(orderPositions) == true) {
        col = new Label(colBodyRepeater.newChildId(), ""); // Empty label.
        addColumn(colBodyRepeater, col, cssStyle);
      } else {
        col = new OrderPositionsPanel(colBodyRepeater.newChildId());
        addColumn(colBodyRepeater, col, cssStyle);
        ((OrderPositionsPanel) col).init(orderPositions);
      }
    }
    col = new Label(colBodyRepeater.newChildId(), task.getShortDescription());
    addColumn(colBodyRepeater, col, cssStyle);
    if (accessChecker.isLoggedInUserMemberOfGroup(ProjectForgeGroup.FINANCE_GROUP) == true) {
      col = new Label(colBodyRepeater.newChildId(), dateTimeFormatter.getFormattedDate(task.getProtectTimesheetsUntil()));
      col.add(AttributeModifier.append("style", new Model<String>("white-space: nowrap;")));
      addColumn(colBodyRepeater, col, cssStyle);
    }
    col = new Label(colBodyRepeater.newChildId(), task.getReference());
    addColumn(colBodyRepeater, col, cssStyle);
    col = TaskListPage.getPriorityLabel(colBodyRepeater.newChildId(), priorityFormatter, task);
    addColumn(colBodyRepeater, col, cssStyle);
    col = TaskListPage.getStatusLabel(colBodyRepeater.newChildId(), taskFormatter, task);
    addColumn(colBodyRepeater, col, cssStyle);
    col = new Label(colBodyRepeater.newChildId(), userFormatter.formatUser(task.getResponsibleUserId()));
    addColumn(colBodyRepeater, col, cssStyle);
  }

  @Override
  protected TreeIconsActionPanel< ? extends TreeTableNode> createTreeIconsActionPanel(final TaskTreeTableNode node)
  {
    final TaskDO task = node.getTask();
    final Label formattedTaskLabel = new Label(ListSelectActionPanel.LABEL_ID, task.getTitle());
    formattedTaskLabel.setEscapeModelStrings(false);
    TreeIconsActionPanel< ? extends TreeTableNode> treeIconsActionPanel;
    if (isSelectMode() == false) {
      treeIconsActionPanel = new TreeIconsActionPanel<TaskTreeTableNode>("c1", new Model<TaskTreeTableNode>(node), TaskEditPage.class, task
          .getId(), formattedTaskLabel, getTreeTable());
    } else {
      treeIconsActionPanel = new TreeIconsActionPanel<TaskTreeTableNode>("c1", new Model<TaskTreeTableNode>(node), caller, selectProperty,
          task.getId(), formattedTaskLabel, getTreeTable());
    }
    treeIconsActionPanel.setUseAjaxAtDefault(parentPage.getTaskFilter().isAjaxSupport());
    return treeIconsActionPanel;
  }
}
