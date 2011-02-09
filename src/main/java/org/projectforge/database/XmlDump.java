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
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.projectforge.database.xstream.HibernateXmlConverter;
import org.projectforge.database.xstream.XStreamSavingConverter;
import org.projectforge.fibu.AuftragDO;
import org.projectforge.fibu.AuftragsPositionDO;
import org.projectforge.fibu.EingangsrechnungDO;
import org.projectforge.fibu.KundeDO;
import org.projectforge.fibu.ProjektDO;
import org.projectforge.fibu.RechnungDO;
import org.projectforge.fibu.kost.Kost1DO;
import org.projectforge.fibu.kost.Kost2ArtDO;
import org.projectforge.fibu.kost.Kost2DO;
import org.projectforge.task.TaskDO;
import org.projectforge.user.GroupDO;
import org.projectforge.user.PFUserDO;
import org.springframework.core.io.ClassPathResource;

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

  private HibernateXmlConverter hibernateXmlConverter;

  public void setHibernateXmlConverter(HibernateXmlConverter hibernateXmlConverter)
  {
    this.hibernateXmlConverter = hibernateXmlConverter;
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
    final List<GroupDO> groups = new ArrayList<GroupDO>();
    final XStreamSavingConverter xstreamSavingConverter = new XStreamSavingConverter() {
      @Override
      public Serializable onBeforeSave(final Session session, final Object obj)
      {
        if (GroupDO.class.isAssignableFrom(obj.getClass())) {
          groups.add((GroupDO) obj);
          // final GroupDO origGroup= (GroupDO)obj;
          // final Set<PFUserDO> assignedUsers = origGroup.getAssignedUsers();
          // if (assignedUsers == null || assignedUsers.size() == 0) {
          // // Nothing to do manually.
          // return null;
          // }
          // // No we've to initialize the set of assigned users.
          // final Serializable id = session.save(origGroup);
          // final GroupDO group = (GroupDO)session.get(GroupDO.class, id, LockOptions.READ);
          // // Users are not added automatically (cascade doesn't work for, why?):
          // for (final PFUserDO assignedUser : assignedUsers) {
          // final PFUserDO dbUser = (PFUserDO)session.load(PFUserDO.class, assignedUser.getId());
          // group.addUser(dbUser);
          // }
          // session.merge(group);
          // return id;
        }
        return null;
      }
    };
    xstreamSavingConverter.appendIgnoredObjects(PropertyDelta.class, SimplePropertyDelta.class, CollectionPropertyDelta.class);
    xstreamSavingConverter.appendOrderedType(PFUserDO.class, GroupDO.class, TaskDO.class, KundeDO.class, ProjektDO.class, Kost1DO.class,
        Kost2ArtDO.class, Kost2DO.class, AuftragDO.class, AuftragsPositionDO.class, RechnungDO.class, EingangsrechnungDO.class,
        HistoryEntry.class);
    try {
      hibernateXmlConverter.fillDatabaseFromXml(reader, xstreamSavingConverter);
    } catch (Exception ex) {
      log.error(ex.getMessage(), ex);
     // throw new RuntimeException(ex);
    } finally {
      IOUtils.closeQuietly(reader);
    }
    showNumberOfAssignedUsers("after import", groups);
    {
//      final SessionFactory sessionFactory = hibernate.getSessionFactory();
//      final Session session = sessionFactory.openSession(EmptyInterceptor.INSTANCE);
//      session.setFlushMode(FlushMode.AUTO);
//      for (final GroupDO origGroup : groups) {
//        if (origGroup.getAssignedUsers() == null) {
//          continue;
//        }
//        final GroupDO group = (GroupDO) session.get(GroupDO.class, origGroup.getId(), LockOptions.READ);
//        // Users are not added automatically (cascade doesn't work for, why?):
//        for (final PFUserDO assignedUser : origGroup.getAssignedUsers()) {
//          final PFUserDO dbUser = (PFUserDO) session.load(PFUserDO.class, assignedUser.getId());
//          group.addUser(dbUser);
//        }
//        session.merge(group);
//      }
//      session.close();
    }
    showNumberOfAssignedUsers("after session.merge() fix", groups);
//    {
//      for (final GroupDO origGroup : groups) {
//        if (origGroup.getAssignedUsers() == null) {
//          continue;
//        }
//        final GroupDO group = groupDao.internalGetById(origGroup.getId());
//        // Users are not added automatically (cascade doesn't work for, why?):
//        for (final PFUserDO assignedUser : origGroup.getAssignedUsers()) {
//          final PFUserDO dbUser = (PFUserDO) userDao.internalGetById(assignedUser.getId());
//          group.addUser(dbUser);
//        }
//        groupDao.internalUpdate(group);
//      }
//    }
//    showNumberOfAssignedUsers("after groupDao fix", groups);
  }

  private void showNumberOfAssignedUsers(final String text, final List<GroupDO> groups)
  {
//    final SessionFactory sessionFactory = hibernate.getSessionFactory();
//    final Session session = sessionFactory.openSession(EmptyInterceptor.INSTANCE);
//    session.setFlushMode(FlushMode.AUTO);
//    for (final GroupDO origGroup : groups) {
//      if (origGroup.getAssignedUsers() == null) {
//        continue;
//      }
//      final GroupDO group = (GroupDO) session.get(GroupDO.class, origGroup.getId(), LockOptions.READ);
//      log.info(text + ": Assigned users " + group.getAssignedUsers().size());
//    }
//    session.close();
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
    Writer writer = null;
    GZIPOutputStream gzipOut = null;
    try {
      if (filename.endsWith(".gz") == true) {
        gzipOut = new GZIPOutputStream(out);
        writer = new OutputStreamWriter(gzipOut, "utf-8");
      } else {
        writer = new OutputStreamWriter(out, "utf-8");
      }
      hibernateXmlConverter.dumpDatabaseToXml(writer, true); // history=false, preserveIds=true
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
