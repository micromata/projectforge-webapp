/////////////////////////////////////////////////////////////////////////////
//
// Project   ProjectForge - Chimney Plugin
//
// Copyright 2012, Micromata GmbH
//           All rights reserved.
//
/////////////////////////////////////////////////////////////////////////////

package org.projectforge.plugins.chimney.gantt;

import org.projectforge.core.MessageParam;
import org.projectforge.core.UserException;
import org.projectforge.plugins.chimney.activities.WbsActivityDO;

/**
 * UserException for any illegal state of an activity
 * @author Sweeps <pf@byte-storm.com>
 *
 */
public class IllegalActivityStateException extends UserException
{

  /**
   * 
   */
  private static final long serialVersionUID = -3057363623418385529L;

  private final WbsActivityDO causeActivity;

  public IllegalActivityStateException(final WbsActivityDO causeActivity, final String i18nKey)
  {
    super(i18nKey);
    this.causeActivity = causeActivity;
  }

  public IllegalActivityStateException(final WbsActivityDO causeActivity, final String i18nKey, final Object... params)
  {
    super(i18nKey, params);
    this.causeActivity = causeActivity;
  }

  public IllegalActivityStateException(final WbsActivityDO causeActivity, final String i18nKey, final MessageParam... msgParams)
  {
    super(i18nKey, msgParams);
    this.causeActivity = causeActivity;
  }

  /**
   * @return the WbsActivityDO without the required fixed begin date set.
   */
  public WbsActivityDO getCauseActivity()
  {
    return causeActivity;
  }

}
