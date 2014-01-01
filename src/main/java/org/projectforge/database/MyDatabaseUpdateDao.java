/////////////////////////////////////////////////////////////////////////////
//
// Project ProjectForge Community Edition
//         www.projectforge.org
//
// Copyright (C) 2001-2014 Kai Reinhard (k.reinhard@micromata.de)
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

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.projectforge.access.AccessChecker;
import org.projectforge.access.AccessException;
import org.projectforge.continuousdb.DatabaseUpdateDao;
import org.projectforge.continuousdb.UpdateEntry;
import org.projectforge.continuousdb.UpdaterConfiguration;
import org.projectforge.core.BaseDO;
import org.projectforge.plugins.core.AbstractPlugin;
import org.projectforge.plugins.core.PluginsRegistry;
import org.projectforge.registry.Registry;
import org.projectforge.user.Login;
import org.projectforge.user.PFUserContext;
import org.projectforge.user.PFUserDO;
import org.projectforge.user.ProjectForgeGroup;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;

/**
 * For manipulating the database (patching data etc.)
 * @author Kai Reinhard (k.reinhard@micromata.de)
 * 
 */
public class MyDatabaseUpdateDao extends DatabaseUpdateDao
{
  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(MyDatabaseUpdateDao.class);

  // private static String CATALOG = null;

  // private static String SCHEMA_PATTERN = null;

  // private static String TABLE_TYPE = "TABLE";

  public MyDatabaseUpdateDao(final UpdaterConfiguration configuration)
  {
    super(configuration);
  }

  private AccessChecker accessChecker;

  private static PFUserDO SYSTEM_ADMIN_PSEUDO_USER = new PFUserDO().setUsername("System admin user only for internal usage");

  public static PFUserDO __internalGetSystemAdminPseudoUser()
  {
    return SYSTEM_ADMIN_PSEUDO_USER;
  }

  @Override
  protected void accessCheck(final boolean writeaccess)
  {
    if (PFUserContext.getUser() == SYSTEM_ADMIN_PSEUDO_USER) {
      // No access check for the system admin pseudo user.
      return;
    }
    if (Login.getInstance().isAdminUser(PFUserContext.getUser()) == false) {
      throw new AccessException(AccessChecker.I18N_KEY_VIOLATION_USER_NOT_MEMBER_OF, ProjectForgeGroup.ADMIN_GROUP.getKey());
    }
    accessChecker.checkRestrictedOrDemoUser();
  }

  /**
   */
  public List<DatabaseUpdateDO> getUpdateHistory()
  {
    accessCheck(false);
    final JdbcTemplate jdbc = new JdbcTemplate(getDataSource());
    final List<Map<String, Object>> dbResult = jdbc.queryForList("select * from t_database_update order by update_date desc");
    final List<DatabaseUpdateDO> result = new ArrayList<DatabaseUpdateDO>();
    for (final Map<String, Object> map : dbResult) {
      final DatabaseUpdateDO entry = new DatabaseUpdateDO();
      entry.setUpdateDate((Date) map.get("update_date"));
      entry.setRegionId((String) map.get("region_id"));
      entry.setVersionString((String) map.get("version"));
      entry.setExecutionResult((String) map.get("execution_result"));
      final PFUserDO executedByUser = Registry.instance().getUserGroupCache().getUser((Integer) map.get("executed_by_user_fk"));
      entry.setExecutedBy(executedByUser);
      entry.setDescription((String) map.get("description"));
      result.add(entry);
    }
    return result;
  }

  /**
   * @see org.projectforge.continuousdb.DatabaseUpdateDao#createMissingIndices()
   */
  @Override
  public int createMissingIndices()
  {
    int result = super.createMissingIndices();
    if (createIndex("idx_timesheet_user_time", "t_timesheet", "user_id, start_time") == true) {
      ++result;
    }
    for (final AbstractPlugin plugin : PluginsRegistry.instance().getPlugins()) {
      if (plugin.isInitialized() == false) {
        // Plug-in not (yet) initialized, skip. this is normal on first start-up phase.
        continue;
      }
      final UpdateEntry updateEntry = plugin.getInitializationUpdateEntry();
      if (updateEntry != null) {
        result += updateEntry.createMissingIndices();
      }
    }
    return result;
  }

  /**
   * There is a bug for Hibernate history with Javassist: Sometimes the data base objects are serialized with the default toString() method
   * instead of using the plain id. This method fixes all wrong data base history entries.
   */
  public int fixDBHistoryEntries()
  {
    accessCheck(true);
    return internalFixDBHistoryEntries();
  }

  /**
   * Without access checking.
   * @see #fixDBHistoryEntries()
   */
  @SuppressWarnings({ "unchecked", "rawtypes"})
  public int internalFixDBHistoryEntries()
  {
    log.info("Fix all broken history entries (if exist).");
    final int counter[] = new int[1];
    counter[0] = 0;
    final JdbcTemplate jdbc = new JdbcTemplate(getDataSource());
    try {
      String sql = " from t_history_property_delta where old_value like 'org.projectforge.%' or new_value like 'org.projectforge.%'";
      jdbc.query("select id, old_value, new_value, property_type" + sql, new ResultSetExtractor() {
        @Override
        public Object extractData(final ResultSet rs) throws SQLException, DataAccessException
        {
          while (rs.next() == true) {
            final int id = rs.getInt("ID");
            final String oldValue = rs.getString("OLD_VALUE");
            final String newValue = rs.getString("NEW_VALUE");
            final Serializable oldId = getObjectId(oldValue);
            final Serializable newId = getObjectId(newValue);
            final String propertyType = rs.getString("PROPERTY_TYPE");
            final int pos = propertyType.indexOf("_$$_javassist_");
            final String newPropertyType;
            if (pos > 0) {
              newPropertyType = propertyType.substring(0, pos);
            } else {
              newPropertyType = null;
            }
            if (oldId == null && newId == null) {
              continue;
            }
            final StringBuffer buf = new StringBuffer();
            boolean first = true;
            buf.append("update t_history_property_delta set ");
            if (oldId != null) {
              buf.append("OLD_VALUE='").append(oldId).append("'");
              first = false;
            }
            if (newId != null) {
              if (first == false) {
                buf.append(", ");
              } else {
                first = false;
              }
              buf.append("NEW_VALUE='").append(newId).append("'");
            }
            if (newPropertyType != null) {
              if (first == false) {
                buf.append(", ");
              } else {
                first = false;
              }
              buf.append("PROPERTY_TYPE='").append(newPropertyType).append("'");
            }
            buf.append(" where ID=").append(id);
            final String sql = buf.toString();
            log.info(sql);
            jdbc.execute(sql);
            counter[0]++;
          }
          return null;
        }
      });
      int no = jdbc.queryForInt("select count(*)" + sql);
      if (no > 0) {
        log.warn("" + no + " of data base history entries aren't fixed.");
      }
      sql = " from t_history_property_delta where property_type like '%_$$_javassist_%'";
      jdbc.query("select id, property_type" + sql, new ResultSetExtractor() {
        @Override
        public Object extractData(final ResultSet rs) throws SQLException, DataAccessException
        {
          while (rs.next() == true) {
            final int id = rs.getInt("ID");
            final String propertyType = rs.getString("PROPERTY_TYPE");
            final int pos = propertyType.indexOf("_$$_javassist_");
            if (pos < 0) {
              log.error("Oups, should not occur.");
              continue;
            }
            final String newPropertyType = propertyType.substring(0, pos);
            final String sql = "update t_history_property_delta set PROPERTY_TYPE='" + newPropertyType + "' where id=" + id;
            log.info(sql);
            jdbc.execute(sql);
            counter[0]++;
          }
          return null;
        }
      });
      no = jdbc.queryForInt("select count(*)" + sql);
      if (no > 0) {
        log.warn("" + no + " of data base history entries aren't fixed.");
      }
    } catch (final Exception ex) {
      log.error(ex.getMessage(), ex);
      return 0;
    }
    return counter[0];
  }

  private Serializable getObjectId(final String serializedObject)
  {
    if (serializedObject == null || serializedObject.startsWith("org.projectforge.") == false || serializedObject.indexOf('@') < 0) {
      return null;
    }
    final String className = serializedObject.substring(0, serializedObject.indexOf('@'));
    Class< ? > clazz = null;
    try {
      clazz = Class.forName(className);
    } catch (final ClassNotFoundException ex) {
      log.error("Can't load class. " + ex, ex);
      return null;
    }
    if (BaseDO.class.isAssignableFrom(clazz) == false) {
      log.error("Unsupported class: " + clazz);
    }
    final Integer id = DatabaseUpdateHelper.getId(serializedObject);
    return id;
  }

  public void setAccessChecker(final AccessChecker accessChecker)
  {
    this.accessChecker = accessChecker;
  }
}
