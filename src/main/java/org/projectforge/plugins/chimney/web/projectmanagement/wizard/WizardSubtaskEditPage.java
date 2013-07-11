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
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.projectforge.plugins.chimney.resources.ImageResources;
import org.projectforge.plugins.chimney.wbs.AbstractWbsNodeDO;
import org.projectforge.plugins.chimney.wbs.SubtaskDO;
import org.projectforge.plugins.chimney.wbs.SubtaskDao;
import org.projectforge.plugins.chimney.web.navigation.BreadcrumbConstants;
import org.projectforge.plugins.chimney.web.navigation.PaginationLinkDescription;

/**
 * Edit page for creating and editing subtasks.
 * @author Sweeps <pf@byte-storm.com>
 */
public class WizardSubtaskEditPage extends AbstractWizardWbsWithParentNodeEditPage<SubtaskDO>
{
  private static final long serialVersionUID = -2653358497553295433L;

  public static final String PAGE_ID = "subtaskEdit";

  @SpringBean(name="subtaskDao")
  protected SubtaskDao subtaskDao;

  /**
   * Default constructor used by navigation
   * @param parameters
   */
  public WizardSubtaskEditPage(final PageParameters parameters) {
    super(parameters, new SubtaskDO());
  }

  /**
   * Constructor for editing a subtask
   * @param parameters
   * @param subtaskId Id if the subtask to be edited
   */
  public WizardSubtaskEditPage(final PageParameters parameters, final int subtaskId) {
    super(parameters, subtaskId, SubtaskDO.prototype);
  }

  /**
   * Constructor for creating a new subtask using the given parent node.
   * If parent is null, it is attempted to set the parent node to the last edited project
   * @param parameters
   * @param parent
   */
  public WizardSubtaskEditPage(final PageParameters parameters, final AbstractWbsNodeDO parent) {
    super(parameters, parent, new SubtaskDO());
  }

  @Override
  protected String getTitle()
  {
    if (isNew)
      return getString("plugins.chimney.createsubtask.title");
    return getString("plugins.chimney.editsubtask.title");
  }

  @Override
  protected void insertBreadcrumbItems(final List<String> items)
  {
    items.add(BreadcrumbConstants.PROJECT_PLANNING);
    if (isNew) {
      items.add(BreadcrumbConstants.WIZARD);
      items.add(BreadcrumbConstants.CREATE_SUBTASK);
    } else {
      items.add(BreadcrumbConstants.EDIT_SUBTASK);
    }
  }

  @Override
  protected String getHeadingI18n()
  {
    if (isNew)
      return "plugins.chimney.createsubtask.heading";
    return "plugins.chimney.editsubtask.heading";
  }

  @Override
  protected void onFormSubmit(final IModel<SubtaskDO> model)
  {
    super.onFormSubmit(model);
    propagatePhaseChange(model.getObject());

    if (isNew) {
      // create a new, empty edit page
      final WizardSubtaskEditPage newPage = new WizardSubtaskEditPage(getPageParameters(), parentModel.getObject());
      newPage.info(getString("plugins.chimney.editsubtask.subtaskcreated")+" " + model.getObject().getTitle());
      setResponsePage(newPage);
    } else {
      final String infoText = getString("plugins.chimney.editsubtask.subtasksaved")+" " + model.getObject().getTitle();
      gotoProjectTreePage(model.getObject(), infoText);
    }
  }


  private void propagatePhaseChange(final AbstractWbsNodeDO node){

    for(int index = 0; index < node.childrenCount(); index++){
      final AbstractWbsNodeDO child = node.getChild(index);

      child.setPhase(node.getPhase());
      wbsUtils.getDaoFor(child).saveOrUpdate(child);
      propagatePhaseChange(child);
    }
  }



  @Override
  protected void onRenderPreviousLink(final PaginationLinkDescription linkDescription)
  {
    linkDescription.displayLink = true;
    linkDescription.linkImage = ImageResources.PREVIOUSLINK_PROJECT;
  }

  @Override
  protected Page getPreviousPage()
  {
    return new WizardProjectEditPage(getPageParameters());
  }

  @Override
  protected void onRenderNextLink(final PaginationLinkDescription linkDescription)
  {
    linkDescription.displayLink = true;
    linkDescription.linkImage = ImageResources.NEXTLINK_WORKPACKAGE;
  }

  @Override
  protected Page getNextPage()
  {
    return new WizardWorkpackageEditPage(getPageParameters(), isNew?null:wbsNodeModel.getObject());
  }


  @Override
  protected String getSubmitSuccessInfo(final IModel<SubtaskDO> model)
  {
    return getString("plugins.chimney.editsubtask.subtaskcreated")+" " + model.getObject().getTitle();
  }

}
