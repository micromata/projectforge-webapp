/////////////////////////////////////////////////////////////////////////////
//
// Project   ProjectForge - Chimney Plugin
//
// Copyright 2012, Micromata GmbH
//           All rights reserved.
//
/////////////////////////////////////////////////////////////////////////////

package org.projectforge.plugins.chimney.wbs;

import org.projectforge.plugins.chimney.wbs.visitors.IVisitor;

/**
 * A generic visitable interface for the Visitor Pattern
 * @author Sweeps <pf@byte-storm.com>
 */
public interface IVisitable<T extends IVisitor>
{
  public void accept(T visitor);
}
