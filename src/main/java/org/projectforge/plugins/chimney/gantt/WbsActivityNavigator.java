/////////////////////////////////////////////////////////////////////////////
//
// Project   ProjectForge - Chimney Plugin
//
// Copyright 2012, Micromata GmbH
//           All rights reserved.
//
/////////////////////////////////////////////////////////////////////////////

package org.projectforge.plugins.chimney.gantt;

import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang.Validate;
import org.projectforge.plugins.chimney.activities.WbsActivityDO;
import org.projectforge.plugins.chimney.activities.WbsActivityDao;
import org.projectforge.plugins.chimney.wbs.AbstractWbsNodeDO;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Transactional(readOnly=true, propagation=Propagation.SUPPORTS)
public class WbsActivityNavigator
{
  private WbsActivityDao wbsActivityDao;

  @Deprecated
  public WbsActivityNavigator() {}

  public WbsActivityNavigator(final WbsActivityDao wbsActivityDao)
  {
    this.wbsActivityDao = wbsActivityDao;
  }

  public WbsActivityDao getWbsActivityDao()
  {
    return wbsActivityDao;
  }

  public void setWbsActivityDao(final WbsActivityDao wbsActivityDao)
  {
    this.wbsActivityDao = wbsActivityDao;
  }

  public List<WbsActivityDO> getChildren(final WbsActivityDO activity)
  {
    Validate.notNull(activity);
    if (activity.getId() == null) {
      throw new IllegalArgumentException("Can not get children for transient wbs activity");
    }

    final List<WbsActivityDO> children = new LinkedList<WbsActivityDO>();
    final AbstractWbsNodeDO wbsNode = activity.getWbsNode();
    for (int i = 0 ; i < wbsNode.childrenCount(); i++) {
      final AbstractWbsNodeDO child = wbsNode.getChild(i);
      children.add(wbsActivityDao.getByOrCreateFor(child));
    }

    return children;
  }

  public WbsActivityDO getParent(final WbsActivityDO a)
  {
    Validate.notNull(a);
    if (a.getId() == null) {
      throw new IllegalArgumentException("Can not get parent for transient wbs activity");
    }

    final AbstractWbsNodeDO possibleParentNode = a.getWbsNode().getParent();
    if (possibleParentNode != null) {
      return wbsActivityDao.getByOrCreateFor(possibleParentNode);
    }

    return null;
  }
}
