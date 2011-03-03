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

package org.projectforge.scripting;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.projectforge.core.ConfigXml;
import org.projectforge.user.I18nHelper;

public class GroovyEngine
{
  private Locale locale;

  private GroovyExecutor groovyExecutor;

  private Map<String, Object> variables = new HashMap<String, Object>();

  public GroovyEngine(final Locale locale)
  {
    this.locale = locale;
    this.variables.put("pf", this);
    this.groovyExecutor = new GroovyExecutor();
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
    return groovyExecutor.executeTemplate(template, variables);
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
   * @param messageKey
   * @param params
   * @see I18nHelper#getLocalizedString(Locale, String)
   */
  public String getString(final String key)
  {
    return I18nHelper.getLocalizedString(locale, key);
  }
}
