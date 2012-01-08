/////////////////////////////////////////////////////////////////////////////
//
// Project ProjectForge Community Edition
//         www.projectforge.org
//
// Copyright (C) 2001-2012 Kai Reinhard (k.reinhard@micromata.com)
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

package org.projectforge.database.xstream;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.ClassUtils;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.projectforge.database.HibernateUtils;
import org.projectforge.fibu.EingangsrechnungsPositionDO;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.ConverterLookup;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

import de.micromata.hibernate.history.HistoryEntry;
import de.micromata.hibernate.history.delta.AssociationPropertyDelta;
import de.micromata.hibernate.history.delta.CollectionPropertyDelta;
import de.micromata.hibernate.history.delta.PropertyDelta;
import de.micromata.hibernate.history.delta.SimplePropertyDelta;

/**
 * Registers all read objects and saves them in the configurable order to the data base.
 * @author Kai Reinhard (k.reinhard@micromata.de)
 * 
 */
public class XStreamSavingConverter implements Converter
{
  /** The logger */
  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(XStreamSavingConverter.class);

  private final ConverterLookup defaultConv;

  private final Map<Class< ? >, List<Object>> allObjects = new HashMap<Class< ? >, List<Object>>();

  private final Set<Class< ? >> writtenObjectTypes = new HashSet<Class< ? >>();

  // Objekte dürfen nur einmal geschrieben werden, daher merken, was bereits gespeichert wurde
  private final Set<Object> writtenObjects = new HashSet<Object>();

  // Store the objects in the given order and all the other object types which are not listed here afterwards.
  private final List<Class< ? >> orderOfSaving = new ArrayList<Class< ? >>();

  // Ignore these objects from saving because the are saved implicit by their parent objects.
  private final Set<Class< ? >> ignoreFromSaving = new HashSet<Class< ? >>();

  // This map contains the mapping between the id's of the given xml stream and the new id's given by Hibernate. This is needed for writing
  // the history entries with the new id's.
  private final Map<String, Serializable> entityMapping = new HashMap<String, Serializable>();

  private List<HistoryEntry> historyEntries = new ArrayList<HistoryEntry>();

  private Map<String, Class< ? >> historyClassMapping = new HashMap<String, Class< ? >>();

  private Session session;

  public XStreamSavingConverter() throws HibernateException
  {
    defaultConv = new XStream().getConverterLookup();
    this.ignoreFromSaving.add(PropertyDelta.class);
    this.ignoreFromSaving.add(SimplePropertyDelta.class);
    this.ignoreFromSaving.add(AssociationPropertyDelta.class);
    this.ignoreFromSaving.add(CollectionPropertyDelta.class);
  }

  public void setSession(final Session session)
  {
    this.session = session;
  }

  public Map<Class< ? >, List<Object>> getAllObjects()
  {
    return allObjects;
  }

  public List<HistoryEntry> getHistoryEntries()
  {
    return historyEntries;
  }

  @SuppressWarnings("unchecked")
  @Override
  public boolean canConvert(Class arg0)
  {
    return true;
  }

  public XStreamSavingConverter appendOrderedType(final Class< ? >... types)
  {
    if (types != null) {
      for (final Class< ? > type : types) {
        this.orderOfSaving.add(type);
      }
    }
    return this;
  }

  public XStreamSavingConverter appendIgnoredObjects(final Class< ? >... types)
  {
    if (types != null) {
      for (final Class< ? > type : types) {
        this.ignoreFromSaving.add(type);
      }
    }
    return this;
  }

  public void saveObjects()
  {
    for (final Class< ? > type : orderOfSaving) {
      this.historyClassMapping.put(getClassname4History(type), type);
      save(type);
    }
    for (final Map.Entry<Class< ? >, List<Object>> entry : allObjects.entrySet()) {
      if (entry.getKey().equals(HistoryEntry.class) == true) {
        continue;
      }
      final Class< ? > type = entry.getKey();
      this.historyClassMapping.put(getClassname4History(type), type);
      save(type);
    }
    for (final Class< ? > type : ignoreFromSaving) {
      this.historyClassMapping.put(getClassname4History(type), type);
    }
    save(HistoryEntry.class);
  }

  /**
   * Will be called directly before an object will be saved.
   * @param obj
   * @return id of the inserted objects if saved manually inside this method or null if the object has to be saved by save method (default).
   */
  public Serializable onBeforeSave(final Session session, final Object obj)
  {
    if (obj instanceof HistoryEntry) {
      final HistoryEntry entry = (HistoryEntry) obj;
      final Integer origEntityId = entry.getEntityId();
      final String entityClassname = entry.getClassName();
      final Serializable newId = getNewId(entityClassname, origEntityId);
      final List<PropertyDelta> delta = entry.getDelta();
      Serializable id = null;
      if (newId != null) {
        // No public access, so try this:
        invokeHistorySetter(entry, "setEntityId", Integer.class, newId);
      } else {
        log.error("Can't find mapping of old entity id. This results in a corrupted history: " + entry);
      }
      invokeHistorySetter(entry, "setDelta", List.class, null);
      id = save(entry);
      invokeHistorySetter(entry, "setDelta", List.class, delta);
      for (final PropertyDelta deltaEntry : delta) {
        save(deltaEntry);
      }
      this.historyEntries.add(entry);
      return id;
    }
    return null;
  }

  /**
   * These methods are not public.
   * @param name
   * @param value
   */
  private void invokeHistorySetter(final HistoryEntry entry, final String name, final Class< ? > parameterType, final Object value)
  {
    try {
      final Method method = HistoryEntry.class.getDeclaredMethod(name, parameterType);
      method.setAccessible(true);
      method.invoke(entry, value);
    } catch (IllegalArgumentException ex) {
      log.error("Can't modify id of history entry. This results in a corrupted history: " + entry);
      log.fatal("Exception encountered " + ex, ex);
    } catch (IllegalAccessException ex) {
      log.error("Can't modify id of history entry. This results in a corrupted history: " + entry);
      log.fatal("Exception encountered " + ex, ex);
    } catch (InvocationTargetException ex) {
      log.error("Can't modify id of history entry. This results in a corrupted history: " + entry);
      log.fatal("Exception encountered " + ex, ex);
    } catch (SecurityException ex) {
      log.error("Can't modify id of history entry. This results in a corrupted history: " + entry);
      log.fatal("Exception encountered " + ex, ex);
    } catch (NoSuchMethodException ex) {
      log.error("Can't modify id of history entry. This results in a corrupted history: " + entry);
      log.fatal("Exception encountered " + ex, ex);
    }
  }

  private void save(final Class< ? > type)
  {
    if (ignoreFromSaving.contains(type) == true || writtenObjectTypes.contains(type) == true) {
      // Already written.
      return;
    }
    writtenObjectTypes.add(type);
    // Persistente Klasse?
    if (HibernateUtils.isEntity(type) == false) {
      return;
    }
    if (log.isDebugEnabled() == true) {
      log.debug("Writing objects from type: " + type);
    }
    final List<Object> list = allObjects.get(type);
    if (list == null) {
      return;
    }
    for (final Object obj : list) {
      if (obj instanceof EingangsrechnungsPositionDO) {
        log.info("Eingangsrechnungspositionen: " + obj);
      }
      if (obj == null || writtenObjects.contains(obj) == true) {
        // Object null or already written. Skip this item.
        continue;
      }
      if (session.contains(obj) == true) {
        continue;
      }
      try {
        if (log.isDebugEnabled()) {
          log.debug("Try to write object " + obj);
        }
        Serializable id = onBeforeSave(session, obj);
        if (id == null) {
          id = save(obj);
        }
        if (log.isDebugEnabled() == true) {
          log.debug("wrote object " + obj + " under id " + id);
        }
      } catch (HibernateException ex) {
        log.fatal("Failed to write " + obj + " ex=" + ex, ex);
      } catch (NullPointerException ex) {
        log.fatal("Failed to write " + obj + " ex=" + ex, ex);
      }
    }
  }

  /**
   * Should return the id value of the imported xml object (the origin id of the data-base the dump is from).
   * @param The object with the origin id.
   * @return null if not overridden.
   */
  protected Serializable getOriginalIdentifierValue(final Object obj)
  {
    return null;
  }

  protected Serializable save(final Object obj)
  {
    final Serializable oldId = getOriginalIdentifierValue(obj);
    final Serializable id = session.save(obj);
    if (oldId != null) {
      registerEntityMapping(obj.getClass(), oldId, id);
    }
    writtenObjects.add(obj);
    return id;
  }

  public Class< ? > getClassFromHistoryName(final String classname)
  {
    return this.historyClassMapping.get(classname);
  }

  private String getClassname4History(final Class< ? > cls)
  {
    return ClassUtils.getShortClassName(cls);
  }

  protected void registerEntityMapping(final Class< ? > entityClass, final Serializable oldId, final Serializable newId)
  {
    final Serializable registeredNewId = getNewId(entityClass, oldId);
    if (registeredNewId != null && registeredNewId.equals(newId) == false) {
      log.error("Oups, double entity mapping found for entity '"
          + entityClass
          + "' with old id="
          + oldId
          + " . New id "
          + newId
          + " ignored, using previous stored id "
          + registeredNewId
          + " instead.");
    } else {
      this.entityMapping.put(getClassname4History(entityClass) + oldId, newId);
    }
  }

  protected Serializable getNewId(final Class< ? > entityClass, final Serializable oldId)
  {
    return getNewId(getClassname4History(entityClass), oldId);
  }

  protected Serializable getNewId(final String entityClassname, final Serializable oldId)
  {
    return this.entityMapping.get(entityClassname + oldId);
  }

  public void marshal(Object arg0, HierarchicalStreamWriter arg1, MarshallingContext arg2)
  {
    defaultConv.lookupConverterForType(arg0.getClass()).marshal(arg0, arg1, arg2);
  }

  public Object unmarshal(HierarchicalStreamReader arg0, UnmarshallingContext arg1)
  {
    Object result;
    Class< ? > targetType = null;
    try {
      targetType = arg1.getRequiredType();
      result = defaultConv.lookupConverterForType(targetType).unmarshal(arg0, arg1);
    } catch (final Exception ex) {
      log.warn("Ignore unknown class or property " + targetType + " " + ex.getMessage());
      return null;
    }
    try {
      if (result != null) {
        registerObject(result);
      }
    } catch (HibernateException ex) {
      log.fatal("Failed to write " + result + " ex=" + ex, ex);
    } catch (NullPointerException ex) {
      log.fatal("Failed to write " + result + " ex=" + ex, ex);
    }
    return result;
  }

  private void registerObject(final Object obj)
  {
    if (obj == null) {
      return;
    }
    if (HibernateUtils.isEntity(obj.getClass()) == false) {
      return;
    }
    if (this.ignoreFromSaving.contains(obj.getClass()) == true) {
      // Don't need this objects as "top level" objects in list. They're usually encapsulated.
      return;
    }
    List<Object> list = this.allObjects.get(obj.getClass());
    if (list == null) {
      list = new ArrayList<Object>();
      this.allObjects.put(obj.getClass(), list);
    }
    list.add(obj);
  }
}
