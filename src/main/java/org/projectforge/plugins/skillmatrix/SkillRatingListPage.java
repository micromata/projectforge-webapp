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

import org.apache.commons.lang.ObjectUtils;
import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.extensions.markup.html.repeater.data.sort.SortOrder;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.projectforge.user.PFUserContext;
import org.projectforge.web.calendar.DateTimeFormatter;
import org.projectforge.web.user.UserFormatter;
import org.projectforge.web.user.UserPropertyColumn;
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
@ListPage(editPage = SkillRatingEditPage.class)
public class SkillRatingListPage extends AbstractListPage<SkillRatingListForm, SkillRatingDao, SkillRatingDO> implements
IListPageColumnsCreator<SkillRatingDO>
{

  private static final long serialVersionUID = 3262800972072452074L;

  @SpringBean(name = "skillRatingDao")
  private SkillRatingDao skillRatingDao;

  @SpringBean(name = "userFormatter")
  private UserFormatter userFormatter;

  public SkillRatingListPage(final PageParameters parameters)
  {
    super(parameters, "plugins.skillmatrix");
  }

  /**
   * @see org.projectforge.web.wicket.IListPageColumnsCreator#createColumns(org.apache.wicket.markup.html.WebPage, boolean)
   */
  @SuppressWarnings("serial")
  @Override
  public List<IColumn<SkillRatingDO, String>> createColumns(final WebPage returnToPage, final boolean sortable)
  {
    final List<IColumn<SkillRatingDO, String>> columns = new ArrayList<IColumn<SkillRatingDO, String>>();
    final CellItemListener<SkillRatingDO> cellItemListener = new CellItemListener<SkillRatingDO>() {
      public void populateItem(final Item<ICellPopulator<SkillRatingDO>> item, final String componentId,
          final IModel<SkillRatingDO> rowModel)
      {
        final SkillRatingDO skillRating = rowModel.getObject();
        appendCssClasses(item, skillRating.getId(), skillRating.isDeleted());
      }
    };

    // Created
    columns.add(new CellItemListenerPropertyColumn<SkillRatingDO>(new Model<String>(getString("created")), getSortable("created", sortable),
        "created", cellItemListener) {
      @SuppressWarnings({ "unchecked", "rawtypes"})
      @Override
      public void populateItem(final Item item, final String componentId, final IModel rowModel)
      {
        final SkillRatingDO skillRating = (SkillRatingDO) rowModel.getObject();
        item.add(new ListSelectActionPanel(componentId, rowModel, SkillRatingEditPage.class, skillRating.getId(), returnToPage, DateTimeFormatter
            .instance().getFormattedDateTime(skillRating.getCreated())));
        // Only the owner can click / edit his entries
        if(ObjectUtils.equals(PFUserContext.getUserId(), skillRating.getUserId())) {
          addRowClick(item);
        }
        cellItemListener.populateItem(item, componentId, rowModel);
      }
    });
    // Modified
    columns.add(new CellItemListenerPropertyColumn<SkillRatingDO>(getString("modified"), getSortable("lastUpdate", sortable), "lastUpdate",
        cellItemListener));
    // User
    columns.add(new UserPropertyColumn<SkillRatingDO>(getString("plugins.skillmatrix.skillrating.user"), getSortable("userId", sortable),
        "user", cellItemListener).withUserFormatter(userFormatter));
    // Skill -> Title
    columns.add(new CellItemListenerPropertyColumn<SkillRatingDO>(getString("plugins.skillmatrix.skillrating.skill"), getSortable(
        "skill.title", sortable), "skill.title", cellItemListener));
    // Experience
    columns.add(new CellItemListenerPropertyColumn<SkillRatingDO>(
        new Model<String>(getString("plugins.skillmatrix.skillrating.rating")), getSortable("skillRating", sortable), "skillRating",
        cellItemListener));
    // Since year
    columns.add(new CellItemListenerPropertyColumn<SkillRatingDO>(
        new Model<String>(getString("plugins.skillmatrix.skillrating.sinceyear")), getSortable("sinceYear", sortable), "sinceYear",
        cellItemListener));
    // Certificates
    columns.add(new CellItemListenerPropertyColumn<SkillRatingDO>(
        new Model<String>(getString("plugins.skillmatrix.skillrating.certificates")), getSortable("certificates", sortable),
        "certificates", cellItemListener));
    // Training courses
    columns.add(new CellItemListenerPropertyColumn<SkillRatingDO>(new Model<String>(
        getString("plugins.skillmatrix.skillrating.trainingcourses")), getSortable("trainingCourses", sortable), "trainingCourses",
        cellItemListener));
    // Description
    columns.add(new CellItemListenerPropertyColumn<SkillRatingDO>(new Model<String>(
        getString("plugins.skillmatrix.skillrating.description")), getSortable("description", sortable), "description", cellItemListener));
    // Comment
    columns.add(new CellItemListenerPropertyColumn<SkillRatingDO>(new Model<String>(getString("plugins.skillmatrix.skillrating.comment")),
        getSortable("comment", sortable), "comment", cellItemListener));

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
  protected SkillRatingDao getBaseDao()
  {
    return skillRatingDao;
  }

  /**
   * @see org.projectforge.web.wicket.AbstractListPage#newListForm(org.projectforge.web.wicket.AbstractListPage)
   */
  @Override
  protected SkillRatingListForm newListForm(final AbstractListPage< ? , ? , ? > parentPage)
  {
    return new SkillRatingListForm(this);
  }
}
