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
 * The controller of the list page. Most functionality such as search etc. is done by the super class.
 * @author Werner Feder (werner.feder@t-online.de)
 */
@ListPage(editPage = AttendeeEditPage.class)
public class AttendeeListPage extends AbstractListPage<AttendeeListForm, AttendeeDao, AttendeeDO> implements
IListPageColumnsCreator<AttendeeDO>
{

  private static final long serialVersionUID = 685671613717879800L;

  public static final String I18N_KEY_PREFIX = "plugins.skillmatrix";

  @SpringBean(name = "attendeeDao")
  private AttendeeDao attendeeDao;

  public AttendeeListPage(final PageParameters parameters)
  {
    super(parameters, I18N_KEY_PREFIX);
  }

  /**
   * @see org.projectforge.web.wicket.IListPageColumnsCreator#createColumns(org.apache.wicket.markup.html.WebPage, boolean)
   */
  @SuppressWarnings("serial")
  @Override
  public List<IColumn<AttendeeDO, String>> createColumns(final WebPage returnToPage, final boolean sortable)
  {
    final List<IColumn<AttendeeDO, String>> columns = new ArrayList<IColumn<AttendeeDO, String>>();
    final CellItemListener<AttendeeDO> cellItemListener = new CellItemListener<AttendeeDO>() {
      public void populateItem(final Item<ICellPopulator<AttendeeDO>> item, final String componentId, final IModel<AttendeeDO> rowModel)
      {
        final AttendeeDO attendeeDO = rowModel.getObject();
        appendCssClasses(item, attendeeDO.getId(), attendeeDO.isDeleted());
      }
    };

    columns.add(new CellItemListenerPropertyColumn<AttendeeDO>(getString("plugins.skillmatrix.skillrating.skill"),
        getSortable("training.skill.title", sortable), "training.skill.title", cellItemListener));
    columns.add(new CellItemListenerPropertyColumn<AttendeeDO>(getString("plugins.skillmatrix.skilltraining.training"),
        getSortable("training.title", sortable), "training.title", cellItemListener));

    columns.add(new CellItemListenerPropertyColumn<AttendeeDO>(getString("plugins.skillmatrix.skilltraining.startDate"),
        getSortable("training.startDate", sortable), "training.startDate",
        cellItemListener));
    columns.add(new CellItemListenerPropertyColumn<AttendeeDO>(getString("plugins.skillmatrix.skilltraining.endDate"),
        getSortable("training.endDate", sortable), "training.endDate",
        cellItemListener));

    columns.add(new CellItemListenerPropertyColumn<AttendeeDO>(getString("plugins.skillmatrix.skilltraining.firstname"),
        getSortable("attendee.firstname", sortable), "attendee.firstname", cellItemListener));
    columns.add(new CellItemListenerPropertyColumn<AttendeeDO>(getString("plugins.skillmatrix.skilltraining.lastname"),
        getSortable("attendee.lastname", sortable), "attendee.lastname", cellItemListener));

    columns.add(new CellItemListenerPropertyColumn<AttendeeDO>(AttendeeDO.class, getSortable("rating", sortable), "rating",
        cellItemListener));
    columns.add(new CellItemListenerPropertyColumn<AttendeeDO>(AttendeeDO.class, getSortable("certificate", sortable), "certificate",
        cellItemListener));
    columns.add(new CellItemListenerPropertyColumn<AttendeeDO>(AttendeeDO.class, getSortable("description", sortable), "description",
        cellItemListener));
    columns.add(new CellItemListenerPropertyColumn<AttendeeDO>(AttendeeDO.class, getSortable("created", sortable), "created",
        cellItemListener) {
      @SuppressWarnings({ "unchecked", "rawtypes"})
      @Override
      public void populateItem(final Item item, final String componentId, final IModel rowModel)
      {
        final AttendeeDO attendeeDO = (AttendeeDO) rowModel.getObject();
        item.add(new ListSelectActionPanel(componentId, rowModel, AttendeeEditPage.class, attendeeDO.getId(), returnToPage,
            DateTimeFormatter.instance().getFormattedDateTime(attendeeDO.getCreated())));
        addRowClick(item);
        cellItemListener.populateItem(item, componentId, rowModel);
      }
    });

    columns.add(new CellItemListenerPropertyColumn<AttendeeDO>(AttendeeDO.class, getSortable("lastUpdate", sortable), "lastUpdate",
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
  protected AttendeeDao getBaseDao()
  {
    return attendeeDao;
  }

  /**
   * @see org.projectforge.web.wicket.AbstractListPage#newListForm(org.projectforge.web.wicket.AbstractListPage)
   */
  @Override
  protected AttendeeListForm newListForm(final AbstractListPage< ? , ? , ? > parentPage)
  {
    return new AttendeeListForm(this);
  }

}
