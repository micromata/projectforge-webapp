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

package org.projectforge.fibu;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.projectforge.access.OperationType;
import org.projectforge.common.DateHelper;
import org.projectforge.common.NumberHelper;
import org.projectforge.core.BaseDao;
import org.projectforge.core.BaseSearchFilter;
import org.projectforge.core.ConfigXml;
import org.projectforge.core.DisplayHistoryEntry;
import org.projectforge.core.MessageParam;
import org.projectforge.core.MessageParamType;
import org.projectforge.core.QueryFilter;
import org.projectforge.core.UserException;
import org.projectforge.database.SQLHelper;
import org.projectforge.mail.Mail;
import org.projectforge.mail.SendMail;
import org.projectforge.task.TaskDO;
import org.projectforge.task.TaskDao;
import org.projectforge.task.TaskTree;
import org.projectforge.user.PFUserDO;
import org.projectforge.user.UserDao;
import org.projectforge.user.UserRightId;
import org.projectforge.xml.stream.XmlObjectReader;
import org.projectforge.xml.stream.XmlObjectWriter;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

public class AuftragDao extends BaseDao<AuftragDO>
{
  public static final UserRightId USER_RIGHT_ID = UserRightId.PM_ORDER_BOOK;

  public final static int START_NUMBER = 1;

  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(AuftragDao.class);

  private static final Class< ? >[] ADDITIONAL_HISTORY_SEARCH_DOS = new Class[] { AuftragsPositionDO.class};

  private static final String[] ADDITIONAL_SEARCH_FIELDS = new String[] { "contactPerson.username", "contactPerson.firstname",
    "contactPerson.lastname", "kunde.name", "projekt.name", "projekt.kunde.name", "positionen.position", "positionen.art",
    "positionen.status", "positionen.titel", "positionen.bemerkung", "positionen.nettoSumme"};

  private UserDao userDao;

  private KundeDao kundeDao;

  private ProjektDao projektDao;

  private SendMail sendMail;

  private Integer abgeschlossenNichtFakturiert;

  private RechnungCache rechnungCache;

  private TaskDao taskDao;

  private TaskTree taskTree;

  public void setUserDao(final UserDao userDao)
  {
    this.userDao = userDao;
  }

  public void setKundeDao(final KundeDao kundeDao)
  {
    this.kundeDao = kundeDao;
  }

  public void setProjektDao(final ProjektDao projektDao)
  {
    this.projektDao = projektDao;
  }

  public void setRechnungCache(final RechnungCache rechnungCache)
  {
    this.rechnungCache = rechnungCache;
  }

  public void setTaskDao(final TaskDao taskDao)
  {
    this.taskDao = taskDao;
  }

  /**
   * Could not use injection by spring, because TaskTree is already injected in AuftragDao.
   * @param taskTree
   */
  public void registerTaskTree(final TaskTree taskTree)
  {
    this.taskTree = taskTree;
  }

  public void setSendMail(final SendMail sendMail)
  {
    this.sendMail = sendMail;
  }

  public AuftragDao()
  {
    super(AuftragDO.class);
    userRightId = USER_RIGHT_ID;
  }

  @Override
  protected String[] getAdditionalSearchFields()
  {
    return ADDITIONAL_SEARCH_FIELDS;
  }

  /**
   * List of all years with invoices: select min(datum), max(datum) from t_fibu_rechnung.
   * @return
   */
  @SuppressWarnings("unchecked")
  public int[] getYears()
  {
    final List<Object[]> list = getSession().createQuery("select min(angebotsDatum), max(angebotsDatum) from AuftragDO t")
        .list();
    return SQLHelper.getYears(list);
  }

  /**
   * @return Map with all order positions referencing a task. The key of the map is the task id.
   */
  public Map<Integer, Set<AuftragsPositionVO>> getTaskReferences()
  {
    final Map<Integer, Set<AuftragsPositionVO>> result = new HashMap<Integer, Set<AuftragsPositionVO>>();
    @SuppressWarnings("unchecked")
    final List<AuftragsPositionDO> list = getHibernateTemplate().find(
        "from AuftragsPositionDO a where a.task.id is not null");
    if (list == null) {
      return result;
    }
    for (final AuftragsPositionDO pos : list) {
      if (pos.getTaskId() == null) {
        log.error("Oups, should not occur, that in getTaskReference a order position without a task reference is found.");
        continue;
      }
      final AuftragsPositionVO vo = new AuftragsPositionVO(pos);
      Set<AuftragsPositionVO> set = result.get(pos.getTaskId());
      if (set == null) {
        set = new TreeSet<AuftragsPositionVO>();
        result.put(pos.getTaskId(), set);
      }
      set.add(vo);
    }
    return result;
  }

  public AuftragsStatistik buildStatistik(final List<AuftragDO> list)
  {
    final AuftragsStatistik stats = new AuftragsStatistik();
    if (list == null) {
      return stats;
    }
    for (final AuftragDO auftrag : list) {
      stats.add(auftrag);
    }
    return stats;
  }

  /**
   * Get all invoices and set the field fakturiertSum for every order of the given col.
   * @param col
   * @see RechnungCache#getRechnungsPositionVOSetByAuftragsPositionId(Integer)
   */
  public void calculateInvoicedSum(final Collection<AuftragDO> col)
  {
    if (col == null) {
      return;
    }
    for (final AuftragDO auftrag : col) {
      if (auftrag.getPositionen() != null) {
        for (final AuftragsPositionDO pos : auftrag.getPositionen()) {
          final Set<RechnungsPositionVO> set = rechnungCache.getRechnungsPositionVOSetByAuftragsPositionId(pos.getId());
          if (set != null) {
            pos.setFakturiertSum(RechnungDao.getNettoSumme(set));
          }
        }
      }
    }
  }

  /**
   * @param auftrag
   * @param contactPersonId If null, then contact person will be set to null;
   * @see BaseDao#getOrLoad(Integer)
   */
  public void setContactPerson(final AuftragDO auftrag, final Integer contactPersonId)
  {
    if (contactPersonId == null) {
      auftrag.setContactPerson(null);
    } else {
      final PFUserDO contactPerson = userDao.getOrLoad(contactPersonId);
      auftrag.setContactPerson(contactPerson);
    }
  }

  /**
   * @param position
   * @param taskId
   * @see BaseDao#getOrLoad(Integer)
   */
  public void setTask(final AuftragsPositionDO position, final Integer taskId)
  {
    final TaskDO task = taskDao.getOrLoad(taskId);
    position.setTask(task);
  }

  /**
   * @param auftrag
   * @param kundeId If null, then kunde will be set to null;
   * @see BaseDao#getOrLoad(Integer)
   */
  public void setKunde(final AuftragDO auftrag, final Integer kundeId)
  {
    final KundeDO kunde = kundeDao.getOrLoad(kundeId);
    auftrag.setKunde(kunde);
  }

  /**
   * @param auftrag
   * @param projektId If null, then projekt will be set to null;
   * @see BaseDao#getOrLoad(Integer)
   */
  public void setProjekt(final AuftragDO auftrag, final Integer projektId)
  {
    final ProjektDO projekt = projektDao.getOrLoad(projektId);
    auftrag.setProjekt(projekt);
  }

  /**
   * @param posString Format ###.## (&lt;order number&gt;.&lt;position number&gt;).
   */
  @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
  public AuftragsPositionDO getAuftragsPosition(final String posString)
  {
    Integer auftragsNummer = null;
    Short positionNummer = null;
    if (posString == null) {
      return null;
    }
    final int sep = posString.indexOf('.');
    if (sep <= 0 || sep + 1 >= posString.length()) {
      return null;
    }
    auftragsNummer = NumberHelper.parseInteger(posString.substring(0, posString.indexOf('.')));
    positionNummer = NumberHelper.parseShort(posString.substring(posString.indexOf('.') + 1));
    if (auftragsNummer == null || positionNummer == null) {
      log.info("Cannot parse order number (format ###.## expected: " + posString);
      return null;
    }
    @SuppressWarnings("unchecked")
    final List<AuftragDO> list = getHibernateTemplate().find("from AuftragDO k where k.nummer=?", auftragsNummer);
    if (CollectionUtils.isEmpty(list) == true) {
      return null;
    }
    return list.get(0).getPosition(positionNummer);
  }

  public synchronized int getAbgeschlossenNichtFakturiertAnzahl()
  {
    if (abgeschlossenNichtFakturiert != null) {
      return abgeschlossenNichtFakturiert;
    }
    final AuftragFilter filter = new AuftragFilter();
    filter.setListType(AuftragFilter.FILTER_ABGESCHLOSSEN_NF);
    final List<AuftragDO> list = getList(filter, false);
    abgeschlossenNichtFakturiert = list != null ? list.size() : 0;
    return abgeschlossenNichtFakturiert;
  }

  @Override
  public List<AuftragDO> getList(final BaseSearchFilter filter)
  {
    return getList(filter, true);
  }

  private List<AuftragDO> getList(final BaseSearchFilter filter, final boolean checkAccess)
  {
    final AuftragFilter myFilter;
    if (filter instanceof AuftragFilter) {
      myFilter = (AuftragFilter) filter;
    } else {
      myFilter = new AuftragFilter(filter);
    }
    final QueryFilter queryFilter = new QueryFilter(myFilter);
    if (myFilter.getYear() > 1900) {
      final Calendar cal = DateHelper.getUTCCalendar();
      cal.set(Calendar.YEAR, myFilter.getYear());
      java.sql.Date lo = null;
      java.sql.Date hi = null;
      cal.set(Calendar.DAY_OF_YEAR, 1);
      lo = new java.sql.Date(cal.getTimeInMillis());
      final int lastDayOfYear = cal.getActualMaximum(Calendar.DAY_OF_YEAR);
      cal.set(Calendar.DAY_OF_YEAR, lastDayOfYear);
      hi = new java.sql.Date(cal.getTimeInMillis());
      queryFilter.add(Restrictions.between("angebotsDatum", lo, hi));
    }
    queryFilter.addOrder(Order.desc("nummer"));
    Boolean vollstaendigFakturiert = null;
    if (myFilter.isShowBeauftragtNochNichtVollstaendigFakturiert() == true) {
      queryFilter.add(Restrictions.not(Restrictions.in("auftragsStatus", new AuftragsStatus[] { AuftragsStatus.ABGELEHNT,
          AuftragsStatus.ERSETZT, AuftragsStatus.GELEGT, AuftragsStatus.GROB_KALKULATION, AuftragsStatus.IN_ERSTELLUNG})));
      vollstaendigFakturiert = false;
    } else if (myFilter.isShowNochNichtVollstaendigFakturiert() == true) {
      queryFilter.add(Restrictions.not(Restrictions.in("auftragsStatus", new AuftragsStatus[] { AuftragsStatus.ABGELEHNT,
          AuftragsStatus.ERSETZT})));
      vollstaendigFakturiert = false;
    } else if (myFilter.isShowVollstaendigFakturiert() == true) {
      vollstaendigFakturiert = true;
    } else if (myFilter.isShowAbgelehnt() == true) {
      queryFilter.add(Restrictions.eq("auftragsStatus", AuftragsStatus.ABGELEHNT));
    } else if (myFilter.isShowAbgeschlossenNichtFakturiert() == true) {
      queryFilter.createAlias("positionen", "position").add(
          Restrictions.or(Restrictions.eq("auftragsStatus", AuftragsStatus.ABGESCHLOSSEN), Restrictions.and(Restrictions.eq(
              "position.status", AuftragsPositionsStatus.ABGESCHLOSSEN), Restrictions.eq("position.vollstaendigFakturiert", false))));
      vollstaendigFakturiert = false; // Und noch nicht fakturiert.
    } else if (myFilter.isShowAkquise() == true) {
      queryFilter.add(Restrictions.in("auftragsStatus", new AuftragsStatus[] { AuftragsStatus.GELEGT, AuftragsStatus.IN_ERSTELLUNG,
          AuftragsStatus.GROB_KALKULATION}));
    } else if (myFilter.isShowBeauftragt() == true) {
      queryFilter.add(Restrictions.in("auftragsStatus", new AuftragsStatus[] { AuftragsStatus.BEAUFTRAGT, AuftragsStatus.LOI,
          AuftragsStatus.ESKALATION}));
    } else if (myFilter.isShowErsetzt() == true) {
      queryFilter.add(Restrictions.eq("auftragsStatus", AuftragsStatus.ERSETZT));
    }
    final List<AuftragDO> list;
    if (checkAccess == true) {
      list = getList(queryFilter);
    } else {
      list = internalGetList(queryFilter);
    }
    if (vollstaendigFakturiert != null || myFilter.getAuftragsPositionsArt() != null) {
      final Boolean fakturiert = vollstaendigFakturiert;
      final AuftragFilter fil = myFilter;
      CollectionUtils.filter(list, new Predicate() {
        public boolean evaluate(final Object object)
        {
          final AuftragDO auftrag = (AuftragDO) object;
          if (fil.getAuftragsPositionsArt() != null) {
            boolean match = false;
            if (CollectionUtils.isNotEmpty(auftrag.getPositionen()) == true) {
              for (final AuftragsPositionDO position : auftrag.getPositionen()) {
                if (fil.getAuftragsPositionsArt() == position.getArt()) {
                  match = true;
                  break;
                }
              }
            }
            if (match == false) {
              return false;
            }
          }
          if (fakturiert != null) {
            return auftrag.isVollstaendigFakturiert() == fakturiert;
          }
          return true;
        }
      });
    }
    return list;
  }

  @SuppressWarnings("unchecked")
  @Override
  protected void onSaveOrModify(final AuftragDO obj)
  {
    if (obj.getNummer() == null) {
      throw new UserException("validation.required.valueNotPresent", new MessageParam("fibu.auftrag.nummer", MessageParamType.I18N_KEY));
    }
    if (obj.getId() == null) {
      // Neuer Auftrag/Angebot
      final Integer next = getNextNumber(obj);
      if (next.intValue() != obj.getNummer().intValue()) {
        throw new UserException("fibu.auftrag.error.nummerIstNichtFortlaufend");
      }
    } else {
      final List<RechnungDO> list = getHibernateTemplate().find("from AuftragDO r where r.nummer = ? and r.id <> ?",
          new Object[] { obj.getNummer(), obj.getId()});
      if (list != null && list.size() > 0) {
        throw new UserException("fibu.auftrag.error.nummerBereitsVergeben");
      }
    }
    if (CollectionUtils.isEmpty(obj.getPositionen()) == true) {
      throw new UserException("fibu.auftrag.error.auftragHatKeinePositionen");
    }
    final int size = obj.getPositionen().size();
    for (int i = size - 1; i > 0; i--) {
      // Don't remove first position, remove only the last empty positions.
      final AuftragsPositionDO position = obj.getPositionen().get(i);
      if (position.getId() == null && position.isEmpty() == true) {
        obj.getPositionen().remove(i);
      } else {
        break;
      }
    }
    if (CollectionUtils.isNotEmpty(obj.getPositionen()) == true) {
      for (final AuftragsPositionDO position : obj.getPositionen()) {
        position.checkVollstaendigFakturiert();
      }
    }
    abgeschlossenNichtFakturiert = null;
    final String uiStatusAsXml = XmlObjectWriter.writeAsXml(obj.getUiStatus());
    obj.setUiStatusAsXml(uiStatusAsXml);
  }

  @Override
  protected void afterSaveOrModify(final AuftragDO obj)
  {
    super.afterSaveOrModify(obj);
    if (taskTree != null) {
      taskTree.refreshOrderPositionReferences();
    }
  }

  @Override
  protected void afterLoad(final AuftragDO obj)
  {
    final XmlObjectReader reader = new XmlObjectReader();
    reader.initialize(AuftragUIStatus.class);
    final String styleAsXml = obj.getUiStatusAsXml();
    final AuftragUIStatus status;
    if (StringUtils.isEmpty(styleAsXml) == true) {
      status = new AuftragUIStatus();
    } else {
      status = (AuftragUIStatus) reader.read(styleAsXml);
    }
    obj.setUiStatus(status);
  }

  /**
   * @see org.projectforge.core.BaseDao#prepareHibernateSearch(org.projectforge.core.ExtendedBaseDO, org.projectforge.access.OperationType)
   */
  @Override
  protected void prepareHibernateSearch(final AuftragDO obj, final OperationType operationType)
  {
    projektDao.initializeProjektManagerGroup(obj.getProjekt());
  }

  /**
   * Sends an e-mail to the projekt manager if exists and is not equals to the logged in user.
   * @param auftrag
   * @param operationType
   * @return
   */
  public boolean sendNotificationIfRequired(final AuftragDO auftrag, final OperationType operationType, final String requestUrl)
  {
    if (ConfigXml.getInstance().isSendMailConfigured() == false) {
      return false;
    }
    final PFUserDO contactPerson = auftrag.getContactPerson();
    if (contactPerson == null) {
      return false;
    }
    if (hasAccess(contactPerson, auftrag, null, OperationType.SELECT, false) == false) {
      return false;
    }
    final Map<String, Object> data = new HashMap<String, Object>();
    data.put("contactPerson", contactPerson);
    data.put("auftrag", auftrag);
    data.put("requestUrl", requestUrl);
    final List<DisplayHistoryEntry> history = getDisplayHistoryEntries(auftrag);
    final List<DisplayHistoryEntry> list = new ArrayList<DisplayHistoryEntry>();
    int i = 0;
    for (final DisplayHistoryEntry entry : history) {
      list.add(entry);
      if (++i >= 10) {
        break;
      }
    }
    data.put("history", list);
    final Mail msg = new Mail();
    msg.setTo(contactPerson);
    final String subject;
    if (operationType == OperationType.INSERT) {
      subject = "Auftrag #" + auftrag.getNummer() + " wurde angelegt.";
    } else if (operationType == OperationType.DELETE) {
      subject = "Auftrag #" + auftrag.getNummer() + " wurde gelöscht.";
    } else {
      subject = "Auftrag #" + auftrag.getNummer() + " wurde geändert.";
    }
    msg.setProjectForgeSubject(subject);
    data.put("subject", subject);
    final String content = sendMail.renderGroovyTemplate(msg, "mail/orderChangeNotification.html", data, contactPerson);
    msg.setContent(content);
    msg.setContentType(Mail.CONTENTTYPE_HTML);
    return sendMail.send(msg);
  }

  /**
   * Gets the highest Auftragsnummer.
   * @param auftrag wird benötigt, damit geschaut werden kann, ob dieser Auftrag ggf. schon existiert. Wenn er schon eine Nummer hatte, so
   *          kann verhindert werden, dass er eine nächst höhere Nummer bekommt. Ein solcher Auftrag bekommt die alte Nummer wieder
   *          zugeordnet.
   */
  @SuppressWarnings("unchecked")
  @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
  public Integer getNextNumber(final AuftragDO auftrag)
  {
    if (auftrag.getId() != null) {
      final AuftragDO orig = internalGetById(auftrag.getId());
      if (orig.getNummer() != null) {
        auftrag.setNummer(orig.getNummer());
        return orig.getNummer();
      }
    }
    final List<Integer> list = getSession().createQuery("select max(t.nummer) from AuftragDO t").list();
    Validate.notNull(list);
    if (list.size() == 0 || list.get(0) == null) {
      log.info("First entry of AuftragDO");
      return START_NUMBER;
    }
    Integer number = list.get(0);
    return ++number;
  }

  /**
   * Gets history entries of super and adds all history entries of the AuftragsPositionDO childs.
   * @see org.projectforge.core.BaseDao#getDisplayHistoryEntries(org.projectforge.core.ExtendedBaseDO)
   */
  @Override
  public List<DisplayHistoryEntry> getDisplayHistoryEntries(final AuftragDO obj)
  {
    final List<DisplayHistoryEntry> list = super.getDisplayHistoryEntries(obj);
    if (hasLoggedInUserHistoryAccess(obj, false) == false) {
      return list;
    }
    if (CollectionUtils.isNotEmpty(obj.getPositionen()) == true) {
      for (final AuftragsPositionDO position : obj.getPositionen()) {
        final List<DisplayHistoryEntry> entries = internalGetDisplayHistoryEntries(position);
        for (final DisplayHistoryEntry entry : entries) {
          final String propertyName = entry.getPropertyName();
          if (propertyName != null) {
            entry.setPropertyName("#" + position.getNumber() + ":" + entry.getPropertyName()); // Prepend number of positon.
          } else {
            entry.setPropertyName("#" + position.getNumber());
          }
        }
        list.addAll(entries);
      }
    }
    Collections.sort(list, new Comparator<DisplayHistoryEntry>() {
      public int compare(final DisplayHistoryEntry o1, final DisplayHistoryEntry o2)
      {
        return (o2.getTimestamp().compareTo(o1.getTimestamp()));
      }
    });
    return list;
  }

  @Override
  protected Class< ? >[] getAdditionalHistorySearchDOs()
  {
    return ADDITIONAL_HISTORY_SEARCH_DOS;
  }

  /**
   * Returns also true, if idSet contains the id of any order position.
   * @see org.projectforge.core.BaseDao#contains(java.util.Set, org.projectforge.core.ExtendedBaseDO)
   */
  @Override
  protected boolean contains(final Set<Integer> idSet, final AuftragDO entry)
  {
    if (super.contains(idSet, entry) == true) {
      return true;
    }
    for (final AuftragsPositionDO pos : entry.getPositionen()) {
      if (idSet.contains(pos.getId()) == true) {
        return true;
      }
    }
    return false;
  }

  @Override
  public AuftragDO newInstance()
  {
    return new AuftragDO();
  }

  /**
   * @see org.projectforge.core.BaseDao#useOwnCriteriaCacheRegion()
   */
  @Override
  protected boolean useOwnCriteriaCacheRegion()
  {
    return true;
  }
}
