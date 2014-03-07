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

import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.extensions.markup.html.repeater.data.sort.SortOrder;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;
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
 * @author Werner Feder (werner.feder@t-online.de)
 * 
 */
@ListPage(editPage = InviteeEditPage.class)
public class InviteeListPage extends AbstractListPage<InviteeListForm, InviteeDao, InviteeDO> implements
IListPageColumnsCreator<InviteeDO>
{

  private static final long serialVersionUID = 685671613717879800L;

  public static final String I18N_KEY_PREFIX = "plugins.skillmatrix";

  @SpringBean(name = "inviteeDao")
  private InviteeDao inviteeDao;

  public InviteeListPage(final PageParameters parameters)
  {
    super(parameters, I18N_KEY_PREFIX);
  }

  /**
   * @see org.projectforge.web.wicket.IListPageColumnsCreator#createColumns(org.apache.wicket.markup.html.WebPage, boolean)
   */

  @SuppressWarnings("serial")
  @Override
  public List<IColumn<InviteeDO, String>> createColumns(final WebPage returnToPage, final boolean sortable)
  {
    final List<IColumn<InviteeDO, String>> columns = new ArrayList<IColumn<InviteeDO, String>>();
    final CellItemListener<InviteeDO> cellItemListener = new CellItemListener<InviteeDO>() {
      public void populateItem(final Item<ICellPopulator<InviteeDO>> item, final String componentId, final IModel<InviteeDO> rowModel)
      {
        final InviteeDO inviteeDO = rowModel.getObject();
        appendCssClasses(item, inviteeDO.getId(), inviteeDO.isDeleted());
      }
    };

    columns.add(new CellItemListenerPropertyColumn<InviteeDO>(getString("plugins.skillmatrix.skillrating.skill"),
        getSortable("training.skill.title", sortable), "training.skill.title", cellItemListener));
    columns.add(new CellItemListenerPropertyColumn<InviteeDO>(getString("plugins.skillmatrix.skilltraining.menu"),
        getSortable("training.title", sortable), "training.title", cellItemListener));

    columns.add(new CellItemListenerPropertyColumn<InviteeDO>(getString("plugins.skillmatrix.skilltraining.startDate"),
        getSortable("training.startDate", sortable), "training.startDate",
        cellItemListener));
    columns.add(new CellItemListenerPropertyColumn<InviteeDO>(getString("plugins.skillmatrix.skilltraining.endDate"),
        getSortable("training.endDate", sortable), "training.endDate",
        cellItemListener));

    columns.add(new CellItemListenerPropertyColumn<InviteeDO>(getString("plugins.skillmatrix.skilltraining.firstname"),
        getSortable("person.firstname", sortable), "person.firstname", cellItemListener));
    columns.add(new CellItemListenerPropertyColumn<InviteeDO>(getString("plugins.skillmatrix.skilltraining.lastname"),
        getSortable("person.lastname", sortable), "person.lastname", cellItemListener));

    columns.add(new CellItemListenerPropertyColumn<InviteeDO>(InviteeDO.class, getSortable("rating", sortable), "rating",
        cellItemListener));
    columns.add(new CellItemListenerPropertyColumn<InviteeDO>(InviteeDO.class, getSortable("certificate", sortable), "certificate",
        cellItemListener));
    columns.add(new CellItemListenerPropertyColumn<InviteeDO>(InviteeDO.class, getSortable("description", sortable), "description",
        cellItemListener));
    columns.add(new CellItemListenerPropertyColumn<InviteeDO>(InviteeDO.class, getSortable("successfully",
        sortable), "successfully", cellItemListener));
    columns.add(new CellItemListenerPropertyColumn<InviteeDO>(InviteeDO.class, getSortable("created", sortable), "created",
        cellItemListener) {
      @SuppressWarnings({ "unchecked", "rawtypes"})
      @Override
      public void populateItem(final Item item, final String componentId, final IModel rowModel)
      {
        final InviteeDO inviteeDO = (InviteeDO) rowModel.getObject();
        item.add(new ListSelectActionPanel(componentId, rowModel, InviteeEditPage.class, inviteeDO.getId(), returnToPage,
            DateTimeFormatter.instance().getFormattedDateTime(inviteeDO.getCreated())));
        addRowClick(item);
        cellItemListener.populateItem(item, componentId, rowModel);
      }
    });

    columns.add(new CellItemListenerPropertyColumn<InviteeDO>(InviteeDO.class, getSortable("lastUpdate", sortable), "lastUpdate",
        cellItemListener));

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
  protected InviteeDao getBaseDao()
  {
    return inviteeDao;
  }

  /**
   * @see org.projectforge.web.wicket.AbstractListPage#newListForm(org.projectforge.web.wicket.AbstractListPage)
   */
  @Override
  protected InviteeListForm newListForm(final AbstractListPage< ? , ? , ? > parentPage)
  {
    return new InviteeListForm(this);
  }

}
