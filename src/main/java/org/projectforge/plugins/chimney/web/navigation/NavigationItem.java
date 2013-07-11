/////////////////////////////////////////////////////////////////////////////
//
// Project   ProjectForge
//
// Copyright 2001-2009, Micromata GmbH, Kai Reinhard
//           All rights reserved.
//
/////////////////////////////////////////////////////////////////////////////

package org.projectforge.plugins.chimney.web.navigation;

import java.io.Serializable;

import org.apache.wicket.Page;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.ExternalLink;
import org.apache.wicket.model.ResourceModel;

/**
 * Navigation item for displaying localized navigation links and labels
 */
public class NavigationItem implements Serializable
{
  private static final long serialVersionUID = -1390164514728795350L;

  private final Class<? extends Page> linkTarget;
  private final String linkTextRessource;

  /**
   * Creates a navigation entry
   * @param linkTarget Class of the link target's page
   * @param linkTextRessource Ressource string for the i18n link text
   */
  public NavigationItem(final Class<? extends Page> linkTarget, final String linkTextRessource) {
    if (linkTarget == null || linkTextRessource == null)
      throw new IllegalArgumentException("Parameters must not be null");
    this.linkTarget = linkTarget;
    this.linkTextRessource = linkTextRessource;
  }

  /**
   * @param linkId Wicket id for creating a BookmarkablePageLink
   * @param labelId Wicket id for the label of the link
   * @return A freshly created BookmarkablePageLink<Void> that can be added to a page
   */
  public BookmarkablePageLink<Void> getLinkInstance(final String linkId, final String labelId) {
    BookmarkablePageLink<Void> link;
    link = new BookmarkablePageLink<Void>(linkId, linkTarget);
    link.add(new Label(labelId, new ResourceModel(linkTextRessource)));
    return link;
  }

  /**
   * Creates a link that links to nowhere
   * @param linkId Wicket id for creating a null link
   * @param labelId Wicket id for the label of the link
   * @return A freshly created BookmarkablePageLink<Void> that can be added to a page
   */
  public ExternalLink getNullLinkInstance(final String linkId, final String labelId) {
    final ExternalLink link = new ExternalLink(linkId, "#");
    link.add(new Label(labelId, new ResourceModel(linkTextRessource)));
    return link;
  }

}
