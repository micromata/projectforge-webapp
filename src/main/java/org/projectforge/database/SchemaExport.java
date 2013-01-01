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

import org.hibernate.HibernateException;
import org.hibernate.cfg.Configuration;
import org.hibernate.tool.hbm2ddl.SchemaUpdate;

/**
 * 
 * @author Kai Reinhard (k.reinhard@micromata.de)
 *
 */
public class SchemaExport
{
  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(SchemaExport.class);
  
  /** Updates the current DB-Schema with the schema defined by the hbm.xml files.
   * 
   * @param script Print the DDL to the console.
   * @param export
   */
  public void updateSchema(Configuration cfg, boolean script, boolean export) {
    try {
      SchemaUpdate exp = new SchemaUpdate(cfg);
      exp.execute(script, export);
    } catch (HibernateException ex) {
      log.fatal("Cant't update database schema: " + ex.getMessage(), ex);
      return;
    }
  }
  
  /**
   * Generates the database schema for the current configured database.
   * 
   * @param filename Write the schema to the given file. No file output, if
   *                  null.
   * @param script Print the DDL to the console.
   * @param export If true, the script will be executed (export the script to
   *                  the database).
   */
  public void exportSchema(Configuration cfg, String filename, boolean script, boolean export) {
    try {
      org.hibernate.tool.hbm2ddl.SchemaExport exp = new org.hibernate.tool.hbm2ddl.SchemaExport(cfg);
      exp.setOutputFile(filename);
      exp.setDelimiter(";\n");
      exp.create(script, export);
    } catch (HibernateException ex) {
      log.fatal("Cant't generate database schema: " + ex.getMessage(), ex);
      return;
    }
  }

  /**
   * Generates the database schema for the current configured database.
   * 
   * @param filename Write the schema to the given file. No file output, if
   *                  null.
   * @param script Print the DDL to the console.
   * @param export If true, the script will be executed (export the script to
   *                  the database).
   */
  public void exportDropSchema(Configuration cfg, String filename, boolean script, boolean export) {
    //String[] dropSQL;
    try {
      org.hibernate.tool.hbm2ddl.SchemaExport exp = new org.hibernate.tool.hbm2ddl.SchemaExport(cfg);
      exp.setOutputFile(filename);
      exp.setDelimiter(";\n");
      exp.drop(script, export);
    } catch (HibernateException ex) {
      log.error("Cant't generate drop statements for schema: " + ex.getMessage(), ex);
      return;
    }
  }
}
