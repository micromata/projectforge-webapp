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

package org.projectforge.database;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TimeZone;

import net.sf.cglib.proxy.Enhancer;

import org.hibernate.EmptyInterceptor;
import org.hibernate.EntityMode;
import org.hibernate.FlushMode;
import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.collection.PersistentBag;
import org.hibernate.collection.PersistentCollection;
import org.hibernate.collection.PersistentList;
import org.hibernate.collection.PersistentMap;
import org.hibernate.collection.PersistentSet;
import org.hibernate.collection.PersistentSortedMap;
import org.hibernate.collection.PersistentSortedSet;
import org.hibernate.metadata.ClassMetadata;
import org.hibernate.proxy.HibernateProxy;
import org.springframework.dao.DataAccessException;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.orm.hibernate3.HibernateTransactionManager;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import com.thoughtworks.xstream.MarshallingStrategy;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.converters.reflection.PureJavaReflectionProvider;
import com.thoughtworks.xstream.converters.reflection.ReflectionConverter;
import com.thoughtworks.xstream.converters.reflection.ReflectionProvider;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import com.thoughtworks.xstream.io.xml.CompactWriter;
import com.thoughtworks.xstream.io.xml.DomDriver;
import com.thoughtworks.xstream.io.xml.PrettyPrintWriter;
import com.thoughtworks.xstream.mapper.Mapper;
import com.thoughtworks.xstream.mapper.MapperWrapper;

import de.micromata.hibernate.dao.HibernateProxyHelper;
import de.micromata.hibernate.history.HistoryEntry;
import de.micromata.hibernate.history.delta.PropertyDelta;
import de.micromata.hibernate.spring.HibernateXmlConverter;
import de.micromata.hibernate.spring.NullWriter;
import de.micromata.hibernate.spring.ProxyIdRefMarshallingStrategy;
import de.micromata.hibernate.spring.XStreamSavingConverter;

/**
 * Hilfsklasse zum Laden und Speichern einer gesamten Hibernate-Datenbank im XML-Format. Zur Darstellung der Daten in XML wird XStream zur
 * Serialisierung eingesetzt. Alle Lazy-Objekte aus Hibernate werden vollständig initialisiert.
 * @author Wolfgang Jung (w.jung@micromata.de)
 * 
 */
public class DatabaseXmlConverter extends HibernateDaoSupport
{
  /** The logger */
  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(HibernateXmlConverter.class);

  /** the wrapper to hibernate */
  private HibernateTemplate hibernate;

  /**
   * Initialisierung der Hibernate-verbindung.
   * 
   * @param hibernate ein bereits initialisiertes HibernateTemplate
   */
  public void setHibernate(HibernateTemplate hibernate)
  {
    this.hibernate = new HibernateTemplate(hibernate.getSessionFactory());
    this.hibernate.setAlwaysUseNewSession(false);
    this.hibernate.setExposeNativeSession(true);
  }

  /**
   * Schreibt alle Objekte der Datenbank in den angegebenen Writer.<br/>
   * <b>Warnung!</b> Bei der Serialisierung von Collections wird derzeit nur {@link java.util.Set} sauber unterstützt.
   * @param writer Ziel für die XML-Datei.
   * @param includeHistory bei false werden die History Einträge nicht geschrieben
   */
  public void dumpDatabaseToXml(final Writer writer, final boolean includeHistory)
  {
    dumpDatabaseToXml(writer, includeHistory, false);
  }

  /**
   * Schreibt alle Objekte der Datenbank in den angegebenen Writer.<br/>
   * <b>Warnung!</b> Bei der Serialisierung von Collections wird derzeit nur {@link java.util.Set} sauber unterstützt.
   * @param writer Ziel für die XML-Datei.
   * @param includeHistory bei false werden die History Einträge nicht geschrieben
   * @param preserveIds If true, the object ids will be preserved, otherwise new ids will be assigned through xstream.
   */
  public void dumpDatabaseToXml(final Writer writer, final boolean includeHistory, final boolean preserveIds)
  {
    final TransactionTemplate tx = new TransactionTemplate(new HibernateTransactionManager(hibernate.getSessionFactory()));
    tx.execute(new TransactionCallback() {
      public Object doInTransaction(final TransactionStatus status)
      {
        hibernate.execute(new HibernateCallback() {
          public Object doInHibernate(Session session) throws HibernateException
          {
            writeObjects(writer, includeHistory, session, preserveIds);
            status.setRollbackOnly();
            return null;
          }
        });
        return null;
      }
    });
  }

  /**
   * Füllt die Datenbank mit den in der XML-Datei angegebenen Objekte. Alle Objekte werden dabei mittels
   * {@link net.sf.hibernate.Session#save(java.lang.Object)} gespeichert, so dass die Datenbank leer sein sollte.
   * @param reader Reader auf eine XML-Datei
   */
  public void fillDatabaseFromXml(final Reader reader)
  {
    final TransactionTemplate tx = new TransactionTemplate(new HibernateTransactionManager(hibernate.getSessionFactory()));
    tx.execute(new TransactionCallback() {
      public Object doInTransaction(final TransactionStatus status)
      {
        SessionFactory sessionFactory = hibernate.getSessionFactory();
        try {
          Session session = sessionFactory.openSession(EmptyInterceptor.INSTANCE);
          session.setFlushMode(FlushMode.AUTO);
          insertObjectsFromStream(reader, session);
          session.flush();
          session.connection().commit();
        } catch (HibernateException ex) {
          log.warn("Failed to load db " + ex, ex);
        } catch (SQLException ex) {
          log.warn("Failed to load db " + ex, ex);
        }
        return null;
      }
    });
  }

  /**
   * @param reader
   * @param session
   * @throws HibernateException
   */
  private void insertObjectsFromStream(final Reader reader, final Session session) throws HibernateException
  {
    log.debug("Loading DB from stream");
    final XStream stream = new XStream(new DomDriver());
    stream.setMode(XStream.ID_REFERENCES);
    Converter save = new XStreamSavingConverter(session);
    stream.registerConverter(save, 10);

    // alle Objekte Laden und speichern
    stream.fromXML(reader);
  }

  /**
   * @param writer
   * @param includeHistory
   * @param session
   * @throws DataAccessException
   * @throws HibernateException
   */
  private void writeObjects(final Writer writer, final boolean includeHistory, Session session, boolean preserveIds)
      throws DataAccessException, HibernateException
  {
    // Container für die Objekte
    List<Object> all = new ArrayList<Object>();
    final XStream stream = initXStream(session, true);
    final XStream defaultXStream = initXStream(session, false);

    session.flush();
    // Alles laden
    List< ? > list = session.createQuery("select o from java.lang.Object o").setReadOnly(true).list();
    for (Iterator< ? > it = list.iterator(); it.hasNext();) {
      Object obj = it.next();
      if (log.isDebugEnabled()) {
        log.debug("loaded object " + obj);
      }
      if ((obj instanceof HistoryEntry || obj instanceof PropertyDelta) && includeHistory == false) {
        continue;
      }
      Hibernate.initialize(obj);
      Class< ? > targetClass = obj.getClass();
      while (Enhancer.isEnhanced(targetClass) == true) {
        targetClass = targetClass.getSuperclass();
      }
      ClassMetadata classMetadata = session.getSessionFactory().getClassMetadata(targetClass);
      if (classMetadata == null) {
        log.fatal("Can't init " + obj + " of type " + targetClass);
        continue;
      }
      // initalisierung des Objekts...
      defaultXStream.marshal(obj, new CompactWriter(new NullWriter()));

      if (preserveIds == false) {
        // Nun kann die ID gelöscht werden
        classMetadata.setIdentifier(obj, null, EntityMode.POJO);
      }
      if (log.isDebugEnabled()) {
        log.debug("loading evicted object " + obj);
      }
      all.add(obj);
    }
    // und schreiben
    try {
      writer.write("<?xml version=\"1.0\" encoding=\"utf-8\" ?>\n");
    } catch (IOException ex) {
      // ignore, will fail on stream.marshal()
    }
    log.info("Wrote " + all.size() + " objects");
    MarshallingStrategy marshallingStrategy = new ProxyIdRefMarshallingStrategy();
    stream.setMarshallingStrategy(marshallingStrategy);
    stream.marshal(all, new PrettyPrintWriter(writer));
  }

  /**
   * @return
   */
  private XStream initXStream(final Session session, boolean nullifyPk)
  {
    final XStream xstream = new XStream() {
      protected MapperWrapper wrapMapper(MapperWrapper next)
      {
        return new HibernateMapper(next);
      }
    };
    final Mapper mapper = xstream.getMapper();
    xstream.registerConverter(new HibernateProxyConverter(mapper, new PureJavaReflectionProvider()), XStream.PRIORITY_VERY_HIGH);
    // xstream.registerConverter(new CollectionConverter(mapper));
    // xstream.registerConverter(new ProxyConverter(mapper));
    xstream.registerConverter(new CollectionConverter(xstream, mapper), 10);
    xstream.registerConverter(new ProxyConverter(xstream, session, nullifyPk), 10);

    xstream.useAttributeFor(TimeZone.class);
    xstream.useAttributeFor(Date.class);
    return xstream;
  }

  /**
   * sanitize hibernate collections, and cure class name
   * @author k.pribluda
   * 
   */
  class HibernateMapper extends MapperWrapper
  {

    final Map<Class< ? >, Class< ? >> collectionMap = new HashMap<Class< ? >, Class< ? >>();

    public void init()
    {
      collectionMap.put(PersistentBag.class, ArrayList.class);
      collectionMap.put(PersistentList.class, ArrayList.class);
      collectionMap.put(PersistentMap.class, HashMap.class);
      collectionMap.put(PersistentSet.class, Set.class);
      collectionMap.put(PersistentSortedMap.class, SortedMap.class);
      collectionMap.put(PersistentSortedSet.class, SortedSet.class);
    }

    public HibernateMapper(final Mapper arg0)
    {
      super(arg0);
      init();
    }

    @SuppressWarnings("unchecked")
    @Override
    public Class defaultImplementationOf(final Class clazz)
    {
      log.debug("checking class:" + clazz);
      if (collectionMap.containsKey(clazz)) {
        log.debug("** substituting " + clazz + " with " + collectionMap.get(clazz));
        return (Class) collectionMap.get(clazz);
      }

      return super.defaultImplementationOf(clazz);
    }

    @SuppressWarnings("unchecked")
    @Override
    public String serializedClass(Class clazz)
    {
      // chekc whether we are hibernate proxy and substitute real name
      for (int i = 0; i < clazz.getInterfaces().length; i++) {
        if (HibernateProxy.class.equals(clazz.getInterfaces()[i])) {
          log.debug("resolving to class name:" + clazz.getSuperclass().getName());
          return clazz.getSuperclass().getName();
        }
      }
      if (collectionMap.containsKey(clazz)) {
        log.debug("** substituting " + clazz + " with " + collectionMap.get(clazz));
        return ((Class) collectionMap.get(clazz)).getName();
      }

      return super.serializedClass(clazz);
    }

  }

  class HibernateProxyConverter extends ReflectionConverter
  {

    public HibernateProxyConverter(Mapper arg0, ReflectionProvider arg1)
    {
      super(arg0, arg1);

    }

    /**
     * be responsible for hibernate proxy
     */
    @SuppressWarnings("unchecked")
    @Override
    public boolean canConvert(Class clazz)
    {
      log.debug("converter says can convert " + clazz + ":" + HibernateProxy.class.isAssignableFrom(clazz));
      return HibernateProxy.class.isAssignableFrom(clazz);
    }

    public void marshal(Object arg0, HierarchicalStreamWriter arg1, MarshallingContext arg2)
    {
      log.debug("converter marshalls: " + ((HibernateProxy) arg0).getHibernateLazyInitializer().getImplementation());
      super.marshal(((HibernateProxy) arg0).getHibernateLazyInitializer().getImplementation(), arg1, arg2);
    }

  }

  class ProxyConverter implements Converter
  {
    private final XStream defaultXStream;

    private final Session session;

    private final boolean nullifyPk;

    public ProxyConverter(XStream defaultXStream, Session session, boolean nullifyPk)
    {
      this.defaultXStream = defaultXStream;
      this.session = session;
      this.nullifyPk = nullifyPk;
    }

    @SuppressWarnings("unchecked")
    public boolean canConvert(Class arg0)
    {
      log.debug("checking " + arg0 + " against HibernateProxy");
      return HibernateProxy.class.isAssignableFrom(arg0);
    }

    public void marshal(Object source, HierarchicalStreamWriter writer, MarshallingContext context)
    {
      try {
        Object obj = HibernateProxyHelper.get(source);
        if (log.isDebugEnabled())
          log.debug("unpack proxy " + source.getClass() + " to " + obj.getClass());
        final Converter converter = defaultXStream.getConverterLookup().lookupConverterForType(obj.getClass());

        if (nullifyPk == true) {
          Class< ? > targetClass = obj.getClass();
          while (Enhancer.isEnhanced(targetClass) == true) {
            targetClass = targetClass.getSuperclass();
          }
          ClassMetadata classMetadata = session.getSessionFactory().getClassMetadata(targetClass);
          if (classMetadata == null) {
            log.fatal("Can't init " + obj + " of type " + targetClass);
          } else {
            if (log.isDebugEnabled())
              log.debug("marshalling object " + obj + " to stream");
            // session.evict(obj);
            classMetadata.setIdentifier(source, null, EntityMode.POJO);
          }
        }
        converter.marshal(obj, writer, context);
      } catch (RuntimeException e) {
        // for debugging purposes ...
        throw e;
      }
    }

    public Object unmarshal(HierarchicalStreamReader arg0, UnmarshallingContext arg1)
    {
      // no unmarshalling
      return null;
    }
  }

  class CollectionConverter implements Converter
  {
    private final Mapper mapper;

    private final XStream defaultXStream;

    public CollectionConverter(final XStream defaultXStream, final Mapper mapper)
    {
      this.defaultXStream = defaultXStream;
      this.mapper = mapper;
    }

    @SuppressWarnings("unchecked")
    public boolean canConvert(Class type)
    {
      log.debug("checking " + type + " against PersistentCollection");
      return PersistentCollection.class.isAssignableFrom(type);
    }

    public void marshal(final Object source, final HierarchicalStreamWriter writer, final MarshallingContext context)
    {
      try {
        final Class< ? > clazz = mapper.defaultImplementationOf(source.getClass());
        if (log.isDebugEnabled())
          log.debug("Using " + clazz + " for " + source.getClass());
        final Converter converter = defaultXStream.getConverterLookup().lookupConverterForType(clazz);
        if (source instanceof PersistentCollection) {
          PersistentCollection set = (PersistentCollection) source;
          set.forceInitialization();
        }
        converter.marshal(source, writer, context);
      } catch (RuntimeException e) {
        // for debugging purposes ...
        throw e;
      }
    }

    public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context)
    {
      // no unmarshalling
      return null;
    }
  }
  // class HibernateCollectionConverter extends CollectionConverter
  // {
  // HibernateCollectionConverter(Mapper mapper)
  // {
  // super(mapper);
  // }
  //
  // public boolean canConvert(Class type)
  // {
  // return super.canConvert(type) || type == List.class || type == Set.class;
  // }
  // }
  //
  // class HibernateMapConverter extends MapConverter
  // {
  //
  // HibernateMapConverter(Mapper mapper)
  // {
  // super(mapper);
  // }
  //
  // public boolean canConvert(Class type)
  // {
  // return super.canConvert(type) || type == Map.class;
  // }
  // }
}
