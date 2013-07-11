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
 * Exception that signals that the parent of a given wbs node equals null.
 * @author Sweeps <pf@byte-storm.com>
 */
public class WbsNodeIsNullException extends UserException
{
  private static final long serialVersionUID = -2079575337041965222L;

  public WbsNodeIsNullException()
  {
    super("plugins.chimney.errors.wbsnodeisnull");
  }

}
