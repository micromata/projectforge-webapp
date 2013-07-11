/////////////////////////////////////////////////////////////////////////////
//
// Project   ProjectForge
//
// Copyright 2001-2009, Micromata GmbH, Kai Reinhard
//           All rights reserved.
//
/////////////////////////////////////////////////////////////////////////////

package org.projectforge.plugins.chimney.wbs;

public class WorkpackageDao extends AbstractWBSNodeDao<WorkpackageDO>
{

  protected WorkpackageDao()
  {
    super(WorkpackageDO.class);
    userRightId = WbsNodeRight.USER_RIGHT_ID;
  }

  @Override
  public WorkpackageDO newInstance()
  {
    return new WorkpackageDO();
  }

}
