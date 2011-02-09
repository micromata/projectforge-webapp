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

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.sf.cglib.proxy.Enhancer;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.PredicateUtils;
import org.hibernate.EmptyInterceptor;
import org.hibernate.EntityMode;
import org.hibernate.FlushMode;
import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.metadata.ClassMetadata;
import org.springframework.dao.DataAccessException;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.orm.hibernate3.HibernateTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import com.thoughtworks.xstream.MarshallingStrategy;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.reflection.PureJavaReflectionProvider;
import com.thoughtworks.xstream.io.xml.CompactWriter;
import com.thoughtworks.xstream.io.xml.DomDriver;
import com.thoughtworks.xstream.io.xml.PrettyPrintWriter;
import com.thoughtworks.xstream.mapper.MapperWrapper;

import de.micromata.hibernate.history.HistoryEntry;
import de.micromata.hibernate.history.delta.PropertyDelta;
import de.micromata.hibernate.spring.NullWriter;
import de.micromata.hibernate.spring.ProxyIdRefMarshallingStrategy;

/**
 * Hilfsklasse zum Laden und Speichern einer gesamten Hibernate-Datenbank im XML-Format. Zur Darstellung der Daten in XML wird XStream zur
 * Serialisierung eingesetzt. Alle Lazy-Objekte aus Hibernate werden vollständig initialisiert. http://jira.codehaus.org/browse/XSTR-377
 * 
 * @author Wolfgang Jung (w.jung@micromata.de)
 * 
 */
public class HibernateXmlConverter
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
    dumpDatabaseToXml(writer, includeHistory, true);
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
    TransactionTemplate tx = new TransactionTemplate(new HibernateTransactionManager(hibernate.getSessionFactory()));
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
  public void fillDatabaseFromXml(final Reader reader, final XStreamSavingConverter xstreamSavingConverter)
  {
    TransactionTemplate tx = new TransactionTemplate(new HibernateTransactionManager(hibernate.getSessionFactory()));
    tx.execute(new TransactionCallback() {
      public Object doInTransaction(final TransactionStatus status)
      {
        SessionFactory sessionFactory = hibernate.getSessionFactory();
        try {
          Session session = sessionFactory.openSession(EmptyInterceptor.INSTANCE);
          session.setFlushMode(FlushMode.AUTO);
          insertObjectsFromStream(reader, session, xstreamSavingConverter);
        } catch (HibernateException ex) {
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
  private void insertObjectsFromStream(final Reader reader, final Session session, final XStreamSavingConverter xstreamSavingConverter) throws HibernateException
  {
    log.debug("Loading DB from stream");
    final XStream xstream = new XStream(new DomDriver());
    xstream.setMode(XStream.ID_REFERENCES);
    xstreamSavingConverter.setSession(session);
    xstream.registerConverter(xstreamSavingConverter, 10);
    // alle Objekte Laden und speichern
    xstream.fromXML(reader);
    xstreamSavingConverter.saveObjects();
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
    list = (List< ? >) CollectionUtils.select(list, PredicateUtils.uniquePredicate());
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
      @Override
      protected MapperWrapper wrapMapper(MapperWrapper next)
      {
        return new HibernateMapper(new HibernateCollectionsMapper(next));
      }
    };

    // Converter für die Hibernate-Collections
    xstream.registerConverter(new HibernateCollectionConverter(xstream.getConverterLookup()));
    xstream.registerConverter(new HibernateProxyConverter(xstream.getMapper(), new PureJavaReflectionProvider(), xstream
        .getConverterLookup()), XStream.PRIORITY_VERY_HIGH);
    xstream.setMarshallingStrategy(new XStreamMarshallingStrategy(XStreamMarshallingStrategy.RELATIVE));
    return xstream;
  }
}
