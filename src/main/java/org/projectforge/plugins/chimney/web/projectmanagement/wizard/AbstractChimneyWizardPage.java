/////////////////////////////////////////////////////////////////////////////
//
// Project   ProjectForge - Chimney Plugin
//
// Copyright 2012, Micromata GmbH
//           All rights reserved.
//
/////////////////////////////////////////////////////////////////////////////

package org.projectforge.plugins.chimney.web.projectmanagement.wizard;

import org.apache.wicket.Page;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.projectforge.plugins.chimney.web.AbstractSecuredChimneyPage;
import org.projectforge.plugins.chimney.web.components.ImageLinkPanel;
import org.projectforge.plugins.chimney.web.components.TextLinkPanel;
import org.projectforge.plugins.chimney.web.navigation.PaginationLinkDescription;

/**
 * An abstract page for previous/next navigation between wizard pages.
 * Text and image links are supported.
 * @author Sweeps <pf@byte-storm.com>
 */
public abstract class AbstractChimneyWizardPage extends AbstractSecuredChimneyPage
{
  private static final long serialVersionUID = 7032975460607345599L;

  public AbstractChimneyWizardPage(final PageParameters parameters)
  {
    super(parameters);
    init();
  }

  public AbstractChimneyWizardPage(final PageParameters parameters, final boolean deferNavCreation)
  {
    super(parameters, deferNavCreation);
    init();
  }

  public AbstractChimneyWizardPage(final PageParameters parameters, final PageParameters linkParameters, final boolean deferNavCreation)
  {
    super(parameters, linkParameters, deferNavCreation);
    init();
  }

  private void init() {
    addPreviousLink();
    addNextLink();
  }

  private void addPreviousLink()
  {
    final PaginationLinkDescription linkDesc = new PaginationLinkDescription();
    linkDesc.linkText = getString("plugins.chimney.editwbsnode.previous");
    this.onRenderPreviousLink(linkDesc);
    linkDesc.sanitize();
    final Panel prevLink = getPrevNextLinkPanel("prevLink", linkDesc, false);
    prevLink.setVisible(linkDesc.displayLink);

    body.add(prevLink);
  }

  private void addNextLink()
  {
    final PaginationLinkDescription linkDesc = new PaginationLinkDescription();
    linkDesc.linkText = getString("plugins.chimney.editwbsnode.next");
    this.onRenderNextLink(linkDesc);
    linkDesc.sanitize();
    final Panel nextLink = getPrevNextLinkPanel("nextLink", linkDesc, true);
    nextLink.setVisible(linkDesc.displayLink);

    body.add(nextLink);
  }

  private Panel getPrevNextLinkPanel(final String id, final PaginationLinkDescription linkDesc, final boolean isNextLink)
  {
    if (linkDesc.linkImage == null) {
      return new TextLinkPanel(id, linkDesc.linkText) {
        private static final long serialVersionUID = -7729000086170527392L;
        @Override
        public void onClick()
        {
          setResponsePage(isNextLink?getNextPage():getPreviousPage());
        }
      };
    } else {
      return new ImageLinkPanel(id, linkDesc.linkImage, linkDesc.width, linkDesc.height, null, linkDesc.linkText) {
        private static final long serialVersionUID = 199461697721719099L;

        @Override
        public void onClick()
        {
          setResponsePage(isNextLink?getNextPage():getPreviousPage());
        }
      };
    }
  }

  /**
   * You must set the appropriate field of linkDescription to display a "Next" link, especially the {@link PaginationLinkDescription#displayLink} property.
   * @param linkDescription
   */
  protected void onRenderNextLink(final PaginationLinkDescription linkDescription)
  {
    // does nothing by default
  }

  /**
   * You must set the appropriate field of linkDescription to display a "Previous" link, especially the {@link PaginationLinkDescription#displayLink} property.
   * @param linkDescription
   */
  protected void onRenderPreviousLink(final PaginationLinkDescription linkDescription)
  {
    // does nothing by default
  }

  /**
   * Override this method to return a Page as link target for the "Previous" link.
   * If {@link #hasPreviousPage()} return false, this method is never called.
   * @return link target for the "Previous" link
   */
  protected Page getPreviousPage()
  {
    throw new UnsupportedOperationException();
  }

  /**
   * Override this method to return a Page as link target for the "Next" link.
   * If {@link #hasNextPage()} return false, this method is never called.
   * @return link target for the "Next" link
   */
  protected Page getNextPage()
  {
    throw new UnsupportedOperationException();
  }

}
