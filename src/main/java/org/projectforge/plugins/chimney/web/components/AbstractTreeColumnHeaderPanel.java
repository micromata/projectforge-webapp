/////////////////////////////////////////////////////////////////////////////
//
// Project   ProjectForge - Chimney Plugin
//
// Copyright 2012, Micromata GmbH
//           All rights reserved.
//
/////////////////////////////////////////////////////////////////////////////

package org.projectforge.plugins.chimney.web.components;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;

/**
 * A panel with a header label, a "collapse all" and "expand all" link.
 * Must override  * {@link #onCollapseAllClicked()} and
 * {@link #onExpandAllClicked()} with appropriate actions.
 * @author Sweeps <pf@byte-storm.com>
 */
public abstract class AbstractTreeColumnHeaderPanel extends Panel
{
  private static final long serialVersionUID = 1L;

  private final IModel<String> model;

  public AbstractTreeColumnHeaderPanel(final String id, final IModel<String> model)
  {
    super(id);
    this.model = model;

  }

  @Override
  protected void onInitialize() {
    super.onInitialize();
    add(new Label("headerLabel", model));
    add(new Link<Void>("expandLink") {
      private static final long serialVersionUID = 1L;

      @Override
      public void onClick()
      {
        onExpandAllClicked();
      }

    }.add(AttributeModifier.replace("title", getString("plugins.chimney.projecttree.expandall"))));
    add(new Link<Void>("collapseLink") {
      private static final long serialVersionUID = 1L;

      @Override
      public void onClick()
      {
        onCollapseAllClicked();
      }

    }.add(AttributeModifier.replace("title", getString("plugins.chimney.projecttree.collapseall"))));
  }
  /**
   * Triggered when the "collapse all" link is clicked
   */
  public abstract void onCollapseAllClicked();

  /**
   * Triggered when the "expand all" link is clicked
   */
  public abstract void onExpandAllClicked();

}
