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

package org.projectforge.plugins.banking;

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
public class BankAccountEditFormRenderer extends AbstractFormRenderer
{
  private static final long serialVersionUID = 8697166062199594608L;

  private final BankAccountDO data;

  final static LayoutLength labelLength = LayoutLength.HALF;

  final static LayoutLength valueLength = LayoutLength.DOUBLE;

  public BankAccountEditFormRenderer(final MarkupContainer container, final LayoutContext layoutContext, final BankAccountDO data)
  {
    super(container, layoutContext);
    this.data = data;
  }

  @Override
  public void add()
  {
    doPanel.newFieldSetPanel(getString("plugins.banking.account"));
    doPanel.addTextField(new PanelContext(data, "name", valueLength, getString("plugins.banking.account.name"), labelLength).setRequired()
        .setStrong());
    doPanel.addTextField(new PanelContext(data, "accountNumber", valueLength, getString("plugins.banking.account.number"), labelLength)
    .setRequired());
    doPanel.addTextField(new PanelContext(data, "bank", valueLength, getString("plugins.banking.bank"), labelLength));
    doPanel.addTextField(new PanelContext(data, "bankIdentificationCode", valueLength,
        getString("plugins.banking.bankIdentificationCode"), labelLength).setRequired().setStrong());
    doPanel.addTextArea(new PanelContext(data, "description", valueLength, getString("description"), labelLength)
    .setCssStyle("height: 10em;"));
  }
}
