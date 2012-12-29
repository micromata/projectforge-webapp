/////////////////////////////////////////////////////////////////////////////
//
// Project ProjectForge Community Edition
//         www.projectforge.org
//
// Copyright (C) 2001-2012 Kai Reinhard (k.reinhard@micromata.de)
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

import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.projectforge.web.WebConfiguration;

public class WicketRenderHeadUtils
{
  /**
   * Jquery, bootstrap, jqueryui and uquery.contextmenu.js.
   */
  public static void renderMainJavaScriptIncludes(final IHeaderResponse response)
  {
    // Load JQuery
    if (WebConfiguration.isDevelopmentMode() == true) {
      renderHeadJavaScriptUrl(response, "scripts/jquery/1.8.2/jquery-1.8.2.js");
      renderHeadJavaScriptUrl(response, "include/bootstrap/js/bootstrap.js");
      renderHeadJavaScriptUrl(response, "scripts/jqueryui/1.9.2/jquery-ui-1.9.2.custom.js");
    } else {
      renderHeadJavaScriptUrl(response, "scripts/jquery/1.8.2/jquery-1.8.2.min.js");
      renderHeadJavaScriptUrl(response, "include/bootstrap/js/bootstrap.min.js");
      renderHeadJavaScriptUrl(response, "scripts/jqueryui/1.9.2/jquery-ui-1.9.2.custom.min.js");
    }
    renderHeadJavaScriptUrl(response, "scripts/contextmenu/jquery.contextmenu.js");
  }

  public static void renderHeadJavaScriptUrl(final IHeaderResponse response, final String url)
  {
    response.render(JavaScriptHeaderItem.forUrl(url));
  }
}
