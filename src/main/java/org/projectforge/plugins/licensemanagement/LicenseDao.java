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

package org.projectforge.plugins.licensemanagement;

import java.util.Collection;

import org.apache.commons.collections.CollectionUtils;
import org.projectforge.common.StringHelper;
import org.projectforge.core.BaseDao;
import org.projectforge.user.PFUserDO;
import org.projectforge.user.UserRightId;
import org.projectforge.web.user.UsersProvider;

/**
 * 
 * @author Kai Reinhard (k.reinhard@micromata.de)
 * 
 */
public class LicenseDao extends BaseDao<LicenseDO>
{
  public static final UserRightId USER_RIGHT_ID = new UserRightId("PLUGIN_LICENSE_MANAGEMENT", "plugin100",
      "plugins.licensemanagement.license");

  private static final String[] ADDITIONAL_SEARCH_FIELDS = new String[] { "owners"};


  private final UsersProvider usersProvider = new UsersProvider();

  @Override
  protected String[] getAdditionalSearchFields()
  {
    return ADDITIONAL_SEARCH_FIELDS;
  }

  public LicenseDao()
  {
    super(LicenseDO.class);
    userRightId = USER_RIGHT_ID;
  }

  /**
   * Please note: Only the string license.owners will be modified (but not be saved)!
   * @param license
   * @param owners Full list of all owners (user id's) which have are assigned to this license.
   * @return
   */
  public void setOwners(final LicenseDO license, final Collection<PFUserDO> owners)
  {
    license.setOwnerIds(usersProvider.getUserIds(owners));
  }

  public Collection<PFUserDO> getSortedOwners(final LicenseDO license)
  {
    return usersProvider.getSortedUsers(license.getOwnerIds());
  }

  public String getSortedOwnernames(final LicenseDO license)
  {
    final Collection<PFUserDO> sortedOwners = getSortedOwners(license);
    if (CollectionUtils.isEmpty(sortedOwners) == true) {
      return "";
    }
    final StringBuffer buf = new StringBuffer();
    boolean first = true;
    for (final PFUserDO owner : sortedOwners) {
      first = StringHelper.append(buf, first, owner.getFullname(), ", ");
    }
    return buf.toString();
  }

  @Override
  public LicenseDO newInstance()
  {
    return new LicenseDO();
  }
}
