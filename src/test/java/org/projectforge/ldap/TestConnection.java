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

/**
 * 
 */
package org.projectforge.ldap;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.projectforge.fibu.kost.AccountingConfig;
import org.projectforge.xml.stream.AliasMap;
import org.projectforge.xml.stream.XmlObjectReader;

/**
 * @author kai
 * 
 */
public class TestConnection
{
  private static final String CONFIG_FILE = System.getProperty("user.home") + "/ProjectForge/testldapConfig.xml";

  public static void main(final String[] args) throws Exception
  {
    new TestConnection().run();
  }

  private void run() throws Exception
  {
    final LdapConfig cfg = readConfig();
    final LdapConnector ldapConnector = new LdapConnector(cfg);
    // new LdapTemplate(ldapConnector) {
    // @Override
    // protected Object call() throws NameNotFoundException, NamingException
    // {
    // // Read supportedSASLMechanisms from root DSE
    // final Attributes attrs = ctx.getAttributes(cfg.getUrl(), new String[] { "supportedSASLMechanisms"});
    //
    // final String SEARCH_BASE = "ou=users";
    // final Object obj = ctx.lookup(SEARCH_BASE);
    //
    // final String accountName = "kai";
    // final List list = new ArrayList();
    //
    // final String searchFilter = "(&(objectClass=person)(uid=" + accountName + "))";
    //
    // // System.out.println(searchFilter);
    //
    // final SearchControls searchControls = new SearchControls();
    // final String[] resultAttributes = { "sn", "givenName", "uid"};
    // searchControls.setReturningAttributes(resultAttributes);
    // searchControls.setSearchScope(SearchControls.SUBTREE_SCOPE);
    //
    // results = ctx.search(SEARCH_BASE, searchFilter, searchControls);
    //
    // while (results.hasMoreElements()) {
    // final SearchResult searchResult = results.nextElement();
    // list.add(searchResult.toString());
    // System.out.println(searchResult.toString());
    // }
    // return null;
    // }
    // }.excecute();

    final PersonDao dao = new PersonDao();
    dao.ldapConnector = ldapConnector;
    final List<Person> list = dao.findAll();
    final Person person = new Person().setUid("42").setCommonName("h.meier").setSurname("Meier").setGivenName("Horst").setOrganisationalUnitName("kunden,users")
        .setDescription("Test entry from ProjectForge dev system.");
    dao.create(person);
    person.setSurname("Changed");
    dao.update(person);
    dao.delete(person);
  }

  private LdapConfig readConfig()
  {
    final File configFile = new File(CONFIG_FILE);
    if (configFile.canRead() == false) {
      throw new IllegalArgumentException("Cannot read from config file: '" + CONFIG_FILE + "'.");
    }
    final XmlObjectReader reader = new XmlObjectReader();
    final AliasMap aliasMap = new AliasMap();
    aliasMap.put(LdapConfig.class, "ldapConfig");
    reader.setAliasMap(aliasMap);
    // reader.initialize(ConfigXml.class);
    AccountingConfig.registerXmlObjects(reader, aliasMap);
    String xml = null;
    try {
      xml = FileUtils.readFileToString(configFile, "UTF-8");
    } catch (final IOException ex) {
      ex.printStackTrace(System.err);
      throw new IllegalArgumentException("Cannot read config file '" + CONFIG_FILE + "' properly : " + ex.getMessage(), ex);
    }
    if (xml == null) {
      throw new IllegalArgumentException("Cannot read from config file: '" + CONFIG_FILE + "'.");
    }
    try {
      final LdapConfig cfg = (LdapConfig) reader.read(xml);
      final String warnings = reader.getWarnings();
      if (StringUtils.isNotBlank(warnings) == true) {
        System.err.println(warnings);
      }
      return cfg;
    } catch (final Throwable ex) {
      throw new IllegalArgumentException("Cannot read config file '" + CONFIG_FILE + "' properly : " + ex.getMessage(), ex);
    }
  }
}
