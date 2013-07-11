/////////////////////////////////////////////////////////////////////////////
//
// Project   ProjectForge - Chimney Plugin
//
// Copyright 2012, Micromata GmbH
//           All rights reserved.
//
/////////////////////////////////////////////////////////////////////////////

package org.projectforge.plugins.chimney.web.projecttree;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.extensions.markup.html.repeater.data.table.AbstractColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.PropertyColumn;
import org.apache.wicket.extensions.markup.html.repeater.tree.ITreeProvider;
import org.apache.wicket.extensions.markup.html.repeater.tree.TableTree;
import org.apache.wicket.extensions.markup.html.repeater.tree.table.TreeColumn;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.projectforge.plugins.chimney.resources.ImageResources;
import org.projectforge.plugins.chimney.wbs.AbstractWbsNodeDO;
import org.projectforge.plugins.chimney.wbs.MilestoneDO;
import org.projectforge.plugins.chimney.wbs.PhaseDO;
import org.projectforge.plugins.chimney.wbs.ProjectDO;
import org.projectforge.plugins.chimney.wbs.SubtaskDO;
import org.projectforge.plugins.chimney.wbs.WbsNodeFilter;
import org.projectforge.plugins.chimney.wbs.WorkpackageDO;
import org.projectforge.plugins.chimney.wbs.visitors.WbsNodeChildValidationVisitor;
import org.projectforge.plugins.chimney.web.WicketWbsUtils;
import org.projectforge.plugins.chimney.web.components.ChimneyFeedbackPanel;
import org.projectforge.plugins.chimney.web.components.ImageLinkPanel;
import org.projectforge.plugins.chimney.web.components.TextLinkPanel;
import org.projectforge.plugins.chimney.web.gantt.GanttPage;
import org.projectforge.plugins.chimney.web.projectmanagement.ProjectListPage;
import org.projectforge.plugins.chimney.web.projectmanagement.powerworkpackage.PowerMilestoneEditPage;
import org.projectforge.plugins.chimney.web.projectmanagement.powerworkpackage.PowerSubtaskEditPage;
import org.projectforge.plugins.chimney.web.projectmanagement.wizard.WizardActivityEditPage;
import org.projectforge.plugins.chimney.web.projectmanagement.wizard.WizardDependencyEditPage;
import org.projectforge.plugins.chimney.web.projectmanagement.wizard.WizardWorkpackageEditPage;
import org.projectforge.plugins.chimney.web.resourceplanning.ResourceAssignmentEditPage;
import org.projectforge.plugins.chimney.web.visitors.WbsNodeEditPageVisitor;
import org.projectforge.web.wicket.AbstractSecuredPage;
import org.projectforge.web.wicket.WicketUtils;

public class ProjectTreePanel extends Panel
{

  private static final long serialVersionUID = 8679078469950181551L;

  @SpringBean(name="wicketWbsUtils")
  private WicketWbsUtils wbsUtils;

  private ITreeProvider<AbstractWbsNodeDO> treeProvider;


  public ProjectTreePanel(final String id)
  {
    super(id);
  }

  public ProjectTreePanel(final String id, final Integer projectId) {
    this(id);
    addFeedbackPanel();
    add(new Label("titlehead", getTitle()));
    addProjectTreeFor(projectId);
  }

  /**
   * @return A filter that determines which node types are displayed or not displayed. By default, all phases are excluded.
   */
  protected WbsNodeFilter getWbsNodeFilter()
  {
    return new WbsNodeFilter(true).addChildType(PhaseDO.class);
  }

  protected String getTitle()
  {
    return getString("plugins.chimney.projecttree.title");
  }

  private void addProjectTreeFor(final Integer projectId)
  {
    ProjectDO projectToLoad = null;
    projectToLoad = wbsUtils.getById(projectId, ProjectDO.prototype);
    if (projectToLoad==null) {
      warn("no project with id "+projectId+" found. ");
    }

    addProjectTreeFor(projectToLoad);
  }

  private void addProjectTreeFor(final ProjectDO project)
  {
    final ProjectDO[] projects = {project};

    treeProvider = new ProjectTreeProvider(wbsUtils, getWbsNodeFilter(), projects);
    final ProjectTreeStateModel treeStateModel = new ProjectTreeStateModel(treeProvider, true);

    final TableTree<AbstractWbsNodeDO, String> tree =
        new ProjectTableTree("projectTree", createTableColumns(), treeProvider, Integer.MAX_VALUE, treeStateModel);
    add(tree);

    add(new TextLinkPanel("expandAll", getString("plugins.chimney.projecttree.expandall")) {
      private static final long serialVersionUID = -7426580004907380551L;
      @Override
      public void onClick()
      {
        treeStateModel.expandAll();
      }
    });

    add(new TextLinkPanel("collapseAll", getString("plugins.chimney.projecttree.collapseall")) {
      private static final long serialVersionUID = 9218457480237583861L;
      @Override
      public void onClick()
      {
        treeStateModel.collapseAll();
      }
    });
  }

  private List<IColumn<AbstractWbsNodeDO, String>> createTableColumns()
  {
    final List<IColumn<AbstractWbsNodeDO, String>> columns = new ArrayList<IColumn<AbstractWbsNodeDO, String>>(8);

    columns.add(
        new PropertyColumn<AbstractWbsNodeDO, String>(
            Model.of(getString("plugins.chimney.projectlist.wbscode")), "getWbsCode")
        );
    columns.add(
        // important: the TreeColumn is always the column where the tree with expand/collapse items is rendered
        new TreeColumn<AbstractWbsNodeDO, String>(
            Model.of(getString("plugins.chimney.projectlist.headname")))
        );
    columns.add(
        new PropertyColumn<AbstractWbsNodeDO, String>(
            Model.of(getString("plugins.chimney.projectlist.headdesc")), "getShortDescription")
        );
    columns.add(
        new AbstractColumn<AbstractWbsNodeDO, String>(Model.of(getString("plugins.chimney.projectlist.headpriority"))) {
          private static final long serialVersionUID = 1L;
          @Override
          public void populateItem(final Item<ICellPopulator<AbstractWbsNodeDO>> cellItem, final String componentId, final IModel<AbstractWbsNodeDO> rowModel)
          {
            String priorityText;
            if (rowModel.getObject().getPriority() != null)
              priorityText = getString(rowModel.getObject().getPriority().getI18nKey());
            else
              priorityText = "-";
            cellItem.add(new Label(componentId, priorityText));
          }
        }

        );
    columns.add(
        new AbstractColumn<AbstractWbsNodeDO, String>(Model.of(getString("plugins.chimney.projectlist.headstatus"))) {
          private static final long serialVersionUID = 1L;
          @Override
          public void populateItem(final Item<ICellPopulator<AbstractWbsNodeDO>> cellItem, final String componentId, final IModel<AbstractWbsNodeDO> rowModel)
          {
            String statusText;
            if (rowModel.getObject().getPriority() != null)
              statusText = getString(rowModel.getObject().getStatus().getI18nKey());
            else
              statusText = "-";
            cellItem.add(new Label(componentId, statusText));
          }
        }
        );

    columns.add(
        new AbstractColumn<AbstractWbsNodeDO, String>(Model.of(getString("plugins.chimney.projectlist.headactions"))) {
          private static final long serialVersionUID = 1L;
          @Override
          public void populateItem(final Item<ICellPopulator<AbstractWbsNodeDO>> cellItem, final String componentId, final IModel<AbstractWbsNodeDO> rowModel)
          {
            cellItem.add(createActionLinksFor(componentId, rowModel.getObject()));
          }
        }
        );

    return columns;
  }

  private Component createActionLinksFor(final String wicketid, final AbstractWbsNodeDO wbsNode) {
    final Fragment fragment = new Fragment(wicketid, "containerFragment", this);
    final RepeatingView rv = new RepeatingView("container");

    final Component editLink = createEditLinkFor(rv.newChildId(), wbsNode);
    rv.add(editLink);
    final Fragment moreActions = new Fragment(rv.newChildId(), "moreActionsFragment", this);
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

    final Component newActivityLink = createNewActivityLink(moreActionsRv.newChildId(), wbsNode);
    moreActionsRv.add(newActivityLink);

    final Component newDependencyLink = createNewDependencyLink(moreActionsRv.newChildId(), wbsNode);
    moreActionsRv.add(newDependencyLink);

    final Component ressourceAssignment = createNewResourceAssignmentLink(moreActionsRv.newChildId(), wbsNode);
    moreActionsRv.add(ressourceAssignment);

    final Component ganttLink = createGanttLinkFor(moreActionsRv.newChildId(), wbsNode);
    moreActionsRv.add(ganttLink);

    final Component deleteLink = createDeleteLinkFor(moreActionsRv.newChildId(), wbsNode);
    moreActionsRv.add(deleteLink);

    // set with of the hidden container depending on the number action links in it
    final int containterWidth = (moreActionsRv.size()*24)+4;
    hiddenContainer.add(AttributeModifier.prepend("style", "width:"+containterWidth+"px;"));

    fragment.add(rv);
    return fragment;
  }

  protected Component createNewMilestoneLink(final String id, final AbstractWbsNodeDO wbsNode)
  {
    return new ImageLinkPanel(id, ImageResources.NEWMILESTONE_SMALL_IMAGE, getString("plugins.chimney.projecttree.newmilestone"), getString("plugins.chimney.projecttree.newmilestone")) {
      private static final long serialVersionUID = 1L;
      @Override
      public void onClick() {
        setResponsePage(new PowerMilestoneEditPage(new PageParameters(), null, wbsNode.getId()));
      }
    };
  }

  protected Component createNewWorkpackageLink(final String id, final AbstractWbsNodeDO wbsNode)
  {
    return new ImageLinkPanel(id, ImageResources.NEWWORKPACKAGE_SMALL_IMAGE, getString("plugins.chimney.projecttree.newworkpackage"), getString("plugins.chimney.projecttree.newworkpackage")) {
      private static final long serialVersionUID = 1L;
      @Override
      public void onClick() {
        setResponsePage(new WizardWorkpackageEditPage(new PageParameters(), wbsNode));
      }
    };
  }

  protected Component createNewSubtaskLink(final String id, final AbstractWbsNodeDO wbsNode)
  {
    return new ImageLinkPanel(id, ImageResources.NEWSUBTASK_SMALL_IMAGE, getString("plugins.chimney.projecttree.newsubtask"), getString("plugins.chimney.projecttree.newsubtask")) {
      private static final long serialVersionUID = 1L;
      @Override
      public void onClick() {
        setResponsePage(new PowerSubtaskEditPage(new PageParameters(), null, wbsNode.getId()));
      }
    };
  }

  protected Component createNewActivityLink(final String id, final AbstractWbsNodeDO wbsNode)
  {
    return new ImageLinkPanel(id, ImageResources.EDITACTIVITY_SMALL_IMAGE, getString("plugins.chimney.projecttree.editactivity"), getString("plugins.chimney.projecttree.editactivity")) {
      private static final long serialVersionUID = 1L;
      @Override
      public void onClick() {
        setResponsePage(new WizardActivityEditPage(new PageParameters(), wbsNode.getId()));
      }
    };
  }

  protected Component createNewDependencyLink(final String id, final AbstractWbsNodeDO wbsNode)
  {
    return new ImageLinkPanel(id, ImageResources.NEWDEPENDENCY_SMALL_IMAGE, getString("plugins.chimney.projecttree.newdependency"), getString("plugins.chimney.projecttree.newdependency")) {
      private static final long serialVersionUID = 1L;
      @Override
      public void onClick() {
        setResponsePage(new WizardDependencyEditPage(new PageParameters(), wbsNode.getId()));
      }
    };
  }

  protected Component createNewResourceAssignmentLink(final String id, final AbstractWbsNodeDO wbsNode)
  {
    return new ImageLinkPanel(id, ImageResources.NEWRESOURCEASSIGNMENT_SMALL_IMAGE, getString("plugins.chimney.projecttree.newressourceassignment"), getString("plugins.chimney.projecttree.newressourceassignment")) {
      private static final long serialVersionUID = 1L;
      @Override
      public void onClick() {
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

  protected Component createEditLinkFor(final String wicketid, final AbstractWbsNodeDO wbsNode) {
    final ImageLinkPanel editLink =  new ImageLinkPanel(wicketid, ImageResources.EDIT_SMALL_IMAGE, getString("plugins.chimney.projecttree.edit"), getString("plugins.chimney.projecttree.edit")) {
      private static final long serialVersionUID = 1L;
      AbstractSecuredPage localEditPage = null;
      @Override
      public void onClick() {
        if (localEditPage==null) {
          localEditPage = (new WbsNodeEditPageVisitor()).createEditPageFor(wbsNode);
        }
        setResponsePage(localEditPage);
      }
    };
    return editLink;
  }

  protected Component createGanttLinkFor(final String wicketid, final AbstractWbsNodeDO wbsNode) {
    // add gantt link and icon
    return new ImageLinkPanel(wicketid, ImageResources.GANTT_SMALL_IMAGE, getString("plugins.chimney.projecttree.showgantt"), getString("plugins.chimney.projecttree.showgantt")) {
      private static final long serialVersionUID = 1L;
      @Override
      public void onClick()
      {
        setResponsePage(
            new GanttPage(wbsNode)
            );

      }
    };
  }

  protected Component createDeleteLinkFor(final String wicketid, final AbstractWbsNodeDO wbsNode) {
    // add delete link and icon
    final ImageLinkPanel deleteLink = new ImageLinkPanel(wicketid, ImageResources.DELETE_SMALL_IMAGE, getString("plugins.chimney.projecttree.delete"), getString("plugins.chimney.projecttree.delete")) {
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

    deleteLink.add(WicketUtils.javaScriptConfirmDialogOnClick(
        getLocalizedMessage("plugins.chimney.common.confirmdeleteitem", wbsNode.getTitle())));
    deleteLink.add(AttributeModifier.append("class", "delete_link"));

    return deleteLink;
  }

  public String getLocalizedMessage(final String key, final Object... params)
  {
    if (params == null) {
      return getString(key);
    }
    return MessageFormat.format(getString(key), params);
  }

  private void addFeedbackPanel()
  {
    final FeedbackPanel feedbackPanel = new ChimneyFeedbackPanel("feedback");
    feedbackPanel.setOutputMarkupId(true);
    add(feedbackPanel);
  }




}
