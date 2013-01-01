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

package org.projectforge.meb;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.sql.DataSource;

import org.apache.commons.lang.Validate;
import org.projectforge.common.StringHelper;
import org.projectforge.core.BaseDao;
import org.projectforge.user.PFUserContext;
import org.projectforge.user.PFUserDO;
import org.projectforge.user.UserDao;
import org.projectforge.user.UserRightId;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * 
 * @author Kai Reinhard (k.reinhard@micromata.de)
 * 
 */
@Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
public class MebDao extends BaseDao<MebEntryDO>
{
  private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(MebDao.class);

  public static final UserRightId USER_RIGHT_ID = UserRightId.MISC_MEB;

  public static final String DATE_FORMAT = "yyyyMMddHHmmss";

  private DataSource dataSource;

  private UserDao userDao;

  private MebCache mebCache = new MebCache(this);

  /**
   * Removes all non digit and letter characters (also white-spaces) first. Afterward a MD5 checksum is calculated.
   * @param message
   * @return MD5 sum of the given string.
   * @see StringHelper#removeNonDigitsAndNonASCIILetters(String)
   */
  public static String createCheckSum(final String message)
  {
    final String str = StringHelper.removeNonDigitsAndNonASCIILetters(message);
    try {
      final MessageDigest md5 = MessageDigest.getInstance("MD5");
      byte[] result = md5.digest(str.getBytes());
      return StringHelper.asHex(result);
    } catch (NoSuchAlgorithmException ex) {
      log.error("Exception encountered " + ex, ex);
      return "";
    }
  }

  public static Date parseDate(final String dateString)
  {
    Date date = null;
    if (dateString.startsWith("20") == true) {
      final DateFormat df = new SimpleDateFormat(DATE_FORMAT);
      try {
        date = df.parse(dateString);
      } catch (final ParseException ex) {
        log.warn("Servlet call for receiving sms ignored because date string is not parseable (format '"
            + DATE_FORMAT
            + "' expected): "
            + dateString);
        return null;
      }
    } else {
      try {
        long seconds = Long.parseLong(dateString);
        if (seconds < 1274480916 || seconds > 1999999999) {
          log.warn("Servlet call for receiving sms ignored because date string is not parseable (millis since 01/01/1970 or format '"
              + DATE_FORMAT
              + "' expected): "
              + dateString);
          return null;
        }
        date = new Date(seconds * 1000);
      } catch (final NumberFormatException ex) {
        log.warn("Servlet call for receiving sms ignored because date string is not parseable (format '"
            + DATE_FORMAT
            + "' expected): "
            + dateString);
        return null;
      }
    }
    return date;
  }

  public MebDao()
  {
    super(MebEntryDO.class);
    userRightId = USER_RIGHT_ID;
  }

  /**
   * Get the number of recent MEB entries for the logged in user. If the user is member of the admin group then the number of unassigned
   * entries (owner not set) will be added. <br/>
   * The result is cached (therefore you can call this method very often).
   * @param userId If null then the current logged in user is assumed.
   * @return Number of recent (and unassigned) MEB entries.
   */
  public int getRecentMEBEntries(Integer userId)
  {
    if (userId == null) {
      userId = PFUserContext.getUserId();
    }
    return mebCache.getRecentMEBEntries(userId);
  }

  /**
   * Called by MebCache to get the number of recent entries for the given users. For administrative users the number of unassigned entries
   * (without owner) is added.
   * @param userId
   * @return Number of recent (and unassigned) MEB entries.
   */
  int internalGetRecentMEBEntries(final Integer userId)
  {
    final JdbcTemplate jdbc = new JdbcTemplate(dataSource);
    try {
      int counter = jdbc.queryForInt("SELECT COUNT(*) FROM t_meb_entry where owner_fk=" + userId + " and status='RECENT'");
      if (accessChecker.isLoggedInUserMemberOfAdminGroup() == true) {
        counter += jdbc.queryForInt("SELECT COUNT(*) FROM t_meb_entry where owner_fk is null");
      }
      return counter;
    } catch (final Exception ex) {
      log.error(ex.getMessage(), ex);
      return 0;
    }
  }

  /**
   * If the owner is changed then also the entry is set to recent (for the new owner).
   * @param mebEntry
   * @param userId If null, then user will be set to null;
   * @see BaseDao#getOrLoad(Integer)
   */
  public void setOwner(final MebEntryDO mebEntry, final Integer userId)
  {
    final PFUserDO user = userDao.getOrLoad(userId);
    if (userId.equals(mebEntry.getOwnerId()) == false) {
      // Entry should be recent for new owner.
      mebEntry.setStatus(MebEntryStatus.RECENT);
    }
    mebEntry.setOwner(user);
  }

  /**
   * Try to assign the owner from the sender number first. Ignore if the entry does already exist in the data base.
   * @param entry
   * @return Number of new imported messages.
   */
  @SuppressWarnings("unchecked")
  @Transactional(readOnly = false, propagation = Propagation.REQUIRES_NEW, isolation = Isolation.REPEATABLE_READ)
  public boolean checkAndAddEntry(final MebEntryDO entry, final String source)
  {
    Validate.notNull(entry.getSender());
    Validate.notNull(entry.getDate());
    Validate.notNull(entry.getMessage());
    entry.setStatus(MebEntryStatus.RECENT);
    synchronized (this) {
      final String checkSum = createCheckSum(entry.getMessage());
      // First check weather the entry is already in the data base or not.
      final List<ImportedMebEntryDO> entryList = getHibernateTemplate().find(
          "from ImportedMebEntryDO e where e.sender = ? and e.date = ? and e.checkSum = ?",
          new Object[] { entry.getSender(), entry.getDate(), checkSum});
      if (entryList != null && entryList.size() > 0) {
        return false;
      }
      // Try to assign the owner from the sender string.
      final List<Object[]> userList = (List<Object[]>) getSession().createQuery(
          "select id, personalMebMobileNumbers from PFUserDO u where deleted = false and personalMebMobileNumbers is not null").list();
      final String senderNumber = StringHelper.removeNonDigits(entry.getSender());
      Integer pk = null;
      for (final Object[] user : userList) {
        final String personalPhoneIdentifiers = StringHelper.removeNonDigits((String) user[1]);
        if (personalPhoneIdentifiers.length() == 0) {
          continue;
        }
        if (personalPhoneIdentifiers.contains(senderNumber) == true) {
          if (pk != null) {
            log.warn("Sender string '" + entry.getSender() + "' found twice (user pk's): " + pk + ", " + user[0]);
          } else {
            pk = (Integer) user[0];
          }
        }
      }
      if (pk != null) {
        final PFUserDO user = (PFUserDO) getSession().load(PFUserDO.class, pk);
        entry.setOwner(user);
      }
      internalSave(entry);
      final ImportedMebEntryDO imported = new ImportedMebEntryDO();
      imported.setCheckSum(checkSum);
      imported.setDate(entry.getDate());
      imported.setSender(entry.getSender());
      imported.setCreated();
      imported.setLastUpdate();
      imported.setSource(source);
      getHibernateTemplate().save(imported);
      return true;
    }
  }

  @Override
  protected void afterSaveOrModify(MebEntryDO obj)
  {
    mebCache.setExpired();
  }

  @Override
  public MebEntryDO newInstance()
  {
    return new MebEntryDO();
  }

  public void setDataSource(DataSource dataSource)
  {
    this.dataSource = dataSource;
  }

  public void setUserDao(UserDao userDao)
  {
    this.userDao = userDao;
  }
}
