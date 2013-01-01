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

package org.projectforge.common;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.math.BigDecimal;

import org.junit.Test;
import org.projectforge.fibu.RechnungsPositionDO;
import org.projectforge.fibu.kost.KostZuweisungDO;
import org.projectforge.fibu.kost.KostZuweisungenCopyHelper;

public class ListCopyHelperTest
{
  @Test
  public void copy()
  {
    final KostZuweisungenCopyHelper lch = new KostZuweisungenCopyHelper();
    final RechnungsPositionDO srcPos = new RechnungsPositionDO();
    final RechnungsPositionDO destPos = new RechnungsPositionDO();
    lch.mycopy(srcPos.getKostZuweisungen(), destPos.getKostZuweisungen(), destPos);
    assertNull(destPos.getKostZuweisungen());
    srcPos.addKostZuweisung(new KostZuweisungDO().setNetto(BigDecimal.ONE).setComment("1"));
    lch.mycopy(srcPos.getKostZuweisungen(), destPos.getKostZuweisungen(), destPos);
    assertEquals(1, destPos.getKostZuweisungen().size());
    destPos.addKostZuweisung(new KostZuweisungDO().setNetto(BigDecimal.ONE).setComment("1"));
    assertEquals(2, destPos.getKostZuweisungen().size());
    lch.mycopy(srcPos.getKostZuweisungen(), destPos.getKostZuweisungen(), destPos);
    assertEquals(1, destPos.getKostZuweisungen().size());

    srcPos.getKostZuweisung(0).setNetto(BigDecimal.TEN).setComment("10");
    lch.mycopy(srcPos.getKostZuweisungen(), destPos.getKostZuweisungen(), destPos);
    assertEquals(1, destPos.getKostZuweisungen().size());
    assertEquals(BigDecimal.TEN, destPos.getKostZuweisung(0).getNetto());
    assertEquals("10", destPos.getKostZuweisung(0).getComment());

    srcPos.deleteKostZuweisung(0);
    lch.mycopy(srcPos.getKostZuweisungen(), destPos.getKostZuweisungen(), destPos);
    assertEquals(0, destPos.getKostZuweisungen().size());
  }
}
