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

package org.projectforge.web.scripting;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.model.PropertyModel;
import org.projectforge.scripting.ScriptDO;
import org.projectforge.scripting.ScriptParameterType;
import org.projectforge.web.HtmlHelper;
import org.projectforge.web.wicket.AbstractEditForm;
import org.projectforge.web.wicket.components.LabelValueChoiceRenderer;
import org.projectforge.web.wicket.components.MaxLengthTextArea;
import org.projectforge.web.wicket.components.MaxLengthTextField;


public class ScriptEditForm extends AbstractEditForm<ScriptDO, ScriptEditPage>
{
  private static final long serialVersionUID = 9088102999434892079L;

  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(ScriptEditForm.class);

  public ScriptEditForm(ScriptEditPage parentPage, ScriptDO data)
  {
    super(parentPage, data);
    this.colspan = 6;
  }

  @Override
  protected void init()
  {
    add(new MaxLengthTextField("name", new PropertyModel<String>(data, "name")));
    add(new MaxLengthTextArea("description", new PropertyModel<String>(data, "description")));
    add(new MaxLengthTextArea("script", new PropertyModel<String>(data, "script")));
    addParameterSettings(1);
    addParameterSettings(2);
    addParameterSettings(3);
    addParameterSettings(4);
    addParameterSettings(5);
    super.init();
  }

  @Override
  protected void addBottomRows()
  {
    final Fragment bottomRowsFragment = new Fragment("bottomRows", "bottomRowsFragment", this);
    bottomRowsFragment.setRenderBodyOnly(true);
    add(bottomRowsFragment);
    String esc = HtmlHelper.escapeXml(data.getScriptBackup());
    esc = StringUtils.replace(esc, "\n", "<br/>\n");
    final Label scriptBackupLabel = new Label("scriptBackup", esc);
    scriptBackupLabel.setEscapeModelStrings(false);
    if (StringUtils.isEmpty(data.getScriptBackup()) == true) {
      scriptBackupLabel.setVisible(false);
    }
    bottomRowsFragment.add(scriptBackupLabel);
  }

  private void addParameterSettings(int idx)
  {
    final String parameterType = "parameter" + idx + "Type";
    final String parameterName = "parameter" + idx + "Name";
    add(new MaxLengthTextField(parameterName, new PropertyModel<String>(data, parameterName)));
    // DropDownChoice type
    final LabelValueChoiceRenderer<ScriptParameterType> typeChoiceRenderer = new LabelValueChoiceRenderer<ScriptParameterType>(this,
        ScriptParameterType.values());
    @SuppressWarnings("unchecked")
    final DropDownChoice typeChoice = new DropDownChoice(parameterType, new PropertyModel(data, parameterType), typeChoiceRenderer
        .getValues(), typeChoiceRenderer);
    typeChoice.setNullValid(true);
    typeChoice.setRequired(false);
    add(typeChoice);
  }

  @Override
  protected Logger getLogger()
  {
    return log;
  }
}
