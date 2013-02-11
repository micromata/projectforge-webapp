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

package org.projectforge.web;

import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.projectforge.web.wicket.AbstractStandardForm;
import org.projectforge.web.wicket.bootstrap.GridSize;
import org.projectforge.web.wicket.flowlayout.DivPanel;
import org.projectforge.web.wicket.flowlayout.DivTextPanel;
import org.projectforge.web.wicket.flowlayout.FieldsetPanel;
import org.projectforge.web.wicket.flowlayout.RadioGroupPanel;

public class LayoutSettingsForm extends AbstractStandardForm<LayoutSettingsData, LayoutSettingsPage>
{
  private static final long serialVersionUID = -2138017238114715368L;

  protected LayoutSettingsData data;

  public LayoutSettingsForm(final LayoutSettingsPage parentPage)
  {
    super(parentPage);
    data = new LayoutSettingsData();
  }

  @SuppressWarnings("serial")
  @Override
  protected void init()
  {
    super.init();
    data.setBrowserScreenWidthType(getMySession().getBrowserScreenWidthType());
    final FieldsetPanel fs = gridBuilder.newFieldset(getString("layout.settings.browserScreenWidth")).setLabelSide(false).supressLabelForWarning();
    final DivPanel radioGroupPanel = fs.addNewRadioBoxDiv();
    final RadioGroupPanel<BrowserScreenWidthType> radioGroup = new RadioGroupPanel<BrowserScreenWidthType>(radioGroupPanel.newChildId(),
        "screenWidthType", new PropertyModel<BrowserScreenWidthType>(data, "browserScreenWidthType")) {
      /**
       * @see org.projectforge.web.wicket.flowlayout.RadioGroupPanel#wantOnSelectionChangedNotifications()
       */
      @Override
      protected boolean wantOnSelectionChangedNotifications()
      {
        return true;
      }

      /**
       * @see org.projectforge.web.wicket.flowlayout.RadioGroupPanel#onSelectionChanged(java.lang.Object)
       */
      @Override
      protected void onSelectionChanged(final Object newSelection)
      {
        parentPage.putUserPrefEntry(LayoutSettingsPage.getBrowserScreenWidthUserPrefKey(getMySession()), data.getBrowserScreenWidthType(),
            true);
        getMySession().setBrowserScreenWidthType(data.getBrowserScreenWidthType());
        setResponsePage(LayoutSettingsPage.class);
      }
    };
    radioGroupPanel.add(radioGroup);
    addRadioBox(radioGroup, BrowserScreenWidthType.NARROW);
    addRadioBox(radioGroup, BrowserScreenWidthType.NORMAL);
    addRadioBox(radioGroup, BrowserScreenWidthType.WIDE);
    gridBuilder.newSplitPanel(GridSize.COL50, true).newSubSplitPanel(GridSize.COL50);
    addCell("1.1");
    gridBuilder.newSubSplitPanel(GridSize.COL50);
    addCell("1.2");
    gridBuilder.newSplitPanel(GridSize.COL50, true).newSubSplitPanel(GridSize.COL50);
    addCell("2.1");
    gridBuilder.newSubSplitPanel(GridSize.COL50);
    addCell("2.2");
    gridBuilder.newSplitPanel(GridSize.COL50, true).newSubSplitPanel(GridSize.COL50);
    addCell("3.1");
    gridBuilder.newSubSplitPanel(GridSize.COL50);
    addCell("3.2");
    gridBuilder.newSplitPanel(GridSize.COL50, true).newSubSplitPanel(GridSize.COL50);
    addCell("4.1");
    gridBuilder.newSubSplitPanel(GridSize.COL50);
    addCell("4.2");
  }

  private void addCell(final String number)
  {
    gridBuilder.newFormHeading(getString("layout.settings.test") + " " + number);
    addContent();
  }

  private void addContent()
  {
    final DivPanel current = gridBuilder.getPanel();
    current
    .add(new DivTextPanel(
        current.newChildId(),
        "Lorem ipsum dolor sit amet, consectetuer adipiscing elit. Aenean commodo ligula eget dolor. Aenean massa. Cum sociis natoque penatibus et magnis dis parturient montes, nascetur ridiculus mus. Donec quam felis, ultricies nec, pellentesque eu, pretium quis, sem. Nulla consequat massa quis enim."));
    final FieldsetPanel fs = gridBuilder.newFieldset(getString("layout.settings.test"));
    fs.add(new TextField<String>(fs.getTextFieldId(), new Model<String>(getString("layout.settings.test"))));
  }

  private void addRadioBox(final RadioGroupPanel<BrowserScreenWidthType> radioGroup, final BrowserScreenWidthType type)
  {
    radioGroup.add(new Model<BrowserScreenWidthType>(type), getString(type.getI18nKey()));
  }
}
