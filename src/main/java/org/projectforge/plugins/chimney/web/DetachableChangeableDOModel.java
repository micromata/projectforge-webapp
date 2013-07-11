/////////////////////////////////////////////////////////////////////////////
//
// Project   ProjectForge - Chimney Plugin
//
// Copyright 2012, Micromata GmbH
//           All rights reserved.
//
/////////////////////////////////////////////////////////////////////////////

package org.projectforge.plugins.chimney.web;

import org.apache.commons.lang.Validate;
import org.apache.wicket.model.LoadableDetachableModel;
import org.projectforge.core.BaseDao;
import org.projectforge.core.ExtendedBaseDO;

/**
 * Detachable model for DO objects that ensures that the model object is always an object backed a valid, open database session.
 * It lazily pulls a new copy of the object from the database when the model is used for the first time in a request-response cycle.
 * The encapsulated DO object can be changed even after model creation.
 * @author Sweeps <pf@byte-storm.com>
 */
public class DetachableChangeableDOModel<D extends ExtendedBaseDO< ? extends Integer>, B extends BaseDao<D>> extends LoadableDetachableModel<D>
{
  private static final long serialVersionUID = 2153617089037166695L;

  protected Integer id;

  protected B baseDao;

  public DetachableChangeableDOModel(final D object, final B baseDao)
  {
    super(object);
    Validate.notNull(object);
    Validate.notNull(object.getId());
    this.id = object.getId();
    this.baseDao = baseDao;
  }

  /**
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode()
  {
    return Long.valueOf(id).hashCode();
  }

  /**
   * used for data view with ReuseIfModelsEqualStrategy item reuse strategy
   * 
   * @see org.apache.wicket.markup.repeater.ReuseIfModelsEqualStrategy
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals(final Object obj)
  {
    if (obj == this) {
      return true;
    } else if (obj == null) {
      return false;
    } else if (obj instanceof DetachableChangeableDOModel< ? , ? >) {
      final DetachableChangeableDOModel< ? , ? > other = (DetachableChangeableDOModel< ? , ? >) obj;
      return other.id == id;
    }
    return false;
  }

  /**
   * @see org.apache.wicket.model.LoadableDetachableModel#load()
   */
  @Override
  protected D load()
  {
    // loads data object from the database
    return getBaseDao().getById(id);
  }

  protected BaseDao<D> getBaseDao()
  {
    return baseDao;
  }

  @Override
  public void setObject(final D object)
  {
    Validate.notNull(object);
    if (object.getId() != null)
      this.id = object.getId();
    super.setObject(object);
  }

}
