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

package org.projectforge.fibu.kost;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;

/**
 * Speichert mehrere BWAs in einer Liste. Es kann eine Tabelle mit verschiedenen BWA-Spalten leicht erzeugt werden.
 * @author Kai Reinhard (k.reinhard@micromata.de)
 * 
 */
public class BwaTable
{
  private List<Bwa> bwaList;

  private BwaZeile[][] table;

  public BwaTable()
  {
    bwaList = new ArrayList<Bwa>();
  }

  public void addBwa(Bwa bwa)
  {
    bwaList.add(bwa);
  }

  /**
   * Gibt alle BwaZeilen als Tabelle zurück. In einer Zeile (erster Index) befinden sich BwaZeilen mit gleicher Zeilennummer, in einer
   * Spalte (2. Index) alle Werte der verschiedenen BWAs.
   * @return
   */
  public BwaZeile[][] getArray()
  {
    if (this.table != null) {
      return table;
    }
    if (CollectionUtils.isEmpty(bwaList) == true) {
      return null;
    }
    Bwa firstBwa = bwaList.get(0);
    int numberOfRows = firstBwa.getZeilen().size();
    int numberOfCols = bwaList.size();
    table = new BwaZeile[numberOfRows][numberOfCols];
    int col = 0;
    int row = 0;
    for (Bwa bwa : bwaList) {
      row = 0;
      for (BwaZeile zeile : bwa.getZeilen()) {
        table[row][col] = zeile;
        row++;
      }
      col++;
    }
    return table;
  }
}
