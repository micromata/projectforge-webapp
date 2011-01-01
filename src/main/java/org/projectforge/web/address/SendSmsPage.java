/////////////////////////////////////////////////////////////////////////////
//
// Project ProjectForge Community Edition
//         www.projectforge.org
//
// Copyright (C) 2001-2011 Kai Reinhard (k.reinhard@me.com)
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

import java.io.IOException;
import java.util.Date;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.lang.StringUtils;
import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.projectforge.address.AddressDO;
import org.projectforge.address.AddressDao;
import org.projectforge.address.PhoneType;
import org.projectforge.common.NumberHelper;
import org.projectforge.common.StringHelper;
import org.projectforge.core.Configuration;
import org.projectforge.core.ConfigurationParam;
import org.projectforge.web.URLHelper;
import org.projectforge.web.calendar.DateTimeFormatter;
import org.projectforge.web.wicket.AbstractSecuredPage;

public class SendSmsPage extends AbstractSecuredPage
{
  public final static String PARAMETER_KEY_ADDRESS_ID = "addressId";

  public final static String PARAMETER_KEY_PHONE_TYPE = "phoneType";

  public final static String PARAMETER_KEY_NUMBER = "number";

  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(SendSmsPage.class);

  @SpringBean(name = "addressDao")
  private AddressDao addressDao;

  @SpringBean(name = "configuration")
  private Configuration configuration;

  private SendSmsForm form;

  private String result;

  @SuppressWarnings("serial")
  public SendSmsPage(PageParameters parameters)
  {
    super(parameters);
    form = new SendSmsForm(this);
    body.add(form);
    form.init();
    final String javaScript = "function showSendQuestionDialog() {\n  return window.confirm('"
        + getString("address.sendSms.sendMessageQuestion")
        + "');\n}\n";
    body.add(new Label("showSendQuestionDialog", javaScript).setEscapeModelStrings(false));
    form.add(new Label("result", new PropertyModel<String>(this, "result")) {
      @Override
      public boolean isVisible()
      {
        return StringUtils.isNotBlank(result);
      }
    });
    parseParameters(parameters);
  }

  @Override
  protected PageParameters getBookmarkPageExtendedParameters()
  {
    final PageParameters pageParameters = new PageParameters();
    pageParameters.put(PARAMETER_KEY_NUMBER, getData().getPhoneNumber());
    return pageParameters;
  }

  private void parseParameters(final PageParameters parameters)
  {
    if (parameters.containsKey(PARAMETER_KEY_ADDRESS_ID) == true) {
      String str = parameters.getString(PARAMETER_KEY_ADDRESS_ID);
      final Integer addressId = NumberHelper.parseInteger(str);
      if (addressId == null)
        return;
      final AddressDO address = addressDao.getById(addressId);
      if (address == null)
        return;
      if (parameters.containsKey(PARAMETER_KEY_PHONE_TYPE) == true) {
        log.info(parameters.get(PARAMETER_KEY_PHONE_TYPE));
        final Object type = parameters.get(PARAMETER_KEY_PHONE_TYPE);
        final PhoneType phoneType;
        if (type instanceof PhoneType) {
          phoneType = (PhoneType)type;
        } else {
          phoneType = PhoneType.valueOf((String)type);
        }
        String number = null;
        if (phoneType == PhoneType.MOBILE) {
          number = address.getMobilePhone();
        } else if (phoneType == PhoneType.PRIVATE_MOBILE) {
          number = address.getPrivateMobilePhone();
        }
        if (number != null) {
          getData().setPhoneNumber(
              SendSmsForm.getPhoneNumberAndPerson(address, number, configuration
                  .getStringValue(ConfigurationParam.DEFAULT_COUNTRY_PHONE_PREFIX)));
        }
      }
    }
    if (parameters.containsKey(PARAMETER_KEY_NUMBER) == true) {
      final String number = parameters.getString(PARAMETER_KEY_NUMBER);
      if (StringUtils.isNotBlank(number) == true) {
        getData().setPhoneNumber(number);
      }
    }
  }

  protected void send()
  {
    final String number = NumberHelper.extractPhonenumber(getData().getPhoneNumber(), configuration
        .getStringValue(ConfigurationParam.DEFAULT_COUNTRY_PHONE_PREFIX));
    if (StringUtils.isBlank(configuration.getSmsUrl()) == true) {
      log.error("Servlet url for sending sms not configured. SMS not supported.");
      return;
    }
    log.info("User sends message to destination number: '" + StringHelper.hideStringEnding(number, 'x', 3));
    final HttpClient client = new HttpClient();
    String url = this.configuration.getSmsUrl();
    url = StringUtils.replaceOnce(url, "#number", number);
    url = StringUtils.replaceOnce(url, "#message", URLHelper.encode(getData().getMessage()));
    final GetMethod method = new GetMethod(url);
    String errorKey = null;
    result = "";
    try {
      client.executeMethod(method);
      String response = method.getResponseBodyAsString();
      if (response == null) {
        errorKey = getString("address.sendSms.sendMessage.result.unknownError");
      } else if (response.startsWith("0") == true) {
        result = getLocalizedMessage("address.sendSms.sendMessage.result.successful", number, DateTimeFormatter.instance()
            .getFormattedDateTime(new Date()));
      } else if (response.startsWith("1") == true) {
        errorKey = "address.sendSms.sendMessage.result.messageMissed";
      } else if (response.startsWith("2") == true) {
        errorKey = "address.sendSms.sendMessage.result.wrongOrMissingNumber";
      } else if (response.startsWith("3") == true) {
        errorKey = "address.sendSms.sendMessage.result.messageToLarge";
      } else {
        errorKey = getString("address.sendSms.sendMessage.result.unknownError");
      }
    } catch (HttpException ex) {
      errorKey = "Call failed. Please contact administrator.";
      log.fatal(errorKey + ": " + this.configuration.getSmsUrl() + StringHelper.hideStringEnding(String.valueOf(number), 'x', 3));
      throw new RuntimeException(ex);
    } catch (IOException ex) {
      errorKey = "Call failed. Please contact administrator.";
      log.fatal(errorKey + ": " + this.configuration.getSmsUrl() + StringHelper.hideStringEnding(String.valueOf(number), 'x', 3));
      throw new RuntimeException(ex);
    }
    if (errorKey != null) {
      form.addError(errorKey);
    }
  }

  @Override
  protected void onAfterRender()
  {
    super.onAfterRender();
    result = null;
  }

  private SendSmsData getData()
  {
    return form.data;
  }

  @Override
  protected String getTitle()
  {
    return getString("address.sendSms.title");
  }
}
