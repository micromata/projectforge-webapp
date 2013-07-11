/////////////////////////////////////////////////////////////////////////////
//
// Project   ProjectForge
//
// Copyright 2001-2009, Micromata GmbH, Kai Reinhard
//           All rights reserved.
//
/////////////////////////////////////////////////////////////////////////////

package org.projectforge.plugins.chimney.activities;

import java.util.Iterator;

import org.joda.time.ReadableDateTime;
import org.joda.time.ReadablePeriod;
import org.projectforge.plugins.chimney.wbs.IWbsNodeReadOnly;

public interface IActivityReadOnly<T>
{

  public IWbsNodeReadOnly getWbsNode();

  public ReadableDateTime getFixedBeginDate();

  public ReadablePeriod getEffortEstimation();

  public ReadableDateTime getFixedEndDate();

  public Iterator<T> predecessorRelationsIterator();

  public Iterator<T> successorRelationsIterator();
}
