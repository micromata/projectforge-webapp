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

package org.projectforge.web.wicket.converter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import java.math.BigDecimal;
import java.util.Locale;

import org.junit.BeforeClass;
import org.junit.Test;
import org.projectforge.core.ConfigXmlTest;


public class CurrencyConverterTest
{
  private static String CURRENCY;

  @BeforeClass
  public static void setUp()
  {
    CURRENCY = ConfigXmlTest.createTestConfiguration().getCurrencySymbol();
  }

  private Locale locale = Locale.GERMAN;

  private CurrencyConverter con = new CurrencyConverter();

  @Test
  public void convertToObject()
  {
    assertNull(con.convertToObject(null, locale));
    assertNull(con.convertToObject("", locale));
    myAssertEquals(BigDecimal.ZERO, con.convertToObject("0", locale));
    myAssertEquals(BigDecimal.ZERO, con.convertToObject("0 " + CURRENCY, locale));
    myAssertEquals(BigDecimal.ZERO, con.convertToObject("0" + CURRENCY, locale));
    myAssertEquals(BigDecimal.ZERO, con.convertToObject("0,00 " + CURRENCY, locale));
    myAssertEquals(new BigDecimal("-10"), con.convertToObject("-10,00 " + CURRENCY, locale));
    myAssertEquals(new BigDecimal("1234.56"), con.convertToObject("1.234,56 " + CURRENCY, locale));
  }

  @Test
  public void convertToString()
  {
    assertEquals("", con.convertToString(null, locale));
    assertEquals("0,00 " + CURRENCY, con.convertToString(BigDecimal.ZERO, locale));
    assertEquals("-1.234,56 " + CURRENCY, con.convertToString(new BigDecimal("-1234.56"), locale));
  }

  @Test
  public void totalAmount()
  {
    final CurrencyConverter converter = new CurrencyConverter(new BigDecimal("200"));
    myAssertEquals(BigDecimal.ZERO, converter.convertToObject("0", locale));
    myAssertEquals(BigDecimal.ZERO, converter.convertToObject("0 " + CURRENCY, locale));
    myAssertEquals(BigDecimal.ZERO, converter.convertToObject("0" + CURRENCY, locale));
    myAssertEquals(BigDecimal.ZERO, converter.convertToObject("0,00 " + CURRENCY, locale));
    myAssertEquals(new BigDecimal("-10"), converter.convertToObject("-10,00 " + CURRENCY, locale));
    myAssertEquals(new BigDecimal("1234.56"), converter.convertToObject("1.234,56 " + CURRENCY, locale));
    myAssertEquals(new BigDecimal("100"), converter.convertToObject("50%", locale));
    myAssertEquals(new BigDecimal("0"), converter.convertToObject("0 %", locale));
    myAssertEquals(new BigDecimal("200"), converter.convertToObject("100,0 %", locale));
    myAssertEquals(new BigDecimal("85.4"), converter.convertToObject("42,7 %", locale));
  }

  private void myAssertEquals(BigDecimal expected, BigDecimal actual)
  {
    if (expected.compareTo(actual) != 0) {
      fail("BigDecimals not equal: expected=" + expected + ", actual=" + actual);
    }
  }
}
