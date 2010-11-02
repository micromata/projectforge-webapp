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
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.RedirectResolution;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.UrlBinding;
import net.sourceforge.stripes.validation.Validate;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.projectforge.calendar.DayHolder;
import org.projectforge.common.BeanHelper;
import org.projectforge.common.DateHelper;
import org.projectforge.common.LabelValueBean;
import org.projectforge.common.NumberHelper;
import org.projectforge.common.StringHelper;
import org.projectforge.core.UserException;
import org.projectforge.user.PFUserContext;
import org.projectforge.web.calendar.SelectDate;
import org.projectforge.web.calendar.SelectDateAction;
import org.projectforge.web.calendar.SelectDateActionSettings;
import org.projectforge.web.user.UserListAction;


/**
 * Extends the BaseActionBean. Additional features are the flow scope and the handling of browsing/selecting sub data.
 */
public abstract class ExtendedActionBean extends BaseActionBean
{
  protected Object preselectedValue;

  /** If in selection mode, this property holds the preselected value (item) of the caller, if exists. */
  public Object getPreselectedValue()
  {
    if (preselectedValue == null) {
      if (getFlowScope(false) != null) {
        this.preselectedValue = getFlowScope(false).get("preselectedValue"); // Put in scope in ExtendedActionBean#processEventObject
        // obj, String event)
      }
    }
    return preselectedValue;
  }

  public void clearPreselectedValue()
  {
    if (getFlowScope(false) != null) {
      this.preselectedValue = getFlowScope(false).remove("preselectedValue");
    }
  }

  /**
   * Gets the standard forward resolution for selecting tasks. This should be used by calling action beans.
   * 
   * @param scope
   * @param callerUrl
   * @param paramKey The key under which the task id will be returned to the caller.
   */
  @SuppressWarnings("unchecked")
  public static void addCallerUrl(FlowScope scope, Class< ? extends BaseListActionBean> clazz, String callerUrl, String paramKey)
  {
    scope.put(getKey4CallerUrl(clazz), callerUrl);
    scope.put(getKey4CallerKey(clazz), paramKey);
  }

  protected static String getKey4CallerUrl(Class< ? > clazz)
  {
    return clazz.getName() + "__callerUrl";
  }

  protected static String getKey4CallerKey(Class< ? > clazz)
  {
    return clazz.getName() + "__callerKey";
  }

  /** This class uses the logger of the extended class. */
  protected abstract Logger getLogger();

  private FlowScope flowScope;

  private String flowKey;

  protected String getFlowDataKey()
  {
    return this.getClass() + ":data";
  }

  protected String getFlowKey(String name)
  {
    return this.getClass() + ":" + name;
  }

  /**
   * Stores the given object in the flow scope under the given key.
   * @param key The key to store the object under.
   * @param obj The object to store in the flash scope.
   */
  protected FlowScope storeFlowScopeObject(String key, Object obj)
  {
    getFlowScope(true);
    flowScope.put(key, obj);
    getContext().getRequest().setAttribute(FlowScope.FLOW_SCOPE_ID, flowScope.getKey());
    return flowScope;
  }

  /**
   * Use able for date picker from combo box.
   * @param days Number of days from today to future (positive value) or number of days past to today (negative values).
   * @return
   */
  public List<LabelValueBean<String, Long>> getDateList(int days)
  {
    List<LabelValueBean<String, Long>> list = new ArrayList<LabelValueBean<String, Long>>();
    list.add(new LabelValueBean<String, Long>(getLocalizedString("pleaseChoose"), (long) -1));
    DateFormat df = new SimpleDateFormat(PFUserContext.getResourceBundle().getString("longDateFormat"), getContext().getLocale());
    if (days < 0) {
      for (int i = 0; i > days; i--) {
        DayHolder day = new DayHolder();
        day.add(Calendar.DAY_OF_YEAR, i);
        list.add(new LabelValueBean<String, Long>(df.format(day.getSQLDate()), day.getSQLDate().getTime()));
      }
    } else {
      for (int i = 0; i < days; i++) {
        DayHolder day = new DayHolder();
        day.add(Calendar.DAY_OF_YEAR, i);
        list.add(new LabelValueBean<String, Long>(df.format(day.getSQLDate()), day.getSQLDate().getTime()));
      }
    }
    return list;
  }

  /**
   * Gets the object of the flow scope by key.
   * @return The flash scope object or null if not exists.
   */
  protected Object getFlowScopeObject(String key, boolean create)
  {
    getFlowScope(true);
    return flowScope.get(key);
  }

  /**
   * Gets the current flow scope. Throws a RuntimeException if not exists.
   * @return
   */
  protected FlowScope getRequiredFlowScope()
  {
    if (getFlowScope(false) == null) {
      throw new UserException(UserException.I18N_KEY_FLOWSCOPE_NOT_EXIST);
    }
    return this.flowScope;
  }

  /**
   * Gets the current flow scope of this bean.
   * @param create If true, a new flow scope will be created if not exists.
   * @return The current flow scope or null, if not exists and created = false.
   */
  protected FlowScope getFlowScope(boolean create)
  {
    if (this.flowScope != null) {
      return this.flowScope;
    }
    this.flowScope = FlowScope.getCurrent(getContext().getRequest(), this, create);
    return this.flowScope;
  }

  /**
   * Removes the flow scope from the user session.
   */
  protected void closeFlowScope()
  {
    if (flowScope != null) {
      flowScope.closeFlowScope(getContext().getRequest());
    }
  }

  /**
   * Appends url parameter for reusing by the next request.
   * @param resolution
   * @return
   */
  protected RedirectResolution getFlowResolution(RedirectResolution resolution)
  {
    FlowScope scope = getFlowScope(false);
    if (scope != null) {
      return resolution.addParameter(FlowScope.FLOW_SCOPE_ID, scope.getKey());
    }
    return resolution;
  }

  /**
   * Appends url parameter for reusing by the next request.
   * @param resolution
   * @return
   */
  protected ForwardResolution getFlowResolution(ForwardResolution resolution)
  {
    FlowScope scope = getFlowScope(false);
    if (scope != null) {
      return resolution.addParameter(FlowScope.FLOW_SCOPE_ID, scope.getKey());
    }
    return resolution;
  }

  /**
   * For storing the flow key in the html form.
   * @return
   */
  @Validate
  public String getFlowKey()
  {
    return flowKey;
  }

  public void setFlowKey(String flowKey)
  {
    this.flowKey = flowKey;
  }

  void setFlowScope(FlowScope scope)
  {
    this.flowScope = scope;
    this.flowKey = scope.getKey();
  }

  protected Resolution handleSelectEvents(Object obj, Resolution inputPage)
  {
    if (StringHelper.startsWith(getEventKey(), "select.") == true) {
      // User has chosen to select/browse a value (e. g. task, date or user)
      Resolution resolution = processEvent(obj, getEventKey().substring(7));
      if (resolution != null) {
        return resolution;
      }
    } else if (StringHelper.startsWith(getEventKey(), "selectDefault.") == true) {
      // User has clicked an event button for selecting default value.
      processSelectDefaultEvent(obj, getEventKey().substring(14));
      eventKey = ""; // Prevent twice handling of this device:
      return inputPage;
    } else if (StringHelper.startsWith(getEventKey(), "unselect.") == true) {
      // User has clicked an unselect button (e. g. unselect task, unselect date or unselect user).
      processUnselectEvent(obj, getEventKey().substring(9));
      eventKey = ""; // Prevent twice handling of this device:
      return inputPage;
    }
    return null;
  }

  /**
   * The user has selected some item (e. g. task from SelectTask) and has returned to the main edit page. We have to determine the
   * selection. Determines all select annotations of this class.
   */
  protected void processSelection(Object obj)
  {
    for (Method method : obj.getClass().getMethods()) {
      if (method.isAnnotationPresent(Select.class) == true || method.isAnnotationPresent(SelectDate.class) == true) {
        if (getLogger().isDebugEnabled() == true) {
          getLogger().debug("Processing user selection '" + BeanHelper.determinePropertyName(method) + "'");
        }
        String param = BeanHelper.determinePropertyName(method);
        String reqValue = getContext().getRequestParameter(param);
        if (reqValue != null && reqValue.length() > 0) {
          Object value = setField(obj, method, reqValue);
          if (value != null) {
            selectedEvent(method, param, value);
          }
        }
      }
    }
  }

  private Object setField(Object obj, Method method, String reqValue)
  {
    Object value = null;
    Class< ? > type = BeanHelper.determinePropertyType(method);
    if (StringUtils.isNotEmpty(reqValue) == true) {
      if (type == Integer.class) {
        value = NumberHelper.parseInteger(reqValue);
      } else if (type == Date.class) {
        value = DateHelper.parseMillis(reqValue);
        /*
         * } else if (type == DateInputField.class) { Date date = DayHolder.parseIsoDate(reqValue); value = new DateInputField(date,
         * DatePrecision.DAY, getContext().getLocale());
         */
      } else {
        throw new UnsupportedOperationException("Unsupported type in processSelect: " + type.getName());
      }
      if (value != null) {
        BeanHelper.invokeSetter(obj, method, value);
      }
    } else {
      BeanHelper.invokeSetter(obj, method, null);
    }
    return value;
  }

  protected void selectedEvent(Method method, String param, Object value)
  {
    // Do nothing.
  }

  /**
   * User has chosen select / browse button. Determines all select annotations of the given object.
   * @param event
   * @param eventType SELECT or UNSELECT
   * @return
   */
  protected Resolution processEvent(Object obj, String event)
  {
    try {
      for (Method method : obj.getClass().getMethods()) {
        String param = BeanHelper.determinePropertyName(method);
        if (event.equals(param) == false) {
          continue;
        }
        if (method.isAnnotationPresent(Select.class)) {
          // Property in ActionBean is annotated, so the property should be select-able via the given selectAction (TaskTreeAction,
          // SelectDateAction etc.)
          preselectedValue = BeanHelper.invoke(obj, method);
          Select annotation = method.getAnnotation(Select.class);
          processEvent(method, event, param);
          addCallerUrl(getFlowScope(false), annotation.selectAction(), getActionUrl(), param);
          return getFlowResolution(new RedirectResolution(annotation.selectAction()));
        } else if (method.isAnnotationPresent(SelectDate.class)) {
          preselectedValue = BeanHelper.invoke(obj, method);
          SelectDateActionSettings settings = new SelectDateActionSettings();
          processEvent(method, event, param);
          addCallerUrl(getFlowScope(false), SelectDateAction.class, getActionUrl(), param);
          String periodStart = method.getAnnotation(SelectDate.class).periodStart();
          if (periodStart != null && periodStart.length() > 0) {
            settings.setPeriodStart(periodStart);
          }
          String periodStop = method.getAnnotation(SelectDate.class).periodStop();
          if (periodStop != null && periodStop.length() > 0) {
            settings.setPeriodStop(periodStop);
          }
          if (method.getAnnotation(SelectDate.class).showTimesheets() == true) {
            settings.setShowTimesheets(true);
          }
          settings.setCurrent((Date) BeanHelper.invoke(obj, method));
          getFlowScope(false).put(SelectDateAction.SETTINGS_KEY, settings);
          return getFlowResolution(new RedirectResolution(SelectDateAction.class));
        }
      }
    } finally {
      // Used by BaseListAction#processSelectMode():
      if (getFlowScope(false) != null) {
        getFlowScope(false).put("preselectedValue", preselectedValue);
      }
    }
    return null;
  }

  /**
   * The user has choosen an item to unselect (e. g. task or user), so the property is set to null.
   * @param obj
   * @param event
   */
  protected void processUnselectEvent(Object obj, String event)
  {
    for (Method method : obj.getClass().getMethods()) {
      String param = BeanHelper.determinePropertyName(method);
      if (event.equals(param) == false) {
        continue;
      }
      if (method.isAnnotationPresent(Select.class) == true || method.isAnnotationPresent(SelectDate.class) == true) {
        setField(obj, method, null);
      }
    }
  }

  /**
   * The user has chosen an item to select the default value (e. g. select me as user), so the property is set to the default.
   * @param obj
   * @param event
   */
  protected void processSelectDefaultEvent(Object obj, String event)
  {
    for (Method method : obj.getClass().getMethods()) {
      String param = BeanHelper.determinePropertyName(method);
      if (event.equals(param) == false) {
        continue;
      }
      if (method.isAnnotationPresent(Select.class) == true) {
        Select annotation = method.getAnnotation(Select.class);
        if (annotation.selectAction().equals(UserListAction.class) == true) {
          // Select me was chosen, so set the current logged in user (context user).
          setField(obj, method, PFUserContext.getUser().getId().toString());
        } else {
          throw new UnsupportedOperationException("Select default not supported for method " + method + " and event " + event + ".");
        }
      }
    }
  }

  /**
   * Debugs the event and stores in the flow scope.
   * @param method Only for debug purposes.
   * @param event Only for debug purposes.
   * @param param Only for debug purposes.
   */
  private void processEvent(Method method, String event, String param)
  {
    if (getLogger().isDebugEnabled() == true) {
      getLogger().debug("processing '" + BeanHelper.determinePropertyName(method) + "'");
    }
    try {
      storeToFlowScope();
    } catch (Throwable ex) {
      getLogger().error(ex.getMessage(), ex);
      throw new RuntimeException(ex);
    }
  }

  /**
   * @see BaseActionBeanContext#putEntry(String, Object, boolean)
   */
  public void putEntry(String key, Object value, boolean persistent)
  {
    getContext().putEntry(key, value, persistent);
  }

  /**
   * @see BaseActionBeanContext#getEntry(String)
   */
  public Object getEntry(String key)
  {
    return getContext().getEntry(key);
  }

  protected abstract void storeToFlowScope();

  /**
   * The action url is needed inside flows (e. g. for selecting task, date etc.)
   * @return
   */
  protected String getActionUrl()
  {
    return getClass().getAnnotation(UrlBinding.class).value();
  }
}
