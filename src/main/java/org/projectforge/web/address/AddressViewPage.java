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

import org.apache.commons.lang.StringUtils;
import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.AbstractLink;
import org.apache.wicket.markup.html.link.ExternalLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.projectforge.address.AddressDO;
import org.projectforge.address.AddressDao;
import org.projectforge.web.HtmlHelper;
import org.projectforge.web.calendar.DateTimeFormatter;
import org.projectforge.web.wicket.AbstractEditPage;
import org.projectforge.web.wicket.AbstractSecuredPage;
import org.projectforge.web.wicket.components.ContentMenuEntryPanel;

public class AddressViewPage extends AbstractSecuredPage
{
  @SpringBean(name = "addressDao")
  private AddressDao addressDao;

  private AddressDO address;

  public AddressViewPage(final PageParameters parameters)
  {
    this(parameters, null);
  }

  @SuppressWarnings("serial")
  public AddressViewPage(final PageParameters parameters, final AbstractSecuredPage returnToPage)
  {
    super(parameters);
    this.returnToPage = returnToPage;
    if (parameters.containsKey(AbstractEditPage.PARAMETER_KEY_ID) == true) {
      final Integer addressId = parameters.getInt(AbstractEditPage.PARAMETER_KEY_ID);
      address = addressDao.getById(addressId);
    } else {
      address = new AddressDO();
    }
    final AbstractLink link;
    if (this.returnToPage != null) {
      link = new Link<String>("link") {
        @Override
        public void onClick()
        {
          setResponsePage(returnToPage);
        }
      };
    } else {
      link = new ExternalLink("link", "javascript:history.back()");
    }
    contentMenuEntries.add(new ContentMenuEntryPanel(getNewContentMenuChildId(), link, getString("back")));
    contentMenuEntries.add(new ContentMenuEntryPanel(getNewContentMenuChildId(), new Link<Object>("link") {
      @Override
      public void onClick()
      {
        final PageParameters params = new PageParameters();
        params.put(AbstractEditPage.PARAMETER_KEY_ID, address.getId());
        final AddressEditPage addressEditPage = new AddressEditPage(params);
        addressEditPage.setReturnToPage(AddressViewPage.this);
        setResponsePage(addressEditPage);
      };
    }, getString("update")));

    StringBuffer buf = new StringBuffer();
    if (address.getForm() != null) {
      buf.append(getString(address.getForm().getI18nKey())).append(" ");
    }
    if (address.getTitle() != null) {
      buf.append(address.getTitle()).append(" ");
    }
    if (address.getFirstName() != null) {
      buf.append(address.getFirstName()).append(" ");
    }
    if (address.getName() != null) {
      buf.append(address.getName());
    }
    body.add(new Label("name", buf.toString()));
    addRow("businessPhone", address.getBusinessPhone());
    addRow("fax", address.getFax());
    addRow("mobilePhone", address.getMobilePhone());
    addRow("privatePhone", address.getPrivatePhone());
    addRow("privateMobilePhone", address.getPrivateMobilePhone());
    addRow("organization", address.getOrganization());
    addRow("division", address.getDivision());
    addRow("positionText", address.getPositionText());
    addEMail("email", address.getEmail());
    addEMail("privateEmail", address.getPrivateEmail());
    addRow("website", address.getWebsite());
    addAddressRow("addressText", address.getAddressText(), address.getZipCode(), address.getCity(), address.getCountry(), address
        .getState());
    addAddressRow("postalAddress", address.getPostalAddressText(), address.getPostalZipCode(), address.getPostalCity(), address
        .getPostalCountry(), address.getPostalState());
    addAddressRow("privateAddress", address.getPrivateAddressText(), address.getPrivateZipCode(), address.getPrivateCity(), address
        .getPrivateCountry(), address.getPrivateState());
    final String birthday = address.getBirthday() != null ? DateTimeFormatter.instance().getFormattedDate(address.getBirthday()) : null;
    addRow("birthday", birthday);
    addRow("comment", address.getComment());
    addRow("publicKey", address.getPublicKey());
    addRow("fingerprint", address.getFingerprint());
  }

  @Override
  protected PageParameters getBookmarkRequiredPageParameters()
  {
    final PageParameters parameters = new PageParameters();
    if (address != null && address.getId() != null) {
      parameters.put("id", address.getId());
    }
    return parameters;
  }

  private void addAddressRow(final String id, final String addressText, final String zipCode, final String city, final String country,
      final String state)
  {
    final StringBuffer buf = new StringBuffer();
    boolean first = true;
    if (StringUtils.isNotBlank(addressText) == true) {
      first = false;
      buf.append(HtmlHelper.escapeXml(addressText));
    }
    if (StringUtils.isNotBlank(zipCode) == true || StringUtils.isNotBlank(city) == true) {
      if (first == true) {
        first = false;
      } else {
        buf.append("<br/>");
      }
      if (zipCode != null) {
        buf.append(HtmlHelper.escapeXml(zipCode)).append(" ");
      }
      if (city != null) {
        buf.append(HtmlHelper.escapeXml(city));
      }
    }
    if (StringUtils.isNotBlank(country) == true) {
      if (first == true) {
        first = false;
      } else {
        buf.append("<br/>");
      }
      if (country != null) {
        buf.append(HtmlHelper.escapeXml(country));
      }
    }
    if (StringUtils.isNotBlank(state) == true) {
      if (first == true) {
        first = false;
      } else {
        buf.append("<br/>");
      }
      if (state != null) {
        buf.append(HtmlHelper.escapeXml(state));
      }
    }
    addRow(id, buf.toString(), false);
  }

  private void addRow(final String id, final String content)
  {
    addRow(id, content, true);
  }

  private void addRow(final String id, final String content, final boolean escapeModelStrings)
  {
    final WebMarkupContainer container = new WebMarkupContainer(id + "Row");
    body.add(container);
    if (StringUtils.isBlank(content) == true) {
      container.setVisible(false);
      return;
    }
    final Label label = new Label(id, content);
    if (escapeModelStrings == false) {
      label.setEscapeModelStrings(escapeModelStrings);
    }
    container.add(label);
  }

  private void addEMail(final String id, final String address)
  {
    final WebMarkupContainer container = new WebMarkupContainer(id + "Row");
    body.add(container);
    if (StringUtils.isBlank(address) == true) {
      container.setVisible(false);
      return;
    }
    final ExternalLink link = new ExternalLink(id, "mailto:" + address, address);
    container.add(link);

  }

  @Override
  protected String getTitle()
  {
    return getString("address.view.title");
  }

}
