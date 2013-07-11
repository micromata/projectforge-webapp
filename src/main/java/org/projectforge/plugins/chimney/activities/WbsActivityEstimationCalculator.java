/////////////////////////////////////////////////////////////////////////////
//
// Project   ProjectForge - Chimney Plugin
//
// Copyright 2012, Micromata GmbH
//           All rights reserved.
//
/////////////////////////////////////////////////////////////////////////////

package org.projectforge.plugins.chimney.activities;


public interface WbsActivityEstimationCalculator
{
  public void estimateBegin(WbsActivityDO activity);
  public void estimateEnd(WbsActivityDO activity);
  public void estimateDuration(WbsActivityDO activity);
}
