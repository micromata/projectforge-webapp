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
import org.apache.wicket.behavior.SimpleAttributeModifier;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
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
import org.projectforge.web.wicket.AbstractForm;
import org.projectforge.web.wicket.WebConstants;
import org.projectforge.web.wicket.autocompletion.PFAutoCompleteTextField;
import org.projectforge.web.wicket.components.SingleButtonPanel;
import org.projectforge.web.wicket.components.TooltipImage;

public class PhoneCallForm extends AbstractForm<PhoneCallData, PhoneCallPage>
{
  private static final long serialVersionUID = -2138017238114715368L;
  
  private static final String USER_PREF_KEY_RECENTS = "phoneCalls";

  @SpringBean(name = "addressDao")
  private AddressDao addressDao;

  @SpringBean(name = "configuration")
  private Configuration configuration;

  protected SendSmsData data;

  private RecentQueue<String> recentSearchTermsQueue;

  public PhoneCallForm(final PhoneCallPage parentPage)
  {
    super(parentPage);
    data = new SendSmsData();
  }

  @Override
  @SuppressWarnings("serial")
  protected void init()
  {
    super.init();
    add(new FeedbackPanel("feedback").setOutputMarkupId(true));
    final PFAutoCompleteTextField<String> numberTextField = new PFAutoCompleteTextField<String>("phoneNumber", new PropertyModel<String>(
        data, "phoneNumber")) {
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
    add(new TooltipImage("numberHelp", getResponse(), WebConstants.IMAGE_HELP_KEYBOARD,
        getString("address.directCall.number.tooltip")));

    final Button sendButton = new Button("button", new Model<String>(getString("address.directCall.call"))) {
      @Override
      public final void onSubmit()
      {
        parentPage.send();
      }
    };
    sendButton.add(new SimpleAttributeModifier("onclick", "return showSendQuestionDialog();"));
    add(new SingleButtonPanel("call", sendButton));
    setDefaultButton(sendButton);
  }

  protected String getPhoneNumberAndPerson(final AddressDO address, final PhoneType phoneType, final String number, final String countryPrefix)
  {
    return StringHelper.listToString(", ", NumberHelper.extractPhonenumber(number, countryPrefix) + ": " + address.getName(), getString(phoneType.getI18nKey()), address
        .getFirstName(), address.getOrganization());
  }

  private void buildAutocompleteEntry(final List<String> list, final AddressDO address, final PhoneType phoneType, final String number)
  {
    if (StringUtils.isBlank(number) == true) {
      return;
    }
    list.add(getPhoneNumberAndPerson(address, phoneType, number, configuration.getStringValue(ConfigurationParam.DEFAULT_COUNTRY_PHONE_PREFIX)));
  }

  @SuppressWarnings("unchecked")
  protected RecentQueue<String> getRecentSearchTermsQueue()
  {
    if (recentSearchTermsQueue == null) {
      recentSearchTermsQueue = (RecentQueue<String>) parentPage.getUserPrefEntry(USER_PREF_KEY_RECENTS);
    }
    if (recentSearchTermsQueue == null) {
      recentSearchTermsQueue = (RecentQueue<String>) parentPage.getUserPrefEntry("org.projectforge.web.address.PhoneCallAction:recentSearchTerms");
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
