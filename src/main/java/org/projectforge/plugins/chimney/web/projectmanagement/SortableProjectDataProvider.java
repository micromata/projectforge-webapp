/////////////////////////////////////////////////////////////////////////////
//
// Project   ProjectForge
//
// Copyright 2001-2009, Micromata GmbH, Kai Reinhard
//           All rights reserved.
//
/////////////////////////////////////////////////////////////////////////////

package org.projectforge.plugins.chimney.web.projectmanagement;

import java.util.List;

import org.apache.wicket.extensions.markup.html.repeater.data.sort.SortOrder;
import org.apache.wicket.injection.Injector;
import org.apache.wicket.model.IModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.projectforge.core.BaseSearchFilter;
import org.projectforge.plugins.chimney.wbs.ProjectDO;
import org.projectforge.plugins.chimney.wbs.ProjectDao;
import org.projectforge.plugins.chimney.web.DetachableDOModel;
import org.projectforge.web.wicket.MySortableDataProvider;

/**
 * Data provider for loading the project list from the database.
 * The project list is automatically sorted by a given property
 * and ascending or descending order.
 */
public class SortableProjectDataProvider extends MySortableDataProvider<ProjectDO>//SortableDataProvider<ProjectDO>
{

  private static final long serialVersionUID = 5794984642339988955L;

  @SpringBean(name = "projectDao")
  private ProjectDao projectDao;

  private final BaseSearchFilter filter;

  public SortableProjectDataProvider(final String property, final SortOrder sortOrder, final BaseSearchFilter filter)
  {
    super(property, sortOrder);
    this.filter = filter;
  }

  @Override
  public List<ProjectDO> getList()
  {
    if (projectDao == null)
      Injector.get().inject(this);
    return projectDao.getList(filter);
  }

  @Override
  protected IModel<ProjectDO> getModel(final ProjectDO object)
  {
    if (projectDao == null)
      Injector.get().inject(this);
    return new DetachableDOModel<ProjectDO, ProjectDao>(object, projectDao);
  }

}
