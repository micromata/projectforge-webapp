/////////////////////////////////////////////////////////////////////////////
//
// Project   ProjectForge - Chimney Plugin
//
// Copyright 2012, Micromata GmbH
//           All rights reserved.
//
/////////////////////////////////////////////////////////////////////////////

package org.projectforge.plugins.chimney.utils;

import java.util.HashMap;
import java.util.Map;

import org.joda.time.Period;
import org.projectforge.plugins.chimney.activities.WbsActivityDO;
import org.projectforge.plugins.chimney.gantt.WbsActivityNavigator;

/**
 * Use this class to get the summed up effort for an activity.
 * 
 * @author Sweeps <pf@byte-storm.com>
 * 
 */
public class CachingEffortEstimator implements EffortEstimator
{
  private Map<WbsActivityDO, Period> cache = new HashMap<WbsActivityDO, Period>();

  private WbsActivityNavigator wbsActivityNavigator;

  public CachingEffortEstimator()
  {
  }

  public CachingEffortEstimator(final WbsActivityNavigator wbsActivityNavigator)
  {
    this.wbsActivityNavigator = wbsActivityNavigator;
  }

  @Override
  public Period estimateEffort(final WbsActivityDO activity)
  {
    if (isCached(activity)) {
      return getCacheValue(activity);
    }

    final Period effortEstimation = activity.getEffortEstimation();
    if (effortEstimation != null) {
      return effortEstimation;
    }

    Period sum = null;

    for (final WbsActivityDO child: wbsActivityNavigator.getChildren(activity)) {
      final Period subEstimation = estimateEffort(child);
      if (subEstimation != null) {
        if (sum == null) {
          sum = new Period(0);
        }
        sum = sum.plus(subEstimation);
      }
    }

    sum = safeNormalize(sum);
    cache(activity, sum);
    return sum;
  }

  private Period safeNormalize(final Period sum)
  {
    if (sum != null) {
      return sum.normalizedStandard();
    }
    return null;
  }

  public void resetCache()
  {
    cache = new HashMap<WbsActivityDO, Period>();
  }

  private void cache(final WbsActivityDO activity, final Period estimatedEffort)
  {
    cache.put(activity, estimatedEffort);
  }

  private Period getCacheValue(final WbsActivityDO activity)
  {
    return cache.get(activity);
  }

  private boolean isCached(final WbsActivityDO activity)
  {
    return cache.containsKey(activity);
  }

  public void setCache(final HashMap<WbsActivityDO, Period> cache)
  {
    this.cache = cache;
  }

  public Map<WbsActivityDO, Period> getCache()
  {
    return cache;
  }

  public void setWbsActivityNavigator(final WbsActivityNavigator wbsActivityNavigator)
  {
    this.wbsActivityNavigator = wbsActivityNavigator;
  }
}
