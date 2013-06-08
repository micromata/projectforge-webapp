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

package org.projectforge.plugins.liquidityplanning;

import java.math.BigDecimal;
import java.util.Date;

import org.apache.log4j.Logger;
import org.apache.wicket.model.PropertyModel;
import org.projectforge.core.Constants;
import org.projectforge.web.wicket.AbstractEditForm;
import org.projectforge.web.wicket.WicketUtils;
import org.projectforge.web.wicket.components.DatePanel;
import org.projectforge.web.wicket.components.DatePanelSettings;
import org.projectforge.web.wicket.components.MaxLengthTextArea;
import org.projectforge.web.wicket.components.RequiredMaxLengthTextField;
import org.projectforge.web.wicket.components.RequiredMinMaxNumberField;
import org.projectforge.web.wicket.flowlayout.FieldsetPanel;

/**
 * This is the edit formular page.
 * @author Kai Reinhard (k.reinhard@micromata.de)
 * 
 */
public class LiquidityEntryEditForm extends AbstractEditForm<LiquidityEntryDO, LiquidityEntryEditPage>
{
  private static final long serialVersionUID = -6208809585214296635L;

  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(LiquidityEntryEditForm.class);

  public LiquidityEntryEditForm(final LiquidityEntryEditPage parentPage, final LiquidityEntryDO data)
  {
    super(parentPage, data);
  }

  @Override
  protected void init()
  {
    super.init();
    {
      // Date of payment
      final FieldsetPanel fs = gridBuilder.newFieldset(getString("plugins.liquidityplanning.entry.dateOfPayment"));
      final DatePanel dateOfPayment = new DatePanel(fs.newChildId(), new PropertyModel<Date>(data, "dateOfPayment"), DatePanelSettings
          .get().withTargetType(java.sql.Date.class));
      fs.add(dateOfPayment);
      dateOfPayment.add(WicketUtils.setFocus());
    }
    {
      // Amount
      final FieldsetPanel fs = gridBuilder.newFieldset(getString("fibu.common.betrag"));
      fs.add(new RequiredMinMaxNumberField<BigDecimal>(fs.getTextFieldId(), new PropertyModel<BigDecimal>(data, "amount"), Constants.TEN_BILLION_NEGATIVE, Constants.TEN_BILLION));
    }
    {
      // Subject
      final FieldsetPanel fs = gridBuilder.newFieldset(getString("fibu.rechnung.betreff"));
      final RequiredMaxLengthTextField subject = new RequiredMaxLengthTextField(fs.getTextFieldId(), new PropertyModel<String>(data,
          "subject"));
      fs.add(subject);
    }
    {
      // Text description
      final FieldsetPanel fs = gridBuilder.newFieldset(getString("plugins.LiquidityEntry.LiquidityEntry"));
      fs.add(new MaxLengthTextArea(fs.getTextAreaId(), new PropertyModel<String>(data, "LiquidityEntry"))).setAutogrow();
    }
  }

  @Override
  protected Logger getLogger()
  {
    return log;
  }
}
