/////////////////////////////////////////////////////////////////////////////
//
// Project ProjectForge Community Edition
//         www.projectforge.org
//
// Copyright (C) 2001-2011 Kai Reinhard (k.reinhard@me.com)
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
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.persistence.Id;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.PredicateUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.ClassUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.apache.log4j.Logger;
import org.apache.lucene.queryParser.MultiFieldQueryParser;
import org.apache.lucene.util.Version;
import org.hibernate.CacheMode;
import org.hibernate.Criteria;
import org.hibernate.FlushMode;
import org.hibernate.HibernateException;
import org.hibernate.LockMode;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.search.FullTextQuery;
import org.hibernate.search.FullTextSession;
import org.hibernate.search.Search;
import org.hibernate.search.annotations.DocumentId;
import org.projectforge.access.AccessChecker;
import org.projectforge.access.AccessException;
import org.projectforge.access.OperationType;
import org.projectforge.common.BeanHelper;
import org.projectforge.common.DateHolder;
import org.projectforge.database.DatabaseDao;
import org.projectforge.lucene.PFAnalyzer;
import org.projectforge.user.PFUserContext;
import org.projectforge.user.PFUserDO;
import org.projectforge.user.UserGroupCache;
import org.projectforge.user.UserRightId;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import de.micromata.hibernate.history.Historizable;
import de.micromata.hibernate.history.HistoryAdapter;
import de.micromata.hibernate.history.HistoryEntry;
import de.micromata.hibernate.history.HistoryUserRetriever;
import de.micromata.hibernate.history.delta.PropertyDelta;

/**
 * 
 * @author Kai Reinhard (k.reinhard@micromata.de)
 * 
 */
@Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
public abstract class BaseDao<O extends ExtendedBaseDO< ? extends Serializable>> extends HibernateDaoSupport implements IDao<O>
{
  public static final String EXCEPTION_HISTORIZABLE_NOTDELETABLE = "Could not delete of Historizable objects (contact your software developer): ";

  /**
   * Use the latest Lucene 2.9 version at default.
   * @see Version#LUCENE_29
   */
  public static final Version LUCENE_VERSION = Version.LUCENE_29;

  /**
   * Maximum allowed mass updates within one massUpdate call.
   */
  public static final int MAX_MASS_UPDATE = 100;

  public static final String MAX_MASS_UPDATE_EXCEEDED_EXCEPTION_I18N = "massUpdate.error.maximumNumberOfAllowedMassUpdatesExceeded";

  private static final List<DisplayHistoryEntry> EMPTY_HISTORY_ENTRIES = new ArrayList<DisplayHistoryEntry>();

  private static final Logger log = Logger.getLogger(BaseDao.class);

  private static final String[] luceneReservedWords = { "AND", "OR", "NOT"};

  /**
   * Additional allowed characters (not at first position) for search string modification with wildcards. Do not forget to update
   * I18nResources.properties and the user documentation after any changes. <br/>
   * ALLOWED_CHARS =
   * @._-+*
   */
  private static final String ALLOWED_CHARS = "@._-+*";

  /**
   * Additional allowed characters (at first position) for search string modification with wildcards. Do not forget to update
   * I18nResources.properties and the user documentation after any changes. <br/>
   * ALLOWED_BEGINNING_CHARS =
   * @._
   */
  private static final String ALLOWED_BEGINNING_CHARS = "@._*";

  /**
   * If the search string containts any of this escape chars, no string modification will be done.
   */
  private static final String ESCAPE_CHARS = "+-";

  protected Class<O> clazz;

  protected AccessChecker accessChecker;

  protected DatabaseDao databaseDao;

  protected UserGroupCache userGroupCache;

  protected HistoryAdapter historyAdapter;

  protected TransactionTemplate txTemplate;

  protected String[] searchFields;

  protected BaseDaoReindexRegistry baseDaoReindexRegistry = BaseDaoReindexRegistry.getSingleton();

  protected UserRightId userRightId = null;

  /**
   * Should the id check (on null) be avoided before save (in save method)? This is use-full if the derived dao manages the id itself (as e.
   * g. KundeDao, Kost2ArtDao).
   */
  protected boolean avoidNullIdCheckBeforeSave;

  /**
   * Set this to true if you overload {@link #afterUpdate(ExtendedBaseDO, ExtendedBaseDO)} and you need the origin data base entryfterup.
   */
  protected boolean supportAfterUpdate = false;

  /**
   * Get all declared hibernate search fields. These fields are defined over annotations in the database object class. The names are the
   * property names or, if defined the name declared in the annotation of a field. <br/>
   * The user can search in these fields explicit by typing e. g. authors:beck (<field>:<searchString>)
   * @return
   */
  public synchronized String[] getSearchFields()
  {
    if (searchFields != null) {
      return searchFields;
    }
    Field[] fields = BeanHelper.getAllDeclaredFields(clazz);
    Set<String> fieldNames = new TreeSet<String>();
    for (Field field : fields) {
      if (field.isAnnotationPresent(org.hibernate.search.annotations.Field.class) == true) {
        // @Field(index = Index.TOKENIZED),
        org.hibernate.search.annotations.Field annotation = field.getAnnotation(org.hibernate.search.annotations.Field.class);
        fieldNames.add(getSearchName(field.getName(), annotation));
      } else if (field.isAnnotationPresent(org.hibernate.search.annotations.Fields.class) == true) {
        // @Fields( {
        // @Field(index = Index.TOKENIZED),
        // @Field(name = "name_forsort", index = Index.UN_TOKENIZED)
        // } )
        org.hibernate.search.annotations.Fields annFields = field.getAnnotation(org.hibernate.search.annotations.Fields.class);
        for (org.hibernate.search.annotations.Field annotation : annFields.value()) {
          fieldNames.add(getSearchName(field.getName(), annotation));
        }
      } else if (field.isAnnotationPresent(Id.class) == true) {
        fieldNames.add(field.getName());
      } else if (field.isAnnotationPresent(DocumentId.class) == true) {
        fieldNames.add(field.getName());
      }
    }
    Method[] methods = clazz.getMethods();
    for (Method method : methods) {
      if (method.isAnnotationPresent(org.hibernate.search.annotations.Field.class) == true) {
        org.hibernate.search.annotations.Field annotation = method.getAnnotation(org.hibernate.search.annotations.Field.class);
        fieldNames.add(getSearchName(method.getName(), annotation));
      } else if (method.isAnnotationPresent(DocumentId.class) == true) {
        String prop = BeanHelper.determinePropertyName(method);
        fieldNames.add(prop);
      }
    }
    if (getAdditionalSearchFields() != null) {
      for (String str : getAdditionalSearchFields()) {
        fieldNames.add(str);
      }
    }
    searchFields = new String[fieldNames.size()];
    fieldNames.toArray(searchFields);
    log.info("Search fields for '" + clazz + "': " + ArrayUtils.toString(searchFields));
    return searchFields;
  }

  /**
   * Overwrite this method for adding search fields manually (e. g. for embedded objects). For example see TimesheetDao.
   * @return
   */
  protected String[] getAdditionalSearchFields()
  {
    return null;
  }

  private String getSearchName(String fieldName, org.hibernate.search.annotations.Field annotation)
  {
    if (StringUtils.isNotEmpty(annotation.name()) == true) {
      // Name of field is changed for hibernate-search via annotation:
      return annotation.name();
    } else {
      return fieldName;
    }
  }

  public void setTxTemplate(TransactionTemplate txTemplate)
  {
    this.txTemplate = txTemplate;
  }

  public void setAccessChecker(AccessChecker accessChecker)
  {
    this.accessChecker = accessChecker;
  }

  public void setDatabaseDao(final DatabaseDao databaseDao)
  {
    this.databaseDao = databaseDao;
  }

  public void setUserGroupCache(UserGroupCache userGroupCache)
  {
    this.userGroupCache = userGroupCache;
  }

  public void setHistoryAdapter(HistoryAdapter historyAdapter)
  {
    this.historyAdapter = historyAdapter;
  }

  @Override
  protected void initDao()
  {
  }

  /**
   * The setting of the DO class is required.
   * @param clazz
   */
  protected BaseDao(Class<O> clazz)
  {
    this.clazz = clazz;
  }

  public Class<O> getDOClass()
  {
    return this.clazz;
  }

  public abstract O newInstance();

  /**
   * getOrLoad checks first weather the id is valid or not. Default implementation: id != 0 && id &gt; 0. Overload this, if the id of the DO
   * can be 0 for example.
   * @param id
   * @return
   */
  protected boolean isIdValid(Integer id)
  {
    return (id != null && id > 0);
  }

  /**
   * If the user has select access then the object will be returned. If not, the hibernate proxy object will be get via getSession().load();
   * @param id
   * @return
   */
  @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
  public O getOrLoad(Integer id)
  {
    if (isIdValid(id) == false) {
      return null;
    } else {
      O obj = internalGetById(id);
      if (obj == null) {
        throw new RuntimeException("Object with id " + id + " not found for class " + clazz);
      }
      if (hasSelectAccess(obj, false) == true) {
        return obj;
      }
    }
    @SuppressWarnings("unchecked")
    O result = (O) getSession().load(clazz, id);
    return result;
  }

  @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
  public List<O> internalLoadAll()
  {
    @SuppressWarnings("unchecked")
    final List<O> list = getHibernateTemplate().find("from " + clazz.getSimpleName() + " t");
    return list;
  }

  /**
   * Builds query filter by simply calling constructor of QueryFilter with given search filter and calls getList(QueryFilter). Override this
   * method for building more complex query filters.
   * @param filter
   * @return
   */
  @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
  public List<O> getList(BaseSearchFilter filter)
  {
    QueryFilter queryFilter = new QueryFilter(filter);
    return getList(queryFilter);
  }

  /**
   * Gets the list filtered by the given filter.
   * @param filter
   * @return
   */
  @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
  public List<O> getList(final QueryFilter filter) throws AccessException
  {
    checkSelectAccess();
    List<O> list = internalGetList(filter);
    if (list == null || list.size() == 0) {
      return list;
    }
    list = extractEntriesWithSelectAccess(list);
    return sort(list);
  }

  /**
   * Gets the list filtered by the given filter.
   * @param filter
   * @return
   */
  @SuppressWarnings("unchecked")
  @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
  public List<O> internalGetList(final QueryFilter filter) throws AccessException
  {
    final BaseSearchFilter searchFilter = filter.getFilter();
    filter.clearErrorMessage();
    if (searchFilter.isIgnoreDeleted() == false) {
      filter.add(Restrictions.eq("deleted", searchFilter.isDeleted()));
    }

    List<O> list = null;
    final Criteria criteria = filter.buildCriteria(getSession(), clazz);
    if (searchFilter.isSearchNotEmpty() == true) {
      String searchString = "";
      try {
        final FullTextSession fullTextSession = Search.getFullTextSession(getSession());
        final String[] searchFields = searchFilter.getSearchFields() != null ? searchFilter.getSearchFields() : getSearchFields();
        final MultiFieldQueryParser parser = new MultiFieldQueryParser(LUCENE_VERSION, searchFields, new PFAnalyzer());
        parser.setAllowLeadingWildcard(true);
        org.apache.lucene.search.Query query = null;
        try {
          searchString = modifySearchString(searchFilter.getSearchString());
          query = parser.parse(searchString);
        } catch (org.apache.lucene.queryParser.ParseException ex) {
          String errorMsg = "Lucene error message: "
              + ex.getMessage()
              + " (for "
              + this.getClass().getSimpleName()
              + ": "
              + searchString
              + ").";
          filter.setErrorMessage(errorMsg);
          log.info(errorMsg);
          return new ArrayList<O>();
        }
        final FullTextQuery fullTextQuery = fullTextSession.createFullTextQuery(query, clazz);
        fullTextQuery.setCriteriaQuery(criteria);
        list = fullTextQuery.list(); // return a list of managed objects
      } catch (Exception ex) {
        String errorMsg = "Lucene error message: "
            + ex.getMessage()
            + " (for "
            + this.getClass().getSimpleName()
            + ": "
            + searchString
            + ").";
        filter.setErrorMessage(errorMsg);
        log.info(errorMsg);
      }
    } else {
      list = criteria.list();
    }
    if (list != null) {
      list = selectUnique(list);
      if (searchFilter.useModificationFilter == true) {
        final Set<Integer> idSet = getModifiedEntries(getSession(), searchFilter);
        final List<O> result = new ArrayList<O>();
        for (final O entry : list) {
          if (contains(idSet, entry) == true) {
            result.add(entry);
          }
        }
        return result;
      }
    }
    return list;
  }

  /**
   * idSet.contains(entry.getId()) at default.
   * @param idSet
   * @param entry
   * @see org.projectforge.fibu.AuftragDao#contains(Set, org.projectforge.fibu.AuftragDO)
   */
  protected boolean contains(final Set<Integer> idSet, final O entry)
  {
    if (idSet == null) {
      return false;
    }
    return idSet.contains(entry.getId());
  }

  protected List<O> selectUnique(List<O> list)
  {
    @SuppressWarnings("unchecked")
    List<O> result = (List<O>) CollectionUtils.select(list, PredicateUtils.uniquePredicate());
    return result;
  }

  protected List<O> extractEntriesWithSelectAccess(final List<O> origList)
  {
    List<O> result = new ArrayList<O>();
    for (O obj : origList) {
      if (hasSelectAccess(obj, false) == true) {
        result.add(obj);
        afterLoad(obj);
      }
    }
    return result;
  }

  /**
   * Overwrite this method for own list sorting. This method returns only the given list.
   * @param list
   */
  protected List<O> sort(List<O> list)
  {
    return list;
  }

  /**
   * If the search string starts with "'" then the searchString will be returned without leading "'". If the search string consists only of
   * alphanumeric characters and allowed chars and spaces the wild card character '*' will be appended for enable ...* search. Otherwise the
   * searchString itself will be returned.
   * @param searchString
   * @return
   * @see #ALLOWED_CHARS
   * @see #ALLOWED_BEGINNING_CHARS
   * @see #ESCAPE_CHARS
   */
  public static String modifySearchString(String searchString)
  {
    if (searchString == null) {
      return "";
    }
    if (searchString.startsWith("'") == true) {
      return searchString.substring(1);
    }
    for (int i = 0; i < searchString.length(); i++) {
      char ch = searchString.charAt(i);
      if (Character.isLetterOrDigit(ch) == false && Character.isWhitespace(ch) == false) {
        String allowed = (i == 0) ? ALLOWED_BEGINNING_CHARS : ALLOWED_CHARS;
        if (allowed.indexOf(ch) < 0) {
          return searchString;
        }
      }
    }
    String[] tokens = StringUtils.split(searchString, ' ');
    StringBuffer buf = new StringBuffer();
    boolean first = true;
    for (String token : tokens) {
      if (first == true) {
        first = false;
      } else {
        buf.append(" ");
      }
      if (ArrayUtils.contains(luceneReservedWords, token) == false) {
        String modified = modifySearchToken(token);
        buf.append(modified);
        if (modified.endsWith("*") == false && StringUtils.containsNone(modified, ESCAPE_CHARS) == true) {
          buf.append('*');
        }
      } else {
        buf.append(token);
      }
    }
    return buf.toString();
  }

  /**
   * Does nothing (because it seems to be work better in most times). Quotes special Lucene characters: '-' -> "\-"
   * @param searchToken One word / token of the search string (one entry of StringUtils.split(searchString, ' ')).
   * @return
   */
  protected static String modifySearchToken(String searchToken)
  {
    StringBuffer buf = new StringBuffer();
    for (int i = 0; i < searchToken.length(); i++) {
      char ch = searchToken.charAt(i);
      /*
       * if (ESCAPE_CHARS.indexOf(ch) >= 0) { buf.append('\\'); }
       */
      buf.append(ch);
    }
    return buf.toString();
  }

  /**
   * @param id primary key of the base object.
   * @return
   */
  @Transactional(readOnly = true, propagation = Propagation.REQUIRES_NEW)
  public O getById(Serializable id) throws AccessException
  {
    checkSelectAccess();
    final O obj = internalGetById(id);
    if (obj == null) {
      return null;
    }
    checkSelectAccess(obj);
    return obj;
  }

  @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
  public O internalGetById(Serializable id)
  {
    if (id == null) {
      return null;
    }
    @SuppressWarnings("unchecked")
    final O obj = (O) getHibernateTemplate().get(clazz, id, LockMode.READ);
    afterLoad(obj);
    return obj;
  }

  /**
   * Gets the history entries of the object.
   * @param id The id of the object.
   * @return
   */
  @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
  public HistoryEntry[] getHistoryEntries(final O obj)
  {
    checkHistoryAccess(obj);
    return internalGetHistoryEntries(obj);
  }

  @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
  public HistoryEntry[] internalGetHistoryEntries(final BaseDO< ? > obj)
  {
    final HistoryAdapter ad = new HistoryAdapter();
    ad.setSessionFactory(getHibernateTemplate().getSessionFactory());
    return ad.getHistoryEntries(obj);
  }

  /**
   * Gets the history entries of the object in flat format.<br/>
   * Please note: If user has no access an empty list will be returned.
   * @param id The id of the object.
   * @return
   */
  @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
  public List<DisplayHistoryEntry> getDisplayHistoryEntries(final O obj)
  {
    if (obj.getId() == null || hasHistoryAccess(obj, false) == false) {
      return EMPTY_HISTORY_ENTRIES;
    }
    @SuppressWarnings("unchecked")
    final List<DisplayHistoryEntry> result = (List<DisplayHistoryEntry>) getHibernateTemplate().execute(new HibernateCallback() {
      public Object doInHibernate(Session session) throws HibernateException, SQLException
      {
        final HistoryEntry[] entries = getHistoryEntries(obj);
        if (entries == null) {
          return null;
        }
        return convertAll(entries, session);
      }
    });
    return result;
  }

  public List<DisplayHistoryEntry> internalGetDisplayHistoryEntries(final BaseDO< ? > obj)
  {
    @SuppressWarnings("unchecked")
    List<DisplayHistoryEntry> result = (List<DisplayHistoryEntry>) getHibernateTemplate().execute(new HibernateCallback() {
      public Object doInHibernate(Session session) throws HibernateException, SQLException
      {
        HistoryEntry[] entries = internalGetHistoryEntries(obj);
        if (entries == null) {
          return null;
        }
        return convertAll(entries, session);
      }
    });
    return result;
  }

  protected List<DisplayHistoryEntry> convertAll(final HistoryEntry[] entries, final Session session)
  {
    final List<DisplayHistoryEntry> list = new ArrayList<DisplayHistoryEntry>();
    for (HistoryEntry entry : entries) {
      final List<DisplayHistoryEntry> l = convert(entry, session);
      list.addAll(l);
    }
    return list;
  }

  public List<DisplayHistoryEntry> convert(HistoryEntry entry, Session session)
  {
    List<DisplayHistoryEntry> result = new ArrayList<DisplayHistoryEntry>();
    List<PropertyDelta> delta = entry.getDelta();
    if (delta == null || delta.size() == 0) {
      DisplayHistoryEntry se = new DisplayHistoryEntry(userGroupCache, entry);
      result.add(se);
    } else {
      for (PropertyDelta prop : delta) {
        DisplayHistoryEntry se = new DisplayHistoryEntry(userGroupCache, entry, prop, session);
        result.add(se);
      }
    }
    return result;
  }

  /**
   * Gets the history entries of the object in flat format.<br/>
   * Please note: No check access will be done! Please check the access before while getting the object.
   * @param id The id of the object.
   * @return
   */
  @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
  public List<SimpleHistoryEntry> getSimpleHistoryEntries(final O obj)
  {
    @SuppressWarnings("unchecked")
    List<SimpleHistoryEntry> result = (List<SimpleHistoryEntry>) getHibernateTemplate().execute(new HibernateCallback() {
      public Object doInHibernate(Session session) throws HibernateException, SQLException
      {
        HistoryEntry[] entries = getHistoryEntries(obj);
        if (entries == null) {
          return null;
        }
        List<SimpleHistoryEntry> list = new ArrayList<SimpleHistoryEntry>();
        for (HistoryEntry entry : entries) {
          List<PropertyDelta> delta = entry.getDelta();
          if (delta == null || delta.size() == 0) {
            SimpleHistoryEntry se = new SimpleHistoryEntry(userGroupCache, entry);
            list.add(se);
          } else {
            for (PropertyDelta prop : delta) {
              SimpleHistoryEntry se = new SimpleHistoryEntry(userGroupCache, entry, prop);
              list.add(se);
            }
          }
        }
        return list;
      }
    });
    return result;
  }

  /**
   * @param obj
   * @return the generated identifier, if save method is used, otherwise null.
   * @throws AccessException
   */
  @Transactional(readOnly = false, propagation = Propagation.REQUIRES_NEW)
  public Serializable saveOrUpdate(O obj) throws AccessException
  {
    Serializable id = null;
    if (obj.getId() != null) {
      update(obj);
    } else {
      id = save(obj);
    }
    return id;
  }

  /**
   * @param obj
   * @return the generated identifier, if save method is used, otherwise null.
   */
  @Transactional(readOnly = false, propagation = Propagation.REQUIRES_NEW)
  public Serializable internalSaveOrUpdate(O obj)
  {
    Serializable id = null;
    if (obj.getId() != null) {
      internalUpdate(obj);
    } else {
      id = internalSave(obj);
    }
    return id;
  }

  /**
   * Call save(O) for every object in the given list.
   * @param objects
   * @return the generated identifier.
   * @throws AccessException
   */
  @Transactional(readOnly = false, propagation = Propagation.REQUIRES_NEW)
  public void save(List<O> objects) throws AccessException
  {
    Validate.notNull(objects);
    for (final O obj : objects) {
      save(obj);
    }
  }

  /**
   * 
   * @param obj
   * @return the generated identifier.
   * @throws AccessException
   */
  @Transactional(readOnly = false, propagation = Propagation.REQUIRES_NEW)
  public Serializable save(O obj) throws AccessException
  {
    Validate.notNull(obj);
    if (avoidNullIdCheckBeforeSave == false) {
      Validate.isTrue(obj.getId() == null);
    }
    checkInsertAccess(obj);
    return internalSave(obj);
  }

  /**
   * This method will be called after loading an object from the data base. Does nothing at default. This method is not called by
   * internalLoadAll.
   */
  protected void afterLoad(final O obj)
  {

  }

  /**
   * This method will be called after inserting, updating, deleting or marking the data object as deleted. This method is for example needed
   * for expiring the UserGroupCache after inserting or updating a user or group data object. Does nothing at default.
   */
  protected void afterSaveOrModify(final O obj)
  {
  }

  /**
   * This method will be called after inserting. Does nothing at default.
   * @param obj The inserted object
   */
  protected void afterSave(final O obj)
  {
  }

  /**
   * This method will be called before inserting, updating, deleting or marking the data object as deleted. Does nothing at default.
   */
  protected void onSaveOrModify(final O obj)
  {
  }

  /**
   * This method will be called after updating. Does nothing at default. PLEASE NOTE: If you overload this method don't forget to set
   * {@link #supportAfterUpdate} to true, otherwise you won't get the origin data base object!
   * @param obj The modified object
   * @param dbObj The object from data base before modification.
   */
  protected void afterUpdate(final O obj, final O dbObj)
  {

  }

  /**
   * This method will be called before updating the data object. Will also called if in internalUpdate no modification was detected. Please
   * note: Do not modify the object oldVersion! Does nothing at default.
   * @param obj The changed object.
   * @param dbObj The current data base version of this object.
   */
  protected void onChange(final O obj, final O dbObj)
  {
  }

  /**
   * This method will be called after deleting. Does nothing at default.
   * @param obj The deleted object.
   * @param dbObj The object from data base before modification.
   */
  protected void afterDelete(final O obj)
  {
  }

  /**
   * This method will be called after undeleting. Does nothing at default.
   * @param obj The deleted object.
   * @param dbObj The object from data base before modification.
   */
  protected void afterUndelete(final O obj)
  {
  }

  /**
   * This method is for internal use e. g. for updating objects without check access.
   * @param obj
   * @return the generated identifier.
   */
  @Transactional(readOnly = false, propagation = Propagation.REQUIRES_NEW, isolation = Isolation.REPEATABLE_READ)
  public Serializable internalSave(O obj)
  {
    Validate.notNull(obj);
    accessChecker.checkDemoUser();
    obj.setCreated();
    obj.setLastUpdate();
    onSaveOrModify(obj);
    final Serializable id = getHibernateTemplate().save(obj);
    log.info("New object added (" + id + "): " + obj.toString());
    prepareHibernateSearch(obj, OperationType.INSERT);
    afterSaveOrModify(obj);
    afterSave(obj);
    return id;
  }

  @Transactional(readOnly = false, propagation = Propagation.REQUIRED, isolation = Isolation.REPEATABLE_READ)
  public void saveOrUpdate(final Collection<O> col)
  {
    for (final O obj : col) {
      saveOrUpdate(obj);
    }
  }

  @Transactional(readOnly = true, propagation = Propagation.REQUIRED, isolation = Isolation.REPEATABLE_READ)
  public void saveOrUpdate(final BaseDao<O> currentProxy, final Collection<O> col, final int blockSize)
  {
    final List<O> list = new ArrayList<O>();
    int counter = 0;
    // final BaseDao<O> currentProxy = (BaseDao<O>) AopContext.currentProxy();
    for (final O obj : col) {
      list.add(obj);
      if (++counter >= blockSize) {
        counter = 0;
        currentProxy.saveOrUpdate(list);
        list.clear();
      }
    }
    currentProxy.saveOrUpdate(list);
  }

  @Transactional(readOnly = false, propagation = Propagation.REQUIRED, isolation = Isolation.REPEATABLE_READ)
  public void internalSaveOrUpdate(final Collection<O> col)
  {
    for (final O obj : col) {
      internalSaveOrUpdate(obj);
    }
  }

  @Transactional(readOnly = false, propagation = Propagation.REQUIRED)
  public void internalSaveOrUpdate(final BaseDao<O> currentProxy, final Collection<O> col, final int blockSize)
  {
    final List<O> list = new ArrayList<O>();
    int counter = 0;
    // final BaseDao<O> currentProxy = (BaseDao<O>) AopContext.currentProxy();
    for (final O obj : col) {
      list.add(obj);
      if (++counter >= blockSize) {
        counter = 0;
        currentProxy.internalSaveOrUpdate(list);
        list.clear();
      }
    }
    currentProxy.internalSaveOrUpdate(list);
  }

  /**
   * @param obj
   * @throws AccessException
   * @return true, if modifications were done, false if no modification detected.
   * @see #internalUpdate(ExtendedBaseDO, boolean)
   */
  @Transactional(readOnly = false, propagation = Propagation.REQUIRES_NEW, isolation = Isolation.REPEATABLE_READ)
  public boolean update(O obj) throws AccessException
  {
    Validate.notNull(obj);
    if (obj.getId() == null) {
      String msg = "Could not update object unless id is not given:" + obj.toString();
      log.error(msg);
      throw new RuntimeException(msg);
    }
    return internalUpdate(obj, true);
  }

  /**
   * This method is for internal use e. g. for updating objects without check access.
   * @param obj
   * @return true, if modifications were done, false if no modification detected.
   * @see #internalUpdate(ExtendedBaseDO, boolean)
   */
  @Transactional(readOnly = false, propagation = Propagation.REQUIRED, isolation = Isolation.REPEATABLE_READ)
  public boolean internalUpdate(O obj)
  {
    return internalUpdate(obj, false);
  }

  /**
   * This method is for internal use e. g. for updating objects without check access.<br/>
   * Please note: update ignores the field deleted. Use markAsDeleted, delete and undelete methods instead.
   * @param obj
   * @param checkAccess If false, any access check will be ignored.
   * @return true, if modifications were done, false if no modification detected.
   */
  @Transactional(readOnly = false, propagation = Propagation.REQUIRED, isolation = Isolation.REPEATABLE_READ)
  public boolean internalUpdate(O obj, boolean checkAccess)
  {
    onSaveOrModify(obj);
    accessChecker.checkDemoUser();
    @SuppressWarnings("unchecked")
    final O dbObj = (O) getHibernateTemplate().load(clazz, obj.getId(), LockMode.PESSIMISTIC_WRITE);
    if (checkAccess == true) {
      checkUpdateAccess(obj, dbObj);
    }
    onChange(obj, dbObj);
    final O dbObjBackup;
    if (supportAfterUpdate == true) {
      dbObjBackup = newInstance();
      copyValues(dbObj, dbObjBackup);
    } else {
      dbObjBackup = null;
    }
    // Copy all values of modified user to database object, ignore field 'deleted'.
    boolean result = copyValues(obj, dbObj, "deleted");
    if (result == true) {
      dbObj.setLastUpdate();
      log.info("Object updated: " + dbObj.toString());
    } else {
      log.info("No modifications detected (no update needed): " + dbObj.toString());
    }
    if (obj.isMinorChange() == false) {
      reindex(obj);
    }
    prepareHibernateSearch(obj, OperationType.UPDATE);
    afterSaveOrModify(obj);
    if (supportAfterUpdate == true) {
      afterUpdate(obj, dbObjBackup);
    }
    return result;
  }

  /**
   * Overwrite this method if you have lazy exceptions while Hibernate-Search re-indexes. See e. g. AuftragDao.
   * @param obj
   */
  protected void prepareHibernateSearch(final O obj, final OperationType operationType)
  {
  }

  /**
   * Object will be marked as deleted (boolean flag), therefore undelete is always possible without any loss of data.
   * @param obj
   */
  @Transactional(readOnly = false, propagation = Propagation.REQUIRES_NEW, isolation = Isolation.REPEATABLE_READ)
  public void markAsDeleted(O obj) throws AccessException
  {
    Validate.notNull(obj);
    if (obj.getId() == null) {
      String msg = "Could not delete object unless id is not given:" + obj.toString();
      log.error(msg);
      throw new RuntimeException(msg);
    }
    @SuppressWarnings("unchecked")
    O dbObj = (O) getHibernateTemplate().load(clazz, obj.getId(), LockMode.PESSIMISTIC_WRITE);
    checkDeleteAccess(obj, dbObj);
    internalMarkAsDeleted(obj);
  }

  @Transactional(readOnly = false, propagation = Propagation.REQUIRES_NEW, isolation = Isolation.REPEATABLE_READ)
  public void internalMarkAsDeleted(O obj)
  {
    if (obj instanceof Historizable == false) {
      log.error("Object is not historizable. Therefore marking as deleted is not supported. Please use delete instead.");
      throw new InternalErrorException();
    }
    accessChecker.checkDemoUser();
    @SuppressWarnings("unchecked")
    O dbObj = (O) getHibernateTemplate().load(clazz, obj.getId(), LockMode.PESSIMISTIC_WRITE);
    onSaveOrModify(obj);
    copyValues(obj, dbObj, "deleted"); // If user has made additional changes.
    dbObj.setDeleted(true);
    dbObj.setLastUpdate();
    afterSaveOrModify(obj);
    afterDelete(obj);
    getSession().flush();
    log.info("Object marked as deleted: " + dbObj.toString());
  }

  /**
   * Object will be deleted finally out of the data base.
   * @param obj
   */
  @Transactional(readOnly = false, propagation = Propagation.REQUIRES_NEW, isolation = Isolation.REPEATABLE_READ)
  public void delete(O obj) throws AccessException
  {
    Validate.notNull(obj);
    if (obj instanceof Historizable) {
      String msg = EXCEPTION_HISTORIZABLE_NOTDELETABLE + obj.toString();
      log.error(msg);
      throw new RuntimeException(msg);
    }
    if (obj.getId() == null) {
      String msg = "Could not destroy object unless id is not given: " + obj.toString();
      log.error(msg);
      throw new RuntimeException(msg);
    }
    accessChecker.checkDemoUser();
    @SuppressWarnings("unchecked")
    O dbObj = (O) getHibernateTemplate().load(clazz, obj.getId(), LockMode.PESSIMISTIC_WRITE);
    checkDeleteAccess(obj, dbObj);
    getHibernateTemplate().delete(dbObj);
    log.info("Object deleted: " + obj.toString());
    afterSaveOrModify(obj);
    afterDelete(obj);
  }

  /**
   * Object will be marked as deleted (booelan flag), therefore undelete is always possible without any loss of data.
   * @param obj
   */
  @Transactional(readOnly = false, propagation = Propagation.REQUIRES_NEW, isolation = Isolation.REPEATABLE_READ)
  public void undelete(O obj) throws AccessException
  {
    Validate.notNull(obj);
    if (obj.getId() == null) {
      String msg = "Could not undelete object unless id is not given:" + obj.toString();
      log.error(msg);
      throw new RuntimeException(msg);
    }
    checkInsertAccess(obj);
    internalUndelete(obj);
  }

  @Transactional(readOnly = false, propagation = Propagation.REQUIRES_NEW, isolation = Isolation.REPEATABLE_READ)
  public void internalUndelete(O obj)
  {
    accessChecker.checkDemoUser();
    @SuppressWarnings("unchecked")
    O dbObj = (O) getHibernateTemplate().load(clazz, obj.getId(), LockMode.PESSIMISTIC_WRITE);
    onSaveOrModify(obj);
    copyValues(obj, dbObj, "deleted"); // If user has made additional changes.
    dbObj.setDeleted(false);
    obj.setDeleted(false);
    dbObj.setLastUpdate();
    obj.setLastUpdate(dbObj.getLastUpdate());
    log.info("Object undeleted: " + dbObj.toString());
    afterSaveOrModify(obj);
    afterUndelete(obj);
  }

  /**
   * Checks the basic select access right. Overload this method if you class supports this right.
   * @return
   */
  protected void checkSelectAccess() throws AccessException
  {
    if (hasSelectAccess(true) == false) {
      // Should not occur!
      log.error("Development error: Subclass should throw an exception instead of returning false.");
      throw new UserException(UserException.I18N_KEY_PLEASE_CONTACT_DEVELOPER_TEAM);
    }
  }

  protected void checkSelectAccess(O obj) throws AccessException
  {
    if (hasSelectAccess(obj, true) == false) {
      // Should not occur!
      log.error("Development error: Subclass should throw an exception instead of returning false.");
      throw new UserException(UserException.I18N_KEY_PLEASE_CONTACT_DEVELOPER_TEAM);
    }
  }

  protected void checkHistoryAccess(O obj) throws AccessException
  {
    if (hasHistoryAccess(true) == false || hasHistoryAccess(obj, true) == false) {
      // Should not occur!
      log.error("Development error: Subclass should throw an exception instead of returning false.");
      throw new UserException(UserException.I18N_KEY_PLEASE_CONTACT_DEVELOPER_TEAM);
    }
  }

  protected void checkInsertAccess(O obj) throws AccessException
  {
    if (hasInsertAccess(obj, true) == false) {
      // Should not occur!
      log.error("Development error: Subclass should throw an exception instead of returning false.");
      throw new UserException(UserException.I18N_KEY_PLEASE_CONTACT_DEVELOPER_TEAM);
    }
  }

  /**
   * @param dbObj The original object (stored in the database)
   * @param obj
   * @throws AccessException
   */
  protected void checkUpdateAccess(O obj, O dbObj) throws AccessException
  {
    if (hasUpdateAccess(obj, dbObj, true) == false) {
      // Should not occur!
      log.error("Development error: Subclass should throw an exception instead of returning false.");
      throw new UserException(UserException.I18N_KEY_PLEASE_CONTACT_DEVELOPER_TEAM);
    }
  }

  protected void checkDeleteAccess(O obj, O dbObj) throws AccessException
  {
    if (hasDeleteAccess(obj, dbObj, true) == false) {
      // Should not occur!
      log.error("Development error: Subclass should throw an exception instead of returning false.");
      throw new UserException(UserException.I18N_KEY_PLEASE_CONTACT_DEVELOPER_TEAM);
    }
  }

  /**
   * Checks the basic select access right. Overwrite this method if the basic select access should be checked.
   * @return true at default or if readWriteUserRightId is given hasReadAccess(boolean).
   * @see #hasReadAccess(boolean)
   */
  public boolean hasSelectAccess(boolean throwException)
  {
    return hasAccess(null, null, OperationType.SELECT, throwException);
  }

  /**
   * If userRightId is given then {@link AccessChecker#hasAccess(UserRightId, Object, Object, OperationType, boolean)} is called and
   * returned. If not given a UnsupportedOperationException is thrown. Checks the user's access to the given object.
   * @param obj The object.
   * @param obj The old version of the object (is only given for operationType {@link OperationType#UPDATE}).
   * @param operationType The operation type (select, insert, update or delete)
   * @return true, if the user has the access right for the given operation type and object.
   */
  public boolean hasAccess(final O obj, final O oldObj, final OperationType operationType, final boolean throwException)
  {
    if (userRightId != null) {
      return accessChecker.hasAccess(userRightId, obj, oldObj, operationType, throwException);
    }
    throw new UnsupportedOperationException(
        "readWriteUserRightId not given. Override this method or set readWriteUserRightId in constructor.");
  }

  /**
   * Checks select access right by calling hasAccess(obj, OperationType.SELECT).
   * @param obj Check access to this object.
   * @return
   * @see #hasAccess(Object, OperationType)
   */
  public boolean hasSelectAccess(O obj, boolean throwException)
  {
    return hasAccess(obj, null, OperationType.SELECT, throwException);
  }

  /**
   * Has the user access to the history of the given object. At default this method calls hasHistoryAccess(boolean) first and then
   * hasSelectAccess.
   * @param throwException
   */
  public boolean hasHistoryAccess(O obj, boolean throwException)
  {
    if (hasHistoryAccess(throwException) == false) {
      return false;
    }
    if (userRightId != null) {
      return accessChecker.hasHistoryAccess(userRightId, obj, throwException);
    }
    return hasSelectAccess(obj, throwException);
  }

  /**
   * Has the user access to the history in general of the objects. At default this method calls hasSelectAccess.
   * @param throwException
   */
  public boolean hasHistoryAccess(boolean throwException)
  {
    if (userRightId != null) {
      return accessChecker.hasHistoryAccess(userRightId, null, throwException);
    }
    return hasSelectAccess(throwException);
  }

  /**
   * Checks insert access right by calling hasAccess(obj, OperationType.INSERT).
   * @param obj Check access to this object.
   * @return
   * @see #hasAccess(Object, OperationType)
   */
  public boolean hasInsertAccess(O obj, boolean throwException)
  {
    return hasAccess(obj, null, OperationType.INSERT, throwException);
  }

  /**
   * Checks write access of the readWriteUserRight. If not given, true is returned at default. This method should only be used for checking
   * the insert access to show an insert button or not. Before inserting any object the write access is checked by has*Access(...)
   * independent of the result of this method.
   * @see org.projectforge.core.IDao#hasInsertAccess()
   */
  public boolean hasInsertAccess()
  {
    if (userRightId != null) {
      return accessChecker.hasInsertAccess(userRightId, false);
    }
    return true;
  }

  /**
   * Checks update access right by calling hasAccess(obj, OperationType.UPDATE).
   * @param dbObj The original object (stored in the database)
   * @param obj Check access to this object.
   * @return
   * @see #hasAccess(Object, OperationType)
   */
  public boolean hasUpdateAccess(final O obj, final O dbObj, boolean throwException)
  {
    return hasAccess(obj, dbObj, OperationType.UPDATE, throwException);
  }

  /**
   * Checks delete access right by calling hasAccess(obj, OperationType.DELETE).
   * @param obj Check access to this object.
   * @param dbObj current version of this object in the data base.
   * @return
   * @see #hasAccess(Object, OperationType)
   */
  public boolean hasDeleteAccess(final O obj, final O dbObj, final boolean throwException)
  {
    return hasAccess(obj, dbObj, OperationType.DELETE, throwException);
  }

  /**
   * Overload this method for copying field manually. Used for modifiing fields inside methods: update, markAsDeleted and undelete.
   * 
   * @param src
   * @param dest
   * @return true, if any field was modified, otherwise false.
   * @see BaseDO#copyValuesFrom(BaseDO, String...)
   */
  protected boolean copyValues(O src, O dest, String... ignoreFields)
  {
    return dest.copyValuesFrom(src, ignoreFields);
  }

  protected void createHistoryEntry(Object entity, Number id, String property, Class< ? > valueClass, Object oldValue, Object newValue)
  {
    accessChecker.checkDemoUser();
    final PFUserDO contextUser = PFUserContext.getUser();
    final String userPk = contextUser != null ? contextUser.getId().toString() : null;
    if (userPk == null) {
      log.warn("No user found for creating history entry.");
    }
    historyAdapter.createHistoryEntry(entity, id, new HistoryUserRetriever() {
      public String getPrincipal()
      {
        return userPk;
      }
    }, property, valueClass, oldValue, newValue);
  }

  /**
   * Only generic check access will be done. The matching entries will not be checked!
   * @param property Property of the data base entity.
   * @param searchString String the user has typed in.
   * @return All matching entries (like search) for the given property modified or updated in the last 2 years.
   */
  @SuppressWarnings("unchecked")
  public List<String> getAutocompletion(final String property, final String searchString)
  {
    checkSelectAccess();
    if (StringUtils.isBlank(searchString) == true) {
      return null;
    }
    final String hql = "select distinct "
        + property
        + " from "
        + clazz.getSimpleName()
        + " t where deleted=false and lastUpdate > ? and lower(t."
        + property
        + ") like ?) order by t."
        + property;
    final Query query = getSession().createQuery(hql);
    final DateHolder dh = new DateHolder();
    dh.add(Calendar.YEAR, -2); // Search only for entries of the last 2 years.
    query.setDate(0, dh.getDate());
    query.setString(1, "%" + StringUtils.lowerCase(searchString) + "%");
    final List<String> list = query.list();
    return list;
  }

  /**
   * If this dao is registered at BaseDaoReindexRegistry then this method should be implemented and should return a list of all objects
   * which are needed to re-index because of dependency of the given modified object.
   * @param obj Modified object.
   */
  public List<O> getDependentObjectsToReindex(final BaseDO< ? > obj)
  {
    log.warn("This dao is registered as dependent of objects of type "
        + obj.getClass()
        + " but does not implement getDependentObjectsToReindex(BaseDO<?>). Ignoring registry enry.");
    return null;
  }

  /**
   * Re-indexes the entries of the last day, 1,000 at max.
   * @see DatabaseDao#createReindexSettings(boolean)
   */
  public void rebuildDatabaseIndex4NewestEntries()
  {
    final ReindexSettings settings = DatabaseDao.createReindexSettings(true);
    databaseDao.rebuildDatabaseSearchIndices(clazz, settings);
  }

  /**
   * Re-indexes all entries (full re-index).
   */
  public void rebuildDatabaseIndex()
  {
    final ReindexSettings settings = DatabaseDao.createReindexSettings(false);
    databaseDao.rebuildDatabaseSearchIndices(clazz, settings);
  }

  /**
   * Re-index this object manually (hibernate search). Also all registered dao classes will be called for re-indexing depending data base
   * entries.
   * @param obj
   */
  @Transactional(readOnly = false, propagation = Propagation.REQUIRES_NEW, isolation = Isolation.REPEATABLE_READ)
  public void reindex(final O obj)
  {
    reindex(obj, new HashSet<String>());
  }

  /**
   * @param obj
   * @param alreadyReindexed Avoids double re-indexing within one run.
   */
  @SuppressWarnings("unchecked")
  @Transactional(readOnly = false, propagation = Propagation.REQUIRED, isolation = Isolation.REPEATABLE_READ)
  protected void reindex(final O obj, final Set<String> alreadyReindexed)
  {
    if (alreadyReindexed.contains(getReindexId(obj)) == true) {
      if (log.isDebugEnabled() == true) {
        log.debug("Object already re-indexed (skipping): " + getReindexId(obj));
      }
      return;
    }
    final Session session = getSession();
    session.flush(); // Needed to flush the object changes!
    final FullTextSession fullTextSession = Search.getFullTextSession(session);
    fullTextSession.setFlushMode(FlushMode.AUTO);
    fullTextSession.setCacheMode(CacheMode.IGNORE);
    O dbObj = (O) session.get(obj.getClass(), obj.getId());
    if (dbObj == null) {
      dbObj = (O) session.load(obj.getClass(), obj.getId());
    }
    fullTextSession.index(dbObj);
    alreadyReindexed.add(getReindexId(dbObj));
    // session.flush(); // clear every batchSize since the queue is processed
    if (log.isDebugEnabled() == true) {
      log.debug("Object added to index: " + getReindexId(dbObj));
    }
    final Set<BaseDao< ? >> dependentDaos = baseDaoReindexRegistry.getRegisteredDependents(dbObj);
    if (dependentDaos != null) {
      for (final BaseDao< ? > dao : dependentDaos) {
        dao.reindexDependents(obj, alreadyReindexed);
      }
    }
  }

  /**
   * Logs error message. Called by registered daos as dependent of modified objects.
   * @param obj Object which is not supported by the dao.
   * @return null
   */
  protected List<O> dependencyNotSupportedOf(final BaseDO< ? > obj)
  {
    log.error("Object of type "
        + getReindexId(obj)
        + " not supported for handling dependents for dao of type: "
        + this.getClass().getName());
    return null;
  }

  private String getReindexId(final BaseDO< ? > obj)
  {
    return obj.getClass() + ":" + obj.getId();
  }

  /**
   * Re-index all entries of the given list. If the given set isn't null, then the id's of the re-indexed objects are added. Objects which
   * are already in the given set, will be ignored.
   * @param reindexObjects Objects to re-index.
   * @param alreadyReindexed This set, if not null, contains already re-indexed objects inside the caller method.
   */
  @Transactional(readOnly = false, propagation = Propagation.REQUIRED, isolation = Isolation.REPEATABLE_READ)
  private void reindexDependents(final BaseDO< ? > obj, final Set<String> alreadyReindexed)
  {
    final List<O> dependents = getDependentObjectsToReindex(obj);
    if (dependents == null) {
      return;
    }
    for (final O dependentObj : dependents) {
      reindex(dependentObj, alreadyReindexed);
    }
  }

  @Transactional(readOnly = false, propagation = Propagation.REQUIRES_NEW)
  public void massUpdate(final List<O> list, final O master)
  {
    if (list == null || list.size() == 0) {
      // No entries to update.
      return;
    }
    if (list.size() > MAX_MASS_UPDATE) {
      throw new UserException(MAX_MASS_UPDATE_EXCEEDED_EXCEPTION_I18N, new Object[] { MAX_MASS_UPDATE});
    }
    final Object store = prepareMassUpdateStore(list, master);
    for (final O entry : list) {
      if (massUpdateEntry(entry, master, store) == true) {
        try {
          update(entry);
        } catch (Exception ex) {
          log.info("Exception occured while updating entry inside mass update: " + entry);
        }
      }
    }
  }

  /**
   * Object pass thru every massUpdateEntry call.
   * @param list
   * @param master
   * @return null if not overloaded.
   */
  protected Object prepareMassUpdateStore(final List<O> list, final O master)
  {
    return null;
  }

  /**
   * Overload this method for mass update support.
   * @param entry
   * @param master
   * @param store Object created with prepareMassUpdateStore if needed. Null at default.
   * @return true, if entry is ready for update otherwise false (no update will be done for this entry).
   */
  protected boolean massUpdateEntry(final O entry, final O master, final Object store)
  {
    throw new UnsupportedOperationException("Mass update is not supported by this dao for: " + clazz.getName());
  }

  private Set<Integer> getModifiedEntries(final Session session, final BaseSearchFilter filter)
  {
    log.debug("Searching in " + clazz);
    if (hasSelectAccess(false) == false || hasHistoryAccess(false) == false) {
      // User has in general no access to history entries of the given object type (clazz).
      return null;
    }
    final Set<Integer> idSet = new HashSet<Integer>();
    getModifiedEntries(session, filter, idSet, clazz);
    if (getAdditionalHistorySearchDOs() != null) {
      for (final Class< ? > aclazz : getAdditionalHistorySearchDOs()) {
        getModifiedEntries(session, filter, idSet, aclazz);
      }
    }
    return idSet;
  }

  private void getModifiedEntries(final Session session, final BaseSearchFilter filter, final Set<Integer> idSet, final Class< ? > clazz)
  {
    log.debug("Searching in " + clazz);
    // First get all history entries matching the filter and the given class.
    final String className = ClassUtils.getShortClassName(clazz);
    final Criteria crit = session.createCriteria(HistoryEntry.class);
    crit.add(Restrictions.eq("className", className));
    if (filter.getStartTimeOfLastModification() != null && filter.getStopTimeOfLastModification() != null) {
      crit.add(Restrictions.between("timestamp", filter.getStartTimeOfLastModification(), filter.getStopTimeOfLastModification()));
    } else if (filter.getStartTimeOfLastModification() != null) {
      crit.add(Restrictions.ge("timestamp", filter.getStartTimeOfLastModification()));
    } else if (filter.getStopTimeOfLastModification() != null) {
      crit.add(Restrictions.le("timestamp", filter.getStopTimeOfLastModification()));
    }
    if (filter.getModifiedByUserId() != null) {
      crit.add(Restrictions.eq("userName", filter.getModifiedByUserId().toString()));
    }
    crit.setCacheable(true);
    crit.setCacheRegion("historyItemCache");
    crit.setProjection(Projections.property("entityId"));
    @SuppressWarnings("unchecked")
    final List<Integer> idList = crit.list();
    if (idList != null && idList.size() > 0) {
      for (final Integer id : idList) {
        idSet.add(id);
      }
    }
  }

  protected Class< ? >[] getAdditionalHistorySearchDOs()
  {
    return null;
  }

  /**
   * @return The type of the data object (BaseDO) this dao is responsible for.
   */
  public Class< ? > getDataObjectType()
  {
    return clazz;
  }

  /**
   * @return Wether the data object (BaseDO) this dao is responsible for is from type Historizable or not.
   */
  public boolean isHistorizable()
  {
    return Historizable.class.isAssignableFrom(clazz);
  }
}
