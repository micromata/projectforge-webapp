/////////////////////////////////////////////////////////////////////////////
//
// Project   ProjectForge
//
// Copyright 2001-2009, Micromata GmbH, Kai Reinhard
//           All rights reserved.
//
/////////////////////////////////////////////////////////////////////////////

package org.projectforge.plugins.chimney.wbs;


/**
 * Read only interface for wbs nodes
 * @author Sweeps <pf@byte-storm.com>
 */
public interface IWbsNodeReadOnly {

  /**
   * @return Id in the database
   */
  public Integer getId();

  /**
   * @return This node's wbs code
   */
  public String getWbsCode();

  /**
   * @return The title of this wbs node
   */
  public String getTitle();

  /**
   * @return The parent node of this wbs node. May be null.
   */
  public AbstractWbsNodeDO getParent();

  /**
   * @return The number of children this wbs node has
   */
  public int childrenCount();

  /**
   * Retrieves a child of this wbs node using the given index
   * @param index Index to be retrieved
   * @return The selected child
   * @throws IndexOutOfBoundsException if the index is out of range (index < 0 || index >=
 childrenCount())
   */
  public IWbsNodeReadOnly getChild(final int index);

  /**
   * @return The progress in percent (range 0-100)
   */
  public int getProgress();

  /**
   * Tries to find the index of a given wbs node in the children list of this node
   * @param node The node to search for
   * @return The index of the node in the children list or -1 if node is not a child of this wbs node
   */
  public int getChildIndex(final AbstractWbsNodeDO node);

  /**
   * @return true if childrenCount() > 0
   */
  public boolean hasChildren();

  /**
   * Tests whether the given wbs node is a child of this wbs node.
   * @param node The node to check
   * @return true if the given is a child of this wbs node, false otherwise
   */
  public boolean isChild(final AbstractWbsNodeDO node);

}
