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

package org.projectforge.web.fibu;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.wicket.PageParameters;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.projectforge.calendar.DayHolder;
import org.projectforge.common.StringHelper;
import org.projectforge.fibu.ProjektDO;
import org.projectforge.fibu.ProjektDao;
import org.projectforge.fibu.RechnungDO;
import org.projectforge.fibu.RechnungDao;
import org.projectforge.fibu.RechnungStatus;
import org.projectforge.fibu.RechnungTyp;
import org.projectforge.fibu.RechnungsPositionDO;
import org.projectforge.web.wicket.AbstractBasePage;
import org.projectforge.web.wicket.AbstractEditPage;
import org.projectforge.web.wicket.EditPage;


@EditPage(defaultReturnPage = RechnungListPage.class)
public class RechnungEditPage extends AbstractEditPage<RechnungDO, RechnungEditForm, RechnungDao> implements ISelectCallerPage
{
  private static final long serialVersionUID = 2561721641251015056L;

  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(RechnungEditPage.class);

  @SpringBean(name = "rechnungDao")
  private RechnungDao rechnungDao;

  @SpringBean(name = "projektDao")
  private ProjektDao projektDao;

  public RechnungEditPage(PageParameters parameters)
  {
    super(parameters, "fibu.rechnung");
    init();
    if (isNew() == true) {
      final DayHolder day = new DayHolder();
      getData().setDatum(day.getSQLDate());
      getData().setStatus(RechnungStatus.GESTELLT);
      getData().setTyp(RechnungTyp.RECHNUNG);
    }
    getData().recalculate(); // Muss immer gemacht werden, damit das Zahlungsziel in Tagen berechnet wird.
  }

  @Override
  public AbstractBasePage onSaveOrUpdate()
  {
    if (isNew() == true && getData().getNummer() == null && getData().getTyp() != RechnungTyp.GUTSCHRIFTSANZEIGE_DURCH_KUNDEN) {
      getData().setNummer(rechnungDao.getNextNumber(getData()));
    }
    return null;
  }

  @Override
  protected RechnungDao getBaseDao()
  {
    return rechnungDao;
  }

  @Override
  protected RechnungEditForm newEditForm(AbstractEditPage< ? , ? , ? > parentPage, RechnungDO data)
  {
    return new RechnungEditForm(this, data);
  }

  @Override
  protected Logger getLogger()
  {
    return log;
  }

  /**
   * Clones the data positions and reset the date and target date etc.
   */
  protected void cloneRechnung()
  {
    log.info("Clone of invoice chosen: " + getData());
    final RechnungDO rechnung = getData();
    rechnung.setId(null);
    rechnung.setNummer(null);
    final int zahlungsZielInTagen = rechnung.getZahlungsZielInTagen();
    final DayHolder day = new DayHolder();
    rechnung.setDatum(day.getSQLDate());
    day.add(Calendar.DAY_OF_MONTH, zahlungsZielInTagen);
    rechnung.setFaelligkeit(day.getSQLDate());
    rechnung.setZahlBetrag(null);
    rechnung.setBezahlDatum(null);
    rechnung.setStatus(RechnungStatus.GESTELLT);
    final List<RechnungsPositionDO> positionen = getData().getPositionen();
    if (positionen != null) {
      rechnung.setPositionen(new ArrayList<RechnungsPositionDO>());
      for (final RechnungsPositionDO origPosition : positionen) {
        final RechnungsPositionDO position = (RechnungsPositionDO)origPosition.newClone();
        rechnung.addPosition(position);
      }
    }
    form.refresh();
    form.cloneButtonPanel.setVisible(false);
  }

  public void cancelSelection(String property)
  {
    // Do nothing.
  }

  public void select(String property, Object selectedValue)
  {
    if ("projektId".equals(property) == true) {
      rechnungDao.setProjekt(getData(), (Integer) selectedValue);
      if (getData().getProjektId() != null
          && getData().getProjektId() >= 0
          && getData().getKundeId() == null
          && StringUtils.isBlank(getData().getKundeText()) == true) {
        // User has selected a project and the kunde is not set:
        final ProjektDO projekt = projektDao.getById(getData().getProjektId());
        if (projekt != null) {
          rechnungDao.setKunde(getData(), projekt.getKundeId());
        }
      }
    } else if ("kundeId".equals(property) == true) {
      rechnungDao.setKunde(getData(), (Integer) selectedValue);
    } else if (StringHelper.isIn(property, "datum", "faelligkeit", "bezahlDatum") == true) {
      final Date date = (Date) selectedValue;
      final java.sql.Date sqlDate = new java.sql.Date(date.getTime());
      if ("datum".equals(property) == true) {
        getData().setDatum(sqlDate);
        form.datumPanel.markModelAsChanged();
      } else if ("faelligkeit".equals(property) == true) {
        getData().setFaelligkeit(sqlDate);
        form.faelligkeitPanel.markModelAsChanged();
      } else if ("bezahlDatum".equals(property) == true) {
        getData().setBezahlDatum(sqlDate);
        form.bezahlDatumPanel.markModelAsChanged();
      }
    } else {
      log.error("Property '" + property + "' not supported for selection.");
    }
  }

  public void unselect(String property)
  {
    if ("projektId".equals(property) == true) {
      getData().setProjekt(null);
    } else if ("kundeId".equals(property) == true) {
      getData().setKunde(null);
    } else {
      log.error("Property '" + property + "' not supported for selection.");
    }
  }
}
