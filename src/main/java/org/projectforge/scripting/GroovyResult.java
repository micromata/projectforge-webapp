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

import java.io.Serializable;

import org.apache.commons.lang.StringUtils;
import org.projectforge.web.HtmlHelper;


public class GroovyResult implements Serializable
{
  private static final long serialVersionUID = -4561647483563741849L;

  private Object result;

  private Exception exception;

  private String output;

  public GroovyResult()
  {
  }

  public GroovyResult(Exception ex)
  {
    this.exception = ex;
  }

  public void setResult(Object result)
  {
    this.result = result;
  }

  public boolean hasResult()
  {
    return result != null;
  }

  public Object getResult()
  {
    return result;
  }

  /**
   * Escapes all html characters. If Groovy result is from type string then all '\n' will be replaced by "<br/>\n".
   * @return
   */
  public String getResultAsHtmlString()
  {
    if (result == null) {
      return null;
    }
    String esc = HtmlHelper.escapeXml(result.toString());
    if (result instanceof String) {
      return StringUtils.replace(esc, "\n", "<br/>\n");
    }
    return esc;
  }

  public boolean hasException()
  {
    return exception != null;
  }

  public Exception getException()
  {
    return exception;
  }

  public String getOutput()
  {
    return output;
  }

  public void setOutput(String output)
  {
    this.output = output;
  }
}
