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

import java.io.IOException;
import java.util.Date;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.lang.StringUtils;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.projectforge.address.AddressDO;
import org.projectforge.address.AddressDao;
import org.projectforge.common.NumberHelper;
import org.projectforge.common.RecentQueue;
import org.projectforge.common.StringHelper;
import org.projectforge.core.ConfigXml;
import org.projectforge.core.Configuration;
import org.projectforge.core.ConfigurationParam;
import org.projectforge.web.calendar.DateTimeFormatter;
import org.projectforge.web.wicket.AbstractSecuredPage;

public class PhoneCallPage extends AbstractSecuredPage
{
  private static final long serialVersionUID = -5040319693295350276L;

  public final static String PARAMETER_KEY_ADDRESS_ID = "addressId";

  public final static String PARAMETER_KEY_NUMBER = "number";

  private static final String SEPARATOR = " | ";

  private static final String USER_PREF_KEY_MY_RECENT_PHONE_ID = "PhoneCall:recentPhoneId";

  private static final String USER_PREF_KEY_RECENT_CALLS = "PhoneCall:recentCalls";

  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(PhoneCallPage.class);

  @SpringBean(name = "addressDao")
  private AddressDao addressDao;

  private final PhoneCallForm form;

  String result;

  private RecentQueue<String> recentCallsQueue;

  public PhoneCallPage(final PageParameters parameters)
  {
    super(parameters);
    log.warn("**** WICKET 1.5 migration: add bookmarkable parameters");
    form = new PhoneCallForm(this);
    body.add(form);
    form.init();
    parseParameters(parameters);
  }

  // @Override
  // protected PageParameters getBookmarkPageExtendedParameters()
  // {
  // final PageParameters pageParameters = new PageParameters();
  // final String phoneNumber = form.getPhoneNumber();
  // if (phoneNumber != null) {
  // pageParameters.add(PARAMETER_KEY_NUMBER, phoneNumber);
  // }
  // return pageParameters;
  // }

  private void parseParameters(final PageParameters parameters)
  {
    if (parameters.get(PARAMETER_KEY_ADDRESS_ID) != null) {
      final String str = parameters.get(PARAMETER_KEY_ADDRESS_ID).toString();
      final Integer addressId = NumberHelper.parseInteger(str);
      if (addressId == null)
        return;
      final AddressDO address = addressDao.getById(addressId);
      if (address == null) {
        return;
      }
      form.address = address;
    }
    if (parameters.get(PARAMETER_KEY_NUMBER) != null) {
      final String number = parameters.get(PARAMETER_KEY_NUMBER).toString();
      if (StringUtils.isNotBlank(number) == true) {
        form.setPhoneNumber(extractPhonenumber(number));
      }
    }
  }

  /**
   * For special phone numbers: id:# or # | name.
   * @return true, if the phone number was successfully processed.
   */
  private boolean processPhoneNumber()
  {
    final String phoneNumber = form.getPhoneNumber();
    if (StringUtils.isNotEmpty(phoneNumber) == true) {
      if (phoneNumber.startsWith("id:") == true && phoneNumber.length() > 3) {
        final Integer id = NumberHelper.parseInteger(phoneNumber.substring(3));
        if (id != null) {
          form.setPhoneNumber("");
          final AddressDO address = addressDao.getById(id);
          if (address != null) {
            form.setAddress(address);
            if (StringUtils.isNotEmpty(address.getBusinessPhone()) == true) {
              setPhoneNumber(address.getBusinessPhone(), true);
            } else if (StringUtils.isNotEmpty(address.getMobilePhone()) == true) {
              setPhoneNumber(address.getMobilePhone(), true);
            }
          }
        }
        return true;
      } else if (phoneNumber.indexOf(SEPARATOR) >= 0) {
        final int pos = phoneNumber.indexOf(SEPARATOR);
        final String rest = phoneNumber.substring(pos + SEPARATOR.length());
        final int numberPos = rest.indexOf('#');
        form.setPhoneNumber(phoneNumber.substring(0, pos));
        if (numberPos > 0) {
          final Integer id = NumberHelper.parseInteger(rest.substring(numberPos + 1));
          if (id != null) {
            final AddressDO address = addressDao.getById(id);
            if (address != null) {
              form.setAddress(address);
            }
          } else {
            form.setAddress(null);
          }
        } else {
          form.setAddress(null);
        }
        return true;
      }
    }
    return false;
  }

  public void setPhoneNumber(String phoneNumber, final boolean extract)
  {
    if (extract == true) {
      phoneNumber = extractPhonenumber(phoneNumber);
    }
    form.setPhoneNumber(phoneNumber);
  }

  String extractPhonenumber(final String number)
  {
    final String result = NumberHelper.extractPhonenumber(number,
        Configuration.getInstance().getStringValue(ConfigurationParam.DEFAULT_COUNTRY_PHONE_PREFIX));
    if (StringUtils.isNotEmpty(result) == true
        && StringUtils.isNotEmpty(ConfigXml.getInstance().getTelephoneSystemNumber()) == true
        && result.startsWith(ConfigXml.getInstance().getTelephoneSystemNumber()) == true) {
      return result.substring(ConfigXml.getInstance().getTelephoneSystemNumber().length());
    }
    return result;
  }

  @Override
  protected void onBeforeRender()
  {
    super.onBeforeRender();
    final String rawInput = form.numberTextField.getRawInput();
    if (StringUtils.isNotEmpty(rawInput) == true) {
      form.setPhoneNumber(rawInput);
    }
    processPhoneNumber();
    form.numberTextField.modelChanged();
  }

  void call()
  {
    final boolean extracted = processPhoneNumber();
    if (extracted == true) {
      return;
    }
    if (StringUtils.containsOnly(form.getPhoneNumber(), "0123456789+-/() ") == false) {
      form.addError("address.phoneCall.number.invalid");
      return;
    }
    form.setPhoneNumber(extractPhonenumber(form.getPhoneNumber()));
    callNow();
  }

  private void callNow()
  {
    if (StringUtils.isBlank(ConfigXml.getInstance().getTelephoneSystemUrl()) == true) {
      log.error("Telephone system url not configured. Phone calls not supported.");
      return;
    }
    log.info("User initiates direct call from phone with id '"
        + form.getMyCurrentPhoneId()
        + "' to destination numer: "
        + StringHelper.hideStringEnding(form.getPhoneNumber(), 'x', 3));
    result = null;
    final StringBuffer buf = new StringBuffer();
    buf.append(form.getPhoneNumber()).append(SEPARATOR);
    final AddressDO address = form.getAddress();
    if (address != null
        && StringHelper.isIn(form.getPhoneNumber(), extractPhonenumber(address.getBusinessPhone()),
            extractPhonenumber(address.getMobilePhone()), extractPhonenumber(address.getPrivatePhone()),
            extractPhonenumber(address.getPrivateMobilePhone())) == true) {
      buf.append(address.getFirstName()).append(" ").append(address.getName());
      if (form.getPhoneNumber().equals(extractPhonenumber(address.getMobilePhone())) == true) {
        buf.append(", ").append(getString("address.phone.mobile"));
      } else if (form.getPhoneNumber().equals(extractPhonenumber(address.getPrivatePhone())) == true) {
        buf.append(", ").append(getString("address.phone.private"));
      }
      buf.append(" #").append(address.getId());
    } else {
      buf.append("???");
    }
    final HttpClient client = new HttpClient();
    String url = ConfigXml.getInstance().getTelephoneSystemUrl();
    url = StringUtils.replaceOnce(url, "#source", form.getMyCurrentPhoneId());
    url = StringUtils.replaceOnce(url, "#target", form.getPhoneNumber());
    final String urlProtected = StringHelper.hideStringEnding(url, 'x', 3);
    final GetMethod method = new GetMethod(url);
    String errorKey = null;
    try {
      form.lastSuccessfulPhoneCall = new Date();
      client.executeMethod(method);
      final String resultStatus = method.getResponseBodyAsString();
      if ("0".equals(resultStatus) == true) {
        result = DateTimeFormatter.instance().getFormattedDateTime(new Date()) + ": " + getString("address.phoneCall.result.successful");
        getRecentCallsQueue().append(buf.toString());
      } else if ("2".equals(resultStatus) == true) {
        errorKey = "address.phoneCall.result.wrongSourceNumber";
      } else if ("3".equals(resultStatus) == true) {
        errorKey = "address.phoneCall.result.wrongDestinationNumber";
      } else {
        errorKey = "address.phoneCall.result.callingError";
      }
    } catch (final HttpException ex) {
      result = "Call failed. Please contact administrator.";
      log.fatal(result + ": " + urlProtected);
      throw new RuntimeException(ex);
    } catch (final IOException ex) {
      result = "Call failed. Please contact administrator.";
      log.fatal(result + ": " + urlProtected);
      throw new RuntimeException(ex);
    }
    if (errorKey != null) {
      form.addError(errorKey);
    }
  }

  @SuppressWarnings("unchecked")
  protected RecentQueue<String> getRecentCallsQueue()
  {
    if (recentCallsQueue == null) {
      recentCallsQueue = (RecentQueue<String>) getUserPrefEntry(USER_PREF_KEY_RECENT_CALLS);
    }
    if (recentCallsQueue == null) {
      recentCallsQueue = new RecentQueue<String>();
      putUserPrefEntry(USER_PREF_KEY_RECENT_CALLS, recentCallsQueue, true);
    }
    return recentCallsQueue;
  }

  protected String getRecentMyPhoneId()
  {
    return (String) getUserPrefEntry(USER_PREF_KEY_MY_RECENT_PHONE_ID);
  }

  protected void setRecentMyPhoneId(final String myPhoneId)
  {
    putUserPrefEntry(USER_PREF_KEY_MY_RECENT_PHONE_ID, myPhoneId, true);
  }

  @Override
  protected void onAfterRender()
  {
    super.onAfterRender();
    result = null;
  }

  @Override
  protected String getTitle()
  {
    return getString("address.phoneCall.title");
  }
}
