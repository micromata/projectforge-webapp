/////////////////////////////////////////////////////////////////////////////
//
// Project   ProjectForge
//
// Copyright 2001-2009, Micromata GmbH, Kai Reinhard
//           All rights reserved.
//
/////////////////////////////////////////////////////////////////////////////

package org.projectforge.plugins.chimney.web.projectmanagement.powerworkpackage;

import java.util.List;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.projectforge.plugins.chimney.wbs.AbstractWbsNodeDO;
import org.projectforge.plugins.chimney.wbs.MilestoneDO;
import org.projectforge.plugins.chimney.wbs.MilestoneDao;
import org.projectforge.plugins.chimney.web.AbstractSecuredChimneyPage;
import org.projectforge.plugins.chimney.web.WicketWbsUtils;
import org.projectforge.plugins.chimney.web.components.ChimneyFeedbackPanel;
import org.projectforge.plugins.chimney.web.navigation.BreadcrumbConstants;
import org.projectforge.plugins.chimney.web.navigation.NavigationConstants;

public class PowerMilestoneEditPage extends AbstractSecuredChimneyPage
{
  private static final long serialVersionUID = -7665037550701489286L;

  public static final String PAGE_ID = "powerMilestoneEdit";

  private final boolean isNew;

  @SpringBean
  private WicketWbsUtils wbsUtils;

  @SpringBean
  private MilestoneDao milestoneDao;

  /**
   * Constructor for creating a new milestone with a given parent
   * @param parameters
   * @param milestoneId currently not used, but there to distinguish the two constructors
   * @param parentId parent of the new milestone. Make sure a node with this id exists or the page will break ungracefully
   */
  public PowerMilestoneEditPage(final PageParameters parameters, final Integer milestoneId, final Integer parentId)
  {
    super(parameters, true);
    isNew = true;
    init(new MilestoneDO(), wbsUtils.getById(parentId));
  }

  /**
   * Constructor for editing a milestone
   * @param parameters
   * @param milestoneId Id of the milestone to edit. Make sure a milestone with this id exists or the page will break ungracefully
   */
  public PowerMilestoneEditPage(final PageParameters parameters, final Integer milestoneId) {
    super(parameters, true);
    isNew = false;
    init(milestoneDao.getOrLoad(milestoneId), null);
  }

  private void init(final MilestoneDO milestone, final AbstractWbsNodeDO parent) {
    final Form<MilestoneDO> form = new PowerMilestoneForm("powerMilestoneEditForm", milestone, parent);
    body.add(form);
    body.add(new ChimneyFeedbackPanel("feedback"));
    body.add(new Label("heading", getString(getHeadingI18n())));
    createNavigation();
  }

  @Override
  protected String getTitle()
  {
    if (isNew)
      return getString("plugins.chimney.createmilestone.title");
    return getString("plugins.chimney.editmilestone.title");
  }

  @Override
  protected String getNavigationBarName()
  {
    //if (isNew)
    //return NavigationConstants.WIZARD;
    return NavigationConstants.MAIN;
  }

  @Override
  protected void insertBreadcrumbItems(final List<String> items)
  {
    items.add(BreadcrumbConstants.PROJECT_PLANNING);
    if (isNew) {
      //items.add(BreadcrumbConstants.WIZARD);
      items.add(BreadcrumbConstants.CREATE_MILESTONE);
    } else {
      items.add(BreadcrumbConstants.EDIT_MILESTONE);
    }
  }

  protected String getHeadingI18n()
  {
    if (isNew)
      return "plugins.chimney.createmilestone.heading";
    return "plugins.chimney.editmilestone.heading";
  }

}
