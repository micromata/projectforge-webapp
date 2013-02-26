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

package org.projectforge.plugins.skillmatrix;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.extensions.markup.html.repeater.data.sort.SortOrder;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.projectforge.web.calendar.DateTimeFormatter;
import org.projectforge.web.wicket.AbstractListPage;
import org.projectforge.web.wicket.CellItemListener;
import org.projectforge.web.wicket.CellItemListenerPropertyColumn;
import org.projectforge.web.wicket.IListPageColumnsCreator;
import org.projectforge.web.wicket.ListPage;
import org.projectforge.web.wicket.ListSelectActionPanel;

/**
 * @author Billy Duong (duong.billy@yahoo.de)
 * 
 */
@ListPage(editPage = SkillEditPage.class)
public class SkillListPage extends AbstractListPage<SkillListForm, SkillDao, SkillDO> implements IListPageColumnsCreator<SkillDO>
{
  private static final long serialVersionUID = 3262800972072452074L;

  @SpringBean(name = "skillDao")
  private SkillDao skillDao;

  public SkillListPage(final PageParameters parameters)
  {
    super(parameters, "plugins.skillmatrix");
  }

  /**
   * @see org.projectforge.web.wicket.IListPageColumnsCreator#createColumns(org.apache.wicket.markup.html.WebPage, boolean)
   */
  @SuppressWarnings("serial")
  @Override
  public List<IColumn<SkillDO, String>> createColumns(final WebPage returnToPage, final boolean sortable)
  {
    final List<IColumn<SkillDO, String>> columns = new ArrayList<IColumn<SkillDO, String>>();
    final CellItemListener<SkillDO> cellItemListener = new CellItemListener<SkillDO>() {
      private static final long serialVersionUID = 3628573642359696336L;

      public void populateItem(final Item<ICellPopulator<SkillDO>> item, final String componentId, final IModel<SkillDO> rowModel)
      {
        final SkillDO skill = rowModel.getObject();
        appendCssClasses(item, skill.getId(), skill.isDeleted());
      }
    };

    // Created
    columns.add(new CellItemListenerPropertyColumn<SkillDO>(new Model<String>(getString("created")), getSortable("created", sortable),
        "created", cellItemListener) {
      @SuppressWarnings({ "unchecked", "rawtypes"})
      @Override
      public void populateItem(final Item item, final String componentId, final IModel rowModel)
      {
        final SkillDO skill = (SkillDO) rowModel.getObject();
        item.add(new ListSelectActionPanel(componentId, rowModel, SkillEditPage.class, skill.getId(), returnToPage, DateTimeFormatter
            .instance().getFormattedDateTime(skill.getCreated())));
        addRowClick(item);
        cellItemListener.populateItem(item, componentId, rowModel);
      }
    });
    // Modified
    columns.add(new CellItemListenerPropertyColumn<SkillDO>(getString("modified"), getSortable("lastUpdate", sortable), "lastUpdate",
        cellItemListener));
    // Title
    columns.add(new CellItemListenerPropertyColumn<SkillDO>(new Model<String>(getString("plugins.skillmatrix.skill.title")), getSortable(
        "title", sortable), "title", cellItemListener));
    // Parent -> Title
    columns.add(new CellItemListenerPropertyColumn<SkillDO>(new Model<String>(getString("plugins.skillmatrix.skill.parent")), getSortable(
        "parent.title", sortable), "parent.title", cellItemListener));
    // Description
    columns.add(new CellItemListenerPropertyColumn<SkillDO>(new Model<String>(getString("plugins.skillmatrix.skill.description")),
        getSortable("description", sortable), "description", cellItemListener));
    // Comment
    columns.add(new CellItemListenerPropertyColumn<SkillDO>(new Model<String>(getString("plugins.skillmatrix.skill.comment")), getSortable(
        "comment", sortable), "comment", cellItemListener));
    // Rateable
    columns.add(new CellItemListenerPropertyColumn<SkillDO>(new Model<String>(getString("plugins.skillmatrix.skill.rateable")),
        getSortable("rateable", sortable), "rateable", cellItemListener));

    return columns;
  }

  /**
   * @see org.projectforge.web.wicket.AbstractListPage#init()
   */
  @Override
  protected void init()
  {
    dataTable = createDataTable(createColumns(this, true), "lastUpdate", SortOrder.DESCENDING);
    form.add(dataTable);
  }

  /**
   * @see org.projectforge.web.wicket.AbstractListPage#getBaseDao()
   */
  @Override
  protected SkillDao getBaseDao()
  {
    return skillDao;
  }

  /**
   * @see org.projectforge.web.wicket.AbstractListPage#newListForm(org.projectforge.web.wicket.AbstractListPage)
   */
  @Override
  protected SkillListForm newListForm(final AbstractListPage< ? , ? , ? > parentPage)
  {
    return new SkillListForm(this);
  }
}
