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

package org.projectforge.plugins.memo;

import org.apache.wicket.MarkupContainer;
import org.projectforge.web.wicket.layout.AbstractDOFormRenderer;
import org.projectforge.web.wicket.layout.LayoutContext;
import org.projectforge.web.wicket.layout.LayoutLength;
import org.projectforge.web.wicket.layout.PanelContext;

/**
 * This layout class is easy to use and generates read-only views as well as edit formulars for browsers and mobile devices.
 * @author Kai Reinhard (k.reinhard@micromata.de)
 *
 */
public class MemoFormRenderer extends AbstractDOFormRenderer
{
  private static final long serialVersionUID = 8697166062199594608L;

  private MemoDO data;

  final static LayoutLength labelLength = LayoutLength.HALF;

  final static LayoutLength valueLength = LayoutLength.DOUBLE;

  public MemoFormRenderer(final MarkupContainer container, final LayoutContext layoutContext, final MemoDO data)
  {
    super(container, layoutContext);
    this.data = data;
  }

  @Override
  public void add()
  {
    doPanel.newFieldSetPanel(getString("plugins.memo.memo"));
    doPanel.addTextField(new PanelContext(data, "subject", valueLength, getString("plugins.memo.subject"), labelLength).setRequired()
        .setStrong());
    doPanel.addTextArea(new PanelContext(data, "memo", valueLength, getString("plugins.memo.memo"), labelLength)
        .setCssStyle("height: 50em;"));
  }
}
