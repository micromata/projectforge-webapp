/////////////////////////////////////////////////////////////////////////////
//
// Project   ProjectForge - Chimney Plugin
//
// Copyright 2012, Micromata GmbH
//           All rights reserved.
//
/////////////////////////////////////////////////////////////////////////////

package org.projectforge.plugins.chimney.web.projectmanagement.powerworkpackage;

import org.projectforge.core.UserException;

public class FormReloadException extends UserException
{
  private static final long serialVersionUID = 613472151942055712L;

  public FormReloadException()
  {
    super("plugins.chimney.errors.reloadexception");
  }
}
