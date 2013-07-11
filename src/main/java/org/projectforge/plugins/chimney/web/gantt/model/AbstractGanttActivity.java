/////////////////////////////////////////////////////////////////////////////
//
// Project   ProjectForge - Chimney Plugin
//
// Copyright 2012, Micromata GmbH
//           All rights reserved.
//
/////////////////////////////////////////////////////////////////////////////

package org.projectforge.plugins.chimney.web.gantt.model;

import java.util.Iterator;

import org.projectforge.plugins.chimney.activities.DependencyRelationType;
import org.projectforge.plugins.chimney.activities.IActivityReadOnly;
import org.projectforge.plugins.chimney.activities.IDependencyRelationReadOnly;

public abstract class AbstractGanttActivity extends AbstractVirtualGanttActivity implements IGanttActivity
{

  private final IActivityReadOnly<? extends IDependencyRelationReadOnly> activity;

  public AbstractGanttActivity(final IActivityReadOnly<? extends IDependencyRelationReadOnly> activity)
  {
    super(activity.getWbsNode());
    this.activity = activity;
  }

  public IActivityReadOnly<? extends IDependencyRelationReadOnly> getActivity()  {
    return activity;
  }


  @Override
  public Iterator<IGanttDependency> predecessorDependencyIterator() {
    return new Iterator<IGanttDependency>() {
      Iterator<? extends IDependencyRelationReadOnly> dependencyIt = activity.predecessorRelationsIterator();
      @Override
      public boolean hasNext()
      {
        return dependencyIt.hasNext();
      }

      @Override
      public IGanttDependency next()
      {
        final IDependencyRelationReadOnly dependency = dependencyIt.next();
        return new IGanttDependency() {
          @Override
          public int getPredecessorId()
          {
            return dependency.getPredecessor().getWbsNode().getId();
          }

          @Override
          public DependencyRelationType getDependencyRelationType()
          {
            return dependency.getType();
          }
        };
      }

      @Override
      public void remove()
      {
        throw new UnsupportedOperationException("Remove operation not supported by this iterator");
      }

    };

  };

}
