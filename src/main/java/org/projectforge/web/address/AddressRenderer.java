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

import static org.projectforge.web.wicket.layout.DateFieldLPanel.DATE_FIELD_ID;
import static org.projectforge.web.wicket.layout.DropDownChoiceLPanel.SELECT_ID;
import static org.projectforge.web.wicket.layout.LayoutLength.FULL;
import static org.projectforge.web.wicket.layout.LayoutLength.HALF;
import static org.projectforge.web.wicket.layout.LayoutLength.ONEHALF;
import static org.projectforge.web.wicket.layout.LayoutLength.QUART;
import static org.projectforge.web.wicket.layout.LayoutLength.THREEQUART;
import static org.projectforge.web.wicket.layout.TextFieldLPanel.INPUT_ID;

import java.util.Date;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.repeater.RepeatingView;
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
import org.projectforge.common.StringHelper;
import org.projectforge.web.calendar.DateTimeFormatter;
import org.projectforge.web.mobile.ActionLinkPanel;
import org.projectforge.web.mobile.ActionLinkType;
import org.projectforge.web.wicket.ImageDef;
import org.projectforge.web.wicket.WicketUtils;
import org.projectforge.web.wicket.autocompletion.PFAutoCompleteTextField;
import org.projectforge.web.wicket.components.DatePanel;
import org.projectforge.web.wicket.components.DatePanelSettings;
import org.projectforge.web.wicket.components.LabelValueChoiceRenderer;
import org.projectforge.web.wicket.layout.AbstractRenderer;
import org.projectforge.web.wicket.layout.FieldSetLPanel;
import org.projectforge.web.wicket.layout.GroupLPanel;
import org.projectforge.web.wicket.layout.GroupMobileLPanel;
import org.projectforge.web.wicket.layout.LabelValueTableLPanel;
import org.projectforge.web.wicket.layout.LayoutContext;
import org.projectforge.web.wicket.layout.RepeaterLabelLPanel;
import org.projectforge.web.wicket.layout.TextAreaLPanel;
import org.projectforge.web.wicket.layout.TextFieldLPanel;

public class AddressRenderer extends AbstractRenderer
{
  private static final long serialVersionUID = -9175062586210446142L;

  private final AddressDao addressDao;

  private AddressDO data;

  protected PersonalAddressDO personalAddress;

  private DropDownChoice<FormOfAddress> formChoice;

  private TextField<String> businessPhoneField, faxField, mobilePhoneField, privatePhoneField, privateMobilePhoneField;

  public AddressRenderer(final MarkupContainer container, final LayoutContext layoutContext, final AddressDao addressDao,
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
    final RepeatingView fieldSetRepeater = new RepeatingView("fieldSetRepeater");
    add(fieldSetRepeater);

    final String title = StringHelper.listToString(" ", data.getTitle(), data.getFirstName(), data.getName());
    FieldSetLPanel fieldSetPanel;
    if (isMobileReadonly() == true) {
      // Append at the end.
    } else {
      fieldSetPanel = createFieldSetPanel(fieldSetRepeater.newChildId(), isNew() == false ? title
          : getString("address.heading.personalData"));
      fieldSetRepeater.add(fieldSetPanel);
      addPersonalData(fieldSetPanel, title);
      addPublicKeyAndFingerprint(fieldSetPanel);
    }

    // *** Business Contact ***
    final String businessContactTitle;
    if (isMobileReadonly() == true) {
      businessContactTitle = title;
    } else {
      businessContactTitle = getString("address.heading.businessContact");
    }
    fieldSetPanel = createFieldSetPanel(fieldSetRepeater.newChildId(), businessContactTitle);
    fieldSetRepeater.add(fieldSetPanel);

    addBusinessData(fieldSetPanel);
    addBusinessPhones(fieldSetPanel);
    addBusinesAddress(fieldSetPanel);
    addPostalAddress(fieldSetPanel);

    // *** Private Contact ***
    doPanel.newFieldSetPanel(getString("address.heading.privateContact"));
    addPrivateEMail();

    addPrivatePhones(fieldSetPanel);
    addPrivateAddress(fieldSetPanel);

    if (isMobileReadonly() == true) {
      addPersonalData(fieldSetPanel, title);
      addPublicKeyAndFingerprint(fieldSetPanel);
    }
  }

  /**
   * Adds the fields of business address: address, zip, city, country, state.
   * @param fieldSetPanel
   */
  public void addBusinesAddress(final FieldSetLPanel fieldSetPanel)
  {
    if (isReadonly() == false || data.hasDefaultAddress() == true) {
      addAddress(fieldSetPanel, "address.heading.businessAddress", "addressText", "zipCode", "city", "country", "state");
    }
  }

  /**
   * Adds the fields of postal address: address, zip, city, country, state.
   * @param fieldSetPanel
   */
  public void addPostalAddress(final FieldSetLPanel fieldSetPanel)
  {
    if (isReadonly() == false || data.hasPostalAddress() == true) {
      addAddress(fieldSetPanel, "address.heading.postalAddress", "postalAddressText", "postalZipCode", "postalCity", "postalCountry",
          "postalState");
    }
  }

  /**
   * Adds the fields of private address: address, zip, city, country, state.
   * @param fieldSetPanel
   */
  public void addPrivateAddress(final FieldSetLPanel fieldSetPanel)
  {
    if (isReadonly() == false || data.hasPrivateAddress() == true) {
      addAddress(fieldSetPanel, "address.heading.privateAddress", "privateAddressText", "privateZipCode", "privateCity", "privateCountry",
          "privateState");
    }
  }

  /**
   * Adds the fields of business phones: business, fax, mobile.
   * @param fieldSetPanel
   */
  public void addBusinessPhones(final FieldSetLPanel fieldSetPanel)
  {
    final String phoneListTooltip = getString("address.tooltip.phonelist");
    final GroupLPanel groupPanel = createGroupPanel(fieldSetPanel.newChildId()).setHeading(getString("address.phoneType.business"));
    fieldSetPanel.add(groupPanel);
    if (isMobileReadonly() == true) {
      final LabelValueTableLPanel labelValueTablePanel = createLabelValueTablePanel(groupPanel.newChildId());
      addPhoneNumber(labelValueTablePanel, "address.phone", data.getBusinessPhone(), false);
      addPhoneNumber(labelValueTablePanel, "address.phoneType.fax", data.getFax(), false);
      addPhoneNumber(labelValueTablePanel, "address.phoneType.mobile", data.getMobilePhone(), true);
      if (labelValueTablePanel != null && labelValueTablePanel.hasChildren() == true) {
        groupPanel.add(labelValueTablePanel);
      }
    } else {
      businessPhoneField = addPhoneNumber(groupPanel, "businessPhone", "address.phone", "favoriteBusinessPhone", phoneListTooltip);
      faxField = addPhoneNumber(groupPanel, "fax", "address.phoneType.fax", "favoriteFax", phoneListTooltip);
      mobilePhoneField = addPhoneNumber(groupPanel, "mobilePhone", "address.phoneType.mobile", "favoriteMobilePhone", phoneListTooltip);
    }
    if (groupPanel.hasChildren() == false) {
      groupPanel.setVisible(false);
    }
  }

  /**
   * Adds the fields of private phones: phone, mobile.
   * @param fieldSetPanel
   */
  public void addPrivatePhones(final FieldSetLPanel fieldSetPanel)
  {
    final String phoneListTooltip = getString("address.tooltip.phonelist");
    final GroupLPanel groupPanel = createGroupPanel(fieldSetPanel.newChildId()).setHeading(getString("address.phoneType.private"));
    fieldSetPanel.add(groupPanel);
    if (isMobileReadonly() == true) {
      final LabelValueTableLPanel labelValueTablePanel = createLabelValueTablePanel(groupPanel.newChildId());
      addPhoneNumber(labelValueTablePanel, "address.phone", data.getPrivatePhone(), false);
      addPhoneNumber(labelValueTablePanel, "address.phoneType.mobile", data.getPrivateMobilePhone(), true);
      if (labelValueTablePanel != null && labelValueTablePanel.hasChildren() == true) {
        groupPanel.add(labelValueTablePanel);
      }
    } else {
      privatePhoneField = addPhoneNumber(groupPanel, "privatePhone", "address.phone", "favoritePrivatePhone", phoneListTooltip);
      privateMobilePhoneField = addPhoneNumber(groupPanel, "privateMobilePhone", "address.phoneType.mobile", "favoritePrivateMobilePhone",
          phoneListTooltip);
    }
    if (groupPanel.hasChildren() == false) {
      groupPanel.setVisible(false);
    }
  }

  /**
   * Adds the fields form, name, first name, contact status, birthday, comment
   * @param fieldSetPanel
   * @param title
   */
  @SuppressWarnings( { "unchecked", "serial"})
  public void addPersonalData(final FieldSetLPanel fieldSetPanel, final String title)
  {
    GroupLPanel groupPanel = createGroupPanel(fieldSetPanel.newChildId());
    fieldSetPanel.add(groupPanel);
    String label;

    // add(new Label("task", taskFormatter.getTaskPath(data.getTaskId(), true, OutputType.HTML)).setEscapeModelStrings(false));
    if (isReadonly() == true) {
    } else {
      // DropDownChoice form of address
      final LabelValueChoiceRenderer<FormOfAddress> formChoiceRenderer = new LabelValueChoiceRenderer<FormOfAddress>(container,
          FormOfAddress.values());
      formChoice = new DropDownChoice(SELECT_ID, new PropertyModel(data, "form"), formChoiceRenderer.getValues(), formChoiceRenderer);
      formChoice.setNullValid(false).setRequired(true);
      label = getString("address.form");
      if (isMobile() == false) {
        groupPanel.add(createLabelPanel(groupPanel.newChildId(), HALF, label, formChoice, true));
      }
      groupPanel.add(createDropDownChoicePanel(groupPanel.newChildId(), THREEQUART, formChoice).setLabel(label));
      final RepeaterLabelLPanel repeaterPanel = createRepeaterLabelPanel(groupPanel.newChildId());
      groupPanel.add(repeaterPanel);
      if (isMobile() == false) {
        final String tooltip = getString("address.tooltip.vCardList");
        repeaterPanel.add(createCheckBoxPanel(repeaterPanel.newChildId(), personalAddress, "favoriteCard").setTooltip(tooltip));
        repeaterPanel.add(createImagePanel(repeaterPanel.newChildId(), ImageDef.HELP, tooltip));
      }
      groupPanel.addMaxLengthTextField(data, "title", "address.title", THREEQUART).setStrong();
      groupPanel.addMaxLengthTextField(data, "firstName", "firstName", FULL).setStrong();
      final TextFieldLPanel nameTextFieldPanel = groupPanel.addMaxLengthTextField(data, "name", "name", FULL).setStrong().setRequired();
      if (isNew() == true) {
        nameTextFieldPanel.setFocus();
      }
    }

    final LabelValueTableLPanel labelValueTablePanel; // Only used for mobile pages.
    if (isMobileReadonly() == true) {
      labelValueTablePanel = createLabelValueTablePanel(groupPanel.newChildId());
      final ContactStatus contactStatus = data.getContactStatus();
      if (contactStatus != null) {
        addLabelValueRow(labelValueTablePanel, getString("address.contactStatus"), getString(contactStatus.getI18nKey()));
      }
      if (data.getBirthday() != null) {
        addLabelValueRow(labelValueTablePanel, getString("address.birthday"), DateTimeFormatter.instance().getFormattedDate(
            data.getBirthday()));
      }
    } else {
      labelValueTablePanel = null; // Only used for mobile pages.
      // DropDownChoice contactStatus
      final LabelValueChoiceRenderer<ContactStatus> contactStatusChoiceRenderer = new LabelValueChoiceRenderer<ContactStatus>(container,
          ContactStatus.values());
      final DropDownChoice contactStatusChoice = new DropDownChoice(SELECT_ID, new PropertyModel(data, "contactStatus"),
          contactStatusChoiceRenderer.getValues(), contactStatusChoiceRenderer);
      contactStatusChoice.setNullValid(false).setRequired(true);
      label = getString("address.contactStatus");
      if (isMobile() == false) {
        groupPanel.add(createLabelPanel(groupPanel.newChildId(), HALF, label, contactStatusChoice, true));
      }
      groupPanel.add(createDropDownChoicePanel(groupPanel.newChildId(), THREEQUART, contactStatusChoice).setLabel(label));
      final DatePanel birthdayPanel = new DatePanel(DATE_FIELD_ID, new PropertyModel<Date>(data, "birthday"), new DatePanelSettings()
          .withTargetType(java.sql.Date.class));
      groupPanel.add(createLabelPanel(groupPanel.newChildId(), HALF, getString("address.birthday"), birthdayPanel.getDateField(), true));
      groupPanel.add(createDateFieldPanel(groupPanel.newChildId(), HALF, birthdayPanel));
      WicketUtils.addTooltip(birthdayPanel.getDateField(), new Model<String>() {
        @Override
        public String getObject()
        {
          return DateHelper.formatAsUTC(data.getBirthday());
        }
      });
    }
    if (labelValueTablePanel != null && labelValueTablePanel.hasChildren() == true) {
      groupPanel.add(labelValueTablePanel);
    }
    if (isMobileReadonly() == true) {
      if (StringUtils.isNotBlank(data.getComment()) == true) {
        groupPanel.add(createLabelValueHeadingPanel(groupPanel.newChildId(), getString("comment")));
        groupPanel.add(createValuePanel(groupPanel.newChildId(), data.getComment()));
      }
    } else {
      // add(new CheckBox("imageBroschure", new PropertyModel<Boolean>(data, "imageBroschure")));
      final TextAreaLPanel commentTextAreaPanel = groupPanel.addMaxLengthTextArea(data, "comment", "comment", ONEHALF);
      commentTextAreaPanel.setBreakBefore().setStyle("height: 20em;");
      if (layoutContext.isNew() == false) {
        commentTextAreaPanel.setFocus();
      }
    }
    if (groupPanel.hasChildren() == false) {
      groupPanel.setVisible(false);
    }
  }

  /**
   * Adds the fields: publicKey, fingerPrint
   * @param fieldSetPanel
   */
  public void addPublicKeyAndFingerprint(final FieldSetLPanel fieldSetPanel)
  {
    final GroupLPanel groupPanel = createGroupPanel(fieldSetPanel.newChildId());
    fieldSetPanel.add(groupPanel);
    if (isMobileReadonly() == true) {
      groupPanel.setHeading(getString("address.publicKey"));
      ((GroupMobileLPanel) groupPanel).setCollapsed();
      if (StringUtils.isNotBlank(data.getFingerprint()) == true) {
        final LabelValueTableLPanel labelValueTablePanel = createLabelValueTablePanel(groupPanel.newChildId());
        groupPanel.add(labelValueTablePanel);
        labelValueTablePanel.add(getString("address.fingerprint"), data.getFingerprint());
      }
      if (StringUtils.isNotBlank(data.getPublicKey()) == true) {
        groupPanel.add(createLabelValueHeadingPanel(groupPanel.newChildId(), getString("address.publicKey")));
        groupPanel.add(createValuePanel(groupPanel.newChildId(), data.getPublicKey()));
      }
    } else {
      groupPanel.addMaxLengthTextArea(data, "publicKey", "address.publicKey", ONEHALF).setBreakBefore();
      groupPanel.addMaxLengthTextField(data, "fingerprint", "address.fingerprint", ONEHALF).setBreakBefore();
    }
    if (groupPanel.hasChildren() == false) {
      groupPanel.setVisible(false);
    }
  }

  /**
   * Adds the fields: organization, division, position, address status, e-mail and web-site.
   * @param fieldSetPanel
   */
  @SuppressWarnings( { "serial", "unchecked"})
  public void addBusinessData(final FieldSetLPanel fieldSetPanel)
  {
    final GroupLPanel groupPanel = createGroupPanel(fieldSetPanel.newChildId());
    fieldSetPanel.add(groupPanel);
    String label;
    final LabelValueTableLPanel labelValueTablePanel; // Only used for mobile devices.
    if (isMobileReadonly()) {
      labelValueTablePanel = createLabelValueTablePanel(groupPanel.newChildId());
      groupPanel.add(labelValueTablePanel);
      addLabelValueRow(labelValueTablePanel, getString("organization"), data.getOrganization());
    } else {
      labelValueTablePanel = null; // Only used for mobile devices.
      final PFAutoCompleteTextField<String> organizationField = new PFAutoCompleteTextField<String>(INPUT_ID, new PropertyModel<String>(
          data, "organization")) {
        @Override
        protected List<String> getChoices(String input)
        {
          return addressDao.getAutocompletion("organization", input);
        }
      }.withMatchContains(true).withMinChars(2);
      groupPanel.add(createLabelPanel(groupPanel.newChildId(), HALF, getString("organization"), organizationField, true));
      groupPanel.add(createTextFieldPanel(groupPanel.newChildId(), FULL, organizationField).setStrong());
    }
    if (isMobileReadonly() == true) {
      addLabelValueRow(labelValueTablePanel, getString("address.division"), data.getDivision());
      addLabelValueRow(labelValueTablePanel, getString("address.positionText"), data.getPositionText());
      final AddressStatus addressStatus = data.getAddressStatus();
      if (addressStatus != null) {
        addLabelValueRow(labelValueTablePanel, getString("address.addressStatus"), getString(addressStatus.getI18nKey()));
      }
      if (StringUtils.isNotBlank(data.getEmail()) == true) {
        addLabelValueRow(labelValueTablePanel, getString("email"), new ActionLinkPanel(LabelValueTableLPanel.WICKET_ID_VALUE,
            ActionLinkType.MAIL, data.getEmail()));
      }
      if (StringUtils.isNotBlank(data.getWebsite()) == true) {
        addLabelValueRow(labelValueTablePanel, getString("address.website"), new ActionLinkPanel(LabelValueTableLPanel.WICKET_ID_VALUE,
            ActionLinkType.EXTERNAL_URL, data.getWebsite()));
      }
    } else {
      groupPanel.addMaxLengthTextField(data, "division", "address.division", FULL);
      groupPanel.addMaxLengthTextField(data, "positionText", "address.positionText", FULL);
      // DropDownChoice addressStatus
      final LabelValueChoiceRenderer<AddressStatus> addressStatusChoiceRenderer = new LabelValueChoiceRenderer<AddressStatus>(container,
          AddressStatus.values());
      final DropDownChoice addressStatusChoice = new DropDownChoice(SELECT_ID, new PropertyModel(data, "addressStatus"),
          addressStatusChoiceRenderer.getValues(), addressStatusChoiceRenderer);
      addressStatusChoice.setNullValid(false).setRequired(true);
      label = getString("address.addressStatus");
      if (isMobile() == false) {
        groupPanel.add(createLabelPanel(groupPanel.newChildId(), HALF, label, addressStatusChoice, true));
      }
      groupPanel.add(createDropDownChoicePanel(groupPanel.newChildId(), THREEQUART, addressStatusChoice).setLabel(label));

      groupPanel.addMaxLengthTextField(data, "email", "email", FULL).setStrong();
      groupPanel.addMaxLengthTextField(data, "website", "address.website", FULL);
    }
    if (labelValueTablePanel != null && labelValueTablePanel.hasChildren() == true) {
      groupPanel.add(labelValueTablePanel);
    }
    if (groupPanel.hasChildren() == false) {
      groupPanel.setVisible(false);
    }
  }

  /**
   * Adds the fields: private e-mail.
   * @param fieldSetPanel
   */
  public void addPrivateEMail()
  {
    doPanel.newGroupPanel(isMobileReadonly() == true ? getString("address.privateEmail") : null);
    doPanel.addMaxLengthTextField(data, "privateEmail", "email", FULL).setStrong();
  }

  protected void addAddress(final FieldSetLPanel fieldSetPanel, final String heading, final String addressTextProperty,
      final String zipCodeProperty, final String cityProperty, final String countryProperty, final String stateProperty)
  {
    final String addressText, zipCode, city, country, state;
    if (isReadonly() == true) {
      addressText = (String) BeanHelper.getProperty(data, addressTextProperty);
      zipCode = (String) BeanHelper.getProperty(data, zipCodeProperty);
      city = (String) BeanHelper.getProperty(data, cityProperty);
      country = (String) BeanHelper.getProperty(data, countryProperty);
      state = (String) BeanHelper.getProperty(data, stateProperty);
    } else {
      addressText = zipCode = city = country = state = null;
    }
    final GroupLPanel groupPanel = createGroupPanel(fieldSetPanel.newChildId()).setHeading(getString(heading));
    fieldSetPanel.add(groupPanel);
    final LabelValueTableLPanel labelValueTablePanel; // Only used for mobile pages.
    if (isMobileReadonly() == true) {
      labelValueTablePanel = createLabelValueTablePanel(groupPanel.newChildId());
      groupPanel.add(labelValueTablePanel);
      addLabelValueRow(labelValueTablePanel, getString("address.addressText"), addressText);
      addLabelValueRow(labelValueTablePanel, getString("address.city"), zipCode + " " + city);
      addLabelValueRow(labelValueTablePanel, getString("address.country"), country);
      addLabelValueRow(labelValueTablePanel, getString("address.state"), state);
    } else {
      labelValueTablePanel = null; // Only used for mobile pages.
      @SuppressWarnings("serial")
      final PFAutoCompleteTextField<String> addressTextField = new PFAutoCompleteTextField<String>(INPUT_ID, new PropertyModel<String>(
          data, addressTextProperty)) {
        @Override
        protected List<String> getChoices(String input)
        {
          return addressDao.getAutocompletion(addressTextProperty, input);
        }
      }.withMatchContains(true).withMinChars(2);
      groupPanel.add(createLabelPanel(groupPanel.newChildId(), HALF, getString("address.addressText"), addressTextField, true));
      groupPanel.add(createTextFieldPanel(groupPanel.newChildId(), FULL, addressTextField));
      final TextFieldLPanel zipCodeFieldPanel = createTextFieldPanel(groupPanel.newChildId(), QUART, data, zipCodeProperty);
      groupPanel.add(createLabelPanel(groupPanel.newChildId(), HALF, getString("address.zipCode") + "/" + getString("address.city"),
          zipCodeFieldPanel, true));
      groupPanel.add(zipCodeFieldPanel);
      groupPanel.add(createTextFieldPanel(groupPanel.newChildId(), THREEQUART, data, cityProperty));

      final TextFieldLPanel countryTextFieldPanel = createTextFieldPanel(groupPanel.newChildId(), HALF, data, countryProperty);
      groupPanel.add(createLabelPanel(groupPanel.newChildId(), HALF, getString("address.country") + "/" + getString("address.state"),
          countryTextFieldPanel, true));
      groupPanel.add(countryTextFieldPanel);
      groupPanel.add(createTextFieldPanel(groupPanel.newChildId(), HALF, data, stateProperty));
    }
    if (labelValueTablePanel != null && labelValueTablePanel.hasChildren() == true) {
      groupPanel.add(labelValueTablePanel);
    }
    if (groupPanel.hasChildren() == false) {
      groupPanel.setVisible(false);
    }
  }

  private void addPhoneNumber(final LabelValueTableLPanel labelValueTablePanel, final String labelKey, final String number,
      final boolean isMobile)
  {
    if (isMobileReadonly() == true && StringUtils.isBlank(number) == true) {
      return;
    }
    final ActionLinkPanel valueContainer = new ActionLinkPanel(LabelValueTableLPanel.WICKET_ID_VALUE,
        isMobile == true ? ActionLinkType.CALL_AND_SMS : ActionLinkType.CALL, number);
    addLabelValueRow(labelValueTablePanel, getString(labelKey), valueContainer);
  }

  private TextField<String> addPhoneNumber(final GroupLPanel groupPanel, final String property, final String labelKey,
      final String favoriteProperty, final String phoneListTooltip)
  {
    final TextFieldLPanel phoneFieldPanel = groupPanel.addMaxLengthTextField(data, property, labelKey, THREEQUART);
    @SuppressWarnings("unchecked")
    final TextField<String> phoneField = (TextField<String>) phoneFieldPanel.getTextField();
    final RepeaterLabelLPanel repeaterPanel = createRepeaterLabelPanel(groupPanel.newChildId());
    groupPanel.add(repeaterPanel);
    repeaterPanel.add(createCheckBoxPanel(repeaterPanel.newChildId(), personalAddress, favoriteProperty).setTooltip(phoneListTooltip));
    repeaterPanel.add(createImagePanel(repeaterPanel.newChildId(), ImageDef.HELP, phoneListTooltip));
    return phoneField;
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
