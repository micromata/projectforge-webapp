/////////////////////////////////////////////////////////////////////////////
//
// Project ProjectForge Community Edition
//         www.projectforge.org
//
// Copyright (C) 2001-2011 Kai Reinhard (k.reinhard@me.com)
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

import java.util.Locale;

import org.junit.BeforeClass;
import org.junit.Test;
import org.projectforge.AppVersion;
import org.projectforge.core.ConfigXmlTest;

public class GroovyEngineTest
{
  @BeforeClass
  public static void setUp()
  {
    // Needed if this tests runs before the ConfigurationTest.
    ConfigXmlTest.createTestConfiguration();
  }

  @Test
  public void get()
  {
    final GroovyEngine engine = new GroovyEngine(Locale.GERMAN);
    engine.putVariable("name", "Kai");

    final String res = engine.executeTemplate("Hallo $name, your locale is '<%= pf.getString(\"locale.de\") %>'.");
    assertEquals("Hallo Kai, your locale is 'Deutsch'.", res);
    assertEquals("Hallo Kai, your locale is 'Deutsch'. " + AppVersion.APP_ID, engine.executeTemplateFile("scripting/template.txt"));
  }
}
