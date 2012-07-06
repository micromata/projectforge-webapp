/////////////////////////////////////////////////////////////////////////////
//
// Project   ProjectForge
//
// Copyright 2001-2009, Micromata GmbH, Kai Reinhard
//           All rights reserved.
//
/////////////////////////////////////////////////////////////////////////////

package org.projectforge.plugins.skillmatrix;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.extensions.markup.html.repeater.data.sort.SortOrder;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.projectforge.web.wicket.AbstractListPage;
import org.projectforge.web.wicket.CellItemListener;
import org.projectforge.web.wicket.CellItemListenerPropertyColumn;
import org.projectforge.web.wicket.DetachableDOModel;
import org.projectforge.web.wicket.IListPageColumnsCreator;
import org.projectforge.web.wicket.ListPage;

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

  // @SpringBean(name = "userFormatter")
  // private UserFormatter userFormatter;

  public SkillListPage(final PageParameters parameters)
  {
    super(parameters, "plugins.skillmatrix");
  }

  /**
   * @see org.projectforge.web.wicket.IListPageColumnsCreator#createColumns(org.apache.wicket.markup.html.WebPage, boolean)
   */
  @Override
  public List<IColumn<SkillDO>> createColumns(final WebPage returnToPage, final boolean sortable)
  {
    final List<IColumn<SkillDO>> columns = new ArrayList<IColumn<SkillDO>>();
    final CellItemListener<SkillDO> cellItemListener = new CellItemListener<SkillDO>() {
      private static final long serialVersionUID = 3628573642359696336L;

      public void populateItem(final Item<ICellPopulator<SkillDO>> item, final String componentId, final IModel<SkillDO> rowModel)
      {
        final SkillDO skill = rowModel.getObject();
        final StringBuffer cssStyle = getCssStyle(skill.getId(), skill.isDeleted());
        if (cssStyle.length() > 0) {
          item.add(AttributeModifier.append("style", new Model<String>(cssStyle.toString())));
        }
      }
    };

    // modified
    columns.add(new CellItemListenerPropertyColumn<SkillDO>(getString("modified"), getSortable("lastUpdate", sortable), "lastUpdate",
        cellItemListener));
    // Title
    columns.add(new CellItemListenerPropertyColumn<SkillDO>(new Model<String>(getString("plugins.skillmatrix.skill.title")), getSortable(
        "title", sortable), "title", cellItemListener));
    // TODO Should only show title of the DO, title -> DO Map? title -> primary/unique key?
    // Parent
    columns.add(new CellItemListenerPropertyColumn<SkillDO>(new Model<String>(getString("plugins.skillmatrix.skill.parent")), getSortable(
        "parent", sortable), "parent", cellItemListener));
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
    // final BookmarkablePageLink<SkillEditPage> addSkillLink = new BookmarkablePageLink<SkillEditPage>("link", SkillEditPage.class);
    // final ContentMenuEntryPanel menuEntry = new ContentMenuEntryPanel(getNewContentMenuChildId(), addSkillLink, getString("templates"));
    // addContentMenuEntry(menuEntry);
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

  /**
   * @see org.projectforge.web.wicket.AbstractListPage#getModel(java.lang.Object)
   */
  @Override
  protected IModel<SkillDO> getModel(final SkillDO object)
  {
    return new DetachableDOModel<SkillDO, SkillDao>(object, getBaseDao());
  }

}
