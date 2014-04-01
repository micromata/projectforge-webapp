/////////////////////////////////////////////////////////////////////////////
//
// Project   ProjectForge
//
// Copyright 2001-2009, Micromata GmbH, Kai Reinhard
//           All rights reserved.
//
/////////////////////////////////////////////////////////////////////////////

package org.projectforge.addresses;

import org.projectforge.core.BaseDao;

/**
 * @author Kai Reinhard (k.reinhard@micromata.de)
 *
 */
public class AddressEntryDao extends BaseDao<AddressEntryDO>
{

  /**
   * @param clazz
   */
  protected AddressEntryDao(final Class<AddressEntryDO> clazz)
  {
    super(AddressEntryDO.class);
  }

  /**
   * @see org.projectforge.core.BaseDao#newInstance()
   */
  @Override
  public AddressEntryDO newInstance()
  {
    return new AddressEntryDO();
  }

}
