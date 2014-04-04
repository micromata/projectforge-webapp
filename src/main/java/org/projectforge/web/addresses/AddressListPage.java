/////////////////////////////////////////////////////////////////////////////
//
// Project   ProjectForge
//
// Copyright 2001-2009, Micromata GmbH, Kai Reinhard
//           All rights reserved.
//
/////////////////////////////////////////////////////////////////////////////

package org.projectforge.web.addresses;

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
import org.projectforge.addresses.Address2DO;
import org.projectforge.addresses.Address2Dao;
import org.projectforge.web.wicket.AbstractListPage;
import org.projectforge.web.wicket.CellItemListener;
import org.projectforge.web.wicket.CellItemListenerPropertyColumn;
import org.projectforge.web.wicket.IListPageColumnsCreator;
import org.projectforge.web.wicket.ListPage;

/**
 * The controller of the list page. Most functionality such as search etc. is done by the super class.
 * @author Werner Feder (werner.feder@t-online.de)
 */
@ListPage(editPage = AddressEditPage.class)
public class AddressListPage extends AbstractListPage<AddressListForm, Address2Dao, Address2DO> implements
IListPageColumnsCreator<Address2DO>
{

  public static final String I18N_KEY_PREFIX = "address2";

  @SpringBean(name = "address2Dao")
  private Address2Dao address2Dao;

  public AddressListPage(final PageParameters parameters)
  {
    super(parameters, I18N_KEY_PREFIX);
  }

  /**
   * @see org.projectforge.web.wicket.IListPageColumnsCreator#createColumns(org.apache.wicket.markup.html.WebPage, boolean)
   */
  @SuppressWarnings("serial")
  @Override
  public List<IColumn<Address2DO, String>> createColumns(final WebPage returnToPage, final boolean sortable)
  {
    final List<IColumn<Address2DO, String>> columns = new ArrayList<IColumn<Address2DO, String>>();
    final CellItemListener<Address2DO> cellItemListener = new CellItemListener<Address2DO>() {
      public void populateItem(final Item<ICellPopulator<Address2DO>> item, final String componentId, final IModel<Address2DO> rowModel)
      {
        final Address2DO address2DO = rowModel.getObject();
        appendCssClasses(item, address2DO.getId(), address2DO.isDeleted());
      }
    };

    columns.add(new CellItemListenerPropertyColumn<Address2DO>(Address2DO.class, getSortable("name", sortable), "name",
        cellItemListener));
    columns.add(new CellItemListenerPropertyColumn<Address2DO>(Address2DO.class, getSortable("firstname", sortable), "firstname",
        cellItemListener));
    columns.add(new CellItemListenerPropertyColumn<Address2DO>(Address2DO.class, getSortable("title", sortable), "title", cellItemListener));
    columns.add(new CellItemListenerPropertyColumn<Address2DO>(Address2DO.class, getSortable("birthday", sortable), "birthday",
        cellItemListener));
    columns.add(new CellItemListenerPropertyColumn<Address2DO>(new Model<String>(getString("modified")),
        getSortable("lastUpdate", sortable), "lastUpdate", cellItemListener));

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
    //addExcelExport(getString("plugins.skillmatrix.skilltraining.attendee.menu"), getString("plugins.skillmatrix.skilltraining.attendee.menu"));
  }


  /**
   * @see org.projectforge.web.wicket.AbstractListPage#getBaseDao()
   */
  @Override
  protected Address2Dao getBaseDao()
  {
    return address2Dao;
  }

  /**
   * @see org.projectforge.web.wicket.AbstractListPage#newListForm(org.projectforge.web.wicket.AbstractListPage)
   */
  @Override
  protected AddressListForm newListForm(final AbstractListPage< ? , ? , ? > parentPage)
  {
    return new AddressListForm(this);
  }

}
