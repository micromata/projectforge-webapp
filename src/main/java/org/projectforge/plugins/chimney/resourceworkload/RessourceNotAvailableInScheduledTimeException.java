/////////////////////////////////////////////////////////////////////////////
//
// Project   ProjectForge - Chimney Plugin
//
// Copyright 2012, Micromata GmbH
//           All rights reserved.
//
/////////////////////////////////////////////////////////////////////////////

package org.projectforge.plugins.chimney.resourceworkload;

import org.joda.time.Period;
import org.joda.time.ReadableInstant;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.PeriodFormatter;
import org.projectforge.core.UserException;
import org.projectforge.plugins.chimney.resourceplanning.ResourceAssignmentDO;
import org.projectforge.plugins.chimney.web.components.ChimneyJodaPeriodConverter;

/**
 * This exception should be thrown if a user is assignet to a task with a scheduled time interval where the user is not available
 * @author Sweeps <pf@byte-storm.com>
 */
public class RessourceNotAvailableInScheduledTimeException extends UserException
{

  /**
   * 
   */
  private static final long serialVersionUID = 8925324718715364437L;

  public RessourceNotAvailableInScheduledTimeException(final ResourceAssignmentDO ra, final ReadableInstant taskBeginDay, final ReadableInstant taskEndDay)
  {
    super("plugins.chimney.errors.illegalresourceplanning",
        ra.getUser().getDisplayUsername(),
        ra.getWbsNode().getTitle(),
        formatPlannedEffort(ra.getPlannedEffort()),
        DateTimeFormat.forStyle("S-").withLocale(ra.getUser().getLocale()).print(taskBeginDay),
        DateTimeFormat.forStyle("S-").withLocale(ra.getUser().getLocale()).print(taskEndDay)
    );

  }

  private static final String formatPlannedEffort(final Period plannedEffort) {
    final PeriodFormatter pf = ChimneyJodaPeriodConverter.getFormatter();
    return pf.print(plannedEffort);
  }



}
