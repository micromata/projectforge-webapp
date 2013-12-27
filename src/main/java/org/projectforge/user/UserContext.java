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

package org.projectforge.user;

import java.io.Serializable;
import java.util.Collection;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.Validate;
import org.projectforge.multitenancy.TenantChecker;
import org.projectforge.multitenancy.TenantDO;
import org.projectforge.multitenancy.TenantsCache;
import org.projectforge.registry.Registry;
import org.projectforge.web.user.UserPreferencesHelper;

/**
 * User context for logged-in users. Contains the user and the current tenant (if any) etc.
 * @author Kai Reinhard (k.reinhard@micromata.de)
 */
public class UserContext implements Serializable
{
  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(UserContext.class);

  private static final long serialVersionUID = 4934701869144478233L;

  private static final String USER_PREF_KEY_CURRENT_TENANT = UserContext.class.getName() + ":currentTenantId";

  private PFUserDO user;

  private TenantDO currentTenant;

  private boolean stayLoggedIn;

  private boolean initialized;

  private UserContext()
  {
  }

  /**
   * Don't use this method. It's used for creating an UserContext without copying a user.
   * @param user
   * @return The created UserContext.
   */
  public static UserContext __internalCreateWithSpecialUser(final PFUserDO user)
  {
    final UserContext userContext = new UserContext();
    userContext.user = user;
    return userContext;
  }

  /**
   * Makes a copy of the given user and stores this user.
   * @param user
   */
  public UserContext(final PFUserDO user)
  {
    Validate.notNull(user);
    this.user = new PFUserDO();
    copyUser(user, this.user);
  }

  /**
   * Clear all fields (user etc.).
   * @return this for chaining.
   */
  public UserContext logout()
  {
    this.user = null;
    this.currentTenant = null;
    this.stayLoggedIn = false;
    return this;
  }

  /**
   * Refreshes the user stored in the user group cache. Ignore fields such as stayLoggedInKey, password and passwordSalt.
   * @return this for chaining.
   */
  public UserContext refreshUser()
  {
    final PFUserDO updatedUser = Registry.instance().getUserGroupCache().getUser(user.getId());
    if (updatedUser == null) {
      log.warn("Couldn't update user from UserGroupCache, should only occur in maintenance mode!");
      return this;
    }
    copyUser(updatedUser, user);
    return this;
  }

  private void copyUser(final PFUserDO srcUser, final PFUserDO destUser)
  {
    destUser.copyValuesFrom(srcUser, "password", "passwordSalt", "stayLoggedInKey");
  }

  /**
   * @return the user
   */
  public PFUserDO getUser()
  {
    return user;
  }

  /**
   * @return the currentTenant
   */
  public TenantDO getCurrentTenant()
  {
    if (initialized == false) {
      initialize();
    }
    return currentTenant;
  }

  /**
   * @param tenant the currentTenant to set
   * @return this for chaining.
   */
  public UserContext setCurrentTenant(final TenantDO tenant)
  {
    if (tenant == null) {
      log.warn("Can't switch to current tenant=null!");
      return this;
    }
    if (tenant.getId() == null) {
      log.warn("Can't switch to current tenant with id=null!");
      return this;
    }
    if (this.currentTenant != null && tenant.getId().equals(this.currentTenant.getId()) == false) {
      log.info("User switched the tenant: [" + tenant.getName() + "] (was [" + this.currentTenant.getName() + "]).");
      UserPreferencesHelper.putEntry(USER_PREF_KEY_CURRENT_TENANT, tenant.getId(), true);
    }
    this.currentTenant = tenant;
    return this;
  }

  /**
   * @return the stayLoggedIn
   */
  public boolean isStayLoggedIn()
  {
    return stayLoggedIn;
  }

  /**
   * @param stayLoggedIn the stayLoggedIn to set
   * @return this for chaining.
   */
  public UserContext setStayLoggedIn(final boolean stayLoggedIn)
  {
    this.stayLoggedIn = stayLoggedIn;
    return this;
  }

  private void initialize()
  {
    synchronized (this) {
      if (initialized == true || ThreadLocalUserContext.getUserContext() == null) {
        return;
      }
      if (user.getId() != null && TenantChecker.getInstance().isMultiTenancyAvailable() == true) {
        // Try to find the last used tenant of the user:
        final Integer currentTenantId = (Integer) UserPreferencesHelper.getEntry(USER_PREF_KEY_CURRENT_TENANT);
        final TenantsCache tenantsCache = Registry.instance().getTenantsCache();
        if (currentTenantId != null) {
          this.currentTenant = tenantsCache.getTenant(currentTenantId);
        } else {
          final Collection<TenantDO> tenants = tenantsCache.getTenantsOfUser(user.getId());
          if (CollectionUtils.isNotEmpty(tenants) == true) {
            setCurrentTenant(tenants.iterator().next());
          }
        }
      }
      initialized = true;
    }
  }
}
