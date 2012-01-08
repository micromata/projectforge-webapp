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
import org.apache.wicket.markup.html.JavascriptPackageResource;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.WebPage;
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
import org.projectforge.address.PhoneType;
import org.projectforge.core.ConfigXml;
import org.projectforge.web.calendar.DateTimeFormatter;
import org.projectforge.web.wicket.AbstractEditPage;
import org.projectforge.web.wicket.AbstractListPage;
import org.projectforge.web.wicket.AttributeAppendModifier;
import org.projectforge.web.wicket.CellItemListener;
import org.projectforge.web.wicket.CellItemListenerPropertyColumn;
import org.projectforge.web.wicket.DetachableDOModel;
import org.projectforge.web.wicket.IListPageColumnsCreator;
import org.projectforge.web.wicket.ListPage;
import org.projectforge.web.wicket.ListSelectActionPanel;
import org.projectforge.web.wicket.PresizedImage;
import org.projectforge.web.wicket.WebConstants;
import org.projectforge.web.wicket.components.ContentMenuEntryPanel;
import org.projectforge.web.wicket.components.ExternalLinkPanel;
import org.projectforge.web.wicket.components.ImageLinkPanel;

@ListPage(editPage = AddressEditPage.class)
public class AddressListPage extends AbstractListPage<AddressListForm, AddressDao, AddressDO> implements IListPageColumnsCreator<AddressDO>
{
  private static final long serialVersionUID = 5168079498385464639L;

  @SpringBean(name = "addressDao")
  private AddressDao addressDao;

  @SpringBean(name = "personalAddressDao")
  private PersonalAddressDao personalAddressDao;

  Map<Integer, PersonalAddressDO> personalAddressMap;

  private boolean messagingSupported;

  private boolean phoneCallSupported;

  public AddressListPage(PageParameters parameters)
  {
    super(parameters, "address");
    add(JavascriptPackageResource.getHeaderContribution("scripts/zoom.js"));
  }

  @Override
  protected void setup()
  {
    super.setup();
    this.recentSearchTermsUserPrefKey = "addressSearchTerms";
    messagingSupported = ConfigXml.getInstance().isSmsConfigured() == true;
    phoneCallSupported = ConfigXml.getInstance().isTelephoneSystemUrlConfigured() == true;
  }

  @Override
  protected void onBodyTag(ComponentTag bodyTag)
  {
    bodyTag.put("onload", "javascript:setOptionStatus();");
  }

  @SuppressWarnings("serial")
  public List<IColumn<AddressDO>> createColumns(final WebPage returnToPage, final boolean sortable)
  {
    final List<IColumn<AddressDO>> columns = new ArrayList<IColumn<AddressDO>>();
    final CellItemListener<AddressDO> cellItemListener = new CellItemListener<AddressDO>() {
      public void populateItem(Item<ICellPopulator<AddressDO>> item, String componentId, IModel<AddressDO> rowModel)
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
    columns.add(new CellItemListenerPropertyColumn<AddressDO>(new Model<String>(getString("modified")),
        getSortable("lastUpdate", sortable), "lastUpdate", cellItemListener) {
      @SuppressWarnings("unchecked")
      @Override
      public void populateItem(final Item item, final String componentId, final IModel rowModel)
      {
        final AddressDO address = (AddressDO) rowModel.getObject();
        final RepeatingView view = new RepeatingView(componentId);
        item.add(view);
        view.add(new ListSelectActionPanel(view.newChildId(), rowModel, AddressEditPage.class, address.getId(), returnToPage,
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
    columns.add(new CellItemListenerPropertyColumn<AddressDO>(new Model<String>(getString("name")), getSortable("name", sortable), "name",
        cellItemListener));
    columns.add(new CellItemListenerPropertyColumn<AddressDO>(new Model<String>(getString("firstName")),
        getSortable("firstName", sortable), "firstName", cellItemListener));
    columns.add(new CellItemListenerPropertyColumn<AddressDO>(new Model<String>(getString("organization")), getSortable("organization",
        sortable), "organization", cellItemListener));
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
          final ExternalLinkPanel mailToLinkPanel = new ExternalLinkPanel(view.newChildId(), "mailto:" + address.getEmail(), address
              .getEmail());
          mailToLinkPanel.getLink().add(new SimpleAttributeModifier("onclick", "javascript:suppressNextRowClick();"));
          view.add(mailToLinkPanel);
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
        cellItemListener.populateItem(item, componentId, rowModel);
      }
    });
    columns.add(new CellItemListenerPropertyColumn<AddressDO>(new Model<String>(getString("address.phoneNumbers")), null, null,
        cellItemListener) {
      @SuppressWarnings("unchecked")
      @Override
      public void populateItem(final Item item, final String componentId, final IModel rowModel)
      {
        final AddressDO address = (AddressDO) rowModel.getObject();
        final PersonalAddressDO personalAddress = personalAddressMap.get(address.getId());
        final RepeatingView view = new RepeatingView(componentId);
        item.add(view);
        final Integer id = address.getId();
        boolean first = addPhoneNumber(view, id, PhoneType.BUSINESS, address.getBusinessPhone(),
            (personalAddress != null && personalAddress.isFavoriteBusinessPhone() == true), false, WebConstants.IMAGE_PHONE, true);
        first = addPhoneNumber(view, id, PhoneType.MOBILE, address.getMobilePhone(), (personalAddress != null && personalAddress
            .isFavoriteMobilePhone() == true), true, WebConstants.IMAGE_PHONE_MOBILE, first);
        first = addPhoneNumber(view, id, PhoneType.PRIVATE, address.getPrivatePhone(), (personalAddress != null && personalAddress
            .isFavoritePrivatePhone() == true), false, WebConstants.IMAGE_PHONE_HOME, first);
        first = addPhoneNumber(view, id, PhoneType.PRIVATE_MOBILE, address.getPrivateMobilePhone(),
            (personalAddress != null && personalAddress.isFavoritePrivateMobilePhone() == true), true, WebConstants.IMAGE_PHONE_MOBILE,
            first);
        cellItemListener.populateItem(item, componentId, rowModel);
        item.add(new AttributeAppendModifier("style", new Model<String>("white-space: nowrap;")));
      }
    });
    return columns;
  }

  @SuppressWarnings("serial")
  @Override
  protected void init()
  {
    if (messagingSupported == true) {
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
    final List<IColumn<AddressDO>> columns = createColumns(this, true);
    dataTable = createDataTable(columns, null, true);
    form.add(dataTable);
  }

  @SuppressWarnings("serial")
  private boolean addPhoneNumber(final RepeatingView view, final Integer addressId, final PhoneType phoneType, final String phoneNumber,
      final boolean favoriteNumber, final boolean sendSms, final String image, final boolean first)
  {
    if (StringUtils.isBlank(phoneNumber) == true) {
      return first;
    }
    if (first == false) {
      view.add(new Label(view.newChildId(), "<br/>").setEscapeModelStrings(false).setRenderBodyOnly(true));
    }
    final Fragment fragment = new Fragment(view.newChildId(), "phoneNumber", this);
    view.add(fragment.setRenderBodyOnly(true));
    final WebMarkupContainer linkOrSpan;
    if (phoneCallSupported == true) {
      linkOrSpan = new Link<String>("directCallLink") {
        @Override
        public void onClick()
        {
          final PageParameters params = new PageParameters();
          params.put(PhoneCallPage.PARAMETER_KEY_ADDRESS_ID, addressId);
          params.add(PhoneCallPage.PARAMETER_KEY_NUMBER, phoneNumber);
          setResponsePage(new PhoneCallPage(params));
        }
      };
      fragment.add(createInvisibleDummyComponent("phoneNumber"));
    } else {
      linkOrSpan = new WebMarkupContainer("phoneNumber");
      fragment.add(createInvisibleDummyComponent("directCallLink"));
    }
    linkOrSpan.add(new SimpleAttributeModifier("onmouseover", "zoom('" + phoneNumber + "'); return false;"));
    final String tooltip = getString(phoneType.getI18nKey());
    fragment.add(linkOrSpan.add(new SimpleAttributeModifier("title", tooltip)));
    final Label numberLabel = new Label("number", phoneNumber);
    if (favoriteNumber == true) {
      numberLabel.add(new SimpleAttributeModifier("style", "color:red; font-weight:bold;"));
    } else {
      numberLabel.setRenderBodyOnly(true);
    }
    linkOrSpan.add(numberLabel);
    linkOrSpan.add(new PresizedImage("phoneImage", getResponse(), image));
    final Link<String> sendMessage = new Link<String>("sendMessageLink") {
      @Override
      public void onClick()
      {
        final PageParameters params = new PageParameters();
        params.put(SendSmsPage.PARAMETER_KEY_ADDRESS_ID, addressId);
        params.put(SendSmsPage.PARAMETER_KEY_PHONE_TYPE, phoneType);
        setResponsePage(SendSmsPage.class, params);
      }
    };
    if (sendSms == false || messagingSupported == false) {
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
