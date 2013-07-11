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
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.projectforge.plugins.chimney.resources.ImageResources;
import org.projectforge.plugins.chimney.wbs.AbstractWbsNodeDO;
import org.projectforge.plugins.chimney.wbs.WorkpackageDO;
import org.projectforge.plugins.chimney.web.navigation.BreadcrumbConstants;
import org.projectforge.plugins.chimney.web.navigation.PaginationLinkDescription;

/**
 * Edit page for creating and editing workpackages.
 * @author Sweeps <pf@byte-storm.com>
 */
public class WizardWorkpackageEditPage extends AbstractWizardWbsWithParentNodeEditPage<WorkpackageDO>
{
  private static final long serialVersionUID = -406870004247796446L;

  public static final String PAGE_ID = "workpackageEdit";

  /**
   * Default constructor used by navigation
   * @param parameters
   */
  public WizardWorkpackageEditPage(final PageParameters parameters) {
    super(parameters, new WorkpackageDO());
  }

  /**
   * Constructor for editing a workpackage
   * @param parameters
   * @param workpackageId Id if the workpackage to be edited
   */
  public WizardWorkpackageEditPage(final PageParameters parameters, final int workpackageId) {
    super(parameters, workpackageId, WorkpackageDO.prototype);
  }

  /**
   * Constructor for creating a new workpackage using the given parent node.
   * If parent is null, it is attempted to set the parent node to the last edited project
   * @param parameters
   * @param parent
   */
  public WizardWorkpackageEditPage(final PageParameters parameters, final AbstractWbsNodeDO parent) {
    super(parameters, parent, new WorkpackageDO());
  }

  @Override
  protected String getTitle()
  {
    if (isNew)
      return getString("plugins.chimney.createworkpackage.title");
    return getString("plugins.chimney.editworkpackage.title");
  }

  @Override
  protected void insertBreadcrumbItems(final List<String> items)
  {
    items.add(BreadcrumbConstants.PROJECT_PLANNING);
    if (isNew) {
      items.add(BreadcrumbConstants.WIZARD);
      items.add(BreadcrumbConstants.CREATE_WORKPACKAGE);
    } else {
      items.add(BreadcrumbConstants.EDIT_WORKPACKAGE);
    }
  }

  @Override
  protected String getHeadingI18n()
  {
    if (isNew)
      return "plugins.chimney.createworkpackage.heading";
    return "plugins.chimney.editworkpackage.heading";
  }

  @Override
  protected void onFormSubmit(final IModel<WorkpackageDO> model)
  {
    super.onFormSubmit(model);

    if (isNew) {
      // create a new, empty edit page
      final WizardWorkpackageEditPage newPage = new WizardWorkpackageEditPage(getPageParameters(), parentModel.getObject());
      newPage.info(getString("plugins.chimney.editworkpackage.workpackagecreated")+" " + model.getObject().getTitle());
      setResponsePage(newPage);
    } else {
      final String infoText = getString("plugins.chimney.editworkpackage.workpackagesaved")+" " + model.getObject().getTitle();
      gotoProjectTreePage(model.getObject(), infoText);
    }
  }

  @Override
  protected void onRenderPreviousLink(final PaginationLinkDescription linkDescription)
  {
    linkDescription.displayLink = true;
    linkDescription.linkImage = ImageResources.PREVIOUSLINK_SUBTASK;
  }

  @Override
  protected Page getPreviousPage()
  {
    return new WizardSubtaskEditPage(getPageParameters());
  }

  @Override
  protected void onRenderNextLink(final PaginationLinkDescription linkDescription)
  {
    linkDescription.displayLink = true;
    linkDescription.linkImage = ImageResources.NEXTLINK_MILESTONE;
  }

  @Override
  protected Page getNextPage()
  {
    return new WizardMilestoneEditPage(getPageParameters());
  }

  @Override
  protected String getSubmitSuccessInfo(final IModel<WorkpackageDO> model)
  {
    return getString("plugins.chimney.editworkpackage.workpackagecreated")+" " + model.getObject().getTitle();
  }
}
