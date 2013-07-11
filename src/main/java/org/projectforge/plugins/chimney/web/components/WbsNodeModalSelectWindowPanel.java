/////////////////////////////////////////////////////////////////////////////
//
// Project   ProjectForge - Chimney Plugin
//
// Copyright 2012, Micromata GmbH
//           All rights reserved.
//
/////////////////////////////////////////////////////////////////////////////

package org.projectforge.plugins.chimney.web.components;

import java.util.List;

import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.extensions.markup.html.tree.BaseTree;
import org.apache.wicket.extensions.markup.html.tree.LinkIconPanel;
import org.apache.wicket.extensions.markup.html.tree.LinkTree;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.resource.ResourceReference;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.projectforge.core.BaseSearchFilter;
import org.projectforge.plugins.chimney.wbs.AbstractWbsNodeDO;
import org.projectforge.plugins.chimney.wbs.PhaseDO;
import org.projectforge.plugins.chimney.wbs.ProjectDO;
import org.projectforge.plugins.chimney.wbs.ProjectDao;
import org.projectforge.plugins.chimney.wbs.WbsNodeDao;
import org.projectforge.plugins.chimney.wbs.WbsNodeFilter;
import org.projectforge.plugins.chimney.wbs.WbsNodeUtils;
import org.projectforge.plugins.chimney.wbs.visitors.InvalidChildErrorType;
import org.projectforge.plugins.chimney.wbs.visitors.WbsIconVisitor;
import org.projectforge.plugins.chimney.wbs.visitors.WbsNodeChildValidationVisitor;
import org.projectforge.plugins.chimney.web.projectmanagement.WbsTreeNode;

/**
 * A panel displayed in a ModalWindow for selecting wbs nodes.
 * The panel displays a project selector drop down menu and a tree of a selected project.
 * The project selector can be disabled and the displayed project can be set externally
 * before opening the ModalWindow.
 * 
 * Optionally, a validity check can be performed that checks whether a node can be parent of
 * a target node. Nodes that are not valid selections are displayed crossed out and show a
 * hint with the reason on mouse-over.
 * 
 * When a valid node is clicked, the window is closed.
 * @author Sweeps <pf@byte-storm.com>
 */
public class WbsNodeModalSelectWindowPanel extends Panel
{
  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(WbsNodeModalSelectWindowPanel.class);

  private static final long serialVersionUID = 4337456198599683560L;
  private final ModalWindow window;
  private final IModel<AbstractWbsNodeDO> wbsModel;
  private final IModel< ? extends AbstractWbsNodeDO> targetModel;
  private LinkTree projectTree;
  private Label projectTreeLabel;

  @SpringBean(name="wbsNodeDao")
  private WbsNodeDao wbsNodeDao;
  @SpringBean(name="projectDao")
  private ProjectDao projectDao;

  private DropDownChoice<ProjectDO> dropDownChoice;

  private WebMarkupContainer projectSelector;

  /**
   * Constructor for wbs select window with or without wbs-child validity check
   * @param id wicket id
   * @param wbsModel model of the currently selected wbs node. May be null.
   * @param target model of the target node for which this select window selects the wbs node for. If null, no parent-child validity check will be performed
   * @param window A ModalWindow
   */
  public WbsNodeModalSelectWindowPanel(final String id, final IModel<AbstractWbsNodeDO> wbsModel, final IModel< ? extends AbstractWbsNodeDO> target, final ModalWindow window) {
    super(id);
    this.wbsModel = wbsModel;
    this.targetModel = target;
    this.window = window;

    addProjectSelectDropDown();
    tryToAddProjectTreeFor(newTreeModelOf(getDefaultProjectRoot()));
  }

  /**
   * @return true, if the project selector drop down is visible, false otherwise
   */
  public boolean isProjectSelectorVisible()
  {
    return projectSelector.isVisible();
  }

  /**
   * Set whether the project selector drop down is visible or not. Must be set before opening the modal window.
   * @param newVisibility true to display the project selector drop down, false otherwise
   */
  public void setProjectSelectorVisible(final boolean newVisibility)
  {
    projectSelector.setVisible(newVisibility);
  }

  /**
   * Sets the project that is displayed as tree in wicket by any node in the project's tree.
   * @param node A wbs node
   * @return true if the tree component has changed, which applies if node is not null and traversing node's parent links eventually leads to a project node.
   */
  public boolean setDisplayedProjectByWbsNode(final AbstractWbsNodeDO node) {
    if (node == null)
      return false;

    // find the associated project node and construct a tree model of it
    final ProjectDO project = getProjectRootFor(node);
    final TreeModel treeModel = newTreeModelOf(project);
    if (treeModel == null)
      return false;

    // create a new tree with the new project
    createProjectTreeFor(treeModel);
    replace(projectTree);
    projectTreeLabel = null;

    // update drop down selection with new project
    dropDownChoice.setModelObject(project);

    return true;
  }

  private void addProjectSelectDropDown()
  {
    // add container for controlling visibility
    projectSelector = new WebMarkupContainer("project_selector");
    add(projectSelector);

    // get a list of all projects
    final List<ProjectDO> projects = projectDao.getList(new BaseSearchFilter());

    // create project selector drop down
    final WbsNodeChoiceRenderer<ProjectDO> choiceRenderer = new WbsNodeChoiceRenderer<ProjectDO>();
    dropDownChoice = new DropDownChoice<ProjectDO>("project_dropdown", new Model<ProjectDO>(getDefaultProjectRoot()), projects, choiceRenderer);
    dropDownChoice.add(new AjaxFormComponentUpdatingBehavior("onchange") {
      private static final long serialVersionUID = 1L;

      @Override
      protected void onUpdate(final AjaxRequestTarget target) {
        if (dropDownChoice.getModelObject() == null)
          return; // illegal drop down option selected (e.g. the "Choose One" option)

        final Integer selectedProjectId = dropDownChoice.getModelObject().getId();
        if (selectedProjectId == null)
          return;
        // get a fresh instance of the root object
        final ProjectDO selectedProject = projectDao.getById(selectedProjectId);

        if (projectTreeHasBeenRenderedBefore()) {
          // update the displayed project with the now selected project
          projectTree.setModelObject(newTreeModelOf(selectedProject));
          target.add(projectTree);
        } else {
          // add the not yet rendered project tree to the page, replacing the info label
          createProjectTreeFor(newTreeModelOf(selectedProject));
          replace(projectTree);
          projectTreeLabel = null;
          target.add(projectTree);
        }
      }

    });
    projectSelector.add(dropDownChoice);
  }

  private void tryToAddProjectTreeFor(final TreeModel model)
  {
    if (model == null) {
      // no TreeModel, render project tree replacement
      createProjectTreeReplacement();
      projectTree = null;
      add(projectTreeLabel);
    } else {
      // got a TreeModel, render project tree
      createProjectTreeFor(model);
      projectTreeLabel = null;
      add(projectTree);
    }
  }

  private void createProjectTreeFor(final TreeModel model)
  {
    projectTree = new CustomizedLinkTree("project_tree", model);
    projectTree.getTreeState().expandAll();
    projectTree.setOutputMarkupId(true);
  }

  private void createProjectTreeReplacement()
  {
    projectTreeLabel = new Label("project_tree", getString("plugins.chimney.wbsnodeselect.norootfound"));
    projectTreeLabel.setOutputMarkupId(true);
  }

  private TreeModel newTreeModelOf(final ProjectDO root)
  {
    if (root == null)
      return null;

    final WbsTreeNode rootNode = new WbsTreeNode(root, wbsNodeDao, new WbsNodeFilter(true).addChildType(PhaseDO.class));
    final TreeModel model = new DefaultTreeModel(rootNode);
    return model;
  }

  private ProjectDO getDefaultProjectRoot() {
    if (wbsModel == null)
      return null;

    final AbstractWbsNodeDO node = wbsModel.getObject();
    return getProjectRootFor(node);
  }

  private ProjectDO getProjectRootFor(final AbstractWbsNodeDO node)
  {
    if (node == null)
      return null;
    return WbsNodeUtils.getProject(node);
  }

  private boolean projectTreeHasBeenRenderedBefore()
  {
    return projectTree != null;
  }

  class CustomizedLinkTree extends LinkTree {
    private static final long serialVersionUID = 3505994282000487677L;

    public CustomizedLinkTree(final String id, final TreeModel model)
    {
      super(id, model);
    }

    @Override
    protected void onNodeLinkClicked(final Object node, final BaseTree tree, final AjaxRequestTarget target)
    {
      // set the selected node as new wbs model object
      final WbsTreeNode treeNode = (WbsTreeNode) node;
      wbsModel.setObject(treeNode.getWbsNode());
      window.close(target);
    }

    @Override
    protected Component newNodeComponent(final String id, final IModel<Object> model)
    {
      return new LinkIconPanel(id, model, CustomizedLinkTree.this)
      {
        private static final long serialVersionUID = 1L;

        @Override
        protected void onNodeLinkClicked(final Object node, final BaseTree tree, final AjaxRequestTarget target)
        {
          super.onNodeLinkClicked(node, tree, target);
          try {
            checkValidParent((WbsTreeNode) node);
            CustomizedLinkTree.this.onNodeLinkClicked(node, tree, target);
          } catch (final InvalidParentException ex) {
            log.info("User clicked on an invalid wbs node: " + ex);
          }
        }

        @Override
        protected Component newContentComponent(final String componentId, final BaseTree tree,
            final IModel<?> model)
        {
          final Label label = new Label(componentId, getNodeTextModel(model));
          final WbsTreeNode treeNode = (WbsTreeNode)model.getObject();
          try {
            checkValidParent(treeNode);
          } catch (final InvalidParentException ex) {
            label.add(AttributeModifier.append("style", "text-decoration: line-through;"));
            label.add(AttributeModifier.replace("title", ex.getLocalizedMessage()));
          }
          return label;
        }

        private void checkValidParent(final WbsTreeNode node) throws InvalidParentException
        {
          if (targetModel == null) return; // return immediately if there is no target model that needs be changed
          final AbstractWbsNodeDO parentNode = node.getWbsNode();
          final AbstractWbsNodeDO targetNode = targetModel.getObject();
          // let visitor check if node is of valid parent type
          final WbsNodeChildValidationVisitor visitor = new WbsNodeChildValidationVisitor();
          parentNode.accept(visitor);
          targetNode.accept(visitor);
          if (!visitor.isValidChild()) {
            throw new InvalidParentException(visitor.getErrorReason());
          }
        }

        @Override
        protected Component newImageComponent(final String componentId, final BaseTree tree, final IModel<Object> model)
        {
          final Component comp = super.newImageComponent(componentId, tree, model);
          comp.add(AttributeModifier.replace("width", 16));
          comp.add(AttributeModifier.replace("height", 16));
          return comp;
        }

        @Override
        protected ResourceReference getImageResourceReference(final BaseTree tree, final Object node)
        {
          if (node != null) {
            final WbsTreeNode treeNode = (WbsTreeNode) node;
            final AbstractWbsNodeDO wbsNode = treeNode.getWbsNode();
            final WbsIconVisitor visitor = new WbsIconVisitor(false);
            wbsNode.accept(visitor);
            final ResourceReference iconRessource = visitor.getSelectedImageResource();
            if (iconRessource != null)
              return iconRessource;
          }
          return super.getImageResourceReference(tree, node);
        }
      };
    }
  }

  class InvalidParentException extends Exception {
    private static final long serialVersionUID = 8130364693741307321L;
    private final InvalidChildErrorType error;

    public InvalidParentException(final InvalidChildErrorType error) {
      this.error = error;
    }

    @Override
    public String getLocalizedMessage()
    {
      return getString(error.getI18nKey());
    }
  }
}
