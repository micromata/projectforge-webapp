/////////////////////////////////////////////////////////////////////////////
//
// Project ProjectForge Community Edition
//         www.projectforge.org
//
// Copyright (C) 2001-2013 Kai Reinhard (k.reinhard@micromata.de)
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

package org.projectforge.core;

import java.util.TimeZone;

public enum ConfigurationParam
{
  SYSTEM_ADMIN_E_MAIL("systemAdministratorEMail", ConfigurationType.STRING), //
  ORGANIZATION("organization", ConfigurationType.TEXT), //
  MESSAGE_OF_THE_DAY("messageOfTheDay", ConfigurationType.TEXT), //
  DEFAULT_TIMEZONE("timezone", ConfigurationType.TIME_ZONE, TimeZone.getDefault().getID()), //
  DATE_FORMATS("dateFormats", ConfigurationType.STRING, "MM/dd/yyyy;dd/MM/yyyy;dd.MM.yyyy;yyyy-MM-dd"), //
  EXCEL_DATE_FORMATS("excelDateFormats", ConfigurationType.STRING, "MM/DD/YYYY;DD/MM/YYYY;DD.MM.YYYY"), //
  FEEDBACK_E_MAIL("feedbackEMail", ConfigurationType.STRING), //
  FIBU_DEFAULT_VAT("fibu.defaultVAT", ConfigurationType.PERCENT), //
  COST_CONFIGURED("fibu.costConfigured", ConfigurationType.BOOLEAN), //
  DEFAULT_TASK_ID_4_ADDRESSES("defaultTask4Addresses", ConfigurationType.TASK), //
  DEFAULT_TASK_ID_4_BOOKS("defaultTask4Books", ConfigurationType.TASK), //
  DEFAULT_COUNTRY_PHONE_PREFIX("countryPhonePrefix", ConfigurationType.STRING, "+49"), //
  MEB_SMS_RECEIVING_PHONE_NUMBER("mebSMSReceivingPhoneNumber", ConfigurationType.STRING);

  private String key;

  private ConfigurationType type;

  private String defaultStringValue;

  /**
   * The key will be used e. g. for i18n.
   * @return
   */
  public String getKey()
  {
    return key;
  }

  public ConfigurationType getType()
  {
    return type;
  }

  /**
   * @return The full i18n key including the i18n prefix "administration.configuration.param.".
   */
  public String getI18nKey()
  {
    return "administration.configuration.param." + key;
  }

  public String getDefaultStringValue()
  {
    return defaultStringValue;
  }

  /**
   * @return The full i18n key including the i18n prefix "administration.configuration.param." and the suffix ".description".
   */
  public String getDescriptionI18nKey()
  {
    return "administration.configuration.param." + key + ".description";
  }

  ConfigurationParam(final String key, final ConfigurationType type)
  {
    this(key, type, null);
  }

  ConfigurationParam(final String key, final ConfigurationType type, final String defaultStringValue)
  {
    this.key = key;
    this.type = type;
    this.defaultStringValue = defaultStringValue;
  }
}
