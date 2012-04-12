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

import org.apache.log4j.Logger;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.projectforge.address.AddressDO;
import org.projectforge.address.AddressDao;
import org.projectforge.address.PersonalAddressDao;
import org.projectforge.web.address.AddressEditSupport.AddressParameters;
import org.projectforge.web.wicket.AbstractEditForm;
import org.projectforge.web.wicket.flowlayout.DivPanel;
import org.projectforge.web.wicket.flowlayout.DivType;
import org.projectforge.web.wicket.flowlayout.FieldType;
import org.projectforge.web.wicket.flowlayout.FieldsetPanel;

public class AddressEditForm extends AbstractEditForm<AddressDO, AddressEditPage>
{
  private static final long serialVersionUID = 3881031215413525517L;

  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(AddressEditForm.class);

  @SpringBean(name = "personalAddressDao")
  private PersonalAddressDao personalAddressDao;

  protected AddressEditSupport addressEditSupport;

  private static final String PHONE_NUMBER_FAVORITE_LABEL = "*";

  public AddressEditForm(final AddressEditPage parentPage, final AddressDO data)
  {
    super(parentPage, data);
  }

  @Override
  protected void init()
  {
    super.init();
    addressEditSupport = new AddressEditSupport(this, gridBuilder, (AddressDao) getBaseDao(), personalAddressDao, data);
    /* GRID8 - BLOCK */
    gridBuilder.newGrid8().newColumnsPanel().newColumnPanel(DivType.COL_50);
    addressEditSupport.addName();
    addressEditSupport.addFirstName();
    final FieldsetPanel fs = addressEditSupport.addFormOfAddress();
    final DivPanel checkBoxPanel = fs.addNewCheckBoxDiv();
    checkBoxPanel.addCheckBox(new PropertyModel<Boolean>(addressEditSupport.personalAddress, "favoriteCard"), getString("favorite"),
        getString("address.tooltip.vCardList"));
    addressEditSupport.addTitle();
    addressEditSupport.addWebsite();

    // /////////////////
    // Second box
    // /////////////////
    gridBuilder.newColumnPanel(DivType.COL_50);
    addressEditSupport.addOrganization();
    addressEditSupport.addDivision();
    addressEditSupport.addPosition();
    addressEditSupport.addEmail();
    addressEditSupport.addPrivateEmail();

    // /////////////////
    // Status
    // /////////////////
    gridBuilder.newGrid8().newColumnsPanel().newColumnPanel(DivType.COL_50);
    addressEditSupport.addContactStatus();
    gridBuilder.newColumnPanel(DivType.COL_50);
    addressEditSupport.addAddressStatus();
    gridBuilder.newColumnsPanel().newColumnPanel(DivType.COL_50);
    addressEditSupport.addBirthday();
    gridBuilder.newColumnPanel(DivType.COL_50);
    addressEditSupport.addLanguage();

    // /////////////////
    // Phone numbers
    // /////////////////
    gridBuilder.newColumnsPanel().newColumnPanel(DivType.COL_50);
    addPhoneNumber("businessPhone", "address.phone", "address.business", "favoriteBusinessPhone", FieldType.PHONE_NO);
    addPhoneNumber("fax", "address.phoneType.fax", "address.business", "favoriteFax", FieldType.PHONE_NO);
    addPhoneNumber("mobilePhone", "address.phoneType.mobile", "address.business", "favoriteMobilePhone", FieldType.MOBILE_PHONE_NO);

    gridBuilder.newColumnPanel(DivType.COL_50);
    addPhoneNumber("privatePhone", "address.phone", "address.private", "favoritePrivatePhone", FieldType.PHONE_NO);
    addPhoneNumber("privateMobilePhone", "address.phoneType.mobile", "address.private", "favoritePrivateMobilePhone",
        FieldType.MOBILE_PHONE_NO);

    // /////////////////
    // Addresses
    // /////////////////
    gridBuilder.newGrid8(true);
    addAddress(addressEditSupport.getBusinessAddressParameters());
    gridBuilder.newGrid8();
    addAddress(addressEditSupport.getPostalAddressParameters());
    gridBuilder.newGrid8();
    addAddress(addressEditSupport.getPrivateAddressParameters());

    gridBuilder.newGrid8().newBlockPanel();
    addressEditSupport.addFingerPrint();
    addressEditSupport.addPublicKey();

    gridBuilder.newGrid16(true).newBlockPanel();
    addressEditSupport.addComment();
  }

  private void addPhoneNumber(final String property, final String labelKey, final String labelDescriptionKey,
      final String favoriteProperty, final FieldType fieldType)
  {
    final FieldsetPanel fs = addressEditSupport.addPhoneNumber(property, labelKey, labelDescriptionKey, favoriteProperty, fieldType);
    final DivPanel checkBoxPanel = fs.addNewCheckBoxDiv();
    checkBoxPanel
    .addCheckBox(new PropertyModel<Boolean>(addressEditSupport.personalAddress, favoriteProperty), PHONE_NUMBER_FAVORITE_LABEL)
    .setTooltip(getString("address.tooltip.phonelist"));
  }

  private void addAddress(final AddressParameters params)
  {
    gridBuilder.newColumnsPanel().newColumnPanel(null);
    addressEditSupport.addAddressText(params.addressType, params.addressTextProperty);
    gridBuilder.newColumnsPanel().newColumnPanel(DivType.COL_50);
    addressEditSupport.addZipCode(params.zipCodeProperty);
    gridBuilder.newColumnPanel(DivType.COL_50);
    addressEditSupport.addCity(params.cityProperty);
    gridBuilder.newColumnsPanel().newColumnPanel(DivType.COL_50);
    addressEditSupport.addCountry(params.countryProperty);
    gridBuilder.newColumnPanel(DivType.COL_50);
    addressEditSupport.addState(params.stateProperty);
  }

  @Override
  protected Logger getLogger()
  {
    return log;
  }
}
