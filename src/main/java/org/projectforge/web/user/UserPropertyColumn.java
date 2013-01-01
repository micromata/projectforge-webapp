/////////////////////////////////////////////////////////////////////////////
//
// Project ProjectForge Community Edition
//         www.projectforge.org
//
// Copyright (C) 2001-2013 Kai Reinhard (k.reinhard@micromata.de)
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

package org.projectforge.web.user;

import org.apache.commons.lang.Validate;
import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.projectforge.common.BeanHelper;
import org.projectforge.user.PFUserDO;
import org.projectforge.user.UserGroupCache;
import org.projectforge.web.wicket.CellItemListener;
import org.projectforge.web.wicket.CellItemListenerPropertyColumn;


public class UserPropertyColumn<T> extends CellItemListenerPropertyColumn<T>
{
  private static final long serialVersionUID = -26352961662061891L;

  private UserFormatter userFormatter;
  
  private UserGroupCache userGroupCache;

  /**
   * @param userFormatter
   * @param label
   * @param sortProperty
   * @param property Should be from type PFUserDO or Integer for user id.
   * @param cellItemListener
   */
  public UserPropertyColumn(final String label, final String sortProperty, final String property,
      final CellItemListener<T> cellItemListener)
  {
    super(new Model<String>(label), sortProperty, property, cellItemListener);
  }

  /**
   * @param userFormatter
   * @param label
   * @param sortProperty
   * @param property Should be from type PFUserDO or Integer for user id.
   */
  public UserPropertyColumn(final String label, final String sortProperty, final String property)
  {
    this(label, sortProperty, property, null);
  }

  @Override
  public void populateItem(final Item<ICellPopulator<T>> item, final String componentId, final IModel<T> rowModel)
  {
    final Label label = new Label(componentId, new Model<String>(getLabelString(rowModel)));
    item.add(label);
    if (cellItemListener != null)
      cellItemListener.populateItem(item, componentId, rowModel);
  }
  
  protected String getLabelString(final IModel<T> rowModel) {
    Object obj = BeanHelper.getNestedProperty(rowModel.getObject(), getPropertyExpression());
    PFUserDO user = null;
    if (obj != null) {
      if (obj instanceof PFUserDO) {
        user = (PFUserDO) obj;
      } else if (obj instanceof Integer) {
        Validate.notNull(userGroupCache);
        Integer userId = (Integer) obj;
        user = userGroupCache.getUser(userId);
      } else {
        throw new IllegalStateException("Unsupported column type: " + obj);
      }
    }
    String result;
    if (user != null) {
      Validate.notNull(userFormatter);
      result = userFormatter.formatUser(user);
    } else {
      result = "";
    }
    return result;
  }
  
  /**
   * Fluent pattern
   * @param userFormatter
   */
  public UserPropertyColumn<T> withUserFormatter(UserFormatter userFormatter)
  {
    this.userFormatter = userFormatter;
    return this;
  }

  /**
   * Fluent pattern
   * @param userFormatter
   */
  public UserPropertyColumn<T> setUserGroupCache(UserGroupCache userGroupCache)
  {
    this.userGroupCache = userGroupCache;
    return this;
  }
}
