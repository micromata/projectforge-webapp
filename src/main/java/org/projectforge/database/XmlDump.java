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

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.Validate;
import org.apache.log4j.Logger;
import org.hibernate.EmptyInterceptor;
import org.hibernate.FlushMode;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.projectforge.access.AccessEntryDO;
import org.projectforge.access.GroupTaskAccessDO;
import org.projectforge.database.xstream.HibernateXmlConverter;
import org.projectforge.database.xstream.XStreamSavingConverter;
import org.projectforge.fibu.AbstractRechnungDO;
import org.projectforge.fibu.AbstractRechnungsPositionDO;
import org.projectforge.fibu.AuftragDO;
import org.projectforge.fibu.AuftragsPositionDO;
import org.projectforge.fibu.EingangsrechnungDO;
import org.projectforge.fibu.EingangsrechnungsPositionDO;
import org.projectforge.fibu.EmployeeSalaryDO;
import org.projectforge.fibu.KundeDO;
import org.projectforge.fibu.ProjektDO;
import org.projectforge.fibu.RechnungDO;
import org.projectforge.fibu.RechnungsPositionDO;
import org.projectforge.fibu.kost.Kost1DO;
import org.projectforge.fibu.kost.Kost2ArtDO;
import org.projectforge.fibu.kost.Kost2DO;
import org.projectforge.fibu.kost.KostZuweisungDO;
import org.projectforge.task.TaskDO;
import org.projectforge.user.GroupDO;
import org.projectforge.user.PFUserDO;
import org.projectforge.user.UserPrefDO;
import org.projectforge.user.UserPrefEntryDO;
import org.projectforge.user.UserRightDO;
import org.springframework.core.io.ClassPathResource;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.orm.hibernate3.HibernateTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

import de.micromata.hibernate.history.HistoryEntry;
import de.micromata.hibernate.history.delta.CollectionPropertyDelta;
import de.micromata.hibernate.history.delta.PropertyDelta;
import de.micromata.hibernate.history.delta.SimplePropertyDelta;

/**
 * 
 * @author Kai Reinhard (k.reinhard@micromata.de)
 * 
 */
public class XmlDump
{
  private static final Logger log = Logger.getLogger(XmlDump.class);

  private static final String XML_DUMP_FILENAME = System.getProperty("user.home") + "/tmp/database-dump.xml.gz";

  private HibernateTemplate hibernate;

  protected TransactionTemplate tx;

  public HibernateTemplate getHibernate()
  {
    Validate.notNull(hibernate);
    return hibernate;
  }

  public void setHibernate(HibernateTemplate hibernate)
  {
    this.hibernate = hibernate;
    tx = new TransactionTemplate(new HibernateTransactionManager(hibernate.getSessionFactory()));
  }

  public TransactionTemplate getTx()
  {
    Validate.notNull(tx);
    return tx;
  }

  public void setTx(TransactionTemplate tx)
  {
    this.tx = tx;
  }

  public void restoreDatabase()
  {
    try {
      restoreDatabase(new InputStreamReader(new FileInputStream(XML_DUMP_FILENAME), "utf-8"));
    } catch (UnsupportedEncodingException ex) {
      log.error(ex.getMessage(), ex);
      throw new RuntimeException(ex);
    } catch (FileNotFoundException ex) {
      log.error(ex.getMessage(), ex);
      throw new RuntimeException(ex);
    }
  }

  public void restoreDatabase(Reader reader)
  {
    final XStreamSavingConverter xstreamSavingConverter = new XStreamSavingConverter() {
      @Override
      public Serializable onBeforeSave(final Session session, final Object obj)
      {
        if (obj instanceof UserRightDO) {
          ((UserRightDO) obj).setUser(null);
        } else if (obj instanceof AbstractRechnungDO<?>) {
          final AbstractRechnungDO<?> rechnung = (AbstractRechnungDO<?>) obj;
          final Serializable id = session.save(rechnung);
          if (rechnung.getPositionen() != null) {
            for (final AbstractRechnungsPositionDO pos : rechnung.getPositionen()) {
              save(pos);
              if (pos.getKostZuweisungen() != null) {
                for (final KostZuweisungDO zuweisung : pos.getKostZuweisungen()) {
                  save(zuweisung);
                }
              }
            }
          }
          return id;
        }
        return null;
      }
    };
    // UserRightDO is inserted on cascade while inserting PFUserDO.
    xstreamSavingConverter.appendIgnoredObjects(UserRightDO.class, EingangsrechnungsPositionDO.class, HistoryEntry.class,
        PropertyDelta.class, SimplePropertyDelta.class, CollectionPropertyDelta.class);
    xstreamSavingConverter.appendOrderedType(PFUserDO.class, GroupDO.class, TaskDO.class, KundeDO.class, ProjektDO.class, Kost1DO.class,
        Kost2ArtDO.class, Kost2DO.class, AuftragDO.class, AuftragsPositionDO.class, //
        RechnungDO.class, RechnungsPositionDO.class, EingangsrechnungDO.class, //
        EmployeeSalaryDO.class, KostZuweisungDO.class,//
        UserPrefEntryDO.class, UserPrefDO.class, //
        GroupTaskAccessDO.class, AccessEntryDO.class);
    try {
      final SessionFactory sessionFactory = hibernate.getSessionFactory();
      final Session session = sessionFactory.openSession(EmptyInterceptor.INSTANCE);
      session.setFlushMode(FlushMode.AUTO);
      final Transaction transaction = session.beginTransaction();
      final XStream xstream = new XStream(new DomDriver());
      xstream.setMode(XStream.ID_REFERENCES);
      xstreamSavingConverter.setSession(session);
      xstream.registerConverter(xstreamSavingConverter, 10);
      // alle Objekte Laden und speichern
      xstream.fromXML(reader);

      xstreamSavingConverter.saveObjects();
      transaction.commit();
    } catch (Exception ex) {
      log.error(ex.getMessage(), ex);
      throw new RuntimeException(ex);
    } finally {
      IOUtils.closeQuietly(reader);
    }
    // {
    // final SessionFactory sessionFactory = hibernate.getSessionFactory();
    // final Session session = sessionFactory.openSession(EmptyInterceptor.INSTANCE);
    // session.setFlushMode(FlushMode.AUTO);
    // for (final GroupDO origGroup : groups) {
    // if (origGroup.getAssignedUsers() == null) {
    // continue;
    // }
    // final GroupDO group = (GroupDO)session.load(GroupDO.class, origGroup.getId(), LockOptions.UPGRADE);
    // // Users are not added automatically (cascade doesn't work for, why?):
    // for (final PFUserDO assignedUser : origGroup.getAssignedUsers()) {
    // group.addUser(assignedUser);
    // }
    // groupDao.internalUpdate(group);
    // }
    // }
  }

  public void restoreDatabaseFromClasspathResource(String path, String encoding)
  {
    ClassPathResource cpres = new ClassPathResource(path);
    Reader reader;
    try {
      InputStream in;
      if (path.endsWith(".gz") == true) {
        in = new GZIPInputStream(cpres.getInputStream());
      } else {
        in = cpres.getInputStream();
      }
      reader = new InputStreamReader(in, encoding);
    } catch (IOException ex) {
      log.error(ex.getMessage(), ex);
      throw new RuntimeException(ex);
    }
    restoreDatabase(reader);
  }

  public void dumpDatabase()
  {
    dumpDatabase(XML_DUMP_FILENAME, "utf-8");
  }

  /**
   * 
   * @param filename virtual filename: If the filename suffix is "gz" then the dump will be compressed.
   * @param out
   */
  public void dumpDatabase(String filename, OutputStream out)
  {
    HibernateXmlConverter converter = new HibernateXmlConverter();
    converter.setHibernate(hibernate);
    Writer writer = null;
    GZIPOutputStream gzipOut = null;
    try {
      if (filename.endsWith(".gz") == true) {
        gzipOut = new GZIPOutputStream(out);
        writer = new OutputStreamWriter(gzipOut, "utf-8");
      } else {
        writer = new OutputStreamWriter(out, "utf-8");
      }
      converter.dumpDatabaseToXml(writer, true); // history=false, preserveIds=true
    } catch (IOException ex) {
      log.error(ex.getMessage(), ex);
    } finally {
      IOUtils.closeQuietly(gzipOut);
      IOUtils.closeQuietly(writer);
    }
  }

  public void dumpDatabase(String path, String encoding)
  {
    OutputStream out = null;
    try {
      out = new FileOutputStream(path);
      dumpDatabase(path, out);
    } catch (IOException ex) {
      log.error(ex.getMessage(), ex);
    } finally {
      IOUtils.closeQuietly(out);
    }
  }
}
