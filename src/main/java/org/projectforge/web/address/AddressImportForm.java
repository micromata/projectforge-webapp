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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Date;
import java.util.LinkedList;
import java.util.List;

import net.fortuna.ical4j.data.ParserException;
import net.fortuna.ical4j.vcard.Parameter;
import net.fortuna.ical4j.vcard.Parameter.Id;
import net.fortuna.ical4j.vcard.Property;
import net.fortuna.ical4j.vcard.VCard;
import net.fortuna.ical4j.vcard.VCardBuilder;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.wicket.markup.html.form.upload.FileUpload;
import org.apache.wicket.markup.html.form.upload.FileUploadField;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.joda.time.DateTime;
import org.projectforge.address.AddressDO;
import org.projectforge.address.AddressDao;
import org.projectforge.address.AddressStatus;
import org.projectforge.address.ContactStatus;
import org.projectforge.address.FormOfAddress;
import org.projectforge.core.BaseSearchFilter;
import org.projectforge.core.QueryFilter;
import org.projectforge.web.wicket.AbstractEditForm;
import org.projectforge.web.wicket.EditPage;
import org.projectforge.web.wicket.bootstrap.GridSize;
import org.projectforge.web.wicket.flowlayout.FieldsetPanel;
import org.projectforge.web.wicket.flowlayout.FileUploadPanel;

/**
 * @author Maximilian Lauterbach (m.lauterbach@micromata.de)
 * 
 */
public class AddressImportForm extends AbstractEditForm<AddressDO, AddressImportPage>
{
  private static final long serialVersionUID = -1691614676645602272L;

  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(AddressImportForm.class);

  private final List<FileUpload> uploads = new LinkedList<FileUpload>();

  /**
   * @param parentPage
   * @param data
   */
  public AddressImportForm(final AddressImportPage parentPage, final AddressDO data)
  {
    super(parentPage, data);
  }

  /**
   * @see org.apache.wicket.Component#onInitialize()
   */
  @Override
  protected void onInitialize()
  {
    super.onInitialize();
    gridBuilder.newSplitPanel(GridSize.COL50);
    final FieldsetPanel newFieldset = gridBuilder.newFieldset(getString("address.book.vCardImport.fileUploadPanel"));

    final FileUploadField uploadField = new FileUploadField(FileUploadPanel.WICKET_ID, new PropertyModel<List<FileUpload>>(this, "uploads"));
    newFieldset.add(new FileUploadPanel(newFieldset.newChildId(), uploadField));
  }

  /**
   * @see org.projectforge.web.wicket.AbstractEditForm#getLogger()
   */
  @Override
  protected Logger getLogger()
  {
    return log;
  }

  @SuppressWarnings("serial")
  public void create()
  {
    if (uploads != null) {
      final FileUpload upload = uploads.get(0);
      if (upload.getClientFileName().endsWith(".vcf") == false) {
        feedbackPanel.error(getString("address.book.vCardImport.wrongFileType"));
      } else {
        try {
          final File file = upload.writeToTempFile();

          final FileInputStream fis = new FileInputStream(file);
          final VCardBuilder builder = new VCardBuilder(fis);
          final VCard card = builder.build();

          // //// SET BASE DATA
          setName(card.getProperty(net.fortuna.ical4j.vcard.Property.Id.N));
          setOrganization(card.getProperty(net.fortuna.ical4j.vcard.Property.Id.ORG));
          setBirth(card.getProperty(net.fortuna.ical4j.vcard.Property.Id.BDAY));
          setNote(card.getProperty(net.fortuna.ical4j.vcard.Property.Id.NOTE));

          // //// SET ADDITIONAL DATA
          final List<Property> li = card.getProperties();
          setProperties(li);

          // handle item entries
          final VCardItemElementHandler ih = new VCardItemElementHandler(new FileInputStream(file));
          if (!ih.getItemList().isEmpty())
            setProperties(ih.getItemList());

          data.setAddressStatus(AddressStatus.UPTODATE);
          data.setDeleted(false);
          data.setLastUpdate(DateTime.now().toDate());
          data.setCreated(DateTime.now().toDate());
          data.setContactStatus(ContactStatus.ACTIVE);
          data.setForm(FormOfAddress.UNKNOWN);

          // //// CHECK IF THERE IS SOMETHING MORE TO ADD
          if (data.getAddressText() == null || data.getAddressText() == "") {
            setOtherPropertiesToWork(li);
            if (!ih.getItemList().isEmpty() && data.getAddressText() == null || data.getAddressText() == "") {
              setOtherPropertiesToWork(ih.getItemList());
            }
          } else if (data.getPrivateAddressText() == null || data.getPrivateAddressText() == "") {
            setOtherPropertiesToPrivate(li);
            if (!ih.getItemList().isEmpty() && data.getPostalAddressText() == null || data.getPostalAddressText() == "")
              setOtherPropertiesToPrivate(ih.getItemList());
          } else {
            setPostalProperties(li);
            if (!ih.getItemList().isEmpty() && data.getPostalAddressText() == null || data.getPostalAddressText() == "")
              setPostalProperties(ih.getItemList());
          }

          // /// CHECK FOR EXISTING ENTRIES
          final BaseSearchFilter af = new BaseSearchFilter();
          af.setSearchString(data.getName() + " " + data.getFirstName() + " ");
          final AddressDao dao = (AddressDao) getBaseDao();
          final QueryFilter queryFilter = new QueryFilter(af);
          final List<AddressDO> list = dao.internalGetList(queryFilter);

          // //// SAVING
          /*
           * if list is > 0 there are entries with the same name.
           */
          if (list.size() == 0) {
            final PageParameters params = new PageParameters();

            // inner class to set the right return page.
            @EditPage(defaultReturnPage = AddressListPage.class)
            class MyEditPage extends AddressEditPage
            {

              /**
               * @param parameters
               */
              public MyEditPage(final PageParameters parameters)
              {
                super(parameters);
              }

              /**
               * @see org.projectforge.web.wicket.AbstractEditPage#init(org.projectforge.core.AbstractBaseDO)
               */
              @Override
              protected void init(final AddressDO data)
              {
                super.init(AddressImportForm.this.getData());
              }
            }
            final AddressEditPage addressEditPage = new MyEditPage(params);
            addressEditPage.newEditForm(parentPage, getData());
            setResponsePage(addressEditPage);
          } else {
            feedbackPanel.error(getString("address.book.vCardImport.existingEntry"));
          }
        } catch (final IOException ex) {
          log.fatal("Exception encountered " + ex, ex);
        } catch (final ParserException ex) {
          log.fatal("Exception encountered " + ex, ex);
        }
      }
    } else
      feedbackPanel.error(getString("address.book.vCardImport.noFile"));
  }

  /**
   * @param property
   */
  private void setNote(final Property property)
  {
    data.setComment(property.getValue());
  }

  /**
   * @param li
   */
  private void setProperties(final List<Property> li)
  {
    for (final Property property : li) {
      final List<Parameter> lii = property.getParameters(Id.TYPE);
      for (final Parameter param : lii) {
        if (param.getValue().equals("HOME"))
          setHomeData(property);
        else if (param.getValue().equals("WORK"))
          setWorkData(property);
      }
    }
  }

  private void setPostalProperties(final List<Property> li)
  {
    for (final Property property : li) {
      final List<Parameter> lii = property.getParameters(Id.TYPE);
      for (final Parameter param : lii) {
        if (param.getValue().equals("OTHER")) {
          // ////SET WORK ADDRESS
          if (property.getId().toString().equals("ADR")) {
            final String str[] = StringUtils.split(property.getValue(), ';');
            final int size = str.length;
            if (size >= 1)
              data.setPostalAddressText(str[0]);
            if (size >= 2)
              data.setPostalCity(str[1]);
            if (size >= 3)
              data.setPostalZipCode(str[2]);
            if (size >= 4)
              data.setPostalCountry(str[3]);
            if (size >= 5)
              data.setPostalState(str[4]);
          }
        }
      }
    }
  }

  private void setOtherPropertiesToWork(final List<Property> li)
  {
    for (final Property property : li) {
      final List<Parameter> lii = property.getParameters(Id.TYPE);
      for (final Parameter param : lii) {
        if (param.getValue().equals("OTHER")) {
          // ////SET WORK ADDRESS
          if (property.getId().toString().equals("ADR")) {
            final String str[] = StringUtils.split(property.getValue(), ';');
            final int size = str.length;
            if (size >= 1)
              data.setAddressText(str[0]);
            if (size >= 2)
              data.setCity(str[1]);
            if (size >= 3)
              data.setZipCode(str[2]);
            if (size >= 4)
              data.setCountry(str[3]);
            if (size >= 5)
              data.setState(str[4]);
          }
        }
      }
    }
  }

  private void setOtherPropertiesToPrivate(final List<Property> li)
  {
    for (final Property property : li) {
      final List<Parameter> lii = property.getParameters(Id.TYPE);
      for (final Parameter param : lii) {
        if (param.getValue().equals("OTHER")) {
          // //// SET HOME ADDRESS
          if (property.getId().toString().equals("ADR")) {
            final String str[] = StringUtils.split(property.getValue(), ';');
            final int size = str.length;
            // street
            if (size >= 1)
              data.setPrivateAddressText(str[0]);
            // city
            if (size >= 2)
              data.setPrivateCity(str[1]);
            // zip code
            if (size >= 3)
              data.setPrivateZipCode(str[2]);
            // country
            if (size >= 4)
              data.setPrivateCountry(str[3]);
            // state
            if (size >= 5)
              data.setPrivateState(str[4]);
          }
        }
      }
    }
  }

  /**
   * @param property
   */
  private void setBirth(final Property property)
  {
    if (property != null)
      data.setBirthday(new Date(DateTime.parse(property.getValue()).getMillis()));
  }

  private void setName(final Property property)
  {
    final String str[] = StringUtils.split(property.getValue(), ';');
    data.setName(str[0]);
    data.setFirstName(str[1]);
    if (str.length >= 3)
      data.setTitle(str[2]);
  }

  private void setOrganization(final Property property)
  {
    final String org = StringUtils.substringBefore(property.getValue(), ";");
    data.setOrganization(org);
    final String division = StringUtils.substringAfter(property.getValue(), ";");
    data.setDivision(division);
  }

  /**
   * Create home data
   * 
   * @param property
   */
  private void setHomeData(final Property property)
  {
    boolean telCheck = true; // to seperate phone and mobil number
    // //// SET HOME EMAIL
    if (property.getId().toString().equals("EMAIL"))
      data.setPrivateEmail(property.getValue());

    // //// SET HOME PHONE
    if (property.getId().toString().equals("TEL")) {
      final List<Parameter> list = property.getParameters();
      for (final Parameter p : list) {
        if (p.getValue().toString().equals("VOICE")) {
          final String tel = getTel(property.getValue());

          // phone number first, mobil number second
          if (telCheck) {
            data.setPrivatePhone(tel);
            telCheck = false;
            break;
          } else {
            data.setPrivateMobilePhone(tel);
            break;
          }
        }
        if (data.getPrivatePhone() == null && property.toString().contains("FAX") == false) {
          data.setPrivatePhone(getTel(property.getValue()));
        } else {
          if (data.getPrivateMobilePhone() == null && property.toString().contains("FAX") == false) {
            data.setPrivateMobilePhone(getTel(property.getValue()));
          }
        }
      }
    }

    // //// SET FAX -> no private fax

    // //// SET HOME ADDRESS
    if (property.getId().toString().equals("ADR")) {
      final String str[] = StringUtils.split(property.getValue(), ';');
      final int size = str.length;
      if (size >= 1)
        data.setPrivateAddressText(str[0]);
      if (size >= 2)
        data.setPrivateCity(str[1]);
      if (size >= 3)
        data.setPrivateZipCode(str[2]);
      if (size >= 4)
        data.setPrivateCountry(str[3]);
      if (size >= 5)
        data.setPrivateState(str[4]);
    }

    // //// SET HOME URL -> no space for home url
  }

  /**
   * @param property
   * @return
   */
  private String getTel(String tel)
  {
    if (tel.startsWith("0")) {
      tel = "+49 " + tel.substring(1);
    } else {
      if (!tel.startsWith("+"))
        tel = "+49 " + tel;
    }
    return tel;
  }

  private void setWorkData(final Property property)
  {
    boolean telCheck = true; // to seperate phone and mobil number

    // //// SET WORK PHONE
    if (property.getId().toString().equals("TEL")) {
      final List<Parameter> list = property.getParameters();
      for (final Parameter p : list) {
        if (p.getValue().toString().equals("VOICE")) {
          final String tel = getTel(property.getValue());

          // phone number first, mobil number second
          if (telCheck) {
            data.setBusinessPhone(tel);
            telCheck = false;
            break;
          } else {
            data.setMobilePhone(tel);
            break;
          }
        }
        // //// SET WORK FAX
        if (p.getValue().toString().equals("FAX")) {
          data.setFax(getTel(property.getValue()));
        }
        if (data.getBusinessPhone() == null && property.toString().contains("FAX") == false) {
          data.setBusinessPhone(getTel(property.getValue()));
        } else {
          if (data.getMobilePhone() == null && property.toString().contains("FAX") == false) {
            data.setMobilePhone(getTel(property.getValue()));
          }
        }
      }
    }

    // //// SET WORK EMAIL
    if (property.getId().toString().equals("EMAIL"))
      data.setEmail(property.getValue());

    // //// SET WORK ADDRESS
    if (property.getId().toString().equals("ADR")) {
      final String str[] = StringUtils.split(property.getValue(), ';');
      final int size = str.length;
      if (size >= 1)
        data.setAddressText(str[0]);
      if (size >= 2)
        data.setCity(str[1]);
      if (size >= 3)
        data.setZipCode(str[2]);
      if (size >= 4)
        data.setCountry(str[3]);
      if (size >= 5)
        data.setState(str[4]);
    }

    // //// SET WORK URL
    if (property.getId().toString().equals("URL"))
      data.setWebsite(property.getValue());
  }
}
