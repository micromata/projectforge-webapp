/////////////////////////////////////////////////////////////////////////////
//
// Project   ProjectForge
//
// Copyright 2001-2009, Micromata GmbH, Kai Reinhard
//           All rights reserved.
//
/////////////////////////////////////////////////////////////////////////////

package org.projectforge.plugins.chimney.resourceplanning;

import java.util.Iterator;
import java.util.List;

import org.hibernate.criterion.Restrictions;
import org.projectforge.core.BaseDao;
import org.projectforge.core.BaseSearchFilter;
import org.projectforge.core.QueryFilter;
import org.projectforge.plugins.chimney.wbs.AbstractWbsNodeDO;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
public class ResourceAssignmentDao extends BaseDao<ResourceAssignmentDO>
{
  public ResourceAssignmentDao()
  {
    super(ResourceAssignmentDO.class);
    userRightId = ResourceAssignmentRight.USER_RIGHT_ID;
  }

  @Override
  public ResourceAssignmentDO newInstance()
  {
    return new ResourceAssignmentDO();
  }


  /**
   * @see org.projectforge.core.BaseDao#getList(org.projectforge.core.QueryFilter)
   */
  @Override
  @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
  public List<ResourceAssignmentDO> getList(final QueryFilter filter) {
    if (filter.getFilter().isDeleted() && !filter.getFilter().isIgnoreDeleted()) {
      return getListDeletedOnly(filter);
    } else if (!filter.getFilter().isDeleted() && !filter.getFilter().isIgnoreDeleted()) {
      return getListUndeletedOnly(filter);
    }
    return super.getList(filter);
  }

  private List<ResourceAssignmentDO> getListUndeletedOnly(final QueryFilter filter)
  {
    final List<ResourceAssignmentDO> list = super.getList(filter);
    for (final Iterator<ResourceAssignmentDO> it = list.iterator(); it.hasNext(); ) {
      final ResourceAssignmentDO ra = it.next();
      // remove any items of which a wbs node in the hierarchy is deleted
      if (ra.getWbsNode().isTransitivelyDeleted())
        it.remove();
    }
    return list;
  }

  private List<ResourceAssignmentDO> getListDeletedOnly(final QueryFilter filter)
  {
    // we only want deleted items, but wbs nodes can be deleted while the corresponding assignment is not.
    // we need to manipulate the filter to return deleted and undeleted items and filter out undeleted items.
    final BaseSearchFilter baseFilter = filter.getFilter();
    baseFilter.setIgnoreDeleted(true);

    final List<ResourceAssignmentDO> list = super.getList(filter);
    for (final Iterator<ResourceAssignmentDO> it = list.iterator(); it.hasNext(); ) {
      final ResourceAssignmentDO ra = it.next();
      // remove any items that are not deleted and of which no wbs node in the hierarchy is deleted
      if (!ra.isDeleted() && !ra.getWbsNode().isTransitivelyDeleted())
        it.remove();
    }
    return list;
  }

  //private List<ResourceAssignmentDO> cacheFilter(final QueryFilter filter, final List<ResourceAssignmentDO> list)
  //{
  //  filterCache.put(filter, list);
  //  return list;
  //}

  @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
  public List<ResourceAssignmentDO> getListByWbsNode(final AbstractWbsNodeDO node) {
    final QueryFilter queryFilter = new QueryFilter();
    queryFilter.add(Restrictions.eq("wbsNode", node));
    return this.getList(queryFilter);
  }
}
