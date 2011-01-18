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

package org.projectforge.web.wicket.layout;

import org.apache.wicket.Component;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.projectforge.web.wicket.components.JiraIssuesPanel;

/**
 * Represents a field set panel. A form or page can contain multiple field sets.
 * @author Kai Reinhard (k.reinhard@micromata.de)
 * 
 */
public class JiraIssuesLPanel extends AbstractLPanel implements IField
{
  private static final long serialVersionUID = 114326766516257034L;

  /**
   * Wicket id.
   */
  public static final String WICKET_ID = "label";

  protected JiraIssuesPanel jiraIssuesPanel;

  JiraIssuesLPanel(final String id, final LayoutLength length, final String text)
  {
    super(id, length);
    final WebMarkupContainer container = new WebMarkupContainer(WICKET_ID);
    add(container);
    this.jiraIssuesPanel = new JiraIssuesPanel("jiraIssuesPanel", text);
    container.add(jiraIssuesPanel);
  }

  public JiraIssuesLPanel setStrong()
  {
    this.classAttributeAppender = "text strong";
    return this;
  }

  public JiraIssuesLPanel setRequired()
  {
    return this;
  }

  public JiraIssuesLPanel setFocus()
  {
    return this;
  }

  @Override
  protected Component getClassModifierComponent()
  {
    return this;
  }
}
