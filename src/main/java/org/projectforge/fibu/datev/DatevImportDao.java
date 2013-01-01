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

package org.projectforge.fibu.datev;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;

import org.apache.commons.lang.Validate;
import org.hibernate.LockMode;
import org.projectforge.access.AccessChecker;
import org.projectforge.access.AccessException;
import org.projectforge.common.ImportStatus;
import org.projectforge.common.ImportStorage;
import org.projectforge.common.ImportedElement;
import org.projectforge.common.ImportedSheet;
import org.projectforge.core.ActionLog;
import org.projectforge.core.UserException;
import org.projectforge.fibu.KontoDO;
import org.projectforge.fibu.KontoDao;
import org.projectforge.fibu.KostFormatter;
import org.projectforge.fibu.kost.BuchungssatzDO;
import org.projectforge.fibu.kost.BuchungssatzDao;
import org.projectforge.fibu.kost.Kost1DO;
import org.projectforge.fibu.kost.Kost1Dao;
import org.projectforge.fibu.kost.Kost2DO;
import org.projectforge.fibu.kost.Kost2Dao;
import org.projectforge.user.UserRightId;
import org.projectforge.user.UserRightValue;
import org.projectforge.xls.ExcelImportException;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
public class DatevImportDao extends HibernateDaoSupport
{
  /**
   * Size of bulk inserts. If this value is too large, exceptions are expected and as more small the value is so as more slowly is the
   * insert process.
   */
  private static final int BUCHUNGSSATZ_INSERT_BLOCK_SIZE = 50;

  /**
   * Size of bulk inserts. If this value is too large, exceptions are expected and as more small the value is so as more slowly is the
   * insert process.
   */
  private static final int KONTO_INSERT_BLOCK_SIZE = 50;

  public enum Type
  {
    KONTENPLAN, BUCHUNGSSAETZE
  }

  public static final UserRightId USER_RIGHT_ID = UserRightId.FIBU_DATEV_IMPORT;

  static final String[] KONTO_DIFF_PROPERTIES = { "nummer", "bezeichnung"};

  static final String[] BUCHUNGSSATZ_DIFF_PROPERTIES = { "satznr", "betrag", "sh", "konto", "kost2", "menge", "beleg", "datum",
    "gegenKonto", "text", "kost1", "comment"};

  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(DatevImportDao.class);

  private AccessChecker accessChecker;

  private KontoDao kontoDao;

  private Kost1Dao kost1Dao;

  private Kost2Dao kost2Dao;

  private BuchungssatzDao buchungssatzDao;

  /**
   * Has the user the right FIBU_DATEV_IMPORT (value true)?
   * @param accessChecker
   * @see UserRightId#FIBU_DATEV_IMPORT
   * @see AccessChecker#hasRight(UserRightId, UserRightValue, boolean)
   */
  public static boolean hasRight(final AccessChecker accessChecker)
  {
    return hasRight(accessChecker, false);
  }

  /**
   * Has the user the right FIBU_DATEV_IMPORT (value true)?
   * @param accessChecker
   * @throws AccessException
   * @see UserRightId#FIBU_DATEV_IMPORT
   * @see AccessChecker#hasRight(UserRightId, UserRightValue, boolean)
   */
  public static boolean checkLoggeinUserRight(final AccessChecker accessChecker)
  {
    return hasRight(accessChecker, true);
  }

  private static boolean hasRight(final AccessChecker accessChecker, final boolean throwException)
  {
    return accessChecker.hasLoggedInUserRight(USER_RIGHT_ID, throwException, UserRightValue.TRUE);
  }

  /**
   * Liest den Kontenplan aus dem InputStream (Exceltabelle) und schreibt die gelesenen Werte des Kontenplans in ImportStorge. Der User muss
   * der FINANCE_GROUP angehören, um diese Funktionalität ausführen zu können.
   * @param is
   * @param filename
   * @return ImportStorage mit den gelesenen Daten.
   * @throws Exception
   */
  public ImportStorage<KontoDO> importKontenplan(final InputStream is, final String filename, final ActionLog actionLog) throws Exception
  {
    checkLoggeinUserRight(accessChecker);
    log.info("importKontenplan called");
    final ImportStorage<KontoDO> storage = new ImportStorage<KontoDO>(Type.KONTENPLAN);
    storage.setFilename(filename);
    final KontenplanExcelImporter imp = new KontenplanExcelImporter();
    imp.doImport(storage, is, actionLog);
    return storage;
  }

  /**
   * Liest die Buchungsdaten aus dem InputStream (Exceltabelle) und schreibt die gelesenen Werte in ImportStorge. Der User muss der
   * FINANCE_GROUP angehören, um diese Funktionalität ausführen zu können.
   * @param is
   * @param filename
   * @return ImportStorage mit den gelesenen Daten.
   * @throws Exception
   */
  public ImportStorage<BuchungssatzDO> importBuchungsdaten(final InputStream is, final String filename, final ActionLog actionLog)
      throws Exception
      {
    checkLoggeinUserRight(accessChecker);
    log.info("importBuchungsdaten called");
    final ImportStorage<BuchungssatzDO> storage = new ImportStorage<BuchungssatzDO>(Type.BUCHUNGSSAETZE);
    storage.setFilename(filename);
    final BuchungssatzExcelImporter imp = new BuchungssatzExcelImporter(storage, kontoDao, kost1Dao, kost2Dao, actionLog);
    try {
      imp.doImport(is);
    } catch (final ExcelImportException ex) {
      throw new UserException("common.import.excel.error", ex.getMessage(), ex.getRow(), ex.getColumnname());
    }
    return storage;
      }

  /**
   * Der ImportStorage wird verprobt, dass heißt ein Schreiben der importierten Werte in die Datenbank wird getestet. Ergebnis sind mögliche
   * Fehler und Statistiken, welche Werte neu geschrieben und welche geändert werden. Der User muss der FINANCE_GROUP angehören, um diese
   * Funktionalität ausführen zu können.
   * @param storage
   * @param name of sheet to reconcile.
   */
  @SuppressWarnings("unchecked")
  public void reconcile(final ImportStorage< ? > storage, final String sheetName)
  {
    checkLoggeinUserRight(accessChecker);
    Validate.notNull(storage.getSheets());
    final ImportedSheet< ? > sheet = storage.getNamedSheet(sheetName);
    Validate.notNull(sheet);
    if (storage.getId() == Type.KONTENPLAN) {
      reconcileKontenplan((ImportedSheet<KontoDO>) sheet);
    } else {
      reconcileBuchungsdaten((ImportedSheet<BuchungssatzDO>) sheet);
    }
    sheet.setNumberOfCommittedElements(-1);
  }

  @SuppressWarnings("unchecked")
  @Transactional(readOnly = false, propagation = Propagation.REQUIRES_NEW, isolation = Isolation.REPEATABLE_READ)
  public void commit(final ImportStorage< ? > storage, final String sheetName)
  {
    checkLoggeinUserRight(accessChecker);
    Validate.notNull(storage.getSheets());
    final ImportedSheet< ? > sheet = storage.getNamedSheet(sheetName);
    Validate.notNull(sheet);
    Validate.isTrue(sheet.getStatus() == ImportStatus.RECONCILED);
    int no = -1;
    if (storage.getId() == Type.KONTENPLAN) {
      no = commitKontenplan((ImportedSheet<KontoDO>) sheet);
    } else {
      no = commitBuchungsdaten((ImportedSheet<BuchungssatzDO>) sheet);
    }
    sheet.setNumberOfCommittedElements(no);
    sheet.setStatus(ImportStatus.IMPORTED);
  }

  private void reconcileKontenplan(final ImportedSheet<KontoDO> sheet)
  {
    log.info("Reconcile Kontenplan called");
    for (final ImportedElement<KontoDO> el : sheet.getElements()) {
      final KontoDO konto = el.getValue();
      final KontoDO dbKonto = kontoDao.getKonto(konto.getNummer());
      if (dbKonto != null) {
        el.setOldValue(dbKonto);
      }
    }
    sheet.setStatus(ImportStatus.RECONCILED);
    sheet.calculateStatistics();
  }

  private void reconcileBuchungsdaten(final ImportedSheet<BuchungssatzDO> sheet)
  {
    log.info("Reconcile Buchungsdaten called");
    for (final ImportedElement<BuchungssatzDO> el : sheet.getElements()) {
      final BuchungssatzDO satz = el.getValue();
      if (el.isFaulty() == true) {
        String kost = (String) el.getErrorProperty("kost1");
        if (kost != null) {
          final int[] vals = KostFormatter.splitKost(kost);
          final Kost1DO kost1 = kost1Dao.getKost1(vals[0], vals[1], vals[2], vals[3]);
          if (kost1 != null) {
            satz.setKost1(kost1);
            el.removeErrorProperty("kost1");
          }
        }
        kost = (String) el.getErrorProperty("kost2");
        if (kost != null) {
          final int[] vals = KostFormatter.splitKost(kost);
          final Kost2DO kost2 = kost2Dao.getKost2(vals[0], vals[1], vals[2], vals[3]);
          if (kost2 != null) {
            satz.setKost2(kost2);
            el.removeErrorProperty("kost2");
          }
        }
      }
      final BuchungssatzDO dbSatz = buchungssatzDao.getBuchungssatz(satz.getYear(), satz.getMonth(), satz.getSatznr());
      if (dbSatz != null) {
        el.setOldValue(dbSatz);
      }
    }
    sheet.setStatus(ImportStatus.RECONCILED);
    sheet.calculateStatistics();
  }

  private int commitKontenplan(final ImportedSheet<KontoDO> sheet)
  {
    log.info("Commit Kontenplan called");
    final Collection<KontoDO> col = new ArrayList<KontoDO>();
    for (final ImportedElement<KontoDO> el : sheet.getElements()) {
      final KontoDO konto = el.getValue();
      final KontoDO dbKonto = kontoDao.getKonto(konto.getNummer());
      if (dbKonto != null) {
        konto.setId(dbKonto.getId());
        if (el.isSelected() == true) {
          col.add(konto);
        }
      } else if (el.isSelected() == true) {
        col.add(konto);
      }
    }
    kontoDao.internalSaveOrUpdate(kontoDao, col, KONTO_INSERT_BLOCK_SIZE);
    return col.size();
  }

  private Object get(final Class< ? > clazz, final Integer id)
  {
    if (id == null) {
      return null;
    }
    return getHibernateTemplate().get(clazz, id, LockMode.READ);
  }

  private int commitBuchungsdaten(final ImportedSheet<BuchungssatzDO> sheet)
  {
    log.info("Commit Buchungsdaten called");
    final Collection<BuchungssatzDO> col = new ArrayList<BuchungssatzDO>();
    for (final ImportedElement<BuchungssatzDO> el : sheet.getElements()) {
      final BuchungssatzDO satz = el.getValue();
      final BuchungssatzDO dbSatz = buchungssatzDao.getBuchungssatz(satz.getYear(), satz.getMonth(), satz.getSatznr());
      boolean addSatz = false;
      if (dbSatz != null) {
        satz.setId(dbSatz.getId());
        if (el.isSelected() == true) {
          addSatz = true;
        }
      } else if (el.isSelected() == true) {
        addSatz = true;
      }
      if (addSatz == true) {
        final BuchungssatzDO newSatz = new BuchungssatzDO();
        newSatz.copyValuesFrom(satz, "konto", "gegenKonto", "kost1", "kost2");
        newSatz.setKonto((KontoDO) get(KontoDO.class, satz.getKontoId()));
        newSatz.setGegenKonto((KontoDO) get(KontoDO.class, satz.getGegenKontoId()));
        newSatz.setKost1((Kost1DO) get(Kost1DO.class, satz.getKost1Id()));
        newSatz.setKost2((Kost2DO) get(Kost2DO.class, satz.getKost2Id()));
        col.add(newSatz);
      }
    }
    buchungssatzDao.internalSaveOrUpdate(buchungssatzDao, col, BUCHUNGSSATZ_INSERT_BLOCK_SIZE);
    return col.size();
  }

  public void setAccessChecker(final AccessChecker accessChecker)
  {
    this.accessChecker = accessChecker;
  }

  public void setKontoDao(final KontoDao kontoDao)
  {
    this.kontoDao = kontoDao;
  }

  public void setKost1Dao(final Kost1Dao kost1Dao)
  {
    this.kost1Dao = kost1Dao;
  }

  public void setKost2Dao(final Kost2Dao kost2Dao)
  {
    this.kost2Dao = kost2Dao;
  }

  public void setBuchungssatzDao(final BuchungssatzDao buchungssatzDao)
  {
    this.buchungssatzDao = buchungssatzDao;
  }
}
