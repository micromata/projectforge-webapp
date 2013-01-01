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

package org.projectforge.scripting;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Locale;
import java.util.TimeZone;

import org.junit.BeforeClass;
import org.junit.Test;
import org.projectforge.AppVersion;
import org.projectforge.core.ConfigXmlTest;
import org.projectforge.core.DisplayHistoryEntry;
import org.projectforge.core.Priority;
import org.projectforge.plugins.todo.ToDoDO;
import org.projectforge.plugins.todo.ToDoType;
import org.projectforge.user.PFUserDO;

public class GroovyEngineTest
{
  // private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(GroovyEngineTest.class);

  @BeforeClass
  public static void setUp()
  {
    // Needed if this tests runs before the ConfigurationTest.
    ConfigXmlTest.createTestConfiguration();
  }

  @Test
  public void renderTest()
  {
    final GroovyEngine engine = new GroovyEngine(Locale.GERMAN, TimeZone.getTimeZone("UTC"));
    engine.putVariable("name", "Kai");

    final String res = engine.executeTemplate("Hallo $name, your locale is '<%= pf.getI18nString(\"locale.de\") %>'.");
    assertEquals("Hallo Kai, your locale is 'Deutsch'.", res);
    assertEquals("Hallo Kai, your locale is 'Deutsch'. " + AppVersion.APP_ID + " Finished: Englisch", engine
        .executeTemplateFile("scripting/template.txt"));
  }

  @Test
  public void mailTemplateTest()
  {
    final GroovyEngine engine = new GroovyEngine(Locale.GERMAN,  TimeZone.getTimeZone("UTC"));
    engine.putVariable("recipient", new PFUserDO().setFirstname("Kai").setLastname("Reinhard"));
    engine.putVariable("todo", new ToDoDO().setType(ToDoType.IMPROVEMENT).setPriority(Priority.HIGH));
    engine.putVariable("history", new ArrayList<DisplayHistoryEntry>());
    engine.putVariable("requestUrl", "https://localhost:8443/wa/toDoEditPage/id/42");
    final String result = engine.executeTemplateFile("mail/todoChangeNotification.html");
    assertTrue("I18n priorty expected.", result.contains("hoch"));
    assertTrue("I18n key for type improvement expected.", result.contains("???plugins.todo.type.improvement???"));
  }
  
  @Test
  public void preprocesTest()
  {
    final GroovyEngine engine = new GroovyEngine(Locale.GERMAN,  TimeZone.getTimeZone("UTC"));
    assertNull(engine.preprocessGroovyXml(null));
    assertEquals("", engine.preprocessGroovyXml(""));
    assertEquals("<% if (value != null) { %>", engine.preprocessGroovyXml("<groovy>if (value != null) {</groovy>"));
    assertEquals("<%= value %>", engine.preprocessGroovyXml("<groovy-out>value</groovy-out>"));
  }
}
