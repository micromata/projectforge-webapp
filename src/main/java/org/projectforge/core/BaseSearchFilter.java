/////////////////////////////////////////////////////////////////////////////
//
// Project ProjectForge Community Edition
//         www.projectforge.org
//
// Copyright (C) 2001-2013 Kai Reinhard (k.reinhard@micromata.de)
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

package org.projectforge.core;

import java.io.Serializable;
import java.util.Date;

import org.apache.commons.lang.StringUtils;

/**
 * Base search filter supported by the DAO's for filtering the result lists. The search filter will be translated via QueryFilter into
 * hibernate query criterias.
 * 
 * @author Kai Reinhard (k.reinhard@micromata.de)
 */
public class BaseSearchFilter implements Serializable
{
  private static final long serialVersionUID = 5970378227395811426L;

  protected String searchString;

  protected boolean deleted = false; // Initialization unnecessary but for documentation.

  protected boolean ignoreDeleted = false; // Initialization unnecessary but for documentation.

  protected int maxRows = -1;

  protected boolean useModificationFilter;

  protected Integer modifiedByUserId;

  @Deprecated
  protected Date startTimeOfLastModification;

  @Deprecated
  protected Date stopTimeOfLastModification;

  protected Date startTimeOfModification;

  protected Date stopTimeOfModification;

  private boolean searchHistory;

  private String errorMessage;

  private transient String[] searchFields;

  public BaseSearchFilter()
  {
  }

  public BaseSearchFilter(final BaseSearchFilter filter)
  {
    if (filter == null) {
      return;
    }
    copyBaseSearchFieldsFrom(filter);
  }

  public void copyBaseSearchFieldsFrom(final BaseSearchFilter filter)
  {
    this.searchString = filter.searchString;
    this.deleted = filter.deleted;
    this.ignoreDeleted = filter.ignoreDeleted;
    this.maxRows = filter.maxRows;
    this.useModificationFilter = filter.useModificationFilter;
    this.modifiedByUserId = filter.modifiedByUserId;
    this.startTimeOfModification = filter.startTimeOfModification;
    this.stopTimeOfModification = filter.stopTimeOfModification;
    this.searchHistory = filter.searchHistory;
  }

  public void reset()
  {
    deleted = false;
    ignoreDeleted = false;
    searchString = "";
    searchHistory = false;
  }

  public boolean isSearchNotEmpty()
  {
    return StringUtils.isNotEmpty(searchString);
  }

  public String getSearchString()
  {
    return searchString;
  }

  public void setSearchString(final String searchString)
  {
    this.searchString = searchString;
  }

  public void setSearchFields(final String... searchFields)
  {
    this.searchFields = searchFields;
  }

  /**
   * If not null and a query string for a full text index search is given, then only the given search fields are used instead of the default
   * search fields of the dao.
   * @return
   */
  public String[] getSearchFields()
  {
    return searchFields;
  }

  /**
   * If true then modifiedByUser and time of last modification is used for filtering.
   * @return
   */
  public boolean isUseModificationFilter()
  {
    return useModificationFilter;
  }

  public void setUseModificationFilter(final boolean useModificationFilter)
  {
    this.useModificationFilter = useModificationFilter;
  }

  public Integer getModifiedByUserId()
  {
    return modifiedByUserId;
  }

  public void setModifiedByUserId(final Integer modifiedByUserId)
  {
    this.modifiedByUserId = modifiedByUserId;
  }

  public Date getStartTimeOfModification()
  {
    return startTimeOfModification;
  }

  public void setStartTimeOfModification(final Date startTimeOfModification)
  {
    this.startTimeOfModification = startTimeOfModification;
  }

  public Date getStopTimeOfModification()
  {
    return stopTimeOfModification;
  }

  public void setStopTimeOfModification(final Date stopTimeOfModification)
  {
    this.stopTimeOfModification = stopTimeOfModification;
  }

  /**
   * If true the history entries are included in the search.
   * @return the searchHistory
   */
  public boolean isSearchHistory()
  {
    return searchHistory;
  }

  /**
   * @param searchHistory the searchHistory to set
   * @return this for chaining.
   */
  public void setSearchHistory(final boolean searchHistory)
  {
    this.searchHistory = searchHistory;
  }

  /**
   * If true, deleted and undeleted objects will be shown.
   */
  public boolean isIgnoreDeleted()
  {
    return ignoreDeleted;
  }

  public void setIgnoreDeleted(final boolean ignoreDeleted)
  {
    this.ignoreDeleted = ignoreDeleted;
  }

  /**
   * If not ignored, only deleted/undeleted object will be shown.
   */
  public boolean isDeleted()
  {
    return deleted;
  }

  public void setDeleted(final boolean deleted)
  {
    this.deleted = deleted;
  }

  /**
   * Maximum number of rows in the result list.
   * @return
   */
  public int getMaxRows()
  {
    return maxRows;
  }

  public void setMaxRows(final int maxRows)
  {
    this.maxRows = maxRows;
  }

  /**
   * If an error occured (e. g. lucene parse exception) this message will be returned.
   * @return
   */
  public String getErrorMessage()
  {
    return errorMessage;
  }

  public void setErrorMessage(final String errorMessage)
  {
    this.errorMessage = errorMessage;
  }

  public boolean hasErrorMessage()
  {
    return StringUtils.isNotEmpty(errorMessage);
  }

  public void clearErrorMessage()
  {
    this.errorMessage = null;
  }
}
