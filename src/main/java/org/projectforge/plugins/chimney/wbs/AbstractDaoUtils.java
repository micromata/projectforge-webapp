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
import org.projectforge.plugins.chimney.wbs.visitors.IVisitor;

/**
 * Dao utility base class to conveniently abstract from the existence of Daos.
 * DOs can be retrieved and stored without having to know the corresponding Dao object.
 * @author Sweeps <pf@byte-storm.com>
 */
public abstract class AbstractDaoUtils<V extends IVisitor> implements Serializable
{
  private static final long serialVersionUID = -2163766887819715863L;

  /**
   * Returns the Dao object for a given DO.
   * @param node The node that the Dao is needed for
   * @return A Dao object.
   */
  public abstract <T extends AbstractVisitableBaseDO<V>> BaseDao<T> getDaoFor(final T node);

  /**
   * Saves a DO object to the database without having to bother about finding the correct Dao.
   * Uses {@link #getDaoFor(AbstractVisitableBaseDO)} to retrieve the necessary Dao object.
   * @param node The object to be saved
   */
  public <T extends AbstractVisitableBaseDO<V>> void saveOrUpdate(final T node) {
    final BaseDao<T> dao = getDaoFor(node);
    dao.saveOrUpdate(node);
  }

  /**
   * Marks a DO object as deleted in the database without having to bother about finding the correct Dao.
   * Uses {@link #getDaoFor(AbstractVisitableBaseDO)} to retrieve the necessary Dao object.
   * @param node The object to be saved
   */
  public <T extends AbstractVisitableBaseDO<V>> void markAsDeleted(final T node) {
    final BaseDao<T> dao = getDaoFor(node);
    dao.markAsDeleted(node);
  }

  /**
   * Retrieves an object from the database given an id and prototype.
   * The prototype is needed to select the correct Dao internally.
   * 
   * <p><b>Hint:</b> This method can only be used on concrete types, not on abstract classes.</p>
   * 
   * @param id Id of the object to fetch
   * @param prototype A prototype, can be any instance of the desired object.
   * @return The retrieved database object
   */
  public <T extends AbstractVisitableBaseDO<V>> T getById(final Serializable id, final T prototype) {
    final BaseDao<T> dao = getDaoFor(prototype);
    return dao.getById(id);
  }

  /**
   * Retrieves a list of objects from the database given a filter and prototype.
   * The prototype is needed to select the correct Dao internally.
   * 
   * <p><b>Hint:</b> This method can only be used on concrete types, not on abstract classes.</p>
   * 
   * @param filter Search filter
   * @param prototype A prototype, can be any instance of the desired object.
   * @return The retrieved database object
   */
  public <T extends AbstractVisitableBaseDO<V>> List<T> getList(final BaseSearchFilter filter, final T prototype) {
    final BaseDao<T> dao = getDaoFor(prototype);
    return dao.getList(filter);
  }

  /**
   * Retrieves a list of objects from the database given a filter and prototype.
   * The prototype is needed to select the correct Dao internally.
   * 
   * <p><b>Hint:</b> This method can only be used on concrete types, not on abstract classes.</p>
   * 
   * @param filter Query filter
   * @param prototype A prototype, can be any instance of the desired object.
   * @return The retrieved database object
   */
  public <T extends AbstractVisitableBaseDO<V>> List<T> getList(final QueryFilter filter, final T prototype) {
    final BaseDao<T> dao = getDaoFor(prototype);
    return dao.getList(filter);
  }



}
