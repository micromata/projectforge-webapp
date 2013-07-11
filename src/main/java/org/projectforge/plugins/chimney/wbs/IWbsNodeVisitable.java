/////////////////////////////////////////////////////////////////////////////
//
// Project   ProjectForge
//
// Copyright 2001-2009, Micromata GmbH, Kai Reinhard
//           All rights reserved.
//
/////////////////////////////////////////////////////////////////////////////

package org.projectforge.plugins.chimney.wbs;

import org.projectforge.plugins.chimney.wbs.visitors.IWbsNodeVisitor;

/**
 * Visitable interface for WbsNodes
 * @author Sweeps <pf@byte-storm.com>
 */
public interface IWbsNodeVisitable extends IVisitable<IWbsNodeVisitor>
{
  public void accept(IWbsNodeVisitor visitor);
}
