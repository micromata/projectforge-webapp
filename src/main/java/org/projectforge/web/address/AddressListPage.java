/////////////////////////////////////////////////////////////////////////////
//
// Project ProjectForge Community Edition
//         www.projectforge.org
//
// Copyright (C) 2001-2010 Kai Reinhard (k.reinhard@me.com)
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

package org.projectforge.web.address;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.PageParameters;
import org.apache.wicket.behavior.SimpleAttributeModifier;
import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.projectforge.address.AddressDO;
import org.projectforge.address.AddressDao;
import org.projectforge.address.PersonalAddressDO;
import org.projectforge.address.PersonalAddressDao;
import org.projectforge.core.Configuration;
import org.projectforge.web.calendar.DateTimeFormatter;
import org.projectforge.web.wicket.AbstractEditPage;
import org.projectforge.web.wicket.AbstractListPage;
import org.projectforge.web.wicket.CellItemListener;
import org.projectforge.web.wicket.CellItemListenerPropertyColumn;
import org.projectforge.web.wicket.DetachableDOModel;
import org.projectforge.web.wicket.ListPage;
import org.projectforge.web.wicket.ListSelectActionPanel;
import org.projectforge.web.wicket.PresizedImage;
import org.projectforge.web.wicket.WebConstants;
import org.projectforge.web.wicket.components.ContentMenuEntryPanel;
import org.projectforge.web.wicket.components.ExternalLinkPanel;
import org.projectforge.web.wicket.components.ImageLinkPanel;

@ListPage(editPage = AddressEditPage.class)
public class AddressListPage extends AbstractListPage<AddressListForm, AddressDao, AddressDO>
{
  private static final long serialVersionUID = 5168079498385464639L;

  @SpringBean(name = "addressDao")
  private AddressDao addressDao;

  @SpringBean(name = "personalAddressDao")
  private PersonalAddressDao personalAddressDao;

  private Map<Integer, PersonalAddressDO> personalAddressMap;

  public AddressListPage(PageParameters parameters)
  {
    super(parameters, "address");
  }

  @Override
  protected void onBodyTag(ComponentTag bodyTag)
  {
    bodyTag.put("onload", "javascript:setOptionStatus();");
  }

  @SuppressWarnings("serial")
  @Override
  protected void init()
  {
    if (Configuration.getInstance().isSmsConfigured() == true) {
      final ContentMenuEntryPanel menuEntry = new ContentMenuEntryPanel(getNewContentMenuChildId(), new Link<Object>("link") {
        @Override
        public void onClick()
        {
          setResponsePage(SendSmsPage.class, new PageParameters());
        };
      }, getString("address.tooltip.writeSMS"));
      addContentMenuEntry(menuEntry);
    }
    personalAddressMap = personalAddressDao.getPersonalAddressByAddressId();
    final List<IColumn<AddressDO>> columns = new ArrayList<IColumn<AddressDO>>();
    final CellItemListener<AddressDO> cellItemListener = new CellItemListener<AddressDO>() {
      public void populateItem(Item<ICellPopulator<AddressDO>> item, String componentId, IModel<AddressDO> rowModel)
      {
        final AddressDO address = rowModel.getObject();
        final StringBuffer cssStyle = getCssStyle(address.getId(), address.isDeleted());
        if (address.isDeleted() == true) {
          // Do nothing further
        } else if (personalAddressMap.containsKey(address.getId()) == true) {
          cssStyle.append("color: red;");
        }
        if (cssStyle.length() > 0) {
          item.add(new AttributeModifier("style", true, new Model<String>(cssStyle.toString())));
        }
      }
    };
    columns.add(new CellItemListenerPropertyColumn<AddressDO>(new Model<String>(getString("modified")), "lastUpdate", "lastUpdate",
        cellItemListener) {
      @SuppressWarnings("unchecked")
      @Override
      public void populateItem(final Item item, final String componentId, final IModel rowModel)
      {
        final AddressDO address = (AddressDO) rowModel.getObject();
        final RepeatingView view = new RepeatingView(componentId);
        item.add(view);
        view.add(new ListSelectActionPanel(view.newChildId(), rowModel, AddressEditPage.class, address.getId(), AddressListPage.this,
            DateTimeFormatter.instance().getFormattedDate(address.getLastUpdate())));
        view.add(new ImageLinkPanel(view.newChildId(), getResponse(), WebConstants.IMAGE_PRINTER, getString("printView")) {

          @Override
          public void onClick()
          {
            final PageParameters params = new PageParameters();
            params.put(AbstractEditPage.PARAMETER_KEY_ID, address.getId());
            final AddressViewPage addressViewPage = new AddressViewPage(params);
            setResponsePage(addressViewPage);
          }
        });
        addRowClick(item);
        cellItemListener.populateItem(item, componentId, rowModel);
      }
    });
    columns.add(new CellItemListenerPropertyColumn<AddressDO>(new Model<String>(getString("name")), "name", "name", cellItemListener));
    columns.add(new CellItemListenerPropertyColumn<AddressDO>(new Model<String>(getString("firstName")), "firstName", "firstName",
        cellItemListener));
    columns.add(new CellItemListenerPropertyColumn<AddressDO>(new Model<String>(getString("organization")), "organization", "organization",
        cellItemListener));
    columns.add(new CellItemListenerPropertyColumn<AddressDO>(new Model<String>(getString("email")), null, "email", cellItemListener) {
      @SuppressWarnings("unchecked")
      @Override
      public void populateItem(final Item item, final String componentId, final IModel rowModel)
      {
        final AddressDO address = (AddressDO) rowModel.getObject();
        final RepeatingView view = new RepeatingView(componentId);
        item.add(view);
        boolean first = true;
        if (StringUtils.isNotBlank(address.getEmail()) == true) {
          view.add(new ExternalLinkPanel(view.newChildId(), "mailto:" + address.getEmail(), address.getEmail()));
          first = false;
        }
        if (StringUtils.isNotBlank(address.getPrivateEmail()) == true) {
          if (first == true) {
            first = false;
          } else {
            view.add(new Label(view.newChildId(), "<br/>").setEscapeModelStrings(false));
          }
          view.add(new ExternalLinkPanel(view.newChildId(), "mailto:" + address.getPrivateEmail(), address.getPrivateEmail()));
        }
        // cellItemListener.populateItem(item, componentId, rowModel);
      }
    });
    columns.add(new CellItemListenerPropertyColumn<AddressDO>(new Model<String>(getString("address.phoneNumbers")), null, null,
        cellItemListener) {
      @SuppressWarnings("unchecked")
      @Override
      public void populateItem(final Item item, final String componentId, final IModel rowModel)
      {
        final AddressDO address = (AddressDO) rowModel.getObject();
        final RepeatingView view = new RepeatingView(componentId);
        item.add(view);
        final Integer id = address.getId();
        boolean first = addPhoneNumber(view, id, "business", address.getBusinessPhone(), false, WebConstants.IMAGE_PHONE, true);
        first = addPhoneNumber(view, id, "mobile", address.getMobilePhone(), true, WebConstants.IMAGE_PHONE_MOBILE, first);
        first = addPhoneNumber(view, id, "private", address.getPrivatePhone(), false, WebConstants.IMAGE_PHONE_HOME, first);
        first = addPhoneNumber(view, id, "privateMobile", address.getPrivateMobilePhone(), true, WebConstants.IMAGE_PHONE_MOBILE, first);
        cellItemListener.populateItem(item, componentId, rowModel);
      }
    });
    dataTable = createDataTable(columns, null, true);
    form.add(dataTable);
  }

  private boolean addPhoneNumber(final RepeatingView view, final Integer addressId, final String name, final String phoneNumber,
      final boolean sendSms, final String image, final boolean first)
  {
    if (StringUtils.isBlank(phoneNumber) == true) {
      return first;
    }
    if (first == false) {
      view.add(new Label(view.newChildId(), "<br/>").setEscapeModelStrings(false).setRenderBodyOnly(true));
    }
    final Fragment fragment = new Fragment(view.newChildId(), "phoneNumber", this);
    view.add(fragment.setRenderBodyOnly(true));
    @SuppressWarnings("serial")
    final Link<String> phoneCall = new Link<String>("directCallLink") {
      @Override
      public void onClick()
      {
        // /secure/address/PhoneCall.action?addressId=2&phoneType=privateMobile
        throw new UnsupportedOperationException();
      }
    };
    phoneCall.add(new SimpleAttributeModifier("onmouseover", "zoom('" + phoneNumber + "'); return false;"));
    final String tooltip = getString("address." + name + "Phone");
    fragment.add(phoneCall.add(new SimpleAttributeModifier("title", tooltip)));
    final Label phoneNumberLabel = new Label("phoneNumber", phoneNumber);
    if (this.personalAddressMap.containsKey(addressId) == true) {
      phoneNumberLabel.add(new SimpleAttributeModifier("style", "color:red; font-weight:bold;"));
    } else {
      phoneNumberLabel.setRenderBodyOnly(true);
    }
    phoneCall.add(phoneNumberLabel);
    phoneCall.add(new PresizedImage("phoneImage", getResponse(), image));
    @SuppressWarnings("serial")
    final Link<String> sendMessage = new Link<String>("sendMessageLink") {
      @Override
      public void onClick()
      {
        final PageParameters params = new PageParameters();
        params.put(SendSmsPage.PARAMETER_KEY_ADDRESS_ID, addressId);
        params.put(SendSmsPage.PARAMETER_KEY_PHONE_TYPE, name);
        setResponsePage(SendSmsPage.class, params);
      }
    };
    if (sendSms == false || Configuration.getInstance().isSmsConfigured() == false) {
      sendMessage.setVisible(false);
    }
    fragment.add(sendMessage);
    return false;
  }

  @Override
  protected void addTopPanel()
  {
    final Fragment topFragment = new Fragment("topPanel", "topFragment", this);
    form.add(topFragment);
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
  protected AddressListForm newListForm(AbstractListPage< ? , ? , ? > parentPage)
  {
    return new AddressListForm(this);
  }

  @Override
  protected AddressDao getBaseDao()
  {
    return addressDao;
  }

  @Override
  protected IModel<AddressDO> getModel(AddressDO object)
  {
    return new DetachableDOModel<AddressDO, AddressDao>(object, getBaseDao());
  }

  protected AddressDao getAddressDao()
  {
    return addressDao;
  }
}
