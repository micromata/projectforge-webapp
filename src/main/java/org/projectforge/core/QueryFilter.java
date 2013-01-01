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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import org.apache.commons.lang.StringUtils;
import org.hibernate.Criteria;
import org.hibernate.FetchMode;
import org.hibernate.Session;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Expression;
import org.hibernate.criterion.Order;
import org.projectforge.common.DateHelper;


/**
 * Stores the expressions and settings for creating a hibernate criteria object. This template is useful for avoiding the need of a
 * hibernate session in the stripes action classes.
 * @author Kai Reinhard (k.reinhard@micromata.de)
 */
public class QueryFilter
{
  private List<Object> filterSettings = new ArrayList<Object>();

  private int maxResults = -1;

  private String name;

  private String alias;

  private BaseSearchFilter filter;

  private FetchMode fetchMode;

  private String associationPath = null;

  private Locale locale;

  /**
   * Creates new QueryFilter with a new SearchFilter as filter.
   */
  public QueryFilter()
  {
    this.filter = new BaseSearchFilter();
  }

  public QueryFilter(BaseSearchFilter filter)
  {
    this.filter = filter;
  }

  private QueryFilter(String name)
  {
    this.name = name;
  }

  public String getName()
  {
    return name;
  }

  public String getAlias()
  {
    return alias;
  }

  public BaseSearchFilter getFilter()
  {
    return filter;
  }

  /**
   * Locale is needed for lucene stemmers (hibernate search).
   * @return
   */
  public Locale getLocale()
  {
    if (locale == null) {
      return Locale.GERMAN;
    }
    return locale;
  }

  public void setLocale(Locale locale)
  {
    this.locale = locale;
  }

  /**
   * If an error occured (e. g. lucene parse exception) this message will be returned.
   * @return
   */
  public String getErrorMessage()
  {
    return filter.getErrorMessage();
  }

  public void setErrorMessage(String errorMessage)
  {
    filter.setErrorMessage(errorMessage);
  }

  public boolean hasErrorMessage()
  {
    return filter.hasErrorMessage();
  }

  public void clearErrorMessage()
  {
    filter.clearErrorMessage();
  }

  /**
   * @see org.hibernate.Criteria#add(Criterion)
   * @param criterion
   * @return
   */
  public QueryFilter add(Criterion criterion)
  {
    filterSettings.add(criterion);
    return this;
  }

  /**
   * @see org.hibernate.Criteria#addOrder(Order)
   * @param order
   * @return
   */
  public QueryFilter addOrder(Order order)
  {
    filterSettings.add(order);
    return this;
  }

  public void setFetchMode(String associationPath, FetchMode mode)
  {
    this.associationPath = associationPath;
    this.fetchMode = mode;
  }

  /**
   * Adds Expression.between for given time period.
   * @param dateField
   * @param year if <= 0 do nothing.
   * @param month if < 0 choose whole year, otherwise given month. (Calendar.MONTH);
   */
  public void setYearAndMonth(final String dateField, final int year, final int month)
  {
    if (year > 0) {
      Calendar cal = DateHelper.getUTCCalendar();
      cal.set(Calendar.YEAR, year);
      java.sql.Date lo = null;
      java.sql.Date hi = null;
      if (month >= 0) {
        cal.set(Calendar.MONTH, month);
        cal.set(Calendar.DAY_OF_MONTH, 1);
        lo = new java.sql.Date(cal.getTimeInMillis());
        int lastDayOfMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
        cal.set(Calendar.DAY_OF_MONTH, lastDayOfMonth);
        hi = new java.sql.Date(cal.getTimeInMillis());
      } else {
        cal.set(Calendar.DAY_OF_YEAR, 1);
        lo = new java.sql.Date(cal.getTimeInMillis());
        int lastDayOfYear = cal.getActualMaximum(Calendar.DAY_OF_YEAR);
        cal.set(Calendar.DAY_OF_YEAR, lastDayOfYear);
        hi = new java.sql.Date(cal.getTimeInMillis());
      }
      add(Expression.between(dateField, lo, hi));
    }
  }

  public Criteria buildCriteria(Session session, Class< ? > clazz)
  {
    Criteria criteria = session.createCriteria(clazz);
    buildCriteria(criteria);
    return criteria;
  }

  private void buildCriteria(Criteria criteria)
  {
    for (Object obj : filterSettings) {
      if (obj instanceof Criterion) {
        criteria.add((Criterion) obj);
      } else if (obj instanceof Order) {
        criteria.addOrder((Order) obj);
      } else if (obj instanceof Alias) {
        Alias alias = (Alias) obj;
        criteria.createAlias(alias.arg0, alias.arg1);
      } else if (obj instanceof QueryFilter) {
        QueryFilter filter = (QueryFilter) obj;
        Criteria subCriteria;
        if (StringUtils.isEmpty(filter.getAlias()) == true) {
          subCriteria = criteria.createCriteria(filter.getName());
        } else {
          subCriteria = criteria.createCriteria(filter.getName(), filter.getAlias());
        }
        filter.buildCriteria(subCriteria);
      }
    }
    if (associationPath != null) {
      criteria.setFetchMode(associationPath, fetchMode);
    }
    if (maxResults > 0) {
      criteria.setMaxResults(maxResults);
    }
  }

  /**
   * @see org.hibernate.Criteria#createAlias(String, String)
   */
  public QueryFilter createAlias(String arg0, String arg1)
  {
    filterSettings.add(new Alias(arg0, arg1));
    return this;
  }

  public QueryFilter createCriteria(String name)
  {
    QueryFilter filter = new QueryFilter(name);
    filterSettings.add(filter);
    return filter;
  }

  public QueryFilter createCriteria(String name, String alias)
  {
    QueryFilter filter = new QueryFilter(name);
    filter.alias = alias;
    filterSettings.add(filter);
    return filter;
  }

  /**
   * @see org.hibernate.Criteria#setMaxResults(int)
   * @param value
   * @return
   */
  public QueryFilter setMaxResults(int value)
  {
    this.maxResults = value;
    return this;
  }

  public int getMaxResults()
  {
    return maxResults;
  }

  class Alias
  {
    String arg0;

    String arg1;

    Alias(String arg0, String arg1)
    {
      this.arg0 = arg0;
      this.arg1 = arg1;
    }
  };
}
