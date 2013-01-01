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

package org.projectforge.web.address;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Date;

import org.apache.log4j.Logger;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.projectforge.address.AddressDO;
import org.projectforge.address.AddressDao;
import org.projectforge.address.PersonalAddressDO;
import org.projectforge.address.PersonalAddressDao;
import org.projectforge.common.DateHelper;
import org.projectforge.core.ConfigXml;
import org.projectforge.web.wicket.AbstractEditPage;
import org.projectforge.web.wicket.AbstractSecuredBasePage;
import org.projectforge.web.wicket.DownloadUtils;
import org.projectforge.web.wicket.EditPage;
import org.projectforge.web.wicket.components.ContentMenuEntryPanel;

@EditPage(defaultReturnPage = AddressListPage.class)
public class AddressEditPage extends AbstractEditPage<AddressDO, AddressEditForm, AddressDao>
{
  private static final long serialVersionUID = 7091721062661400435L;

  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(AddressEditPage.class);

  @SpringBean(name = "addressDao")
  private AddressDao addressDao;

  @SpringBean(name = "personalAddressDao")
  private PersonalAddressDao personalAddressDao;

  @SuppressWarnings("serial")
  public AddressEditPage(final PageParameters parameters)
  {
    super(parameters, "address");
    init();
    if (isNew() == false) {
      ContentMenuEntryPanel menu = new ContentMenuEntryPanel(getNewContentMenuChildId(), new Link<Void>(ContentMenuEntryPanel.LINK_ID) {
        @Override
        public void onClick()
        {
          final Integer addressId = form.getData().getId();
          final PageParameters params = new PageParameters();
          params.add(AbstractEditPage.PARAMETER_KEY_ID, addressId);
          final AddressViewPage addressViewPage = new AddressViewPage(params);
          setResponsePage(addressViewPage);
        };
      }, getString("printView"));
      addContentMenuEntry(menu);

      final ContentMenuEntryPanel singleIcalExport = new ContentMenuEntryPanel(getNewContentMenuChildId(), new Link<Void>(ContentMenuEntryPanel.LINK_ID) {
        @Override
        public void onClick()
        {
          final AddressDO address = form.getData();
          final String filename = "ProjectForge-" + address.getFullName() + "_" + DateHelper.getDateAsFilenameSuffix(new Date()) + ".vcf";
          final StringWriter writer = new StringWriter();
          addressDao.exportVCard(new PrintWriter(writer), address);
          DownloadUtils.setUTF8CharacterEncoding(getResponse());
          DownloadUtils.setDownloadTarget(writer.toString().getBytes(), filename);
        };
      }, getString("address.book.vCardSingleExport"));
      addContentMenuEntry(singleIcalExport);


      if (ConfigXml.getInstance().isTelephoneSystemUrlConfigured() == true) {
        menu = new ContentMenuEntryPanel(getNewContentMenuChildId(), new Link<Void>(ContentMenuEntryPanel.LINK_ID) {
          @Override
          public void onClick()
          {
            final Integer addressId = form.getData().getId();
            final PageParameters params = new PageParameters();
            params.add(PhoneCallPage.PARAMETER_KEY_ADDRESS_ID, addressId);
            setResponsePage(new PhoneCallPage(params));
          };
        }, getString("address.directCall.call"));
        addContentMenuEntry(menu);
      }
    }
  }

  @Override
  public AbstractSecuredBasePage afterSaveOrUpdate()
  {
    final AddressDO address = addressDao.getOrLoad(getData().getId());
    final PersonalAddressDO personalAddress = form.addressEditSupport.personalAddress;
    personalAddress.setAddress(address);
    personalAddressDao.setOwner(personalAddress, getUserId()); // Set current logged in user as owner.
    personalAddressDao.saveOrUpdate(personalAddress);
    return null;
  }

  @Override
  protected AddressDao getBaseDao()
  {
    return addressDao;
  }

  @Override
  protected AddressEditForm newEditForm(final AbstractEditPage< ? , ? , ? > parentPage, final AddressDO data)
  {
    return new AddressEditForm(this, data);
  }

  @Override
  protected Logger getLogger()
  {
    return log;
  }
}
