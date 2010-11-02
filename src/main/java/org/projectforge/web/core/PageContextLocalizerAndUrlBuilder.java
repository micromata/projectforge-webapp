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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;

import net.sourceforge.stripes.util.UrlBuilder;

import org.apache.commons.lang.Validate;
import org.projectforge.user.PFUserContext;


/**
 *
 * @author Kai Reinhard (k.reinhard@micromata.de)
 */
@Deprecated
public class PageContextLocalizerAndUrlBuilder implements LocalizerAndUrlBuilder
{
  private PageContext pageContext;
  
  public PageContextLocalizerAndUrlBuilder(PageContext pageContext)
  {
    this.pageContext = pageContext;
  }

  public String buildUrl(String url)
  {
    Validate.notNull(pageContext);
    Validate.notNull(url);
    HttpServletRequest request = (HttpServletRequest) pageContext.getRequest();
    HttpServletResponse response = (HttpServletResponse) pageContext.getResponse();
    String contextPath = request.getContextPath();

    // Append the context path, but only if the user didn't already
    if (url.startsWith("/") == true && "/".equals(contextPath) == false && url.contains(contextPath + "/") == false) {
      url = contextPath + url;
    }
    // Add all the parameters and reset the href attribute; pass to false here because
    // the HtmlTagSupport will HtmlEncode the ampersands for us
    UrlBuilder builder = new UrlBuilder(PFUserContext.getLocale(), url, false);
    return response.encodeURL(builder.toString());
  }

  public String getLocalizedMessage(String messageKey, Object... params)
  {
    return PFUserContext.getLocalizedMessage(messageKey, params);
  }

  public String getString(String i18nKey)
  {
    return PFUserContext.getLocalizedString(i18nKey);
  }
}
