/////////////////////////////////////////////////////////////////////////////
//
// Project   ProjectForge - Chimney Plugin
//
// Copyright 2012, Micromata GmbH
//           All rights reserved.
//
/////////////////////////////////////////////////////////////////////////////

package org.projectforge.plugins.chimney.web.projectmanagement.wizard;

import java.util.List;

import org.apache.wicket.Page;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.projectforge.plugins.chimney.activities.CyclicDependencyRelationException;
import org.projectforge.plugins.chimney.activities.DependencyRelationDO;
import org.projectforge.plugins.chimney.activities.DependencyRelationDao;
import org.projectforge.plugins.chimney.activities.DependencyRelationType;
import org.projectforge.plugins.chimney.activities.SelfDependencyRelationException;
import org.projectforge.plugins.chimney.activities.WbsActivityDO;
import org.projectforge.plugins.chimney.activities.WbsActivityDao;
import org.projectforge.plugins.chimney.resources.ImageResources;
import org.projectforge.plugins.chimney.wbs.AbstractWbsNodeDO;
import org.projectforge.plugins.chimney.wbs.ProjectDO;
import org.projectforge.plugins.chimney.web.WicketWbsUtils;
import org.projectforge.plugins.chimney.web.components.ChimneyJodaPeriodField;
import org.projectforge.plugins.chimney.web.components.WbsNodeModalSelectWindowPanel;
import org.projectforge.plugins.chimney.web.components.WbsNodeSelectPanel;
import org.projectforge.plugins.chimney.web.navigation.BreadcrumbConstants;
import org.projectforge.plugins.chimney.web.navigation.NavigationConstants;
import org.projectforge.plugins.chimney.web.navigation.PaginationLinkDescription;
import org.projectforge.plugins.chimney.web.projecttree.DependencyTreePage;
import org.projectforge.plugins.chimney.web.utils.WicketUtil;
import org.projectforge.web.wicket.components.SingleButtonPanel;

/**
 * Edit page for creating new dependencies. Dependencies between wbs nodes are actually stored in {@link WbsActivityDO} objects due to SoC
 * principle.
 * @author Sweeps <pf@byte-storm.com>
 */
public class WizardDependencyEditPage extends AbstractChimneyWizardPage
{
  private static final long serialVersionUID = -1541878384596166170L;

  public static final String PAGE_ID = "dependencyEdit";

  Form<DependencyRelationDO> form;

  @SpringBean(name = "dependencyRelationDao")
  private DependencyRelationDao dependencyDao;

  @SpringBean(name = "wbsActivityDao")
  private WbsActivityDao activityDao;

  @SpringBean(name = "wicketWbsUtils")
  private WicketWbsUtils wbsUtils;

  private final IModel<DependencyRelationDO> dependencyModel;

  private final IModel<AbstractWbsNodeDO> predecessorModel;

  private final IModel<AbstractWbsNodeDO> successorModel;

  private WbsNodeSelectPanel predSelect;

  private WbsNodeSelectPanel succSelect;

  /**
   * Constructor for new dependencies.
   * @param parameters
   */
  public WizardDependencyEditPage(final PageParameters parameters)
  {
    super(parameters);
    dependencyModel = new Model<DependencyRelationDO>(new DependencyRelationDO());
    predecessorModel = new Model<AbstractWbsNodeDO>();
    successorModel = new Model<AbstractWbsNodeDO>();

    init();
  }

  /**
   * Constructor for new dependencies with a preselected successor.
   * @param parameters
   * @param successorId Id of a wbs node
   */
  public WizardDependencyEditPage(final PageParameters parameters, final int successorId)
  {
    super(parameters);
    dependencyModel = new Model<DependencyRelationDO>(new DependencyRelationDO());
    successorModel = wbsUtils.getModelFor(wbsUtils.getById(successorId));
    predecessorModel = new Model<AbstractWbsNodeDO>();

    init();
  }

  private void init()
  {
    addFeedbackPanel();
    addForm();
  }


  private void addForm()
  {
    form = new Form<DependencyRelationDO>("dependencyForm", new CompoundPropertyModel<DependencyRelationDO>(dependencyModel)) {
      private static final long serialVersionUID = -2800048128949818693L;

      @Override
      protected void onSubmit()
      {
        submitForm();
      }
    };

    body.add(new Label("heading", getString("plugins.chimney.createdependency.heading")));

    final WbsNodeSelectPanel predSelect = getPredecessorSelectPanel();
    final WbsNodeSelectPanel succSelect = getSuccessorSelectPanel();

    // disable the project selector of the predecessor select window and set its displayed
    // project tree if the successor is predefined.
    if (successorModel.getObject() != null) {
      final WbsNodeModalSelectWindowPanel predModalWindow = predSelect.getModalWindowPanel();
      predModalWindow.setProjectSelectorVisible(false);
      predModalWindow.setDisplayedProjectByWbsNode(successorModel.getObject());
    }

    form.add(predSelect);
    form.add(succSelect);
    form.add(new ChimneyJodaPeriodField("offset"));
    final DropDownChoice<DependencyRelationType> typeChoice = WicketUtil.getNewDropDownChoice(this, "type", "type", dependencyModel,
        DependencyRelationType.values());
    typeChoice.setNullValid(false);
    form.add(typeChoice);

    form.add(new SingleButtonPanel("submit", new Button("button"), getString("plugins.chimney.editpage.save"),
        SingleButtonPanel.DEFAULT_SUBMIT));

    body.add(form);
  }

  private WbsNodeSelectPanel getSuccessorSelectPanel()
  {
    if (succSelect == null) {
      succSelect = new WbsNodeSelectPanel("successor", successorModel, getString("plugins.chimney.editdependency.successor")) {
        private static final long serialVersionUID = -7376837714390224347L;

        @Override
        protected void onModalWindowClosed(final IModel<AbstractWbsNodeDO> model, final AjaxRequestTarget target)
        {
          if (model.getObject() != null) {
            // disable project selector drop-down of predecessor selector and set to the same project since inter-project dependencies make
            // no sense
            final WbsNodeModalSelectWindowPanel predModalWindow = getPredecessorSelectPanel().getModalWindowPanel();
            predModalWindow.setDisplayedProjectByWbsNode(model.getObject());
            predModalWindow.setProjectSelectorVisible(false);
          }
        }
      };
      succSelect.setRequired(true);
    }
    return succSelect;
  }

  private WbsNodeSelectPanel getPredecessorSelectPanel()
  {
    if (predSelect == null) {
      predSelect = new WbsNodeSelectPanel("predecessor", predecessorModel, getString("plugins.chimney.editdependency.predecessor")) {
        private static final long serialVersionUID = -4028890776875026304L;

        @Override
        protected void onModalWindowClosed(final IModel<AbstractWbsNodeDO> model, final AjaxRequestTarget target)
        {
          if (model.getObject() != null) {
            // disable project selector drop-down of successor selector and set to the same project since inter-project dependencies make no
            // sense
            final WbsNodeModalSelectWindowPanel predModalWindow = getSuccessorSelectPanel().getModalWindowPanel();
            predModalWindow.setDisplayedProjectByWbsNode(model.getObject());
            predModalWindow.setProjectSelectorVisible(false);
          }
        }
      };
      predSelect.setRequired(true);
    }
    return predSelect;
  }

  private WbsActivityDO ensureActivityExists(final IModel<AbstractWbsNodeDO> nodeModel)
  {
    if (nodeModel == null || nodeModel.getObject() == null) {
      return null;
    }

    WbsActivityDO activity = activityDao.getByWbsNode(nodeModel.getObject());
    if (activity == null) {
      activity = new WbsActivityDO(nodeModel.getObject());
      activityDao.save(activity);
    }

    return activity;
  }

  private void submitForm()
  {
    final DependencyRelationDO dbDep = dependencyDao.getByActivities(ensureActivityExists(predecessorModel),
        ensureActivityExists(successorModel));

    if (dbDep != null)
      updateExistingDependency(dbDep);
    else
      saveNewDependency();

    // create a new dep edit page to allow another dep to be created
    final AbstractWbsNodeDO node = predecessorModel.getObject();
    final ProjectDO project = WicketWbsUtils.getProject(node);
    setResponsePage(new DependencyTreePage(project.getId()));
  }

  private void updateExistingDependency(final DependencyRelationDO dbDep)
  {
    dbDep.setType(dependencyModel.getObject().getType());
    dbDep.setOffset(dependencyModel.getObject().getOffset());

    try {
      dependencyDao.saveOrUpdate(dbDep);
    } catch (final CyclicDependencyRelationException cDepExep) {
      form.error(getLocalizedMessage("plugins.chimney.errors.cyclicdependency", dbDep.getPredecessor().getWbsNode(), dbDep.getSuccessor().getWbsNode()));
      return;
    } catch (final SelfDependencyRelationException sDepExcp) {
      form.error(getLocalizedMessage("plugins.chimney.errors.selfdependency", dbDep.getPredecessor().getWbsNode()));
      return;
    }

    form.info(getString("plugins.chimney.editdependency.dependencycreated") + " " + dbDep);
  }

  private void saveNewDependency()
  {
    final DependencyRelationDO dep = dependencyModel.getObject();

    final WbsActivityDO predAct = ensureActivityExists(predecessorModel);
    if (predAct != null) {
      dep.setAndPropagatePredecessor(predAct);
    }

    final WbsActivityDO succAct = ensureActivityExists(successorModel);
    if (succAct != null) {
      dep.setAndPropagateSuccessor(succAct);
    }

    try {
      dependencyDao.save(dep);
    } catch (final CyclicDependencyRelationException cDepExep) {
      form.error(getString("plugins.chimney.errors.cyclicdependency"));
      return;
    } catch (final SelfDependencyRelationException sDepExcp) {
      form.error(getString("plugins.chimney.errors.selfdependency"));
      return;
    }

    form.info(getString("plugins.chimney.editdependency.dependencycreated") + " " + dep);
  }

  /**
   * @see org.projectforge.plugins.chimney.web.AbstractSecuredChimneyPage#getNavigationBarName()
   */
  @Override
  protected String getNavigationBarName()
  {
    return NavigationConstants.WIZARD;
  }

  /**
   * @see org.projectforge.plugins.chimney.web.AbstractSecuredChimneyPage#insertBreadcrumbItems(java.util.List)
   */
  @Override
  protected void insertBreadcrumbItems(final List<String> items)
  {
    items.add(BreadcrumbConstants.PROJECT_PLANNING);
    items.add(BreadcrumbConstants.WIZARD);
    items.add(BreadcrumbConstants.CREATE_DEPENDENCY);
  }

  /**
   * @see org.projectforge.web.wicket.AbstractUnsecureBasePage#getTitle()
   */
  @Override
  protected String getTitle()
  {
    return getString("plugins.chimney.editdependency.title");
  }

  @Override
  protected void onRenderPreviousLink(final PaginationLinkDescription linkDescription)
  {
    linkDescription.displayLink = true;
    linkDescription.linkImage = ImageResources.PREVIOUSLINK_ACTIVITY;
  }

  @Override
  protected Page getPreviousPage()
  {
    return new WizardActivityEditPage(getPageParameters());
  }

}
