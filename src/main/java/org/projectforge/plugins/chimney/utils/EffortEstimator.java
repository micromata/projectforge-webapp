/////////////////////////////////////////////////////////////////////////////
//
// Project   ProjectForge - Chimney Plugin
//
// Copyright 2012, Micromata GmbH
//           All rights reserved.
//
/////////////////////////////////////////////////////////////////////////////

package org.projectforge.plugins.chimney.utils;

import org.joda.time.Period;
import org.projectforge.plugins.chimney.activities.WbsActivityDO;

public interface EffortEstimator
{

  public abstract Period estimateEffort(final WbsActivityDO activity);

}