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
import static org.projectforge.web.wicket.layout.LayoutLength.DOUBLE;
import static org.projectforge.web.wicket.layout.LayoutLength.FULL;
import static org.projectforge.web.wicket.layout.LayoutLength.HALF;
import static org.projectforge.web.wicket.layout.LayoutLength.ONEHALF;
import static org.projectforge.web.wicket.layout.LayoutLength.QUART;
import static org.projectforge.web.wicket.layout.LayoutLength.THREEQUART;
import static org.projectforge.web.wicket.layout.TextFieldLPanel.INPUT_ID;

import java.util.Date;
import java.util.List;

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
import org.projectforge.common.DateHelper;
import org.projectforge.common.StringHelper;
import org.projectforge.web.wicket.ImageDef;
import org.projectforge.web.wicket.WicketUtils;
import org.projectforge.web.wicket.autocompletion.PFAutoCompleteTextField;
import org.projectforge.web.wicket.components.DatePanel;
import org.projectforge.web.wicket.components.DatePanelSettings;
import org.projectforge.web.wicket.components.LabelValueChoiceRenderer;
import org.projectforge.web.wicket.layout.AbstractRenderer;
import org.projectforge.web.wicket.layout.CheckBoxLPanel;
import org.projectforge.web.wicket.layout.DateFieldLPanel;
import org.projectforge.web.wicket.layout.DropDownChoiceLPanel;
import org.projectforge.web.wicket.layout.FieldSetLPanel;
import org.projectforge.web.wicket.layout.GroupLPanel;
import org.projectforge.web.wicket.layout.ImageLPanel;
import org.projectforge.web.wicket.layout.LabelLPanel;
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

  @SuppressWarnings( { "serial", "unchecked"})
  @Override
  public void add()
  {
    final RepeatingView fieldSetRepeater = new RepeatingView("fieldSetRepeater");
    add(fieldSetRepeater);
    FieldSetLPanel fieldSetPanel = createFieldSetLPanel(fieldSetRepeater.newChildId(), getString("address.heading.personalData"));
    fieldSetRepeater.add(fieldSetPanel);
    GroupLPanel groupPanel = new GroupLPanel(fieldSetPanel.newChildId());
    fieldSetPanel.add(groupPanel);

    // add(new Label("task", taskFormatter.getTaskPath(data.getTaskId(), true, OutputType.HTML)).setEscapeModelStrings(false));
    {
      // DropDownChoice form of address
      final LabelValueChoiceRenderer<FormOfAddress> formChoiceRenderer = new LabelValueChoiceRenderer<FormOfAddress>(container,
          FormOfAddress.values());
      formChoice = new DropDownChoice(SELECT_ID, new PropertyModel(data, "form"), formChoiceRenderer.getValues(), formChoiceRenderer);
      formChoice.setNullValid(false).setRequired(true);
      groupPanel.add(new LabelLPanel(groupPanel.newChildId(), HALF, getString("address.form"), formChoice, true));
      groupPanel.add(new DropDownChoiceLPanel(groupPanel.newChildId(), THREEQUART, formChoice));
      final RepeaterLabelLPanel repeaterPanel = new RepeaterLabelLPanel(groupPanel.newChildId());
      groupPanel.add(repeaterPanel);
      final String tooltip = getString("address.tooltip.vCardList");
      repeaterPanel.add(new CheckBoxLPanel(repeaterPanel.newChildId(), personalAddress, "favoriteCard").setTooltip(tooltip));
      repeaterPanel.add(new ImageLPanel(repeaterPanel.newChildId(), ImageDef.HELP, tooltip));
    }

    groupPanel.addMaxLengthTextField(data, "title", "address.title", FULL).setStrong();
    groupPanel.addMaxLengthTextField(data, "firstName", "firstName", ONEHALF).setStrong();
    final TextFieldLPanel nameTextFieldPanel = groupPanel.addMaxLengthTextField(data, "name", "name", ONEHALF).setStrong().setRequired();
    if (layoutContext.isNew() == true) {
      nameTextFieldPanel.setFocus();
    }
    groupPanel.addMaxLengthTextField(data, "division", "address.division", ONEHALF);
    groupPanel.addMaxLengthTextField(data, "positionText", "address.positionText", ONEHALF);
    groupPanel.addMaxLengthTextField(data, "email", "email", ONEHALF);
    groupPanel.addMaxLengthTextField(data, "website", "address.website", ONEHALF);
    {
      // DropDownChoice contactStatus
      final LabelValueChoiceRenderer<ContactStatus> contactStatusChoiceRenderer = new LabelValueChoiceRenderer<ContactStatus>(container,
          ContactStatus.values());
      final DropDownChoice contactStatusChoice = new DropDownChoice(SELECT_ID, new PropertyModel(data, "contactStatus"),
          contactStatusChoiceRenderer.getValues(), contactStatusChoiceRenderer);
      contactStatusChoice.setNullValid(false).setRequired(true);
      groupPanel.add(new LabelLPanel(groupPanel.newChildId(), HALF, getString("address.contactStatus"), contactStatusChoice, true));
      groupPanel.add(new DropDownChoiceLPanel(groupPanel.newChildId(), THREEQUART, contactStatusChoice));
    }
    final DatePanel birthdayPanel = new DatePanel(DATE_FIELD_ID, new PropertyModel<Date>(data, "birthday"), new DatePanelSettings()
        .withTargetType(java.sql.Date.class));
    groupPanel.add(new LabelLPanel(groupPanel.newChildId(), HALF, getString("address.birthday"), birthdayPanel.getDateField(), true));
    groupPanel.add(new DateFieldLPanel(groupPanel.newChildId(), HALF, birthdayPanel));
    WicketUtils.addTooltip(birthdayPanel.getDateField(), new Model<String>() {
      @Override
      public String getObject()
      {
        return DateHelper.formatAsUTC(data.getBirthday());
      }
    });

    // add(new CheckBox("imageBroschure", new PropertyModel<Boolean>(data, "imageBroschure")));
    final TextAreaLPanel commentTextAreaPanel = groupPanel.addMaxLengthTextArea(data, "comment", "comment", DOUBLE);
    commentTextAreaPanel.setBreakBefore();
    if (layoutContext.isNew() == false) {
      commentTextAreaPanel.setFocus();
    }
    groupPanel.addMaxLengthTextArea(data, "publicKey", "address.publicKey", DOUBLE).setBreakBefore();
    groupPanel.addMaxLengthTextField(data, "fingerprint", "address.fingerprint", DOUBLE).setBreakBefore();

    // *** Business Contact ***
    fieldSetPanel = createFieldSetLPanel(fieldSetRepeater.newChildId(), getString("address.heading.businessContact"));
    fieldSetRepeater.add(fieldSetPanel);
    groupPanel = new GroupLPanel(fieldSetPanel.newChildId());
    fieldSetPanel.add(groupPanel);
    {
      final PFAutoCompleteTextField<String> organizationField = new PFAutoCompleteTextField<String>(INPUT_ID, new PropertyModel<String>(
          data, "organization")) {
        @Override
        protected List<String> getChoices(String input)
        {
          return addressDao.getAutocompletion("organization", input);
        }
      }.withMatchContains(true).withMinChars(2);
      groupPanel.add(new LabelLPanel(groupPanel.newChildId(), HALF, getString("organization"), organizationField, true));
      groupPanel.add(new TextFieldLPanel(groupPanel.newChildId(), ONEHALF, organizationField));
    }
    {
      // DropDownChoice addressStatus
      final LabelValueChoiceRenderer<AddressStatus> addressStatusChoiceRenderer = new LabelValueChoiceRenderer<AddressStatus>(container,
          AddressStatus.values());
      final DropDownChoice addressStatusChoice = new DropDownChoice(SELECT_ID, new PropertyModel(data, "addressStatus"),
          addressStatusChoiceRenderer.getValues(), addressStatusChoiceRenderer);
      addressStatusChoice.setNullValid(false).setRequired(true);
      groupPanel.add(new LabelLPanel(groupPanel.newChildId(), HALF, getString("address.addressStatus"), addressStatusChoice, true));
      groupPanel.add(new DropDownChoiceLPanel(groupPanel.newChildId(), THREEQUART, addressStatusChoice));
    }

    // *** Business Contact: Business address, postal address
    addAddress(fieldSetPanel, "address.heading.businessAddress", "addressText", "zipCode", "city", "country", "state");
    addAddress(fieldSetPanel, "address.heading.postalAddress", "postalAddressText", "postalZipCode", "postalCity", "postalCountry",
        "postalState");

    // *** Business Contact: Phone
    groupPanel = new GroupLPanel(fieldSetPanel.newChildId()).setHeading(getString("address.phone"));
    fieldSetPanel.add(groupPanel);
    final String phoneListTooltip = getString("address.tooltip.phonelist");
    businessPhoneField = addPhoneNumber(groupPanel, "businessPhone", "address.phone", "favoriteBusinessPhone", phoneListTooltip);
    faxField = addPhoneNumber(groupPanel, "fax", "address.phoneType.fax", "favoriteFax", phoneListTooltip);
    mobilePhoneField = addPhoneNumber(groupPanel, "mobilePhone", "address.phoneType.mobile", "favoriteMobilePhone", phoneListTooltip);

    // *** Private Contact ***
    fieldSetPanel = createFieldSetLPanel(fieldSetRepeater.newChildId(), getString("address.heading.privateContact"));
    fieldSetRepeater.add(fieldSetPanel);

    // *** Private Contact: address
    addAddress(fieldSetPanel, "address.heading.privateAddress", "privateAddressText", "privateZipCode", "privateCity", "privateCountry",
        "privateState");

    // *** Private Contact: Phone
    groupPanel = new GroupLPanel(fieldSetPanel.newChildId()).setHeading(getString("address.phone"));
    fieldSetPanel.add(groupPanel);
    privatePhoneField = addPhoneNumber(groupPanel, "privatePhone", "address.phone", "favoritePrivatePhone", phoneListTooltip);
    privateMobilePhoneField = addPhoneNumber(groupPanel, "privateMobilePhone", "address.phoneType.mobile", "favoritePrivateMobilePhone",
        phoneListTooltip);

    groupPanel.addMaxLengthTextField(data, "privateEmail", "email", ONEHALF);
  }

  protected void addAddress(final FieldSetLPanel fieldSetPanel, final String heading, final String addressTextProperty,
      final String zipCodeProperty, final String cityProperty, final String countryProperty, final String stateProperty)
  {
    final GroupLPanel groupPanel = new GroupLPanel(fieldSetPanel.newChildId()).setHeading(getString(heading));
    fieldSetPanel.add(groupPanel);
    {
      @SuppressWarnings("serial")
      final PFAutoCompleteTextField<String> addressTextField = new PFAutoCompleteTextField<String>(INPUT_ID, new PropertyModel<String>(
          data, addressTextProperty)) {
        @Override
        protected List<String> getChoices(String input)
        {
          return addressDao.getAutocompletion(addressTextProperty, input);
        }
      }.withMatchContains(true).withMinChars(2);
      groupPanel.add(new LabelLPanel(groupPanel.newChildId(), HALF, getString("address.addressText"), addressTextField, true));
      groupPanel.add(new TextFieldLPanel(groupPanel.newChildId(), ONEHALF, addressTextField));
    }
    final TextFieldLPanel zipCodeFieldPanel = new TextFieldLPanel(groupPanel.newChildId(), QUART, data, zipCodeProperty);
    groupPanel.add(new LabelLPanel(groupPanel.newChildId(), HALF, getString("address.zipCode") + "/" + getString("address.city"),
        zipCodeFieldPanel, true));
    groupPanel.add(zipCodeFieldPanel);
    groupPanel.add(new TextFieldLPanel(groupPanel.newChildId(), FULL, data, cityProperty));
    groupPanel.addMaxLengthTextField(data, countryProperty, "address.country", ONEHALF);
    groupPanel.addMaxLengthTextField(data, stateProperty, "address.state", ONEHALF);
  }

  private TextField<String> addPhoneNumber(final GroupLPanel groupPanel, final String property, final String labelKey,
      final String favoriteProperty, final String phoneListTooltip)
  {
    final TextFieldLPanel phoneFieldPanel = groupPanel.addMaxLengthTextField(data, property, labelKey, THREEQUART);
    @SuppressWarnings("unchecked")
    final TextField<String> phoneField = (TextField<String>) phoneFieldPanel.getTextField();
    final RepeaterLabelLPanel repeaterPanel = new RepeaterLabelLPanel(groupPanel.newChildId());
    groupPanel.add(repeaterPanel);
    repeaterPanel.add(new CheckBoxLPanel(repeaterPanel.newChildId(), personalAddress, favoriteProperty).setTooltip(phoneListTooltip));
    repeaterPanel.add(new ImageLPanel(repeaterPanel.newChildId(), ImageDef.HELP, phoneListTooltip));
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
