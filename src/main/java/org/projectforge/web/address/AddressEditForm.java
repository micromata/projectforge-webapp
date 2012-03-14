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

import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.form.validation.IFormValidator;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.projectforge.address.AddressDO;
import org.projectforge.address.AddressDao;
import org.projectforge.address.AddressStatus;
import org.projectforge.address.ContactStatus;
import org.projectforge.address.FormOfAddress;
import org.projectforge.address.PersonalAddressDO;
import org.projectforge.address.PersonalAddressDao;
import org.projectforge.web.common.PhoneNumberValidator;
import org.projectforge.web.wicket.AbstractEditForm;
import org.projectforge.web.wicket.WicketUtils;
import org.projectforge.web.wicket.autocompletion.PFAutoCompleteMaxLengthTextField;
import org.projectforge.web.wicket.autocompletion.PFAutoCompleteTextField;
import org.projectforge.web.wicket.components.DatePanel;
import org.projectforge.web.wicket.components.DatePanelSettings;
import org.projectforge.web.wicket.components.LabelValueChoiceRenderer;
import org.projectforge.web.wicket.components.LanguageField;
import org.projectforge.web.wicket.components.MaxLengthTextArea;
import org.projectforge.web.wicket.components.MaxLengthTextField;
import org.projectforge.web.wicket.flowlayout.DivPanel;
import org.projectforge.web.wicket.flowlayout.DivType;
import org.projectforge.web.wicket.flowlayout.FieldType;
import org.projectforge.web.wicket.flowlayout.FieldsetPanel;
import org.projectforge.web.wicket.flowlayout.HtmlCommentPanel;
import org.projectforge.web.wicket.flowlayout.TextAreaPanel;

public class AddressEditForm extends AbstractEditForm<AddressDO, AddressEditPage>
{
  private static final long serialVersionUID = 3881031215413525517L;

  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(AddressEditForm.class);

  @SpringBean(name = "personalAddressDao")
  private PersonalAddressDao personalAddressDao;

  protected PersonalAddressDO personalAddress;

  private final String favorite;

  @SuppressWarnings("unchecked")
  private final TextField<String>[] dependentFormComponents = new TextField[3];

  public AddressEditForm(final AddressEditPage parentPage, final AddressDO data)
  {
    super(parentPage, data);
    personalAddress = null;
    if (isNew() == false) {
      personalAddress = personalAddressDao.getByAddressId(getData().getId());
    }
    if (personalAddress == null) {
      personalAddress = new PersonalAddressDO();
    }
    favorite = "*";
  }

  @SuppressWarnings("serial")
  @Override
  protected void init()
  {
    super.init();
    add(new IFormValidator() {
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
          error(getString("address.form.error.toFewFields"));
        }
      }
    });
    /* GRID8 - BLOCK */
    gridBuilder.newGrid8().newColumnsPanel().newColumnPanel(DivType.COL_50);
    {
      // Name
      final FieldsetPanel fs = gridBuilder.newFieldset(getString("name"));
      final MaxLengthTextField name = new MaxLengthTextField(fs.getTextFieldId(), new PropertyModel<String>(data, "name"));
      fs.add(dependentFormComponents[1] = name);
      if (isNew() == true) {
        WicketUtils.setFocus(name);
      }
    }
    {
      // First name
      final FieldsetPanel fs = gridBuilder.newFieldset(getString("firstName"));
      fs.add(dependentFormComponents[0] = new MaxLengthTextField(fs.getTextFieldId(), new PropertyModel<String>(data, "firstName")));
    }
    {
      // DropDownChoice form of address
      final FieldsetPanel fs = gridBuilder.newFieldset(getString("address.form"), true);
      final LabelValueChoiceRenderer<FormOfAddress> formChoiceRenderer = new LabelValueChoiceRenderer<FormOfAddress>(this,
          FormOfAddress.values());
      fs.addDropDownChoice(new PropertyModel<FormOfAddress>(data, "form"), formChoiceRenderer.getValues(), formChoiceRenderer)
      .setRequired(true).setNullValid(false);
      final DivPanel checkBoxPanel = fs.addNewCheckBoxDiv();
      checkBoxPanel.addCheckBox(new PropertyModel<Boolean>(personalAddress, "favoriteCard"), favorite,
          getString("address.tooltip.vCardList"));
    }
    {
      // Title
      final FieldsetPanel fs = gridBuilder.newFieldset(getString("address.title"));
      fs.add(new MaxLengthTextField(fs.getTextFieldId(), new PropertyModel<String>(data, "title")));
    }
    {
      // Website
      final FieldsetPanel fs = gridBuilder.newFieldset(getString("address.website"));
      fs.add(new MaxLengthTextField(fs.getTextFieldId(), new PropertyModel<String>(data, "website"))).setFieldType(FieldType.WEB_PAGE);
    }

    // /////////////////
    // Second box
    // /////////////////
    gridBuilder.newColumnPanel(DivType.COL_50);
    {
      // Organization
      final FieldsetPanel fs = gridBuilder.newFieldset(getString("organization"));
      fs.add(dependentFormComponents[2] = new PFAutoCompleteMaxLengthTextField(fs.getTextFieldId(), new PropertyModel<String>(data,
          "organization")) {

        @Override
        protected List<String> getChoices(final String input)
        {
          return getBaseDao().getAutocompletion("organization", input);
        }
      }.withMatchContains(true).withMinChars(2));
    }
    {
      // Division
      final FieldsetPanel fs = gridBuilder.newFieldset(getString("address.division"));
      fs.add(new MaxLengthTextField(fs.getTextFieldId(), new PropertyModel<String>(data, "division")));
    }
    {
      // Position
      final FieldsetPanel fs = gridBuilder.newFieldset(getString("address.positionText"));
      fs.add(new MaxLengthTextField(fs.getTextFieldId(), new PropertyModel<String>(data, "positionText")));
    }
    {
      // E-Mail
      final FieldsetPanel fs = gridBuilder.newFieldset(getString("email"), getString("address.business"));
      fs.add(new MaxLengthTextField(fs.getTextFieldId(), new PropertyModel<String>(data, "email"))).setFieldType(FieldType.E_MAIL);
    }
    {
      // Private E-Mail
      final FieldsetPanel fs = gridBuilder.newFieldset(getString("email"), getString("address.private"));
      fs.add(new MaxLengthTextField(fs.getTextFieldId(), new PropertyModel<String>(data, "privateEmail"))).setFieldType(FieldType.E_MAIL);
    }

    // /////////////////
    // Status
    // /////////////////
    gridBuilder.newGrid8().newColumnsPanel().newColumnPanel(DivType.COL_50);
    {
      // DropDownChoice contactStatus
      final FieldsetPanel fs = gridBuilder.newFieldset(getString("address.contactStatus"));
      final LabelValueChoiceRenderer<ContactStatus> contactStatusChoiceRenderer = new LabelValueChoiceRenderer<ContactStatus>(this,
          ContactStatus.values());
      fs.addDropDownChoice(new PropertyModel<ContactStatus>(data, "contactStatus"), contactStatusChoiceRenderer.getValues(),
          contactStatusChoiceRenderer).setRequired(true).setNullValid(false);
    }
    gridBuilder.newColumnPanel(DivType.COL_50);
    {
      // DropDownChoice addressStatus
      final FieldsetPanel fs = gridBuilder.newFieldset(getString("address.addressStatus"));
      final LabelValueChoiceRenderer<AddressStatus> addressStatusChoiceRenderer = new LabelValueChoiceRenderer<AddressStatus>(this,
          AddressStatus.values());
      fs.addDropDownChoice(new PropertyModel<AddressStatus>(data, "addressStatus"), addressStatusChoiceRenderer.getValues(),
          addressStatusChoiceRenderer).setRequired(true).setNullValid(false);
    }
    gridBuilder.newColumnsPanel().newColumnPanel(DivType.COL_50);
    {
      // Birthday
      final FieldsetPanel fs = gridBuilder.newFieldset(getString("address.birthday"), true);
      fs.add(new DatePanel(fs.newChildId(), new PropertyModel<Date>(data, "birthday"), DatePanelSettings.get().withTargetType(
          java.sql.Date.class)));
      fs.add(new HtmlCommentPanel(fs.newChildId(), new Model<String>() {
        @Override
        public String getObject()
        {
          return WicketUtils.getUTCDate("birthday", data.getBirthday());
        }
      }));
    }
    gridBuilder.newColumnPanel(DivType.COL_50);
    {
      // Language
      final FieldsetPanel fs = gridBuilder.newFieldset(getString("language"), true);
      final LanguageField language = new LanguageField(fs.getTextFieldId(), new PropertyModel<Locale>(data, "communicationLanguage"));
      language.setFavoriteLanguages(((AddressDao) getBaseDao()).getUsedCommunicationLanguages());
      fs.add(language);
      fs.addKeyboardHelpIcon(getString("tooltip.autocomplete.language"));
    }

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
    addAddress(getString("address.heading.businessAddress"), "addressText", "zipCode", "city", "country", "state");
    gridBuilder.newGrid8();
    addAddress(getString("address.heading.postalAddress"), "postalAddressText", "postalZipCode", "postalCity", "postalCountry",
        "postalState");
    gridBuilder.newGrid8();
    addAddress(getString("address.heading.privateAddress"), "privateAddressText", "privateZipCode", "privateCity", "privateCountry",
        "privateState");

    gridBuilder.newGrid8().newBlockPanel();
    {
      // Finger print
      final FieldsetPanel fs = gridBuilder.newFieldset(getString("address.fingerprint"));
      fs.add(new MaxLengthTextField(fs.getTextFieldId(), new PropertyModel<String>(data, "fingerprint")));
    }
    {
      // Public key
      final FieldsetPanel fs = gridBuilder.newFieldset(getString("address.publicKey"));
      fs.add(new MaxLengthTextArea(TextAreaPanel.WICKET_ID, new PropertyModel<String>(data, "publicKey")));// .setAutogrow();
    }

    gridBuilder.newGrid16(true).newBlockPanel();
    {
      // Comment
      final FieldsetPanel fs = gridBuilder.newFieldset(getString("comment"));
      final MaxLengthTextArea comment = new MaxLengthTextArea(TextAreaPanel.WICKET_ID, new PropertyModel<String>(data, "comment"));
      fs.add(comment).setAutogrow();
    }

  }

  private void addPhoneNumber(final String property, final String labelKey, final String labelDescriptionKey,
      final String favoriteProperty, final FieldType fieldType)
  {
    final FieldsetPanel fs = gridBuilder.newFieldset(getString(labelKey), getString(labelDescriptionKey), true);
    final MaxLengthTextField phoneNumber = new MaxLengthTextField(fs.getTextFieldId(), new PropertyModel<String>(data, property));
    fs.add(phoneNumber).setFieldType(fieldType);
    phoneNumber.add(new PhoneNumberValidator());
    final DivPanel checkBoxPanel = fs.addNewCheckBoxDiv();
    checkBoxPanel.addCheckBox(new PropertyModel<Boolean>(personalAddress, favoriteProperty), favorite).setTooltip(
        getString("address.tooltip.phonelist"));
  }

  @SuppressWarnings("serial")
  private void addAddress(final String addressType, final String addressTextProperty, final String zipCodeProperty,
      final String cityProperty, final String countryProperty, final String stateProperty)
  {
    gridBuilder.newColumnsPanel().newColumnPanel(null);
    {
      // Address
      final FieldsetPanel fs = gridBuilder.newFieldset(getString("address.addressText"), addressType);
      fs.add(new PFAutoCompleteTextField<String>(fs.getTextFieldId(), new PropertyModel<String>(data, addressTextProperty)) {
        @Override
        protected List<String> getChoices(final String input)
        {
          return getBaseDao().getAutocompletion(addressTextProperty, input);
        }
      }.withMatchContains(true).withMinChars(2));
    }
    gridBuilder.newColumnsPanel().newColumnPanel(DivType.COL_50);
    {
      // Zip code
      final FieldsetPanel fs = gridBuilder.newFieldset(getString("address.zipCode"));
      fs.add(new MaxLengthTextField(fs.getTextFieldId(), new PropertyModel<String>(data, zipCodeProperty)));
    }
    gridBuilder.newColumnPanel(DivType.COL_50);
    {
      // City
      final FieldsetPanel fs = gridBuilder.newFieldset(getString("address.city"));
      fs.add(new MaxLengthTextField(fs.getTextFieldId(), new PropertyModel<String>(data, cityProperty)));
    }
    gridBuilder.newColumnsPanel().newColumnPanel(DivType.COL_50);
    {
      // Country
      final FieldsetPanel fs = gridBuilder.newFieldset(getString("address.country"));
      fs.add(new MaxLengthTextField(fs.getTextFieldId(), new PropertyModel<String>(data, countryProperty)));
    }
    gridBuilder.newColumnPanel(DivType.COL_50);
    {
      // State
      final FieldsetPanel fs = gridBuilder.newFieldset(getString("address.state"));
      fs.add(new MaxLengthTextField(fs.getTextFieldId(), new PropertyModel<String>(data, stateProperty)));
    }
  }

  @Override
  protected Logger getLogger()
  {
    return log;
  }
}
