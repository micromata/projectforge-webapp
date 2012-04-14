/////////////////////////////////////////////////////////////////////////////
//
// Project   ProjectForge
//
// Copyright 2001-2009, Micromata GmbH, Kai Reinhard
//           All rights reserved.
//
/////////////////////////////////////////////////////////////////////////////

package org.projectforge.web.wicket.flowlayout;

import java.io.Serializable;

import org.apache.wicket.Component;

/**
 * @author Kai Reinhard (k.reinhard@micromata.de)
 * 
 */
public interface GridBuilderInterface<T extends AbstractFieldsetPanel<?>> extends Serializable
{
  /**
   * Generates new grid panel. For narrow screens a grid16 panel will be created.
   * @param id if no RepeatingView is given as parent, the id is needed.
   * @return grid16 panel for narrow screens, otherwise grid8.
   */
  public GridBuilderInterface<T> newGrid8(final String id);

  /**
   * Generates new grid panel. For narrow screens a grid16 panel will be created.
   * @param id if no RepeatingView is given as parent, the id is needed.
   * @param clearfix If true then css class clearfix will be added.
   * @return grid16 panel for narrow screens, otherwise grid8.
   */
  public GridBuilderInterface<T> newGrid8(final String id, final boolean clearfix);

  /**
   * Generates new grid panel. For narrow screens a grid16 panel will be created.
   * @return grid16 panel for narrow screens, otherwise grid8.
   */
  public GridBuilderInterface<T> newGrid8();

  /**
   * Generates new grid panel. For narrow screens a grid16 panel will be created.
   * @param clearfix If true then css class clearfix will be added.
   * @return grid16 panel for narrow screens, otherwise grid8.
   */
  public GridBuilderInterface<T> newGrid8(final boolean clearfix);

  /**
   * @param id if no RepeatingView is given as parent, the id is needed.
   * @return grid16 panel.
   */
  public GridBuilderInterface<T> newGrid16(final String id);

  /**
   * @param id if no RepeatingView is given as parent, the id is needed.
   * @return grid16 panel.
   */
  public GridBuilderInterface<T> newGrid16(final String id, final boolean clearfix);

  /**
   * @return grid16 panel.
   */
  public GridBuilderInterface<T> newGrid16();

  /**
   * @return grid16 panel.
   */
  public GridBuilderInterface<T> newGrid16(final boolean clearfix);

  /**
   * @return new block panel.
   */
  public GridBuilderInterface<T> newBlockPanel();

  /**
   * Should be used for the DivPanel constructor if {@link #addColumnsPanel(DivPanel)} is used.
   * @see DivPanel#newChildId()
   */
  public String newColumnsPanelId();

  /**
   * If you need to implement isVisible() of DivPanel etc. you can use this method instead of {@link #newColumnsPanel()}.
   * @param colPanel Please use {@link #newColumnsPanelId()} as component id.
   * @return new columns panel for wide screens and gridPanel itself for all other screens.
   */
  public GridBuilderInterface<T> addColumnsPanel(final DivPanel colPanel);

  /**
   */
  public GridBuilderInterface<T> newColumnsPanel();

  /**
   * @param length {@link DivType#COL_25}, {@link DivType#COL_33}, ...
   */
  public GridBuilderInterface<T> newColumnPanel(final DivType length);

  /**
   * @param length {@link DivType#COL_25}, {@link DivType#COL_33}, ...
   */
  public GridBuilderInterface<T> newColumnPanel(final DivType length, final boolean newBlock4NonWideScreen);

  /**
   * Should be used for the DivPanel constructor if {@link #addColumnPanel(DivPanel, DivType)} is used.
   * @see DivPanel#newChildId()
   */
  public String newColumnPanelId();

  /**
   * If you need to implement isVisible() of DivPanel etc. you can use this method instead of {@link #newColumnPanel(DivType)}.
   * @param colPanel Please use {@link #newColumnPanelId()} as component id.
   * @param length {@link DivType#COL_25}, {@link DivType#COL_33}, ... Please don't set the width directly to the given DivPanel because the
   *          layout manager can't handle different screen resolutions anymore properly.
   * @return new columns panel for wide screens and gridPanel itself for all other screens.
   */
  public GridBuilderInterface<T> addColumnPanel(final DivPanel colPanel, final DivType length);

  /**
   * @return the current panel to add childs to.
   */
  public DivPanel getPanel();

  /**
   * Adds a heading (e. g. h2) to the current panel.
   * @param label
   * @return The created heading.
   */
  public FormHeadingPanel newFormHeading(final String label);

  public T newFieldset(final String label);

  public T newFieldset(final String label, final boolean multipleChildren);

  public T newFieldset(final String labelText, final String labelDescription);

  public T newFieldset(final String labelText, final String labelDescription, final boolean multipleChildren);

  public DivPanel newSectionPanel();

  /**
   * @param i18nKey
   * @see Component#get(String)
   */
  public String getString(String i18nKey);
}