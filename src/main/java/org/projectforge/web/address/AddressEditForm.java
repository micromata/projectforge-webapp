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
import static org.projectforge.web.wicket.layout.LayoutLength.THREEQUART;
import static org.projectforge.web.wicket.layout.TextFieldLPanel.INPUT_ID;

import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.projectforge.address.AddressDO;
import org.projectforge.address.AddressStatus;
import org.projectforge.address.ContactStatus;
import org.projectforge.address.FormOfAddress;
import org.projectforge.address.PersonalAddressDO;
import org.projectforge.address.PersonalAddressDao;
import org.projectforge.common.DateHelper;
import org.projectforge.common.StringHelper;
import org.projectforge.web.common.OutputType;
import org.projectforge.web.task.TaskFormatter;
import org.projectforge.web.wicket.AbstractEditForm;
import org.projectforge.web.wicket.FocusOnLoadBehavior;
import org.projectforge.web.wicket.ImageDef;
import org.projectforge.web.wicket.WebConstants;
import org.projectforge.web.wicket.WicketUtils;
import org.projectforge.web.wicket.autocompletion.PFAutoCompleteTextField;
import org.projectforge.web.wicket.components.DatePanel;
import org.projectforge.web.wicket.components.DatePanelSettings;
import org.projectforge.web.wicket.components.LabelValueChoiceRenderer;
import org.projectforge.web.wicket.components.MaxLengthTextArea;
import org.projectforge.web.wicket.components.MaxLengthTextField;
import org.projectforge.web.wicket.components.TooltipImage;
import org.projectforge.web.wicket.layout.CheckBoxLPanel;
import org.projectforge.web.wicket.layout.DropDownChoiceLPanel;
import org.projectforge.web.wicket.layout.FieldSetLPanel;
import org.projectforge.web.wicket.layout.GroupLPanel;
import org.projectforge.web.wicket.layout.ImageLPanel;
import org.projectforge.web.wicket.layout.LabelLPanel;
import org.projectforge.web.wicket.layout.LayoutLength;
import org.projectforge.web.wicket.layout.RepeaterLabelLPanel;
import org.projectforge.web.wicket.layout.TextFieldLPanel;

public class AddressEditForm extends AbstractEditForm<AddressDO, AddressEditPage>
{
  private static final long serialVersionUID = 3881031215413525517L;

  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(AddressEditForm.class);

  @SpringBean(name = "personalAddressDao")
  private PersonalAddressDao personalAddressDao;

  @SpringBean(name = "taskFormatter")
  private TaskFormatter taskFormatter;

  protected PersonalAddressDO personalAddress;

  private DropDownChoice<FormOfAddress> formChoice;

  private TextField<String> businessPhoneField, faxField, mobilePhoneField, privatePhoneField, privateMobilePhoneField;

  public AddressEditForm(AddressEditPage parentPage, AddressDO data)
  {
    super(parentPage, data);
    this.colspan = 6;
    if (isNew() == false) {
      personalAddress = personalAddressDao.getByAddressId(getData().getId());
    }
    if (personalAddress == null) {
      personalAddress = new PersonalAddressDO();
    }
  }

  protected TextFieldLPanel addMaxLengthTextField(final GroupLPanel groupPanel, final String property, final String labelKey,
      final LayoutLength length)
  {
    final MaxLengthTextField textField = new MaxLengthTextField(INPUT_ID, new PropertyModel<String>(data, property));
    groupPanel.add(new LabelLPanel(groupPanel.newChildId(), HALF, getString(labelKey)).setLabelFor(textField).setBreakBefore());
    final TextFieldLPanel textFieldPanel = new TextFieldLPanel(groupPanel.newChildId(), length, textField);
    groupPanel.add(textFieldPanel);
    return textFieldPanel;
  }

  @SuppressWarnings( { "serial", "unchecked"})
  @Override
  protected void init()
  {
    super.init();

    final RepeatingView fieldSetRepeater = new RepeatingView("fieldSetRepeater");
    add(fieldSetRepeater);
    FieldSetLPanel fieldSetPanel = new FieldSetLPanel(fieldSetRepeater.newChildId(), getString("address.heading.personalData"));
    fieldSetRepeater.add(fieldSetPanel);
    GroupLPanel groupPanel = new GroupLPanel(fieldSetPanel.newChildId());
    fieldSetPanel.add(groupPanel);

    // add(new Label("task", taskFormatter.getTaskPath(data.getTaskId(), true, OutputType.HTML)).setEscapeModelStrings(false));
    {
      // DropDownChoice form of address
      final LabelValueChoiceRenderer<FormOfAddress> formChoiceRenderer = new LabelValueChoiceRenderer<FormOfAddress>(this, FormOfAddress
          .values());
      formChoice = new DropDownChoice(SELECT_ID, new PropertyModel(data, "form"), formChoiceRenderer.getValues(), formChoiceRenderer);
      formChoice.setNullValid(false).setRequired(true);
      groupPanel.add(new DropDownChoiceLPanel(groupPanel.newChildId(), THREEQUART, formChoice));
    }
    groupPanel.add(new LabelLPanel(groupPanel.newChildId(), HALF, getString("address.form")).setLabelFor(formChoice).setBreakBefore());
    final RepeaterLabelLPanel repeaterPanel = new RepeaterLabelLPanel(groupPanel.newChildId());
    groupPanel.add(repeaterPanel);
    repeaterPanel.add(new CheckBoxLPanel(repeaterPanel.newChildId(), personalAddress, "favoriteCard"));
    repeaterPanel.add(new ImageLPanel(repeaterPanel.newChildId(), ImageDef.HELP, getString("address.tooltip.vCardList")));

    addMaxLengthTextField(groupPanel, "title", "address.title", FULL).setStrong();
    addMaxLengthTextField(groupPanel, "firstName", "firstName", ONEHALF).setStrong();
    final TextFieldLPanel nameTextFieldPanel = addMaxLengthTextField(groupPanel, "name", "name", ONEHALF).setStrong().setRequired();
    if (isNew() == true) {
      nameTextFieldPanel.setFocus();
    }
    addMaxLengthTextField(groupPanel, "division", "address.division", ONEHALF);
    addMaxLengthTextField(groupPanel, "positionText", "address.positionText", ONEHALF);
    addMaxLengthTextField(groupPanel, "email", "email", ONEHALF);
    addMaxLengthTextField(groupPanel, "website", "address.website", ONEHALF);

    final String phoneListTooltip = getString("address.tooltip.phonelist");
    add(new Label("task", taskFormatter.getTaskPath(data.getTaskId(), true, OutputType.HTML)).setEscapeModelStrings(false));
    add(businessPhoneField = new MaxLengthTextField("businessPhone", new PropertyModel<String>(data, "businessPhone")));
    add(WicketUtils.addTooltip(new CheckBox("favoriteBusinessPhone", new PropertyModel<Boolean>(personalAddress, "favoriteBusinessPhone")),
        phoneListTooltip));
    add(new TooltipImage("favoriteBusinessPhoneHelpImage", getResponse(), WebConstants.IMAGE_HELP, phoneListTooltip));
    add(faxField = new MaxLengthTextField("fax", new PropertyModel<String>(data, "fax")));
    add(WicketUtils.addTooltip(new CheckBox("favoriteFax", new PropertyModel<Boolean>(personalAddress, "favoriteFax")), phoneListTooltip));
    add(new TooltipImage("favoriteFaxHelpImage", getResponse(), WebConstants.IMAGE_HELP, phoneListTooltip));
    add(new PFAutoCompleteTextField<String>("organization", new PropertyModel<String>(data, "organization")) {
      @Override
      protected List<String> getChoices(String input)
      {
        return parentPage.getBaseDao().getAutocompletion("organization", input);
      }
    }.withMatchContains(true).withMinChars(2));
    add(mobilePhoneField = new MaxLengthTextField("mobilePhone", new PropertyModel<String>(data, "mobilePhone")));
    add(WicketUtils.addTooltip(new CheckBox("favoriteMobilePhone", new PropertyModel<Boolean>(personalAddress, "favoriteMobilePhone")),
        phoneListTooltip));
    add(new TooltipImage("favoriteMobilePhoneHelpImage", getResponse(), WebConstants.IMAGE_HELP, phoneListTooltip));
    add(new CheckBox("imageBroschure", new PropertyModel<Boolean>(data, "imageBroschure")));
    add(new PFAutoCompleteTextField<String>("addressText", new PropertyModel<String>(data, "addressText")) {
      @Override
      protected List<String> getChoices(String input)
      {
        return parentPage.getBaseDao().getAutocompletion("addressText", input);
      }
    }.withMatchContains(true).withMinChars(2));
    add(new PFAutoCompleteTextField<String>("postalAddressText", new PropertyModel<String>(data, "postalAddressText")) {
      @Override
      protected List<String> getChoices(String input)
      {
        return parentPage.getBaseDao().getAutocompletion("postalAddressText", input);
      }
    }.withMatchContains(true).withMinChars(2));
    add(new MaxLengthTextField("zipCode", new PropertyModel<String>(data, "zipCode")));
    add(new MaxLengthTextField("city", new PropertyModel<String>(data, "city")));
    add(new MaxLengthTextField("postalZipCode", new PropertyModel<String>(data, "postalZipCode")));
    add(new MaxLengthTextField("postalCity", new PropertyModel<String>(data, "postalCity")));
    add(new MaxLengthTextField("country", new PropertyModel<String>(data, "country")));
    add(new MaxLengthTextField("state", new PropertyModel<String>(data, "state")));
    add(new MaxLengthTextField("postalCountry", new PropertyModel<String>(data, "postalCountry")));
    add(new MaxLengthTextField("postalState", new PropertyModel<String>(data, "postalState")));
    add(new MaxLengthTextField("privateAddressText", new PropertyModel<String>(data, "privateAddressText")));
    add(privatePhoneField = new MaxLengthTextField("privatePhone", new PropertyModel<String>(data, "privatePhone")));
    add(WicketUtils.addTooltip(new CheckBox("favoritePrivatePhone", new PropertyModel<Boolean>(personalAddress, "favoritePrivatePhone")),
        phoneListTooltip));
    add(new TooltipImage("favoritePrivatePhoneHelpImage", getResponse(), WebConstants.IMAGE_HELP, phoneListTooltip));
    add(new MaxLengthTextField("privateZipCode", new PropertyModel<String>(data, "privateZipCode")));
    add(new MaxLengthTextField("privateCity", new PropertyModel<String>(data, "privateCity")));
    add(privateMobilePhoneField = new MaxLengthTextField("privateMobilePhone", new PropertyModel<String>(data, "privateMobilePhone")));
    add(WicketUtils.addTooltip(new CheckBox("favoritePrivateMobilePhone", new PropertyModel<Boolean>(personalAddress,
        "favoritePrivateMobilePhone")), phoneListTooltip));
    add(new TooltipImage("favoritePrivateMobilePhoneHelpImage", getResponse(), WebConstants.IMAGE_HELP, phoneListTooltip));
    add(new MaxLengthTextField("privateCountry", new PropertyModel<String>(data, "privateCountry")));
    add(new MaxLengthTextField("privateState", new PropertyModel<String>(data, "privateState")));
    add(new MaxLengthTextField("privateEmail", new PropertyModel<String>(data, "privateEmail")));
    add(new DatePanel("birthday", new PropertyModel<Date>(data, "birthday"), new DatePanelSettings().withTargetType(java.sql.Date.class)));
    add(new Label("showBirthdayAsUTC", new Model<String>() {
      @Override
      public String getObject()
      {
        return DateHelper.formatAsUTC(data.getBirthday());
      }
    }));
    // add(new PresizedImage("cakeImage", getResponse(), WebConstants.IMAGE_BIRTHDAY));
    final MaxLengthTextArea commentTextField = new MaxLengthTextArea("comment", new PropertyModel<String>(data, "comment"));
    add(commentTextField);
    if (isNew() == false) {
      commentTextField.add(new FocusOnLoadBehavior());
    }
    add(new MaxLengthTextArea("publicKey", new PropertyModel<String>(data, "publicKey")));
    add(new MaxLengthTextField("fingerprint", new PropertyModel<String>(data, "fingerprint")));

    // DropDownChoice contactStatus
    final LabelValueChoiceRenderer<ContactStatus> contactStatusChoiceRenderer = new LabelValueChoiceRenderer<ContactStatus>(this,
        ContactStatus.values());
    final DropDownChoice contactStatusChoice = new DropDownChoice("contactStatus", new PropertyModel(data, "contactStatus"),
        contactStatusChoiceRenderer.getValues(), contactStatusChoiceRenderer);
    contactStatusChoice.setNullValid(false).setRequired(true);
    add(contactStatusChoice);

    // DropDownChoice addressStatus
    final LabelValueChoiceRenderer<AddressStatus> addressStatusChoiceRenderer = new LabelValueChoiceRenderer<AddressStatus>(this,
        AddressStatus.values());
    final DropDownChoice addressStatusChoice = new DropDownChoice("addressStatus", new PropertyModel(data, "addressStatus"),
        addressStatusChoiceRenderer.getValues(), addressStatusChoiceRenderer);
    addressStatusChoice.setNullValid(false).setRequired(true);
    add(addressStatusChoice);
  }

  @Override
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

  private void validatePhoneNumber(final FormComponent<String> component)
  {
    if (StringHelper.checkPhoneNumberFormat(component.getConvertedInput()) == false) {
      addComponentError(component, "address.error.phone.invalidFormat");
    }
  }

  @Override
  protected Logger getLogger()
  {
    return log;
  }
}
