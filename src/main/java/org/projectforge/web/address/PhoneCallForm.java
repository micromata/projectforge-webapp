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

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.apache.commons.lang.StringUtils;
import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.apache.wicket.util.convert.IConverter;
import org.projectforge.address.AddressDO;
import org.projectforge.address.AddressDao;
import org.projectforge.address.AddressFilter;
import org.projectforge.address.PhoneType;
import org.projectforge.common.NumberHelper;
import org.projectforge.common.RecentQueue;
import org.projectforge.common.StringHelper;
import org.projectforge.core.Configuration;
import org.projectforge.user.PFUserContext;
import org.projectforge.user.UserDao;
import org.projectforge.web.wicket.AbstractEditPage;
import org.projectforge.web.wicket.AbstractForm;
import org.projectforge.web.wicket.WebConstants;
import org.projectforge.web.wicket.autocompletion.PFAutoCompleteTextField;
import org.projectforge.web.wicket.components.LabelValueChoiceRenderer;
import org.projectforge.web.wicket.components.SingleButtonPanel;
import org.projectforge.web.wicket.components.TooltipImage;

public class PhoneCallForm extends AbstractForm<Object, PhoneCallPage>
{
  private static final long serialVersionUID = -2138017238114715368L;

  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(PhoneCallForm.class);

  private static final String USER_PREF_KEY_RECENTS = "phoneCalls";

  @SpringBean(name = "addressDao")
  private AddressDao addressDao;

  @SpringBean(name = "configuration")
  private Configuration configuration;

  @SpringBean(name = "userDao")
  private UserDao userDao;

  protected AddressDO address;

  private RepeatingView phoneNumbersRepeatingView;

  protected PFAutoCompleteTextField<AddressDO> numberTextField;

  private WebMarkupContainer addressNameRow;

  private String phoneNumber;

  private String myCurrentPhoneId;

  Date lastSuccessfulPhoneCall;

  private RecentQueue<String> recentSearchTermsQueue;

  public PhoneCallForm(final PhoneCallPage parentPage)
  {
    super(parentPage);
  }

  public String getPhoneNumber()
  {
    return phoneNumber;
  }

  public void setPhoneNumber(String phoneNumber)
  {
    this.phoneNumber = phoneNumber;
  }

  public String getMyCurrentPhoneId()
  {
    if (myCurrentPhoneId == null) {
      myCurrentPhoneId = parentPage.getRecentMyPhoneId();
    }
    return myCurrentPhoneId;
  }

  public void setMyCurrentPhoneId(String myCurrentPhoneId)
  {
    this.myCurrentPhoneId = myCurrentPhoneId;
    if (this.myCurrentPhoneId != null) {
      parentPage.setRecentMyPhoneId(this.myCurrentPhoneId);
    }
  }

  public AddressDO getAddress()
  {
    return address;
  }

  public void setAddress(AddressDO address)
  {
    this.address = address;
  }

  @Override
  @SuppressWarnings( { "serial", "unchecked"})
  protected void init()
  {
    super.init();
    add(new FeedbackPanel("feedback").setOutputMarkupId(true));
    numberTextField = new PFAutoCompleteTextField<AddressDO>("phoneNumber", new Model() {
      @Override
      public Serializable getObject()
      {
        // Pseudo object for storing search string (title field is used for this foreign purpose).
        return new AddressDO().setName(phoneNumber);
      }

      @Override
      public void setObject(final Serializable object)
      {
        if (object != null) {
          if (object instanceof String) {
            phoneNumber = (String) object;
          }
        } else {
          phoneNumber = "";
        }
      }
    }) {
      @Override
      protected List<AddressDO> getChoices(String input)
      {
        final AddressFilter addressFilter = new AddressFilter();
        addressFilter.setSearchString(input);
        addressFilter.setSearchFields("name", "firstName", "organization");
        return addressDao.getList(addressFilter);
      }

      @Override
      protected List<String> getRecentUserInputs()
      {
        return parentPage.getRecentCallsQueue().getRecents();
      }

      @Override
      protected String formatLabel(final AddressDO address)
      {
        return StringHelper.listToString(", ", address.getName(), address.getFirstName(), address.getOrganization());
      }

      @Override
      protected String formatValue(final AddressDO address)
      {
        return "id:" + address.getId();
      }

      @Override
      public IConverter getConverter(Class< ? > type)
      {
        return new IConverter() {
          @Override
          public Object convertToObject(final String value, final Locale locale)
          {
            phoneNumber = value;
            return new AddressDO().setName(phoneNumber);
          }

          @Override
          public String convertToString(final Object value, final Locale locale)
          {
            return phoneNumber;
          }
        };
      }
    };
    numberTextField.withLabelValue(true).withMatchContains(true).withMinChars(2).withFocus(true).withAutoSubmit(true);
    add(numberTextField);
    add(new TooltipImage("numberHelp", getResponse(), WebConstants.IMAGE_HELP_KEYBOARD, getString("address.directCall.number.tooltip")));

    // DropDownChoice myCurrentPhoneId
    final LabelValueChoiceRenderer<String> myCurrentPhoneIdChoiceRenderer = new LabelValueChoiceRenderer<String>();
    final String[] ids = userDao.getPersonalPhoneIdentifiers(PFUserContext.getUser());
    if (ids == null) {
      myCurrentPhoneIdChoiceRenderer.addValue("--", getString("user.personalPhoneIdentifiers.pleaseDefine"));
    } else {
      for (String id : ids) {
        myCurrentPhoneIdChoiceRenderer.addValue(id, id);
      }
    }
    final DropDownChoice myCurrentPhoneIdChoice = new DropDownChoice("myCurrentPhoneId", new PropertyModel(this, "myCurrentPhoneId"),
        myCurrentPhoneIdChoiceRenderer.getValues(), myCurrentPhoneIdChoiceRenderer);
    myCurrentPhoneIdChoice.setNullValid(false).setRequired(true);
    add(myCurrentPhoneIdChoice);
    add(new TooltipImage("myCurrentPhoneIdHelp", getResponse(), WebConstants.IMAGE_HELP, getString("address.myCurrentPhoneId.tooltip")));
    addressNameRow = new WebMarkupContainer("addressNameRow") {
      @Override
      public boolean isVisible()
      {
        return address != null;
      }
    };
    add(addressNameRow);
    final Link<String> addressViewLink = new Link<String>("addressViewLink") {
      @Override
      public void onClick()
      {
        if (address == null) {
          log.error("Oups should not occur: AddressViewLink is shown without a given address. Ignoring link.");
          return;
        }
        final PageParameters params = new PageParameters();
        params.put(AbstractEditPage.PARAMETER_KEY_ID, address.getId());
        setResponsePage(new AddressViewPage(params, parentPage));
      }
    };
    addressNameRow.add(addressViewLink);
    addressViewLink.add(new Label("label", new Model<String>() {
      @Override
      public String getObject()
      {
        if (address == null) {
          return "";
        }
        final StringBuffer buf = new StringBuffer();
        if (address.getForm() != null) {
          buf.append(getString(address.getForm().getI18nKey())).append(" ");
        }
        if (StringUtils.isNotBlank(address.getTitle()) == true) {
          buf.append(address.getTitle()).append(" ");
        }
        if (StringUtils.isNotBlank(address.getFirstName()) == true) {
          buf.append(address.getFirstName()).append(" ");
        }
        if (StringUtils.isNotBlank(address.getName()) == true) {
          buf.append(address.getName());
        }
        return buf.toString();
      }
    }));
    final Button callButton = new Button("button", new Model<String>(getString("address.directCall.call"))) {
      @Override
      public final void onSubmit()
      {
        parentPage.call();
      }
    };
    add(new SingleButtonPanel("call", callButton));
    setDefaultButton(callButton);
    final WebMarkupContainer showOperatorPanel = new WebMarkupContainer("telephoneSystemOperatorPanel");
    add(showOperatorPanel);
    final String url = configuration.getTelephoneSystemOperatorPanelUrl();
    if (url == null) {
      showOperatorPanel.setVisible(false);
    } else {
      final Label content = new Label("content", url);
      showOperatorPanel.add(content.setEscapeModelStrings(false));
    }
    refresh();
  }

  protected void refresh()
  {
    if (phoneNumbersRepeatingView == null) {
      phoneNumbersRepeatingView = new RepeatingView("phoneNumberRepeater");
      add(phoneNumbersRepeatingView);
    } else {
      phoneNumbersRepeatingView.removeAll();
    }
    if (address == null) {
      return;
    }
    addPhoneNumber(address.getBusinessPhone(), getString(PhoneType.BUSINESS.getI18nKey()));
    addPhoneNumber(address.getMobilePhone(), getString(PhoneType.MOBILE.getI18nKey()));
    addPhoneNumber(address.getPrivatePhone(), getString(PhoneType.PRIVATE.getI18nKey()));
    addPhoneNumber(address.getPrivateMobilePhone(), getString(PhoneType.PRIVATE_MOBILE.getI18nKey()));
  }

  @SuppressWarnings("serial")
  private void addPhoneNumber(final String phoneNumber, final String label)
  {
    if (StringUtils.isBlank(phoneNumber) == true) {
      return;
    }
    final WebMarkupContainer item = new WebMarkupContainer(phoneNumbersRepeatingView.newChildId());
    phoneNumbersRepeatingView.add(item);
    item.add(new Label("label", label));
    final Link<String> link = new Link<String>("callNumberLink") {
      @Override
      public void onClick()
      {
        setPhoneNumber(parentPage.extractPhonenumber(phoneNumber));
        numberTextField.setModelObject(new AddressDO().setName(getPhoneNumber()));
        numberTextField.modelChanged();
        parentPage.call();
      }
    };
    item.add(link);
    link.add(new Label("number", phoneNumber).setRenderBodyOnly(true));
  }

  protected String getPhoneNumberAndPerson(final AddressDO address, final PhoneType phoneType, final String number,
      final String countryPrefix)
  {
    return StringHelper.listToString(", ", NumberHelper.extractPhonenumber(number, countryPrefix) + ": " + address.getName(), address
        .getFirstName(), getString(phoneType.getI18nKey()), address.getOrganization());
  }

  @SuppressWarnings("unchecked")
  protected RecentQueue<String> getRecentSearchTermsQueue()
  {
    if (recentSearchTermsQueue == null) {
      recentSearchTermsQueue = (RecentQueue<String>) parentPage.getUserPrefEntry(USER_PREF_KEY_RECENTS);
    }
    if (recentSearchTermsQueue == null) {
      recentSearchTermsQueue = (RecentQueue<String>) parentPage
          .getUserPrefEntry("org.projectforge.web.address.PhoneCallAction:recentSearchTerms");
      if (recentSearchTermsQueue != null) {
        // Old entries:
        parentPage.putUserPrefEntry(USER_PREF_KEY_RECENTS, recentSearchTermsQueue, true);
        parentPage.removeUserPrefEntry("org.projectforge.web.address.PhoneCallAction:recentSearchTerms");
      }
    }
    if (recentSearchTermsQueue == null) {
      recentSearchTermsQueue = new RecentQueue<String>();
      parentPage.putUserPrefEntry(USER_PREF_KEY_RECENTS, recentSearchTermsQueue, true);
    }
    return recentSearchTermsQueue;
  }
}
