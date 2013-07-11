/////////////////////////////////////////////////////////////////////////////
//
// Project   ProjectForge - Chimney Plugin
//
// Copyright 2012, Micromata GmbH
//           All rights reserved.
//
/////////////////////////////////////////////////////////////////////////////

package org.projectforge.plugins.chimney.web.resourceplanning;

import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.extensions.markup.html.repeater.data.table.AbstractColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.projectforge.plugins.chimney.resourceplanning.ResourceAssignmentDO;
import org.projectforge.plugins.chimney.resourceplanning.ResourceAssignmentDao;
import org.projectforge.plugins.chimney.wbs.AbstractWbsNodeDO;
import org.projectforge.plugins.chimney.web.navigation.BreadcrumbConstants;
import org.projectforge.plugins.chimney.web.projecttree.ExtendableProjectTreePage;

/**
 * Tree page that displays one or more projects in a tree and each nodes
 * associated resource assignments.
 * @author Sweeps <pf@byte-storm.com>
 */
public class ResourceAssignmentTreePage extends ExtendableProjectTreePage
{
  private static final long serialVersionUID = 2340760474451446636L;
  public static final String PAGE_ID = "resourceAssignmentTreePage";

  @SpringBean(name="resourceAssignmentDao")
  private ResourceAssignmentDao raDao;

  public ResourceAssignmentTreePage(final Integer... projectIds)
  {
    super(projectIds);
  }

  public ResourceAssignmentTreePage(final PageParameters parameters)
  {
    super(parameters);
  }

  @Override
  protected IColumn<AbstractWbsNodeDO, String> getCustomColumn()
  {
    return new AbstractColumn<AbstractWbsNodeDO, String>(Model.of(getString("plugins.chimney.resourceassignmenttree.headplannedresources"))) {
      private static final long serialVersionUID = 1L;
      @Override
      public void populateItem(final Item<ICellPopulator<AbstractWbsNodeDO>> cellItem, final String componentId, final IModel<AbstractWbsNodeDO> rowModel)
      {
        final List<ResourceAssignmentDO> resAssignments = raDao.getListByWbsNode(rowModel.getObject());

        cellItem.add(new ResourceAssignmentTablePanel(componentId, resAssignments) {
          private static final long serialVersionUID = 1L;

          @Override
          public void deleteRessourceAssignment(final int resourceAssignmentId)
          {
            final ResourceAssignmentDO ra = raDao.getById(resourceAssignmentId);
            if (ra == null) {
              ResourceAssignmentTreePage.this.error(getString("plugins.chimney.errors.resourceassignmentnotfound"));
            } else {
              raDao.markAsDeleted(ra);
              ResourceAssignmentTreePage.this.info(getString("plugins.chimney.resourceassignmenttree.deleted")+" "+ra.toString());
            }
          }
        });
      }
    };
  }

  @Override
  protected void addActionLinks(final RepeatingView rv, final AbstractWbsNodeDO wbsNode)
  {
    final Component newResourceAssignmentLink = createNewResourceAssignmentLink(rv.newChildId(), wbsNode);
    rv.add(newResourceAssignmentLink);
  }

  @Override
  protected String getTitle()
  {
    return getString("plugins.chimney.resourceassignmenttree.title");
  }

  @Override
  protected void insertBreadcrumbItems(final List<String> items)
  {
    items.add(BreadcrumbConstants.PROJECT_PLANNING);
    items.add(BreadcrumbConstants.DEPENDENCY_TREE);
  }
}
