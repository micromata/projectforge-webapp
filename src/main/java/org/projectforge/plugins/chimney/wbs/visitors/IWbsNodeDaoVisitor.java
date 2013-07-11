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

/**
 * Extended wbs node visitor that returns Dao objects for the visitee.
 * @author Sweeps <pf@byte-storm.com>
 */
public interface IWbsNodeDaoVisitor extends IWbsNodeVisitor
{
  public BaseDao< ? extends AbstractVisitableBaseDO< ? extends IVisitor>> getDao();

}
