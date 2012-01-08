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

package org.projectforge.web.wicket.layout;

import org.apache.wicket.Component;
import org.projectforge.web.wicket.components.DatePanel;

/**
 * Represents a field set panel. A form or page can contain multiple field sets.
 * @author Kai Reinhard (k.reinhard@micromata.de)
 * 
 */
public class DateFieldLPanel extends AbstractLPanel
{
  private static final long serialVersionUID = 5771712946605166500L;

  /**
   * Wicket id.
   */
  public static final String DATE_FIELD_ID = "dateField";

  private DatePanel datePanel;

  /**
   * @see AbstractFormRenderer#createDateFieldPanel(String, LayoutLength, DatePanel)
   */
  DateFieldLPanel(final String id, final DatePanel datePanel, final PanelContext ctx)
  {
    super(id, ctx);
    this.datePanel = datePanel;
    this.classAttributeAppender = "text";
    add(datePanel);
  }

  @Deprecated
  public DateFieldLPanel setStrong()
  {
    this.classAttributeAppender = "text strong";
    return this;
  }

  @Deprecated
  public DateFieldLPanel setRequired()
  {
    datePanel.setRequired(true);
    return this;
  }

  @Deprecated
  public DateFieldLPanel setFocus()
  {
    datePanel.setFocus();
    return this;
  }

  public DatePanel getDatePanel()
  {
    return datePanel;
  }

  @Override
  protected Component getClassModifierComponent()
  {
    return datePanel.getDateField();
  }
}
