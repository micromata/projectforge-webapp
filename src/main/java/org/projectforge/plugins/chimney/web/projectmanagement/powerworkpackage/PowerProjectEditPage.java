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
import org.projectforge.plugins.chimney.wbs.ProjectDO;
import org.projectforge.plugins.chimney.wbs.ProjectDao;
import org.projectforge.plugins.chimney.web.AbstractSecuredChimneyPage;
import org.projectforge.plugins.chimney.web.components.ChimneyFeedbackPanel;
import org.projectforge.plugins.chimney.web.navigation.BreadcrumbConstants;
import org.projectforge.plugins.chimney.web.navigation.NavigationConstants;

public class PowerProjectEditPage extends AbstractSecuredChimneyPage
{
  private static final long serialVersionUID = -7665037550701489286L;

  public static final String PAGE_ID = "powerProjectEdit";

  private final boolean isNew;

  @SpringBean
  ProjectDao projectDao;

  /**
   * Constructor for creating a new project
   * @param parameters
   */
  public PowerProjectEditPage(final PageParameters parameters)
  {
    super(parameters, true);
    isNew = true;
    init(new ProjectDO());
  }

  /**
   * Constructor for editing a project
   * @param parameters
   * @param projectId Id of the project to edit. Make sure a project with this id exists or this page will break ungracefully
   */
  public PowerProjectEditPage(final PageParameters parameters, final Integer projectId) {
    super(parameters, true);
    isNew = false;
    init(projectDao.getOrLoad(projectId));
  }

  private void init(final ProjectDO project) {
    final Form<ProjectDO> form = new PowerProjectForm("powerProjectEditForm", project);
    body.add(form);
    body.add(new ChimneyFeedbackPanel("feedback"));
    body.add(new Label("heading", getString(getHeadingI18n())));
    createNavigation();
  }

  @Override
  protected String getTitle()
  {
    if (isNew)
      return getString("plugins.chimney.createproject.title");
    return getString("plugins.chimney.editproject.title");
  }

  @Override
  protected String getNavigationBarName()
  {
    //if (isNew)
    //  return NavigationConstants.WIZARD;
    return NavigationConstants.MAIN;
  }

  @Override
  protected void insertBreadcrumbItems(final List<String> items)
  {
    items.add(BreadcrumbConstants.PROJECT_PLANNING);
    if (isNew) {
      //items.add(BreadcrumbConstants.WIZARD);
      items.add(BreadcrumbConstants.CREATE_PROJECT);
    } else {
      items.add(BreadcrumbConstants.EDIT_PROJECT);
    }
  }

  protected String getHeadingI18n()
  {
    if (isNew)
      return "plugins.chimney.createproject.heading";
    return "plugins.chimney.editproject.heading";
  }

}
