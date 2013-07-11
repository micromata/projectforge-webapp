/////////////////////////////////////////////////////////////////////////////
//
// Project   ProjectForge
//
// Copyright 2001-2009, Micromata GmbH, Kai Reinhard
//           All rights reserved.
//
/////////////////////////////////////////////////////////////////////////////

package org.projectforge.plugins.chimney.wbs;

public class MilestoneDao extends AbstractWBSNodeDao<MilestoneDO>
{

  protected MilestoneDao()
  {
    super(MilestoneDO.class);
    userRightId = WbsNodeRight.USER_RIGHT_ID;
  }

  @Override
  public MilestoneDO newInstance()
  {
    return new MilestoneDO();
  }

}
