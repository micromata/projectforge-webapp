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

package org.projectforge.database.xstream;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.projectforge.core.BaseDO;
import org.projectforge.database.HibernateUtils;
import org.projectforge.fibu.EingangsrechnungsPositionDO;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.ConverterLookup;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

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

  private final Set<Class< ? >> ignoreFromSaving = new HashSet<Class< ? >>();

  private Session session;

  private Integer highestId = 0;

  public XStreamSavingConverter() throws HibernateException
  {
    defaultConv = new XStream().getConverterLookup();
  }

  public void setSession(final Session session)
  {
    this.session = session;
  }

  public Integer getHighestId()
  {
    return highestId;
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
      save(type);
    }
    for (final Map.Entry<Class< ? >, List<Object>> entry : allObjects.entrySet()) {
      save(entry.getKey());
    }
  }

  /**
   * Will be called directly before an object will be saved.
   * @param obj
   * @return id of the inserted objects if saved manually inside this method or null if the object has to be saved by save method (default).
   */
  public Serializable onBeforeSave(final Session session, final Object obj)
  {
    return null;
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

  protected Serializable save(final Object obj)
  {
    final Serializable id = session.save(obj);
    writtenObjects.add(obj);
    return id;
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
    if (obj instanceof BaseDO< ? >) {
      final Serializable id = ((BaseDO< ? >) obj).getId();
      if (id instanceof Integer && (Integer) id > highestId) {
        highestId = (Integer) id;
      }
    }
    List<Object> list = this.allObjects.get(obj.getClass());
    if (list == null) {
      list = new ArrayList<Object>();
      this.allObjects.put(obj.getClass(), list);
    }
    list.add(obj);
  }
}