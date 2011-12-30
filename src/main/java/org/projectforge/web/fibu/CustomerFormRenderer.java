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

import static org.projectforge.web.wicket.layout.DropDownChoiceLPanel.SELECT_ID;

import org.apache.wicket.MarkupContainer;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.util.convert.IConverter;
import org.projectforge.fibu.KundeDO;
import org.projectforge.fibu.KundeStatus;
import org.projectforge.web.wicket.components.LabelValueChoiceRenderer;
import org.projectforge.web.wicket.components.MinMaxNumberField;
import org.projectforge.web.wicket.converter.IntegerConverter;
import org.projectforge.web.wicket.layout.AbstractFormRenderer;
import org.projectforge.web.wicket.layout.LayoutContext;
import org.projectforge.web.wicket.layout.LayoutLength;
import org.projectforge.web.wicket.layout.PanelContext;
import org.projectforge.web.wicket.layout.TextFieldLPanel;

public class CustomerFormRenderer extends AbstractFormRenderer
{
  private static final long serialVersionUID = -9175062586210446142L;

  private final KundeDO data;

  private final static LayoutLength LABEL_LENGTH = LayoutLength.HALF;

  private final static LayoutLength VALUE_LENGTH = LayoutLength.ONEHALF;

  public CustomerFormRenderer(final MarkupContainer container, final LayoutContext layoutContext,
      final KundeDO data)
  {
    super(container, layoutContext);
    this.data = data;
  }

  @SuppressWarnings("serial")
  @Override
  public void add()
  {
    // doPanel.newGroupPanel(getString("system.admin.group.title.databaseSearchIndices"));
    doPanel.addTextField(
        new MinMaxNumberField<Integer>(TextFieldLPanel.INPUT_ID, new PropertyModel<Integer>(data, "id"), 0, KundeDO.MAX_ID) {
          @Override
          public IConverter getConverter(final Class< ? > type)
          {
            return new IntegerConverter(3);
          }
        }, new PanelContext(LayoutLength.QUART, getString("fibu.kunde.nummer"), LABEL_LENGTH)
        .setStrong().setFocus(isNew() == true).setEnabled(isNew() == true));
    doPanel.addTextField(new PanelContext(data, "name", VALUE_LENGTH, getString("fibu.kunde.name"), LABEL_LENGTH).setRequired()
        .setStrong().setFocus(isNew() == false));
    doPanel.addTextField(new PanelContext(data, "identifier", VALUE_LENGTH, getString("fibu.kunde.identifier"), LABEL_LENGTH));
    doPanel.addTextField(new PanelContext(data, "division", VALUE_LENGTH, getString("fibu.kunde.division"), LABEL_LENGTH));
    doPanel.addTextArea(new PanelContext(data, "description", VALUE_LENGTH, getString("description"), LABEL_LENGTH)
    .setCssStyle("height: 20ex;"));
    {
      // Status drop down box:
      final LabelValueChoiceRenderer<KundeStatus> statusChoiceRenderer = new LabelValueChoiceRenderer<KundeStatus>(container, KundeStatus
          .values());
      final DropDownChoice<KundeStatus> statusChoice = new DropDownChoice<KundeStatus>(SELECT_ID, new PropertyModel<KundeStatus>(data,
      "status"), statusChoiceRenderer.getValues(), statusChoiceRenderer);
      statusChoice.setNullValid(false).setRequired(true);
      doPanel.addDropDownChoice(statusChoice, new PanelContext(LayoutLength.THREEQUART, getString("status"), LABEL_LENGTH));
    }
  }
}
