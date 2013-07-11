/////////////////////////////////////////////////////////////////////////////
//
// Project   ProjectForge - Chimney Plugin
//
// Copyright 2012, Micromata GmbH
//           All rights reserved.
//
/////////////////////////////////////////////////////////////////////////////

package org.projectforge.plugins.chimney.utils.date;

import org.joda.time.DateTime;
import org.projectforge.common.DateFormatType;
import org.projectforge.common.DateFormats;

public class InconsistentFixedDatesException extends RuntimeException
{

  @Override
  public String getMessage()
  {
    final String pattern = DateFormats.getFormatString(DateFormatType.DATE);;
    return String.format("Inconsistent begin and end dates. Begin: %s, End: %s", fixedBeginDate.toString(pattern), fixedEndDate.toString(pattern));
  }

  /**
   * 
   */
  private static final long serialVersionUID = 2722424768725963321L;
  private final DateTime fixedBeginDate;
  private final DateTime fixedEndDate;

  public InconsistentFixedDatesException(final DateTime fixedBeginDate, final DateTime fixedEndDate)
  {
    this.fixedBeginDate = fixedBeginDate;
    this.fixedEndDate = fixedEndDate;
  }
}
