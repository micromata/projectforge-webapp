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

package org.projectforge.common;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.projectforge.common.StringHelper;

public class StringHelperTest
{
  @Test
  public void append()
  {
    StringBuffer buf = new StringBuffer();
    boolean first = StringHelper.append(buf, true, null, ",");
    first = StringHelper.append(buf, first, "", ",");
    first = StringHelper.append(buf, first, "1", ",");
    first = StringHelper.append(buf, first, "2", ",");
    assertEquals("1,2", buf.toString());
  }

  @Test
  public void listToString()
  {
    List<String> list = new ArrayList<String>();
    list.add("Micromata");
    list.add("Computer");
    list.add("IT-Services");
    assertEquals("Micromata,Computer,IT-Services", StringHelper.listToString(list, ",", false));
    assertEquals("Computer, IT-Services, Micromata", StringHelper.listToString(list, ", ", true));
    assertEquals("Micromata,Computer,IT-Services", StringHelper.listToString(",", "Micromata", "Computer", "IT-Services"));
    assertEquals("Micromata", StringHelper.listToString(",", "Micromata"));
    assertEquals("(Micromata == ?) and (Computer == ?) and (IT-Services == ?)", StringHelper.listToExpressions(" and ", "(", " == ?)",
        "Micromata", "Computer", "IT-Services"));
    assertEquals("(Micromata == ?)", StringHelper.listToExpressions(" and ", "(", " == ?)", "Micromata"));

    assertEquals("a,b,c", StringHelper.listToString(",", "a", null, "b", "", "c"));

    assertEquals("1,2", StringHelper.listToString(",", new Object[] { 1, 2}));
  }

  @Test
  public void sortAndUnique()
  {
    assertNull(StringHelper.sortAndUnique(null));
    compareStringArray(new String[] {}, StringHelper.sortAndUnique(new String[] {}));
    compareStringArray(new String[] { "hallo"}, StringHelper.sortAndUnique(new String[] { "hallo"}));
    compareStringArray(new String[] { "hallo"}, StringHelper.sortAndUnique(new String[] { "hallo", "hallo"}));
    compareStringArray(new String[] { "1", "2", "3"}, StringHelper.sortAndUnique(new String[] { "1", "3", "2", "1", "3"}));
  }

  @Test
  public void isIn()
  {
    assertEquals(true, StringHelper.isIn("open", new String[] { "open", "close", "explore", "implore"}));
    assertEquals(true, StringHelper.isIn("close", new String[] { "open", "close", "explore", "implore"}));
    assertEquals(true, StringHelper.isIn("explore", new String[] { "open", "close", "explore", "implore"}));
    assertEquals(true, StringHelper.isIn("implore", new String[] { "open", "close", "explore", "implore"}));
    assertEquals(false, StringHelper.isIn("pen", new String[] { "open", "close", "explore", "implore"}));
    assertEquals(false, StringHelper.isIn(null, new String[] { "open", "close", "explore", "implore"}));
  }

  @Test
  public void startsWith()
  {
    assertEquals(true, StringHelper.startsWith("Hurzel", "Hu"));
    assertEquals(false, StringHelper.startsWith(null, "Hu"));
    assertEquals(false, StringHelper.startsWith(null, null));
    try {
      assertEquals(false, StringHelper.startsWith("Hurzel", null));
      fail();
    } catch (NullPointerException ex) {
    }
    assertEquals(false, StringHelper.startsWith("Hurzel", "foo"));
  }

  @Test
  public void format2DigitNumber()
  {
    assertEquals("00", StringHelper.format2DigitNumber(0));
    assertEquals("01", StringHelper.format2DigitNumber(1));
    assertEquals("09", StringHelper.format2DigitNumber(9));
    assertEquals("10", StringHelper.format2DigitNumber(10));
    assertEquals("23", StringHelper.format2DigitNumber(23));
    assertEquals("99", StringHelper.format2DigitNumber(99));
    assertEquals("100", StringHelper.format2DigitNumber(100));
  }

  @Test
  public void format3DigitNumber()
  {
    assertEquals("000", StringHelper.format3DigitNumber(0));
    assertEquals("001", StringHelper.format3DigitNumber(1));
    assertEquals("099", StringHelper.format3DigitNumber(99));
    assertEquals("999", StringHelper.format3DigitNumber(999));
    assertEquals("1000", StringHelper.format3DigitNumber(1000));
  }

  @Test
  public void removeNonDigits()
  {
    assertEquals("", StringHelper.removeNonDigits(null));
    assertEquals("", StringHelper.removeNonDigits("a"));
    assertEquals("1", StringHelper.removeNonDigits("1"));
    assertEquals("495613167930", StringHelper.removeNonDigits("+49 561 / 316793 - 0"));
    assertEquals("056131679311", StringHelper.removeNonDigits("0561 / 316793-11"));
  }

  @Test
  public void removeNonDigitsAndNonASCIILetters()
  {
    assertEquals("", StringHelper.removeNonDigitsAndNonASCIILetters(null));
    assertEquals("", StringHelper.removeNonDigitsAndNonASCIILetters("."));
    assertEquals("e1", StringHelper.removeNonDigitsAndNonASCIILetters(".éeö1-.;:_'*`´ $%&/()=@"));
  }

  @Test
  public void abbreviate()
  {
    int[] maxWidth = new int[] { 5, 5, 100};
    String str = StringHelper.abbreviate(new String[] { "1", "Hello", "ProjectForge"}, maxWidth, 22, ": ");
    assertEquals("1: Hello: ProjectForge", str);
    assertTrue(str.length() <= 22);
    str = StringHelper.abbreviate(new String[] { "11234567", "Hello, how are you?",
        "ProjectForge is the world fines Project management app."}, maxWidth, 22, ": ");
    assertTrue(str.length() == 22);
    assertEquals("11...: He...: Proje...", str);
    str = StringHelper.abbreviate(new String[] { null, "1", "ProjectForge is the world fines Project management app."}, maxWidth, 22, ": ");
    assertTrue(str.length() == 22);
    assertEquals("1: ProjectForge is ...", str);
  }

  @Test
  public void getWildcardString()
  {
    assertEquals("", StringHelper.getWildcardString((String[]) null));
    assertEquals("", StringHelper.getWildcardString(""));
    assertEquals("", StringHelper.getWildcardString("", null, ""));
    assertEquals("", StringHelper.getWildcardString("hallo", null, "hallo"));
    assertEquals("", StringHelper.getWildcardString(null, "hallo", "hallo"));
    assertEquals("", StringHelper.getWildcardString(null, null, null));
    assertEquals("", StringHelper.getWildcardString("", "", ""));
    assertEquals("", StringHelper.getWildcardString("h", "h", ""));
    assertEquals("h", StringHelper.getWildcardString("h", "h", "h"));
    assertEquals("h", StringHelper.getWildcardString("hallo", "hurz", "house"));
    assertEquals("hallo", StringHelper.getWildcardString("hallo", "hallo", "hallo"));
    assertEquals("hallo", StringHelper.getWildcardString("hallo1", "hallo2", "hallo3"));
  }

  @Test
  public void checkPhoneNumberFormat()
  {
    assertTrue(StringHelper.checkPhoneNumberFormat(null));
    assertTrue(StringHelper.checkPhoneNumberFormat(""));
    assertTrue(StringHelper.checkPhoneNumberFormat(" "));
    assertTrue(StringHelper.checkPhoneNumberFormat("+49 561 316793-0"));
    assertFalse("+490561 123456 not allowed.", StringHelper.checkPhoneNumberFormat("+490561 123456"));
    assertFalse("+49 0561 123456 not allowed.", StringHelper.checkPhoneNumberFormat("+49 0561 123456"));
    assertFalse("+49 0561 123456 not allowed.", StringHelper.checkPhoneNumberFormat("+49     0561 123456"));
    assertFalse("Leading country code expected.", StringHelper.checkPhoneNumberFormat("0561 316793-0"));
    assertFalse("+ is only allowed at first char", StringHelper.checkPhoneNumberFormat("+49 561 316793+0"));
  }

  @Test
  public void hideStringEnding()
  {
    assertEquals(null, StringHelper.hideStringEnding(null, 'x', 3));
    assertEquals("0170 12345xxx", StringHelper.hideStringEnding("0170 12345678", 'x', 3));
    assertEquals("0xxx", StringHelper.hideStringEnding("0170", 'x', 3));
    assertEquals("xxx", StringHelper.hideStringEnding("017", 'x', 3));
    assertEquals("xx", StringHelper.hideStringEnding("01", 'x', 3));
    assertEquals("x", StringHelper.hideStringEnding("0", 'x', 3));
    assertEquals("", StringHelper.hideStringEnding("", 'x', 3));
  }

  @Test
  public void splitToInts()
  {
    compareIntArray(new int[] { 1, 111, 5, 11}, StringHelper.splitToInts("1.111.05.11", "."));
    compareIntArray(new int[] { 1, 0, 5, 11}, StringHelper.splitToInts("1.null.05.11", "."));
  }

  @Test
  public void splitAndTrim()
  {
    assertNull(StringHelper.splitAndTrim(null, ","));
    compareStringArray(new String[] {}, StringHelper.splitAndTrim("", ","));
    compareStringArray(new String[] { "a", "b"}, StringHelper.splitAndTrim("a, b", ","));
    compareStringArray(new String[] { "a", "b"}, StringHelper.splitAndTrim(",a,,, b,", ","));
    compareStringArray(new String[] { "a", "", "b c"}, StringHelper.splitAndTrim(",a, ,, b c,", ","));
  }

  @Test
  public void isNotBlank()
  {
    assertEquals(false, StringHelper.isNotBlank());
    assertEquals(false, StringHelper.isNotBlank((String[]) null));
    assertEquals(false, StringHelper.isNotBlank(null, ""));
    assertEquals(false, StringHelper.isNotBlank(null, "", " \t\n"));
    assertEquals(true, StringHelper.isNotBlank("a"));
    assertEquals(true, StringHelper.isNotBlank(null, "a", ""));
  }

  @Test
  public void compareTo()
  {
    assertEquals(0, StringHelper.compareTo(null, null));
    assertTrue(StringHelper.compareTo(null, "") < 0);
    assertTrue(StringHelper.compareTo(null, "Hurzel") < 0);
    assertTrue(StringHelper.compareTo("", null) > 0);
    assertEquals(0, StringHelper.compareTo("", ""));
    assertTrue(StringHelper.compareTo("Hurzel", null) > 0);
    assertTrue(StringHelper.compareTo("", "Test") < 0);
    assertTrue(StringHelper.compareTo("Anton", "Berta") < 0);
    assertEquals(0, StringHelper.compareTo("Anton", "Anton"));
    assertTrue(StringHelper.compareTo("Berta", "") > 0);
    assertTrue(StringHelper.compareTo("Berta", "Anton") > 0);
    assertEquals(0, StringHelper.compareTo("Anton", "Anton"));
  }

  @Test
  public void asHex()
  {
    assertEquals("", StringHelper.asHex(null));
    assertEquals("", StringHelper.asHex(new byte[] {}));
    assertEquals("00", StringHelper.asHex(new byte[] { 0}));
    assertEquals("000a0f", StringHelper.asHex(new byte[] { 0, 0x0a, 0x0f}));
    assertEquals("000aef", StringHelper.asHex(new byte[] { 0, 0x0a, (byte) 0xef}));
  }

  private void compareIntArray(int[] a1, int[] a2)
  {
    assertEquals(a1.length, a2.length);
    for (int i = 0; i < a1.length; i++) {
      assertEquals(a1[i], a2[i]);
    }
  }

  private void compareStringArray(String[] a1, String[] a2)
  {
    assertEquals(a1.length, a2.length);
    for (int i = 0; i < a1.length; i++) {
      assertEquals(a1[i], a2[i]);
    }
  }
}
