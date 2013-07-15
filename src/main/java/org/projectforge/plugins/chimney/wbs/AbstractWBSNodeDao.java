/////////////////////////////////////////////////////////////////////////////
//
// Project   ProjectForge
//
// Copyright 2001-2009, Micromata GmbH, Kai Reinhard
//           All rights reserved.
//
/////////////////////////////////////////////////////////////////////////////

package org.projectforge.plugins.chimney.wbs;

import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang.Validate;
import org.projectforge.core.BaseDao;
import org.projectforge.core.BaseSearchFilter;
import org.projectforge.task.TaskDao;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
public abstract class AbstractWBSNodeDao<O extends AbstractWbsNodeDO> extends BaseDao<O>
{
  protected TaskDao taskDao;

  protected AbstractWBSNodeDao(final Class<O> clazz)
  {
    super(clazz);
  }

  @Override
  protected void onSaveOrModify(final O obj)
  {
    Validate.notNull(obj.getTask(), "TaskDO is not set.");
    // must explicitly save the task via TaskDao, otherwise TaskTree will not be updated
    taskDao.saveOrUpdate(obj.getTask());
  }

  @Override
  public List<O> getList(final BaseSearchFilter filter)
  {
    final List<O> result = super.getList(filter);
    if (!filter.isDeleted() && !filter.isIgnoreDeleted())
      // if only undeleted items were requested, filter out any transitively (via TaskDO)
      // deleted WBSNodeDOs that may not have been correctly updated, yet.
      for (final Iterator<O> it = result.iterator(); it.hasNext(); ) {
        final O item = it.next();
        // this check transitively also checks TaskDO
        if (item.isDeleted()) {
          // Deleted status has been automatically copied from TaskDO to WBSNodeDO.
          // Persist to DB so that it does not need to be filtered again, next time.
          update(item);
          // Remove item from list
          it.remove();
        }
      }
    return result;
  }

  @Override
  protected void onDelete(final O obj)
  {
    Validate.notNull(obj.getTask(), "TaskDO is not set");
    Validate.isTrue(obj.getParent() == null, "Node must be removed from its parent's child list before deleting it.");
    taskDao.markAsDeleted(obj.getTask());
  }

  public void setTaskDao(final TaskDao taskDao)
  {
    this.taskDao = taskDao;
  }

}
