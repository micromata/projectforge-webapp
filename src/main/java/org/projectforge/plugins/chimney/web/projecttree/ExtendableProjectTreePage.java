/////////////////////////////////////////////////////////////////////////////
//
// Project   ProjectForge - Chimney Plugin
//
// Copyright 2012, Micromata GmbH
//           All rights reserved.
//
/////////////////////////////////////////////////////////////////////////////

package org.projectforge.plugins.chimney.web.projecttree;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.extensions.markup.html.repeater.data.table.AbstractColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.projectforge.plugins.chimney.wbs.AbstractWbsNodeDO;

/**
 * Another version of ProjectTreePage that allows to create different tree pages
 * by adding a custom column.
 * @author Sweeps <pf@byte-storm.com>
 */
public abstract class ExtendableProjectTreePage extends ProjectTreePage
{
  private static final long serialVersionUID = -6920718046967351841L;

  public ExtendableProjectTreePage(final Integer... projectIds)
  {
    super(projectIds);
  }

  public ExtendableProjectTreePage(final PageParameters parameters)
  {
    super(parameters);
  }

  @Override
  protected List<IColumn<AbstractWbsNodeDO, String>> createTableColumns(final ProjectTreeStateModel treeStateModel) {
    final List<IColumn<AbstractWbsNodeDO, String>> columns = new ArrayList<IColumn<AbstractWbsNodeDO, String>>(4);

    columns.add(getWbsCodeColumn());
    columns.add(getTreeColumn(treeStateModel));
    final IColumn<AbstractWbsNodeDO, String> customColumn = getCustomColumn();
    if (customColumn != null)
      columns.add(getCustomColumn());
    columns.add(
        new AbstractColumn<AbstractWbsNodeDO, String>(Model.of(getString("plugins.chimney.projectlist.headactions"))) {
          private static final long serialVersionUID = 1L;
          @Override
          public void populateItem(final Item<ICellPopulator<AbstractWbsNodeDO>> cellItem, final String componentId, final IModel<AbstractWbsNodeDO> rowModel)
          {
            cellItem.add(createActionLinksFor(componentId, rowModel.getObject()));
          }
        }
        );

    return columns;
  }

  private Component createActionLinksFor(final String wicketid, final AbstractWbsNodeDO wbsNode) {
    final Fragment fragment = new Fragment(wicketid, "containerFragment", body);
    final RepeatingView rv = new RepeatingView("container");

    addActionLinks(rv, wbsNode);

    fragment.add(rv);
    return fragment;
  }

  /**
   * @return A custom column for iterating over AbstractWbsNodeDOs.
   */
  protected abstract IColumn<AbstractWbsNodeDO, String> getCustomColumn();

  /**
   * Use the passed RepeatingView to add your own action links for the given wbs node
   * @param rv The RepeatingView
   * @param wbsNode The current wbs node
   */
  protected abstract void addActionLinks(RepeatingView rv, final AbstractWbsNodeDO wbsNode);
}
