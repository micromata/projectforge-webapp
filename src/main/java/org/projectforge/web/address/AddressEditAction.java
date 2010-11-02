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

import net.sourceforge.stripes.action.DontValidate;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.UrlBinding;
import net.sourceforge.stripes.validation.EmailTypeConverter;
import net.sourceforge.stripes.validation.EnumeratedTypeConverter;
import net.sourceforge.stripes.validation.Validate;
import net.sourceforge.stripes.validation.ValidateNestedProperties;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.projectforge.address.AddressDO;
import org.projectforge.address.AddressDao;
import org.projectforge.address.AddressStatus;
import org.projectforge.address.ContactStatus;
import org.projectforge.address.FormOfAddress;
import org.projectforge.address.PersonalAddressDO;
import org.projectforge.address.PersonalAddressDao;
import org.projectforge.common.LabelValueBean;
import org.projectforge.common.StringHelper;
import org.projectforge.web.core.BaseAction;
import org.projectforge.web.core.BaseEditAction;
import org.projectforge.web.core.BaseEditActionBean;
import org.projectforge.web.stripes.DateTypeConverter;


/**
 */
@UrlBinding("/secure/address/AddressEdit.action")
@BaseAction(jspUrl = "/WEB-INF/jsp/address/addressEdit.jsp")
@BaseEditAction(listAction = AddressListAction.class)
public class AddressEditAction extends BaseEditActionBean<AddressDao, AddressDO>
{
  private static final Logger log = Logger.getLogger(AddressEditAction.class);

  private PersonalAddressDO personalAddress;

  private PersonalAddressDao personalAddressDao;

  public void setPersonalAddressDao(PersonalAddressDao personalAddressDao)
  {
    this.personalAddressDao = personalAddressDao;
  }

  public PersonalAddressDO getPersonalAddress()
  {
    if (personalAddress == null) {
      personalAddress = new PersonalAddressDO();
    }
    return personalAddress;
  }

  @DontValidate
  public Resolution organizationAutocomplete()
  {
    String q = getAjaxAutocompleteValue();
    String result = baseDao.getAutocompletion(q, true, false, "organization");
    return getJsonResolution(result);
  }

  @DontValidate
  public Resolution addressTextAutocomplete()
  {
    String q = getAjaxAutocompleteValue();
    String result = baseDao.getAutocompletion(q, true, false, "addressText");
    return getJsonResolution(result);
  }


  @Override
  protected void onPreEdit()
  {
    if (getData().getId() != null) {
      // Update address, so get personalAddressDO if exists:
      personalAddress = personalAddressDao.getByAddressId(getData().getId());
    }
    if (personalAddress == null) {
      personalAddress = new PersonalAddressDO();
    }
    if (getData().getTaskId() == null) {
      baseDao.setTask(getData(), baseDao.getDefaultTaskId());
    }
  }

  @Override
  public Resolution reset()
  {
    super.reset();
    onPreEdit();
    return getInputPage();
  }

  @Override
  protected Resolution afterSaveOrUpdate()
  {
    AddressDO address = baseDao.getOrLoad(getData().getId());
    personalAddress.setAddress(address);
    personalAddressDao.setOwner(personalAddress, getContext().getUser().getId());
    personalAddressDao.saveOrUpdate(personalAddress);
    return null;
  }

  public List<LabelValueBean<String, FormOfAddress>> getAddressFormList()
  {
    FormOfAddress[] values = FormOfAddress.LIST;
    List<LabelValueBean<String, FormOfAddress>> list = new ArrayList<LabelValueBean<String, FormOfAddress>>();
    list.add(new LabelValueBean<String, FormOfAddress>(getLocalizedString("pleaseChoose"), null));
    for (FormOfAddress status : values) {
      list.add(new LabelValueBean<String, FormOfAddress>(getLocalizedString("address.form." + status.getKey()), status));
    }
    return list;
  }

  public List<LabelValueBean<String, AddressStatus>> getAddressStatusList()
  {
    AddressStatus[] values = AddressStatus.LIST;
    List<LabelValueBean<String, AddressStatus>> list = new ArrayList<LabelValueBean<String, AddressStatus>>();
    for (AddressStatus status : values) {
      list.add(new LabelValueBean<String, AddressStatus>(getLocalizedString("address.addressStatus." + status.getKey()), status));
    }
    return list;
  }

  public List<LabelValueBean<String, ContactStatus>> getContactStatusList()
  {
    ContactStatus[] values = ContactStatus.LIST;
    List<LabelValueBean<String, ContactStatus>> list = new ArrayList<LabelValueBean<String, ContactStatus>>();
    for (ContactStatus status : values) {
      list.add(new LabelValueBean<String, ContactStatus>(getLocalizedString("address.contactStatus." + status.getKey()), status));
    }
    return list;
  }

  public void setAddressDao(AddressDao addressDao)
  {
    this.baseDao = addressDao;
  }

  @ValidateNestedProperties( { @Validate(field = "name", required = true, maxlength = 255),
      @Validate(field = "firstName", maxlength = 255), @Validate(field = "title", maxlength = 255),
      @Validate(field = "positionText", maxlength = 255), @Validate(field = "organization", maxlength = 255),
      @Validate(field = "businessPhone", maxlength = 255), @Validate(field = "privatePhone", maxlength = 255),
      @Validate(field = "privateMobilePhone", maxlength = 255), @Validate(field = "mobilePhone", maxlength = 255),
      @Validate(field = "fax", maxlength = 255), @Validate(field = "addressText", maxlength = 255),
      @Validate(field = "zipCode", minlength = 3, maxlength = 255), @Validate(field = "city", maxlength = 255),
      @Validate(field = "state", maxlength = 255), @Validate(field = "country", maxlength = 255),
      @Validate(field = "email", on = { "save", "update"}, maxlength = 255, converter = EmailTypeConverter.class),
      @Validate(field = "addressStatus", converter = EnumeratedTypeConverter.class),
      @Validate(field = "contactStatus", converter = EnumeratedTypeConverter.class), @Validate(field = "website", maxlength = 255),
      @Validate(field = "publicKey", maxlength = 5000), @Validate(field = "fingerprint", maxlength = 255),
      @Validate(field = "comment", maxlength = 5000),
      @Validate(field = "birthday", on = { "save", "update"}, converter = DateTypeConverter.class, maxlength = 255)})
  public AddressDO getAddress()
  {
    return getData();
  }

  @Override
  protected void validate()
  {
    validatePhoneNumber("address.businessPhone", getAddress().getBusinessPhone());
    validatePhoneNumber("address.privatePhone", getAddress().getPrivatePhone());
    validatePhoneNumber("address.mobilePhone", getAddress().getMobilePhone());
    validatePhoneNumber("address.privateMobilePhone", getAddress().getPrivateMobilePhone());
    validatePhoneNumber("address.fax", getAddress().getFax());
    if (StringUtils.isNotBlank(getAddress().getFirstName()) == true && getAddress().getForm() == FormOfAddress.UNKNOWN) {
      addError("address.form", "address.error.formMustBeGivenIfFirstnameIsGiven");
    }
  }

  private void validatePhoneNumber(String field, String value)
  {
    if (StringHelper.checkPhoneNumberFormat(value) == false) {
      addError(field, "address.error.phone.invalidFormat", field);
    }
  }

  @Validate(required = true)
  public Integer getTaskId()
  {
    return getData().getTaskId();
  }

  public void setTaskId(Integer taskId)
  {
    baseDao.setTask(getData(), taskId);
  }

  public void setAddress(AddressDO data)
  {
    setData(data);
  }

  @Override
  protected Logger getLogger()
  {
    return log;
  }

  @Override
  protected AddressDO createDataInstance()
  {
    return new AddressDO();
  }
}
