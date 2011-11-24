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

package org.projectforge.plugins.marketing;

import org.apache.wicket.MarkupContainer;
import org.projectforge.web.wicket.layout.AbstractFormRenderer;
import org.projectforge.web.wicket.layout.LayoutContext;
import org.projectforge.web.wicket.layout.LayoutLength;
import org.projectforge.web.wicket.layout.PanelContext;

/**
 * This layout class is easy to use and generates read-only views as well as edit formulars for browsers and mobile devices.
 * @author Kai Reinhard (k.reinhard@micromata.de)
 *
 */
public class CampaignFormRenderer extends AbstractFormRenderer
{
  private static final long serialVersionUID = 5545284725937684187L;

  private final CampaignDO data;

  final static LayoutLength labelLength = LayoutLength.HALF;

  final static LayoutLength valueLength = LayoutLength.DOUBLE;

  public CampaignFormRenderer(final MarkupContainer container, final LayoutContext layoutContext, final CampaignDO data)
  {
    super(container, layoutContext);
    this.data = data;
  }

  @Override
  public void add()
  {
    doPanel.newFieldSetPanel(getString("plugins.marketing.campaign"));
    doPanel.addTextField(new PanelContext(data, "title", valueLength, getString("title"), labelLength).setRequired()
        .setStrong());
    doPanel.addTextField(new PanelContext(data, "values", valueLength, getString("values"), labelLength).setRequired());
    doPanel.addTextArea(new PanelContext(data, "comment", valueLength, getString("comment"), labelLength)
    .setCssStyle("height: 20em;"));
  }
}
