/////////////////////////////////////////////////////////////////////////////
//
// Project ProjectForge Community Edition
//         www.projectforge.org
//
// Copyright (C) 2001-2011 Kai Reinhard (k.reinhard@me.com)
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

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.Iterator;
import java.util.List;

import org.junit.Test;

public class CampaignDOTest
{
  @Test
  public void getValuesAsArray()
  {
    assertNull(getValues(null));
    assertNull(getValues(""));
    assertNull(getValues("   "));
    assertArrayEquals(new String[] { "true"}, getValues("true; "));
    assertArrayEquals(new String[] { "true", "false"}, getValues("true; false"));
  }

  @Test
  public void getValuesAsList()
  {
    testValuesAsList("", (String[])null);
    testValuesAsList("true", "true");
    testValuesAsList("true; false", "true", "false");
  }

  private String[] getValues(final String values)
  {
    final AddressCampaignDO campaign = new AddressCampaignDO();
    campaign.setValues(values);
    final String[] result = campaign.getValuesArray();
    assertArrayEquals(result, AddressCampaignDO.getValuesArray(campaign.getValues()));
    return result;
  }

  private void testValuesAsList(final String valuesAsSingleString, final String... values)
  {
    final AddressCampaignDO campaign = new AddressCampaignDO();
    campaign.setValues(valuesAsSingleString);
    final List<String> result = campaign.getValuesList();
    if (values == null) {
      assertNull(result);
      return;
    }
    final Iterator<String> it = result.iterator();
    for (final String value : values) {
      assertEquals(value, it.next());
    }
  }
}
