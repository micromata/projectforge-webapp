/////////////////////////////////////////////////////////////////////////////
//
// Project   ProjectForge - Chimney Plugin
//
// Copyright 2012, Micromata GmbH
//           All rights reserved.
//
/////////////////////////////////////////////////////////////////////////////

package org.projectforge.plugins.chimney.web.projectmanagement.wizard;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.wicket.Page;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.list.AbstractItem;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.joda.time.DateMidnight;
import org.joda.time.DateTime;
import org.projectforge.core.UserException;
import org.projectforge.plugins.chimney.activities.DependencyRelationDO;
import org.projectforge.plugins.chimney.activities.DependencyRelationDao;
import org.projectforge.plugins.chimney.activities.WbsActivityDO;
import org.projectforge.plugins.chimney.activities.WbsActivityDao;
import org.projectforge.plugins.chimney.resources.ImageResources;
import org.projectforge.plugins.chimney.utils.date.InconsistentFixedDatesException;
import org.projectforge.plugins.chimney.wbs.AbstractWbsNodeDO;
import org.projectforge.plugins.chimney.web.DetachableDOModel;
import org.projectforge.plugins.chimney.web.JodaDateModel;
import org.projectforge.plugins.chimney.web.WicketWbsUtils;
import org.projectforge.plugins.chimney.web.components.ChimneyJodaPeriodField;
import org.projectforge.plugins.chimney.web.components.WbsNodeSelectPanel;
import org.projectforge.plugins.chimney.web.navigation.BreadcrumbConstants;
import org.projectforge.plugins.chimney.web.navigation.NavigationConstants;
import org.projectforge.plugins.chimney.web.navigation.PaginationLinkDescription;
import org.projectforge.web.wicket.components.JodaDatePanel;
import org.projectforge.web.wicket.flowlayout.ButtonPanel;
import org.projectforge.web.wicket.flowlayout.ButtonType;

/**
 * Edit page for activities. Any wbs node object has at most one activity. From the user perspective every wbs node has exactly one
 * activity. Therefore, no distinction is made between creating and editing activities. If no {@link WbsActivityDO} objects exists for a
 * given wbs node, one is created transparently.
 * @author Sweeps <pf@byte-storm.com>
 */
public class WizardActivityEditPage extends AbstractChimneyWizardPage
{
  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(WizardActivityEditPage.class);

  private static final long serialVersionUID = -1541878384596166170L;

  public static final String PAGE_ID = "activityEdit";

  Form<WbsActivityDO> form;

  @SpringBean(name = "wbsActivityDao")
  private WbsActivityDao activityDao;

  @SpringBean(name = "dependencyRelationDao")
  private DependencyRelationDao dependencyDao;

  @SpringBean(name = "wicketWbsUtils")
  private WicketWbsUtils wbsUtils;

  private final IModel<AbstractWbsNodeDO> wbsModel;

  private IModel<WbsActivityDO> wbsActivityModel;

  private final List<PropertyModel<Boolean>> dependencyDeleteModels = new ArrayList<PropertyModel<Boolean>>();

  /**
   * Constructor that is used if user gets here through the wizard. A page with only a wbs node selector is displayed.
   * @param parameters
   */
  public WizardActivityEditPage(final PageParameters parameters)
  {
    super(parameters);
    wbsModel = new Model<AbstractWbsNodeDO>(null);
    wbsActivityModel = new Model<WbsActivityDO>(new WbsActivityDO());
    init();
  }

  /**
   * Constructor for editing/creating an activity for a wbsNode
   * @param parameters
   * @param wbsNodeId id of the wbs node for which to edit/create the activity
   */
  public WizardActivityEditPage(final PageParameters parameters, final int wbsNodeId)
  {
    super(parameters, false);
    wbsModel = wbsUtils.getModelFor(wbsUtils.getById(wbsNodeId));
    final WbsActivityDO activity = activityDao.getByWbsNode(wbsModel.getObject());
    if (activity == null)
      wbsActivityModel = new Model<WbsActivityDO>(new WbsActivityDO());
    else wbsActivityModel = new DetachableDOModel<WbsActivityDO, WbsActivityDao>(activity, activityDao);

    init();
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
    items.add(BreadcrumbConstants.EDIT_ACTIVITY);
  }

  /**
   * @see org.projectforge.web.wicket.AbstractUnsecureBasePage#getTitle()
   */
  @Override
  protected String getTitle()
  {
    return getString("plugins.chimney.editactivity.title");
  }

  private void init()
  {
    addFeedbackPanel();
    addWbsSelector();
    addForm();
  }

  private void addWbsSelector()
  {
    body.add(new WbsNodeSelectPanel("wbsSelect", wbsModel, getString("plugins.chimney.editactivity.wbsNode")) {
      private static final long serialVersionUID = 1L;

      @Override
      protected void onModalWindowClosed(final IModel<AbstractWbsNodeDO> model, final AjaxRequestTarget target)
      {
        if (model != null && model.getObject() != null)
          setResponsePage(new WizardActivityEditPage(getPageParameters(), model.getObject().getId()));
      }
    });
  }

  private void addForm()
  {
    form = new Form<WbsActivityDO>("activityForm", new CompoundPropertyModel<WbsActivityDO>(wbsActivityModel)) {
      private static final long serialVersionUID = -2800048128949818693L;

      @Override
      protected void beforeUpdateFormComponentModels()
      {
        // set fixed dates to null before model is filled with form data to avoid
        // inconsistency exception if the the new begin date is after the old end date
        // or if the new end date is before the old start date
        final WbsActivityDO activity = wbsActivityModel.getObject();
        activity.setFixedBeginDate(null);
        activity.setFixedEndDate(null);
      }

      @Override
      public boolean isVisible()
      {
        // display form only if a wbs node is selected, otherwise only show the wbs selector
        return wbsModel.getObject() != null;
      }
    };

    body.add(new Label("heading", getString("plugins.chimney.editactivity.heading")));

    form.add(new JodaDatePanel("fixedBeginDate", getBeginDateModel()));
    form.add(new JodaDatePanel("fixedEndDate", getEndDateModel()));
    form.add(new ChimneyJodaPeriodField("effortEstimation"));

    // PredecessorRelation List
    final RepeatingView predRepeatingView = new RepeatingView("predecessorRelations");
    form.add(predRepeatingView);
    putDependencyRelationsInto(predRepeatingView, wbsActivityModel.getObject().getPredecessorRelations());

    // SuccessorRelation List
    final RepeatingView succRepeatingView = new RepeatingView("successorRelations");
    form.add(succRepeatingView);
    putDependencyRelationsInto(succRepeatingView, wbsActivityModel.getObject().getSuccessorRelations());

    form.add(new ButtonPanel("submit", getString("plugins.chimney.editpage.save"), new Button("button") {
      private static final long serialVersionUID = -782806647465421462L;

      @Override
      public void onSubmit()
      {
        try {
          onFormSubmit();
        } catch (final UserException ex) {
          form.error(translateParams(ex));
        }
      }
    }, ButtonType.DEFAULT_SUBMIT));

    form.add(new ButtonPanel("submitAndReturn", getString("plugins.chimney.editpage.saveandreturn"), new Button("button") {
      private static final long serialVersionUID = -8122069034450580512L;

      @Override
      public void onSubmit()
      {
        try {
          onFormSubmitAndReturn();
        } catch (final UserException ex) {
          form.error(translateParams(ex));
        }
      }
    }, ButtonType.DEFAULT_SUBMIT));

    body.add(form);
  }

  protected IModel<DateMidnight> getEndDateModel()
  {
    return new JodaDateModel<WbsActivityDO>(wbsActivityModel) {
      private static final long serialVersionUID = 1L;

      @Override
      protected DateTime getDateTime()
      {
        return getModelObject().getFixedEndDate();
      }

      @Override
      protected void setDateTime(final DateTime dateTime)
      {
        try {
          getModelObject().setFixedEndDate(dateTime);
        } catch (final InconsistentFixedDatesException ex) {
          form.error(getString("plugins.chimney.errors.beginenddateinconsistent"));
        }
      }
    };
  }

  protected IModel<DateMidnight> getBeginDateModel()
  {
    return new JodaDateModel<WbsActivityDO>(wbsActivityModel) {
      private static final long serialVersionUID = 1L;

      @Override
      protected DateTime getDateTime()
      {
        return getModelObject().getFixedBeginDate();
      }

      @Override
      protected void setDateTime(final DateTime dateTime)
      {
        try {
          getModelObject().setFixedBeginDate(dateTime);
        } catch (final InconsistentFixedDatesException ex) {
          form.error(getString("plugins.chimney.errors.beginenddateinconsistent"));
        }
      }
    };
  }

  private void putDependencyRelationsInto(final RepeatingView repeatingView, final Set<DependencyRelationDO> relations)
  {
    for (final DependencyRelationDO dep : relations) {
      if (dep.isDeleted())
        continue;

      final AbstractItem item = new AbstractItem(repeatingView.newChildId());
      repeatingView.add(item);

      // dependency label
      item.add(new Label("label", dep.toString()));

      // deleted property CheckBox
      final PropertyModel<Boolean> deletedModel = new PropertyModel<Boolean>(dep, "deleted");
      dependencyDeleteModels.add(deletedModel);
      final CheckBox deletedCheckBox = new CheckBox("deleted", deletedModel) {
        private static final long serialVersionUID = 8553202942627125923L;

        @Override
        protected boolean wantOnSelectionChangedNotifications()
        {
          return true;
        }

      };
      item.add(deletedCheckBox);
    }
  }

  protected void onFormSubmit()
  {
    saveActivity();
    final WizardActivityEditPage newPage = new WizardActivityEditPage(getPageParameters(), wbsActivityModel.getObject().getWbsNode()
        .getId());
    setResponsePage(newPage);
  }

  protected void onFormSubmitAndReturn()
  {
    saveActivity();
    // on success, go to project tree page
    final AbstractWbsNodeDO wbsNode = wbsActivityModel.getObject().getWbsNode();
    final String infoText = getSubmitSuccessInfo(wbsActivityModel.getObject());
    gotoProjectTreePage(wbsNode, infoText);
  }

  private void saveActivity()
  {
    deleteCheckedDependencies();

    // set the wbsNode to a fresh one held by the session if the activity is freshly created
    // if we did this already in the constructor, Hibernate would complain about a transient wbs node instance
    final WbsActivityDO activity = wbsActivityModel.getObject();
    if (activity.getWbsNode() == null)
      activity.setWbsNode(wbsModel.getObject());

    // save the activity
    activityDao.saveOrUpdate(activity);

  }

  private void deleteCheckedDependencies()
  {
    for (final PropertyModel<Boolean> model : dependencyDeleteModels) {
      log.warn("********** To be implemented."); // TODO
      // DependencyRelationDO dep = (DependencyRelationDO) model.getTarget();
      //
      // if (dep.isDeleted()) {
      // // dirty workaround for LazyInitException
      // dep = dependencyDao.getById(dep.getId());
      // dependencyDao.markAsDeleted(dep);
      // }
    }
  }

  private String getSubmitSuccessInfo(final WbsActivityDO activity)
  {
    final AbstractWbsNodeDO wbsNode = activity.getWbsNode();
    return getString("plugins.chimney.editactivity.activitycreated") + " " + wbsNode.getTitle() + " (" + wbsNode.getWbsCode() + ")";
  }

  @Override
  protected void onRenderPreviousLink(final PaginationLinkDescription linkDescription)
  {
    linkDescription.displayLink = true;
    linkDescription.linkImage = ImageResources.PREVIOUSLINK_MILESTONE;
  }

  @Override
  protected Page getPreviousPage()
  {
    return new WizardMilestoneEditPage(getPageParameters());
  }

  @Override
  protected void onRenderNextLink(final PaginationLinkDescription linkDescription)
  {
    linkDescription.displayLink = true;
    linkDescription.linkImage = ImageResources.NEXTLINK_DEPENDENCY;
  }

  @Override
  protected Page getNextPage()
  {
    return new WizardDependencyEditPage(getPageParameters());
  }

}
