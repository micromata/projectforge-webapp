/////////////////////////////////////////////////////////////////////////////
//
// Project   ProjectForge - Chimney Plugin
//
// Copyright 2012, Micromata GmbH
//           All rights reserved.
//
/////////////////////////////////////////////////////////////////////////////

package org.projectforge.plugins.chimney.web.resourceplanning;

import java.util.List;

import org.apache.commons.lang.Validate;
import org.apache.wicket.Page;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.joda.time.DateMidnight;
import org.joda.time.DateTime;
import org.projectforge.common.NumberHelper;
import org.projectforge.core.UserException;
import org.projectforge.plugins.chimney.resourceplanning.ResourceAssignmentDO;
import org.projectforge.plugins.chimney.resourceplanning.ResourceAssignmentDao;
import org.projectforge.plugins.chimney.resources.ImageResources;
import org.projectforge.plugins.chimney.utils.date.InconsistentFixedDatesException;
import org.projectforge.plugins.chimney.wbs.AbstractWbsNodeDO;
import org.projectforge.plugins.chimney.wbs.ProjectDO;
import org.projectforge.plugins.chimney.wbs.WbsNodeIsNullException;
import org.projectforge.plugins.chimney.web.DetachableChangeableDOModel;
import org.projectforge.plugins.chimney.web.DetachableDOModel;
import org.projectforge.plugins.chimney.web.JodaDateModel;
import org.projectforge.plugins.chimney.web.WicketWbsUtils;
import org.projectforge.plugins.chimney.web.components.ChimneyJodaPeriodField;
import org.projectforge.plugins.chimney.web.components.WbsNodeSelectPanel;
import org.projectforge.plugins.chimney.web.navigation.BreadcrumbConstants;
import org.projectforge.plugins.chimney.web.navigation.NavigationConstants;
import org.projectforge.plugins.chimney.web.navigation.PaginationLinkDescription;
import org.projectforge.plugins.chimney.web.projectmanagement.wizard.AbstractChimneyWizardPage;
import org.projectforge.plugins.chimney.web.projectmanagement.wizard.WizardDependencyEditPage;
import org.projectforge.plugins.chimney.web.projectmanagement.wizard.WizardMilestoneEditPage;
import org.projectforge.user.PFUserDO;
import org.projectforge.user.UserDao;
import org.projectforge.web.fibu.ISelectCallerPage;
import org.projectforge.web.user.UserSelectPanel;
import org.projectforge.web.wicket.flowlayout.ButtonPanel;
import org.projectforge.web.wicket.flowlayout.ButtonType;

/**
 * Edit page for assigning resources to activities.
 * @author Sweeps <pf@byte-storm.com>
 */
public class ResourceAssignmentEditPage extends AbstractChimneyWizardPage implements ISelectCallerPage
{

  private static final long serialVersionUID = -1541878384596166170L;
  public static final String PAGE_ID = "resourceAssignmentEdit";

  protected Form<?> form;

  @SpringBean(name="resourceAssignmentDao")
  private ResourceAssignmentDao raDao;
  @SpringBean(name="wicketWbsUtils")
  private WicketWbsUtils wbsUtils;
  @SpringBean(name="userDao")
  private UserDao userDao;

  private final IModel<AbstractWbsNodeDO> wbsModel;
  private final IModel<ResourceAssignmentDO> resourceAssignmentModel;
  private final IModel<PFUserDO> userModel;
  private final boolean isNew;

  /**
   * Constructor that is used if user gets here through the wizard.
   * A page with only a wbs node selector is displayed.
   * @param parameters
   */
  public ResourceAssignmentEditPage(final PageParameters parameters)
  {
    super(parameters);
    wbsModel = new Model<AbstractWbsNodeDO>(null);
    resourceAssignmentModel = new Model<ResourceAssignmentDO>(new ResourceAssignmentDO());
    userModel = new Model<PFUserDO>(null);
    isNew = true;
    init();
  }

  /**
   * Constructor for creating a resource assignment for a wbsNode
   * @param parameters
   * @param wbsNode Wbs node for which to create the resource assignment
   */
  public ResourceAssignmentEditPage(final PageParameters parameters, final AbstractWbsNodeDO wbsNode)
  {
    super(parameters, false);
    wbsModel = wbsUtils.getModelFor(wbsNode);
    resourceAssignmentModel = new Model<ResourceAssignmentDO>(new ResourceAssignmentDO());
    userModel = new Model<PFUserDO>(null);
    isNew = true;
    init();
  }

  /**
   * Constructor for editing a resource assignment
   * @param parameters
   * @param resourceAssignmentId Id if the resource assignment to be edited
   */
  public ResourceAssignmentEditPage(final PageParameters parameters, final int resourceAssignmentId) {
    super(parameters, false);
    resourceAssignmentModel = new DetachableDOModel<ResourceAssignmentDO, ResourceAssignmentDao>(raDao.getById(resourceAssignmentId), raDao);
    wbsModel = wbsUtils.getModelFor(resourceAssignmentModel.getObject().getWbsNode());
    userModel = new DetachableChangeableDOModel<PFUserDO, UserDao>(resourceAssignmentModel.getObject().getUser(), userDao);
    isNew = true;
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
    items.add(BreadcrumbConstants.CREATE_RESOURCEASSIGNMENT);
  }

  /**
   * @see org.projectforge.web.wicket.AbstractUnsecureBasePage#getTitle()
   */
  @Override
  protected String getTitle()
  {
    return getString("plugins.chimney.editresourceassignment.title");
  }

  private void init(){
    addFeedbackPanel();
    addWbsSelector();
    addForm();
  }


  private void addWbsSelector()
  {
    body.add(new WbsNodeSelectPanel("wbsSelect", wbsModel, getString("plugins.chimney.editresourceassignment.wbsNode")));
  }

  private void addForm()
  {
    form = new Form<ResourceAssignmentDO>("resourceAssignmentForm", new CompoundPropertyModel<ResourceAssignmentDO>(resourceAssignmentModel)){
      private static final long serialVersionUID = -2800048128949818693L;

      @Override
      public boolean isVisible()
      {
        // display form only if a wbs node is selected, otherwise only show the wbs selector
        //return wbsModel.getObject() != null;
        return true;
      }
    };

    body.add(new Label("heading", getString("plugins.chimney.editresourceassignment.heading")));

    final UserSelectPanel userSelectPanel = new UserSelectPanel("userSelect", userModel, this, "userId");
    userSelectPanel.setRequired(true);
    form.add(userSelectPanel);
    userSelectPanel.init();

    form.add(new ChimneyJodaPeriodField("plannedEffort").setRequired(true));

    form.add(new ButtonPanel("submit", getString("plugins.chimney.editpage.save"), new Button("button") {
      private static final long serialVersionUID = -3293341033191453218L;

      @Override
      public void onSubmit()
      {
        try{
          onFormSubmit();
        } catch(final UserException ex){
          form.error(translateParams(ex));
        }
      }
    }, ButtonType.DEFAULT_SUBMIT));

    form.add(new ButtonPanel("submitAndReturn", getString("plugins.chimney.editpage.saveandreturn"), new Button("button"){
      private static final long serialVersionUID = -8122069034450580512L;

      @Override
      public void onSubmit()
      {
        try{
          onFormSubmitAndReturn();
        } catch(final UserException ex){
          form.error(translateParams(ex));
        }
      }
    }, ButtonType.DEFAULT_SUBMIT).setVisible(isNew));
    body.add(form);
  }

  protected IModel<DateMidnight> getEndDateModel()
  {
    return new JodaDateModel<ResourceAssignmentDO>(resourceAssignmentModel) {
      private static final long serialVersionUID = 1L;

      @Override
      protected DateTime getDateTime()
      {
        return getModelObject().getEndDate();
      }

      @Override
      protected void setDateTime(final DateTime dateTime)
      {
        try {
          getModelObject().setEndDate(dateTime);
        } catch (final InconsistentFixedDatesException ex) {
          form.error(getString("plugins.chimney.errors.beginenddateinconsistent"));
        }
      }
    };
  }

  protected IModel<DateMidnight> getBeginDateModel()
  {
    return new JodaDateModel<ResourceAssignmentDO>(resourceAssignmentModel) {
      private static final long serialVersionUID = 1L;

      @Override
      protected DateTime getDateTime()
      {
        return getModelObject().getBeginDate();
      }

      @Override
      protected void setDateTime(final DateTime dateTime)
      {
        try {
          getModelObject().setBeginDate(dateTime);
        } catch (final InconsistentFixedDatesException ex) {
          form.error(getString("plugins.chimney.errors.beginenddateinconsistent"));
        }
      }
    };
  }

  @Override
  public void select(final String property, final Object selectedValue)
  {
    if ("userId".equals(property) == true) {
      final Integer id;
      if (selectedValue instanceof String) {
        id = NumberHelper.parseInteger((String) selectedValue);
      } else {
        id = (Integer) selectedValue;
      }

      if (id != null) {
        final PFUserDO user = userDao.getOrLoad(id);
        userModel.setObject(user);
      }
    }
  }

  @Override
  public void unselect(final String property)
  {
  }

  @Override
  public void cancelSelection(final String property)
  {
    // do nothing
  }

  protected void onFormSubmit() {
    saveResourceAssigment();

    if (isNew) {
      // create a new, empty edit page
      final ResourceAssignmentEditPage newPage = new ResourceAssignmentEditPage(getPageParameters(), wbsModel.getObject());
      newPage.info(getString("plugins.chimney.editresourceassignment.resourceassignmentcreated")+" " + resourceAssignmentModel.getObject().toString());
      setResponsePage(newPage);
    } else {
      gotoRessourceAssignmentTreePage(wbsModel.getObject(), getSubmitSuccessInfo());
    }

  }

  protected void onFormSubmitAndReturn() {
    saveResourceAssigment();
    gotoRessourceAssignmentTreePage(wbsModel.getObject(), getSubmitSuccessInfo());
  }

  private void saveResourceAssigment()
  {
    if (wbsModel == null || wbsModel.getObject() == null)
      throw new WbsNodeIsNullException();
    final ResourceAssignmentDO resourceAssignment = resourceAssignmentModel.getObject();
    if (resourceAssignment.getWbsNode() == null)
      resourceAssignment.setWbsNode(wbsModel.getObject());
    resourceAssignment.setUser(userModel.getObject());

    raDao.saveOrUpdate(resourceAssignment);
  }

  private String getSubmitSuccessInfo() {
    return getString("plugins.chimney.editresourceassignment.resourceassignmentsaved")+" " + resourceAssignmentModel.getObject().toString();
  }

  protected void gotoRessourceAssignmentTreePage(final AbstractWbsNodeDO node, final String infoText) {
    Validate.notNull(node);
    final ProjectDO project = WicketWbsUtils.getProject(node);

    final ResourceAssignmentTreePage newPage = new ResourceAssignmentTreePage(project.getId());
    if (infoText != null && !infoText.isEmpty())
      newPage.info(infoText);
    setResponsePage(newPage);
  }

  @Override
  protected void onRenderPreviousLink(final PaginationLinkDescription linkDescription)
  {
    linkDescription.displayLink = true;
    // TODO change image
    linkDescription.linkImage = ImageResources.PREVIOUSLINK_MILESTONE;
  }

  @Override
  protected Page getPreviousPage()
  {
    // TODO change page
    return new WizardMilestoneEditPage(getPageParameters());
  }

  @Override
  protected void onRenderNextLink(final PaginationLinkDescription linkDescription)
  {
    linkDescription.displayLink = true;
    // TODO change image
    linkDescription.linkImage = ImageResources.NEXTLINK_DEPENDENCY;
  }

  @Override
  protected Page getNextPage()
  {
    // TODO change page
    return new WizardDependencyEditPage(getPageParameters());
  }
}
