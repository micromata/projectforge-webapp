/////////////////////////////////////////////////////////////////////////////
//
// Project   ProjectForge
//
// Copyright 2001-2009, Micromata GmbH, Kai Reinhard
//           All rights reserved.
//
/////////////////////////////////////////////////////////////////////////////

package org.projectforge.plugins.chimney.wbs;

public class SubtaskDao extends AbstractWBSNodeDao<SubtaskDO>
{

  protected SubtaskDao()
  {
    super(SubtaskDO.class);
    userRightId = WbsNodeRight.USER_RIGHT_ID;
  }

  @Override
  public SubtaskDO newInstance()
  {
    return new SubtaskDO();
  }

}
