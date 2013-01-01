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

package org.projectforge.core;

import static org.junit.Assert.assertEquals;

import org.junit.Test;


public class BaseDaoTest
{
  @Test
  public void modifySearchString()
  {
    assertEquals("hallo*", BaseDao.modifySearchString("hallo"));
    assertEquals("hallo* ProjectForge*", BaseDao.modifySearchString("hallo ProjectForge"));
    assertEquals("ha1lo* ProjectForge*", BaseDao.modifySearchString("ha1lo ProjectForge"));
    assertEquals("k.reinhard@projectforge*", BaseDao.modifySearchString("k.reinhard@projectforge"));
    assertEquals("email:k.reinhard@projectforge", BaseDao.modifySearchString("email:k.reinhard@projectforge"));
    assertEquals("hallo", BaseDao.modifySearchString("'hallo"));
    assertEquals("title:hallo", BaseDao.modifySearchString("'title:hallo"));
    assertEquals("hallo* AND test* NOT hurz* OR test*", BaseDao.modifySearchString("hallo AND test NOT hurz OR test"));
    assertEquals("hallo* AND 2008-11-21 NOT hurz* OR test*", BaseDao.modifySearchString("hallo AND 2008-11-21 NOT hurz OR test"));
    assertEquals("-hallo", BaseDao.modifySearchString("-hallo"));
    assertEquals("+hallo", BaseDao.modifySearchString("+hallo"));
    assertEquals("+hallo", BaseDao.modifySearchString("+hallo"));
    assertEquals("h+a-llo", BaseDao.modifySearchString("h+a-llo"));
    assertEquals("hu-melder", BaseDao.modifySearchString("hu-melder"));
    assertEquals("*h+a-llo*", BaseDao.modifySearchString("*h+a-llo*"));
  }
}
