/////////////////////////////////////////////////////////////////////////////
//
// Project   ProjectForge - Chimney Plugin
//
// Copyright 2012, Micromata GmbH
//           All rights reserved.
//
/////////////////////////////////////////////////////////////////////////////

package org.projectforge.plugins.chimney.wbs;

import java.io.Serializable;
import java.util.List;

import org.projectforge.core.BaseDao;
import org.projectforge.core.BaseSearchFilter;
import org.projectforge.core.QueryFilter;
import org.projectforge.plugins.chimney.wbs.visitors.DefaultWbsNodeDaoVisitor;
import org.projectforge.plugins.chimney.wbs.visitors.GetProjectRootVisitor;
import org.projectforge.plugins.chimney.wbs.visitors.IWbsNodeDaoVisitor;
import org.projectforge.plugins.chimney.wbs.visitors.IWbsNodeVisitor;

/**
 * Helper class for various Wbs node related operations like changing the parent node,
 * getting the right Dao, a database backed model, etc.
 * @author Sweeps <pf@byte-storm.com>
 */
public abstract class WbsNodeUtils extends AbstractDaoUtils<IWbsNodeVisitor>
{
  private static final long serialVersionUID = 544955025401102862L;

  /**
   * Changes the parent of node to newParent
   * @param node The node that is to be rewired
   * @param newParent The new parent
   * @throws IllegalArgumentException if newParent is not a compatible parent for node
   */
  public void changeParentAndSave(final AbstractWbsNodeDO node, final AbstractWbsNodeDO newParent) {
    if (newParent == null) {
      throw new WbsNodeParentIsNullException();
    }
    final AbstractWbsNodeDO oldParent = node.getParent();

    // A new child is born, so we update the autoincrement
    if (oldParent == null) {
      newParent.autoIncrementAndGet();
    }

    // update references only if parent really changed
    if (!newParent.equals(oldParent)) {
      // remove workpackage from children of old parent if applicable
      if (oldParent != null) {
        final boolean removedSomething = oldParent.removeChild(node);
        if (!removedSomething)
          throw new IllegalStateException("Something went terribly wrong. The old parent node claims to not have this workpackage as child!?");
      }
      // add workpackage as child of new parent
      newParent.addChild(node);
      // save the wbs node before persisting parent changes to be able to handle exceptions thrown by underlying TaskDO/Dao
      try {
        getDaoFor(node).saveOrUpdate(node);
      } catch (final RuntimeException ex) {
        // saving the node failed, undo change of parent references
        newParent.removeChild(node);
        if (oldParent != null) {
          oldParent.addChild(node);
        }
        // Decrement auto-increment
        newParent.setAutoIncrementChildren(newParent.getAutoIncrementChildren()-1);

        // re-throw exception
        throw ex;
      }
      // save old parent node to persist changed children list
      if (oldParent != null)
        getDaoFor(oldParent).saveOrUpdate(oldParent);
      // save old and new parent node to persist changed children list
      getDaoFor(newParent).saveOrUpdate(newParent);
    } else {
      getDaoFor(node).saveOrUpdate(node);
    }
  }

  /**
   * Returns the Dao object for a given DO by asking the set {@link IWbsNodeDaoVisitor} instance to the DO.
   * @param node The node that the Dao is needed for
   * @return A Dao object as returned by the given {@link IWbsNodeDaoVisitor} implementation.
   */
  @Override
  @SuppressWarnings("unchecked")
  public <T extends AbstractVisitableBaseDO<IWbsNodeVisitor>> BaseDao<T> getDaoFor(final T node) {
    node.accept(getDaoFinder());
    return (BaseDao<T>) getDaoFinder().getDao();
  }

  /**
   * Retrieves an wbs node object from the database given an id.
   * This method can be used to retrieve wbs nodes if the concrete type
   * is not known at compile time.
   * @param id Id of the object to fetch
   * @return The retrieved database object
   */
  public AbstractWbsNodeDO getById(final Serializable id) {
    return getWbsNodeDao().getById(id);
  }

  /**
   * Retrieves a list of wbs node objects from the database given a filter.
   * This method can be used to retrieve wbs nodes if the concrete type
   * is not known at compile time.
   * @param id Id of the object to fetch
   * @return The retrieved database object
   */
  public List<AbstractWbsNodeDO> getList(final BaseSearchFilter filter) {
    return getWbsNodeDao().getList(filter);
  }

  /**
   * Retrieves a list of wbs node objects from the database given a filter.
   * This method can be used to retrieve wbs nodes if the concrete type
   * is not known at compile time.
   * @param id Id of the object to fetch
   * @return The retrieved database object
   */
  public List<AbstractWbsNodeDO> getList(final QueryFilter filter) {
    return getWbsNodeDao().getList(filter);
  }

  /**
   * Marks the given wbs node as deleted, removes the node from its parent's
   * children list (if applicable) and persists it in the database.
   * @param node The node to be marked as deleted
   */
  public void markAsDeleted(final AbstractWbsNodeDO node)
  {
    // remove node as child from its parent
    final AbstractWbsNodeDO parent = node.getParent();
    if (parent != null) {
      parent.removeChild(node);
      saveOrUpdate(parent);
    }
    super.markAsDeleted(node);
  }

  /**
   * Returns the project node for a given wbs node, or null,
   * if there is no project at the top of the hierarchy.
   * @param node The node for which the project is to fetched for.
   * @return The project of node or null
   */
  public static ProjectDO getProject(final AbstractWbsNodeDO node) {
    final GetProjectRootVisitor rv = new GetProjectRootVisitor();
    node.accept(rv);
    return rv.getProjectRoot();
  }

  /**
   * @return The Dao for looking up wbs nodes. Use this instead of accessing the WbsNodeDao directly
   *  to prevent NullPointerExceptions due to the Dao being transient and this utility class being serialized.
   */
  protected abstract WbsNodeDao getWbsNodeDao();

  /**
   * @return A Visitor for finding the responsible Dao for a wbs node
   */
  protected abstract DefaultWbsNodeDaoVisitor getDaoFinder();

}
