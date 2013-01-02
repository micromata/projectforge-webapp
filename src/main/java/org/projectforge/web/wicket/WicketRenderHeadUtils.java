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

package org.projectforge.web.wicket;

import org.apache.wicket.markup.head.CssReferenceHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptReferenceHeaderItem;
import org.projectforge.web.WebConfiguration;

public class WicketRenderHeadUtils
{
  private static final String[][] JAVASCRIPT_FILES_DEF = { //
    { "scripts/jquery/1.8.2/jquery-1.8.2", ".min"},//
    { "include/bootstrap/js/bootstrap", ".min"}, //
    { "scripts/jqueryui/1.9.2/jquery-ui-1.9.2.custom", ".min"}, //
    { "scripts/contextmenu/jquery.contextmenu", ""}, //
    { "scripts/adminica-2.2/prefixfree/prefixfree", "-min"}, //
    { "scripts/adminica-2.2/adminica_ui", ""}, // modified!
    { "scripts/adminica-2.2/adminica_mobile", "-min"}, //
    { "scripts/adminica-2.2/adminica_load", ""}, // modified!
    { "scripts/projectforge", ""} //
  };

  private static final String[] JAVASCRIPT_FILES;

  private static final String[][] CSS_FILES_DEF = { //
    { "styles/google-fonts/google-fonts", ""}, //
    // "http://fonts.googleapis.com/css?family=Droid+Sans:regular&amp;subset=latin", //
    { "styles/jqueryui/1.9.2/cupertino/jquery-ui-1.9.2.custom", ".min"}, //
    { "styles/table", ""}, //
    { "scripts/contextmenu/css/jquery.contextmenu", ""}, //
    { "styles/adminica-2.2/main", ""}, //
    // "styles/adminica-2.2/mobile", //
    { "include/bootstrap/css/bootstrap", ".min"}, //
    { "styles/projectforge-main", ""}, //
    { "styles/projectforge-bootstrap", ""}, //
    { "styles/projectforge-form", ""}, // before projectforge.css!
    { "styles/projectforge", ""} //
  };

  private static final String[] CSS_FILES;

  private static final String[] SELECT2_JAVASCRIPT_FILES = { //
  "scripts/select2/select2.js"};

  private static final String[][] AUTOGROW_JAVASCRIPT_FILES_DEF = { //
    { "scripts/autogrow/jquery.autogrowtextarea", ""}};

  private static final String[] AUTOGROW_JAVASCRIPT_FILES;

  static {
    JAVASCRIPT_FILES = new String[JAVASCRIPT_FILES_DEF.length];
    CSS_FILES = new String[CSS_FILES_DEF.length];
    AUTOGROW_JAVASCRIPT_FILES = new String[AUTOGROW_JAVASCRIPT_FILES_DEF.length];
    if (WebConfiguration.isDevelopmentMode() == true) {
      for (int i = 0; i < JAVASCRIPT_FILES_DEF.length; i++) {
        JAVASCRIPT_FILES[i] = JAVASCRIPT_FILES_DEF[i][0] + ".js";
      }
      for (int i = 0; i < CSS_FILES_DEF.length; i++) {
        CSS_FILES[i] = CSS_FILES_DEF[i][0] + ".css";
      }
      for (int i = 0; i < AUTOGROW_JAVASCRIPT_FILES_DEF.length; i++) {
        AUTOGROW_JAVASCRIPT_FILES[i] = AUTOGROW_JAVASCRIPT_FILES_DEF[i][0] + ".js";
      }
    } else {
      for (int i = 0; i < JAVASCRIPT_FILES_DEF.length; i++) {
        JAVASCRIPT_FILES[i] = JAVASCRIPT_FILES_DEF[i][0] + JAVASCRIPT_FILES_DEF[i][1] + ".js";
      }
      for (int i = 0; i < CSS_FILES_DEF.length; i++) {
        CSS_FILES[i] = CSS_FILES_DEF[i][0] + CSS_FILES_DEF[i][1] + ".css";
      }
      for (int i = 0; i < AUTOGROW_JAVASCRIPT_FILES_DEF.length; i++) {
        AUTOGROW_JAVASCRIPT_FILES[i] = AUTOGROW_JAVASCRIPT_FILES_DEF[i][0] + AUTOGROW_JAVASCRIPT_FILES_DEF[i][1] + ".js";
      }
    }
  }

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

  /**
   * Jquery, bootstrap, jqueryui and uquery.contextmenu.js.
   */
  public static void renderAutogrowJavaScriptIncludes(final IHeaderResponse response)
  {
    renderMainJavaScriptIncludes(response);
    for (final String url : AUTOGROW_JAVASCRIPT_FILES) {
      response.render(JavaScriptReferenceHeaderItem.forUrl(url));
    }
  }


}
