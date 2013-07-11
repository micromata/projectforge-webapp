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
import org.projectforge.plugins.chimney.wbs.WorkpackageDO;
import org.projectforge.plugins.chimney.wbs.WorkpackageDao;
import org.projectforge.plugins.chimney.web.AbstractSecuredChimneyPage;
import org.projectforge.plugins.chimney.web.WicketWbsUtils;
import org.projectforge.plugins.chimney.web.components.ChimneyFeedbackPanel;
import org.projectforge.plugins.chimney.web.navigation.BreadcrumbConstants;
import org.projectforge.plugins.chimney.web.navigation.NavigationConstants;

public class PowerWorkpackageEditPage extends AbstractSecuredChimneyPage
{
  private static final long serialVersionUID = -7665037550701489286L;

  public static final String PAGE_ID = "powerWorkpackageEdit";

  private final boolean isNew;

  @SpringBean
  private WicketWbsUtils wbsUtils;

  @SpringBean
  private WorkpackageDao workpackageDao;

  /**
   * Constructor for creating a new workpackage with a given parent
   * @param parameters
   * @param workpackageId currently not used, but there to distinguish the two constructors
   * @param parentId parent of the new workpackage. Make sure a node with this id exists or the page will break ungracefully
   */
  public PowerWorkpackageEditPage(final PageParameters parameters, final Integer workpackageId, final Integer parentId)
  {
    super(parameters, true);
    isNew = true;
    init(new WorkpackageDO(), wbsUtils.getById(parentId));
  }

  /**
   * Constructor for editing a workpackage
   * @param parameters
   * @param workpackageId Id of the workpackage to edit. Make sure a workpackage with this id exists or the page will break ungracefully
   */
  public PowerWorkpackageEditPage(final PageParameters parameters, final Integer workpackageId) {
    super(parameters, true);
    isNew = false;
    init(workpackageDao.getOrLoad(workpackageId), null);
  }

  private void init(final WorkpackageDO workpackage, final AbstractWbsNodeDO parent) {
    final Form<WorkpackageDO> form = new PowerWorkpackageForm("powerWorkpackageEditForm", workpackage, parent);
    body.add(form);
    body.add(new ChimneyFeedbackPanel("feedback"));
    body.add(new Label("heading", getString(getHeadingI18n())));
    createNavigation();
  }

  @Override
  protected String getTitle()
  {
    if (isNew)
      return getString("plugins.chimney.createworkpackage.title");
    return getString("plugins.chimney.editworkpackage.title");
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
      items.add(BreadcrumbConstants.CREATE_WORKPACKAGE);
    } else {
      items.add(BreadcrumbConstants.EDIT_WORKPACKAGE);
    }
  }

  protected String getHeadingI18n()
  {
    if (isNew)
      return "plugins.chimney.createworkpackage.heading";
    return "plugins.chimney.editworkpackage.heading";
  }

}
