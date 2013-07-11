/////////////////////////////////////////////////////////////////////////////
//
// Project   ProjectForge
//
// Copyright 2001-2009, Micromata GmbH, Kai Reinhard
//           All rights reserved.
//
/////////////////////////////////////////////////////////////////////////////

package org.projectforge.plugins.chimney.web.projectmanagement.wizard;

import java.util.List;

import org.apache.wicket.Page;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.projectforge.plugins.chimney.resources.ImageResources;
import org.projectforge.plugins.chimney.wbs.MilestoneDO;
import org.projectforge.plugins.chimney.wbs.ProjectDO;
import org.projectforge.plugins.chimney.web.SessionAttribute;
import org.projectforge.plugins.chimney.web.WicketWbsUtils;
import org.projectforge.plugins.chimney.web.navigation.BreadcrumbConstants;
import org.projectforge.plugins.chimney.web.navigation.PaginationLinkDescription;
import org.projectforge.plugins.chimney.web.projecttree.ProjectTreePage;

/**
 * Edit page for creating and editing projects. Unlike other wbs node edit pages, ProjectEditPage has no parent selector as projects are
 * always root nodes.
 * @author Sweeps <pf@byte-storm.com>
 */
public class WizardProjectEditPage extends AbstractWizardWbsNodeEditPage<ProjectDO>
{
  private static final long serialVersionUID = -406870004247796446L;

  public static final String PAGE_ID = "projectEdit";

  @SpringBean(name = "wicketWbsUtils")
  private WicketWbsUtils wbsUtils;

  public WizardProjectEditPage(final PageParameters parameters)
  {
    this(parameters, null);
  }

  public WizardProjectEditPage(final PageParameters parameters, final Integer projectId)
  {
    super(parameters);
    init(getModel(projectId), projectId == null);
  }

  private IModel<ProjectDO> getModel(final Integer projectId)
  {
    if (projectId != null)
      return wbsUtils.getModelFor(projectId, ProjectDO.prototype);
    return new Model<ProjectDO>(new ProjectDO());
  }

  @Override
  protected void addAdditionalFields(final String id, final Form<ProjectDO> form, final IModel<ProjectDO> wbsNodeModel)
  {
    form.replace(new ProjectAdditionalFieldsPanel("responsibleUser", wbsNodeModel));
    super.addAdditionalFields(id, form, wbsNodeModel);
  }

  @Override
  protected String getTitle()
  {
    if (isNew)
      return getString("plugins.chimney.createproject.title");
    return getString("plugins.chimney.editproject.title");
  }

  @Override
  protected void insertBreadcrumbItems(final List<String> items)
  {
    items.add(BreadcrumbConstants.PROJECT_PLANNING);
    if (isNew) {
      items.add(BreadcrumbConstants.WIZARD);
      items.add(BreadcrumbConstants.CREATE_PROJECT);
    } else {
      items.add(BreadcrumbConstants.EDIT_PROJECT);
    }
  }

  @Override
  protected String getHeadingI18n()
  {
    if (isNew)
      return "plugins.chimney.createproject.heading";
    return "plugins.chimney.editproject.heading";
  }

  @Override
  protected void onFormSubmit(final IModel<ProjectDO> model)
  {
    // save the project
    wbsUtils.saveOrUpdate(model);

    // set the response page based on whether a new project was created
    if (isNew) {
      addInitialMilestone(model);

      // create a new, empty edit page
      final ProjectDO projectDo = model.getObject();
      final WizardProjectEditPage newPage = new WizardProjectEditPage(getPageParameters());
      newPage.info(getString("plugins.chimney.editproject.projectcreated") + " " + projectDo.getTitle());
      // save the id of the newly inserted project as the last used project id in the session
      getSession().setAttribute(SessionAttribute.LAST_USED_PROJECT_ID, projectDo.getId());
      setResponsePage(newPage);
    } else {
      // return to list page
      final String infoText = getString("plugins.chimney.editproject.projectsaved") + " " + model.getObject().getTitle();
      gotoProjectTreePage(model.getObject(), infoText);
    }
  }

  private void addInitialMilestone(final IModel<ProjectDO> model)
  {
    final MilestoneDO milestone = new MilestoneDO();
    milestone.setTitle(getMilestoneTitle(model));
    model.getObject().autoIncrementAndGet();
    milestone.setWbsCode(getMilestoneWBSCode(model));
    model.getObject().addChild(milestone);
    wbsUtils.getDaoFor(milestone).saveOrUpdate(milestone);

    wbsUtils.getDaoFor(model.getObject()).saveOrUpdate(model.getObject());

  }

  private String getMilestoneWBSCode(final IModel<ProjectDO> model)
  {
    return model.getObject().getWbsCode() + "." + "1";
  }

  private String getMilestoneTitle(final IModel<ProjectDO> model)
  {
    // TODO: i18n-key
    return model.getObject().getTitle() + " " + "Start";
  }

  @Override
  protected void onFormSubmitAndReturn(final IModel<ProjectDO> model)
  {
    // save the project
    wbsUtils.saveOrUpdate(model);

    addInitialMilestone(model);

    final ProjectTreePage newPage = new ProjectTreePage(model.getObject().getId());
    newPage.info(getString("plugins.chimney.editproject.projectcreated") + " " + model.getObject().getTitle());
    setResponsePage(newPage);
  }

  @Override
  protected Page getNextPage()
  {
    return new WizardSubtaskEditPage(getPageParameters(), isNew ? null : wbsNodeModel.getObject());
  }

  @Override
  protected void onRenderNextLink(final PaginationLinkDescription linkDescription)
  {
    linkDescription.displayLink = true;
    linkDescription.linkImage = ImageResources.NEXTLINK_SUBTASK;
  }
}
