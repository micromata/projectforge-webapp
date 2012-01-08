/////////////////////////////////////////////////////////////////////////////
//
// Project ProjectForge Community Edition
//         www.projectforge.org
//
// Copyright (C) 2001-2012 Kai Reinhard (k.reinhard@micromata.com)
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

package org.projectforge.web.gwiki;

import javax.servlet.http.Cookie;

import org.apache.commons.lang.StringUtils;
import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.JavascriptPackageResource;
import org.projectforge.web.wicket.AbstractSecuredPage;

public class GWikiPage extends AbstractSecuredPage
{
  public GWikiPage(final PageParameters parameters)
  {
    super(parameters);

    final Cookie frameCookie = this.getWebRequestCycle().getWebRequest().getCookie("frameSrc");
    String pageId = parameters.getString("pageId");

    add(JavascriptPackageResource.getHeaderContribution("scripts/gwiki-iframe.js"));
    add(JavascriptPackageResource.getHeaderContribution("scripts/stringutils.js"));

    if (pageId == null) {
      if (frameCookie != null && StringUtils.isNotBlank(frameCookie.getValue())) {
        pageId = frameCookie.getValue();
      } else {
        pageId = "Index";
      }
    }

    body.add(new GWikiInlineFrame("gwiki-frame", pageId));

    // TODO: (cclaus) anything like this to hide the content menu
    // contentMenuArea.setVisible(false);
  }

  @Override
  protected String getTitle()
  {
    return "GWiki";
  }
}
