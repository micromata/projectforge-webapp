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

package org.projectforge.web.gantt;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.PageParameters;
import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.projectforge.gantt.GanttChartDO;
import org.projectforge.gantt.GanttChartDao;
import org.projectforge.task.TaskTree;
import org.projectforge.web.task.TaskFormatter;
import org.projectforge.web.task.TaskPropertyColumn;
import org.projectforge.web.user.UserFormatter;
import org.projectforge.web.user.UserPropertyColumn;
import org.projectforge.web.wicket.AbstractListPage;
import org.projectforge.web.wicket.CellItemListener;
import org.projectforge.web.wicket.CellItemListenerPropertyColumn;
import org.projectforge.web.wicket.DetachableDOModel;
import org.projectforge.web.wicket.ListPage;
import org.projectforge.web.wicket.ListSelectActionPanel;

@ListPage(editPage = GanttChartEditPage.class)
public class GanttChartListPage extends AbstractListPage<GanttChartListForm, GanttChartDao, GanttChartDO>
{
  private static final long serialVersionUID = 671935723386728113L;

  @SpringBean(name = "ganttChartDao")
  private GanttChartDao ganttChartDao;

  @SpringBean(name = "taskFormatter")
  private TaskFormatter taskFormatter;

  @SpringBean(name = "taskTree")
  private TaskTree taskTree;

  @SpringBean(name = "userFormatter")
  private UserFormatter userFormatter;

  public GanttChartListPage(PageParameters parameters)
  {
    super(parameters, "gantt");
  }

  @SuppressWarnings("serial")
  @Override
  protected void init()
  {
    final List<IColumn<GanttChartDO>> columns = new ArrayList<IColumn<GanttChartDO>>();

    final CellItemListener<GanttChartDO> cellItemListener = new CellItemListener<GanttChartDO>() {
      public void populateItem(Item<ICellPopulator<GanttChartDO>> item, String componentId, IModel<GanttChartDO> rowModel)
      {
        final GanttChartDO ganttChart = rowModel.getObject();
        final StringBuffer cssStyle = getCssStyle(ganttChart.getId(), ganttChart.isDeleted());
        if (cssStyle.length() > 0) {
          item.add(new AttributeModifier("style", true, new Model<String>(cssStyle.toString())));
        }
      }
    };
    columns.add(new CellItemListenerPropertyColumn<GanttChartDO>(new Model<String>(getString("gantt.name")), "name", "number",
        cellItemListener) {
      @SuppressWarnings("unchecked")
      @Override
      public void populateItem(final Item item, final String componentId, final IModel rowModel)
      {
        final GanttChartDO ganttChart = (GanttChartDO) rowModel.getObject();
        item.add(new ListSelectActionPanel(componentId, rowModel, GanttChartEditPage.class, ganttChart.getId(), GanttChartListPage.this,
            ganttChart.getName()));
        cellItemListener.populateItem(item, componentId, rowModel);
        addRowClick(item);
      }
    });
    columns.add(new CellItemListenerPropertyColumn<GanttChartDO>(new Model<String>(getString("created")), "created", "created",
        cellItemListener));
    columns.add(new UserPropertyColumn<GanttChartDO>(getString("gantt.owner"), "user.fullname", "owner", cellItemListener)
        .withUserFormatter(userFormatter));
    columns.add(new TaskPropertyColumn<GanttChartDO>(this, getString("task"), "task.title", "task", cellItemListener).withTaskFormatter(
        taskFormatter).withTaskTree(taskTree));
    dataTable = createDataTable(columns, "name", false);
    form.add(dataTable);
  }

  @Override
  protected GanttChartListForm newListForm(AbstractListPage< ? , ? , ? > parentPage)
  {
    return new GanttChartListForm(this);
  }

  @Override
  protected GanttChartDao getBaseDao()
  {
    return ganttChartDao;
  }

  @Override
  protected IModel<GanttChartDO> getModel(GanttChartDO object)
  {
    return new DetachableDOModel<GanttChartDO, GanttChartDao>(object, getBaseDao());
  }
}
