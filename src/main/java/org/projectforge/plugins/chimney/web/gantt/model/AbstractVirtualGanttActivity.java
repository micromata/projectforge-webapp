/////////////////////////////////////////////////////////////////////////////
//
// Project   ProjectForge - Chimney Plugin
//
// Copyright 2012, Micromata GmbH
//           All rights reserved.
//
/////////////////////////////////////////////////////////////////////////////

package org.projectforge.plugins.chimney.web.gantt.model;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.joda.time.ReadableDateTime;
import org.projectforge.plugins.chimney.wbs.IWbsNodeReadOnly;

public abstract class AbstractVirtualGanttActivity implements IGanttActivity
{

  private final IWbsNodeReadOnly wbsNode;
  public AbstractVirtualGanttActivity(final IWbsNodeReadOnly wbsNode) {
    this.wbsNode = wbsNode;
  }
  @Override
  public Integer getId()
  {
    return wbsNode.getId();
  }

  @Override
  public String getTitle()
  {
    return wbsNode.getTitle();
  }

  @Override
  public String getWbsCode()
  {
    return wbsNode.getWbsCode();
  }

  @Override
  public boolean hasParent()
  {
    return wbsNode.getParent() != null;
  }

  @Override
  public Integer getParentId()
  {
    return wbsNode.getParent().getId();
  }

  @Override
  public abstract ReadableDateTime getBeginDate();

  @Override
  public abstract ReadableDateTime getEndDate();

  @Override
  public int getProgress()
  {
    return wbsNode.getProgress();
  }

  @Override
  public GanttActivityVisualizationType getVisualizationType()
  {
    return GanttActivityVisualizationType.SPAN;
  }

  @Override
  public Iterator<IGanttDependency> predecessorDependencyIterator()
  {
    final List<IGanttDependency> emptyList = Collections.emptyList();
    return emptyList.iterator();
  }

}
