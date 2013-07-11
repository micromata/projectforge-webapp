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
import org.projectforge.plugins.chimney.wbs.SubtaskDO;
import org.projectforge.plugins.chimney.wbs.SubtaskDao;
import org.projectforge.plugins.chimney.web.AbstractSecuredChimneyPage;
import org.projectforge.plugins.chimney.web.WicketWbsUtils;
import org.projectforge.plugins.chimney.web.components.ChimneyFeedbackPanel;
import org.projectforge.plugins.chimney.web.navigation.BreadcrumbConstants;
import org.projectforge.plugins.chimney.web.navigation.NavigationConstants;

public class PowerSubtaskEditPage extends AbstractSecuredChimneyPage
{
  private static final long serialVersionUID = -7665037550701489286L;

  public static final String PAGE_ID = "powerSubtaskEdit";

  private final boolean isNew;

  @SpringBean
  private WicketWbsUtils wbsUtils;

  @SpringBean
  private SubtaskDao subtaskDao;

  /**
   * Constructor for creating a new subtask with a given parent
   * @param parameters
   * @param subtaskId currently not used, but there to distinguish the two constructors
   * @param parentId parent of the new subtask. Make sure a node with this id exists or the page will break ungracefully
   */
  public PowerSubtaskEditPage(final PageParameters parameters, final Integer subtaskId, final Integer parentId)
  {
    super(parameters, true);
    isNew = true;
    init(new SubtaskDO(), wbsUtils.getById(parentId));
  }

  /**
   * Constructor for editing a subtask
   * @param parameters
   * @param subtaskId Id of the subtask to edit. Make sure a subtask with this id exists or the page will break ungracefully
   */
  public PowerSubtaskEditPage(final PageParameters parameters, final Integer subtaskId) {
    super(parameters, true);
    isNew = false;
    init(subtaskDao.getOrLoad(subtaskId), null);
  }

  private void init(final SubtaskDO subtask, final AbstractWbsNodeDO parent) {
    final Form<SubtaskDO> form = new PowerSubtaskForm("powerSubtaskEditForm", subtask, parent);
    body.add(form);
    body.add(new ChimneyFeedbackPanel("feedback"));
    body.add(new Label("heading", getString(getHeadingI18n())));
    createNavigation();
  }

  @Override
  protected String getTitle()
  {
    if (isNew)
      return getString("plugins.chimney.createsubtask.title");
    return getString("plugins.chimney.editsubtask.title");
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
      items.add(BreadcrumbConstants.CREATE_SUBTASK);
    } else {
      items.add(BreadcrumbConstants.EDIT_SUBTASK);
    }
  }

  protected String getHeadingI18n()
  {
    if (isNew)
      return "plugins.chimney.createsubtask.heading";
    return "plugins.chimney.editsubtask.heading";
  }

}
