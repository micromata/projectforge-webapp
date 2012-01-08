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
import org.projectforge.common.NumberHelper;
import org.projectforge.common.RecentQueue;
import org.projectforge.common.StringHelper;
import org.projectforge.core.Configuration;
import org.projectforge.core.ConfigurationParam;
import org.projectforge.web.wicket.AbstractForm;
import org.projectforge.web.wicket.autocompletion.PFAutoCompleteTextField;
import org.projectforge.web.wicket.components.MaxLengthTextArea;
import org.projectforge.web.wicket.components.SingleButtonPanel;

public class SendSmsForm extends AbstractForm<SendSmsData, SendSmsPage>
{
  private static final long serialVersionUID = -2138017238114715368L;

  private static final String USER_PREF_KEY_RECENTS = "messagingReceivers";

  @SpringBean(name = "addressDao")
  private AddressDao addressDao;

  @SpringBean(name = "configuration")
  private Configuration configuration;

  protected SendSmsData data;

  private RecentQueue<String> recentSearchTermsQueue;

  public SendSmsForm(final SendSmsPage parentPage)
  {
    super(parentPage);
    data = new SendSmsData();
  }

  protected static String getPhoneNumberAndPerson(final AddressDO address, final String number, final String countryPrefix)
  {
    return StringHelper.listToString(", ", NumberHelper.extractPhonenumber(number, countryPrefix) + ": " + address.getName(), address
        .getFirstName(), address.getOrganization());
  }

  private void buildAutocompleteEntry(final List<String> list, final AddressDO address, final String number)
  {
    if (StringUtils.isBlank(number) == true) {
      return;
    }
    list.add(getPhoneNumberAndPerson(address, number, configuration.getStringValue(ConfigurationParam.DEFAULT_COUNTRY_PHONE_PREFIX)));
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
          buildAutocompleteEntry(list, address, address.getMobilePhone());
          buildAutocompleteEntry(list, address, address.getPrivateMobilePhone());
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
    data.setMessage(getUser().getFullname() + ". " + getString("address.sendSms.doNotReply"));
    final MaxLengthTextArea messageTextArea = new MaxLengthTextArea("message", new PropertyModel<String>(data, "message"), 160);
    add(messageTextArea);

    final Button resetButton = new Button("button", new Model<String>(getString("reset"))) {
      @Override
      public final void onSubmit()
      {
        data.setMessage("");
        data.setPhoneNumber("");
      }
    };
    resetButton.setDefaultFormProcessing(false);
    final SingleButtonPanel resetButtonPanel = new SingleButtonPanel("reset", resetButton);
    resetButtonPanel.setVisible(false);
    add(resetButtonPanel);
    final Button sendButton = new Button("button", new Model<String>(getString("send"))) {
      @Override
      public final void onSubmit()
      {
        parentPage.send();
      }
    };
    sendButton.add(new SimpleAttributeModifier("onclick", "return showSendQuestionDialog();"));
    add(new SingleButtonPanel("send", sendButton));
    setDefaultButton(sendButton);
  }

  @SuppressWarnings("unchecked")
  protected RecentQueue<String> getRecentSearchTermsQueue()
  {
    if (recentSearchTermsQueue == null) {
      recentSearchTermsQueue = (RecentQueue<String>) parentPage.getUserPrefEntry(USER_PREF_KEY_RECENTS);
    }
    if (recentSearchTermsQueue == null) {
      recentSearchTermsQueue = (RecentQueue<String>) parentPage.getUserPrefEntry(this.getClass().getName() + ":recentSearchTerms");
      if (recentSearchTermsQueue != null) {
        // Old entries:
        parentPage.putUserPrefEntry(USER_PREF_KEY_RECENTS, recentSearchTermsQueue, true);
        parentPage.removeUserPrefEntry(this.getClass().getName() + ":recentSearchTerms");
      }
    }
    if (recentSearchTermsQueue == null) {
      recentSearchTermsQueue = new RecentQueue<String>();
      parentPage.putUserPrefEntry(USER_PREF_KEY_RECENTS, recentSearchTermsQueue, true);
    }
    return recentSearchTermsQueue;
  }
}
