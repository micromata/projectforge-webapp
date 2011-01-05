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
import org.projectforge.web.wicket.AbstractSelectPanel;
import org.projectforge.web.wicket.components.DatePanel;

/**
 * @author Kai Reinhard (k.reinhard@micromata.de)
 */
public class SelectLPanel extends AbstractLPanel
{
  private static final long serialVersionUID = 523715368835144558L;

  /**
   * Wicket id.
   */
  public static final String WICKET_ID_SELECT_PANEL = "selectPanel";

  private AbstractSelectPanel<?> selectPanel;

  /**
   * @see AbstractDOFormRenderer#createDateFieldPanel(String, LayoutLength, DatePanel)
   */
  SelectLPanel(final String id, final LayoutLength length, final AbstractSelectPanel<?> selectPanel)
  {
    super(id, length);
    this.selectPanel = selectPanel;
    add(selectPanel);
  }

  /**
   * Only for select panels which supports 
   * @see org.projectforge.web.wicket.layout.AbstractLPanel#setStrong()
   */
  public SelectLPanel setStrong()
  {
    this.classAttributeAppender = "strong";
    return this;
  }

  public AbstractSelectPanel<?> getSelectPanel()
  {
    return selectPanel;
  }

  @Override
  protected Component getClassModifierComponent()
  {
    return selectPanel.getClassModifierComponent();
  }
}
