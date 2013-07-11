/////////////////////////////////////////////////////////////////////////////
//
// Project   ProjectForge - Chimney Plugin
//
// Copyright 2012, Micromata GmbH
//           All rights reserved.
//
/////////////////////////////////////////////////////////////////////////////

package org.projectforge.plugins.chimney.wbs;

import org.projectforge.core.UserException;

/**
 * Exception that signals that a node is not a child of a given parent node.
 * @author Sweeps <pf@byte-storm.com>
 */
public class WbsNodeIsNotAChildException extends UserException
{
  private static final long serialVersionUID = -8280333545865374322L;

  public WbsNodeIsNotAChildException(final AbstractWbsNodeDO node, final AbstractWbsNodeDO parent)
  {
    super("plugins.chimney.errors.wbsnodeisnotachild", node, parent);
  }

}
