/////////////////////////////////////////////////////////////////////////////
//
// Project   ProjectForge - Chimney Plugin
//
// Copyright 2012, Micromata GmbH
//           All rights reserved.
//
/////////////////////////////////////////////////////////////////////////////

package org.projectforge.plugins.chimney.wbs;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import org.apache.commons.collections.IteratorUtils;

/**
 * A filter used to only get parts of the children of a node
 * @author Sweeps <pf@byte-storm.com>
 */
public class WbsNodeFilter implements Serializable
{
  private static final long serialVersionUID = -4945318884150694729L;

  private final List<Class< ? extends AbstractWbsNodeDO>> childrenTypes;
  private final boolean excluding;

  /**
   * Creates a wbs node filter with a pre-defined list of childrenTypes and excluding flag
   * @param childrenTypes Filter with an array of children types
   * @param excluding If true, only children that do not match the specified classes are returned. If false, only children that match are returned.
   */
  public WbsNodeFilter(final List<Class< ? extends AbstractWbsNodeDO>> childrenTypes, final boolean excluding)
  {
    super();
    this.childrenTypes = childrenTypes;
    this.excluding = excluding;
  }

  /**
   * Creates a wbs node filter with excluding flag
   * @param excluding If true, only children that do not match the specified classes are returned. If false, only children that match are returned.
   */
  public WbsNodeFilter(final boolean excluding)
  {
    super();
    this.childrenTypes = new ArrayList<Class< ? extends AbstractWbsNodeDO>>();
    this.excluding = excluding;
  }

  /**
   * Adds the type of child to the current filter.
   * @param childType The child type to add to the filter.
   * @return this for chaining
   */
  public WbsNodeFilter addChildType(final Class< ? extends AbstractWbsNodeDO> childType) {
    childrenTypes.add(childType);
    return this;
  }

  /**
   * @return The value of the excluding flag
   */
  public boolean isExcluding()
  {
    return excluding;
  }

  /**
   * @return An unmodifiable iterator over this filter's children types
   */
  @SuppressWarnings("unchecked")
  public ListIterator<Class< ? extends AbstractWbsNodeDO>> getChildrenTypesIterator() {
    return IteratorUtils.unmodifiableListIterator(childrenTypes.listIterator());
  }

  /**
   * Determines if the given node is allowed by this filter
   * @param node The node to check
   * @return true if the node is allowed, false if not
   */
  public boolean isAllowed(final AbstractWbsNodeDO node) {
    boolean isAllowed = isExcluding();
    for (final ListIterator<Class< ? extends AbstractWbsNodeDO>> it = getChildrenTypesIterator(); it.hasNext();) {
      final Class< ? extends AbstractWbsNodeDO> childType = it.next();
      if (childType.isInstance(node)) {
        isAllowed = !isAllowed;
        break;
      }
    }
    return isAllowed;
  }
}
