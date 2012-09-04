/////////////////////////////////////////////////////////////////////////////
//
// Project ProjectForge Community Edition
//         www.projectforge.org
//
// Copyright (C) 2001-2012 Kai Reinhard (k.reinhard@micromata.com)
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

package org.projectforge.ldap;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.projectforge.fibu.kost.AccountingConfig;
import org.projectforge.xml.stream.AliasMap;
import org.projectforge.xml.stream.XmlObjectReader;

public class LdapRealTestBase
{
  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(LdapRealTestBase.class);

  private static final String CONFIG_FILE = System.getProperty("user.home") + "/ProjectForge/testldapConfig.xml";

  LdapConfig cfg;

  LdapConnector ldapConnector;

  protected void setup()
  {
    cfg = readConfig();
    ldapConnector = new LdapConnector(cfg);
  }

  static LdapConfig readConfig()
  {
    final File configFile = new File(CONFIG_FILE);
    if (configFile.canRead() == false) {
      return null;
    }
    final XmlObjectReader reader = new XmlObjectReader();
    final AliasMap aliasMap = new AliasMap();
    aliasMap.put(LdapConfig.class, "ldapConfig");
    reader.setAliasMap(aliasMap);
    AccountingConfig.registerXmlObjects(reader, aliasMap);
    String xml = null;
    try {
      xml = FileUtils.readFileToString(configFile, "UTF-8");
    } catch (final IOException ex) {
      log.error(ex.getMessage(), ex);
      throw new IllegalArgumentException("Cannot read config file '" + CONFIG_FILE + "' properly : " + ex.getMessage(), ex);
    }
    if (xml == null) {
      throw new IllegalArgumentException("Cannot read from config file: '" + CONFIG_FILE + "'.");
    }
    try {
      final LdapConfig cfg = (LdapConfig) reader.read(xml);
      final String warnings = reader.getWarnings();
      if (StringUtils.isNotBlank(warnings) == true) {
        log.error(warnings);
      }
      return cfg;
    } catch (final Throwable ex) {
      throw new IllegalArgumentException("Cannot read config file '" + CONFIG_FILE + "' properly : " + ex.getMessage(), ex);
    }
  }

}
