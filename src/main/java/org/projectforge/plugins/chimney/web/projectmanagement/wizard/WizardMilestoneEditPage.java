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
import org.projectforge.plugins.chimney.wbs.MilestoneDO;
import org.projectforge.plugins.chimney.web.navigation.BreadcrumbConstants;
import org.projectforge.plugins.chimney.web.navigation.PaginationLinkDescription;

/**
 * Edit page for creating and editing milestones.
 * @author Sweeps <pf@byte-storm.com>
 */
public class WizardMilestoneEditPage extends AbstractWizardWbsWithParentNodeEditPage<MilestoneDO>
{
  private static final long serialVersionUID = -2653358497553295433L;

  public static final String PAGE_ID = "milestoneEdit";

  /**
   * Default constructor used by navigation
   * @param parameters
   */
  public WizardMilestoneEditPage(final PageParameters parameters)
  {
    super(parameters, new MilestoneDO());
  }

  /**
   * Constructor for editing a milestone
   * @param parameters
   * @param milestoneId Id if the milestone to be edited
   */
  public WizardMilestoneEditPage(final PageParameters parameters, final int milestoneId)
  {
    super(parameters, milestoneId, MilestoneDO.prototype);
  }

  /**
   * Constructor for creating a new milestone using the given parent node. If parent is null, it is attempted to set the parent node to the
   * last edited project
   * @param parameters
   * @param parent
   */
  public WizardMilestoneEditPage(final PageParameters parameters, final AbstractWbsNodeDO parent)
  {
    super(parameters, parent, new MilestoneDO());
  }

  @Override
  protected String getTitle()
  {
    if (isNew)
      return getString("plugins.chimney.createmilestone.title");
    return getString("plugins.chimney.editmilestone.title");
  }

  @Override
  protected void insertBreadcrumbItems(final List<String> items)
  {
    items.add(BreadcrumbConstants.PROJECT_PLANNING);
    if (isNew) {
      items.add(BreadcrumbConstants.WIZARD);
      items.add(BreadcrumbConstants.CREATE_MILESTONE);
    } else {
      items.add(BreadcrumbConstants.EDIT_MILESTONE);
    }
  }

  @Override
  protected String getHeadingI18n()
  {
    if (isNew)
      return "plugins.chimney.createmilestone.heading";
    return "plugins.chimney.editmilestone.heading";
  }

  @Override
  protected void onFormSubmit(final IModel<MilestoneDO> model)
  {
    super.onFormSubmit(model);

    if (isNew) {
      // create a new, empty edit page
      final WizardMilestoneEditPage newPage = new WizardMilestoneEditPage(getPageParameters(), parentModel.getObject());
      newPage.info(getString("plugins.chimney.editmilestone.milestonecreated") + " " + model.getObject().getTitle());
      setResponsePage(newPage);
    } else {
      final String infoText = getString("plugins.chimney.editmilestone.milestonesaved") + " " + model.getObject().getTitle();
      gotoProjectTreePage(model.getObject(), infoText);
    }
  }

  @Override
  protected void onFormSubmitAndReturn(final IModel<MilestoneDO> model)
  {
    super.onFormSubmitAndReturn(model);
  }

  @Override
  protected Page getPreviousPage()
  {
    return new WizardSubtaskEditPage(getPageParameters());
  }

  @Override
  protected void onRenderPreviousLink(final PaginationLinkDescription linkDescription)
  {
    linkDescription.displayLink = true;
    linkDescription.linkImage = ImageResources.PREVIOUSLINK_SUBTASK;
  }

  @Override
  protected Page getNextPage()
  {
    return new WizardActivityEditPage(getPageParameters());
  }

  @Override
  protected void onRenderNextLink(final PaginationLinkDescription linkDescription)
  {
    linkDescription.displayLink = true;
    linkDescription.linkImage = ImageResources.NEXTLINK_ACTIVITY;
  }

  @Override
  protected String getSubmitSuccessInfo(final IModel<MilestoneDO> model)
  {
    return getString("plugins.chimney.editmilestone.milestonecreated") + " " + model.getObject().getTitle();
  }

  // @Override
  // protected void addAdditionalFields(final String id, final Form<MilestoneDO> form, final IModel<MilestoneDO> wbsNodeModel)
  // {
  // form.add(new ResponsibleUserPanel<MilestoneDO>(id, wbsNodeModel){
  // private static final long serialVersionUID = -1551457796146347898L;
  //
  // @Override
  // protected void addNodeResponsibleUser()
  // {
  // add(new Label("userSelect", "-"));
  // }
  // });
  // }

}
