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

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.apache.commons.lang.StringUtils;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.form.validation.IFormValidator;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.projectforge.address.AddressDO;
import org.projectforge.address.AddressDao;
import org.projectforge.address.AddressStatus;
import org.projectforge.address.ContactStatus;
import org.projectforge.address.FormOfAddress;
import org.projectforge.address.PersonalAddressDO;
import org.projectforge.address.PersonalAddressDao;
import org.projectforge.web.common.PhoneNumberValidator;
import org.projectforge.web.wicket.WicketUtils;
import org.projectforge.web.wicket.autocompletion.PFAutoCompleteMaxLengthTextField;
import org.projectforge.web.wicket.autocompletion.PFAutoCompleteTextField;
import org.projectforge.web.wicket.components.DatePanel;
import org.projectforge.web.wicket.components.DatePanelSettings;
import org.projectforge.web.wicket.components.LabelValueChoiceRenderer;
import org.projectforge.web.wicket.components.LanguageField;
import org.projectforge.web.wicket.components.MaxLengthTextArea;
import org.projectforge.web.wicket.components.MaxLengthTextField;
import org.projectforge.web.wicket.flowlayout.FieldType;
import org.projectforge.web.wicket.flowlayout.FieldsetPanel;
import org.projectforge.web.wicket.flowlayout.GridBuilder;
import org.projectforge.web.wicket.flowlayout.HtmlCommentPanel;
import org.projectforge.web.wicket.flowlayout.TextAreaPanel;

/**
 * For sharing functionality between mobile and normal edit pages.
 * @author Kai Reinhard (k.reinhard@micromata.de)
 * 
 */
class AddressEditSupport implements Serializable
{
  private static final long serialVersionUID = -7434267003873770614L;

  private final AddressDao addressDao;

  protected PersonalAddressDO personalAddress;

  private final AddressDO address;

  private final Form<AddressDO> form;

  @SuppressWarnings("unchecked")
  private final TextField<String>[] dependentFormComponents = new TextField[3];

  GridBuilder gridBuilder;

  @SuppressWarnings("serial")
  public AddressEditSupport(final Form<AddressDO> form, final GridBuilder gridBuilder, final AddressDao addressDao,
      final PersonalAddressDao personalAddressDao, final AddressDO address)
  {
    this.form = form;
    this.gridBuilder = gridBuilder;
    this.addressDao = addressDao;
    this.address = address;
    personalAddress = null;
    if (isNew() == false) {
      personalAddress = personalAddressDao.getByAddressId(address.getId());
    }
    if (personalAddress == null) {
      personalAddress = new PersonalAddressDO();
    }
    form.add(new IFormValidator() {
      @Override
      public FormComponent< ? >[] getDependentFormComponents()
      {
        return dependentFormComponents;
      }

      @Override
      public void validate(final Form< ? > form)
      {
        final TextField<String> name = dependentFormComponents[0];
        final TextField<String> firstName = dependentFormComponents[1];
        final TextField<String> organization = dependentFormComponents[2];
        if (StringUtils.isBlank(name.getValue()) == true
            && StringUtils.isBlank(firstName.getValue()) == true
            && StringUtils.isBlank(organization.getValue()) == true) {
          form.error(getString("address.form.error.toFewFields"));
        }
      }
    });
  }

  public FieldsetPanel addName()
  {
    final FieldsetPanel fs = gridBuilder.newFieldset(getString("name"));
    final MaxLengthTextField name = new MaxLengthTextField(fs.getTextFieldId(), new PropertyModel<String>(address, "name"));
    fs.add(dependentFormComponents[1] = name);
    if (isNew() == true) {
      WicketUtils.setFocus(name);
    }
    return fs;
  }

  public FieldsetPanel addFirstName()
  {
    final FieldsetPanel fs = gridBuilder.newFieldset(getString("firstName"));
    fs.add(dependentFormComponents[0] = new MaxLengthTextField(fs.getTextFieldId(), new PropertyModel<String>(address, "firstName")));
    return fs;
  }

  public FieldsetPanel addFormOfAddress()
  {
    final FieldsetPanel fs = gridBuilder.newFieldset(getString("address.form"), true);
    final LabelValueChoiceRenderer<FormOfAddress> formChoiceRenderer = new LabelValueChoiceRenderer<FormOfAddress>(form,
        FormOfAddress.values());
    fs.addDropDownChoice(new PropertyModel<FormOfAddress>(address, "form"), formChoiceRenderer.getValues(), formChoiceRenderer)
    .setRequired(true).setNullValid(false);
    return fs;
  }

  public FieldsetPanel addTitle()
  {
    final FieldsetPanel fs = gridBuilder.newFieldset(getString("address.title"));
    fs.add(new MaxLengthTextField(fs.getTextFieldId(), new PropertyModel<String>(address, "title")));
    return fs;
  }

  public FieldsetPanel addWebsite()
  {
    final FieldsetPanel fs = gridBuilder.newFieldset(getString("address.website"));
    fs.add(new MaxLengthTextField(fs.getTextFieldId(), new PropertyModel<String>(address, "website"))).setFieldType(FieldType.WEB_PAGE);
    return fs;
  }

  @SuppressWarnings("serial")
  public FieldsetPanel addOrganization()
  {
    final FieldsetPanel fs = gridBuilder.newFieldset(getString("organization"));
    fs.add(dependentFormComponents[2] = new PFAutoCompleteMaxLengthTextField(fs.getTextFieldId(), new PropertyModel<String>(address,
        "organization")) {

      @Override
      protected List<String> getChoices(final String input)
      {
        return addressDao.getAutocompletion("organization", input);
      }
    }.withMatchContains(true).withMinChars(2));
    return fs;
  }

  public FieldsetPanel addDivision()
  {
    final FieldsetPanel fs = gridBuilder.newFieldset(getString("address.division"));
    fs.add(new MaxLengthTextField(fs.getTextFieldId(), new PropertyModel<String>(address, "division")));
    return fs;
  }

  public FieldsetPanel addPosition()
  {
    final FieldsetPanel fs = gridBuilder.newFieldset(getString("address.positionText"));
    fs.add(new MaxLengthTextField(fs.getTextFieldId(), new PropertyModel<String>(address, "positionText")));
    return fs;
  }

  public FieldsetPanel addEmail()
  {
    final FieldsetPanel fs = gridBuilder.newFieldset(getString("email"), getString("address.business"));
    fs.add(new MaxLengthTextField(fs.getTextFieldId(), new PropertyModel<String>(address, "email"))).setFieldType(FieldType.E_MAIL);
    return fs;
  }

  public FieldsetPanel addPrivateEmail()
  {
    final FieldsetPanel fs = gridBuilder.newFieldset(getString("email"), getString("address.private"));
    fs.add(new MaxLengthTextField(fs.getTextFieldId(), new PropertyModel<String>(address, "privateEmail"))).setFieldType(FieldType.E_MAIL);
    return fs;
  }

  public FieldsetPanel addContactStatus()
  {
    final FieldsetPanel fs = gridBuilder.newFieldset(getString("address.contactStatus"));
    final LabelValueChoiceRenderer<ContactStatus> contactStatusChoiceRenderer = new LabelValueChoiceRenderer<ContactStatus>(form,
        ContactStatus.values());
    fs.addDropDownChoice(new PropertyModel<ContactStatus>(address, "contactStatus"), contactStatusChoiceRenderer.getValues(),
        contactStatusChoiceRenderer).setRequired(true).setNullValid(false);
    return fs;
  }

  public FieldsetPanel addAddressStatus()
  {
    final FieldsetPanel fs = gridBuilder.newFieldset(getString("address.addressStatus"));
    final LabelValueChoiceRenderer<AddressStatus> addressStatusChoiceRenderer = new LabelValueChoiceRenderer<AddressStatus>(form,
        AddressStatus.values());
    fs.addDropDownChoice(new PropertyModel<AddressStatus>(address, "addressStatus"), addressStatusChoiceRenderer.getValues(),
        addressStatusChoiceRenderer).setRequired(true).setNullValid(false);
    return fs;
  }

  @SuppressWarnings("serial")
  public FieldsetPanel addBirthday()
  {
    final FieldsetPanel fs = gridBuilder.newFieldset(getString("address.birthday"), true);
    fs.add(new DatePanel(fs.newChildId(), new PropertyModel<Date>(address, "birthday"), DatePanelSettings.get().withTargetType(
        java.sql.Date.class)));
    fs.add(new HtmlCommentPanel(fs.newChildId(), new Model<String>() {
      @Override
      public String getObject()
      {
        return WicketUtils.getUTCDate("birthday", address.getBirthday());
      }
    }));
    return fs;
  }

  public FieldsetPanel addLanguage()
  {
    final FieldsetPanel fs = gridBuilder.newFieldset(getString("language"), true);
    final LanguageField language = new LanguageField(fs.getTextFieldId(), new PropertyModel<Locale>(address, "communicationLanguage"));
    language.setFavoriteLanguages(addressDao.getUsedCommunicationLanguages());
    fs.add(language);
    fs.addKeyboardHelpIcon(getString("tooltip.autocomplete.language"));
    return fs;
  }

  public FieldsetPanel addFingerPrint()
  {
    // Finger print
    final FieldsetPanel fs = gridBuilder.newFieldset(getString("address.fingerprint"));
    fs.add(new MaxLengthTextField(fs.getTextFieldId(), new PropertyModel<String>(address, "fingerprint")));
    return fs;
  }

  public FieldsetPanel addPublicKey()
  {
    final FieldsetPanel fs = gridBuilder.newFieldset(getString("address.publicKey"));
    fs.add(new MaxLengthTextArea(TextAreaPanel.WICKET_ID, new PropertyModel<String>(address, "publicKey")));// .setAutogrow();
    return fs;
  }

  public FieldsetPanel addComment()
  {
    final FieldsetPanel fs = gridBuilder.newFieldset(getString("comment"));
    final MaxLengthTextArea comment = new MaxLengthTextArea(TextAreaPanel.WICKET_ID, new PropertyModel<String>(address, "comment"));
    fs.add(comment).setAutogrow();
    return fs;
  }

  public FieldsetPanel addPhoneNumber(final String property, final String labelKey, final String labelDescriptionKey,
      final String favoriteProperty, final FieldType fieldType)
  {
    final FieldsetPanel fs = gridBuilder.newFieldset(getString(labelKey), getString(labelDescriptionKey), true);
    final MaxLengthTextField phoneNumber = new MaxLengthTextField(fs.getTextFieldId(), new PropertyModel<String>(address, property));
    fs.add(phoneNumber).setFieldType(fieldType);
    phoneNumber.add(new PhoneNumberValidator());
    return fs;
  }

  @SuppressWarnings("serial")
  public FieldsetPanel addAddressText(final String addressType, final String addressTextProperty)
  {
    final FieldsetPanel fs = gridBuilder.newFieldset(getString("address.addressText"), addressType);
    fs.add(new PFAutoCompleteTextField<String>(fs.getTextFieldId(), new PropertyModel<String>(address, addressTextProperty)) {
      @Override
      protected List<String> getChoices(final String input)
      {
        return addressDao.getAutocompletion(addressTextProperty, input);
      }
    }.withMatchContains(true).withMinChars(2));
    return fs;
  }

  public FieldsetPanel addZipCode(final String zipCodeProperty)
  {
    final FieldsetPanel fs = gridBuilder.newFieldset(getString("address.zipCode"));
    fs.add(new MaxLengthTextField(fs.getTextFieldId(), new PropertyModel<String>(address, zipCodeProperty)));
    return fs;
  }

  public FieldsetPanel addCity(final String cityProperty)
  {
    final FieldsetPanel fs = gridBuilder.newFieldset(getString("address.city"));
    fs.add(new MaxLengthTextField(fs.getTextFieldId(), new PropertyModel<String>(address, cityProperty)));
    return fs;
  }

  public FieldsetPanel addCountry(final String countryProperty)
  {
    final FieldsetPanel fs = gridBuilder.newFieldset(getString("address.country"));
    fs.add(new MaxLengthTextField(fs.getTextFieldId(), new PropertyModel<String>(address, countryProperty)));
    return fs;
  }

  public FieldsetPanel addState(final String stateProperty)
  {
    final FieldsetPanel fs = gridBuilder.newFieldset(getString("address.state"));
    fs.add(new MaxLengthTextField(fs.getTextFieldId(), new PropertyModel<String>(address, stateProperty)));
    return fs;
  }

  public String getString(final String key)
  {
    return form.getString(key);
  }

  /**
   * @return true, if id of address is null (id not yet exists).
   */
  public boolean isNew()
  {
    return address.getId() == null;
  }

  public AddressParameters getBusinessAddressParameters()
  {
    return new AddressParameters(getString("address.heading.businessAddress"), "addressText", "zipCode", "city", "country", "state");
  }

  public AddressParameters getPostalAddressParameters()
  {
    return new AddressParameters(getString("address.heading.postalAddress"), "postalAddressText", "postalZipCode", "postalCity",
        "postalCountry", "postalState");
  }

  public AddressParameters getPrivateAddressParameters()
  {
    return new AddressParameters(getString("address.heading.privateAddress"), "privateAddressText", "privateZipCode", "privateCity",
        "privateCountry", "privateState");
  }

  class AddressParameters
  {
    AddressParameters(final String addressType, final String addressTextProperty, final String zipCodeProperty, final String cityProperty,
        final String countryProperty, final String stateProperty)
        {
      this.addressType = addressType;
      this.addressTextProperty = addressTextProperty;
      this.zipCodeProperty = zipCodeProperty;
      this.cityProperty = cityProperty;
      this.countryProperty = countryProperty;
      this.stateProperty = stateProperty;
        }

    final String addressType, addressTextProperty, zipCodeProperty, cityProperty, countryProperty, stateProperty;
  };
}
