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

package org.projectforge.fibu;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.MapUtils;
import org.apache.log4j.Logger;
import org.projectforge.common.AbstractCache;
import org.springframework.orm.hibernate3.HibernateTemplate;


/**
 * Caches the DATEV accounts.
 * @author Kai Reinhard (k.reinhard@micromata.de)
 */
public class KontoCache extends AbstractCache
{
  private static Logger log = Logger.getLogger(KontoCache.class);

  private HibernateTemplate hibernateTemplate;

  /** The key is the order id. */
  private Map<Integer, KontoDO> accountMapById;

  public boolean isEmpty() {
    checkRefresh();
    return MapUtils.isEmpty(accountMapById);
  }

  public KontoDO getKonto(final Integer id)
  {
    if (id == null) {
      return null;
    }
    checkRefresh();
    return accountMapById.get(id);
  }

  /**
   * This method will be called by CacheHelper and is synchronized via getData();
   */
  @Override
  @SuppressWarnings("unchecked")
  protected void refresh()
  {
    log.info("Initializing KontoCache ...");
    // This method must not be synchronized because it works with a new copy of maps.
    final Map<Integer, KontoDO> map = new HashMap<Integer, KontoDO>();
    final List<KontoDO> list = hibernateTemplate.find("from KontoDO t where deleted=false");
    for (final KontoDO konto : list) {
      map.put(konto.getId(), konto);
    }
    this.accountMapById = map;
    log.info("Initializing of KontoCache done.");
  }

  public void setHibernateTemplate(final HibernateTemplate hibernateTemplate)
  {
    this.hibernateTemplate = hibernateTemplate;
  }
}
