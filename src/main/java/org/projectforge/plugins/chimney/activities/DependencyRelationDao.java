/////////////////////////////////////////////////////////////////////////////
//
// Project   ProjectForge
//
// Copyright 2001-2009, Micromata GmbH, Kai Reinhard
//           All rights reserved.
//
/////////////////////////////////////////////////////////////////////////////

package org.projectforge.plugins.chimney.activities;

import java.util.List;

import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Restrictions;
import org.joda.time.DateTime;
import org.projectforge.core.BaseDao;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
public class DependencyRelationDao extends BaseDao<DependencyRelationDO>
{
  public DependencyRelationDao() {
    super(DependencyRelationDO.class);
    userRightId = DependencyRelationRight.USER_RIGHT_ID;
  }

  @Override
  public DependencyRelationDO newInstance()
  {
    return new DependencyRelationDO();
  }

  @Override
  protected void onSaveOrModify(final DependencyRelationDO obj)
  {
    obj.ensureValid();
  }

  public DependencyRelationDO getByActivities(final WbsActivityDO predActivity, final WbsActivityDO succActivity)
  {
    checkLoggedInUserSelectAccess();
    final DependencyRelationDO dependencyDO = getInternalByActivities(predActivity, succActivity);
    if (dependencyDO == null)
      return null;
    checkLoggedInUserSelectAccess(dependencyDO);
    return dependencyDO;
  }


  public DependencyRelationDO getInternalByActivities(final WbsActivityDO predActivity, final WbsActivityDO succActivity)
  {
    final DetachedCriteria criteria = DetachedCriteria.forClass(clazz);
    criteria.createCriteria("predecessor").add(Restrictions.eq("id", predActivity.getId()));
    criteria.createCriteria("successor").add(Restrictions.eq("id", succActivity.getId()));

    @SuppressWarnings("unchecked")
    final List<DependencyRelationDO> dependenciesList = getHibernateTemplate().findByCriteria(criteria);

    for (final DependencyRelationDO dep : dependenciesList) {
      if(!dep.isDeleted())
        return dep;
    }

    return null;
  }

  @Override
  protected void onDelete(final DependencyRelationDO obj)
  {
    super.onDelete(obj);
    final WbsActivityDO predecessor = obj.getPredecessor();
    if (predecessor != null)
      predecessor.removeSuccessorRelation(obj);

    final WbsActivityDO successor = obj.getSuccessor();
    if (successor != null)
      successor.removePredecessorRelation(obj);

    obj.setDeletedDateTime(new DateTime());
  }

}
