/////////////////////////////////////////////////////////////////////////////
//
// Project   ProjectForge
//
// Copyright 2001-2009, Micromata GmbH, Kai Reinhard
//           All rights reserved.
//
/////////////////////////////////////////////////////////////////////////////

package org.projectforge.web.gwiki;

import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.protocol.http.WebApplication;

/**
 * Component to render an iframe with a specific target.
 * 
 * @author Kai Reinhard (k.reinhard@micromata.de)
 * 
 */
public class GWikiInlineFrame extends WebMarkupContainer
{
  private static final long serialVersionUID = 3903792810732132670L;

  private String target = null;

  public GWikiInlineFrame(String id, String target)
  {
    super(id);

    this.target = target;
  }

  private String getTargetURL()
  {
    String url = WebApplication.get().getServletContext().getContextPath() + "/gwiki/" + target;
    return url;
  }

  @Override
  protected final void onComponentTag(final ComponentTag tag)
  {
    checkComponentTag(tag, "iframe");

    tag.put("src", getTargetURL());

    super.onComponentTag(tag);
  }

}
