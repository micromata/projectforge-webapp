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

package org.projectforge.database;

import java.io.File;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.ClassUtils;
import org.hibernate.CacheMode;
import org.hibernate.Criteria;
import org.hibernate.FlushMode;
import org.hibernate.ScrollMode;
import org.hibernate.ScrollableResults;
import org.hibernate.Session;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.search.FullTextSession;
import org.hibernate.search.Search;
import org.projectforge.access.GroupTaskAccessDO;
import org.projectforge.address.AddressDO;
import org.projectforge.book.BookDO;
import org.projectforge.calendar.DayHolder;
import org.projectforge.core.Configuration;
import org.projectforge.core.ConfigurationDO;
import org.projectforge.core.ExtendedBaseDO;
import org.projectforge.core.ReindexSettings;
import org.projectforge.fibu.AuftragDO;
import org.projectforge.fibu.AuftragsPositionDO;
import org.projectforge.fibu.BankAccountBalanceDO;
import org.projectforge.fibu.BankAccountDO;
import org.projectforge.fibu.BankAccountingEntryDO;
import org.projectforge.fibu.EingangsrechnungDO;
import org.projectforge.fibu.EingangsrechnungsPositionDO;
import org.projectforge.fibu.EmployeeDO;
import org.projectforge.fibu.EmployeeSalaryDO;
import org.projectforge.fibu.KontoDO;
import org.projectforge.fibu.KundeDO;
import org.projectforge.fibu.ProjektDO;
import org.projectforge.fibu.RechnungDO;
import org.projectforge.fibu.RechnungsPositionDO;
import org.projectforge.fibu.kost.BuchungssatzDO;
import org.projectforge.fibu.kost.Kost1DO;
import org.projectforge.fibu.kost.Kost2ArtDO;
import org.projectforge.fibu.kost.Kost2DO;
import org.projectforge.fibu.kost.KostZuweisungDO;
import org.projectforge.gantt.GanttChartDO;
import org.projectforge.humanresources.HRPlanningDO;
import org.projectforge.humanresources.HRPlanningEntryDO;
import org.projectforge.meb.MebEntryDO;
import org.projectforge.orga.ContractDO;
import org.projectforge.orga.PostausgangDO;
import org.projectforge.orga.PosteingangDO;
import org.projectforge.scripting.ScriptDO;
import org.projectforge.task.TaskDO;
import org.projectforge.timesheet.TimesheetDO;
import org.projectforge.user.GroupDO;
import org.projectforge.user.PFUserDO;
import org.projectforge.user.UserPrefDO;
import org.projectforge.user.UserRightDO;
import org.projectforge.web.calendar.DateTimeFormatter;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Creates index creation script and re-indexes data-base.
 * @author Kai Reinhard (k.reinhard@micromata.de)
 * 
 */
@Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
public class DatabaseDao extends HibernateDaoSupport
{
  private static final int MIN_REINDEX_ENTRIES_4_USE_SCROLL_MODE = 2000;

  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(DatabaseDao.class);

  private Date currentReindexRun = null;

  /**
   * Since yesterday and 1,000 newest entries at maximimum.
   * @return
   */
  public static ReindexSettings createReindexSettings(final boolean onlyNewest)
  {
    if (onlyNewest == true) {
      final DayHolder day = new DayHolder();
      day.add(Calendar.DAY_OF_MONTH, -1); // Since yesterday:
      return new ReindexSettings(day.getDate(), 1000); // Maximum 1,000 newest entries.
    } else {
      return new ReindexSettings();
    }
  }

  @Transactional(readOnly = false, propagation = Propagation.REQUIRES_NEW, isolation = Isolation.REPEATABLE_READ)
  public String rebuildDatabaseSearchIndices()
  {
    return rebuildDatabaseSearchIndices(new ReindexSettings());
  }

  @Transactional(readOnly = false, propagation = Propagation.REQUIRES_NEW, isolation = Isolation.REPEATABLE_READ)
  public String rebuildDatabaseSearchIndices(final Class< ? > clazz, final ReindexSettings settings)
  {
    if (currentReindexRun != null) {
      return "Another re-index job is already running. The job was started at: "
          + DateTimeFormatter.instance().getFormattedDateTime(currentReindexRun);
    }
    synchronized (this) {
      try {
        currentReindexRun = new Date();
        StringBuffer buf = new StringBuffer();
        reindex(clazz, settings, buf);
        return buf.toString();
      } finally {
        currentReindexRun = null;
      }
    }
  }

  @Transactional(readOnly = false, propagation = Propagation.REQUIRES_NEW, isolation = Isolation.REPEATABLE_READ)
  public String rebuildDatabaseSearchIndices(final ReindexSettings settings)
  {
    if (currentReindexRun != null) {
      return "Another re-index job is already running. The job was started at: "
          + DateTimeFormatter.instance().getFormattedDateTime(currentReindexRun);
    }
    synchronized (this) {
      try {
        currentReindexRun = new Date();
        StringBuffer buf = new StringBuffer();
        reindex(PFUserDO.class, settings, buf);
        reindex(GroupDO.class, settings, buf);
        reindex(TaskDO.class, settings, buf); // needs PFUserDO
        reindex(KontoDO.class, settings, buf);
        reindex(KostZuweisungDO.class, settings, buf);
        reindex(Kost1DO.class, settings, buf);
        reindex(Kost2DO.class, settings, buf);
        reindex(Kost2ArtDO.class, settings, buf);
        reindex(KundeDO.class, settings, buf);
        reindex(ProjektDO.class, settings, buf);

        reindex(AddressDO.class, settings, buf);
        reindex(AuftragDO.class, settings, buf); // Needs Kunde, Projekt, User
        reindex(AuftragsPositionDO.class, settings, buf); // Needs AuftragDO
        reindex(BankAccountDO.class, settings, buf);
        reindex(BankAccountingEntryDO.class, settings, buf);
        reindex(BankAccountBalanceDO.class, settings, buf);
        reindex(BookDO.class, settings, buf);
        reindex(BuchungssatzDO.class, settings, buf); // Needs Konto, Kost1, Kost2
        reindex(ConfigurationDO.class, settings, buf);
        reindex(ContractDO.class, settings, buf);
        reindex(EingangsrechnungDO.class, settings, buf);
        reindex(EingangsrechnungsPositionDO.class, settings, buf);
        reindex(EmployeeDO.class, settings, buf);
        reindex(EmployeeSalaryDO.class, settings, buf);
        reindex(GanttChartDO.class, settings, buf);
        reindex(GroupTaskAccessDO.class, settings, buf);
        reindex(HRPlanningDO.class, settings, buf);
        reindex(HRPlanningEntryDO.class, settings, buf);
        reindex(MebEntryDO.class, settings, buf);
        reindex(PostausgangDO.class, settings, buf);
        reindex(PosteingangDO.class, settings, buf);
        reindex(RechnungDO.class, settings, buf); // Needs Kunde, Projekt
        reindex(RechnungsPositionDO.class, settings, buf); // Needs Kunde, Projekt
        reindex(ScriptDO.class, settings, buf);
        reindex(TimesheetDO.class, settings, buf);
        reindex(UserRightDO.class, settings, buf);
        reindex(UserPrefDO.class, settings, buf);

        return buf.toString();
      } finally {
        currentReindexRun = null;
      }
    }
  }

  private void reindex(final Class< ? > clazz, final ReindexSettings settings, final StringBuffer buf)
  {
    buf.append(ClassUtils.getShortClassName(clazz));
    final File file = new File(Configuration.getInstance().getApplicationHomeDir() + "/hibernate-search/" + clazz.getName() + "/write.lock");
    if (file.exists() == true) {
      final Date lastModified = new Date(file.lastModified());
      final String message;
      if (System.currentTimeMillis() - file.lastModified() > 60000) { // Last modified date is older than 60 seconds.
        message = "(*** write.lock with last modification '"
            + DateTimeFormatter.instance().getFormattedDateTime(lastModified)
            + "' exists (skip re-index). May-be your admin should delete this file (see log). ***)";
        log.error(file.getAbsoluteFile() + " " + message);
      } else {
        message = "(*** write.lock temporarily exists (skip re-index). ***)";
        log.info(file.getAbsolutePath() + " " + message);
      }
      buf.append(" ").append(message);
    } else {
      reindex(clazz, settings);
    }
    buf.append(", ");
  }

  /**
   * 
   * @param clazz
   */
  @Transactional(readOnly = false, propagation = Propagation.REQUIRES_NEW, isolation = Isolation.REPEATABLE_READ)
  private void reindex(final Class< ? > clazz, final ReindexSettings settings)
  {
    final Session session = getSession();
    Criteria criteria = createCriteria(session, clazz, settings, true);
    final Long number = (Long) criteria.uniqueResult(); // Get number of objects to re-index (select count(*) from).
    final boolean scrollMode = number > MIN_REINDEX_ENTRIES_4_USE_SCROLL_MODE ? true : false;
    log.info("Starting re-indexing of "
        + number
        + " entries  (total number) of type "
        + clazz.getName()
        + " with scrollMode="
        + scrollMode
        + "...");
    final int batchSize = 1000;// NumberUtils.createInteger(System.getProperty("hibernate.search.worker.batch_size")
    final FullTextSession fullTextSession = Search.getFullTextSession(session);
    fullTextSession.setFlushMode(FlushMode.MANUAL);
    fullTextSession.setCacheMode(CacheMode.IGNORE);
    int index = 0;
    if (scrollMode == true) {
      // Scroll-able results will avoid loading too many objects in memory
      criteria = createCriteria(fullTextSession, clazz, settings, false);
      final ScrollableResults results = criteria.scroll(ScrollMode.FORWARD_ONLY);
      while (results.next() == true) {
        final Object obj = results.get(0);
        if (obj instanceof ExtendedBaseDO< ? >) {
          ((ExtendedBaseDO< ? >) obj).recalculate();
        }
        fullTextSession.index(obj); // index each element
        if (index++ % batchSize == 0)
          session.flush(); // clear every batchSize since the queue is processed
      }
    } else {
      criteria = createCriteria(session, clazz, settings, false);
      final List< ? > list = criteria.list();
      for (final Object obj : list) {
        if (obj instanceof ExtendedBaseDO< ? >) {
          ((ExtendedBaseDO< ? >) obj).recalculate();
        }
        fullTextSession.index(obj);
        if (index++ % batchSize == 0)
          session.flush(); // clear every batchSize since the queue is processed
      }
    }
    log.info("Re-indexing of " + index + " objects of type " + clazz.getName() + " done.");
  }

  private Criteria createCriteria(final Session session, final Class< ? > clazz, final ReindexSettings settings, final boolean rowCount)
  {
    final Criteria criteria = session.createCriteria(clazz);
    if (rowCount == true) {
      criteria.setProjection(Projections.rowCount());
    } else {
      if (settings.getLastNEntries() != null) {
        criteria.addOrder(Order.desc("lastUpdate")).setMaxResults(settings.getLastNEntries());
      }
      if (settings.getFromDate() != null) {
        criteria.add(Restrictions.ge("lastUpdate", settings.getFromDate()));
      }
    }
    return criteria;
  }
}
