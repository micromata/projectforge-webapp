/////////////////////////////////////////////////////////////////////////////
//
// Project   ProjectForge
//
// Copyright 2001-2009, Micromata GmbH, Kai Reinhard
//           All rights reserved.
//
/////////////////////////////////////////////////////////////////////////////

package org.projectforge.web.gwiki;

import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.JavascriptPackageResource;
import org.projectforge.web.wicket.AbstractSecuredPage;

public class GWikiPage extends AbstractSecuredPage
{
  public GWikiPage(final PageParameters parameters)
  {
    super(parameters);

    add(JavascriptPackageResource.getHeaderContribution("scripts/gwiki-iframe.js"));

    final GWikiInlineFrame inlineFrame = new GWikiInlineFrame("gwiki-frame", "Index");
    body.add(inlineFrame);

    // TODO: (cclaus) anything like this to hide the content menu
    // contentMenuArea.setVisible(false);
  }

  @Override
  protected String getTitle()
  {
    return "GWiki";
  }
}
