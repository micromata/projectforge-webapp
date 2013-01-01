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

package org.projectforge.web.wicket.components;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.projectforge.scripting.GroovyResult;

/**
 * For displaying source-code. No syntax highlighting yet available.
 * @author Kai Reinhard (k.reinhard@micromata.de)
 * 
 */
public class SourceCodePanel extends Panel
{
  private static final long serialVersionUID = 2275250048929648255L;

  private final RepeatingView linesRepeater;

  private String sourceCode;

  /**
   * @param id
   * @param model
   */
  public SourceCodePanel(final String id)
  {
    super(id);
    add(linesRepeater = new RepeatingView("lines"));
  }

  /**
   * Sets the source code only and only if groovyResult is not null and has an exception.
   * @param sourceCode
   * @param groovyResult
   * @see GroovyResult#hasException()
   */
  public void setCode(final String sourceCode, final GroovyResult groovyResult) {
    if (groovyResult == null || groovyResult.hasException() == false) {
      setCode(null);
    } else {
      setCode(sourceCode);
    }
  }

  public void setCode(final String sourceCode)
  {
    if (StringUtils.equals(this.sourceCode, sourceCode) == true) {
      return;
    }
    this.sourceCode = sourceCode;
    linesRepeater.removeAll();
    final List<String> lines = getLines();
    int lineNo = 1;
    for (final String line : lines) {
      final WebMarkupContainer item = new WebMarkupContainer(linesRepeater.newChildId());
      linesRepeater.add(item);
      item.add(new Label("lineNo", String.valueOf(lineNo++)));
      final Label lineLabel = new Label("line", line.toString());
      // lineLabel.setEscapeModelStrings(false);
      item.add(lineLabel);
    }
  }

  /**
   * @return true, if source code is contained, otherwise false.
   * @see org.apache.wicket.Component#isVisible()
   */
  @Override
  public boolean isVisible()
  {
    return sourceCode != null;
  }

  /**
   * Gets the lines of the groovy script for displaying line number etc.
   * @return
   */
  private List<String> getLines()
  {
    final List<String> lines = new ArrayList<String>();
    if (sourceCode == null) {
      return lines;
    }
    StringBuffer line = new StringBuffer();
    for (int i = 0; i < sourceCode.length(); i++) {
      final char c = sourceCode.charAt(i);
      if (c == '\n') {
        lines.add(line.toString());
        line = new StringBuffer();
      } else {
        line.append(c);
      }
    }
    lines.add(line.toString());
    return lines;
  }
}
