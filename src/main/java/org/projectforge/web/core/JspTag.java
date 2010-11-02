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
import java.util.ResourceBundle;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;

import org.projectforge.user.PFUserContext;
import org.projectforge.web.HtmlHelper;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;


public class JspTag extends TagSupport
{
  private static final long serialVersionUID = -2044762145056268548L;

  // private static final Logger log = org.apache.log4j.Logger.getLogger(JspTag.class);

  private Locale locale;

  private ResourceBundle resourceBundle;

  protected HtmlHelper htmlHelper;

  public JspTag()
  {
    super();
  }

  /**
   * Appends <not visible> element (italic and gray colored) to the given StringBuffer. This is used by the text for displaying not
   * accessible fields.
   */
  public void appendNotVisible(StringBuffer sb)
  {
    sb.append("<span style=\"font-style:italic; color: gray;\">&lt;").append(resolveMessage("notVisible")).append("&gt;</span>");
  }

  public String getNotVisibleString()
  {
    StringBuffer sb = new StringBuffer();
    appendNotVisible(sb);
    return sb.toString();
  }

  public void setHtmlHelper(HtmlHelper htmlHelper)
  {
    this.htmlHelper = htmlHelper;
  }

  /**
   * @return locale if given otherwise default Locale.
   * @see Locale#getDefault()
   */
  public Locale getLocale()
  {
    if (locale != null) {
      return locale;
    }
    return resolveLocale();
  }

  private Locale resolveLocale()
  {
    return PFUserContext.getLocale(this.pageContext.getRequest().getLocale());
  }

  public void setLocale(Object locale)
  {
    if (locale instanceof Locale) {
      Locale lc = (Locale) locale;
      this.locale = lc;
    } else if (locale instanceof String) {
      this.locale = new Locale((String) locale);
    }
  }

  /**
   * When will this method be called? TODO: Life cycle of TagSupport.
   * @see javax.servlet.jsp.tagext.TagSupport#release()
   */
  @Override
  public void release()
  {
    super.release();
    locale = null;
    resourceBundle = null;
  }

  protected void init()
  {
    synchronized (this) {
      // TODO: Life cycle not clear: So re-init this tag if the locale is changed:
      if (resolveLocale().equals(locale) == true) {
        return;
      }
      locale = resolveLocale();
      resourceBundle = BaseActionBean.getResourceBundle(locale);
      WebApplicationContext webApplicationContext = WebApplicationContextUtils.getWebApplicationContext(pageContext.getSession()
          .getServletContext());
      ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext(new String[] {}, webApplicationContext);
      ctx.getBeanFactory().autowireBeanProperties(this, AutowireCapableBeanFactory.AUTOWIRE_BY_NAME, false);
    }
  }

  @Override
  public int doStartTag() throws JspException
  {
    init();
    return EVAL_PAGE;
  }

  protected String resolveMessage(String i18nKey)
  {
    String result = resourceBundle.getString(i18nKey);
    if (result == null) {
      return "???" + i18nKey + "???";
    }
    return result;
  }

  protected String resolveMessage(String i18nKey, Object... args)
  {
    String template = resourceBundle.getString(i18nKey);
    if (template == null) {
      return "???" + i18nKey + "???";
    }
    String result = MessageFormat.format(template, args);
    if (result == null) {
      return "???" + i18nKey + "???";
    }
    return result;
  }
}
