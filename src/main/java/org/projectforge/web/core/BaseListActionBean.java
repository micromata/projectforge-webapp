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

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.DontValidate;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.RedirectResolution;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.UrlBinding;
import net.sourceforge.stripes.validation.Validate;
import net.sourceforge.stripes.validation.ValidateNestedProperties;

import org.apache.commons.lang.ClassUtils;
import org.apache.commons.lang.StringUtils;
import org.projectforge.common.LabelValueBean;
import org.projectforge.common.RecentQueue;
import org.projectforge.core.BaseDO;
import org.projectforge.core.BaseDao;
import org.projectforge.core.BaseSearchFilter;
import org.projectforge.core.UserException;
import org.projectforge.web.user.UserListAction;


/**
 * The convenient base class for edit actions.
 */
@SuppressWarnings("unchecked")
public abstract class BaseListActionBean<F extends BaseSearchFilter, D extends org.projectforge.core.BaseDao< ? >, O> extends
    ExtendedActionBean
{
  protected F actionFilter;

  protected D baseDao;

  protected Integer pageSize;

  protected boolean storeFilter = true;

  protected List<O> result;

  protected boolean refreshResultList = false;

  protected boolean storeRecentSearchTerms = false;

  protected RecentQueue<String> recentSearchTermsQueue;

  protected final String FLOW_FILTER_KEY = this.getClass().getName() + ":filter";

  /**
   * Needed for instantiating new data object.
   * @return
   */
  protected abstract F createFilterInstance();

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
   * Should the result list be shown instantly on calling the list page or only after click on search button? NOTE: If not overwritten by
   * sub class, always false will be returned!
   * @return True, if results should be shown ever the page is shown, false otherwise (then the result list will only be shown after click
   *         on search button).
   */
  protected boolean isShowResultInstantly()
  {
    return false;
  }

  /**
   * Quick select support.<br/> Only used for select mode. Override this, if you want to implement an own quick select meaning to select
   * automatically the single entry if the result list has only 1 entry (e. g. after typing searchstring).
   * @see UserListAction#getSingleEntryValue()
   * @return the id of the entry (pk).
   */
  protected String getSingleEntryValue()
  {
    List<O> list = getList();
    if (list != null && list.size() == 1) {
      return String.valueOf(((BaseDO) getList().get(0)).getId()); // return the pk.
    }
    return null;
  }

  public List<LabelValueBean<String, Integer>> getPageSizes()
  {
    List<LabelValueBean<String, Integer>> list = new ArrayList<LabelValueBean<String, Integer>>();
    NumberFormat nf = NumberFormat.getInstance(getContext().getLocale());
    list.add(new LabelValueBean<String, Integer>("25", 25));
    list.add(new LabelValueBean<String, Integer>("50", 50));
    list.add(new LabelValueBean<String, Integer>("100", 100));
    list.add(new LabelValueBean<String, Integer>("200", 200));
    list.add(new LabelValueBean<String, Integer>("500", 500));
    list.add(new LabelValueBean<String, Integer>(String.valueOf(nf.format(1000)), 1000));
    return list;
  }

  /**
   * For displaying the hibernate search fields. Returns list as csv. These fields the user can directly address in his search string, e. g.
   * street:marie.
   * @return
   * @see org.projectforge.core.BaseDao#getSearchFields()
   */
  public String getSearchFields()
  {
    StringBuffer buf = new StringBuffer();
    int counter = 0;
    for (String field : baseDao.getSearchFields()) {
      if (counter++ > 0) {
        buf.append(", ");
      }
      buf.append(field);
    }
    return buf.toString();
  }

  public String getSearchToolTip()
  {
    return getLocalizedMessage("search.string.info", getSearchFields());
  }

  @DefaultHandler
  @DontValidate
  public Resolution execute()
  {
    if (isShowResultInstantly() == true) {
      refreshResultList = true;
    }
    Resolution resolution = onExecute();
    if (resolution != null) {
      return resolution;
    }
    Resolution inputPage = new ForwardResolution(getJspUrl()); // Need this construction, because getInputPage calls getList, if
    // refreshResultList == true.
    resolution = handleSelectEvents(getActionFilter(), inputPage);
    if (resolution != null) {
      if (resolution.equals(inputPage) == true) {
        // Force rebuild of list:
        return getInputPage();
      }
      return resolution;
    }
    if (isSelectMode() == true) {
      return processSelectMode();
    } else {
      return processNormalMode();
    }
  }

  protected Resolution processNormalMode()
  {
    FlowScope scope = getFlowScope(false);
    if (scope != null) {
      actionFilter = (F) scope.get(FLOW_FILTER_KEY);
      if (actionFilter != null) {
        getLogger().debug("filter restored in execute: " + actionFilter);
      }
      scope.closeFlowScope(getContext().getRequest());
      processSelection(getActionFilter());
    } else {
      getLogger().debug("action");
    }
    Resolution resolution = afterExecute();
    if (resolution != null) {
      return resolution;
    }
    return getInputPage();
  }

  /**
   * Will be called by execute, if this page works in selection mode.
   * @return
   */
  protected Resolution processSelectMode()
  {
    FlowScope scope = getFlowScope(false);
    if ("select".equals(eventKey) == true || "cancel".equals(eventKey) == true) { // User has selected one entry or has cancelled the
      // selection.
      if (getLogger().isDebugEnabled() == true) {
        getLogger().debug("Processing eventKey='" + eventKey + "', selectedValue='" + selectedValue + "'");
      }
      if (scope != null) {
        return redirectToCaller(scope);
      }
      throw new UserException(UserException.I18N_KEY_FLOWSCOPE_NOT_EXIST);
    }
    Resolution resolution = afterExecute();
    if (resolution != null) {
      return resolution;
    }
    return getInputPage();
  }

  /**
   * Gets the caller url and caller param from the given scope and redirects to the caller url.
   * @param scope
   * @return
   */
  protected Resolution redirectToCaller(FlowScope scope)
  {
    String callerUrl = (String) scope.get(getKey4CallerUrl(getClass()));
    String callerParam = (String) scope.get(getKey4CallerKey(getClass()));
    // redirect to the caller with the result of the selected value.
    if (getSelectedValue() != null) {
      return getFlowResolution(new RedirectResolution(callerUrl)).addParameter(callerParam, getSelectedValue());
    } else {
      return getFlowResolution(new RedirectResolution(callerUrl));
    }
  }

  protected Resolution afterExecute()
  {
    return null;
  }

  public Resolution search()
  {
    getLogger().debug("search");
    refreshResultList = true;
    if (isSelectMode() == true) {
      FlowScope scope = getFlowScope(false);
      if (scope != null) {
        String str = getSingleEntryValue();
        if (str != null) {
          setSelectedValue(str);
          // Quick select because result list is reduced to one single entry:
          getLogger().debug("Quick selection.");
          return redirectToCaller(scope);
        }
      }
    }
    return getInputPage();
  }

  public List<O> getList()
  {
    if (refreshResultList == true) {
      result = buildList();
      refreshResultList = false;
    }
    return result;
  }

  protected List<O> buildList()
  {
    List<O> list = (List<O>) baseDao.getList((BaseSearchFilter) getActionFilter());
    if (isStoreRecentSearchTerms()) {
      addRecentSearchTerm(actionFilter);
    }
    return list;
  }

  /**
   * Adds the search string to the recent list, if filter is from type BaseSearchFilter and the search string is not blank and not from type
   * id:4711.
   * @param Filter The search filter.
   */
  protected void addRecentSearchTerm(F Filter)
  {
    BaseSearchFilter filter = (BaseSearchFilter) getActionFilter();
    if (StringUtils.isNotBlank(filter.getSearchString()) == true) {
      String s = filter.getSearchString();
      if (s.startsWith("id:") == false || StringUtils.isNumeric(s.substring(3)) == false) {
        // OK, search string is not from type id:4711
        getRecentSearchTermsQueue().append(s);
      }
    }
  }

  @DontValidate
  public Resolution reset()
  {
    getLogger().debug("reset");
    actionFilter.reset();
    return getInputPage();
  }

  /**
   * The BaseDao modifies the search string. This methods gets the modified search string for displaying.
   * @return
   * @see BaseDao#modifySearchString(String)
   */
  public String getLuceneSearchString()
  {
    if (getActionFilter() instanceof BaseSearchFilter == true) {
      return BaseDao.modifySearchString(((BaseSearchFilter) actionFilter).getSearchString());
    } else return "";
  }

  /**
   * If field storeFilter is true (default) then the stored filter will be stored and overwritten.
   * @return
   */
  @ValidateNestedProperties( { @Validate(field = "searchString")})
  public F getActionFilter()
  {
    if (actionFilter == null) {
      if (isStoreFilter() == true) {
        Object filter = getContext().getEntry(this.getClass().getName() + ":Filter");
        if (filter != null) {
          F dummy = createFilterInstance();
          if (ClassUtils.isAssignable(filter.getClass(), dummy.getClass()) == true) {
            actionFilter = (F) filter;
          } else {
            // Probably a new software release results in an incompability of old and new filter format.
            getLogger().info(
                "Could not restore filter from user prefs: (old) filter type "
                    + filter.getClass().getName()
                    + " is not assignable to (new) filter type "
                    + dummy.getClass().getName()
                    + " (OK, probably new software release).");
          }
        }
      }
    }
    if (actionFilter == null) {
      actionFilter = createFilterInstance();
      actionFilter.reset();
      if (isStoreFilter() == true) {
        getContext().putEntry(this.getClass().getName() + ":Filter", actionFilter, true);
      }
    }
    return actionFilter;
  }

  protected RecentQueue<String> getRecentSearchTermsQueue()
  {
    if (recentSearchTermsQueue == null) {
      recentSearchTermsQueue = (RecentQueue<String>) getContext().getEntry(this.getClass().getName() + ":recentSearchTerms");
    }
    if (recentSearchTermsQueue == null) {
      recentSearchTermsQueue = new RecentQueue<String>();
      if (isStoreRecentSearchTerms() == true) {
        getContext().putEntry(this.getClass().getName() + ":recentSearchTerms", recentSearchTermsQueue, true);
      }
    }
    return recentSearchTermsQueue;
  }

  /**
   * Gets the recent search strings as Json object.
   */
  public String getRecentSearchTerms()
  {
    if (getRecentSearchTermsQueue().size() > 0) {
      return BaseDao.buildJsonRows(false, recentSearchTermsQueue.getRecents());
    }
    return "[]";
  }

  /**
   * In select mode, this page will only be used for selecting a single entry for calling pages.
   */
  public boolean isSelectMode()
  {
    return getCallerUrl() != null;
  }

  /**
   * Returns the caller's url, if stored in the current flow scope.
   * @return
   */
  public String getCallerUrl()
  {
    if (getFlowScope(false) != null) {
      return (String) getFlowScopeObject(getKey4CallerUrl(getClass()), false);
    }
    return null;
  }

  /**
   * The page size of display tag (result table).
   */
  @Validate
  public Integer getPageSize()
  {
    if (pageSize == null) {
      pageSize = (Integer) getContext().getEntry(this.getClass().getName() + ":pageSize");
    }
    if (pageSize == null) {
      pageSize = 50;
    }
    return pageSize;
  }

  public void setPageSize(Integer pageSize)
  {
    this.pageSize = pageSize;
    getContext().putEntry(this.getClass().getName() + ":pageSize", this.pageSize, true);
  }

  /**
   * If false then the action filter will not be stored (the previous stored filter will be preserved). true is default.
   */
  public boolean isStoreFilter()
  {
    return storeFilter;
  }

  public void setStoreFilter(boolean storeFilter)
  {
    this.storeFilter = storeFilter;
  }

  /**
   * If true then the search terms will be stored in a recent queue.
   * @return
   */
  public boolean isStoreRecentSearchTerms()
  {
    return storeRecentSearchTerms;
  }

  public void setStoreRecentSearchTerms(boolean storeRecentSearchTerms)
  {
    this.storeRecentSearchTerms = storeRecentSearchTerms;
  }

  /**
   * The action url is needed inside flows (e. g. for selecting task, date etc.)
   * @return
   */
  protected String getActionUrl()
  {
    return getClass().getAnnotation(UrlBinding.class).value();
  }

  protected Resolution getInputPage()
  {
    if (refreshResultList == true) {
      // Get the list for checking any errors:
      getList();
      BaseSearchFilter filter = (BaseSearchFilter) getActionFilter();
      if (filter != null && filter.hasErrorMessage() == true) {
        addError("actionFilter.searchString", "error", ((BaseSearchFilter) getActionFilter()).getErrorMessage());
        filter.clearErrorMessage();
      }
    }
    BaseListAction ann = getClass().getAnnotation(BaseListAction.class);
    if (ann != null && ann.flowSope() == true) {
      return getFlowResolution(new ForwardResolution(getJspUrl()));
    }
    return new ForwardResolution(getJspUrl());
  }

  /**
   * @see org.projectforge.web.core.ExtendedActionBean#storeToFlowScope()
   */
  @Override
  protected void storeToFlowScope()
  {
    if (getLogger().isDebugEnabled() == true) {
      getLogger().debug("Storing filter under key '" + FLOW_FILTER_KEY + "' in flow scope: " + actionFilter);
    }
    storeFlowScopeObject(FLOW_FILTER_KEY, getActionFilter());
  }
}
