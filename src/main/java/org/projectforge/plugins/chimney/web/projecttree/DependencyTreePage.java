/////////////////////////////////////////////////////////////////////////////
//
// Project   ProjectForge - Chimney Plugin
//
// Copyright 2012, Micromata GmbH
//           All rights reserved.
//
/////////////////////////////////////////////////////////////////////////////

package org.projectforge.plugins.chimney.web.projecttree;

import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.extensions.markup.html.repeater.data.table.AbstractColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.projectforge.plugins.chimney.activities.DependencyRelationDO;
import org.projectforge.plugins.chimney.activities.DependencyRelationDao;
import org.projectforge.plugins.chimney.activities.WbsActivityDO;
import org.projectforge.plugins.chimney.activities.WbsActivityDao;
import org.projectforge.plugins.chimney.wbs.AbstractWbsNodeDO;
import org.projectforge.plugins.chimney.web.navigation.BreadcrumbConstants;

/**
 * Tree page that displays one or more projects in a tree and each nodes
 * associated predecessor dependencies.
 * @author Sweeps <pf@byte-storm.com>
 */
public class DependencyTreePage extends ExtendableProjectTreePage
{
  private static final long serialVersionUID = 2340760474451446636L;
  public static final String PAGE_ID = "dependencyTreePage";

  @SpringBean(name = "dependencyRelationDao")
  private DependencyRelationDao dependencyDao;

  @SpringBean(name = "wbsActivityDao")
  private WbsActivityDao activityDao;

  public DependencyTreePage(final Integer... projectIds)
  {
    super(projectIds);
  }

  public DependencyTreePage(final PageParameters parameters)
  {
    super(parameters);
  }

  @Override
  protected IColumn<AbstractWbsNodeDO, String> getCustomColumn()
  {
    return new AbstractColumn<AbstractWbsNodeDO, String>(Model.of(getString("plugins.chimney.dependencytree.headpredecessors"))) {
      private static final long serialVersionUID = 1L;
      @Override
      public void populateItem(final Item<ICellPopulator<AbstractWbsNodeDO>> cellItem, final String componentId, final IModel<AbstractWbsNodeDO> rowModel)
      {
        final WbsActivityDO activity = activityDao.getByWbsNode(rowModel.getObject());
        if (activity == null || activity.getPredecessorRelations() == null || activity.getPredecessorRelations().isEmpty())
          cellItem.add(new Label(componentId, "-"));
        else
          cellItem.add(new DependencyTablePanel(componentId, Model.of(activity), new PredecessorWbsCodeComparator()) {
            private static final long serialVersionUID = 1L;

            @Override
            public void deleteDependency(final int dependencyId)
            {
              final DependencyRelationDO dep = dependencyDao.getById(dependencyId);
              if (dep == null) {
                DependencyTreePage.this.error(getString("plugins.chimney.errors.dependencynotfound"));
              } else {
                dependencyDao.markAsDeleted(dep);
                DependencyTreePage.this.info(getString("plugins.chimney.dependencytree.deleted")+" "+dep.toString());
              }
            }
          });
      }
    };
  }

  @Override
  protected void addActionLinks(final RepeatingView rv, final AbstractWbsNodeDO wbsNode)
  {
    final Component newDependencyLink = createNewDependencyLink(rv.newChildId(), wbsNode);
    rv.add(newDependencyLink);
  }

  @Override
  protected String getTitle()
  {
    return getString("plugins.chimney.dependencytree.title");
  }

  @Override
  protected void insertBreadcrumbItems(final List<String> items)
  {
    items.add(BreadcrumbConstants.PROJECT_PLANNING);
    items.add(BreadcrumbConstants.DEPENDENCY_TREE);
  }
}
