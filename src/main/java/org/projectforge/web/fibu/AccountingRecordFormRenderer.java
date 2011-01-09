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

package org.projectforge.web.fibu;

import static org.projectforge.web.wicket.layout.LayoutLength.HALF;
import static org.projectforge.web.wicket.layout.LayoutLength.QUART;
import static org.projectforge.web.wicket.layout.LayoutLength.THREEQUART;
import static org.projectforge.web.wicket.layout.TextFieldLPanel.INPUT_ID;

import java.util.Date;

import org.apache.wicket.MarkupContainer;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.util.convert.IConverter;
import org.projectforge.fibu.kost.BuchungssatzDO;
import org.projectforge.web.wicket.WicketUtils;
import org.projectforge.web.wicket.components.DatePanel;
import org.projectforge.web.wicket.components.DatePanelSettings;
import org.projectforge.web.wicket.components.RequiredMinMaxNumberField;
import org.projectforge.web.wicket.converter.IntegerConverter;
import org.projectforge.web.wicket.layout.AbstractDOFormRenderer;
import org.projectforge.web.wicket.layout.DateFieldLPanel;
import org.projectforge.web.wicket.layout.LayoutContext;
import org.projectforge.web.wicket.layout.TextFieldLPanel;

public class AccountingRecordFormRenderer extends AbstractDOFormRenderer
{
  private static final long serialVersionUID = -3418428748298018009L;

  private BuchungssatzDO data;

  public AccountingRecordFormRenderer(final MarkupContainer container, final LayoutContext layoutContext, final BuchungssatzDO data)
  {
    super(container, layoutContext);
    this.data = data;
  }

  @SuppressWarnings("serial")
  @Override
  public void add()
  {
    final DatePanel datePanel = new DatePanel(DateFieldLPanel.DATE_FIELD_ID, new PropertyModel<Date>(data, "datum"),
        new DatePanelSettings().withTargetType(java.sql.Date.class));
    WicketUtils.setReadonly(datePanel.getDateField());
    doPanel.addDateFieldPanel(data, "datum", getString("date"), HALF, datePanel, HALF);
    final String monthLabel = getString("calendar.month");
    final TextFieldLPanel textFieldPanel = (TextFieldLPanel) doPanel.addTextField(data, "year", getString("calendar.year") + "/" + monthLabel,
        HALF, QUART);
    WicketUtils.setReadonly(textFieldPanel.getTextField());
    final RequiredMinMaxNumberField<Integer> monthField = new RequiredMinMaxNumberField<Integer>(INPUT_ID, monthLabel,
        new PropertyModel<Integer>(data, "month"), 1, 12) {
      @Override
      public IConverter getConverter(Class< ? > type)
      {
        return new IntegerConverter(2);
      }
    };
    doPanel.addTextField(monthField, QUART);
    WicketUtils.setReadonly(monthField);

    final String satzNrLabel = getString("fibu.buchungssatz.satznr");
    final RequiredMinMaxNumberField<Integer> satzNrField = new RequiredMinMaxNumberField<Integer>(INPUT_ID, satzNrLabel,
        new PropertyModel<Integer>(data, "satznr"), 1, 99999) {
      @Override
      public IConverter getConverter(Class< ? > type)
      {
        return new IntegerConverter(5);
      }
    };
    WicketUtils.setReadonly(satzNrField);
    doPanel.addTextField(satzNrLabel, HALF, satzNrField, QUART);

    // <tr>
    // <th><fmt:message key="fibu.common.betrag" />&nbsp;<fmt:message key="fibu.buchungssatz.sh" /></th>
    // <td><stripes:text class="stdtext" readonly="true" name="buchungssatz.betrag" style="width:8em;" formatType="currency"
    // formatPattern="decimal" /> <stripes:text size="6" readonly="true" name="buchungssatz.sh" /></td>
    // <th><fmt:message key="fibu.buchungssatz.beleg" /></th>
    // <td><stripes:text class="stdtext" readonly="true" name="buchungssatz.beleg" /></td>
    // </tr>
    // <tr>
    // <th><fmt:message key="fibu.kost1" /></th>
    // <td><stripes:text class="stdtext" style="width:8em;" readonly="true" name="kost1" /></td>
    // <th><fmt:message key="fibu.kost2" /></th>
    // <td><stripes:text class="stdtext" style="width:8em;" readonly="true" name="kost2" /></td>
    // </tr>
    // <tr>
    // <th><fmt:message key="fibu.buchungssatz.konto" /></th>
    // <td><stripes:text class="stdtext" style="width:8em;" readonly="true" name="konto" /></td>
    // <th><fmt:message key="fibu.buchungssatz.gegenKonto" /></th>
    // <td><stripes:text class="stdtext" style="width:8em;" readonly="true" name="gegenKonto" /></td>
    // </tr>
    // <tr>
    // <th><fmt:message key="fibu.buchungssatz.text" /></th>
    // <td><stripes:text class="stdtext" readonly="true" name="buchungssatz.text" /></td>
    // <th><fmt:message key="fibu.buchungssatz.menge" /></th>
    // <td><stripes:text class="stdtext" readonly="true" name="buchungssatz.menge" /></td>
    // </tr>
    // <tr>
    // <th><fmt:message key="comment" /></th>
    // <td colspan="3"><stripes:textarea class="stdtext" readonly="true" name="buchungssatz.comment" style="height:20ex;" /></td>

  }
}
