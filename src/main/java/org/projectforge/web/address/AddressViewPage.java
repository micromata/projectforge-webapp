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
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.projectforge.address.AddressDO;
import org.projectforge.address.AddressDao;
import org.projectforge.common.StringHelper;
import org.projectforge.web.HtmlHelper;
import org.projectforge.web.calendar.DateTimeFormatter;
import org.projectforge.web.wicket.AbstractEditPage;
import org.projectforge.web.wicket.AbstractSecuredPage;
import org.projectforge.web.wicket.components.ContentMenuEntryPanel;
import org.projectforge.web.wicket.components.ExternalLinkPanel;
import org.projectforge.web.wicket.flowlayout.DivPanel;
import org.projectforge.web.wicket.flowlayout.DivTextPanel;
import org.projectforge.web.wicket.flowlayout.FieldsetPanel;
import org.projectforge.web.wicket.flowlayout.GridBuilder;
import org.projectforge.web.wicket.flowlayout.GridBuilderImpl;
import org.projectforge.web.wicket.flowlayout.Heading1Panel;
import org.projectforge.web.wicket.flowlayout.Heading3Panel;
import org.projectforge.web.wicket.flowlayout.ParTextPanel;

public class AddressViewPage extends AbstractSecuredPage
{
  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(AddressViewPage.class);

  private static final long serialVersionUID = 6317382828021216284L;

  @SpringBean(name = "addressDao")
  private AddressDao addressDao;

  private AddressDO address;

  private GridBuilder gridBuilder;

  public AddressViewPage(final PageParameters parameters)
  {
    this(parameters, null);
  }

  @SuppressWarnings("serial")
  public AddressViewPage(final PageParameters parameters, final AbstractSecuredPage returnToPage)
  {
    super(parameters);
    log.warn("**** WICKET 1.5 migration: add bookmarkable parameters");
    this.returnToPage = returnToPage;
    if (parameters.get(AbstractEditPage.PARAMETER_KEY_ID).isEmpty() == false) {
      final Integer addressId = parameters.get(AbstractEditPage.PARAMETER_KEY_ID).toInteger();
      address = addressDao.getById(addressId);
    }
    if (address == null && returnToPage != null) {
      setResponsePage(returnToPage);
      return;
    }
    if (this.returnToPage != null) {
      final ContentMenuEntryPanel back = new ContentMenuEntryPanel(getNewContentMenuChildId(), new Link<Object>("link") {
        @Override
        public void onClick()
        {
          setResponsePage(returnToPage);
        };
      }, getString("back"));
      addContentMenuEntry(back);
    }
    {
      final ContentMenuEntryPanel edit = new ContentMenuEntryPanel(getNewContentMenuChildId(), new Link<Object>("link") {
        @Override
        public void onClick()
        {
          final PageParameters params = new PageParameters();
          params.add(AbstractEditPage.PARAMETER_KEY_ID, address.getId());
          final AddressEditPage addressEditPage = new AddressEditPage(params);
          addressEditPage.setReturnToPage(AddressViewPage.this);
          setResponsePage(addressEditPage);
        };
      }, getString("edit"));
      addContentMenuEntry(edit);
    }

    final RepeatingView flowform = new RepeatingView("flowform");
    body.add(flowform);
    gridBuilder = new GridBuilderImpl(flowform, getMySession());
    final StringBuffer buf = new StringBuffer();
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
    final String name = buf.toString();

    gridBuilder.newGrid8(true).newBlockPanel();
    DivPanel section = gridBuilder.newSectionPanel();
    section.add(new Heading1Panel(section.newChildId(), name));
    appendFieldset("organization", address.getOrganization());
    appendFieldset("division", address.getDivision());
    appendFieldset("address.positionText", address.getPositionText());
    appendEmailFieldset("email", address.getEmail());
    appendEmailFieldset("address.privateEmail", address.getPrivateEmail());
    appendFieldset("address.website", address.getWebsite());
    final String birthday = address.getBirthday() != null ? DateTimeFormatter.instance().getFormattedDate(address.getBirthday()) : null;
    appendFieldset("address.birthday", birthday);
    // addRow("publicKey", address.getPublicKey());
    // addRow("fingerprint", address.getFingerprint());

    gridBuilder.newGrid8().newBlockPanel();
    section = gridBuilder.newSectionPanel();
    section.add(new Heading1Panel(section.newChildId(), getString("address.addresses")));
    final boolean output = addAddressRow(section, "address.heading.postalAddress", name, address.getOrganization(),
        address.getPostalAddressText(), address.getPostalZipCode(), address.getPostalCity(), address.getPostalCountry(),
        address.getPostalState(), null, null, null);
    if (output == true) {
      section = gridBuilder.newSectionPanel();
    }
    addAddressRow(section, "address.business", name, address.getOrganization(), address.getAddressText(), address.getZipCode(),
        address.getCity(), address.getCountry(), address.getState(), address.getBusinessPhone(), address.getMobilePhone(), address.getFax());
    if (output == true) {
      section = gridBuilder.newSectionPanel();
    }
    addAddressRow(section, "address.private", name, null, address.getPrivateAddressText(), address.getPrivateZipCode(),
        address.getPrivateCity(), address.getPrivateCountry(), address.getPrivateState(), address.getPrivatePhone(),
        address.getPrivateMobilePhone(), null);

    if (StringUtils.isNotBlank(address.getComment()) == true) {
      gridBuilder.newGrid16();
      section = gridBuilder.newSectionPanel();
      section.add(new Heading3Panel(section.newChildId(), getString("comment")));
      final ParTextPanel textPanel = new ParTextPanel(section.newChildId(), HtmlHelper.escapeHtml(address.getComment(), true));
      textPanel.getLabel().setEscapeModelStrings(false);
      section.add(textPanel);
    }
  }

  // @Override
  // protected PageParameters getBookmarkRequiredPageParameters()
  // {
  // final PageParameters parameters = new PageParameters();
  // if (address != null && address.getId() != null) {
  // parameters.add("id", address.getId());
  // }
  // return parameters;
  // }

  private boolean addAddressRow(final DivPanel section, final String type, final String name, final String organization,
      final String addressText, final String zipCode, final String city, final String country, final String state, final String phone,
      final String mobile, final String fax)
  {
    if (StringHelper.isNotBlank(addressText, zipCode, city, country, state, phone, mobile, fax) == false) {
      return false;
    }
    section.add(new ParTextPanel(section.newChildId(), getString(type) + ":"));
    final StringBuffer buf = new StringBuffer();
    boolean first = true;
    if (organization != null) {
      section.add(new Heading3Panel(section.newChildId(), organization));
      first = appendRow(buf, first, name);
    } else {
      section.add(new Heading3Panel(section.newChildId(), name));
    }
    if (StringUtils.isNotBlank(addressText) == true) {
      first = appendRow(buf, first, addressText);
    }
    if (StringUtils.isNotBlank(zipCode) == true || StringUtils.isNotBlank(city) == true) {
      final StringBuffer buf2 = new StringBuffer();
      if (zipCode != null) {
        buf2.append(HtmlHelper.escapeXml(zipCode)).append(" ");
      }
      if (city != null) {
        buf2.append(HtmlHelper.escapeXml(city));
      }
      first = appendRow(buf, first, buf2.toString());
    }
    if (StringUtils.isNotBlank(country) == true) {
      first = appendRow(buf, first, country);
    }
    if (StringUtils.isNotBlank(state) == true) {
      first = appendRow(buf, first, state);
    }
    if (StringUtils.isNotBlank(phone) == true) {
      first = appendRow(buf, first, getString("address.phone") + ": " + phone);
    }
    if (StringUtils.isNotBlank(fax) == true) {
      first = appendRow(buf, first, getString("address.phoneType.fax") + ": " + fax);
    }
    if (StringUtils.isNotBlank(mobile) == true) {
      first = appendRow(buf, first, getString("address.phoneType.mobile") + ": " + mobile);
    }
    final ParTextPanel text = new ParTextPanel(section.newChildId(), buf.toString());
    text.getLabel().setEscapeModelStrings(false);
    section.add(text);
    return true;
  }

  private boolean appendRow(final StringBuffer buf, final boolean first, final String str)
  {
    return StringHelper.append(buf, first, HtmlHelper.escapeXml(str), "<br/>");
  }

  private boolean appendFieldset(final String label, final String value)
  {
    if (StringUtils.isBlank(value) == true) {
      return false;
    }
    final FieldsetPanel fs = gridBuilder.newFieldset(getString(label)).setNoLabelFor();
    fs.add(new DivTextPanel(fs.newChildId(), value));
    return true;
  }

  private boolean appendEmailFieldset(final String label, final String value)
  {
    if (StringUtils.isBlank(value) == true) {
      return false;
    }
    final FieldsetPanel fs = gridBuilder.newFieldset(getString(label)).setNoLabelFor();
    fs.add(new ExternalLinkPanel(fs.newChildId(), "mailto:" + value, value));
    return true;
  }

  @Override
  protected String getTitle()
  {
    return getString("address.view.title");
  }

}
