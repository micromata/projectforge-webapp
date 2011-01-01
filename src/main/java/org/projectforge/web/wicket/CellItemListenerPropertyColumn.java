/////////////////////////////////////////////////////////////////////////////
//
// Project ProjectForge Community Edition
//         www.projectforge.org
//
// Copyright (C) 2001-2011 Kai Reinhard (k.reinhard@me.com)
//
// ProjectForge is dual-licensed.
//
// This community edition is free software; you can redistribute it and/or
// modify it under the terms of the GNU General Public License as published
// by the Free Software Foundation; version 3 of the License.
//
// This community edition is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
// Public License for more details.
//
// You should have received a copy of the GNU General Public License along
// with this program; if not, see http://www.gnu.org/licenses/.
//
/////////////////////////////////////////////////////////////////////////////

package org.projectforge.web.wicket;

import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.extensions.markup.html.repeater.data.table.PropertyColumn;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

/**
 * Supports CellItemListener.
 * @author Kai Reinhard (k.reinhard@micromata.de)
 *
 */
@SuppressWarnings("serial")
public class CellItemListenerPropertyColumn<T> extends PropertyColumn<T>
{
  protected CellItemListener<T> cellItemListener;
  
  /**
   * @param displayModelString For creation of new Model<String>.
   * @param sortProperty
   * @param propertyExpression
   * @param cellItemListener
   */
  public CellItemListenerPropertyColumn(final String displayModelString, final String sortProperty, final String propertyExpression,
      final CellItemListener<T> cellItemListener)
  {
    super(new Model<String>(displayModelString), sortProperty, propertyExpression);
    this.cellItemListener = cellItemListener;
  }

  /**
   * @param displayModelString
   * @param sortProperty
   * @param propertyExpression
   * @see #CellItemListenerPropertyColumn(String, String, String, CellItemListener)
   */
  public CellItemListenerPropertyColumn(final String displayModelString, final String sortProperty, final String propertyExpression)
  {
    this(displayModelString, sortProperty, propertyExpression, null);
  }

  public CellItemListenerPropertyColumn(final IModel<String> displayModel, final String sortProperty, final String propertyExpression,
      final CellItemListener<T> cellItemListener)
  {
    super(displayModel, sortProperty, propertyExpression);
    this.cellItemListener = cellItemListener;
  }

  public CellItemListenerPropertyColumn(final IModel<String> displayModel, final String sortProperty, final String propertyExpression)
  {
    this(displayModel, sortProperty, propertyExpression, null);
  }

  /**
   * Call CellItemListener.
   * @see org.apache.wicket.extensions.markup.html.repeater.data.table.PropertyColumn#populateItem(org.apache.wicket.markup.repeater.Item, java.lang.String, org.apache.wicket.model.IModel)
   * @see CellItemListener#populateItem(Item, String, IModel)
   */
  @Override
  public void populateItem(final Item<ICellPopulator<T>> item, final String componentId, final IModel<T> rowModel)
  {
    super.populateItem(item, componentId, rowModel);
    if (cellItemListener != null)
      cellItemListener.populateItem(item, componentId, rowModel);
  }
}
