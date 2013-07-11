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

import org.apache.commons.lang.Validate;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Restrictions;
import org.projectforge.access.AccessException;
import org.projectforge.core.BaseDao;
import org.projectforge.plugins.chimney.wbs.AbstractWbsNodeDO;
import org.projectforge.plugins.chimney.wbs.IWbsNodeReadOnly;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
public class WbsActivityDao extends BaseDao<WbsActivityDO>
{
  public WbsActivityDao()
  {
    super(WbsActivityDO.class);
    userRightId = WbsActivityRight.USER_RIGHT_ID;
  }

  @Override
  public WbsActivityDO newInstance()
  {
    return new WbsActivityDO();
  }

  /**
   * Retrieves an activity by a wbs node
   * @param wbsNode The wbs node
   * @return the activity corresponding to the given wbsNode or null
   */
  public WbsActivityDO getByWbsNode(final IWbsNodeReadOnly wbsNode) throws AccessException
  {
    Validate.notNull(wbsNode);
    checkLoggedInUserSelectAccess();
    final WbsActivityDO activityDO = internalGetByWbsNode(wbsNode);
    if (activityDO == null)
      return null;
    checkLoggedInUserSelectAccess(activityDO);
    return activityDO;
  }

  private WbsActivityDO internalGetByWbsNode(final IWbsNodeReadOnly wbsNode)
  {
    final DetachedCriteria criteria = DetachedCriteria.forClass(clazz);
    criteria.createCriteria("wbsNode").add(Restrictions.eq("id", wbsNode.getId()));

    @SuppressWarnings("unchecked")
    final List<WbsActivityDO> activitiesList = getHibernateTemplate().findByCriteria(criteria);

    if (activitiesList.isEmpty())
      return null;
    return activitiesList.get(0);
  }

  /**
   * <p>Retrieves an activity by a wbs node.</p>
   * <p>If the activity does not already exist, it will be created (and persisted) on the fly.</p>
   * @param wbsNode must be non-transient
   * @throws IllegalArgumentException if the argument is transient or null
   * @return
   */
  public WbsActivityDO getByOrCreateFor(final AbstractWbsNodeDO wbsNode)
  {
    Validate.notNull(wbsNode);

    if (wbsNode.getId() == null) {
      throw new IllegalArgumentException(String.format("Wbs node '%s' must not be transient to get or create activity for it.", wbsNode.toString()));
    }

    final WbsActivityDO possibleActivity = getByWbsNode(wbsNode);
    if (possibleActivity != null) {
      return possibleActivity;
    }

    return createActivityFor(wbsNode);
  }

  private WbsActivityDO createActivityFor(final AbstractWbsNodeDO abstractWbsNodeDO)
  {
    final WbsActivityDO activity = new WbsActivityDO(abstractWbsNodeDO);
    this.save(activity);
    return activity;
  }
}
