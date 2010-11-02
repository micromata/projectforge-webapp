/////////////////////////////////////////////////////////////////////////////
//
// Project ProjectForge Community Edition
//         www.projectforge.org
//
// Copyright (C) 2001-2010 Kai Reinhard (k.reinhard@me.com)
//
// ProjectForge is dual-licensed.
//
// This community edition is free software; you can redistribute it and/or
// modify it under the terms of the GNU General Public License as published
// by the Free Software Foundation; version 3 of the License.
//
// This community edition is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
// Public License for more details.
//
// You should have received a copy of the GNU General Public License along
// with this program; if not, see http://www.gnu.org/licenses/.
//
/////////////////////////////////////////////////////////////////////////////

package org.projectforge.web.core;

import java.lang.reflect.Method;
import java.util.List;

import org.projectforge.common.BeanHelper;
import org.projectforge.core.BaseDO;
import org.projectforge.core.BaseDao;
import org.projectforge.core.DisplayHistoryEntry;
import org.projectforge.core.ExtendedBaseDO;

import net.sourceforge.stripes.action.ActionBean;
import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.DontValidate;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.RedirectResolution;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.validation.Validate;

/**
 * The convenient base class for edit actions.
 */
public abstract class BaseEditActionBean<D extends BaseDao<O>, O extends ExtendedBaseDO<Integer>> extends ExtendedActionBean
{
  protected Integer id;

  protected D baseDao;

  private O data;

  protected boolean showHistory = false;

  protected List<DisplayHistoryEntry> historyEntries = null;

  /**
   * Do nothing. Overwrite this for doing some stuff in execute first. If a resolution is returned, execution will not be continued and the
   * resolution will be returned.
   * @return null.
   */
  protected Resolution onExecute()
  {
    // Do nothing.
    return null;
  }

  /**
   * Needed for instantiating new data object.
   * @return
   */
  protected abstract O createDataInstance();

  public O getData()
  {
    if (data == null) {
      // Muss gesetzt werden, da Stripes dieses Objekt z. B. nach der Rückkehr von einem select date dieses nur instantiiert, wenn auch
      // Felder von data gefüllt waren. Sonst gibt es eine NPE.
      data = createDataInstance();
    }
    return data;
  }

  protected void setData(O data)
  {
    this.data = data;
  }

  public void setId(Integer id)
  {
    this.id = id;
  }

  @Validate
  public Integer getId()
  {
    return id;
  }

  public void setShowHistory(boolean showHistory)
  {

    this.showHistory = showHistory;
  }

  public boolean isShowHistory()
  {
    return showHistory;
  }

  public boolean isBaseDO()
  {
    return (this.data instanceof BaseDO<?>);
  }

  public List<DisplayHistoryEntry> getHistory()
  {
    if (historyEntries == null) {
      historyEntries = baseDao.getDisplayHistoryEntries(getData());
    }
    return historyEntries;
  }

  /**
   * Override this method if some initial data or fields have to be set. onPreEdit will be called on both, on adding new data objects and on
   * updating existing data objects. The decision on adding or updating depends on getData().getId() != null.
   */
  protected void onPreEdit()
  {
    // Do nothing at default
  }

  /**
   * Will be called before the data object will be stored. Does nothing at default. If a resolution is returned the processing of the
   * request will not be continued normally, the request results in the returned resolution.
   */
  protected Resolution onSaveOrUpdate()
  {
    // Do nothing at default
    return null;
  }

  /**
   * Will be called before the data object will be stored. Here you can add validation errors manually. If this method returns a resolution
   * then method create and update will redirect to this resolution without calling the baseDao.save or baseDao.update methods. <br/> Here
   * you can do validations with add(Global)Error or manipulate the data object before storing to the database etc. <br/> Will be called
   * directly after onSaveOrUpdate.
   */
  protected void validate()
  {
    // Do nothing at default;
  }

  /**
   * Will be called before the data object will be deleted or marked as deleted. Here you can add validation errors manually. If this method
   * returns a resolution then a redirect to this resolution without calling the baseDao methods will done. <br/> Here you can do
   * validations with add(Global)Error or manipulate the data object before storing to the database etc.
   */
  protected Resolution onDelete()
  {
    // Do nothing at default
    return null;
  }

  /**
   * Will be called before the data object will be restored (undeleted). Here you can add validation errors manually. If this method returns
   * a resolution then a redirect to this resolution without calling the baseDao methods will done. <br/> Here you can do validations with
   * add(Global)Error or manipulate the data object before storing to the database etc.
   */
  protected Resolution onUndelete()
  {
    // Do nothing at default
    return null;
  }

  /**
   * Will be called directly after storing the data object (insert, update, delete). If a not null resolution is returned, then the
   * resolution will be returned to stripes controller.
   */
  protected Resolution afterSaveOrUpdate()
  {
    // Do nothing at default.
    return null;
  }

  /**
   * Will be called directly after storing the data object (insert). If a not null resolution is returned, then the resolution will be
   * returned to stripes controller.
   */
  protected Resolution afterSave()
  {
    // Do nothing at default.
    return null;
  }

  /**
   * Will be called directly after storing the data object (update).
   * @param modified true, if the object was modified, otherwise false.If a not null resolution is returned, then the resolution will be
   *                returned to stripes controller.
   * @see BaseDao#update(ExtendedBaseDO)
   */
  protected Resolution afterUpdate(boolean modified)
  {
    // Do nothing at default.
    return null;
  }

  /** Loads the data on to the form ready for editing. */
  @DontValidate
  public Resolution preEdit()
  {
    getLogger().debug("preEdit");
    if (id != null) {
      data = baseDao.getById(id);
      getLogger().debug("Getting data for edit: " + data);
      setShowHistory(true);
    } else {
      getLogger().debug("Preparing new object for adding.");
      data = createDataInstance();
    }
    if (getClass().getAnnotation(BaseEditAction.class).flowSope() == true) {
      storeFlowScope();
    }
    onPreEdit();
    return getInputPage();
  }

  @DefaultHandler
  @DontValidate
  public Resolution execute()
  {
    Resolution resolution = onExecute();
    if (resolution != null) {
      return resolution;
    }
    resolution = handleSelectEvents(this, getInputPage());
    if (resolution != null) {
      return resolution;
    }
    FlowScope scope = getFlowScope(false);
    if (scope != null) {
      restoreFromFlowScope(scope);
      processSelection(this);
    } else {
      getLogger().info("Oups, caller of default handler? Maybe forgetten annotation for the property of BaseEditActionBean.");
    }
    return getInputPage();
  }

  @SuppressWarnings("unchecked")
  protected void restoreFromFlowScope(FlowScope scope)
  {
    data = (O) scope.get(getFlowDataKey());
    getLogger().debug("dataObject restored in execute: " + data);
    for (Method method : getClass().getMethods()) {
      if (method.isAnnotationPresent(Scope.class) == false || method.getAnnotation(Scope.class).flow() == false) {
        continue;
      }
      String prop = "property_" + BeanHelper.determinePropertyName(method);
      // Storing the property to the flow scope:
      Object obj = scope.get(prop);
      if (getLogger().isDebugEnabled() == true) {
        getLogger().debug("Restoring property key '" + prop + "' from flow scope: " + obj);
      }
      // Restoring the property to the flow scope:
      BeanHelper.invokeSetter(this, method, obj);
    }
  }

  /**
   * Store the current form to a new or already existing flow scope. This is done e. g. if the user has clicked a browse button for
   * selecting a date, task etc.
   * @see org.projectforge.web.core.ExtendedActionBean#storeToFlowScope()
   */
  @Override
  protected void storeToFlowScope()
  {
    if (getLogger().isDebugEnabled() == true) {
      getLogger().debug("Storing data under key '" + getFlowDataKey() + "' in flow scope: " + getData());
    }
    storeFlowScopeObject(getFlowDataKey(), getData());
    for (Method method : getClass().getMethods()) {
      if (method.isAnnotationPresent(Scope.class) == false || method.getAnnotation(Scope.class).flow() == false) {
        continue;
      }
      String prop = "property_" + BeanHelper.determinePropertyName(method);
      // Storing the property to the flow scope:
      if (getLogger().isDebugEnabled() == true) {
        getLogger().debug("Storing property key '" + prop + "' in flow scope: " + BeanHelper.invoke(this, method));
      }
      storeFlowScopeObject(prop, BeanHelper.invoke(this, method));
    }
  }

  protected Resolution getInputPage()
  {
    return new ForwardResolution(getJspUrl());
  }

  /**
   * Redirects to the action with preEdit event of current data object.
   */
  protected Resolution getRedirectPreEditAction()
  {
    return new RedirectResolution(getActionUrl()).addParameter("id", getData().getId()).addParameter("preEdit", "");
  }

  /**
   * User has clicked the save button for storing a new item.
   * @return
   */
  public Resolution create()
  {
    getLogger().debug("create: " + data);
    if (getClass().getAnnotation(BaseEditAction.class).flowSope() == true) {
      restoreFlowScope();
    }
    Resolution resolution = onSaveOrUpdate();
    if (resolution != null) {
      return resolution;
    }
    validate();
    if (hasErrors() == true) {
      return getContext().getSourcePageResolution();
    }
    baseDao.save(data);
    resolution = afterSaveOrUpdate();
    if (resolution != null) {
      closeFlowScope();
      return resolution;
    }
    resolution = afterSave();
    if (resolution != null) {
      closeFlowScope();
      return resolution;
    }
    closeFlowScope();
    return getForwardResolution();
  }

  /**
   * User has clicked the update button for updating an existing item.
   * @return
   */
  public Resolution update()
  {
    getLogger().debug("update: " + data);
    if (getClass().getAnnotation(BaseEditAction.class).flowSope() == true) {
      restoreFlowScope();
    }
    Resolution resolution = onSaveOrUpdate();
    if (resolution != null) {
      return resolution;
    }
    validate();
    if (hasErrors() == true) {
      return getContext().getSourcePageResolution();
    }
    boolean modified = baseDao.update(data);
    resolution = afterSaveOrUpdate();
    if (resolution != null) {
      closeFlowScope();
      return resolution;
    }
    resolution = afterUpdate(modified);
    if (resolution != null) {
      closeFlowScope();
      return resolution;
    }
    closeFlowScope();
    return getForwardResolution();
  }

  /**
   * User has clicked the cancel button, so forward the user to the list view.
   * @return
   */
  @DontValidate
  public Resolution cancel()
  {
    getLogger().debug("cancel: " + data);
    closeFlowScope();
    return getForwardResolution();
  }

  /** Overload this method for resetting object. */
  @DontValidate
  public Resolution reset()
  {
    getLogger().debug("reset: " + data);
    data = createDataInstance();
    return getInputPage();
  }

  @DontValidate
  public Resolution markAsDeleted()
  {
    getLogger().debug("Mark object as deleted: " + data);
    Resolution resolution = onDelete();
    if (resolution != null) {
      return resolution;
    }
    if (hasErrors() == true) {
      return getContext().getSourcePageResolution();
    }
    baseDao.markAsDeleted(data);
    closeFlowScope();
    return getForwardResolution();
  }

  @DontValidate
  public Resolution undelete()
  {
    getLogger().debug("Undelete object: " + data);
    Resolution resolution = onUndelete();
    if (resolution != null) {
      return resolution;
    }
    validate();
    if (hasErrors() == true) {
      return getContext().getSourcePageResolution();
    }
    baseDao.undelete(data);
    closeFlowScope();
    return getForwardResolution();
  }

  protected ForwardResolution getForwardResolution()
  {
    return new ForwardResolution(getListActionClass());
  }

  protected Class< ? extends ActionBean> getListActionClass()
  {
    return getClass().getAnnotation(BaseEditAction.class).listAction();
  }

  /**
   * Must be implemented if your class is annotated with BaseEditAction.flowScope.
   */
  protected void storeFlowScope()
  {
    throw new UnsupportedOperationException();
  }

  /**
   * Must be implemented if your class is annotated with BaseEditAction.flowScope.
   */
  protected FlowScope restoreFlowScope()
  {
    throw new UnsupportedOperationException();
  }
}
