/////////////////////////////////////////////////////////////////////////////
//
// Project   ProjectForge - Chimney Plugin
//
// Copyright 2012, Micromata GmbH
//           All rights reserved.
//
/////////////////////////////////////////////////////////////////////////////

package org.projectforge.plugins.chimney.gantt;

import java.util.List;

import org.joda.time.ReadablePeriod;
import org.projectforge.plugins.chimney.wbs.AbstractWbsNodeDO;

public interface PlannedResourcesProvider
{
  List<ReadablePeriod> findPlannedResources(AbstractWbsNodeDO wbsNode);
}
