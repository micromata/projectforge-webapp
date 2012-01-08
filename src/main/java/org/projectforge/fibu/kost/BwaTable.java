/////////////////////////////////////////////////////////////////////////////
//
// Project ProjectForge Community Edition
//         www.projectforge.org
//
// Copyright (C) 2001-2012 Kai Reinhard (k.reinhard@micromata.com)
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

import org.projectforge.common.LabelValueBean;

/**
 * Speichert mehrere BWAs in einer Liste. Es kann eine Tabelle mit verschiedenen BWA-Spalten leicht erzeugt werden.
 * @author Kai Reinhard (k.reinhard@micromata.de)
 * 
 */
public class BwaTable
{
  private List<LabelValueBean<String, Bwa>> bwaList;

  public BwaTable()
  {
    bwaList = new ArrayList<LabelValueBean<String, Bwa>>();
  }

  public List<LabelValueBean<String, Bwa>> getBwaList()
  {
    return bwaList;
  }

  public void addBwa(final String label, final Bwa bwa)
  {
    bwaList.add(new LabelValueBean<String, Bwa>(label, bwa));
  }
}
