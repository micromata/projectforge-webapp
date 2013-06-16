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

import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.util.convert.IConverter;
import org.projectforge.core.Constants;
import org.projectforge.web.wicket.AbstractStandardForm;
import org.projectforge.web.wicket.WicketUtils;
import org.projectforge.web.wicket.components.RequiredMinMaxNumberField;
import org.projectforge.web.wicket.converter.CurrencyConverter;
import org.projectforge.web.wicket.flowlayout.FieldsetPanel;

public class LiquidityAnalysisForm extends AbstractStandardForm<Object, LiquidityAnalysisPage>
{
  private static final long serialVersionUID = -4518924991100703065L;

  private static final String USER_PREF_KEY_SETTINGS = LiquidityAnalysisSettings.class.getName();

  private LiquidityAnalysisSettings settings;

  public LiquidityAnalysisForm(final LiquidityAnalysisPage parentPage)
  {
    super(parentPage);
  }

  @SuppressWarnings("serial")
  @Override
  protected void init()
  {
    super.init();
    final FieldsetPanel fs = gridBuilder.newFieldset(getString("plugins.liquidityplanning.analysis.startAmount"));
    final RequiredMinMaxNumberField<BigDecimal> amount = new RequiredMinMaxNumberField<BigDecimal>(fs.getTextFieldId(),
        new PropertyModel<BigDecimal>(getSettings(), "startAmount"), Constants.TEN_BILLION_NEGATIVE, Constants.TEN_BILLION) {
      @SuppressWarnings({ "rawtypes", "unchecked"})
      @Override
      public IConverter getConverter(final Class type)
      {
        return new CurrencyConverter();
      }
    };
    WicketUtils.setSize(amount, 8);
    fs.add(amount);
  }

  protected LiquidityAnalysisSettings getSettings()
  {
    if (settings == null) {
      settings = (LiquidityAnalysisSettings) parentPage.getUserPrefEntry(USER_PREF_KEY_SETTINGS);
    }
    if (settings == null) {
      settings = new LiquidityAnalysisSettings();
      parentPage.putUserPrefEntry(USER_PREF_KEY_SETTINGS, settings, true);
    }
    return settings;
  }
}
