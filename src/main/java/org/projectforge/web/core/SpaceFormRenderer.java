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

package org.projectforge.web.core;

import org.apache.wicket.MarkupContainer;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.model.PropertyModel;
import org.projectforge.core.SpaceDO;
import org.projectforge.core.SpaceStatus;
import org.projectforge.web.wicket.components.LabelValueChoiceRenderer;
import org.projectforge.web.wicket.layout.AbstractFormRenderer;
import org.projectforge.web.wicket.layout.DropDownChoiceLPanel;
import org.projectforge.web.wicket.layout.LayoutContext;
import org.projectforge.web.wicket.layout.LayoutLength;
import org.projectforge.web.wicket.layout.PanelContext;

public class SpaceFormRenderer extends AbstractFormRenderer
{
  private static final long serialVersionUID = -33764866877505432L;

  private SpaceDO data;

  private final static LayoutLength LABEL_LENGTH = LayoutLength.HALF;

  private final static LayoutLength VALUE_LENGTH = LayoutLength.ONEHALF;

  @SuppressWarnings("unused")
  private String templateName;

  protected Boolean saveAsTemplate;

  public SpaceFormRenderer(final MarkupContainer container, final LayoutContext layoutContext, final SpaceDO data)
  {
    super(container, layoutContext);
    this.data = data;
  }

  @Override
  public void add()
  {
    doPanel.newFieldSetPanel(getString("plugins.todo.todo"));
    doPanel.addTextField(new PanelContext(data, "identifier", VALUE_LENGTH, getString("space.identifier"), LABEL_LENGTH).setRequired()
        .setStrong().setFocus());
    {
      final LabelValueChoiceRenderer<SpaceStatus> typeChoiceRenderer = new LabelValueChoiceRenderer<SpaceStatus>(container, SpaceStatus
          .values());
      final DropDownChoice<SpaceStatus> typeChoice = new DropDownChoice<SpaceStatus>(DropDownChoiceLPanel.SELECT_ID,
          new PropertyModel<SpaceStatus>(data, "status"), typeChoiceRenderer.getValues(), typeChoiceRenderer);
      typeChoice.setNullValid(true);
      doPanel.addDropDownChoice(typeChoice, new PanelContext(LayoutLength.THREEQUART, getString("status"), LABEL_LENGTH));
    }
    doPanel.addTextField(new PanelContext(data, "title", VALUE_LENGTH, getString("space.title"), LABEL_LENGTH).setRequired());
    doPanel.addTextArea(new PanelContext(data, "description", VALUE_LENGTH, getString("description"), LABEL_LENGTH)
        .setCssStyle("height: 10em;"));
  }
}
