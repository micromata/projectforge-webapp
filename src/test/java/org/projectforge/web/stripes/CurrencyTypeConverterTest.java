/////////////////////////////////////////////////////////////////////////////
//
// Project ProjectForge Community Edition
//         www.projectforge.org
//
// Copyright (C) 2001-2010 Kai Reinhard (k.reinhard@me.com)
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

package org.projectforge.web.stripes;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.projectforge.web.stripes.CurrencyTypeConverter;


public class CurrencyTypeConverterTest
{
  CurrencyTypeConverter converter = new CurrencyTypeConverter();

  @Test
  public void listToString()
  {
    // assertNumbers("0", "0,00 €");
    assertEquals("0,00", converter.modifyCurrencyString("0,00 €"));
    assertEquals("0,00", converter.modifyCurrencyString("0,00"));
    assertEquals("0,00 0 €", converter.modifyCurrencyString("0,00 0 €"));
    assertEquals(null, converter.modifyCurrencyString(null));
    assertEquals("", converter.modifyCurrencyString(""));
    assertEquals("€", converter.modifyCurrencyString("€"));
  }

  /*
   * private void assertNumbers(String s1, String s2) { Collection<ValidationError> col = new ArrayList<ValidationError>(); BigDecimal n1 =
   * new BigDecimal(s1); BigDecimal n2 = converter.convert(s1, BigDecimal.class, col); assertNotNull("BigDecimal shouldn't be null.", n1);
   * assertNotNull("BigDecimal shouldn't be null.", n2); assertTrue("BigDecimals should be equal.", n1.compareTo(n2) == 0); }
   */
}
