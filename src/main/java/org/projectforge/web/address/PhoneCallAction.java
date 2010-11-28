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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.DontValidate;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.StrictBinding;
import net.sourceforge.stripes.action.UrlBinding;
import net.sourceforge.stripes.validation.Validate;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.projectforge.address.AddressDO;
import org.projectforge.address.AddressDao;
import org.projectforge.address.FormOfAddress;
import org.projectforge.common.LabelValueBean;
import org.projectforge.common.NumberHelper;
import org.projectforge.common.RecentQueue;
import org.projectforge.common.StringHelper;
import org.projectforge.core.BaseDao;
import org.projectforge.core.Configuration;
import org.projectforge.core.ConfigurationParam;
import org.projectforge.user.PFUserContext;
import org.projectforge.user.UserDao;
import org.projectforge.web.core.BaseAction;
import org.projectforge.web.core.ExtendedActionBean;

/**
 * For direct calling with support of telephone system.
 */
@StrictBinding
@UrlBinding("/secure/address/PhoneCall.action")
@BaseAction(jspUrl = "/WEB-INF/jsp/address/phoneCall.jsp")
public class PhoneCallAction extends ExtendedActionBean
{
  private static final Logger log = Logger.getLogger(PhoneCallAction.class);

  private static final String SEPARATOR = " | ";

  private Configuration configuration;

  private UserDao userDao;

  private AddressDao addressDao;

  private AddressDO address;

  private Integer addressId;

  private boolean callNow;

  private String form;

  private String phoneNumber;

  private String phoneType;

  private String myCurrentPhoneId;

  private String resultStatus;

  private Date lastSuccessfulPhoneCall;

  private RecentQueue<String> recentSearchTermsQueue;

  public void setConfiguration(final Configuration configuration)
  {
    this.configuration = configuration;
  }

  public void setUserDao(final UserDao userDao)
  {
    this.userDao = userDao;
  }

  public void setAddressDao(final AddressDao addressDao)
  {
    this.addressDao = addressDao;
  }

  public List<LabelValueBean<String, String>> getTelephoneIds()
  {
    List<LabelValueBean<String, String>> list = new ArrayList<LabelValueBean<String, String>>();
    String[] ids = userDao.getPersonalPhoneIdentifiers(PFUserContext.getUser());
    if (ids == null) {
      list.add(new LabelValueBean<String, String>(getLocalizedString("user.personalPhoneIdentifiers.pleaseDefine"), "--"));
    } else {
      list.add(new LabelValueBean<String, String>(getLocalizedString("pleaseChoose"), "--"));
      for (String id : ids) {
        list.add(new LabelValueBean<String, String>(id, id));
      }
    }
    return list;
  }

  @SuppressWarnings("unchecked")
  protected RecentQueue<String> getRecentSearchTermsQueue()
  {
    if (recentSearchTermsQueue == null) {
      recentSearchTermsQueue = (RecentQueue<String>) getContext().getEntry(this.getClass().getName() + ":recentSearchTerms");
    }
    if (recentSearchTermsQueue == null) {
      recentSearchTermsQueue = new RecentQueue<String>();
      getContext().putEntry(this.getClass().getName() + ":recentSearchTerms", recentSearchTermsQueue, true);
    }
    return recentSearchTermsQueue;
  }

  /**
   * Gets the recent search strings as Json object.
   */
  public String getRecentSearchTerms()
  {
    if (getRecentSearchTermsQueue().size() > 0) {
      return BaseDao.buildJsonRows(false, recentSearchTermsQueue.getRecents());
    }
    return "[]";
  }

  public AddressDO getAddress()
  {
    if (address != null || addressId == null) {
      return address;
    }
    address = addressDao.getById(this.addressId);
    if (address == null) {
      return address;
    }
    if (address.getForm() != FormOfAddress.UNKNOWN) {
      form = getLocalizedString(address.getForm().getI18nKey());
    } else {
      form = null;
    }
    return address;
  }

  @Validate
  public Integer getAddressId()
  {
    return addressId;
  }

  public void setAddressId(Integer id)
  {
    this.addressId = id;
  }

  public String getForm()
  {
    return form;
  }

  @Validate(required = true)
  public String getPhoneNumber()
  {
    return phoneNumber;
  }

  public void setPhoneNumber(String phoneNumber)
  {
    this.phoneNumber = phoneNumber;
  }

  public void setPhoneNumber(String phoneNumber, boolean extract)
  {
    if (extract == true) {
      phoneNumber = extractPhonenumber(phoneNumber);
    }
    this.phoneNumber = phoneNumber;
  }

  @Validate
  public String getPhoneType()
  {
    return phoneType;
  }

  /**
   * Value 'business', 'mobile', 'private', 'privateMobile', ... use-able by the caller for defining the phone number to call from the given
   * address.
   * @param phoneType
   */
  public void setPhoneType(String phoneType)
  {
    this.phoneType = phoneType;
  }

  /**
   * Returns always false.
   */
  @Validate
  public boolean isCallNow()
  {
    return false;
  }

  /**
   * If call is set to true, a phone call is directly instantiated in default handler.
   */
  public void setCallNow(boolean callNow)
  {
    this.callNow = callNow;
  }

  /**
   * @return the time stamp of the last successful phone call.
   */
  public Date getLastSuccessfulPhoneCall()
  {
    return lastSuccessfulPhoneCall;
  }

  /**
   * For automatically dialing from this phone id.
   */
  @Validate(required = true)
  public String getMyCurrentPhoneId()
  {
    if (myCurrentPhoneId == null) {
      myCurrentPhoneId = (String) getContext().getEntry(this.getClass().getName() + ":myCurrentPhoneId");
    }
    return myCurrentPhoneId;
  }

  public void setMyCurrentPhoneId(String myCurrentPhoneId)
  {
    if (StringUtils.equals(myCurrentPhoneId, "--") == true) {
      myCurrentPhoneId = null;
    }
    this.myCurrentPhoneId = myCurrentPhoneId;
    if (this.myCurrentPhoneId != null) {
      String[] ids = userDao.getPersonalPhoneIdentifiers(PFUserContext.getUser());
      if (ArrayUtils.contains(ids, myCurrentPhoneId) == false) {
        return;
      }
    }
    getContext().putEntry(this.getClass().getName() + ":myCurrentPhoneId", this.myCurrentPhoneId, true);
  }

  public String getResult()
  {
    return resultStatus;
  }

  /**
   * For special phone numbers: id:# or # | name.
   * @return true, if the phone number was successfully processed.
   */
  private boolean processPhoneNumber()
  {
    if (StringUtils.isNotEmpty(this.phoneNumber) == true) {
      if (this.phoneNumber.startsWith("id:") == true && this.phoneNumber.length() > 3) {
        Integer id = NumberHelper.parseInteger(this.phoneNumber.substring(3));
        if (id != null) {
          this.phoneNumber = "";
          this.addressId = id;
          if (getAddress() != null) {
            if (StringUtils.isNotEmpty(this.address.getBusinessPhone()) == true) {
              setPhoneNumber(this.address.getBusinessPhone(), true);
            } else if (StringUtils.isNotEmpty(this.address.getMobilePhone()) == true) {
              setPhoneNumber(this.address.getMobilePhone(), true);
            }
          }
        }
        return true;
      } else if (this.phoneNumber.indexOf(SEPARATOR) >= 0) {
        final int pos = this.phoneNumber.indexOf(SEPARATOR);
        final String rest = this.phoneNumber.substring(pos + SEPARATOR.length());
        final int numberPos = rest.indexOf('#');
        this.phoneNumber = this.phoneNumber.substring(0, pos);
        if (numberPos > 0) {
          Integer id = NumberHelper.parseInteger(rest.substring(numberPos + 1));
          if (id != null) {
            this.address = addressDao.getById(id);
            if (this.address != null) {
              this.addressId = this.address.getId();
            }
          } else {
            this.address = null;
            this.addressId = null;
          }
        } else {
          this.address = null;
          this.addressId = null;
        }
        return true;
      }
    }
    return false;
  }

  @DefaultHandler
  @DontValidate
  public Resolution init()
  {
    boolean extracted = processPhoneNumber();
    if (getAddress() == null) {
      return getInputPage();
    }
    if (extracted == false && StringUtils.isNotEmpty(this.phoneType) == true) {
      if ("business".equals(this.phoneType) == true) {
        setPhoneNumber(address.getBusinessPhone(), true);
      } else if ("mobile".equals(this.phoneType) == true) {
        setPhoneNumber(address.getMobilePhone(), true);
      } else if ("private".equals(this.phoneType) == true) {
        setPhoneNumber(address.getPrivatePhone(), true);
      } else if ("privateMobile".equals(this.phoneType) == true) {
        setPhoneNumber(address.getPrivateMobilePhone(), true);
      }
    }
    if (callNow == true) {
      if (validate() == false) {
        return getInputPage();
      }
      return callNow();
    }
    return getInputPage();
  }

  @DontValidate
  public Resolution searchAutocomplete()
  {
    String q = getAjaxAutocompleteValue();
    final String result = addressDao.getAutocompletion(q, true, true, "name", "firstName", "organization");
    return getJsonResolution(result);
  }

  private String extractPhonenumber(String number)
  {
    final String result = NumberHelper.extractPhonenumber(number, configuration
        .getStringValue(ConfigurationParam.DEFAULT_COUNTRY_PHONE_PREFIX));
    if (StringUtils.isNotEmpty(result) == true
        && StringUtils.isNotEmpty(configuration.getTelephoneSystemNumber()) == true
        && result.startsWith(configuration.getTelephoneSystemNumber()) == true) {
      return result.substring(configuration.getTelephoneSystemNumber().length());
    }
    return result;
  }

  /**
   * Execute call: Uses HttpClient for connecting asterisk telephone system.
   * @return
   */
  public Resolution call()
  {
    if (StringUtils.isBlank(this.phoneNumber) == true) {
      return getInputPage();
    }
    boolean extracted = processPhoneNumber();
    if (validate() == false) {
      return getInputPage();
    }
    if (extracted == true) {
      return getInputPage();
    }
    this.phoneNumber = extractPhonenumber(this.phoneNumber);
    return callNow();
  }

  private boolean validate()
  {
    boolean result = true;
    if (StringUtils.isBlank(configuration.getTelephoneSystemUrl()) == true) {
      addGlobalError("address.phoneCall.notConfigured");
    }
    if (StringUtils.isBlank(this.myCurrentPhoneId) == true) {
      addError("myCurrentPhoneId", "validation.required.valueNotPresent", getLocalizedMessage("address.myCurrentPhoneId"));
      result = false;
    }
    if (StringUtils.containsOnly(this.phoneNumber, "0123456789+-/() ") == false) {
      addError("phoneNumber", "address.phoneCall.number.invalid");
      result = false;
    }
    return result;
  }

  private Resolution callNow()
  {
    if (StringUtils.isBlank(configuration.getTelephoneSystemUrl()) == true) {
      log.error("Telephone system url not configured. Phone calls not supported.");
      return getInputPage();
    }
    log.info("User initiates direct call from phone with id '"
        + myCurrentPhoneId
        + "' to destination numer: "
        + StringHelper.hideStringEnding(getPhoneNumber(), 'x', 3));
    resultStatus = null;
    final StringBuffer buf = new StringBuffer();
    buf.append(this.phoneNumber).append(SEPARATOR);
    if (getAddress() != null
        && StringHelper.isIn(this.phoneNumber, extractPhonenumber(address.getBusinessPhone()),
            extractPhonenumber(address.getMobilePhone()), extractPhonenumber(address.getPrivatePhone()), extractPhonenumber(address
                .getPrivateMobilePhone())) == true) {
      buf.append(this.address.getFirstName()).append(" ").append(this.address.getName());
      if (this.phoneNumber.equals(extractPhonenumber(address.getMobilePhone())) == true) {
        buf.append(", ").append(getLocalizedString("address.phone.mobile"));
      } else if (this.phoneNumber.equals(extractPhonenumber(address.getPrivatePhone())) == true) {
        buf.append(", ").append(getLocalizedString("address.phone.private"));
      }
      buf.append(" #").append(this.address.getId());
    } else {
      buf.append("???");
    }
    getRecentSearchTermsQueue().append(buf.toString());
    final HttpClient client = new HttpClient();
    String url = this.configuration.getTelephoneSystemUrl();
    url = StringUtils.replaceOnce(url, "#source", this.myCurrentPhoneId);
    url = StringUtils.replaceOnce(url, "#target", this.phoneNumber);
    final String urlProtected = StringHelper.hideStringEnding(url, 'x', 3);
    final GetMethod method = new GetMethod(url);
    try {
      this.lastSuccessfulPhoneCall = new Date();
      client.executeMethod(method);
      resultStatus = method.getResponseBodyAsString();
      if ("0".equals(resultStatus) == true) {
        resultStatus = getLocalizedString("address.phoneCall.result.successful");
      } else if ("1".equals(resultStatus) == true) {
        resultStatus = getLocalizedString("address.phoneCall.result.callingError");
      } else if ("2".equals(resultStatus) == true) {
        resultStatus = getLocalizedString("address.phoneCall.result.wrongSourceNumber");
      } else if ("3".equals(resultStatus) == true) {
        resultStatus = getLocalizedString("address.phoneCall.result.wrongDestinationNumber");
      }
    } catch (HttpException ex) {
      resultStatus = "Call failed. Please contact administrator.";
      log.fatal(resultStatus + ": " + urlProtected);
      throw new RuntimeException(ex);
    } catch (IOException ex) {
      resultStatus = "Call failed. Please contact administrator.";
      log.fatal(resultStatus + ": " + urlProtected);
      throw new RuntimeException(ex);
    }
    return getInputPage();
  }

  /**
   * Forward to address list action.
   */
  public Resolution cancel()
  {
    return new ForwardResolution("null");
  }

  protected Resolution getInputPage()
  {
    return new ForwardResolution(getJspUrl());
  }

  @Override
  protected Logger getLogger()
  {
    return log;
  }

  @Override
  protected void storeToFlowScope()
  {
  }

}
