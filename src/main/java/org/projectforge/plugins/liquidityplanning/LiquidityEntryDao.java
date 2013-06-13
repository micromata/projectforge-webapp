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

import java.util.List;

import org.projectforge.core.BaseDao;
import org.projectforge.user.UserRightId;

/**
 * 
 * @author Kai Reinhard (k.reinhard@micromata.de)
 * 
 */
public class LiquidityEntryDao extends BaseDao<LiquidityEntryDO>
{
  public static final UserRightId USER_RIGHT_ID = new UserRightId("PLUGIN_LIQUIDITY_PLANNING", "fibu10", "plugins.liquidityplanning.menu");;

  public LiquidityEntryDao()
  {
    super(LiquidityEntryDO.class);
    userRightId = USER_RIGHT_ID;
  }

  public LiquidityEntriesStatistics buildStatistics(final List<LiquidityEntryDO> list)
  {
    final LiquidityEntriesStatistics stats = new LiquidityEntriesStatistics();
    if (list == null) {
      return stats;
    }
    for (final LiquidityEntryDO entry : list) {
      stats.add(entry);
    }
    return stats;
  }


  @Override
  public LiquidityEntryDO newInstance()
  {
    return new LiquidityEntryDO();
  }

  /**
   * @see org.projectforge.core.BaseDao#useOwnCriteriaCacheRegion()
   */
  @Override
  protected boolean useOwnCriteriaCacheRegion()
  {
    return true;
  }
}
