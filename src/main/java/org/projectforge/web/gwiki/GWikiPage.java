/////////////////////////////////////////////////////////////////////////////
//
// Project   ProjectForge
//
// Copyright 2001-2009, Micromata GmbH, Kai Reinhard
//           All rights reserved.
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
