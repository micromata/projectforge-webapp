/////////////////////////////////////////////////////////////////////////////
//
// Project ProjectForge Community Edition
//         www.projectforge.org
//
// Copyright (C) 2001-2012 Kai Reinhard (k.reinhard@micromata.com)
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

package org.projectforge.web.task;

import org.apache.commons.lang.Validate;
import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.hibernate.Hibernate;
import org.projectforge.common.BeanHelper;
import org.projectforge.task.TaskDO;
import org.projectforge.task.TaskTree;
import org.projectforge.web.wicket.CellItemListener;
import org.projectforge.web.wicket.CellItemListenerPropertyColumn;
import org.projectforge.web.wicket.WicketLocalizerAndUrlBuilder;

public class TaskPropertyColumn<T> extends CellItemListenerPropertyColumn<T>
{
  private static final long serialVersionUID = -26352961662061891L;

  private TaskFormatter taskFormatter;

  private TaskTree taskTree;

  private WebPage parentPage;

  /**
   * @param taskFormatter
   * @param label
   * @param sortProperty
   * @param property Should be from type TaskDO or Integer for task id.
   * @param cellItemListener
   */
  public TaskPropertyColumn(final WebPage parentPage, final String label, final String sortProperty, final String property,
      final CellItemListener<T> cellItemListener)
  {
    super(new Model<String>(label), sortProperty, property, cellItemListener);
    this.parentPage = parentPage;
  }

  /**
   * @param taskFormatter
   * @param label
   * @param sortProperty
   * @param property Should be from type TaskDO or Integer for task id.
   */
  public TaskPropertyColumn(final WebPage parentPage, final String label, final String sortProperty, final String property)
  {
    this(parentPage, label, sortProperty, property, null);
  }

  @Override
  public void populateItem(final Item<ICellPopulator<T>> item, final String componentId, final IModel<T> rowModel)
  {
    final Label label = new Label(componentId, new Model<String>(getLabelString(rowModel)));
    label.setEscapeModelStrings(false);
    item.add(label);
    if (cellItemListener != null)
      cellItemListener.populateItem(item, componentId, rowModel);
  }

  protected String getLabelString(final IModel<T> rowModel)
  {
    final Object obj = BeanHelper.getNestedProperty(rowModel.getObject(), getPropertyExpression());
    TaskDO task = null;
    if (obj != null) {
      if (obj instanceof TaskDO) {
        task = (TaskDO) obj;
        if (Hibernate.isInitialized(task) == false) {
          if (taskTree != null) {
            task = taskTree.getTaskById(task.getId());
          } else {
            Hibernate.initialize(task);
          }
        }
      } else if (obj instanceof Integer) {
        Validate.notNull(taskTree);
        final Integer taskId = (Integer) obj;
        task = taskTree.getTaskById(taskId);
      } else {
        throw new IllegalStateException("Unsupported column type: " + obj);
      }
    }
    String result;
    if (task != null) {
      Validate.notNull(taskFormatter);
      final StringBuffer buf = new StringBuffer();
      taskFormatter.appendFormattedTask(buf, new WicketLocalizerAndUrlBuilder(parentPage.getResponse()), task, false, true, false);
      result = buf.toString();
    } else {
      result = "";
    }
    return result;
  }

  /**
   * Fluent pattern
   * @param taskFormatter
   */
  public TaskPropertyColumn<T> withTaskFormatter(final TaskFormatter taskFormatter)
  {
    this.taskFormatter = taskFormatter;
    return this;
  }

  /**
   * Fluent pattern
   * @param taskFormatter
   */
  public TaskPropertyColumn<T> withTaskTree(final TaskTree taskTree)
  {
    this.taskTree = taskTree;
    return this;
  }
}
