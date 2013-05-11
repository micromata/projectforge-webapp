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

package org.projectforge.web.wicket.components;

import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;

/**
 * Panel for source code editor ACE, see http://ace.ajax.org
 * 
 * @author Johannes Unterstein (j.unterstein@micromata.de)
 */
public class AceEditorPanel extends Panel
{

  private WebMarkupContainer editor;
  private TextArea textArea;

  public AceEditorPanel(String id, IModel<String> model)
  {
    super(id, model);
    editor = new WebMarkupContainer("editor");
    editor.setOutputMarkupId(true);
    textArea = new TextArea("textArea", model);
    textArea.setOutputMarkupId(true);
    add(textArea);
    add(editor);
  }

  @Override
  public void renderHead(IHeaderResponse response)
  {
    super.renderHead(response);
    // init ace editor
    String script = "$(function() { initAceEditor('" + editor.getMarkupId() + "', '" + textArea.getMarkupId() + "'); });";
    response.render(JavaScriptHeaderItem.forScript(script, null));
  }
}
