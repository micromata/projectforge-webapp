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
import org.projectforge.web.wicket.components.DatePanel;
import org.projectforge.web.wicket.components.DateTimePanel;

/**
 * Represents a field set panel. A form or page can contain multiple field sets.
 * @author Kai Reinhard (k.reinhard@micromata.de)
 * 
 */
public class DateTimeFieldLPanel extends AbstractLPanel
{
  private static final long serialVersionUID = 4453864060379946168L;

  /**
   * Wicket id.
   */
  public static final String DATE_TIME_FIELD_ID = "dateTimeField";

  private DateTimePanel dateTimePanel;

  /**
   * @see AbstractDOFormRenderer#createDateFieldPanel(String, LayoutLength, DatePanel)
   */
  DateTimeFieldLPanel(final String id, final LayoutLength length, final DateTimePanel dateTimePanel)
  {
    super(id, length);
    this.dateTimePanel = dateTimePanel;
    this.classAttributeAppender = "text";
    add(dateTimePanel);
  }

  public DateTimeFieldLPanel setStrong()
  {
    this.classAttributeAppender = "text strong";
    return this;
  }

  public DateTimeFieldLPanel setRequired()
  {
    dateTimePanel.setRequired(true);
    return this;
  }

  public DateTimeFieldLPanel setFocus()
  {
    dateTimePanel.setFocus();
    return this;
  }

  public DateTimePanel getDatePanel()
  {
    return dateTimePanel;
  }

  @Override
  protected Component getClassModifierComponent()
  {
    return dateTimePanel.getDateField();
  }
}
