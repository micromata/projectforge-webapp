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

package org.projectforge.web.core;

import java.text.MessageFormat;
import java.util.Locale;

import net.sourceforge.stripes.action.ActionBean;
import net.sourceforge.stripes.localization.LocalizationUtility;
import net.sourceforge.stripes.validation.ValidationError;

public class I18nError implements ValidationError
{
  /**
   * 
   */
  private static final long serialVersionUID = 2490515710360557087L;

  protected String fieldNameKey;

  protected String fieldValue;

  protected String actionPath;

  protected String messageKey;

  protected Object[] parameter;
  
  protected Class< ? extends ActionBean> actionBean;

  /**
   * Constructs a simple error message.
   * 
   * @param message the String message (template) to display
   * @param parameter zero or more parameters for replacement into the message
   */
  public I18nError(ActionBean actionBean, String messageKey, Object... parameter)
  {
    this.actionBean = actionBean.getClass();
    this.messageKey = messageKey;
    this.parameter = parameter;
  }

  public String getActionPath()
  {
    return this.actionPath;
  }

  /**
   * @return null
   * @see net.sourceforge.stripes.validation.ValidationError#getFieldName()
   */
  public String getFieldName()
  {
    return this.fieldNameKey;
  }

  /**
   * @return null
   * @see net.sourceforge.stripes.validation.ValidationError#getFieldValue()
   */
  public String getFieldValue()
  {
    return this.fieldValue;
  }

  public void setActionPath(String actionPath)
  {
    this.actionPath = actionPath;
  }

  public void setFieldName(String name)
  {
    this.fieldNameKey = name;
  }

  public void setFieldValue(String value)
  {
    this.fieldValue = value;
  }

  public String getMessage(Locale locale)
  {
    String template = LocalizationUtility.getErrorMessage(locale, messageKey);
    if (template == null) {
      return messageKey;
    }
    return MessageFormat.format(template, this.parameter);
  }

  public Class< ? extends ActionBean> getBeanclass()
  {
    return actionBean;
  }

  public void setBeanclass(Class< ? extends ActionBean> actionBean)
  {
    this.actionBean = actionBean;
  }
}
