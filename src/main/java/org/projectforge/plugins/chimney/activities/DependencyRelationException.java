/////////////////////////////////////////////////////////////////////////////
//
// Project   ProjectForge - Chimney Plugin
//
// Copyright 2012, Micromata GmbH
//           All rights reserved.
//
/////////////////////////////////////////////////////////////////////////////

package org.projectforge.plugins.chimney.activities;

import org.projectforge.core.MessageParam;
import org.projectforge.core.UserException;

public abstract class DependencyRelationException extends UserException
{
  public DependencyRelationException(final String i18nKey, final MessageParam... msgParams)
  {
    super(i18nKey, msgParams);
  }

  public DependencyRelationException(final String i18nKey, final Object... params)
  {
    super(i18nKey, params);
  }

  public DependencyRelationException(final String i18nKey)
  {
    super(i18nKey);
  }

  /**
   * 
   */
  private static final long serialVersionUID = 2249163767597589990L;

}
