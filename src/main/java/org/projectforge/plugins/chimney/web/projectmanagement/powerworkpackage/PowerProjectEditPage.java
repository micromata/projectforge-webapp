/////////////////////////////////////////////////////////////////////////////
//
// Project   ProjectForge
//
// Copyright 2001-2009, Micromata GmbH, Kai Reinhard
//           All rights reserved.
//
/////////////////////////////////////////////////////////////////////////////

package org.projectforge.plugins.chimney.web.projectmanagement.powerworkpackage;

import org.apache.log4j.Logger;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.projectforge.plugins.chimney.activities.WbsActivityDO;
import org.projectforge.plugins.chimney.activities.WbsActivityDao;
import org.projectforge.plugins.chimney.wbs.ProjectDO;
import org.projectforge.plugins.chimney.wbs.ProjectDao;
import org.projectforge.plugins.chimney.web.WicketWbsUtils;
import org.projectforge.plugins.chimney.web.projecttree.ProjectTreePage;
import org.projectforge.web.fibu.ISelectCallerPage;
import org.projectforge.web.wicket.AbstractEditPage;
import org.projectforge.web.wicket.AbstractSecuredBasePage;

public class PowerProjectEditPage extends AbstractEditPage<ProjectDO, PowerProjectEditForm, ProjectDao> implements ISelectCallerPage
{
  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(PowerProjectEditPage.class);

  private static final long serialVersionUID = -7665037550701489286L;

  public static final String PAGE_ID = "powerProjectEdit";

  @SpringBean
  private ProjectDao projectDao;

  @SpringBean
  private WbsActivityDao wbsActivityDao;

  /**
   * Constructor for creating a new project
   * @param parameters
   */
  public PowerProjectEditPage(final PageParameters parameters)
  {
    super(parameters, "plugins.chimney.project");
    init();
  }

  /**
   * @see org.projectforge.web.wicket.AbstractEditPage#afterSaveOrUpdate()
   */
  @Override
  public AbstractSecuredBasePage afterSaveOrUpdate()
  {
    final WbsActivityDO activity = form.getActivity();
    // Currently there is no possibility (at least I have not found any) to fully validate the project before trying to save it. The
    // case of sister tasks with the same title can only be observed by trying to save can catch the exception on error.
    wbsActivityDao.setWbsNode(activity, getData().getId());
    wbsActivityDao.saveOrUpdate(activity);
    return new ProjectTreePage(WicketWbsUtils.getProject(getData()).getId());
  }

  /**
   * @see org.projectforge.web.fibu.ISelectCallerPage#select(java.lang.String, java.lang.Integer)
   */
  public void select(final String property, final Object selectedValue)
  {
    if ("responsibleUserId".equals(property) == true) {
      projectDao.setResponsibleUser(getData(), (Integer) selectedValue);
    } else {
      log.error("Property '" + property + "' not supported for selection.");
    }
  }

  /**
   * @see org.projectforge.web.fibu.ISelectCallerPage#unselect(java.lang.String)
   */
  public void unselect(final String property)
  {
    if ("responsibleUserId".equals(property) == true) {
      getData().setResponsibleUser(null);
    } else {
      log.error("Property '" + property + "' not supported for selection.");
    }
  }

  /**
   * @see org.projectforge.web.fibu.ISelectCallerPage#cancelSelection(java.lang.String)
   */
  public void cancelSelection(final String property)
  {
    // Do nothing.
  }

  /**
   * @see org.projectforge.web.wicket.AbstractEditPage#getBaseDao()
   */
  @Override
  protected ProjectDao getBaseDao()
  {
    return projectDao;
  }

  @Override
  protected PowerProjectEditForm newEditForm(final AbstractEditPage< ? , ? , ? > parentPage, final ProjectDO data)
  {
    return new PowerProjectEditForm(this, data);
  }

  /**
   * @see org.projectforge.web.wicket.AbstractEditPage#getLogger()
   */
  @Override
  protected Logger getLogger()
  {
    return log;
  }
}
