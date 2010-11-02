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

package org.projectforge.fibu.kost.reporting;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.projectforge.fibu.kost.BuchungssatzDO;
import org.projectforge.fibu.kost.Bwa;
import org.projectforge.reporting.Buchungssatz;
import org.projectforge.reporting.impl.BuchungssatzImpl;
import org.projectforge.reporting.impl.ReportBwaImpl;


public class ReportGenerator
{
  private String jasperReportId;

  private Map<String, Object> parameters = new HashMap<String, Object>();

  private ReportOutputType outputType = ReportOutputType.PDF;

  private Collection< ? > beanCollection;

  /**
   * If given then the jasper report with this id will be used for generating the report. If not given then the default report will be used.
   * @return
   */
  public String getJasperReportId()
  {
    return jasperReportId;
  }

  public void setJasperReportId(String jasperReportId)
  {
    this.jasperReportId = jasperReportId;
  }

  /**
   * Adds all lines of the given bwa as parameters.
   * @see ReportBwaImpl#putBwaWerte(Map, Bwa)
   */
  public void addBwa(Bwa bwa)
  {
    Bwa.putBwaWerte(parameters, bwa);
  }

  /**
   * Creates a new Bwa from the given buchungsSaetze and adds all lines of the resulting bwa as parameters.
   * @see ReportBwaImpl#putBwaWerte(Map, Bwa)
   */
  public Bwa addBwa(List<BuchungssatzDO> buchungsSaetze)
  {
    Bwa bwa = new Bwa(buchungsSaetze);
    addBwa(bwa);
    return bwa;
  }

  /**
   * Adds a parameter which is accessible under the name from inside the JasperReport.
   */
  public void addParameter(String name, Object value)
  {
    parameters.put(name, value);
  }

  public Object getParameter(String name)
  {
    return parameters.get(name);
  }

  /**
   * Attention: Overwrites any existing parameter!
   * @param parameters
   */
  public void setParameters(Map<String, Object> parameters)
  {
    this.parameters = parameters;
  }
  
  public Map<String, Object> getParameters()
  {
    return parameters;
  }

  /**
   * The bean collection used by the JasperReport.
   * @param beanCollection
   */
  public Collection< ? > getBeanCollection()
  {
    return beanCollection;
  }

  /**
   * Converts any collection of BuchungssatzDO into list of Buchungssatz.
   * @param beanCollection
   */
  public void setBeanCollection(Collection< ? > beanCollection)
  {
    if (CollectionUtils.isEmpty(beanCollection) == true) {
      this.beanCollection = beanCollection;
      return;
    }
    Iterator< ? > it = beanCollection.iterator();
    if (it.next() instanceof BuchungssatzDO == true) {
      List<Buchungssatz> list = new ArrayList<Buchungssatz>();
      @SuppressWarnings("unchecked")
      Collection<BuchungssatzDO> col = (Collection<BuchungssatzDO>)beanCollection;
      for (BuchungssatzDO buchungssatzDO : col) {
        Buchungssatz satz = new BuchungssatzImpl(buchungssatzDO);
        list.add(satz);
      }
      this.beanCollection = list;
    } else {
      this.beanCollection = beanCollection;
    }
  }

  public ReportOutputType getOutputType()
  {
    return outputType;
  }

  public void setOutputType(ReportOutputType outputType)
  {
    this.outputType = outputType;
  }

  public void setOutputType(String outputType)
  {
    this.outputType = ReportOutputType.getType(outputType);
  }
}
