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

package org.projectforge.web.wicket;

import org.apache.wicket.Response;
import org.projectforge.user.PFUserContext;
import org.projectforge.web.core.LocalizerAndUrlBuilder;


/**
 * 
 * @author Kai Reinhard (k.reinhard@micromata.de)
 */
public class WicketLocalizerAndUrlBuilder implements LocalizerAndUrlBuilder
{
  private Response response;

  public WicketLocalizerAndUrlBuilder(final Response response)
  {
    this.response = response;
  }

  public String buildUrl(String url)
  {
    return WicketUtils.getUrl(response, url, true);
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
