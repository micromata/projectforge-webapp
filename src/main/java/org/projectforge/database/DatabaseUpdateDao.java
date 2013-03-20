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

package org.projectforge.database;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;

import javax.sql.DataSource;

import org.apache.commons.lang.StringUtils;
import org.projectforge.access.AccessChecker;
import org.projectforge.access.AccessException;
import org.projectforge.common.StringHelper;
import org.projectforge.core.BaseDO;
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
public class DatabaseUpdateDao
{
  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(DatabaseUpdateDao.class);

  // private static String CATALOG = null;

  // private static String SCHEMA_PATTERN = null;

  // private static String TABLE_TYPE = "TABLE";

  private AccessChecker accessChecker;

  private DataSource dataSource;

  DatabaseSupport databaseSupport;

  private static PFUserDO SYSTEM_ADMIN_PSEUDO_USER = new PFUserDO().setUsername("System admin user only for internal usage");

  public static PFUserDO __internalGetSystemAdminPseudoUser()
  {
    return SYSTEM_ADMIN_PSEUDO_USER;
  }

  private DatabaseSupport getDatabaseSupport()
  {
    if (databaseSupport == null) {
      databaseSupport = new DatabaseSupport();
    }
    return databaseSupport;
  }

  private void accessCheck(final boolean writeaccess)
  {
    if (writeaccess == false && PFUserContext.getUser() == SYSTEM_ADMIN_PSEUDO_USER) {
      // No access check for the system admin pseudo user.
      return;
    }
    if (Login.getInstance().isAdminUser(PFUserContext.getUser()) == false) {
      throw new AccessException(AccessChecker.I18N_KEY_VIOLATION_USER_NOT_MEMBER_OF, ProjectForgeGroup.ADMIN_GROUP.getKey());
    }
    accessChecker.checkRestrictedOrDemoUser();
  }

  public boolean doesTableExist(final String table)
  {
    accessCheck(false);
    return internalDoesTableExist(table);
  }

  public boolean doesExist(final Table... tables)
  {
    accessCheck(false);
    for (final Table table : tables) {
      if (internalDoesTableExist(table.getName()) == false) {
        return false;
      }
    }
    return true;
  }

  /**
   * Without check access.
   * @param table
   * @return
   */
  public boolean internalDoesTableExist(final String table)
  {
    /*
     * try { final ResultSet resultSet = dataSource.getConnection().getMetaData().getTables(CATALOG, SCHEMA_PATTERN, table, new String[] {
     * TABLE_TYPE}); return resultSet.next(); } catch (final SQLException ex) { log.error(ex.getMessage(), ex); throw new
     * InternalErrorException(ex.getMessage()); }
     */
    final JdbcTemplate jdbc = new JdbcTemplate(dataSource);
    try {
      jdbc.queryForInt("SELECT COUNT(*) FROM " + table);
    } catch (final Exception ex) {
      return false;
    }
    return true;
  }

  public boolean doesTableAttributeExist(final String table, final String attribute)
  {
    accessCheck(false);
    final JdbcTemplate jdbc = new JdbcTemplate(dataSource);
    try {
      jdbc.queryForInt("SELECT COUNT(" + attribute + ") FROM " + table);
    } catch (final Exception ex) {
      return false;
    }
    return true;
  }

  public boolean doesTableAttributesExist(final Table table, final String... properties)
  {
    accessCheck(false);
    for (final String property : properties) {
      final TableAttribute attr = new TableAttribute(table.getEntityClass(), property);
      if (doesTableAttributeExist(table.getName(), attr.getName()) == false) {
        return false;
      }
    }
    return true;
  }



  public boolean isTableEmpty(final String table)
  {
    accessCheck(false);
    return internalIsTableEmpty(table);
  }

  public boolean internalIsTableEmpty(final String table)
  {
    final JdbcTemplate jdbc = new JdbcTemplate(dataSource);
    try {
      return jdbc.queryForInt("SELECT COUNT(*) FROM " + table) == 0;
    } catch (final Exception ex) {
      return false;
    }
  }

  /**
   * @param table
   * @return true, if the table is successfully dropped or does not exist.
   */
  public boolean dropTable(final String table)
  {
    accessCheck(true);
    if (doesTableExist(table) == false) {
      // Table is already dropped or does not exist.
      return true;
    }
    if (isTableEmpty(table) == false) {
      // Table is not empty.
      log.warn("Could not drop table '" + table + "' because the table is not empty.");
      return false;
    }
    execute("DROP TABLE " + table);
    return true;
  }

  /**
   * @since 3.3.46
   * @param table
   * @param attribute
   * @return
   */
  public boolean dropTableAttribute(final String table, final String attribute)
  {
    accessCheck(true);
    execute("ALTER TABLE " + table + " DROP COLUMN " + attribute);
    return true;
  }

  public void buildCreateTableStatement(final StringBuffer buf, final Table table)
  {
    buf.append("CREATE TABLE " + table.getName() + " (\n");
    boolean first = true;
    for (final TableAttribute attr : table.getAttributes()) {
      if (first == true) {
        first = false;
      } else {
        buf.append(",\n");
      }
      buf.append("  ");
      buildAttribute(buf, attr);
    }
    final TableAttribute primaryKey = table.getPrimaryKey();
    if (primaryKey != null) {
      buf.append(getDatabaseSupport().getPrimaryKeyTableSuffix(primaryKey));
    }
    // Create foreign keys if exist
    for (final TableAttribute attr : table.getAttributes()) {
      if (StringUtils.isNotEmpty(attr.getForeignTable()) == true) {
        // foreign key (user_fk) references t_pf_user(pk)
        buf.append(",\n  FOREIGN KEY ("
            + attr.getName()
            + ") REFERENCES "
            + attr.getForeignTable()
            + "("
            + attr.getForeignAttribute()
            + ")");
      }
    }
    buf.append("\n);\n");
  }

  private void buildAttribute(final StringBuffer buf, final TableAttribute attr)
  {
    buf.append(attr.getName()).append(" ").append(getDatabaseSupport().getType(attr));
    if (attr.isPrimaryKey() == true) {
      buf.append(getDatabaseSupport().getPrimaryKeyAttributeSuffix(attr));
    }
    databaseSupport.addDefaultAndNotNull(buf, attr);
    // if (attr.isNullable() == false) {
    // buf.append(" NOT NULL");
    // }
    // if (StringUtils.isNotBlank(attr.getDefaultValue()) == true) {
    // buf.append(" DEFAULT(").append(attr.getDefaultValue()).append(")");
    // }
  }

  public void buildForeignKeyConstraint(final StringBuffer buf, final String table, final TableAttribute attr)
  {
    buf.append("ALTER TABLE ").append(table).append(" ADD CONSTRAINT ").append(table).append("_").append(attr.getName()).append(
        " FOREIGN KEY (").append(attr.getName()).append(") REFERENCES ").append(attr.getForeignTable()).append("(").append(
            attr.getForeignAttribute()).append(");\n");
  }

  public boolean createTable(final Table table)
  {
    accessCheck(true);
    if (doesExist(table) == true) {
      log.info("Table '" + table.getName() + "' does already exist.");
      return false;
    }
    final StringBuffer buf = new StringBuffer();
    buildCreateTableStatement(buf, table);
    execute(buf.toString());
    return true;
  }

  public void buildAddTableAttributesStatement(final StringBuffer buf, final String table, final TableAttribute... attributes)
  {
    for (final TableAttribute attr : attributes) {
      if (doesTableAttributeExist(table, attr.getName()) == true) {
        buf.append("-- Does already exist: ");
      }
      buf.append("ALTER TABLE ").append(table).append(" ADD COLUMN ");
      buildAttribute(buf, attr);
      buf.append(";\n");
    }
    for (final TableAttribute attr : attributes) {
      if (attr.getForeignTable() != null) {
        if (doesTableAttributeExist(table, attr.getName()) == true) {
          buf.append("-- Column does already exist: ");
        }
        buildForeignKeyConstraint(buf, table, attr);
      }
    }
  }

  public void buildAddTableAttributesStatement(final StringBuffer buf, final String table, final Collection<TableAttribute> attributes)
  {
    buildAddTableAttributesStatement(buf, table, attributes.toArray(new TableAttribute[0]));
  }

  public boolean addTableAttributes(final String table, final TableAttribute... attributes)
  {
    final StringBuffer buf = new StringBuffer();
    buildAddTableAttributesStatement(buf, table, attributes);
    execute(buf.toString());
    return true;
  }

  public boolean addTableAttributes(final Table table, final TableAttribute... attributes)
  {
    return addTableAttributes(table.getName(), attributes);
  }

  public boolean addTableAttributes(final String table, final Collection<TableAttribute> attributes)
  {
    final StringBuffer buf = new StringBuffer();
    buildAddTableAttributesStatement(buf, table, attributes);
    execute(buf.toString());
    return true;
  }

  public boolean addTableAttributes(final Table table, final Collection<TableAttribute> attributes)
  {
    return addTableAttributes(table.getName(), attributes);
  }

  public void buildAddUniqueConstraintStatement(final StringBuffer buf, final String table, final String constraintName,
      final String... attributes)
  {
    buf.append("ALTER TABLE ").append(table).append(" ADD CONSTRAINT ").append(constraintName).append(" UNIQUE (");
    buf.append(StringHelper.listToString(", ", attributes));
    buf.append(");\n");
  }

  /** @since 3.3.48 */
  public boolean renameTableAttribute(final String table, final String oldName, final String newName)
  {
    final String alterStatement = getDatabaseSupport().renameAttribute(table, oldName, newName);
    execute(alterStatement);
    return true;
  }

  public boolean addUniqueConstraint(final Table table, final String constraintName, final String... attributes)
  {
    accessCheck(true);
    return addUniqueConstraint(table.getName(), constraintName, attributes);
  }

  public boolean addUniqueConstraint(final String table, final String constraintName, final String... attributes)
  {
    accessCheck(true);
    final StringBuffer buf = new StringBuffer();
    buildAddUniqueConstraintStatement(buf, table, constraintName, attributes);
    execute(buf.toString());
    return true;
  }

  /**
   * Creates missing data base indices.
   * @return Number of successful created data base indices.
   */
  public int createMissingIndices()
  {
    accessCheck(true);
    log.info("createMissingIndices called.");
    int counter = 0;
    // For user / time period search:
    createIndex("idx_timesheet_user_time", "t_timesheet", "user_id, start_time");
    try {
      final ResultSet reference = dataSource.getConnection().getMetaData().getCrossReference(null, null, null, null, null, null);
      while (reference.next()) {
        final String fkTable = reference.getString("FKTABLE_NAME");
        final String fkCol = reference.getString("FKCOLUMN_NAME");
        if (fkTable.startsWith("t_") == true) {
          // Table of ProjectForge
          if (createIndex("idx_fk_" + fkTable + "_" + fkCol, fkTable, fkCol) == true) {
            counter++;
          }
        }
      }
    } catch (final SQLException ex) {
      log.error(ex.getMessage(), ex);
    }
    return counter;
  }

  public void insertInto(final String table, final String[] columns, final Object[] values)
  {
    final StringBuffer buf = new StringBuffer();
    buf.append("insert into ").append(table).append(" (").append(StringHelper.listToString(",", columns)).append(") values (");
    boolean first = true;
    for (int i = 0; i < values.length; i++) {
      first = StringHelper.append(buf, first, "?", ",");
    }
    buf.append(")");
    final JdbcTemplate jdbc = new JdbcTemplate(dataSource);
    final String sql = buf.toString();
    log.info(sql + "; values = " + StringHelper.listToString(", ", values));
    jdbc.update(sql, values);
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
    final JdbcTemplate jdbc = new JdbcTemplate(dataSource);
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

  /**
   * Creates the given data base index if not already exists.
   * @param name
   * @param table
   * @param attributes
   * @return true, if the index was created, false if an error has occured or the index already exists.
   */
  public boolean createIndex(final String name, final String table, final String attributes)
  {
    accessCheck(true);
    try {
      final String jdbcString = "CREATE INDEX " + name + " ON " + table + "(" + attributes + ");";
      final JdbcTemplate jdbc = new JdbcTemplate(dataSource);
      jdbc.execute(jdbcString);
      log.info(jdbcString);
      return true;
    } catch (final Throwable ex) {
      // Index does already exist (or an error has occurred).
      return false;
    }
  }

  /**
   * @param name
   * @param attributes
   * @return true, if the index was dropped, false if an error has occured or the index does not exist.
   */
  public boolean dropIndex(final String name)
  {
    accessCheck(true);
    try {
      execute("DROP INDEX " + name);
      return true;
    } catch (final Throwable ex) {
      // Index does already exist (or an error has occurred).
      return false;
    }
  }

  /**
   * @param jdbcString
   * @see #execute(String, boolean)
   */
  public void execute(final String jdbcString)
  {
    execute(jdbcString, true);
  }

  /**
   * Executes the given String
   * @since 3.3.48
   * @param jdbcString
   * @param ignoreErrors If true (default) then errors will be caught and logged.
   * @return true if no error occurred (no exception was caught), otherwise false.
   */
  public boolean execute(final String jdbcString, final boolean ignoreErrors)
  {
    accessCheck(true);
    final JdbcTemplate jdbc = new JdbcTemplate(dataSource);
    log.info(jdbcString);
    if (ignoreErrors == true) {
      try {
        jdbc.execute(jdbcString);
      } catch (final Throwable ex) {
        log.info(ex.getMessage(), ex);
        return false;
      }
    } else {
      jdbc.execute(jdbcString);
    }
    return true;
  }

  public int queryForInt(final String jdbcQuery)
  {
    accessCheck(false);
    final JdbcTemplate jdbc = new JdbcTemplate(dataSource);
    log.info(jdbcQuery);
    return jdbc.queryForInt(jdbcQuery);
  }

  public void setAccessChecker(final AccessChecker accessChecker)
  {
    this.accessChecker = accessChecker;
  }

  public void setDataSource(final DataSource dataSource)
  {
    this.dataSource = dataSource;
  }
}
