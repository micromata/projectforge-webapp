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
import org.projectforge.web.wicket.flowlayout.AbstractGridBuilder;
import org.projectforge.web.wicket.flowlayout.DivPanel;
import org.projectforge.web.wicket.flowlayout.DivType;
import org.projectforge.web.wicket.flowlayout.FormHeadingPanel;

/**
 * @author Kai Reinhard (k.reinhard@micromata.de)
 * 
 */
public class MobileGridBuilder extends AbstractGridBuilder<MobileFieldsetPanel>
{
  private static final long serialVersionUID = 134863232462613937L;

  public MobileGridBuilder(final RepeatingView parent)
  {
    this.parentRepeatingView = parent;
  }

  public MobileGridBuilder(final DivPanel parent)
  {
    this.parentDivPanel = parent;
  }

  /**
   * Throws {@link UnsupportedOperationException}.
   */
  @Override
  public MobileGridBuilder newGrid8(final String id)
  {
    throw new UnsupportedOperationException();
  }

  /**
   * Throws {@link UnsupportedOperationException}.
   */
  @Override
  public MobileGridBuilder newGrid8(final String id, final boolean clearfix)
  {
    throw new UnsupportedOperationException();
  }

  /**
   * Throws {@link UnsupportedOperationException}.
   */
  @Override
  public MobileGridBuilder newGrid8()
  {
    throw new UnsupportedOperationException();
  }

  /**
   * Throws {@link UnsupportedOperationException}.
   */
  @Override
  public MobileGridBuilder newGrid8(final boolean clearfix)
  {
    throw new UnsupportedOperationException();
  }

  /**
   * Throws {@link UnsupportedOperationException}.
   */
  @Override
  public MobileGridBuilder newGrid16(final String id)
  {
    throw new UnsupportedOperationException();
  }

  /**
   * Throws {@link UnsupportedOperationException}.
   */
  @Override
  public MobileGridBuilder newGrid16(final String id, final boolean clearfix)
  {
    throw new UnsupportedOperationException();
  }

  /**
   * Throws {@link UnsupportedOperationException}.
   */
  @Override
  public MobileGridBuilder newGrid16()
  {
    throw new UnsupportedOperationException();
  }

  /**
   * Throws {@link UnsupportedOperationException}.
   */
  @Override
  public MobileGridBuilder newGrid16(final boolean clearfix)
  {
    throw new UnsupportedOperationException();
  }

  /**
   * @return new block panel.
   */
  @Override
  public MobileGridBuilder newBlockPanel()
  {
    blockPanel = new DivPanel(newParentChildId());
    getParent().add(blockPanel);
    current = blockPanel;
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
  public MobileGridBuilder addColumnsPanel(final DivPanel colPanel)
  {
    throw new UnsupportedOperationException();
  }

  /**
   * Throws {@link UnsupportedOperationException}.
   */
  @Override
  public MobileGridBuilder newColumnsPanel()
  {
    throw new UnsupportedOperationException();
  }

  /**
   * Throws {@link UnsupportedOperationException}.
   */
  @Override
  public MobileGridBuilder newColumnPanel(final DivType length)
  {
    throw new UnsupportedOperationException();
  }

  /**
   * Throws {@link UnsupportedOperationException}.
   */
  @Override
  public MobileGridBuilder newColumnPanel(final DivType length, final boolean newBlock4NonWideScreen)
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
  public MobileGridBuilder addColumnPanel(final DivPanel colPanel, final DivType length)
  {
    throw new UnsupportedOperationException();
  }

  /**
   * @see org.projectforge.web.wicket.flowlayout.GridBuilderInterface#getPanel()
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
   * @see org.projectforge.web.wicket.flowlayout.GridBuilderInterface#newFieldset(java.lang.String)
   */
  @Override
  public MobileFieldsetPanel newFieldset(final String label)
  {
    return new MobileFieldsetPanel(current, label);
  }

  /**
   * @see org.projectforge.web.wicket.flowlayout.GridBuilderInterface#newFieldset(java.lang.String, boolean)
   */
  @Override
  public MobileFieldsetPanel newFieldset(final String label, final boolean multipleChildren)
  {
    throw new UnsupportedOperationException();
  }

  /**
   * @see org.projectforge.web.wicket.flowlayout.GridBuilderInterface#newFieldset(java.lang.String, java.lang.String)
   */
  @Override
  public MobileFieldsetPanel newFieldset(final String labelText, final String labelDescription)
  {
    throw new UnsupportedOperationException();
  }

  /**
   * @see org.projectforge.web.wicket.flowlayout.GridBuilderInterface#newFieldset(java.lang.String, java.lang.String, boolean)
   */
  @Override
  public MobileFieldsetPanel newFieldset(final String labelText, final String labelDescription, final boolean multipleChildren)
  {
    throw new UnsupportedOperationException();
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
