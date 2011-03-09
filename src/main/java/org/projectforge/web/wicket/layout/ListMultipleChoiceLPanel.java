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
import org.apache.wicket.markup.html.form.ListMultipleChoice;

/**
 * Represents a field set panel. A form or page can contain multiple field sets.
 * @author Kai Reinhard (k.reinhard@micromata.de)
 * 
 */
public class ListMultipleChoiceLPanel extends AbstractLPanel
{
  private static final long serialVersionUID = 5113871779517795608L;

  /**
   * Wicket id.
   */
  public static final String SELECT_ID = "select";

  protected Component listMultipleChoice;

  /**
   */
  ListMultipleChoiceLPanel(final String id,  final ListMultipleChoice< ? > listMultipleChoice, final PanelContext ctx)
  {
    super(id, ctx);
    this.listMultipleChoice = listMultipleChoice;
    this.classAttributeAppender = "select";
    add(this.listMultipleChoice);
  }

  /**
   * Only used by DropDownChoiceMobileLPanel.
   * @param id
   * @param length
   */
  protected ListMultipleChoiceLPanel(final String id, final PanelContext ctx)
  {
    super(id, ctx);
  }

  /**
   * Does nothing.
   * @param label
   * @return
   */
  public ListMultipleChoiceLPanel setLabel(final String label)
  {
    return this;
  }
  
  public ListMultipleChoice< ? > getListMultipleChoice()
  {
    if (listMultipleChoice instanceof ListMultipleChoice< ? >) {
      return (ListMultipleChoice< ? >) listMultipleChoice;
    } else {
      return null;
    }
  }


  @Override
  public Component getWrappedComponent()
  {
    return listMultipleChoice;
  }

  @Override
  protected Component getClassModifierComponent()
  {
    return listMultipleChoice;
  }
}
