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

import org.apache.wicket.markup.head.CssReferenceHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptReferenceHeaderItem;

public class WicketRenderHeadUtils
{
  private static final String[] JAVASCRIPT_FILES = { //
    "scripts/jquery/1.8.2/jquery-1.8.2.js", //
    "include/bootstrap/js/bootstrap.js", //
    "scripts/jqueryui/1.9.2/jquery-ui-1.9.2.custom.js", //
    "scripts/contextmenu/jquery.contextmenu.js", //
    "scripts/adminica-2.2/prefixfree/prefixfree.js", //
    "scripts/adminica-2.2/adminica_ui.js", //
    "scripts/adminica-2.2/adminica_mobile.js", //
    "scripts/adminica-2.2/adminica_load.js", //
    "scripts/projectforge.js" //
  };

  private static final String[] CSS_FILES = { //
    "styles/google-fonts/google-fonts.css", //
    // "http://fonts.googleapis.com/css?family=Droid+Sans:regular&amp;subset=latin", //
    "styles/jqueryui/1.9.2/cupertino/jquery-ui-1.9.2.custom.css", //
    "styles/table.css", //
    "scripts/contextmenu/css/jquery.contextmenu.css", //
    "styles/adminica-2.2/main.css", //
    // "styles/adminica-2.2/mobile.css", //
    "include/bootstrap/css/bootstrap.css", //
    "styles/projectforge-main.css", //
    "styles/projectforge-bootstrap.css", //
    "styles/projectforge-form.css", // before projectforge.css!
    "styles/projectforge.css" //
  };

  private static final String[] SELECT2_JAVASCRIPT_FILES = { //
  "scripts/select2/select2.js"};

  /**
   * Jquery, bootstrap, jqueryui and uquery.contextmenu.js.
   */
  public static void renderMainJavaScriptIncludes(final IHeaderResponse response)
  {
    for (final String url : JAVASCRIPT_FILES) {
      response.render(JavaScriptReferenceHeaderItem.forUrl(url));
    }
  }

  /**
   * Jquery, bootstrap, jqueryui and uquery.contextmenu.js.
   */
  public static void renderMainCSSIncludes(final IHeaderResponse response)
  {
    for (final String url : CSS_FILES) {
      response.render(CssReferenceHeaderItem.forUrl(url));
    }
  }

  /**
   * Renders all main JavaScript files and "select2.js".
   */
  public static void renderSelect2JavaScriptIncludes(final IHeaderResponse response)
  {
    renderMainJavaScriptIncludes(response);
    for (final String url : SELECT2_JAVASCRIPT_FILES) {
      response.render(JavaScriptReferenceHeaderItem.forUrl(url));
    }
  }
}
