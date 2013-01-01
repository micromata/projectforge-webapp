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

package org.projectforge.scripting;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.projectforge.common.NumberHelper;
import org.projectforge.core.ConfigXml;
import org.projectforge.core.Configuration;
import org.projectforge.core.CurrencyFormatter;
import org.projectforge.core.I18nEnum;
import org.projectforge.core.NumberFormatter;
import org.projectforge.fibu.KostFormatter;
import org.projectforge.fibu.KundeDO;
import org.projectforge.fibu.ProjektDO;
import org.projectforge.task.TaskDO;
import org.projectforge.user.I18nHelper;
import org.projectforge.user.PFUserDO;
import org.projectforge.web.HtmlHelper;
import org.projectforge.web.calendar.DateTimeFormatter;
import org.projectforge.web.common.OutputType;
import org.projectforge.web.task.TaskFormatter;

public class GroovyEngine
{
  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(GroovyEngine.class);

  private Locale locale;

  private TimeZone timeZone;

  private boolean htmlFormat = true;

  private GroovyExecutor groovyExecutor;

  private Map<String, Object> variables;

  public GroovyEngine(final Locale locale, final TimeZone timeZone)
  {
    this(new HashMap<String, Object>(), locale, timeZone);
  }

  public GroovyEngine(final Map<String, Object> variables, final Locale locale, final TimeZone timeZone)
  {
    if (locale != null) {
      this.locale = locale;
    } else {
      this.locale = ConfigXml.getInstance().getDefaultLocale();
    }
    if (timeZone != null) {
      this.timeZone = timeZone;
    } else {
      this.timeZone = Configuration.getInstance().getDefaultTimeZone();
    }
    this.variables = variables;
    this.variables.put("pf", this);
    this.groovyExecutor = new GroovyExecutor();
  }

  /**
   * For achieving well-formed XML files you can replace '&lt;% ... %&gt;' by '&lt;groovy&gt; ... &lt;/groovy&gt;' and '&lt;%= ... %&gt;' by
   * '&lt;groovy-out&gt; ... &lt;/groovy-out&gt;'
   * @param template
   * @return
   */
  public String preprocessGroovyXml(final String template)
  {
    if (template == null) {
      return null;
    }
    return template.replaceAll("<groovy>", "<% ").replaceAll("</groovy>", " %>").replaceAll("<groovy-out>", "<%= ").replaceAll(
        "</groovy-out>", " %>");
  }

  /**
   * @param variables
   * @see Map#putAll(Map)
   */
  public void putVariables(final Map<String, Object> variables)
  {
    variables.putAll(variables);
  }

  /**
   * @param variables
   * @return this for chaining.
   * @see Map#putAll(Map)
   */
  public GroovyEngine putVariable(final String key, final Object value)
  {
    variables.put(key, value);
    return this;
  }

  /**
   * @param template
   * @see GroovyExecutor#executeTemplate(String, Map)
   */
  public String executeTemplate(final String template)
  {
    final String content = replaceIncludes(template).replaceAll("#HURZ#", "\\$");
    return groovyExecutor.executeTemplate(content, variables);
  }

  private String replaceIncludes(final String template)
  {
    if (template == null) {
      return null;
    }
    final Pattern p = Pattern.compile("#INCLUDE\\{([0-9\\.a-zA-Z/]*)\\}", Pattern.MULTILINE);
    final StringBuffer buf = new StringBuffer();
    final Matcher m = p.matcher(template);
    while (m.find()) {
      if (m.group(1) != null) {
        final String filename = m.group(1);
        final Object[] res = ConfigXml.getInstance().getContent(filename);
        String content = (String) res[0];
        if (content != null) {
          content = replaceIncludes(content).replaceAll("\\$", "#HURZ#");
          m.appendReplacement(buf, content); // Doesn't work with $ in content
        } else {
          m.appendReplacement(buf, "*** " + filename + " not found! ***");
        }
      }
    }
    m.appendTail(buf);
    return buf.toString();
  }

  /**
   * 
   * @param path
   * @return
   * @see ConfigXml#getContent(String)
   */
  public String executeTemplateFile(final String file)
  {
    final Object[] res = ConfigXml.getInstance().getContent(file);
    final String template = (String) res[0];
    if (template == null) {
      log.error("Template with filename '" + file + "' not found (whether in resource path nor in ProjectForge's application dir.");
      return "";
    }
    return executeTemplate(template);
  }

  /**
   * Gets i18n message.
   * @param messageKey
   * @param params
   * @see I18nHelper#getLocalizedMessage(Locale, String, Object...)
   */
  public String getMessage(String messageKey, Object... params)
  {
    return I18nHelper.getLocalizedMessage(locale, messageKey, params);
  }

  /**
   * Gets i18n string.
   * @param key
   * @see I18nHelper#getLocalizedString(Locale, String)
   */
  public String getI18nString(final String key)
  {
    return I18nHelper.getLocalizedString(locale, key);
  }

  /**
   * @param value
   * @return The value as string using the toString() method or "" if the given value is null.
   */
  public String getString(final Object value)
  {
    if (value == null) {
      return "";
    }
    return htmlFormat ? HtmlHelper.formatText(value.toString(), true) : value.toString();
  }

  /**
   * @param value
   * @return true if the value is null or instance of NullObject, otherwise false.
   */
  public boolean isNull(final Object value)
  {
    if (value == null) {
      return true;
    }
    return value instanceof NullObject;
  }

  /**
   * @param value
   * @see StringUtils#isBlank(String)
   */
  public boolean isBlank(final String value)
  {
    return StringUtils.isBlank(value);
  }

  /**
   * @param value
   * @return true if value is null or value.toString() is blank.
   * @see StringUtils#isBlank(String)
   */
  public boolean isBlank(final Object value)
  {
    if (value == null) {
      return true;
    }
    return StringUtils.isBlank(value.toString());
  }

  /**
   * @param value
   * @return Always true.
   */
  public boolean isBlank(final NullObject value)
  {
    return true;
  }

  /**
   * @param value
   * @see StringUtils#isEmpty(String)
   */
  public boolean isEmpty(final String value)
  {
    return StringUtils.isEmpty(value);
  }

  /**
   * @param value
   * @return true if value is null or value.toString() is empty.
   * @see StringUtils#isEmpty(String)
   */
  public boolean isEmpty(final Object value)
  {
    if (value == null) {
      return true;
    }
    return StringUtils.isEmpty(value.toString());
  }

  /**
   * @param value
   * @return Always true.
   */
  public boolean isEmpty(final NullObject value)
  {
    return true;
  }

  public void log(final String message)
  {
    log.info(message);
  }

  /**
   * @param value
   * @return The given string itself or "" if value is null.
   */
  public String getString(final String value)
  {
    if (value == null) {
      return "";
    }
    return htmlFormat ? HtmlHelper.formatText(value, true) : value;
  }

  /**
   * Gets i18n string.
   * @param i18nEnum
   * @see I18nHelper#getLocalizedString(Locale, String)
   * @see I18nEnum#getI18nKey()
   */
  public String getString(final I18nEnum i18nEnum)
  {
    if (i18nEnum == null) {
      return "";
    }
    return I18nHelper.getLocalizedString(locale, i18nEnum.getI18nKey());
  }

  /**
   * Gets the customer's name.
   * @param customer
   * @see KostFormatter#formatKunde(KundeDO)
   */
  public String getString(final KundeDO customer)
  {
    if (customer == null) {
      return "";
    }
    return KostFormatter.formatKunde(customer);
  }

  /**
   * Gets the project's name.
   * @param project
   * @see KostFormatter#formatProjekt(ProjektDO)
   */
  public String getString(final ProjektDO project)
  {
    if (project == null) {
      return "";
    }
    return KostFormatter.formatProjekt(project);
  }

  /**
   * Gets the user's name (full name).
   * @param user
   * @see PFUserDO#getFullname()
   */
  public String getString(final PFUserDO user)
  {
    if (user == null) {
      return "";
    }
    return user.getFullname();
  }

  /**
   * Gets the user's name (full name).
   * @param user
   * @see PFUserDO#getFullname()
   */
  public String getString(final Number value)
  {
    if (value == null) {
      return "";
    }
    return NumberHelper.getAsString(value);
  }

  /**
   * Gets the user's name (full name).
   * @param user
   * @see PFUserDO#getFullname()
   */
  public String getString(final TaskDO task)
  {
    if (task == null) {
      return "";
    }
    final TaskFormatter taskFormatter = TaskFormatter.instance();
    if (taskFormatter != null) {
      return taskFormatter.getTaskPath(task.getId(), true, OutputType.PLAIN);
    } else {
      // Only for test-cases (if task tree is not initialized)
      return task.getTitle();
    }
  }

  public String getCurrency(final BigDecimal value)
  {
    if (value == null) {
      return "";
    }
    return CurrencyFormatter.format(value, locale);
  }

  public String getString(final BigDecimal value)
  {
    if (value == null) {
      return "";
    }
    return NumberFormatter.format(value, locale);
  }

  public String getString(final java.sql.Date date)
  {
    if (date == null) {
      return "";
    }
    return DateTimeFormatter.instance().getFormattedDate(date, locale, timeZone);
  }

  public String getString(final java.util.Date date)
  {
    if (date == null) {
      return "";
    }
    return DateTimeFormatter.instance().getFormattedDateTime(date, locale, timeZone);
  }
}
