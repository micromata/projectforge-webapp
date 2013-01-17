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

package org.projectforge.ldap;

import org.junit.Assert;
import org.junit.Test;

public class LdapSambaAccountConfigTest
{
  @Test
  public void getSambaSID()
  {
    LdapSambaAccountsConfig config = new LdapSambaAccountsConfig().setSambaSIDPrefix("123-456");
    Assert.assertEquals("123-456-789", config.getSambaSID(789));
    Assert.assertEquals("123-456-???", config.getSambaSID(null));
    config = new LdapSambaAccountsConfig().setSambaSIDPrefix(null);
    Assert.assertEquals("S-000-000-000-789", config.getSambaSID(789));
    Assert.assertEquals("S-000-000-000-???", config.getSambaSID(null));
  }

  @Test
  public void getSambaSIDNumber()
  {
    final LdapSambaAccountsConfig config = new LdapSambaAccountsConfig().setSambaSIDPrefix("123-456");
    Assert.assertEquals(789, config.getSambaSIDNumber("123-456-789").intValue());
    Assert.assertEquals(789, config.getSambaSIDNumber("-789").intValue());
    Assert.assertEquals(1, config.getSambaSIDNumber("-1").intValue());
    Assert.assertNull(config.getSambaSIDNumber("123456789"));
    Assert.assertNull(config.getSambaSIDNumber(""));
    Assert.assertNull(config.getSambaSIDNumber("-"));
  }
}
