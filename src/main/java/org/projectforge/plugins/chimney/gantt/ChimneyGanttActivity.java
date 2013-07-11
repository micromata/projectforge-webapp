/////////////////////////////////////////////////////////////////////////////
//
// Project   ProjectForge - Chimney Plugin
//
// Copyright 2012, Micromata GmbH
//           All rights reserved.
//
/////////////////////////////////////////////////////////////////////////////

package org.projectforge.plugins.chimney.gantt;

import org.joda.time.Period;
import org.joda.time.ReadableDateTime;
import org.projectforge.plugins.chimney.activities.WbsActivityDO;
import org.projectforge.plugins.chimney.wbs.AbstractWbsNodeDO;
import org.projectforge.plugins.chimney.web.gantt.model.AbstractGanttActivity;
import org.projectforge.plugins.chimney.web.gantt.model.GanttActivityVisualizationType;

public class ChimneyGanttActivity extends AbstractGanttActivity
{

  private final AbstractWbsNodeDO wbsNode;
  private final ReadableDateTime begin, end;
  private String linkUrl = "";


  public ChimneyGanttActivity(final WbsActivityDO activity, final ReadableDateTime begin, final ReadableDateTime end)
  {
    super(activity);
    this.wbsNode = activity.getWbsNode();
    this.begin = begin;
    this.end = end;
  }

  @Override
  public GanttActivityVisualizationType getVisualizationType() {
    if (Period.ZERO.equals(getActivity().getEffortEstimation())) {
      return GanttActivityVisualizationType.MILESTONE;
    } else {
      return super.getVisualizationType();
    }
  }

  @Override
  public ReadableDateTime getBeginDate()
  {
    return begin;
  }

  @Override
  public ReadableDateTime getEndDate()
  {
    return end;
  }

  public void setLinkUrl(final String linkUrl)
  {
    this.linkUrl = linkUrl;
  }

  @Override
  public String getLinkUrl()
  {
    return linkUrl;
  }

  public AbstractWbsNodeDO getWbsNode()
  {
    return wbsNode;
  }

}
