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
import org.projectforge.address.AddressDO;
import org.projectforge.address.AddressDao;
import org.projectforge.address.AddressFilter;
import org.projectforge.address.PhoneType;
import org.projectforge.common.NumberHelper;
import org.projectforge.common.RecentQueue;
import org.projectforge.common.StringHelper;
import org.projectforge.core.Configuration;
import org.projectforge.core.ConfigurationParam;
import org.projectforge.user.PFUserContext;
import org.projectforge.user.UserDao;
import org.projectforge.web.wicket.AbstractEditPage;
import org.projectforge.web.wicket.AbstractForm;
import org.projectforge.web.wicket.WebConstants;
import org.projectforge.web.wicket.autocompletion.PFAutoCompleteTextField;
import org.projectforge.web.wicket.components.LabelValueChoiceRenderer;
import org.projectforge.web.wicket.components.SingleButtonPanel;
import org.projectforge.web.wicket.components.TooltipImage;

public class PhoneCallForm extends AbstractForm<PhoneCallData, PhoneCallPage>
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

  private WebMarkupContainer addressNameRow;

  private String phoneNumber;

  private String myCurrentPhoneId;

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
    return myCurrentPhoneId;
  }

  public void setMyCurrentPhoneId(String myCurrentPhoneId)
  {
    this.myCurrentPhoneId = myCurrentPhoneId;
  }

  @Override
  @SuppressWarnings("serial")
  protected void init()
  {
    super.init();
    add(new FeedbackPanel("feedback").setOutputMarkupId(true));
    final PFAutoCompleteTextField<String> numberTextField = new PFAutoCompleteTextField<String>("phoneNumber", new PropertyModel<String>(
        this, "phoneNumber")) {
      @Override
      protected List<String> getChoices(String input)
      {
        final AddressFilter addressFilter = new AddressFilter();
        addressFilter.setSearchString(input);
        final List<String> list = new ArrayList<String>();
        for (final AddressDO address : addressDao.getList(addressFilter)) {
          buildAutocompleteEntry(list, address, PhoneType.BUSINESS, address.getBusinessPhone());
          buildAutocompleteEntry(list, address, PhoneType.MOBILE, address.getMobilePhone());
          buildAutocompleteEntry(list, address, PhoneType.PRIVATE, address.getPrivatePhone());
          buildAutocompleteEntry(list, address, PhoneType.PRIVATE_MOBILE, address.getPrivateMobilePhone());
        }
        return list;
      }

      @Override
      protected List<String> getFavorites()
      {
        return getRecentSearchTermsQueue().getRecents();
      }
    };
    numberTextField.withMatchContains(true).withMinChars(2).withFocus(true);
    numberTextField.setRequired(true);
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
    @SuppressWarnings("unchecked")
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
  }

  @Override
  public void onBeforeRender()
  {
    super.onBeforeRender();
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
      }
    };
    item.add(link);
    link.add(new Label("number", phoneNumber).setRenderBodyOnly(true));
  }

  protected String getPhoneNumberAndPerson(final AddressDO address, final PhoneType phoneType, final String number,
      final String countryPrefix)
  {
    return StringHelper.listToString(", ", NumberHelper.extractPhonenumber(number, countryPrefix) + ": " + address.getName(),
        getString(phoneType.getI18nKey()), address.getFirstName(), address.getOrganization());
  }

  private void buildAutocompleteEntry(final List<String> list, final AddressDO address, final PhoneType phoneType, final String number)
  {
    if (StringUtils.isBlank(number) == true) {
      return;
    }
    list.add(getPhoneNumberAndPerson(address, phoneType, number, configuration
        .getStringValue(ConfigurationParam.DEFAULT_COUNTRY_PHONE_PREFIX)));
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
