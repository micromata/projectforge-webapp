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
import org.projectforge.common.DatePrecision;
import org.projectforge.common.StringHelper;
import org.projectforge.web.mobile.ActionLinkPanel;
import org.projectforge.web.mobile.ActionLinkType;
import org.projectforge.web.wicket.ImageDef;
import org.projectforge.web.wicket.WicketUtils;
import org.projectforge.web.wicket.autocompletion.PFAutoCompleteTextField;
import org.projectforge.web.wicket.components.DatePanel;
import org.projectforge.web.wicket.components.DatePanelSettings;
import org.projectforge.web.wicket.components.LabelValueChoiceRenderer;
import org.projectforge.web.wicket.layout.AbstractRenderer;
import org.projectforge.web.wicket.layout.DateFieldLPanel;
import org.projectforge.web.wicket.layout.FieldSetLPanel;
import org.projectforge.web.wicket.layout.FieldType;
import org.projectforge.web.wicket.layout.GroupLPanel;
import org.projectforge.web.wicket.layout.GroupMobileLPanel;
import org.projectforge.web.wicket.layout.IField;
import org.projectforge.web.wicket.layout.LabelValueTableLPanel;
import org.projectforge.web.wicket.layout.LayoutContext;
import org.projectforge.web.wicket.layout.RepeaterLabelLPanel;
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
    doPanel.newFieldSetPanel(isNew() == false ? title : getString("address.heading.personalData"));
    FieldSetLPanel fieldSetPanel;
    if (isMobileReadonly() == true) {
      // Append at the end.
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
    fieldSetPanel = createFieldSetPanel(fieldSetRepeater.newChildId(), businessContactTitle);
    fieldSetRepeater.add(fieldSetPanel);

    addBusinessData();
    addBusinessPhones(fieldSetPanel);
    addBusinesAddress();
    addPostalAddress();

    // *** Private Contact ***
    doPanel.newFieldSetPanel(getString("address.heading.privateContact"));
    addPrivateEMail();

    addPrivatePhones(fieldSetPanel);
    addPrivateAddress();

    if (isMobileReadonly() == true) {
      addPersonalData(title);
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
      addAddress("address.heading.postalAddress", "postalAddressText", "postalZipCode", "postalCity", "postalCountry", "postalState");
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
  public void addPersonalData(final String title)
  {
    doPanel.newGroupPanel();
    // add(new Label("task", taskFormatter.getTaskPath(data.getTaskId(), true, OutputType.HTML)).setEscapeModelStrings(false));
    {
      // DropDownChoice form of address
      final LabelValueChoiceRenderer<FormOfAddress> formChoiceRenderer = new LabelValueChoiceRenderer<FormOfAddress>(container,
          FormOfAddress.values());
      formChoice = new DropDownChoice(SELECT_ID, new PropertyModel(data, "form"), formChoiceRenderer.getValues(), formChoiceRenderer);
      formChoice.setNullValid(false).setRequired(true);
      doPanel.addDropDownChoice(data, "form", getString("address.form"), HALF, formChoice, THREEQUART);
    }
    if (isMobile() == false) {
      final String tooltip = getString("address.tooltip.vCardList");
      doPanel.addCheckBox(personalAddress, "favoriteCard").setTooltip(tooltip);
      doPanel.addImage(ImageDef.HELP).setTooltip(tooltip);
    }
    doPanel.addTextField(data, "title", getString("address.title"), HALF, THREEQUART).setStrong();
    doPanel.addTextField(data, "firstName", getString("firstName"), HALF, FULL).setStrong();
    final IField nameTextField = doPanel.addTextField(data, "name", getString("name"), HALF, FULL).setStrong().setRequired();
    if (isNew() == true) {
      nameTextField.setFocus();
    }

    {
      // DropDownChoice contactStatus
      final LabelValueChoiceRenderer<ContactStatus> contactStatusChoiceRenderer = new LabelValueChoiceRenderer<ContactStatus>(container,
          ContactStatus.values());
      final DropDownChoice contactStatusChoice = new DropDownChoice(SELECT_ID, new PropertyModel(data, "contactStatus"),
          contactStatusChoiceRenderer.getValues(), contactStatusChoiceRenderer);
      contactStatusChoice.setNullValid(false).setRequired(true);
      doPanel.addDropDownChoice(data, "contactStatus", getString("address.contactStatus"), HALF, contactStatusChoice, THREEQUART);
    }
    if (isReadonly() == true) {
      doPanel.addDateFieldPanel(data, "birthday", getString("address.birthday"), HALF, DatePrecision.DAY, HALF);
    } else {
      final DatePanel birthdayPanel = new DatePanel(DateFieldLPanel.DATE_FIELD_ID, new PropertyModel<Date>(data, "birthday"),
          new DatePanelSettings().withTargetType(java.sql.Date.class));
      doPanel.addDateFieldPanel(data, "birthday", getString("address.birthday"), HALF, birthdayPanel, HALF);
      WicketUtils.addTooltip(birthdayPanel.getDateField(), new Model<String>() {
        @Override
        public String getObject()
        {
          return DateHelper.formatAsUTC(data.getBirthday());
        }
      });
    }
    final IField commentField = doPanel.addTextArea(data, "comment", getString("comment"), HALF, ONEHALF, true)
        .setCssStyle("height: 20em;");
    if (layoutContext.isNew() == false) {
      commentField.setFocus();
    }
  }

  /**
   * Adds the fields: publicKey, fingerPrint
   * @param fieldSetPanel
   */
  public void addPublicKeyAndFingerprint()
  {
    if (isMobileReadonly() == true) {
      final GroupMobileLPanel groupMobilePanel = (GroupMobileLPanel) doPanel.newGroupPanel(getString("address.publicKey"));
      groupMobilePanel.setCollapsed();
    }
    doPanel.addTextField(data, "fingerprint", getString("address.fingerprint"), HALF, ONEHALF, true);
    doPanel.addTextArea(data, "publicKey", getString("address.publicKey"), HALF, ONEHALF, true).setCssStyle("height: 5em;");
  }

  /**
   * Adds the fields: organization, division, position, address status, e-mail and web-site.
   * @param fieldSetPanel
   */
  @SuppressWarnings( { "serial", "unchecked"})
  public void addBusinessData()
  {
    if (isMobileReadonly()) {
      doPanel.addTextField(data, "organization", getString("organization"), HALF, ONEHALF);
    } else {
      final PFAutoCompleteTextField<String> organizationField = new PFAutoCompleteTextField<String>(INPUT_ID, new PropertyModel<String>(
          data, "organization")) {
        @Override
        protected List<String> getChoices(String input)
        {
          return addressDao.getAutocompletion("organization", input);
        }
      }.withMatchContains(true).withMinChars(2);
      doPanel.addTextField(getString("organization"), HALF, organizationField, FULL).setStrong();
    }
    doPanel.addTextField(data, "division", getString("address.division"), HALF, FULL);
    doPanel.addTextField(data, "positionText", getString("address.positionText"), HALF, FULL);
    {
      // DropDownChoice addressStatus
      final LabelValueChoiceRenderer<AddressStatus> addressStatusChoiceRenderer = new LabelValueChoiceRenderer<AddressStatus>(container,
          AddressStatus.values());
      final DropDownChoice addressStatusChoice = new DropDownChoice(SELECT_ID, new PropertyModel(data, "addressStatus"),
          addressStatusChoiceRenderer.getValues(), addressStatusChoiceRenderer);
      addressStatusChoice.setNullValid(false).setRequired(true);
      doPanel.addDropDownChoice(data, "addressStatus", getString("address.addressStatus"), HALF, addressStatusChoice, THREEQUART);
    }
    doPanel.addTextField(data, "email", getString("email"), HALF, FULL, FieldType.E_MAIL, false);
    doPanel.addTextField(data, "website", getString("address.website"), HALF, FULL, FieldType.WEB_PAGE, false);
  }

  /**
   * Adds the fields: private e-mail.
   * @param fieldSetPanel
   */
  public void addPrivateEMail()
  {
    doPanel.newGroupPanel(isMobileReadonly() == true ? getString("address.privateEmail") : null);
    doPanel.addTextField(data, "privateEmail", getString("email"), HALF, FULL).setStrong();
  }

  protected void addAddress(final String heading, final String addressTextProperty, final String zipCodeProperty,
      final String cityProperty, final String countryProperty, final String stateProperty)
  {
    final String zipCode, city;
    if (isReadonly() == true) {
      zipCode = (String) BeanHelper.getProperty(data, zipCodeProperty);
      city = (String) BeanHelper.getProperty(data, cityProperty);
    } else {
      zipCode = city = null;
    }
    doPanel.newGroupPanel(getString(heading));
    if (isMobileReadonly() == true) {
      doPanel.addReadonlyTextField(data, addressTextProperty, getString("address.addressText"), null, null);
      doPanel.addReadonlyTextField(zipCode + " " + city, getString("address.city"), null, null);
      doPanel.addReadonlyTextField(data, countryProperty, getString("address.country"), null, null);
      doPanel.addReadonlyTextField(data, stateProperty, getString("address.state"), null, null);
    } else {
      @SuppressWarnings("serial")
      final PFAutoCompleteTextField<String> addressTextField = new PFAutoCompleteTextField<String>(INPUT_ID, new PropertyModel<String>(
          data, addressTextProperty)) {
        @Override
        protected List<String> getChoices(String input)
        {
          return addressDao.getAutocompletion(addressTextProperty, input);
        }
      }.withMatchContains(true).withMinChars(2);
      doPanel.addTextField(getString("address.addressText"), HALF, addressTextField, FULL);
      doPanel.addTextField(data, zipCodeProperty, getString("address.zipCode") + "/" + getString("address.city"), HALF, QUART);
      doPanel.addTextField(data, cityProperty, THREEQUART);
      doPanel.addTextField(data, countryProperty, getString("address.country") + "/" + getString("address.state"), HALF, HALF);
      doPanel.addTextField(data, stateProperty, HALF);
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
    final TextFieldLPanel phoneFieldPanel = groupPanel.addTextField(data, property, labelKey, HALF, THREEQUART);
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
