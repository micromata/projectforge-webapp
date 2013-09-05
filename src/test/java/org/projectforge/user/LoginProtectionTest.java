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

import junit.framework.Assert;

import org.junit.Test;

public class LoginProtectionTest
{
  private static final long DURATION_48_HOURS = 48 * 60 * 60 * 1000;

  private static final long DURATION_4_HOURS = 4 * 60 * 60 * 1000;

  @Test
  public void testLoginProtection() throws InterruptedException
  {
    final long current = System.currentTimeMillis();
    final LoginProtection lp = LoginProtection.instance();
    lp.clearAll();
    Assert.assertEquals(0, lp.getSizeOfLastFailedLoginMap());
    Assert.assertEquals(0, lp.getSizeOfLoginFailedAttemptsMap());
    lp.incrementFailedLoginTimeOffset("kai");
    lp.incrementFailedLoginTimeOffset("kai");
    lp.incrementFailedLoginTimeOffset("kai");
    Assert.assertTrue(lp.getFailedLoginTimeOffsetIfExist("kai") > 0);
    Assert.assertTrue(lp.getFailedLoginTimeOffsetIfExist("kai") < 3001);
    Assert.assertEquals(0, (int) lp.getFailedLoginTimeOffsetIfExist("horst"));
    lp.setEntry("horst", 10, current - DURATION_48_HOURS); // Expired.
    lp.incrementFailedLoginTimeOffset("kai"); // 10 failed login attempts should be deleted now:
    Assert.assertEquals(1, lp.getSizeOfLastFailedLoginMap());
    Assert.assertEquals(1, lp.getSizeOfLoginFailedAttemptsMap());
    Assert.assertNull(lp.getNumberOfFailedLoginAttempts("horst"));
    Assert.assertEquals(0, (int) lp.getFailedLoginTimeOffsetIfExist("horst"));
    lp.setEntry("horst", 10, current - DURATION_4_HOURS); // Not expired.
    lp.incrementFailedLoginTimeOffset("kai");
    Assert.assertEquals(0, (int) lp.getFailedLoginTimeOffsetIfExist("horst"));
    lp.incrementFailedLoginTimeOffset("horst");
    Assert.assertEquals(11, (int) lp.getNumberOfFailedLoginAttempts("horst"));
    Assert.assertTrue(lp.getFailedLoginTimeOffsetIfExist("horst") > 0);
    Assert.assertTrue(lp.getFailedLoginTimeOffsetIfExist("horst") < 11001);
    lp.clearLoginTimeOffset("horst");
    Assert.assertEquals(0, (int) lp.getFailedLoginTimeOffsetIfExist("horst"));
    lp.incrementFailedLoginTimeOffset("horst");
    final long offset = lp.getFailedLoginTimeOffsetIfExist("horst");
    Assert.assertTrue(offset > 0 && offset < 1001);
    Thread.sleep(offset + 1);
    Assert.assertEquals(0, (int) lp.getFailedLoginTimeOffsetIfExist("horst"));
  }
}
