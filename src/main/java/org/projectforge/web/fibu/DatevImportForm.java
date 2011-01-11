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

import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.RadioGroup;
import org.apache.wicket.markup.html.form.upload.FileUploadField;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.util.lang.Bytes;
import org.projectforge.web.wicket.AbstractForm;
import org.projectforge.web.wicket.components.RadioButtonLabelPanel;
import org.projectforge.web.wicket.components.SingleButtonPanel;

public class DatevImportForm extends AbstractForm<DatevImportFilter, DatevImportPage>
{
  private static final long serialVersionUID = -4812284533159635654L;

  protected DatevImportFilter filter = new DatevImportFilter();

  protected FileUploadField fileUploadField;

  public DatevImportForm(final DatevImportPage parentPage)
  {
    super(parentPage);
    // set this form to multipart mode (always needed for uploads!)
    setMultiPart(true);
    // Add one file input field
    add(fileUploadField = new FileUploadField("fileInput"));
    setMaxSize(Bytes.megabytes(1));
  }

  @SuppressWarnings("serial")
  @Override
  protected void init()
  {
    super.init();

    final RadioGroup<String> filterType = new RadioGroup<String>("filterType", new PropertyModel<String>(filter, "listType"));
    add(filterType);
    final RepeatingView radioButtonRepeater = new RepeatingView("repeater");
    radioButtonRepeater.add(new RadioButtonLabelPanel<String>(radioButtonRepeater.newChildId(), new Model<String>("all"),
        getString("filter.all")).setSubmitOnChange());
    radioButtonRepeater.add(new RadioButtonLabelPanel<String>(radioButtonRepeater.newChildId(), new Model<String>("modified"),
        getString("modified")).setSubmitOnChange());
    radioButtonRepeater.add(new RadioButtonLabelPanel<String>(radioButtonRepeater.newChildId(), new Model<String>("faulty"),
        getString("filter.faulty")).setSubmitOnChange());
    filterType.add(radioButtonRepeater);

    final Button uploadButton = new Button("button", new Model<String>(getString("upload"))) {
      @Override
      public final void onSubmit()
      {
        parentPage.upload();
      }
    };
    add(new SingleButtonPanel("upload", uploadButton));

    // final UserSelectPanel userSelectPanel = new UserSelectPanel("selectUser", new PropertyModel<PFUserDO>(filter, "user"), parentPage,
    // "user");
    // userSelectPanel.setRequired(true);
    // add(userSelectPanel);
    // userSelectPanel.init();
    // {
    // // DropDownChoice months
    // final LabelValueChoiceRenderer<Integer> monthChoiceRenderer = new LabelValueChoiceRenderer<Integer>();
    // for (int i = 0; i <= 11; i++) {
    // monthChoiceRenderer.addValue(i, StringHelper.format2DigitNumber(i + 1));
    // }
    // monthChoice = new DropDownChoice<Integer>("month", new PropertyModel<Integer>(filter, "month"), monthChoiceRenderer.getValues(),
    // monthChoiceRenderer);
    // monthChoice.setNullValid(false).setRequired(true);
    // add(monthChoice);
    // }
    // yearChoice = new DropDownChoice<Integer>("year", new PropertyModel<Integer>(filter, "year"), new ArrayList<Integer>());
    // yearChoice.setNullValid(false).setRequired(true);
    // add(yearChoice);
    //
    // final RepeatingView actionButtonsView = new RepeatingView("actionButtons");
    // add(actionButtonsView.setRenderBodyOnly(true));
    // final Button resetButton = new Button("button", new Model<String>(getString("reset"))) {
    // @Override
    // public final void onSubmit()
    // {
    // filter.reset();
    // yearChoice.modelChanged();
    // monthChoice.modelChanged();
    // }
    // };
    // resetButton.add(WebConstants.BUTTON_CLASS_RESET);
    // resetButton.setDefaultFormProcessing(false);
    // final SingleButtonPanel resetButtonPanel = new SingleButtonPanel(actionButtonsView.newChildId(), resetButton);
    // actionButtonsView.add(resetButtonPanel);
    // final Button exportAsPdfButton = new Button("button", new Model<String>(getString("exportAsPdf"))) {
    // @Override
    // public final void onSubmit()
    // {
    // parentPage.exportAsPdf();
    // }
    // };
    // actionButtonsView.add(new SingleButtonPanel(actionButtonsView.newChildId(), exportAsPdfButton));
    // final Button showButton = new Button("button", new Model<String>(getString("show")));
    // showButton.add(WebConstants.BUTTON_CLASS_DEFAULT);
    // setDefaultButton(showButton);
    // actionButtonsView.add(new SingleButtonPanel(actionButtonsView.newChildId(), showButton));
  }

}
