/////////////////////////////////////////////////////////////////////////////
//
// Project   ProjectForge - Chimney Plugin
//
// Copyright 2012, Micromata GmbH
//           All rights reserved.
//
/////////////////////////////////////////////////////////////////////////////

package org.projectforge.plugins.chimney.wbs;

import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
public class PhaseDao extends AbstractWBSNodeDao<PhaseDO>
{
  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(PhaseDao.class);

  protected PhaseDao()
  {
    super(PhaseDO.class);
    userRightId = WbsNodeRight.USER_RIGHT_ID;
  }

  @Override
  public PhaseDO newInstance()
  {
    return new PhaseDO();
  }

  @Override
  protected void onSaveOrModify(final PhaseDO obj)
  {
    // prevent a TaskDO from being saved to the task table
    obj.setTask(null);
    log.info("onSaveOrModify: taskDO auf null gesetzt");
  }

  @Override
  protected void onDelete(final PhaseDO obj)
  {
    // prevent the TaskDO to be deleted from the task table (it does not even exist there)
    obj.setTask(null);
    log.info("onDelete");
  }

}
