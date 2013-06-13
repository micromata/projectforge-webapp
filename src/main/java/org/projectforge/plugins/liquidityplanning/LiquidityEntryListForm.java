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

package org.projectforge.plugins.liquidityplanning;

import org.apache.log4j.Logger;
import org.apache.wicket.model.Model;
import org.projectforge.core.BaseSearchFilter;
import org.projectforge.core.CurrencyFormatter;
import org.projectforge.web.wicket.AbstractListForm;
import org.projectforge.web.wicket.WebConstants;
import org.projectforge.web.wicket.flowlayout.DivTextPanel;
import org.projectforge.web.wicket.flowlayout.FieldsetPanel;
import org.projectforge.web.wicket.flowlayout.TextStyle;

/**
 * The list formular for the list view (this example has no filter settings). See ToDoListPage for seeing how to use filter settings.
 * @author Kai Reinhard (k.reinhard@micromata.de)
 * 
 */
public class LiquidityEntryListForm extends AbstractListForm<BaseSearchFilter, LiquidityEntryListPage>
{
  private static final long serialVersionUID = 2040255193023406307L;

  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(LiquidityEntryListForm.class);

  public LiquidityEntryListForm(final LiquidityEntryListPage parentPage)
  {
    super(parentPage);
  }

  private LiquidityEntriesStatistics getStats()
  {
    return parentPage.getStatistics();
  }

  /**
   * @see org.projectforge.web.wicket.AbstractListForm#init()
   */
  @SuppressWarnings("serial")
  @Override
  protected void init()
  {
    super.init();
    gridBuilder.newGridPanel();
    {
      // Statistics
      final FieldsetPanel fs = gridBuilder.newFieldset(getString("statistics")).suppressLabelForWarning();
      fs.add(new DivTextPanel(fs.newChildId(), new Model<String>() {
        @Override
        public String getObject()
        {
          return getString("fibu.rechnung.status.bezahlt")
              + ": "
              + CurrencyFormatter.format(getStats().getPayed())
              + WebConstants.HTML_TEXT_DIVIDER;
        }
      }));
      fs.add(new DivTextPanel(fs.newChildId(), new Model<String>() {
        @Override
        public String getObject()
        {
          return getString("totalSum") + ": " + CurrencyFormatter.format(getStats().getTotal()) + WebConstants.HTML_TEXT_DIVIDER;
        }
      }));
      fs.add(new DivTextPanel(fs.newChildId(), new Model<String>() {
        @Override
        public String getObject()
        {
          return getString("fibu.rechnung.offen") + ": " + CurrencyFormatter.format(getStats().getOpen()) + WebConstants.HTML_TEXT_DIVIDER;
        }
      }, TextStyle.BLUE));
      fs.add(new DivTextPanel(fs.newChildId(), new Model<String>() {
        @Override
        public String getObject()
        {
          return getString("fibu.rechnung.filter.ueberfaellig") + ": " + CurrencyFormatter.format(getStats().getOverdue());
        }
      }, TextStyle.RED));
    }
  }

  @Override
  protected BaseSearchFilter newSearchFilterInstance()
  {
    return new BaseSearchFilter();
  }

  @Override
  protected Logger getLogger()
  {
    return log;
  }
}
