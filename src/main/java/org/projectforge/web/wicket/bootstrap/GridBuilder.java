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

  private final DivPanel mainContainer;

  private final DivPanel[] rowPanel = new DivPanel[MAX_LEVEL + 1];

  private final DivPanel[] gridPanel = new DivPanel[MAX_LEVEL + 1];

  private int currentLevel = 0;

  private int splitDepth = 1;

  // Counts the length of grid panels of current row. After reaching full length, a new row will be created automatically.
  private final int lengthCounter[] = new int[MAX_LEVEL + 1];

  private final boolean fluid;

  /**
   * @param parent
   * @param id
   * @param session
   * @param fluid Default is true.
   */
  public GridBuilder(final MarkupContainer parent, final String id, final MySession session, final boolean fluid)
  {
    super(session);
    this.parent = parent;
    this.fluid = fluid;
    mainContainer = new DivPanel(id, fluid == true ? GridType.CONTAINER_FLUID : GridType.CONTAINER);
    parent.add(mainContainer);
  }

  public GridBuilder(final MarkupContainer parent, final String id, final MySession session)
  {
    this(parent, id, session, true);
  }

  public GridBuilder newGridPanel(final GridType... gridTypes)
  {
    return newGridPanel(0, GridSize.SPAN12, gridTypes);
  }

  public GridBuilder newSplitPanel(final GridSize size, final GridType... gridTypes)
  {
    return newSplitPanel(size, false, gridTypes);
  }

  public GridBuilder newSplitPanel(final GridSize size, final boolean hasSubSplitPanel, final GridType... gridTypes)
  {
    if (hasSubSplitPanel == true) {
      splitDepth = 2;
    } else {
      splitDepth = 1;
    }
    if (browserScreenWidthType == BrowserScreenWidthType.NARROW) {
      if (splitDepth == 1) {
        return newGridPanel(0, GridSize.COL100, gridTypes);
      } else {
        return this;
      }
    } else if (browserScreenWidthType == BrowserScreenWidthType.NORMAL) {
      if (splitDepth == 1) {
        return newGridPanel(0, size, gridTypes);
      } else {
        return this;
      }
    } else {
      return newGridPanel(0, size, gridTypes);
    }
  }

  public GridBuilder newSubSplitPanel(final GridSize size, final GridType... gridTypes)
  {
    if (splitDepth < 2) {
      throw new IllegalArgumentException("Dear developer: please call gridBuilder.newSplitPanel(GridSize, true, ...) first!");
    }
    if (browserScreenWidthType == BrowserScreenWidthType.NARROW) {
      return newGridPanel(0, GridSize.COL100, gridTypes);
    } else if (browserScreenWidthType == BrowserScreenWidthType.NORMAL) {
      return newGridPanel(0, size, gridTypes);
    } else {
      return newGridPanel(1, size, gridTypes);
    }
  }

  /**
   * Sets the gridPanel of the given level as current level. You can only set a lower level than current level, otherwise an exception will
   * be thrown.
   * @param level the currentLevel to set
   * @return this for chaining.
   */
  public GridBuilder setCurrentLevel(final int level)
  {
    if (level > this.currentLevel) {
      throw new IllegalArgumentException("You can only set a lower level than current level, current level is "
          + this.currentLevel
          + ", desired level is "
          + level);
    }
    if (level < 0) {
      throw new IllegalArgumentException("Level must be a positive value: " + level);
    }
    this.currentLevel = level;
    setNullPanel(level, level + 1);
    return this;
  }

  /**
   * If row panel of this level doesn't exist it will be created.
   * @param level
   * @param size
   * @param gridTypes
   * @return this for chaining.
   */
  private GridBuilder newGridPanel(final int level, final GridSize size, final GridType... gridTypes)
  {
    validateGridPanelLevel(level);
    currentLevel = level;
    if (rowPanel[level] == null) {
      newRowPanel(level);
    }
    lengthCounter[level] += size.getLength();
    if (lengthCounter[level] > 12) {
      newRowPanel(level);
      lengthCounter[level] = size.getLength();
    }
    final DivPanel divPanel = new DivPanel(rowPanel[level].newChildId(), size, gridTypes);
    return addGridPanel(level, divPanel);
  }

  /**
   * If you use this method, the lengthCounter won't be incremented and you use to call setCurrentLevel(level -1) manually.
   * @param level
   * @param divPanel
   * @return
   */
  private GridBuilder addGridPanel(final int level, final DivPanel divPanel)
  {
    validateGridPanelLevel(level);
    currentLevel = level;
    if (rowPanel[level] == null) {
      newRowPanel(level);
    }
    gridPanel[level] = divPanel;
    rowPanel[level].add(divPanel);
    setNullPanel(level + 1, level + 1);
    return this;
  }

  private String newRowPanelId(final int level)
  {
    validateRowPanelLevel(level);
    if (level > 0) {
      return gridPanel[level - 1].newChildId();
    } else {
      return mainContainer.newChildId();
    }
  }

  private GridBuilder newRowPanel(final int level, final GridType... gridTypes)
  {
    validateRowPanelLevel(level);
    final DivPanel rowPanel = new DivPanel(newRowPanelId(level), fluid == true ? GridType.ROW_FLUID : GridType.ROW);
    rowPanel.addCssClasses(gridTypes);
    return addRowPanel(level, rowPanel);
  }

  private GridBuilder addRowPanel(final int level, final DivPanel rowPanel)
  {
    validateRowPanelLevel(level);
    this.rowPanel[level] = rowPanel;
    lengthCounter[level] = 0;
    if (level > 0) {
      gridPanel[level - 1].add(rowPanel);
    } else {
      mainContainer.add(rowPanel);
    }
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
   * @return new child id (Wicket id) of the current grid panel.
   */
  public String newRowId()
  {
    return rowPanel[currentLevel].newChildId();
  }

  /**
   * @return new child id (Wicket id) of the current row panel.
   */
  public String newGridPanelId()
  {
    return gridPanel[currentLevel].newChildId();
  }

  /**
   * If you need to implement isVisible() of DivPanel etc. you can use this method instead of {@link #newNestedRowPanel()}.
   * @param colPanel Please use {@link #newRowId()} as component id.
   * @return new columns panel for wide screens and gridPanel itself for all other screens.
   */
  @Deprecated
  public GridBuilder addNestedRowPanel(final DivPanel nestedRowPanel)
  {
    return addRowPanel(1, nestedRowPanel);
  }

  /**
   */
  @Deprecated
  public GridBuilder newNestedRowPanel()
  {
    return newRowPanel(0);
  }

  /**
   * @param length {@link DivType#COL_25}, {@link DivType#COL_33}, ...
   */
  @Deprecated
  public GridBuilder newNestedPanel(final DivType length)
  {
    return newGridPanel(0, getSize(length));
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
    return addGridPanel(0, gridPanel);
  }

  /**
   * @see org.projectforge.web.wicket.flowlayout.GridBuilderInterface#getPanel()
   */
  public DivPanel getPanel()
  {
    if (currentLevel == 0 && gridPanel[currentLevel] == null) {
      newGridPanel(0, GridSize.SPAN12);
    }
    return gridPanel[currentLevel];
  }

  public DivPanel getRowPanel()
  {
    return rowPanel[currentLevel];
  }

  /**
   * @see org.projectforge.web.wicket.flowlayout.GridBuilderInterface#newFormHeading(java.lang.String)
   */
  public FormHeadingPanel newFormHeading(final String label)
  {
    final FormHeadingPanel formHeading = new FormHeadingPanel(getPanel().newChildId(), label);
    getPanel().add(formHeading);
    return formHeading;
  }

  /**
   * @see org.projectforge.web.wicket.flowlayout.GridBuilderInterface#newSectionPanel()
   * @deprecated no operation.
   */
  @Deprecated
  public DivPanel newSectionPanel()
  {
    return getPanel();
  }

  public RepeatingView newRepeatingView()
  {
    final RepeatingView repeater = new RepeatingView(getPanel().newChildId());
    getPanel().add(repeater);
    return repeater;
  }

  /**
   * @see org.projectforge.web.wicket.flowlayout.AbstractGridBuilder#newFieldset(org.projectforge.web.wicket.flowlayout.FieldProperties)
   */
  @Override
  public FieldsetPanel newFieldset(final FieldProperties< ? > fieldProperties)
  {
    return new FieldsetPanel(getPanel(), fieldProperties);
  }

  /**
   * @see org.projectforge.web.wicket.flowlayout.GridBuilderInterface#newFieldset(java.lang.String)
   */
  @Override
  public FieldsetPanel newFieldset(final String label)
  {
    return new FieldsetPanel(getPanel(), label);
  }

  /**
   * @see org.projectforge.web.wicket.flowlayout.GridBuilderInterface#newFieldset(java.lang.String, boolean)
   */
  @Override
  public FieldsetPanel newFieldset(final String label, final boolean multipleChildren)
  {
    return new FieldsetPanel(getPanel(), label, multipleChildren);
  }

  /**
   * @see org.projectforge.web.wicket.flowlayout.GridBuilderInterface#newFieldset(java.lang.String, java.lang.String)
   */
  @Override
  public FieldsetPanel newFieldset(final String labelText, final String labelDescription)
  {
    return new FieldsetPanel(getPanel(), labelText, labelDescription);
  }

  /**
   * @see org.projectforge.web.wicket.flowlayout.GridBuilderInterface#newFieldset(java.lang.String, java.lang.String, boolean)
   */
  @Override
  public FieldsetPanel newFieldset(final String labelText, final String labelDescription, final boolean multipleChildren)
  {
    return new FieldsetPanel(getPanel(), labelText, labelDescription, multipleChildren);
  }

  private void validateRowPanelLevel(final int level)
  {
    if (level < 0 || level > MAX_LEVEL) {
      throw new IllegalArgumentException("Level '" + "' not supported. Value must be between 1 and " + MAX_LEVEL);
    }
    if (level > 0 && gridPanel[level - 1] == null) {
      throw new IllegalArgumentException("Can't add row panel of level '"
          + level
          + "'. Grid panel of level "
          + (level - 1)
          + " doesn't exist!");
    }
  }

  private void validateGridPanelLevel(final int level)
  {
    if (level < 0 || level > MAX_LEVEL) {
      throw new IllegalArgumentException("Level '" + level + "' not supported. Value must be between 0 and " + MAX_LEVEL);
    }
  }

  private void setNullPanel(final int rowFromLevel, final int gridFromLevel)
  {
    for (int i = rowFromLevel; i <= MAX_LEVEL; i++) {
      rowPanel[i] = null;
      lengthCounter[i] = 0;
    }
    for (int i = gridFromLevel; i <= MAX_LEVEL; i++) {
      gridPanel[i] = null;
    }
  }

  @SuppressWarnings("deprecation")
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
