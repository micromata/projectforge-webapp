/////////////////////////////////////////////////////////////////////////////
//
// Project   ProjectForge - Chimney Plugin
//
// Copyright 2012, Micromata GmbH
//           All rights reserved.
//
/////////////////////////////////////////////////////////////////////////////

package org.projectforge.plugins.chimney.web.components;

import org.apache.wicket.Component;
import org.apache.wicket.extensions.markup.html.repeater.tree.table.TreeColumn;
import org.apache.wicket.model.IModel;

/**
 * A custom {@link TreeColumn} that has buttons for "expand all"
 * and "collapse all" in its header. You have to override
 * {@link #onCollapseAllClicked()} and {@link #onExpandAllClicked()}
 * to use this component.
 * @author Sweeps <pf@byte-storm.com>
 */
public abstract class AbstractExpandableCollapsibleTreeColumn<T> extends TreeColumn<T, String> {

  private static final long serialVersionUID = 1L;

  public AbstractExpandableCollapsibleTreeColumn(final IModel<String> displayModel, final String sortProperty)
  {
    super(displayModel, sortProperty);
  }

  public AbstractExpandableCollapsibleTreeColumn(final IModel<String> displayModel)
  {
    super(displayModel);
  }

  @Override
  public Component getHeader(final String componentId)
  {
    return new AbstractTreeColumnHeaderPanel(componentId, getDisplayModel()) {
      private static final long serialVersionUID = 1L;

      @Override
      public void onCollapseAllClicked()
      {
        AbstractExpandableCollapsibleTreeColumn.this.onCollapseAllClicked();
      }

      @Override
      public void onExpandAllClicked()
      {
        AbstractExpandableCollapsibleTreeColumn.this.onExpandAllClicked();
      }

    };
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
