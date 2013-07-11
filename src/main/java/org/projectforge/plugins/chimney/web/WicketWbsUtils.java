/////////////////////////////////////////////////////////////////////////////
//
// Project   ProjectForge - Chimney Plugin
//
// Copyright 2012, Micromata GmbH
//           All rights reserved.
//
/////////////////////////////////////////////////////////////////////////////

package org.projectforge.plugins.chimney.web;

import java.io.Serializable;

import org.apache.wicket.injection.Injector;
import org.apache.wicket.model.IModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.projectforge.core.BaseDao;
import org.projectforge.plugins.chimney.wbs.AbstractVisitableBaseDO;
import org.projectforge.plugins.chimney.wbs.WbsNodeDao;
import org.projectforge.plugins.chimney.wbs.WbsNodeUtils;
import org.projectforge.plugins.chimney.wbs.visitors.DefaultWbsNodeDaoVisitor;
import org.projectforge.plugins.chimney.wbs.visitors.IWbsNodeVisitor;

public class WicketWbsUtils extends WbsNodeUtils
{
  private static final long serialVersionUID = -1469626159536398305L;

  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(WicketWbsUtils.class);

  @SpringBean(name = "wbsNodeDao")
  private transient WbsNodeDao wbsNodeDao;

  @SpringBean(name = "wbsNodeDaoFinder")
  private transient DefaultWbsNodeDaoVisitor daoFinder;

  /**
   * Returns a database-backed {@link IModel} for the passed object
   * @param node The object a model is requested for
   * @return A database-backed {@link IModel} for the passed object
   */
  public <T extends AbstractVisitableBaseDO<IWbsNodeVisitor>> IModel<T> getModelFor(final T node)
  {
    return new DetachableChangeableDOModelInternal<T, BaseDao<T>>(node);
  }

  /**
   * Convenience method to save a DO object held by a model to the database without having to bother about finding the correct Dao. Calls
   * {@link #saveOrUpdate(AbstractVisitableBaseDO)} internally
   * @param model Model of the object to be saved
   */
  public <T extends AbstractVisitableBaseDO<IWbsNodeVisitor>> void saveOrUpdate(final IModel<T> model)
  {
    saveOrUpdate(model.getObject());
  }

  /**
   * Returns a database-backed {@link IModel} for the given id and prototype. The prototype is needed to select the correct Dao internally.
   * 
   * <p>
   * <b>Hint:</b> This method can only be used on concrete types, not on abstract classes.
   * </p>
   * 
   * @param id Id of the object to fetch
   * @param prototype A prototype, can be any instance of the desired object.
   * @return A database-backed {@link IModel} for the database object or null if no such object exists in the database
   */
  public <T extends AbstractVisitableBaseDO<IWbsNodeVisitor>> IModel<T> getModelFor(final Serializable id, final T prototype)
  {
    final T node = this.getById(id, prototype);
    if (node == null)
      return null;
    return new DetachableChangeableDOModelInternal<T, BaseDao<T>>(node);
  }

  @Override
  protected WbsNodeDao getWbsNodeDao()
  {
    if (wbsNodeDao == null)
      Injector.get().inject(this);
    return wbsNodeDao;
  }

  @Override
  protected DefaultWbsNodeDaoVisitor getDaoFinder()
  {
    if (daoFinder == null)
      Injector.get().inject(this);
    return daoFinder;
  }

  /**
   * Modified DetachableChangeableDOModel that ensures that always the correct Dao object is used for all operations.
   * @author Sweeps <pf@byte-storm.com>
   */
  class DetachableChangeableDOModelInternal<D extends AbstractVisitableBaseDO<IWbsNodeVisitor>, B extends BaseDao<D>> extends
      DetachableChangeableDOModel<D, B>
  {
    private static final long serialVersionUID = -6889719521471597893L;

    private transient BaseDao<D> dao;

    private D daoTemplate;

    public DetachableChangeableDOModelInternal(final D object)
    {
      super(object, null);
    }

    @Override
    public void detach()
    {
      daoTemplate = getBaseDao().newInstance();
      super.detach();
    }

    @Override
    public void setObject(final D object)
    {
      dao = null;
      super.setObject(object);
    }

    @Override
    protected BaseDao<D> getBaseDao()
    {
      if (dao == null)
        dao = WicketWbsUtils.this.getDaoFor(getObject() != null ? getObject() : daoTemplate);
      return dao;
    }
  }

  // ----------------
  // Spring setters
  // ----------------
  public void setDaoFinder(final DefaultWbsNodeDaoVisitor daoFinder)
  {
    log.info(daoFinder.getClass() + " " + daoFinder.toString() + " injected");
    this.daoFinder = daoFinder;
  }

  public void setWbsNodeDao(final WbsNodeDao wbsNodeDao)
  {
    log.info(wbsNodeDao.getClass() + " " + wbsNodeDao.toString() + " injected");
    this.wbsNodeDao = wbsNodeDao;
  }

}
