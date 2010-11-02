/////////////////////////////////////////////////////////////////////////////
//
// Project ProjectForge Community Edition
//         www.projectforge.org
//
// Copyright (C) 2001-2010 Kai Reinhard (k.reinhard@me.com)
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

import java.math.BigDecimal;

import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.projectforge.common.BeanHelper;
import org.projectforge.core.CurrencyFormatter;


public class CurrencyPropertyColumn<T> extends CellItemListenerPropertyColumn<T>
{
  private static final long serialVersionUID = -26352961662061891L;

  private boolean suppressZeroValues;

  public CurrencyPropertyColumn(final String label, final String sortProperty, final String property, CellItemListener<T> cellItemListener)
  {
    super(new Model<String>(label), sortProperty, property, cellItemListener);
  }

  public CurrencyPropertyColumn(final String label, final String sortProperty, final String property)
  {
    this(label, sortProperty, property, null);
  }

  @Override
  public void populateItem(final Item<ICellPopulator<T>> item, final String componentId, final IModel<T> rowModel)
  {
    final BigDecimal value = (BigDecimal) BeanHelper.getProperty(rowModel.getObject(), getPropertyExpression());
    final Label label;
    if (this.suppressZeroValues == true && value != null && value.compareTo(BigDecimal.ZERO) == 0) {
      label = new Label(componentId, "");
    } else {
      label = new Label(componentId, CurrencyFormatter.format(value));
    }
    item.add(label);
    if (cellItemListener != null) {
      cellItemListener.populateItem(item, componentId, rowModel);
    }
    item.add(new AttributeAppendModifier("style", new Model<String>("white-space: nowrap; text-align: right;")));
  }
  
  public CurrencyPropertyColumn<T> setSuppressZeroValues(boolean supressZeroValues)
  {
    this.suppressZeroValues = supressZeroValues;
    return this;
  }
}
