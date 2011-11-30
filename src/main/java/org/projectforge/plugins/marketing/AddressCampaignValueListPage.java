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

package org.projectforge.plugins.marketing;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.PageParameters;
import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.extensions.markup.html.repeater.data.table.AbstractColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.projectforge.address.AddressDO;
import org.projectforge.address.AddressDao;
import org.projectforge.address.PersonalAddressDO;
import org.projectforge.address.PersonalAddressDao;
import org.projectforge.web.calendar.DateTimeFormatter;
import org.projectforge.web.timesheet.TimesheetMassUpdatePage;
import org.projectforge.web.wicket.AbstractListPage;
import org.projectforge.web.wicket.AttributeAppendModifier;
import org.projectforge.web.wicket.CellItemListener;
import org.projectforge.web.wicket.CellItemListenerPropertyColumn;
import org.projectforge.web.wicket.DetachableDOModel;
import org.projectforge.web.wicket.IListPageColumnsCreator;
import org.projectforge.web.wicket.ListPage;
import org.projectforge.web.wicket.ListSelectActionPanel;
import org.projectforge.web.wicket.components.CheckBoxPanel;

/**
 * The controller of the list page. Most functionality such as search etc. is done by the super class.
 * @author Kai Reinhard (k.reinhard@micromata.de)
 * 
 */
@ListPage(editPage = AddressCampaignValueEditPage.class)
public class AddressCampaignValueListPage extends AbstractListPage<AddressCampaignValueListForm, AddressDao, AddressDO> implements
IListPageColumnsCreator<AddressDO>
{
  private static final long serialVersionUID = -2418497742599443358L;

  @SpringBean(name = "addressDao")
  private AddressDao addressDao;

  @SpringBean(name = "addressCampaignValueDao")
  private AddressCampaignValueDao addressCampaignValueDao;

  @SpringBean(name = "personalAddressDao")
  private PersonalAddressDao personalAddressDao;

  Map<Integer, PersonalAddressDO> personalAddressMap;

  Map<Integer, AddressCampaignValueDO> addressCampaignValueMap;

  public AddressCampaignValueListPage(final PageParameters parameters)
  {
    super(parameters, "plugins.marketing.addressCampaignValue");
  }

  @Override
  protected void onBodyTag(final ComponentTag bodyTag)
  {
    bodyTag.put("onload", "javascript:setOptionStatus();");
  }

  public List<IColumn<AddressDO>> createColumns(final WebPage returnToPage, final boolean sortable)
  {
    return createColumns(returnToPage, sortable, false);
  }

  @SuppressWarnings("serial")
  public List<IColumn<AddressDO>> createColumns(final WebPage returnToPage, final boolean sortable, final boolean massUpdateMode)
  {
    final List<IColumn<AddressDO>> columns = new ArrayList<IColumn<AddressDO>>();
    final CellItemListener<AddressDO> cellItemListener = new CellItemListener<AddressDO>() {
      public void populateItem(final Item<ICellPopulator<AddressDO>> item, final String componentId, final IModel<AddressDO> rowModel)
      {
        final AddressDO address = rowModel.getObject();
        final PersonalAddressDO personalAddress = personalAddressMap.get(address.getId());
        final StringBuffer cssStyle = getCssStyle(address.getId(), address.isDeleted());
        if (address.isDeleted() == true) {
          // Do nothing further
        } else if (personalAddress != null && personalAddress.isFavoriteCard() == true) {
          cssStyle.append("color: red;");
        }
        if (cssStyle.length() > 0) {
          item.add(new AttributeModifier("style", true, new Model<String>(cssStyle.toString())));
        }
      }
    };

    if (massUpdateMode == true) {
      columns.add(new CellItemListenerPropertyColumn<AddressDO>("", null, "selected", cellItemListener) {
        @Override
        public void populateItem(final Item<ICellPopulator<AddressDO>> item, final String componentId, final IModel<AddressDO> rowModel)
        {
          final AddressDO address = rowModel.getObject();
          final CheckBoxPanel checkBoxPanel = new CheckBoxPanel(componentId, new PropertyModel<Boolean>(address, "selected"), true);
          item.add(checkBoxPanel);
          cellItemListener.populateItem(item, componentId, rowModel);
          addRowClick(item, massUpdateMode);
        }
      });
    } else {
      columns.add(new CellItemListenerPropertyColumn<AddressDO>(new Model<String>(getString("created")), getSortable("created",
          sortable), "created", cellItemListener) {
        @SuppressWarnings("unchecked")
        @Override
        public void populateItem(final Item item, final String componentId, final IModel rowModel)
        {
          final AddressDO address = (AddressDO) rowModel.getObject();
          final AddressCampaignValueDO addressCampaignValue = addressCampaignValueMap.get(address.getId());
          final Integer addressCampaignValueId = addressCampaignValue != null ? addressCampaignValue.getId() : null;
          item.add(new ListSelectActionPanel(componentId, rowModel, AddressCampaignValueEditPage.class, addressCampaignValueId, returnToPage,
              DateTimeFormatter.instance().getFormattedDateTime(address.getCreated()), AddressCampaignValueEditPage.PARAMETER_ADDRESS_ID,
              String.valueOf(address.getId()), AddressCampaignValueEditPage.PARAMETER_ADDRESS_CAMPAIGN_ID, String.valueOf(form
                  .getSearchFilter().getAddressCampaignId())));
          addRowClick(item);
          cellItemListener.populateItem(item, componentId, rowModel);
        }
      });
    }
    columns.add(new CellItemListenerPropertyColumn<AddressDO>(new Model<String>(getString("name")), getSortable("name", sortable), "name",
        cellItemListener));
    columns.add(new CellItemListenerPropertyColumn<AddressDO>(new Model<String>(getString("firstName")),
        getSortable("firstName", sortable), "firstName", cellItemListener));
    columns.add(new CellItemListenerPropertyColumn<AddressDO>(new Model<String>(getString("organization")), getSortable("organization",
        sortable), "organization", cellItemListener));

    columns.add(new AbstractColumn<AddressDO>(new Model<String>(getString("value"))) {
      @Override
      public void populateItem(final Item<ICellPopulator<AddressDO>> item, final String componentId, final IModel<AddressDO> rowModel)
      {
        final AddressDO address = rowModel.getObject();
        final Integer id = address.getId();
        final AddressCampaignValueDO addressCampaignValue = addressCampaignValueMap.get(id);
        if (addressCampaignValue != null) {
          item.add(new Label(componentId, addressCampaignValue.getValue()));
          item.add(new AttributeAppendModifier("style", new Model<String>("white-space: nowrap;")));
        } else {
          item.add(new Label(componentId, ""));
        }
        cellItemListener.populateItem(item, componentId, rowModel);
      }
    });
    columns.add(new AbstractColumn<AddressDO>(new Model<String>(getString("comment"))) {
      @Override
      public void populateItem(final Item<ICellPopulator<AddressDO>> item, final String componentId, final IModel<AddressDO> rowModel)
      {
        final AddressDO address = rowModel.getObject();
        final Integer id = address.getId();
        final AddressCampaignValueDO addressCampaignValue = addressCampaignValueMap.get(id);
        if (addressCampaignValue != null) {
          item.add(new Label(componentId, addressCampaignValue.getComment()));
          item.add(new AttributeAppendModifier("style", new Model<String>("white-space: nowrap;")));
        } else {
          item.add(new Label(componentId, ""));
        }
        cellItemListener.populateItem(item, componentId, rowModel);
      }
    });
    return columns;
  }

  @Override
  protected void onNextSubmit()
  {
    final ArrayList<AddressDO> list = new ArrayList<AddressDO>();
    for (final AddressDO sheet : getList()) {
      if (sheet.isSelected() == true) {
        list.add(sheet);
      }
    }
    setResponsePage(new TimesheetMassUpdatePage(null, null));
  }

  @Override
  public boolean isSupportsMassUpdate()
  {
    return true;
  }

  @Override
  protected void onBeforeRender()
  {
    addressCampaignValueMap = addressCampaignValueDao.getAddressCampaignValuesByAddressId(form.getSearchFilter());
    super.onBeforeRender();
  }

  @Override
  protected void init()
  {
    personalAddressMap = personalAddressDao.getPersonalAddressByAddressId();
  }

  @Override
  protected void createDataTable()
  {
    final List<IColumn<AddressDO>> columns = createColumns(this, !isMassUpdateMode(), isMassUpdateMode());
    dataTable = createDataTable(columns, "name", true);
    form.add(dataTable);
  }

  @Override
  public void refresh()
  {
    super.refresh();
    if (form.getSearchFilter().isNewest() == true && StringUtils.isBlank(form.getSearchFilter().getSearchString()) == true) {
      form.getSearchFilter().setMaxRows(form.getPageSize());
    }
  }
  @Override
  protected AddressCampaignValueListForm newListForm(final AbstractListPage< ? , ? , ? > parentPage)
  {
    return new AddressCampaignValueListForm(this);
  }

  @Override
  protected AddressDao getBaseDao()
  {
    return addressDao;
  }

  @Override
  protected IModel<AddressDO> getModel(final AddressDO object)
  {
    return new DetachableDOModel<AddressDO, AddressDao>(object, getBaseDao());
  }

  protected AddressCampaignValueDao getAddressCampaignValueDao()
  {
    return addressCampaignValueDao;
  }
}
