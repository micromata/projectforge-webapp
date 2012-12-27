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

package org.projectforge.web.wicket.bootstrap;

import org.apache.wicket.MarkupContainer;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.projectforge.web.BrowserScreenWidthType;
import org.projectforge.web.wicket.MySession;
import org.projectforge.web.wicket.flowlayout.AbstractGridBuilder;
import org.projectforge.web.wicket.flowlayout.DivPanel;
import org.projectforge.web.wicket.flowlayout.DivType;
import org.projectforge.web.wicket.flowlayout.FieldProperties;
import org.projectforge.web.wicket.flowlayout.FieldsetPanel;
import org.projectforge.web.wicket.flowlayout.FormHeadingPanel;

/**
 * @author Kai Reinhard (k.reinhard@micromata.de)
 * 
 */
public class GridBuilder extends AbstractGridBuilder<FieldsetPanel>
{
  private static final long serialVersionUID = 4323077384391963834L;

  public static final int MAX_LEVEL = 2;

  private final DivPanel[] rowPanel = new DivPanel[MAX_LEVEL + 1];

  private final DivPanel[] gridPanel = new DivPanel[MAX_LEVEL + 1];

  private DivPanel currentGridPanel;

  private int fourColumnsCounter;

  public GridBuilder(final MarkupContainer parent, final String id, final MySession session)
  {
    super(session);
    this.parent = parent;
    rowPanel[0] = new DivPanel(id, GridType.ROW_FLUID);
    parent.add(rowPanel[0]);
  }

  public GridBuilder newSplitScreen2Columns()
  {
    newGridPanel(0, GridSize.SPAN12).newRowPanel(1).newGridPanel(1, GridSize.SPAN6);
    return this;
  }

  public GridBuilder newSplitScreen4Columns()
  {
    newSplitScreen2Columns();
    newRowPanel(2).newGridPanel(2, GridSize.SPAN6);
    fourColumnsCounter = 0;
    return this;
  }

  public GridBuilder nextScreen4Columns()
  {
    ++fourColumnsCounter;
    if (fourColumnsCounter % 2 == 0) {
      newGridPanel(1, GridSize.SPAN6).newRowPanel(2).newGridPanel(2, GridSize.SPAN6);
    } else {
      newGridPanel(2, GridSize.SPAN6);
    }
    return this;
  }

  public GridBuilder newGridPanel(final int level, final GridSize size, final GridType... gridTypes)
  {
    if (levelSupportedByResolution(level) == false) {
      // Not supported by this screens.
      return this;
    }
    validateGridPanelLevel(level);
    final DivPanel divPanel = new DivPanel(rowPanel[level].newChildId(), size, gridTypes);
    return newGridPanel(level, divPanel);
  }

  public GridBuilder newGridPanel(final int level, final DivPanel divPanel)
  {
    if (levelSupportedByResolution(level) == false) {
      // Not supported by this screens.
      return this;
    }
    validateGridPanelLevel(level);
    currentGridPanel = divPanel;
    gridPanel[level] = currentGridPanel;
    rowPanel[level].add(currentGridPanel);
    setNullPanel(level + 1, level + 1);
    return this;
  }

  public GridBuilder newRowPanel(final int level, final GridType... gridTypes)
  {
    if (levelSupportedByResolution(level) == false) {
      // Not supported by this screens.
      return this;
    }
    validateRowPanelLevel(level);
    final DivPanel rowPanel = new DivPanel(gridPanel[level - 1].newChildId(), GridType.ROW_FLUID);
    rowPanel.addCssClasses(gridTypes);
    return newRowPanel(level, rowPanel);
  }

  public GridBuilder newRowPanel(final int level, final DivPanel rowPanel)
  {
    if (levelSupportedByResolution(level) == false) {
      // Not supported by this screens.
      return this;
    }
    validateRowPanelLevel(level);
    this.rowPanel[level] = rowPanel;
    gridPanel[level - 1].add(rowPanel);
    setNullPanel(level + 1, level);
    return this;
  }

  /**
   * Generates new grid panel. For narrow screens a grid12 panel will be created.
   * @return grid12 panel for narrow screens, otherwise grid6.
   */
  @Deprecated
  public GridBuilder newGrid6()
  {
    return newGrid6(false);
  }

  /**
   * Generates new grid panel. For narrow screens a grid12 panel will be created.
   * @param clearfix If true then css class clearfix will be added.
   * @return grid12 panel for narrow screens, otherwise grid6.
   */
  @Deprecated
  public GridBuilder newGrid6(final boolean clearfix)
  {
    return newGridPanel(0, GridSize.SPAN6);
  }

  /**
   * @see org.projectforge.web.wicket.flowlayout.GridBuilderInterface#newGrid12()
   */
  @Deprecated
  public GridBuilder newGrid12()
  {
    return newGrid12(false);
  }

  /**
   * @return grid16 panel.
   */
  @Deprecated
  public GridBuilder newGrid12(final boolean clearfix)
  {
    return newGrid(DivType.GRID12, clearfix);
  }

  @Deprecated
  private GridBuilder newGrid(final DivType gridType, final boolean clearfix)
  {
    if (gridType == DivType.GRID12) {
      return newGridPanel(0, GridSize.SPAN12);
    } else {
      return newGridPanel(0, GridSize.SPAN6);
    }
  }

  /**
   * @return new block panel.
   * @deprecated does nothing (no replacement).
   */
  @Deprecated
  public GridBuilder newBlockPanel()
  {
    return this;
  }

  /**
   * @return new child id (Wicket) id of the current grid panel.
   * @see org.projectforge.web.wicket.flowlayout.GridBuilderInterface#newPanelId()
   */
  @Deprecated
  public String newPanelId()
  {
    return currentGridPanel.newChildId();
  }

  /**
   * If you need to implement isVisible() of DivPanel etc. you can use this method instead of {@link #newNestedRowPanel()}.
   * @param colPanel Please use {@link #newPanelId()} as component id.
   * @return new columns panel for wide screens and gridPanel itself for all other screens.
   */
  @Deprecated
  public GridBuilder addNestedRowPanel(final DivPanel nestedRowPanel)
  {
    return newRowPanel(1, nestedRowPanel);
  }

  /**
   */
  @Deprecated
  public GridBuilder newNestedRowPanel()
  {
    return newRowPanel(1);
  }

  /**
   * @param length {@link DivType#COL_25}, {@link DivType#COL_33}, ...
   */
  @Deprecated
  public GridBuilder newNestedPanel(final DivType length)
  {
    return newGridPanel(1, getSize(length));
  }

  /**
   * @see org.projectforge.web.wicket.flowlayout.GridBuilderInterface#newRowChildPanelId()
   */
  @Deprecated
  public String newRowChildPanelId()
  {
    return rowPanel[0].newChildId();
  }

  /**
   * If you need to implement isVisible() of DivPanel etc. you can use this method instead of {@link #newNestedPanel(DivType)}.
   * @param gridPanel Please use {@link #newRowChildPanelId()} as component id.
   * @param length {@link DivType#COL_25}, {@link DivType#COL_33}, ... Please don't set the width directly to the given DivPanel because the
   *          layout manager can't handle different screen resolutions anymore properly.
   * @return new columns panel for wide screens and gridPanel itself for all other screens.
   */
  @Deprecated
  public GridBuilder addRowChildPanel(final DivPanel gridPanel, final DivType length)
  {
    gridPanel.addCssClasses(length);
    return newGridPanel(0, gridPanel);
  }

  /**
   * @see org.projectforge.web.wicket.flowlayout.GridBuilderInterface#getPanel()
   */
  public DivPanel getPanel()
  {
    return currentGridPanel;
  }

  /**
   * @see org.projectforge.web.wicket.flowlayout.GridBuilderInterface#newFormHeading(java.lang.String)
   */
  public FormHeadingPanel newFormHeading(final String label)
  {
    final FormHeadingPanel formHeading = new FormHeadingPanel(currentGridPanel.newChildId(), label);
    currentGridPanel.add(formHeading);
    return formHeading;
  }

  /**
   * @see org.projectforge.web.wicket.flowlayout.GridBuilderInterface#newSectionPanel()
   * @deprecated no operation.
   */
  @Deprecated
  public DivPanel newSectionPanel()
  {
    return currentGridPanel;
  }

  @Deprecated
  public RepeatingView newRepeatingView() {
    final RepeatingView repeater = new RepeatingView(currentGridPanel.newChildId());
    currentGridPanel.add(repeater);
    return repeater;
  }

  /**
   * @see org.projectforge.web.wicket.flowlayout.AbstractGridBuilder#newFieldset(org.projectforge.web.wicket.flowlayout.FieldProperties)
   */
  @Override
  public FieldsetPanel newFieldset(final FieldProperties< ? > fieldProperties)
  {
    return new FieldsetPanel(currentGridPanel, fieldProperties);
  }

  /**
   * @see org.projectforge.web.wicket.flowlayout.GridBuilderInterface#newFieldset(java.lang.String)
   */
  @Override
  public FieldsetPanel newFieldset(final String label)
  {
    return new FieldsetPanel(currentGridPanel, label);
  }

  /**
   * @see org.projectforge.web.wicket.flowlayout.GridBuilderInterface#newFieldset(java.lang.String, boolean)
   */
  @Override
  public FieldsetPanel newFieldset(final String label, final boolean multipleChildren)
  {
    return new FieldsetPanel(currentGridPanel, label, multipleChildren);
  }

  /**
   * @see org.projectforge.web.wicket.flowlayout.GridBuilderInterface#newFieldset(java.lang.String, java.lang.String)
   */
  @Override
  public FieldsetPanel newFieldset(final String labelText, final String labelDescription)
  {
    return new FieldsetPanel(currentGridPanel, labelText, labelDescription);
  }

  /**
   * @see org.projectforge.web.wicket.flowlayout.GridBuilderInterface#newFieldset(java.lang.String, java.lang.String, boolean)
   */
  @Override
  public FieldsetPanel newFieldset(final String labelText, final String labelDescription, final boolean multipleChildren)
  {
    return new FieldsetPanel(currentGridPanel, labelText, labelDescription, multipleChildren);
  }

  private void validateRowPanelLevel(final int level)
  {
    if (level < 1 || level > MAX_LEVEL) {
      throw new IllegalArgumentException("Level '" + "' not supported. Value must be between 1 and " + MAX_LEVEL);
    }
    if (gridPanel[level - 1] == null) {
      throw new IllegalArgumentException("Can't add row panel of level '"
          + level
          + "'. Grid panel of level "
          + (level - 1)
          + " doesn't exist!");
    }
  }

  private boolean levelSupportedByResolution(final int level)
  {
    if (browserScreenWidthType == BrowserScreenWidthType.NORMAL
        && level >= 2
        || browserScreenWidthType == BrowserScreenWidthType.NARROW
        && level >= 1) {
      // Not supported by this screens.
      return false;
    }
    return true;
  }

  private void validateGridPanelLevel(final int level)
  {
    if (level < 0 || level > MAX_LEVEL) {
      throw new IllegalArgumentException("Level '" + level + "' not supported. Value must be between 0 and " + MAX_LEVEL);
    }
    if (rowPanel[level] == null) {
      throw new IllegalArgumentException("Can't add grid panel of level '" + level + "'. Row panel of same level doesn't exist!");
    }
  }

  private void setNullPanel(final int rowFromLevel, final int gridFromLevel)
  {
    for (int i = rowFromLevel; i <= MAX_LEVEL; i++) {
      rowPanel[i] = null;
    }
    for (int i = gridFromLevel; i <= MAX_LEVEL; i++) {
      gridPanel[i] = null;
    }
  }

  private GridSize getSize(final DivType divType)
  {
    GridSize size;
    if (divType == DivType.COL_75) {
      size = GridSize.SPAN9;
    } else if (divType == DivType.COL_66 || divType == DivType.COL_60) {
      size = GridSize.SPAN8;
    } else if (divType == DivType.COL_50) {
      size = GridSize.SPAN6;
    } else if (divType == DivType.COL_40 || divType == DivType.COL_33) {
      size = GridSize.SPAN4;
    } else if (divType == DivType.COL_25) {
      size = GridSize.SPAN3;
    } else {
      size = GridSize.SPAN12;
    }
    return size;
  }
}
