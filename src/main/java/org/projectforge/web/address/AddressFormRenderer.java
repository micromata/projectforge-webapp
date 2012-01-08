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

import static org.projectforge.web.wicket.layout.DropDownChoiceLPanel.SELECT_ID;
import static org.projectforge.web.wicket.layout.LayoutLength.FULL;
import static org.projectforge.web.wicket.layout.LayoutLength.HALF;
import static org.projectforge.web.wicket.layout.LayoutLength.ONEHALF;
import static org.projectforge.web.wicket.layout.LayoutLength.QUART;
import static org.projectforge.web.wicket.layout.LayoutLength.THREEQUART;
import static org.projectforge.web.wicket.layout.TextFieldLPanel.INPUT_ID;

import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.apache.commons.lang.StringUtils;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.projectforge.address.AddressDO;
import org.projectforge.address.AddressDao;
import org.projectforge.address.AddressStatus;
import org.projectforge.address.ContactStatus;
import org.projectforge.address.FormOfAddress;
import org.projectforge.address.PersonalAddressDO;
import org.projectforge.common.BeanHelper;
import org.projectforge.common.DateHelper;
import org.projectforge.common.DatePrecision;
import org.projectforge.common.StringHelper;
import org.projectforge.web.wicket.WicketUtils;
import org.projectforge.web.wicket.autocompletion.PFAutoCompleteTextField;
import org.projectforge.web.wicket.components.DatePanel;
import org.projectforge.web.wicket.components.DatePanelSettings;
import org.projectforge.web.wicket.components.LabelValueChoiceRenderer;
import org.projectforge.web.wicket.components.LanguageField;
import org.projectforge.web.wicket.layout.AbstractFormRenderer;
import org.projectforge.web.wicket.layout.DateFieldLPanel;
import org.projectforge.web.wicket.layout.FieldType;
import org.projectforge.web.wicket.layout.GroupLPanel;
import org.projectforge.web.wicket.layout.GroupMobileLPanel;
import org.projectforge.web.wicket.layout.IField;
import org.projectforge.web.wicket.layout.LayoutContext;
import org.projectforge.web.wicket.layout.LayoutLength;
import org.projectforge.web.wicket.layout.PanelContext;
import org.projectforge.web.wicket.layout.TextFieldLPanel;

public class AddressFormRenderer extends AbstractFormRenderer
{
  private static final long serialVersionUID = -9175062586210446142L;

  private final AddressDao addressDao;

  private final AddressDO data;

  protected PersonalAddressDO personalAddress;

  private DropDownChoice<FormOfAddress> formChoice;

  private TextField<String> businessPhoneField, faxField, mobilePhoneField, privatePhoneField, privateMobilePhoneField;

  private final static LayoutLength LABEL_LENGTH = LayoutLength.HALF;

  private final static LayoutLength VALUE_LENGTH = LayoutLength.FULL;

  public AddressFormRenderer(final MarkupContainer container, final LayoutContext layoutContext, final AddressDao addressDao,
      final AddressDO data, final PersonalAddressDO personalAddress)
  {
    super(container, layoutContext);
    this.data = data;
    this.addressDao = addressDao;
    this.personalAddress = personalAddress;
  }

  @Override
  public void add()
  {
    final String title = StringHelper.listToString(" ", data.getTitle(), data.getFirstName(), data.getName());
    doPanel.newFieldSetPanel(isNew() == false ? title : getString("address.heading.personalData"));
    if (isMobile() == true) {
      if (isReadonly() == true) {
        // Append at the end.
      } else {
        addPersonalData(title);
      }
    } else {
      addPersonalData(title);
      addPublicKeyAndFingerprint();
    }

    // *** Business Contact ***
    final String businessContactTitle;
    if (isMobileReadonly() == true) {
      businessContactTitle = title;
    } else {
      businessContactTitle = getString("address.heading.businessContact");
    }
    doPanel.newFieldSetPanel(businessContactTitle);

    addBusinessData();
    addBusinessPhones();
    addBusinesAddress();
    addPostalAddress();

    if (isReadonly() == false && isMobile() == false) {
      doPanel.newGroupPanel(getString("address.communication"));
      final LanguageField language = new LanguageField(TextFieldLPanel.INPUT_ID, new PropertyModel<Locale>(data, "communicationLanguage"));
      language.setFavoriteLanguages(addressDao.getUsedCommunicationLanguages());
      doPanel.addTextField(language, new PanelContext(FULL, getString("language"), LABEL_LENGTH)
      .setTooltip(getString("tooltip.autocomplete.language")));
    }

    // *** Private Contact ***
    doPanel.newFieldSetPanel(getString("address.heading.privateContact"));
    addPrivateEMail();

    addPrivatePhones();
    addPrivateAddress();

    if (isMobile() == true) {
      if (isReadonly() == true) {
        addPersonalData(title);
      }
      addPublicKeyAndFingerprint();
    }
  }

  /**
   * Adds the fields of business address: address, zip, city, country, state.
   * @param fieldSetPanel
   */
  public void addBusinesAddress()
  {
    if (isReadonly() == false || data.hasDefaultAddress() == true) {
      addAddress("address.heading.businessAddress", "addressText", "zipCode", "city", "country", "state");
    }
  }

  /**
   * Adds the fields of postal address: address, zip, city, country, state.
   * @param fieldSetPanel
   */
  public void addPostalAddress()
  {
    if (isReadonly() == false || data.hasPostalAddress() == true) {
      final GroupLPanel groupPanel = addAddress("address.heading.postalAddress", "postalAddressText", "postalZipCode", "postalCity",
          "postalCountry", "postalState");
      if (isMobile() == true && data.hasPostalAddress() == false) {
        ((GroupMobileLPanel) groupPanel).setCollapsed();
      }
    }
  }

  /**
   * Adds the fields of private address: address, zip, city, country, state.
   * @param fieldSetPanel
   */
  public void addPrivateAddress()
  {
    if (isReadonly() == false || data.hasPrivateAddress() == true) {
      addAddress("address.heading.privateAddress", "privateAddressText", "privateZipCode", "privateCity", "privateCountry", "privateState");
    }
  }

  /**
   * Adds the fields of business phones: business, fax, mobile.
   * @param fieldSetPanel
   */
  public void addBusinessPhones()
  {
    final String phoneListTooltip = getString("address.tooltip.phonelist");
    doPanel.newGroupPanel(getString("address.phoneType.business"));
    businessPhoneField = addPhoneNumber("businessPhone", "address.phone", "favoriteBusinessPhone", false, phoneListTooltip, true);
    faxField = addPhoneNumber("fax", "address.phoneType.fax", "favoriteFax", false, phoneListTooltip, false);
    mobilePhoneField = addPhoneNumber("mobilePhone", "address.phoneType.mobile", "favoriteMobilePhone", true, phoneListTooltip, false);
  }

  /**
   * Adds the fields of private phones: phone, mobile.
   * @param fieldSetPanel
   */
  public void addPrivatePhones()
  {
    final String phoneListTooltip = getString("address.tooltip.phonelist");
    doPanel.newGroupPanel(getString("address.phoneType.private"));
    privatePhoneField = addPhoneNumber("privatePhone", "address.phone", "favoritePrivatePhone", false, phoneListTooltip, true);
    privateMobilePhoneField = addPhoneNumber("privateMobilePhone", "address.phoneType.mobile", "favoritePrivateMobilePhone", true,
        phoneListTooltip, false);
  }

  /**
   * Adds the fields form, name, first name, contact status, birthday, comment
   * @param fieldSetPanel
   * @param title
   */
  @SuppressWarnings( { "unchecked", "serial"})
  public void addPersonalData(final String title)
  {
    PanelContext ctx;
    doPanel.newGroupPanel();
    // add(new Label("task", taskFormatter.getTaskPath(data.getTaskId(), true, OutputType.HTML)).setEscapeModelStrings(false));
    {
      // DropDownChoice form of address
      final LabelValueChoiceRenderer<FormOfAddress> formChoiceRenderer = new LabelValueChoiceRenderer<FormOfAddress>(container,
          FormOfAddress.values());
      formChoice = new DropDownChoice(SELECT_ID, new PropertyModel(data, "form"), formChoiceRenderer.getValues(), formChoiceRenderer);
      formChoice.setNullValid(false).setRequired(true);
      doPanel.addDropDownChoice(formChoice, new PanelContext(data, "form", THREEQUART, getString("address.form"), LABEL_LENGTH));
    }
    if (isMobile() == false) {
      final String tooltip = getString("address.tooltip.vCardList");
      doPanel.addCheckBox(new PanelContext(personalAddress, "favoriteCard").setTooltip(tooltip));
    }
    doPanel.addTextField(new PanelContext(data, "title", VALUE_LENGTH, getString("address.title"), LABEL_LENGTH).setStrong());
    doPanel.addTextField(new PanelContext(data, "firstName", VALUE_LENGTH, getString("firstName"), LABEL_LENGTH).setStrong());
    ctx = new PanelContext(data, "name", VALUE_LENGTH, getString("name"), LABEL_LENGTH).setStrong().setRequired();
    if (isNew() == true) {
      ctx.setFocus();
    }
    doPanel.addTextField(ctx);
    {
      // DropDownChoice contactStatus
      final LabelValueChoiceRenderer<ContactStatus> contactStatusChoiceRenderer = new LabelValueChoiceRenderer<ContactStatus>(container,
          ContactStatus.values());
      final DropDownChoice contactStatusChoice = new DropDownChoice(SELECT_ID, new PropertyModel(data, "contactStatus"),
          contactStatusChoiceRenderer.getValues(), contactStatusChoiceRenderer);
      contactStatusChoice.setNullValid(false).setRequired(true);
      doPanel.addDropDownChoice(contactStatusChoice, new PanelContext(data, "contactStatus", THREEQUART,
          getString("address.contactStatus"), LABEL_LENGTH));
    }
    if (isReadonly() == true) {
      doPanel.addDateFieldPanel(DatePrecision.DAY, new PanelContext(data, "birthday", HALF, getString("address.birthday"), LABEL_LENGTH));
    } else {
      final DatePanel birthdayPanel = new DatePanel(DateFieldLPanel.DATE_FIELD_ID, new PropertyModel<Date>(data, "birthday"),
          new DatePanelSettings().withTargetType(java.sql.Date.class));
      doPanel.addDateFieldPanel(birthdayPanel, new PanelContext(HALF, getString("address.birthday"), LABEL_LENGTH));
      WicketUtils.addTooltip(birthdayPanel.getDateField(), new Model<String>() {
        @Override
        public String getObject()
        {
          return DateHelper.formatAsUTC(data.getBirthday());
        }
      });
    }
    ctx = new PanelContext(data, "comment", ONEHALF, getString("comment"), ONEHALF).setBreakBetweenLabelAndField(true).setCssStyle(
    "height: 20em;");
    if (layoutContext.isNew() == false) {
      ctx.setFocus();
    }
    doPanel.addTextArea(ctx);
  }

  /**
   * Adds the fields: publicKey, fingerPrint
   * @param fieldSetPanel
   */
  public void addPublicKeyAndFingerprint()
  {
    if (isMobile() == true) {
      final GroupMobileLPanel groupMobilePanel = (GroupMobileLPanel) doPanel.newGroupPanel(getString("address.publicKey"));
      groupMobilePanel.setCollapsed();
    }
    doPanel.addTextField(new PanelContext(data, "fingerprint", ONEHALF, getString("address.fingerprint"), ONEHALF)
    .setBreakBetweenLabelAndField(true));
    doPanel.addTextArea(new PanelContext(data, "publicKey", ONEHALF, getString("address.publicKey"), ONEHALF).setBreakBetweenLabelAndField(
        true).setCssStyle("height: 5em;"));
  }

  /**
   * Adds the fields: organization, division, position, address status, e-mail and web-site.
   * @param fieldSetPanel
   */
  @SuppressWarnings( { "serial", "unchecked"})
  public void addBusinessData()
  {
    if (isMobileReadonly()) {
      doPanel.addTextField(new PanelContext(data, "organization", VALUE_LENGTH, getString("organization"), LABEL_LENGTH));
    } else {
      final PFAutoCompleteTextField<String> organizationField = new PFAutoCompleteTextField<String>(INPUT_ID, new PropertyModel<String>(
          data, "organization")) {
        @Override
        protected List<String> getChoices(final String input)
        {
          return addressDao.getAutocompletion("organization", input);
        }
      }.withMatchContains(true).withMinChars(2);
      doPanel.addTextField(organizationField, new PanelContext(VALUE_LENGTH, getString("organization"), LABEL_LENGTH).setStrong());
    }
    doPanel.addTextField(new PanelContext(data, "division", VALUE_LENGTH, getString("address.division"), LABEL_LENGTH));
    doPanel.addTextField(new PanelContext(data, "positionText", VALUE_LENGTH, getString("address.positionText"), LABEL_LENGTH));
    {
      // DropDownChoice addressStatus
      final LabelValueChoiceRenderer<AddressStatus> addressStatusChoiceRenderer = new LabelValueChoiceRenderer<AddressStatus>(container,
          AddressStatus.values());
      final DropDownChoice addressStatusChoice = new DropDownChoice(SELECT_ID, new PropertyModel(data, "addressStatus"),
          addressStatusChoiceRenderer.getValues(), addressStatusChoiceRenderer);
      addressStatusChoice.setNullValid(false).setRequired(true);
      doPanel.addDropDownChoice(addressStatusChoice, new PanelContext(data, "addressStatus", THREEQUART,
          getString("address.addressStatus"), LABEL_LENGTH));
    }
    doPanel.addTextField(new PanelContext(data, "email", VALUE_LENGTH, getString("email"), LABEL_LENGTH).setFieldType(FieldType.E_MAIL)
        .setStrong());
    doPanel.addTextField(new PanelContext(data, "website", VALUE_LENGTH, getString("address.website"), LABEL_LENGTH)
    .setFieldType(FieldType.WEB_PAGE));
  }

  /**
   * Adds the fields: private e-mail.
   * @param fieldSetPanel
   */
  public void addPrivateEMail()
  {
    doPanel.newGroupPanel(isMobile() == true ? getString("address.privateEmail") : null);
    doPanel.addTextField(new PanelContext(data, "privateEmail", VALUE_LENGTH, getString("email"), LABEL_LENGTH).setFieldType(
        FieldType.E_MAIL).setStrong());
  }

  protected GroupLPanel addAddress(final String heading, final String addressTextProperty, final String zipCodeProperty,
      final String cityProperty, final String countryProperty, final String stateProperty)
  {
    final String zipCode, city;
    if (isReadonly() == true) {
      zipCode = (String) BeanHelper.getProperty(data, zipCodeProperty);
      city = (String) BeanHelper.getProperty(data, cityProperty);
    } else {
      zipCode = city = null;
    }
    final GroupLPanel groupPanel = doPanel.newGroupPanel(getString(heading));
    if (isMobileReadonly() == true) {
      doPanel.addReadonlyTextField(new PanelContext(data, addressTextProperty).setLabel(getString("address.addressText")));
      doPanel.addReadonlyTextField(zipCode + " " + city, new PanelContext(null, getString("address.city"), null));
      doPanel.addReadonlyTextField(new PanelContext(data, countryProperty).setLabel(getString("address.country")));
      doPanel.addReadonlyTextField(new PanelContext(data, stateProperty).setLabel(getString("address.state")));
    } else {
      @SuppressWarnings("serial")
      final PFAutoCompleteTextField<String> addressTextField = new PFAutoCompleteTextField<String>(INPUT_ID, new PropertyModel<String>(
          data, addressTextProperty)) {
        @Override
        protected List<String> getChoices(final String input)
        {
          return addressDao.getAutocompletion(addressTextProperty, input);
        }
      }.withMatchContains(true).withMinChars(2);
      doPanel.addTextField(addressTextField, new PanelContext(VALUE_LENGTH, getString("address.addressText"), LABEL_LENGTH));
      if (isMobile() == true) {
        doPanel.addTextField(new PanelContext(data, zipCodeProperty, QUART, getString("address.zipCode"), LABEL_LENGTH));
        doPanel.addTextField(new PanelContext(data, cityProperty, VALUE_LENGTH, getString("address.city"), LABEL_LENGTH));
        doPanel.addTextField(new PanelContext(data, countryProperty, VALUE_LENGTH, getString("address.country"), LABEL_LENGTH));
        doPanel.addTextField(new PanelContext(data, stateProperty, VALUE_LENGTH, getString("address.state"), LABEL_LENGTH));
      } else {
        doPanel.addTextField(new PanelContext(data, zipCodeProperty, QUART, getString("address.zipCode") + "/" + getString("address.city"),
            LABEL_LENGTH));
        doPanel.addTextField(new PanelContext(data, cityProperty, THREEQUART).setLabel(getString("address.zipCode")));
        doPanel.addTextField(new PanelContext(data, countryProperty, HALF, getString("address.country") + "/" + getString("address.state"),
            LABEL_LENGTH));
        doPanel.addTextField(new PanelContext(data, stateProperty, HALF).setLabel(getString("address.state")));
      }
    }
    return groupPanel;
  }

  private TextField<String> addPhoneNumber(final String property, final String labelKey, final String favoriteProperty,
      final boolean mobileNumber, final String phoneListTooltip, final boolean first)
      {
    if (isMobile() == false) {
      final IField field = doPanel.addTextField(new PanelContext(data, property, THREEQUART, getString(labelKey), LABEL_LENGTH));
      doPanel.addCheckBox(new PanelContext(personalAddress, favoriteProperty).setTooltip(phoneListTooltip));
      @SuppressWarnings("unchecked")
      final TextField<String> textField = (TextField<String>) ((TextFieldLPanel) field).getTextField();
      return textField;
    } else {
      final String number = (String) BeanHelper.getProperty(data, property);
      if (isMobileReadonly() == true && StringUtils.isBlank(number) == true) {
        return null;
      }
      final IField field = doPanel.addTextField(new PanelContext(data, property, VALUE_LENGTH, getString(labelKey), LABEL_LENGTH)
      .setFieldType(mobileNumber == true ? FieldType.MOBILE_PHONE_NO : FieldType.PHONE_NO).setStrong());
      if (isReadonly() == false) {
        @SuppressWarnings("unchecked")
        final TextField<String> textField = (TextField<String>) ((TextFieldLPanel) field).getTextField();
        return textField;
      }
    }
    return null;
      }

  protected void validation()
  {
    businessPhoneField.validate();
    faxField.validate();
    mobilePhoneField.validate();
    privatePhoneField.validate();
    privateMobilePhoneField.validate();
    validatePhoneNumber(businessPhoneField);
    validatePhoneNumber(faxField);
    validatePhoneNumber(mobilePhoneField);
    validatePhoneNumber(privatePhoneField);
    validatePhoneNumber(privateMobilePhoneField);
  }

  protected void validatePhoneNumber(final FormComponent<String> component)
  {
    if (StringHelper.checkPhoneNumberFormat(component.getConvertedInput()) == false) {
      component.error(getString("address.error.phone.invalidFormat"));
    }
  }
}
