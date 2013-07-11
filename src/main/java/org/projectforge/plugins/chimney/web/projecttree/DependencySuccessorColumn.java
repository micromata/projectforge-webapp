/////////////////////////////////////////////////////////////////////////////
//
// Project   ProjectForge - Chimney Plugin
//
// Copyright 2012, Micromata GmbH
//           All rights reserved.
//
/////////////////////////////////////////////////////////////////////////////

package org.projectforge.plugins.chimney.web.projecttree;

import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.extensions.markup.html.repeater.data.table.AbstractColumn;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;
import org.projectforge.plugins.chimney.activities.DependencyRelationDao;
import org.projectforge.plugins.chimney.activities.WbsActivityDao;
import org.projectforge.plugins.chimney.wbs.AbstractWbsNodeDO;

public class DependencySuccessorColumn extends AbstractColumn<AbstractWbsNodeDO, String>
{
  private static final long serialVersionUID = -7500495916889692115L;

  public DependencySuccessorColumn(final IModel<String> displayModel, final DependencyRelationDao dependencyDao, final WbsActivityDao activityDao)
  {
    super(displayModel);
  }

  @Override
  public void populateItem(final Item<ICellPopulator<AbstractWbsNodeDO>> cellItem, final String componentId, final IModel<AbstractWbsNodeDO> rowModel)
  {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException();
  }

}
