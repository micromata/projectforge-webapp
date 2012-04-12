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

package org.projectforge.web.wicket.mobileflowlayout;

import org.apache.wicket.markup.repeater.RepeatingView;
import org.projectforge.web.wicket.flowlayout.DivPanel;
import org.projectforge.web.wicket.flowlayout.DivType;
import org.projectforge.web.wicket.flowlayout.FieldsetPanel;
import org.projectforge.web.wicket.flowlayout.FormHeadingPanel;
import org.projectforge.web.wicket.flowlayout.GridBuilder;
import org.projectforge.web.wicket.flowlayout.GridBuilderImpl;

/**
 * @author Kai Reinhard (k.reinhard@micromata.de)
 * 
 */
public class GridBuilderMobileImpl extends GridBuilderImpl
{
  private static final long serialVersionUID = 134863232462613937L;

  public GridBuilderMobileImpl(final RepeatingView parent)
  {
    this.parentRepeatingView = parent;
  }

  public GridBuilderMobileImpl(final DivPanel parent)
  {
    this.parentDivPanel = parent;
  }

  /**
   * Throws {@link UnsupportedOperationException}.
   */
  @Override
  public GridBuilder newGrid8(final String id)
  {
    throw new UnsupportedOperationException();
  }

  /**
   * Throws {@link UnsupportedOperationException}.
   */
  @Override
  public GridBuilder newGrid8(final String id, final boolean clearfix)
  {
    throw new UnsupportedOperationException();
  }

  /**
   * Throws {@link UnsupportedOperationException}.
   */
  @Override
  public GridBuilder newGrid8()
  {
    throw new UnsupportedOperationException();
  }

  /**
   * Throws {@link UnsupportedOperationException}.
   */
  @Override
  public GridBuilder newGrid8(final boolean clearfix)
  {
    throw new UnsupportedOperationException();
  }

  /**
   * Throws {@link UnsupportedOperationException}.
   */
  @Override
  public GridBuilder newGrid16(final String id)
  {
    throw new UnsupportedOperationException();
  }

  /**
   * Throws {@link UnsupportedOperationException}.
   */
  @Override
  public GridBuilder newGrid16(final String id, final boolean clearfix)
  {
    throw new UnsupportedOperationException();
  }

  /**
   * Throws {@link UnsupportedOperationException}.
   */
  @Override
  public GridBuilder newGrid16()
  {
    throw new UnsupportedOperationException();
  }

  /**
   * Throws {@link UnsupportedOperationException}.
   */
  @Override
  public GridBuilder newGrid16(final boolean clearfix)
  {
    throw new UnsupportedOperationException();
  }

  /**
   * @return new block panel.
   */
  @Override
  public GridBuilder newBlockPanel()
  {
    // final DivPanel divPanel = new DivPanel(newParentChildId());
    // getParent().add(divPanel);
    // gridPanel = new DivPanel(DivPanel.CHILD_ID, DivType.TOGGLE_CONTAINER);
    // blockPanel = new DivPanel(gridPanel.newChildId(), DivType.BLOCK);
    // gridPanel.add(blockPanel);
    // current = blockPanel;
    return this;
  }

  /**
   * Throws {@link UnsupportedOperationException}.
   */
  @Override
  public String newColumnsPanelId()
  {
    throw new UnsupportedOperationException();
  }

  /**
   * Throws {@link UnsupportedOperationException}.
   */
  @Override
  public GridBuilder addColumnsPanel(final DivPanel colPanel)
  {
    throw new UnsupportedOperationException();
  }

  /**
   * Throws {@link UnsupportedOperationException}.
   */
  @Override
  public GridBuilder newColumnsPanel()
  {
    throw new UnsupportedOperationException();
  }

  /**
   * Throws {@link UnsupportedOperationException}.
   */
  @Override
  public GridBuilder newColumnPanel(final DivType length)
  {
    throw new UnsupportedOperationException();
  }

  /**
   * Throws {@link UnsupportedOperationException}.
   */
  @Override
  public GridBuilder newColumnPanel(final DivType length, final boolean newBlock4NonWideScreen)
  {
    throw new UnsupportedOperationException();
  }

  /**
   * Throws {@link UnsupportedOperationException}.
   */
  @Override
  public String newColumnPanelId()
  {
    throw new UnsupportedOperationException();
  }

  /**
   * Throws {@link UnsupportedOperationException}.
   */
  @Override
  public GridBuilder addColumnPanel(final DivPanel colPanel, final DivType length)
  {
    throw new UnsupportedOperationException();
  }

  /**
   * @see org.projectforge.web.wicket.flowlayout.GridBuilder#getPanel()
   */
  @Override
  public DivPanel getPanel()
  {
    return current;
  }

  /**
   * Throws {@link UnsupportedOperationException}.
   */
  @Override
  public FormHeadingPanel newFormHeading(final String label)
  {
    throw new UnsupportedOperationException();
  }

  /**
   * @see org.projectforge.web.wicket.flowlayout.GridBuilder#newFieldset(java.lang.String)
   */
  @Override
  public FieldsetPanel newFieldset(final String label)
  {
    return new FieldsetPanel(current, label);
  }

  /**
   * @see org.projectforge.web.wicket.flowlayout.GridBuilder#newFieldset(java.lang.String, boolean)
   */
  @Override
  public FieldsetPanel newFieldset(final String label, final boolean multipleChildren)
  {
    return new FieldsetPanel(current, label, multipleChildren);
  }

  /**
   * @see org.projectforge.web.wicket.flowlayout.GridBuilder#newFieldset(java.lang.String, java.lang.String)
   */
  @Override
  public FieldsetPanel newFieldset(final String labelText, final String labelDescription)
  {
    return new FieldsetPanel(current, labelText, labelDescription);
  }

  /**
   * @see org.projectforge.web.wicket.flowlayout.GridBuilder#newFieldset(java.lang.String, java.lang.String, boolean)
   */
  @Override
  public FieldsetPanel newFieldset(final String labelText, final String labelDescription, final boolean multipleChildren)
  {
    return new FieldsetPanel(current, labelText, labelDescription, multipleChildren);
  }

  /**
   * Throws {@link UnsupportedOperationException}.
   */
  @Override
  public DivPanel newSectionPanel()
  {
    throw new UnsupportedOperationException();
  }
}
