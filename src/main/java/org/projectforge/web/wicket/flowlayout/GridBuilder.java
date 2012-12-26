/////////////////////////////////////////////////////////////////////////////
//
// Project ProjectForge Community Edition
//         www.projectforge.org
//
// Copyright (C) 2001-2012 Kai Reinhard (k.reinhard@micromata.de)
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

package org.projectforge.web.wicket.flowlayout;

import static org.projectforge.web.BrowserScreenWidthType.NARROW;
import static org.projectforge.web.BrowserScreenWidthType.NORMAL;
import static org.projectforge.web.BrowserScreenWidthType.WIDE;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.projectforge.web.wicket.MySession;

/**
 * @author Kai Reinhard (k.reinhard@micromata.de)
 * 
 */
public class GridBuilder extends AbstractGridBuilder<FieldsetPanel>
{
  private static final long serialVersionUID = 4323077384391963834L;

  private DivPanel gridPanel, columnsPanel, columnPanel;

  protected DivPanel blockPanel;

  protected DivPanel current;

  private DivType gridSize;

  public GridBuilder(final RepeatingView parent, final MySession session)
  {
    super(parent, session);
  }

  public GridBuilder(final DivPanel parent, final MySession session)
  {
    super(parent, session);
  }

  /**
   * Generates new grid panel. For narrow screens a grid16 panel will be created.
   * @param id if no RepeatingView is given as parent, the id is needed.
   * @return grid16 panel for narrow screens, otherwise grid8.
   */
  public GridBuilder newGrid8(final String id)
  {
    return newGrid8(id, false);
  }

  /**
   * Generates new grid panel. For narrow screens a grid16 panel will be created.
   * @param id if no RepeatingView is given as parent, the id is needed.
   * @param clearfix If true then css class clearfix will be added.
   * @return grid16 panel for narrow screens, otherwise grid8.
   */
  public GridBuilder newGrid8(final String id, final boolean clearfix)
  {
    if (browserScreenWidthType == NARROW) {
      return newGrid(DivType.GRID16, id, clearfix);
    } else {
      return newGrid(DivType.GRID8, id, clearfix);
    }
  }

  /**
   * Generates new grid panel. For narrow screens a grid16 panel will be created.
   * @return grid16 panel for narrow screens, otherwise grid8.
   */
  public GridBuilder newGrid8()
  {
    return newGrid8(newParentChildId());
  }

  /**
   * Generates new grid panel. For narrow screens a grid16 panel will be created.
   * @param clearfix If true then css class clearfix will be added.
   * @return grid16 panel for narrow screens, otherwise grid8.
   */
  public GridBuilder newGrid8(final boolean clearfix)
  {
    return newGrid8(newParentChildId(), clearfix);
  }

  /**
   * @see org.projectforge.web.wicket.flowlayout.GridBuilderInterface#newGrid16(java.lang.String)
   */
  public GridBuilder newGrid16(final String id)
  {
    return newGrid16(id, false);
  }

  /**
   * @see org.projectforge.web.wicket.flowlayout.GridBuilderInterface#newGrid16(java.lang.String, boolean)
   */
  public GridBuilder newGrid16(final String id, final boolean clearfix)
  {
    return newGrid(DivType.GRID16, id, clearfix);
  }

  /**
   * @see org.projectforge.web.wicket.flowlayout.GridBuilderInterface#newGrid16()
   */
  public GridBuilder newGrid16()
  {
    return newGrid16(newParentChildId());
  }

  /**
   * @return grid16 panel.
   */
  public GridBuilder newGrid16(final boolean clearfix)
  {
    return newGrid16(newParentChildId(), clearfix);
  }

  private GridBuilder newGrid(final DivType gridType, final String id, final boolean clearfix)
  {
    gridSize = gridType;
    final DivPanel divPanel = new DivPanel(id, gridType, DivType.BOX);
    if (clearfix == true) {
      divPanel.add(AttributeModifier.append("style", "clear: left;"));
    }
    getParent().add(divPanel);
    gridPanel = divPanel;
    blockPanel = columnsPanel = columnPanel = null;
    current = gridPanel;
    return this;
  }

  /**
   * @return new block panel.
   */
  public GridBuilder newBlockPanel()
  {
    blockPanel = new DivPanel(gridPanel.newChildId(), DivType.BLOCK_LINES);
    gridPanel.add(blockPanel);
    columnsPanel = columnPanel = null;
    current = blockPanel;
    return this;
  }

  /**
   * @see org.projectforge.web.wicket.flowlayout.GridBuilderInterface#newColumnsPanelId()
   */
  public String newColumnsPanelId()
  {
    if (blockPanel == null) {
      newBlockPanel();
    }
    return blockPanel.newChildId();
  }

  /**
   * If you need to implement isVisible() of DivPanel etc. you can use this method instead of {@link #newColumnsPanel()}.
   * @param colPanel Please use {@link #newColumnsPanelId()} as component id.
   * @return new columns panel for wide screens and gridPanel itself for all other screens.
   */
  public GridBuilder addColumnsPanel(final DivPanel colPanel)
  {
    blockPanel.add(columnsPanel = colPanel);
    current = columnsPanel;
    return this;
  }

  /**
   */
  public GridBuilder newColumnsPanel()
  {
    if (blockPanel == null) {
      newBlockPanel();
    }
    columnPanel = null;
    columnsPanel = columnPanel = null;
    if (browserScreenWidthType == WIDE || browserScreenWidthType == NORMAL && gridSize == DivType.GRID16) {
      columnsPanel = new DivPanel(blockPanel.newChildId(), DivType.COLUMNS, DivType.CLEARFIX);
      blockPanel.add(columnsPanel);
      current = columnsPanel;
      return this;
    } else {
      current = blockPanel;
      columnsPanel = columnPanel = null;
      return this;
    }
  }

  /**
   * @param length {@link DivType#COL_25}, {@link DivType#COL_33}, ...
   */
  public GridBuilder newColumnPanel(final DivType length)
  {
    return addColumnPanel(new DivPanel(newColumnPanelId()), length);
  }

  /**
   * @param length {@link DivType#COL_25}, {@link DivType#COL_33}, ...
   */
  public GridBuilder newColumnPanel(final DivType length, final boolean newBlock4NonWideScreen)
  {
    if (newBlock4NonWideScreen == false || browserScreenWidthType == WIDE) {
      return addColumnPanel(new DivPanel(newColumnPanelId()), length);
    } else {
      return newGrid(gridSize, newParentChildId(), false);
    }
  }

  /**
   * @see org.projectforge.web.wicket.flowlayout.GridBuilderInterface#newColumnPanelId()
   */
  public String newColumnPanelId()
  {
    if (columnsPanel != null) {
      return columnsPanel.newChildId();
    } else {
      return blockPanel.newChildId();
    }
  }

  /**
   * If you need to implement isVisible() of DivPanel etc. you can use this method instead of {@link #newColumnPanel(DivType)}.
   * @param colPanel Please use {@link #newColumnPanelId()} as component id.
   * @param length {@link DivType#COL_25}, {@link DivType#COL_33}, ... Please don't set the width directly to the given DivPanel because the
   *          layout manager can't handle different screen resolutions anymore properly.
   * @return new columns panel for wide screens and gridPanel itself for all other screens.
   */
  public GridBuilder addColumnPanel(final DivPanel colPanel, final DivType length)
  {
    if (columnsPanel != null) {
      columnPanel = colPanel;
      if (browserScreenWidthType == WIDE || browserScreenWidthType == NORMAL && gridSize == DivType.GRID16) {
        colPanel.addCssClasses(length);
      } else {
        // No length e. g. for extended filter in AbstractListForm:
      }
      columnsPanel.add(columnPanel);
      current = columnPanel;
      return this;
    } else {
      // ToDo:
      if (blockPanel.hasChilds() == true) {
        // newGrid(gridSize, newParentChildId());
        return newBlockPanel();
      } else {
        current = blockPanel;
        return this;
      }
    }
  }

  /**
   * @see org.projectforge.web.wicket.flowlayout.GridBuilderInterface#getPanel()
   */
  public DivPanel getPanel()
  {
    return current;
  }

  /**
   * @see org.projectforge.web.wicket.flowlayout.GridBuilderInterface#newFormHeading(java.lang.String)
   */
  public FormHeadingPanel newFormHeading(final String label)
  {
    final FormHeadingPanel formHeading = new FormHeadingPanel(current.newChildId(), label);
    current.add(formHeading);
    return formHeading;
  }

  /**
   * @see org.projectforge.web.wicket.flowlayout.GridBuilderInterface#newSectionPanel()
   */
  public DivPanel newSectionPanel()
  {
    final DivPanel section = new DivPanel(current.newChildId(), DivType.SECTION);
    current.add(section);
    return section;
  }

  /**
   * @see org.projectforge.web.wicket.flowlayout.AbstractGridBuilder#newFieldset(org.projectforge.web.wicket.flowlayout.FieldProperties)
   */
  @Override
  public FieldsetPanel newFieldset(final FieldProperties< ? > fieldProperties)
  {
    return new FieldsetPanel(current, fieldProperties);
  }

  /**
   * @see org.projectforge.web.wicket.flowlayout.GridBuilderInterface#newFieldset(java.lang.String)
   */
  @Override
  public FieldsetPanel newFieldset(final String label)
  {
    return new FieldsetPanel(current, label);
  }

  /**
   * @see org.projectforge.web.wicket.flowlayout.GridBuilderInterface#newFieldset(java.lang.String, boolean)
   */
  @Override
  public FieldsetPanel newFieldset(final String label, final boolean multipleChildren)
  {
    return new FieldsetPanel(current, label, multipleChildren);
  }

  /**
   * @see org.projectforge.web.wicket.flowlayout.GridBuilderInterface#newFieldset(java.lang.String, java.lang.String)
   */
  @Override
  public FieldsetPanel newFieldset(final String labelText, final String labelDescription)
  {
    return new FieldsetPanel(current, labelText, labelDescription);
  }

  /**
   * @see org.projectforge.web.wicket.flowlayout.GridBuilderInterface#newFieldset(java.lang.String, java.lang.String, boolean)
   */
  @Override
  public FieldsetPanel newFieldset(final String labelText, final String labelDescription, final boolean multipleChildren)
  {
    return new FieldsetPanel(current, labelText, labelDescription, multipleChildren);
  }
}
