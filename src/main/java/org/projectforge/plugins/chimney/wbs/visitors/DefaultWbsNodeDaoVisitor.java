/////////////////////////////////////////////////////////////////////////////
//
// Project   ProjectForge - Chimney Plugin
//
// Copyright 2012, Micromata GmbH
//           All rights reserved.
//
/////////////////////////////////////////////////////////////////////////////

package org.projectforge.plugins.chimney.wbs.visitors;

import org.projectforge.core.BaseDao;
import org.projectforge.plugins.chimney.wbs.AbstractVisitableBaseDO;
import org.projectforge.plugins.chimney.wbs.AbstractWbsNodeDO;
import org.projectforge.plugins.chimney.wbs.MilestoneDO;
import org.projectforge.plugins.chimney.wbs.MilestoneDao;
import org.projectforge.plugins.chimney.wbs.PhaseDO;
import org.projectforge.plugins.chimney.wbs.PhaseDao;
import org.projectforge.plugins.chimney.wbs.ProjectDO;
import org.projectforge.plugins.chimney.wbs.ProjectDao;
import org.projectforge.plugins.chimney.wbs.SubtaskDO;
import org.projectforge.plugins.chimney.wbs.SubtaskDao;
import org.projectforge.plugins.chimney.wbs.WbsNodeDao;
import org.projectforge.plugins.chimney.wbs.WorkpackageDO;
import org.projectforge.plugins.chimney.wbs.WorkpackageDao;

public class DefaultWbsNodeDaoVisitor implements IWbsNodeDaoVisitor
{
  // this visitor will be initialized as singleton by spring which may be used by multiple sessions concurrently.
  // To avoid synchronization, we use a ThreadLocal as state variable
  ThreadLocal<BaseDao< ? extends AbstractVisitableBaseDO< ? extends IVisitor>>> selectedDao = new ThreadLocal<BaseDao< ? extends AbstractVisitableBaseDO< ? extends IVisitor>>>();

  private MilestoneDao milestoneDao;

  private ProjectDao projectDao;

  private SubtaskDao subtaskDao;

  private WbsNodeDao wbsNodeDao;

  private WorkpackageDao workpackageDao;

  private PhaseDao phaseDao;

  public DefaultWbsNodeDaoVisitor()
  {
  }

  @Override
  public void visit(final MilestoneDO node)
  {
    selectedDao.set(milestoneDao);
  }

  @Override
  public void visit(final ProjectDO node)
  {
    selectedDao.set(projectDao);
  }

  @Override
  public void visit(final SubtaskDO node)
  {
    selectedDao.set(subtaskDao);
  }

  @Override
  public void visit(final WorkpackageDO node)
  {
    selectedDao.set(workpackageDao);
  }

  @Override
  public void visit(final AbstractWbsNodeDO node)
  {
    selectedDao.set(wbsNodeDao);
  }

  @Override
  public void visit(final PhaseDO node)
  {
    selectedDao.set(phaseDao);
  }

  @Override
  public BaseDao< ? extends AbstractVisitableBaseDO< ? extends IVisitor>> getDao()
  {
    return selectedDao.get();
  }

  public void setMilestoneDao(final MilestoneDao milestoneDao)
  {
    this.milestoneDao = milestoneDao;
  }

  public void setProjectDao(final ProjectDao projectDao)
  {
    this.projectDao = projectDao;
  }

  public void setSubtaskDao(final SubtaskDao subtaskDao)
  {
    this.subtaskDao = subtaskDao;
  }

  public void setWbsNodeDao(final WbsNodeDao wbsNodeDao)
  {
    this.wbsNodeDao = wbsNodeDao;
  }

  public void setWorkpackageDao(final WorkpackageDao workpackageDao)
  {
    this.workpackageDao = workpackageDao;
  }

  public void setPhaseDao(final PhaseDao phaseDao)
  {
    this.phaseDao = phaseDao;
  }

}
