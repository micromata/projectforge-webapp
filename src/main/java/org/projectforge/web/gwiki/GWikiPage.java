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
import org.projectforge.web.wicket.AbstractSecuredPage;

public class GWikiPage extends AbstractSecuredPage 
{
  public GWikiPage(final PageParameters parameters)
  {
    super(parameters);
    body.add(new GWikiInlineFrame("gwiki-frame", "Index"));
  }

  @Override
  protected String getTitle()
  {
    return "GWiki";
  }
}
