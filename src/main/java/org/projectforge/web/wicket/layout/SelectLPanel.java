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
import org.projectforge.web.wicket.AbstractSelectPanel;
import org.projectforge.web.wicket.WicketUtils;
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

  private AbstractSelectPanel< ? > selectPanel;

  /**
   * @see AbstractFormRenderer#createDateFieldPanel(String, LayoutLength, DatePanel)
   */
  SelectLPanel(final String id, final AbstractSelectPanel< ? > selectPanel, final PanelContext ctx)
  {
    super(id, ctx);
    this.selectPanel = selectPanel;
    add(selectPanel);
    if (ctx.isStrong() == true) {
      this.classAttributeAppender = "strong";
    }
  }

  public AbstractSelectPanel< ? > getSelectPanel()
  {
    return selectPanel;
  }
  
  public SelectLPanel setTooltip(final String tooltip)
  {
    WicketUtils.addTooltip(selectPanel, tooltip);
    return this;
  }

  @Override
  protected Component getClassModifierComponent()
  {
    return selectPanel.getClassModifierComponent();
  }

  @Override
  public Component getWrappedComponent()
  {
    return selectPanel.getWrappedComponent();
  }
}
