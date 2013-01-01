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

package org.projectforge.plugins.marketing;

import org.projectforge.address.AddressDO;
import org.projectforge.admin.UpdateEntry;
import org.projectforge.admin.UpdateEntryImpl;
import org.projectforge.admin.UpdatePreCheckStatus;
import org.projectforge.admin.UpdateRunningStatus;
import org.projectforge.database.DatabaseUpdateDao;
import org.projectforge.database.Table;
import org.projectforge.database.TableAttribute;
import org.projectforge.database.TableAttributeType;

/**
 * Contains the initial data-base set-up script and later all update scripts if any data-base schema updates are required by any later
 * release of this to-do plugin. <br/>
 * This is a part of the convenient auto update functionality of ProjectForge. You only have to insert update methods here for any further
 * release (with e. g. required data-base modifications). ProjectForge will do the rest.
 * @author Kai Reinhard (k.reinhard@micromata.de)
 */
public class MarketingPluginUpdates
{
  static DatabaseUpdateDao dao;

  @SuppressWarnings("serial")
  public static UpdateEntry getInitializationUpdateEntry()
  {
    return new UpdateEntryImpl(MarketingPlugin.ADDRESS_CAMPAIGN_ID, "1.0.0", "2011-11-24", "Adds tables T_PLUGIN_MARKETING_*.") {
      final Table addressCampaignTable = new Table(AddressCampaignDO.class);

      final Table addressCampaignValueTable = new Table(AddressCampaignValueDO.class);

      @Override
      public UpdatePreCheckStatus runPreCheck()
      {
        // Does the data-base table already exist?
        return dao.doesExist(addressCampaignTable, addressCampaignValueTable) ? UpdatePreCheckStatus.ALREADY_UPDATED
            : UpdatePreCheckStatus.OK;
      }

      @Override
      public UpdateRunningStatus runUpdate()
      {
        // Create initial data-base table:
        Table table = new Table(AddressCampaignDO.class) //
        .addDefaultBaseDOAttributes() //
        .addAttributes("title", "values", "comment");
        dao.createTable(table);
        table = new Table(AddressCampaignValueDO.class) //
        .addDefaultBaseDOAttributes() //
        .addAttributes("value", "comment") //
        .addAttribute(new TableAttribute("address_fk", TableAttributeType.INT).setForeignTable(AddressDO.class)) //
        .addAttribute(new TableAttribute("address_campaign_fk", TableAttributeType.INT).setForeignTable(AddressCampaignDO.class));
        dao.createTable(table);
        dao.addUniqueConstraint(table, "t_address_campaign_value_unique", "address_fk", "address_campaign_fk");
        return UpdateRunningStatus.DONE;
      }
    };
  }
}
