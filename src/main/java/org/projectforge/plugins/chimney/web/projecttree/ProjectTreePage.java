/////////////////////////////////////////////////////////////////////////////
//
// Project   ProjectForge - Chimney Plugin
//
// Copyright 2012, Micromata GmbH
//           All rights reserved.
//
/////////////////////////////////////////////////////////////////////////////

package org.projectforge.plugins.chimney.web.projecttree;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Transient;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.extensions.markup.html.repeater.data.table.AbstractColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.PropertyColumn;
import org.apache.wicket.extensions.markup.html.repeater.tree.TableTree;
import org.apache.wicket.extensions.markup.html.repeater.tree.table.TreeColumn;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.projectforge.core.BaseSearchFilter;
import org.projectforge.plugins.chimney.resources.ImageResources;
import org.projectforge.plugins.chimney.wbs.AbstractWbsNodeDO;
import org.projectforge.plugins.chimney.wbs.MilestoneDO;
import org.projectforge.plugins.chimney.wbs.PhaseDO;
import org.projectforge.plugins.chimney.wbs.ProjectDO;
import org.projectforge.plugins.chimney.wbs.SubtaskDO;
import org.projectforge.plugins.chimney.wbs.WbsNodeFilter;
import org.projectforge.plugins.chimney.wbs.WorkpackageDO;
import org.projectforge.plugins.chimney.wbs.visitors.WbsNodeChildValidationVisitor;
import org.projectforge.plugins.chimney.web.AbstractSecuredChimneyPage;
import org.projectforge.plugins.chimney.web.WicketWbsUtils;
import org.projectforge.plugins.chimney.web.components.AbstractExpandableCollapsibleTreeColumn;
import org.projectforge.plugins.chimney.web.components.ImageLinkPanel;
import org.projectforge.plugins.chimney.web.gantt.GanttPage;
import org.projectforge.plugins.chimney.web.navigation.BreadcrumbConstants;
import org.projectforge.plugins.chimney.web.navigation.NavigationConstants;
import org.projectforge.plugins.chimney.web.projectmanagement.ProjectListPage;
import org.projectforge.plugins.chimney.web.projectmanagement.powerworkpackage.PowerMilestoneEditPage;
import org.projectforge.plugins.chimney.web.projectmanagement.powerworkpackage.PowerSubtaskEditPage;
import org.projectforge.plugins.chimney.web.projectmanagement.powerworkpackage.PowerWorkpackageEditPage;
import org.projectforge.plugins.chimney.web.projectmanagement.wizard.WizardActivityEditPage;
import org.projectforge.plugins.chimney.web.projectmanagement.wizard.WizardDependencyEditPage;
import org.projectforge.plugins.chimney.web.resourceplanning.ResourceAssignmentEditPage;
import org.projectforge.plugins.chimney.web.visitors.WbsNodeEditPageVisitor;
import org.projectforge.web.wicket.AbstractSecuredPage;
import org.projectforge.web.wicket.WicketUtils;
import org.projectforge.web.wicket.flowlayout.ImagePanel;

/**
 * wicket page for visualization of a project tree (table tree component)
 * @see ProjectTableTree
 * 
 * @author Sweeps <pf@byte-storm.com>
 */
public class ProjectTreePage extends AbstractSecuredChimneyPage
{
  private static final long serialVersionUID = -6997020690105259499L;

  public static final String PAGE_ID = "projectTableTreePage";

  @SpringBean(name = "wicketWbsUtils")
  private WicketWbsUtils wbsUtils;

  protected ProjectTreeProvider treeProvider;

  private RepeatingView container;

  @Transient
  private transient Integer[] projectIds;

  /**
   * default Page constructor. visualizes a project tree for project id given by the page parameter 'projectId', if any.
   */
  public ProjectTreePage(final PageParameters parameters)
  {
    super(parameters);

    if (parameters.getNamedKeys().contains("projectId")) {
      final Integer projectIdToLoad = parameters.get("projectId").toInteger();
      projectIds = new Integer[] { projectIdToLoad};
    } else {
      // load all Projects
      final List<ProjectDO> allProjects = wbsUtils.getDaoFor(ProjectDO.prototype).getList(new BaseSearchFilter());
      if (allProjects.isEmpty()) {
        warn("There are no Projects to view.");
      }
      this.projectIds = new Integer[allProjects.size()];
      for (int i = 0; i < projectIds.length; ++i) {
        projectIds[i] = allProjects.get(i).getId();
      }
    }

  }

  /**
   * constructor which visualizes multiple project trees for each given project id
   * @param projectIds unique ids of all projects to visualize in the tree component
   */
  public ProjectTreePage(final Integer... projectIds)
  {
    super(new PageParameters());
    this.projectIds = projectIds;
  }

  @Override
  protected void onInitialize()
  {
    super.onInitialize();
    final ProjectDO[] projects = loadProjects(projectIds);
    addFeedbackPanel();
    addTabs(projectIds);
    prepareAdditionalFields();
    addProjectTreeFor(projects);
  }

  protected Integer[] getProjectIds()
  {
    return this.projectIds;
  }

  /**
   * loads all projects in the array of project ids
   * @param projectIds unique ids of all projects to visualize in the tree component
   * @return array of ProjectDO objects corresponding to the given array of ids
   */
  protected ProjectDO[] loadProjects(final Integer... projectIds)
  {
    if (projectIds == null)
      return new ProjectDO[] {};

    ProjectDO projectToLoad = null;
    String errMsg = "";
    final ProjectDO[] allProjectsArray = new ProjectDO[projectIds.length];
    for (int i = 0; i < projectIds.length; ++i) {
      final Integer id = projectIds[i];
      projectToLoad = wbsUtils.getById(id, ProjectDO.prototype);
      if (projectToLoad == null) {
        errMsg += "no project with id " + id + " found. ";
      }
      allProjectsArray[i] = projectToLoad;
    }
    if (errMsg.length() > 0)
      warn(errMsg);

    return allProjectsArray;
  }

  private void addTabs(final Integer[] projectIds)
  {
    body.add(new ProjectTreeTabsPanel("tabs", projectIds, this));
  }

  /**
   * adds the projects tree (table tree component) to the pade
   * @param projects projects to visualize in the tree component
   */
  private void addProjectTreeFor(final ProjectDO... projects)
  {
    body.add(new Label("titlehead", getTitle()));

    treeProvider = new ProjectTreeProvider(wbsUtils, getWbsNodeFilter(), projects);
    final ProjectTreeStateModel treeStateModel = new ProjectTreeStateModel(treeProvider, true);

    final TableTree<AbstractWbsNodeDO, String> tree = new ProjectTableTree("projectTree", createTableColumns(treeStateModel), treeProvider,
        Integer.MAX_VALUE, treeStateModel);
    body.add(tree);
  }

  /**
   * @return A filter that determines which node types are displayed or not displayed. By default, all phases are excluded.
   */
  protected WbsNodeFilter getWbsNodeFilter()
  {
    return new WbsNodeFilter(true).addChildType(PhaseDO.class);
  }

  protected List<IColumn<AbstractWbsNodeDO, String>> createTableColumns(final ProjectTreeStateModel treeStateModel)
  {
    final List<IColumn<AbstractWbsNodeDO, String>> columns = new ArrayList<IColumn<AbstractWbsNodeDO, String>>(8);

    columns.add(getWbsCodeColumn());
    columns.add(getTreeColumn(treeStateModel));
    columns.add(new PropertyColumn<AbstractWbsNodeDO, String>(Model.of(getString("plugins.chimney.projectlist.headdesc")),
        "getShortDescription"));
    columns.add(new AbstractColumn<AbstractWbsNodeDO, String>(Model.of(getString("plugins.chimney.projectlist.headpriority"))) {
      private static final long serialVersionUID = 1L;

      @Override
      public void populateItem(final Item<ICellPopulator<AbstractWbsNodeDO>> cellItem, final String componentId,
          final IModel<AbstractWbsNodeDO> rowModel)
      {
        String priorityText;
        if (rowModel.getObject().getPriority() != null)
          priorityText = getString(rowModel.getObject().getPriority().getI18nKey());
        else priorityText = "-";
        cellItem.add(new Label(componentId, priorityText));
      }
    });
    columns.add(new AbstractColumn<AbstractWbsNodeDO, String>(Model.of(getString("plugins.chimney.projectlist.headstatus"))) {
      private static final long serialVersionUID = 1L;

      @Override
      public void populateItem(final Item<ICellPopulator<AbstractWbsNodeDO>> cellItem, final String componentId,
          final IModel<AbstractWbsNodeDO> rowModel)
      {
        String statusText;
        if (rowModel.getObject().getStatus() != null)
          statusText = getString(rowModel.getObject().getStatus().getI18nKey());
        else statusText = "-";
        cellItem.add(new Label(componentId, statusText));
      }
    });

    columns.add(new AbstractColumn<AbstractWbsNodeDO, String>(Model.of(getString("plugins.chimney.projectlist.headresponsible"))) {
      private static final long serialVersionUID = 1L;

      @Override
      public void populateItem(final Item<ICellPopulator<AbstractWbsNodeDO>> cellItem, final String componentId,
          final IModel<AbstractWbsNodeDO> rowModel)
      {
        String userName;

        if (rowModel.getObject().getResponsibleUser() != null)
          userName = rowModel.getObject().getResponsibleUser().getFullname();
        else userName = "-";

        cellItem.add(new Label(componentId, userName));
      }
    });

    columns.add(new AbstractColumn<AbstractWbsNodeDO, String>(Model.of(getString("plugins.chimney.projectlist.headactions"))) {
      private static final long serialVersionUID = 1L;

      @Override
      public void populateItem(final Item<ICellPopulator<AbstractWbsNodeDO>> cellItem, final String componentId,
          final IModel<AbstractWbsNodeDO> rowModel)
      {
        cellItem.add(createActionLinksFor(componentId, rowModel.getObject()));
      }

      @Override
      public String getCssClass()
      {
        return "actionLinkColumn";
      }
    });

    return columns;
  };

  /**
   * Method must return a {@link TreeColumn} for navigating thriugh the tree. The TreeColumn is always the column where the tree with
   * expand/collapse items is rendered
   * @param treeStateModel The model of the tree state
   * @return A {@link TreeColumn}
   */
  protected TreeColumn<AbstractWbsNodeDO, String> getTreeColumn(final ProjectTreeStateModel treeStateModel)
  {
    return new AbstractExpandableCollapsibleTreeColumn<AbstractWbsNodeDO>(Model.of(getString("plugins.chimney.projectlist.headname"))) {
      private static final long serialVersionUID = 1L;

      @Override
      public void onCollapseAllClicked()
      {
        treeStateModel.collapseAll();
      }

      @Override
      public void onExpandAllClicked()
      {
        treeStateModel.expandAll();
      }
    };
  }

  protected IColumn<AbstractWbsNodeDO, String> getWbsCodeColumn()
  {
    return new PropertyColumn<AbstractWbsNodeDO, String>(Model.of(getString("plugins.chimney.projectlist.wbscode")), "getWbsCode");
  }

  /**
   * creates a component with several action links for a given project node (=row).
   * @param wicketid wicketid for the action link component
   * @param wbsNode node for which action links are added
   * @return the component with action links for the given node
   */
  private Component createActionLinksFor(final String wicketid, final AbstractWbsNodeDO wbsNode)
  {
    final Fragment fragment = new Fragment(wicketid, "containerFragment", body);
    final RepeatingView rv = new RepeatingView("container");

    // final Component editLink = createEditLinkFor(rv.newChildId(), wbsNode);
    // rv.add(editLink);
    final Component deleteLink = createDeleteLinkFor(rv.newChildId(), wbsNode);
    rv.add(deleteLink);
    final Component moveUpLink = createMoveUpLink(rv.newChildId(), wbsNode);
    rv.add(moveUpLink);
    final Component moveDownLink = createMoveDownLink(rv.newChildId(), wbsNode);
    rv.add(moveDownLink);

    final Fragment moreActions = new Fragment(rv.newChildId(), "moreActionsFragment", body);
    rv.add(moreActions);
    final WebMarkupContainer hiddenContainer = new WebMarkupContainer("hiddenContainer");

    final RepeatingView moreActionsRv = new RepeatingView("action");
    moreActions.add(new Image("icon", ImageResources.DOWN_ARROW));
    hiddenContainer.add(moreActionsRv);
    moreActions.add(hiddenContainer);

    if (canCreateLinkFor(wbsNode, SubtaskDO.prototype)) {
      final Component newSubTaskLink = createNewSubtaskLink(moreActionsRv.newChildId(), wbsNode);
      moreActionsRv.add(newSubTaskLink);
    }

    if (canCreateLinkFor(wbsNode, WorkpackageDO.prototype)) {
      final Component newWorkpackageLink = createNewWorkpackageLink(moreActionsRv.newChildId(), wbsNode);
      moreActionsRv.add(newWorkpackageLink);
    }

    if (canCreateLinkFor(wbsNode, MilestoneDO.prototype)) {
      final Component newMilestoneLink = createNewMilestoneLink(moreActionsRv.newChildId(), wbsNode);
      moreActionsRv.add(newMilestoneLink);
    }

    // final Component newActivityLink = createNewActivityLink(moreActionsRv.newChildId(), wbsNode);
    // moreActionsRv.add(newActivityLink);

    /*
     * final Component newDependencyLink = createNewDependencyLink(moreActionsRv.newChildId(), wbsNode);
     * moreActionsRv.add(newDependencyLink);
     * 
     * final Component ressourceAssignment = createNewResourceAssignmentLink(moreActionsRv.newChildId(), wbsNode);
     * moreActionsRv.add(ressourceAssignment);
     * 
     * final Component ganttLink = createGanttLinkFor(moreActionsRv.newChildId(), wbsNode); moreActionsRv.add(ganttLink);
     */

    // set with of the hidden container depending on the number action links in it
    final int containterWidth = (moreActionsRv.size() * 24) + 4;
    hiddenContainer.add(AttributeModifier.prepend("style", "width:" + containterWidth + "px;"));

    // show the more actions arrow only, if there actually are more actions
    moreActions.setVisible(moreActionsRv.size() > 0);

    fragment.add(rv);
    return fragment;
  }

  protected Component createNewMilestoneLink(final String id, final AbstractWbsNodeDO wbsNode)
  {
    return new ImageLinkPanel(id, ImageResources.NEWMILESTONE_SMALL_IMAGE, getString("plugins.chimney.projecttree.newmilestone"),
        getString("plugins.chimney.projecttree.newmilestone")) {
      private static final long serialVersionUID = 1L;

      @Override
      public void onClick()
      {
        setResponsePage(new PowerMilestoneEditPage(new PageParameters(), null, wbsNode.getId()));
      }
    };
  }

  protected Component createNewWorkpackageLink(final String id, final AbstractWbsNodeDO wbsNode)
  {
    return new ImageLinkPanel(id, ImageResources.NEWWORKPACKAGE_SMALL_IMAGE, getString("plugins.chimney.projecttree.newworkpackage"),
        getString("plugins.chimney.projecttree.newworkpackage")) {
      private static final long serialVersionUID = 1L;

      @Override
      public void onClick()
      {
        setResponsePage(new PowerWorkpackageEditPage(new PageParameters(), null, wbsNode.getId()));
      }
    };
  }

  protected Component createNewSubtaskLink(final String id, final AbstractWbsNodeDO wbsNode)
  {
    return new ImageLinkPanel(id, ImageResources.NEWSUBTASK_SMALL_IMAGE, getString("plugins.chimney.projecttree.newsubtask"),
        getString("plugins.chimney.projecttree.newsubtask")) {
      private static final long serialVersionUID = 1L;

      @Override
      public void onClick()
      {
        setResponsePage(new PowerSubtaskEditPage(new PageParameters(), null, wbsNode.getId()));
      }
    };
  }

  protected Component createNewActivityLink(final String id, final AbstractWbsNodeDO wbsNode)
  {
    return new ImageLinkPanel(id, ImageResources.EDITACTIVITY_SMALL_IMAGE, getString("plugins.chimney.projecttree.editactivity"),
        getString("plugins.chimney.projecttree.editactivity")) {
      private static final long serialVersionUID = 1L;

      @Override
      public void onClick()
      {
        setResponsePage(new WizardActivityEditPage(new PageParameters(), wbsNode.getId()));
      }
    };
  }

  protected Component createNewDependencyLink(final String id, final AbstractWbsNodeDO wbsNode)
  {
    return new ImageLinkPanel(id, ImageResources.NEWDEPENDENCY_SMALL_IMAGE, getString("plugins.chimney.projecttree.newdependency"),
        getString("plugins.chimney.projecttree.newdependency")) {
      private static final long serialVersionUID = 1L;

      @Override
      public void onClick()
      {
        setResponsePage(new WizardDependencyEditPage(new PageParameters(), wbsNode.getId()));
      }
    };
  }

  protected Component createNewResourceAssignmentLink(final String id, final AbstractWbsNodeDO wbsNode)
  {
    return new ImageLinkPanel(id, ImageResources.NEWRESOURCEASSIGNMENT_SMALL_IMAGE,
        getString("plugins.chimney.projecttree.newressourceassignment"), getString("plugins.chimney.projecttree.newressourceassignment")) {
      private static final long serialVersionUID = 1L;

      @Override
      public void onClick()
      {
        setResponsePage(new ResourceAssignmentEditPage(new PageParameters(), wbsNode));
      }
    };
  }

  protected boolean canCreateLinkFor(final AbstractWbsNodeDO wbsNode, final AbstractWbsNodeDO childNode)
  {
    final WbsNodeChildValidationVisitor visitor = new WbsNodeChildValidationVisitor();
    wbsNode.accept(visitor);
    childNode.accept(visitor);
    return visitor.isValidChild();
  }

  protected Component createEditLinkFor(final String wicketid, final AbstractWbsNodeDO wbsNode)
  {
    final ImageLinkPanel editLink = new ImageLinkPanel(wicketid, ImageResources.EDIT_SMALL_IMAGE,
        getString("plugins.chimney.projecttree.edit"), getString("plugins.chimney.projecttree.edit")) {
      private static final long serialVersionUID = 1L;

      AbstractSecuredPage localEditPage = null;

      @Override
      public void onClick()
      {
        if (localEditPage == null) {
          localEditPage = WbsNodeEditPageVisitor.createEditPageFor(wbsNode);
        }
        setResponsePage(localEditPage);
      }
    };
    return editLink;
  }

  protected Component createGanttLinkFor(final String wicketid, final AbstractWbsNodeDO wbsNode)
  {
    // add gantt link and icon
    return new ImageLinkPanel(wicketid, ImageResources.GANTT_SMALL_IMAGE, getString("plugins.chimney.projecttree.showgantt"),
        getString("plugins.chimney.projecttree.showgantt")) {
      private static final long serialVersionUID = 1L;

      @Override
      public void onClick()
      {
        setResponsePage(new GanttPage(wbsNode));

      }
    };
  }

  protected Component createDeleteLinkFor(final String wicketid, final AbstractWbsNodeDO wbsNode)
  {
    // add delete link and icon
    final ImageLinkPanel deleteLink = new ImageLinkPanel(wicketid, ImageResources.DELETE_SMALL_IMAGE,
        getString("plugins.chimney.projecttree.delete"), getString("plugins.chimney.projecttree.delete")) {
      private static final long serialVersionUID = 1L;

      @Override
      public void onClick()
      {
        if (wbsNode.getParent() == null) {
          setResponsePage(ProjectListPage.class);
        }
        wbsUtils.markAsDeleted(wbsNode);
      }
    };

    deleteLink.add(WicketUtils.javaScriptConfirmDialogOnClick(getLocalizedMessage("plugins.chimney.common.confirmdeleteitem",
        wbsNode.getTitle())));
    deleteLink.add(AttributeModifier.append("class", "delete_link"));

    return deleteLink;
  }

  protected Component createMoveUpLink(final String id, final AbstractWbsNodeDO node)
  {
    final AbstractWbsNodeDO otherNode = treeProvider.getPredecessor(node);
    if (otherNode == null)
      return getPlaceHolderImagePanel(id, 14, 24);

    return new ImageLinkPanel(id, ImageResources.MOVEUP, getString("plugins.chimney.projecttree.moveup"),
        getString("plugins.chimney.projecttree.moveup")) {
      private static final long serialVersionUID = 1L;

      @Override
      public void onClick()
      {
        swapNodesAndSave(node, otherNode);
      }
    };
  }

  protected Component createMoveDownLink(final String id, final AbstractWbsNodeDO node)
  {
    final AbstractWbsNodeDO otherNode = treeProvider.getSuccessor(node);
    if (otherNode == null)
      return getPlaceHolderImagePanel(id, 14, 24);

    return new ImageLinkPanel(id, ImageResources.MOVEDOWN, getString("plugins.chimney.projecttree.movedown"),
        getString("plugins.chimney.projecttree.movedown")) {
      private static final long serialVersionUID = 1L;

      @Override
      public void onClick()
      {
        swapNodesAndSave(node, otherNode);
      }
    };
  }

  protected Component getPlaceHolderImagePanel(final String id, final int width, final int height)
  {
    final Image placeHolderImage = new Image(ImagePanel.IMAGE_ID, ImageResources.BLANK);
    placeHolderImage.add(AttributeModifier.replace("width", Model.of(width)));
    placeHolderImage.add(AttributeModifier.replace("height", Model.of(height)));
    return new ImagePanel(id, placeHolderImage);
  }

  private void swapNodesAndSave(final AbstractWbsNodeDO node, final AbstractWbsNodeDO otherNode)
  {
    final AbstractWbsNodeDO parent = node.getParent();
    if (parent == null || otherNode == null)
      return;

    parent.swapChildren(node, otherNode);
    wbsUtils.getDaoFor(parent).update(parent);
  }

  private void prepareAdditionalFields()
  {
    container = new RepeatingView("additionalContainer");
    body.add(container);
    addAdditionalFields(container);
  }

  protected void addAdditionalFields(final RepeatingView container)
  {

  }

  @Override
  protected String getTitle()
  {
    return getString("plugins.chimney.projecttree.title");
  }

  @Override
  protected String getNavigationBarName()
  {
    return NavigationConstants.MAIN;
  }

  @Override
  protected void insertBreadcrumbItems(final List<String> items)
  {
    items.add(BreadcrumbConstants.PROJECT_PLANNING);
    items.add(BreadcrumbConstants.PROJECT_TREE);
  }

}
