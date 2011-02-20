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

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.Map;

import org.apache.commons.jelly.JellyContext;
import org.apache.commons.jelly.JellyException;
import org.apache.commons.jelly.XMLOutput;
import org.projectforge.AppVersion;
import org.xml.sax.InputSource;


/**
 * Helper for running and processing jelly scripts.
 * @author Kai Reinhard (k.reinhard@micromata.de)
 * 
 */
public class JellyExecutor
{
  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(JellyExecutor.class);

  /**
   * Run the given Jelly script and hands over all objects defined in data.
   * @param is Jelly script.
   * @param data Data available as variables in Jelly script (variable name ist the map entry key and value is the map entry value).
   * @return
   */
  public static String runJelly(InputStream is, Map<String, Object> data)
  {
    StringWriter writer = new StringWriter();
    JellyContext context = new JellyContext();
    // Set the value of a <param> in the stylesheet
    for (Map.Entry<String, Object> entry : data.entrySet()) {
      context.setVariable(entry.getKey(), entry.getValue());
    }
    context.setVariable("appId", AppVersion.APP_ID);
    context.setVariable("appVersion", AppVersion.NUMBER);
    XMLOutput xmlOutput = XMLOutput.createXMLOutput(writer, false);

    InputSource src = new InputSource(is);
    try {
      context.runScript(src, xmlOutput);
      xmlOutput.flush();
    } catch (JellyException ex) {
      log.error(ex.getMessage(), ex);
      throw new RuntimeException(ex);
    } catch (IOException ex) {
      log.error(ex.getMessage(), ex);
      throw new RuntimeException(ex);
    }
    return writer.toString();
  }
}
