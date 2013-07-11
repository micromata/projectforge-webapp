/////////////////////////////////////////////////////////////////////////////
//
// Project   ProjectForge - Chimney Plugin
//
// Copyright 2012, Micromata GmbH
//           All rights reserved.
//
/////////////////////////////////////////////////////////////////////////////

package org.projectforge.plugins.chimney.web.components;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.Panel;

/**
 * A simple component with a text link.
 * @author Sweeps <pf@byte-storm.com>
 */
public class TextLinkPanel extends Panel
{
  private static final long serialVersionUID = -6814329972260829872L;

  private final Link<Void> link;

  private TextLinkPanel(final String id)
  {
    super(id);
    link = new Link<Void>("link") {
      private static final long serialVersionUID = 1L;
      @Override
      public void onClick()
      {
        TextLinkPanel.this.onClick();
      };
    };
    link.setOutputMarkupId(true);
    add(link);
  }

  /**
   * Constructor for displaying a text link
   * @param id Wicket id
   * @param imageResource Resource of the image to display
   */
  public TextLinkPanel(final String id, final String linkText) {
    this(id);
    link.add(new Label("linktext", linkText));
  }

  public void onClick()
  {
  }

}
