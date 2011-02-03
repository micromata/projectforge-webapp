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

package org.projectforge.web.admin;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.wicket.behavior.SimpleAttributeModifier;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.projectforge.admin.UpdatePreCheckStatus;
import org.projectforge.admin.UpdateScript;
import org.projectforge.web.wicket.AbstractForm;
import org.projectforge.web.wicket.components.SingleButtonPanel;

public class SystemUpdateForm extends AbstractForm<SystemUpdateForm, SystemUpdatePage>
{
  private static final long serialVersionUID = 2492737003121592489L;

  protected WebMarkupContainer scripts;

  public boolean showOldUpdateScripts;

  public SystemUpdateForm(final SystemUpdatePage parentPage)
  {
    super(parentPage);
  }

  @SuppressWarnings("serial")
  protected void init()
  {
    scripts = new WebMarkupContainer("scripts");
    add(scripts);
    updateScriptRows();
    add(new CheckBox("showOldVersionUpdatesCheckBox", new PropertyModel<Boolean>(this, "showOldUpdateScripts")));
    add(new FeedbackPanel("feedback").setOutputMarkupId(true));
    final Button refresh = new Button("button", new Model<String>("refresh")) {
      @Override
      public final void onSubmit()
      {
        parentPage.refresh();
      }
    };
    add(new SingleButtonPanel("refresh", refresh));
    setDefaultButton(refresh);
  }

  @SuppressWarnings("serial")
  protected void updateScriptRows()
  {
    scripts.removeAll();
    final RepeatingView scriptRows = new RepeatingView("scriptRows");
    scripts.add(scriptRows);
    final List<UpdateScript> updateScripts = parentPage.systemUpdater.getUpdateScripts();
    if (updateScripts == null) {
      return;
    }
    for (final UpdateScript updateScript : updateScripts) {
      if (showOldUpdateScripts == false && updateScript.getPreCheckStatus() == UpdatePreCheckStatus.ALREADY_UPDATED) {
        continue;
      }
      final String version = updateScript.getVersion();
      final WebMarkupContainer item = new WebMarkupContainer(scriptRows.newChildId());
      scriptRows.add(item);
      item.add(new Label("version", StringUtils.isBlank(version) == true ? "???" : version));
      final Link<String> downloadScriptLink = new Link<String>("downloadScript") {
        @Override
        public void onClick()
        {
          parentPage.downloadUpdateScript(updateScript);
        }
      };
      downloadScriptLink.add(new SimpleAttributeModifier("title", "You can use this script for own modifications and manual updates."));
      item.add(downloadScriptLink);
      downloadScriptLink.add(new Label("fileName", "update-script-" + version).setRenderBodyOnly(true));
      item.add(new Label("preCheckResult", new Model<String>() {
        @Override
        public String getObject()
        {
          return updateScript.getPreCheckResult() != null ? updateScript.getPreCheckResult().getResultAsHtmlString() : "";
        }
      }));
      if (updateScript.getPreCheckStatus() == UpdatePreCheckStatus.OK) {
        final Button updateButton = new Button("button", new Model<String>("update")) {
          @Override
          public final void onSubmit()
          {
            parentPage.update(updateScript);
          }
        };
        item.add(new SingleButtonPanel("update", updateButton));
      } else {
        item.add(new Label("update", new Model<String>() {
          @Override
          public String getObject()
          {
            return updateScript.getRunningResult() != null ? updateScript.getRunningResult().getResultAsHtmlString() : "";
          }
        }));
      }
    }
  }

  public boolean isShowOldUpdateScripts()
  {
    return showOldUpdateScripts;
  }

  public void setShowOldUpdateScripts(boolean showOldUpdateScripts)
  {
    this.showOldUpdateScripts = showOldUpdateScripts;
  }
}
