/////////////////////////////////////////////////////////////////////////////
//
// Project ProjectForge Community Edition
//         www.projectforge.org
//
// Copyright (C) 2001-2012 Kai Reinhard (k.reinhard@micromata.de)
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

package org.projectforge.web.scripting;

import org.apache.log4j.Logger;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.projectforge.scripting.ScriptDO;
import org.projectforge.scripting.ScriptParameterType;
import org.projectforge.web.HtmlHelper;
import org.projectforge.web.wicket.AbstractEditForm;
import org.projectforge.web.wicket.WicketUtils;
import org.projectforge.web.wicket.bootstrap.GridSize;
import org.projectforge.web.wicket.components.LabelValueChoiceRenderer;
import org.projectforge.web.wicket.components.MaxLengthTextArea;
import org.projectforge.web.wicket.components.MaxLengthTextField;
import org.projectforge.web.wicket.components.SingleButtonPanel;
import org.projectforge.web.wicket.flowlayout.DialogPanel;
import org.projectforge.web.wicket.flowlayout.DivPanel;
import org.projectforge.web.wicket.flowlayout.DivTextPanel;
import org.projectforge.web.wicket.flowlayout.FieldsetPanel;

public class ScriptEditForm extends AbstractEditForm<ScriptDO, ScriptEditPage>
{
  private static final long serialVersionUID = 9088102999434892079L;

  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(ScriptEditForm.class);

  private static final String SHOW_BACKUP_SCRIPT_DIALOG_ID = "showBackupScriptModalWindow";

  private ModalWindow showBackupScriptModalWindow;

  public ScriptEditForm(final ScriptEditPage parentPage, final ScriptDO data)
  {
    super(parentPage, data);
  }

  @Override
  protected void init()
  {
    super.init();
    gridBuilder.newGridPanel();
    {
      final FieldsetPanel fs = gridBuilder.newFieldset(getString("scripting.script.name"));
      fs.add(new MaxLengthTextField(fs.getTextFieldId(), new PropertyModel<String>(data, "name")));
    }
    gridBuilder.newSplitPanel(GridSize.COL50);
    addParameterSettings(1);
    gridBuilder.newSplitPanel(GridSize.COL50);
    addParameterSettings(2);
    gridBuilder.newSplitPanel(GridSize.COL50);
    addParameterSettings(3);
    gridBuilder.newSplitPanel(GridSize.COL50);
    addParameterSettings(4);
    gridBuilder.newSplitPanel(GridSize.COL50);
    addParameterSettings(5);
    gridBuilder.newSplitPanel(GridSize.COL50);
    addParameterSettings(6);
    gridBuilder.newGridPanel();
    {
      final FieldsetPanel fs = gridBuilder.newFieldset(getString("description"));
      fs.add(new MaxLengthTextArea(fs.getTextAreaId(), new PropertyModel<String>(data, "description")));
    }
    {
      final FieldsetPanel fs = gridBuilder.newFieldset(getString("scripting.script"), true);
      final MaxLengthTextArea script = new MaxLengthTextArea(fs.getTextAreaId(), new PropertyModel<String>(data, "script"));
      WicketUtils.setHeight(script, 50);
      fs.add(script);
      fs.addHelpIcon(getString("fieldNotHistorizable"));
    }
    showBackupScriptModalWindow = new ModalWindow(SHOW_BACKUP_SCRIPT_DIALOG_ID);
    add(showBackupScriptModalWindow);
  }

  private void addParameterSettings(final int idx)
  {
    final FieldsetPanel fs = gridBuilder.newFieldset(getString("scripting.script.parameterName") + " " + idx, true);

    final String parameterType = "parameter" + idx + "Type";
    final String parameterName = "parameter" + idx + "Name";
    final MaxLengthTextField name = new MaxLengthTextField(fs.getTextFieldId(), new PropertyModel<String>(data, parameterName));
    WicketUtils.setSize(name, 20);
    fs.add(name);
    // DropDownChoice type
    final LabelValueChoiceRenderer<ScriptParameterType> typeChoiceRenderer = new LabelValueChoiceRenderer<ScriptParameterType>(this,
        ScriptParameterType.values());
    final DropDownChoice<ScriptParameterType> typeChoice = new DropDownChoice<ScriptParameterType>(fs.getDropDownChoiceId(),
        new PropertyModel<ScriptParameterType>(data, parameterType), typeChoiceRenderer.getValues(), typeChoiceRenderer);
    typeChoice.setNullValid(true);
    typeChoice.setRequired(false);
    fs.add(typeChoice);
  }

  protected void showBackupScriptModalWindow(final AjaxRequestTarget target)
  {
    // Close dialog
    final DialogPanel showBackuScriptDialog = new DialogPanel(showBackupScriptModalWindow, getString("scripting.scriptBackup"));
    showBackupScriptModalWindow.setContent(showBackuScriptDialog);

    final DivPanel content = new DivPanel(showBackuScriptDialog.newChildId());
    showBackuScriptDialog.add(content);
    final FieldsetPanel fs = new FieldsetPanel(content, getString("scripting.scriptBackup"));
    final String esc = HtmlHelper.escapeHtml(data.getScriptBackup(), true);
    final DivTextPanel scriptBackup = new DivTextPanel(fs.newChildId(), esc);
    scriptBackup.getLabel().setEscapeModelStrings(false);
    fs.add(scriptBackup);

    @SuppressWarnings("serial")
    final AjaxButton okButton = new AjaxButton(SingleButtonPanel.WICKET_ID, new Model<String>("ok")) {

      @Override
      protected void onSubmit(final AjaxRequestTarget target, final Form< ? > form)
      {
        showBackupScriptModalWindow.close(target);
      }

      /**
       * @see org.apache.wicket.ajax.markup.html.form.AjaxButton#onError(org.apache.wicket.ajax.AjaxRequestTarget,
       *      org.apache.wicket.markup.html.form.Form)
       */
      @Override
      protected void onError(final AjaxRequestTarget target, final Form< ? > form)
      {
      }
    };
    okButton.setDefaultFormProcessing(false); // No validation
    final SingleButtonPanel okButtonPanel = new SingleButtonPanel(showBackuScriptDialog.newButtonChildId(), okButton, getString("ok"),
        SingleButtonPanel.DEFAULT_SUBMIT);
    showBackuScriptDialog.addButton(okButtonPanel);
    showBackupScriptModalWindow.setCloseButtonCallback(new ModalWindow.CloseButtonCallback() {
      private static final long serialVersionUID = 6761625465164911336L;

      public boolean onCloseButtonClicked(final AjaxRequestTarget target)
      {
        return true;
      }
    });
    showBackupScriptModalWindow.show(target);
  }

  @Override
  protected Logger getLogger()
  {
    return log;
  }
}
