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

import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.wicket.behavior.SimpleAttributeModifier;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.apache.wicket.util.convert.IConverter;
import org.projectforge.common.StringHelper;
import org.projectforge.fibu.KundeDO;
import org.projectforge.fibu.ProjektDO;
import org.projectforge.fibu.ProjektStatus;
import org.projectforge.fibu.kost.KostCache;
import org.projectforge.reporting.Kost2Art;
import org.projectforge.task.TaskDO;
import org.projectforge.user.GroupDO;
import org.projectforge.web.task.TaskSelectPanel;
import org.projectforge.web.user.GroupSelectPanel;
import org.projectforge.web.wicket.AbstractEditForm;
import org.projectforge.web.wicket.FocusOnLoadBehavior;
import org.projectforge.web.wicket.WebConstants;
import org.projectforge.web.wicket.components.LabelValueChoiceRenderer;
import org.projectforge.web.wicket.components.MaxLengthTextArea;
import org.projectforge.web.wicket.components.MaxLengthTextField;
import org.projectforge.web.wicket.components.MinMaxNumberField;
import org.projectforge.web.wicket.components.TooltipImage;
import org.projectforge.web.wicket.converter.IntegerConverter;


public class ProjektEditForm extends AbstractEditForm<ProjektDO, ProjektEditPage>
{
  private static final long serialVersionUID = -6018131069720611834L;

  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(ProjektEditForm.class);

  @SpringBean(name = "kostCache")
  private KostCache kostCache;

  List<Kost2Art> kost2Arts;

  public ProjektEditForm(ProjektEditPage parentPage, ProjektDO data)
  {
    super(parentPage, data);
    this.colspan = 6;
  }

  @SuppressWarnings("serial")
  @Override
  protected void init()
  {
    super.init();
    add(new MinMaxNumberField<Integer>("nummer", new PropertyModel<Integer>(data, "nummer"), 0, 99) {
      @Override
      public IConverter getConverter(Class< ? > type)
      {
        return new IntegerConverter(2);
      }
    });
    final CustomerSelectPanel kundeSelectPanel = new CustomerSelectPanel("kunde", new PropertyModel<KundeDO>(data, "kunde"), null, parentPage,
        "kundeId");
    add(kundeSelectPanel);
    kundeSelectPanel.init();
    add(new MinMaxNumberField<Integer>("internKost2_4", new PropertyModel<Integer>(data, "internKost2_4"), 0, 999) {
      @Override
      public IConverter getConverter(Class< ? > type)
      {
        return new IntegerConverter(3);
      }
    });
    add(new MaxLengthTextField("name", new PropertyModel<String>(data, "name")).add(new FocusOnLoadBehavior()));
    add(new MaxLengthTextField("identifier", new PropertyModel<String>(data, "identifier")));
    final GroupSelectPanel groupSelectPanel = new GroupSelectPanel("selectProjektManagerGroup", new PropertyModel<GroupDO>(data,
        "projektManagerGroup"), parentPage, "projektManagerGroupId");
    add(groupSelectPanel);
    groupSelectPanel.init();
    add(new MaxLengthTextArea("description", new PropertyModel<String>(data, "description")));
    final TaskSelectPanel taskSelectPanel = new TaskSelectPanel("task", new PropertyModel<TaskDO>(data, "task"), parentPage, "taskId");
    add(taskSelectPanel);
    taskSelectPanel.init();
    // DropDownChoice status
    final LabelValueChoiceRenderer<ProjektStatus> statusChoiceRenderer = new LabelValueChoiceRenderer<ProjektStatus>(this, ProjektStatus
        .values());
    @SuppressWarnings("unchecked")
    final DropDownChoice statusChoice = new DropDownChoice("status", new PropertyModel(data, "status"), statusChoiceRenderer.getValues(),
        statusChoiceRenderer);
    statusChoice.setNullValid(false).setRequired(true);
    add(statusChoice);
    if (isNew() == true) {
      kost2Arts = kostCache.getAllKostArts();
    } else {
      kost2Arts = kostCache.getAllKost2Arts(getData().getId());
    }
    final Iterator<Kost2Art> it = kost2Arts.iterator();
    final RepeatingView kost2artRowsRepeater = new RepeatingView("kost2artRows");
    add(kost2artRowsRepeater);
    while (it.hasNext() == true) {
      final WebMarkupContainer rowItem = new WebMarkupContainer(kost2artRowsRepeater.newChildId());
      kost2artRowsRepeater.add(rowItem);
      final RepeatingView kost2artColsRepeater = new RepeatingView("kost2artCols");
      rowItem.add(kost2artColsRepeater);
      for (int i = 0; i < 2 && it.hasNext() == true; i++) {
        final WebMarkupContainer colItem = new WebMarkupContainer(kost2artColsRepeater.newChildId());
        kost2artColsRepeater.add(colItem);
        final Kost2Art kost2Art = it.next();
        String style = null;
        if (kost2Art.isExistsAlready() == true) {
          if (kost2Art.isProjektStandard() == true) {
            style = "color: green;";
          }
        } else {
          if (kost2Art.isProjektStandard() == true) {
            style = "color: red;";
          }
        }
        final CheckBox checkBox = new CheckBox("kost2artSelect", new PropertyModel<Boolean>(kost2Art, "selected"));
        colItem.add(checkBox);
        final TooltipImage image = new TooltipImage("acceptImage", getResponse(), WebConstants.IMAGE_ACCEPT,
            getString("fibu.projekt.edit.kost2DoesAlreadyExists"));
        colItem.add(image);
        if (kost2Art.isExistsAlready() == true) {
          checkBox.setVisible(false);
        } else {
          image.setVisibilityAllowed(false);
        }
        final Label kost2artNummerLabel = new Label("kost2artNummer", StringHelper.format2DigitNumber(kost2Art.getId()));
        colItem.add(kost2artNummerLabel);
        final Label kost2artNameLabel = new Label("kost2artName", kost2Art.isFakturiert() == true ? kost2Art.getName() : kost2Art.getName()
            + " (nf)");
        colItem.add(kost2artNameLabel);
        if (style != null) {
          kost2artNummerLabel.add(new SimpleAttributeModifier("style", style));
          kost2artNameLabel.add(new SimpleAttributeModifier("style", style));
        }
      }
    }
  }

  @Override
  protected Logger getLogger()
  {
    return log;
  }
}
