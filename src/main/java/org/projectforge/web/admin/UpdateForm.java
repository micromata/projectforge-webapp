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
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.upload.FileUploadField;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.util.lang.Bytes;
import org.projectforge.Version;
import org.projectforge.admin.UpdatePreCheckStatus;
import org.projectforge.admin.UpdateScript;
import org.projectforge.web.wicket.AbstractForm;
import org.projectforge.web.wicket.components.SingleButtonPanel;

public class UpdateForm extends AbstractForm<UpdateForm, UpdatePage>
{
  private static final long serialVersionUID = 2492737003121592489L;

  protected FileUploadField fileUploadField;

  protected WebMarkupContainer scripts;

  public boolean showOldUpdateScripts;

  public UpdateForm(final UpdatePage parentPage)
  {
    super(parentPage);

    // set this form to multipart mode (allways needed for uploads!)
    setMultiPart(true);

    // Add one file input field
    add(fileUploadField = new FileUploadField("fileInput"));

    // Set maximum size to 100K for demo purposes
    setMaxSize(Bytes.kilobytes(100));
  }

  @SuppressWarnings("serial")
  protected void init()
  {
    add(new Label("currentVersion", Version.NUMBER));
    scripts = new WebMarkupContainer("scripts");
    add(scripts);
    updateScriptRows();
    add(new CheckBox("showOldVersionUpdatesCheckBox", new PropertyModel<Boolean>(this, "showOldUpdateScripts")));
    add(new FeedbackPanel("feedback").setOutputMarkupId(true));
    final Button uploadButton = new Button("button", new Model<String>(getString("upload"))) {
      @Override
      public final void onSubmit()
      {
        parentPage.upload();
      }
    };
    add(new SingleButtonPanel("upload", uploadButton));
    final Button refresh = new Button("button", new Model<String>("refresh")) {
      @Override
      public final void onSubmit()
      {
        parentPage.updateScriptStatus();
      }
    };
    add(new SingleButtonPanel("refresh", refresh));
    setDefaultButton(refresh);
    final Button checkForUpdate = new Button("button", new Model<String>("check for updates")) {
      @Override
      public final void onSubmit()
      {
        parentPage.checkForUpdates();
      }
    };
    add(new SingleButtonPanel("checkForUpdates", checkForUpdate));
  }

  @SuppressWarnings("serial")
  protected void updateScriptRows()
  {
    scripts.removeAll();
    final RepeatingView scriptRows = new RepeatingView("scriptRows");
    scripts.add(scriptRows);
    final List<UpdateScript> updateScripts = parentPage.updateScripts;
    if (updateScripts == null) {
      return;
    }
    for (final UpdateScript updateScript : updateScripts) {
      final String version = updateScript.getVersion();
      final WebMarkupContainer item = new WebMarkupContainer(scriptRows.newChildId());
      scriptRows.add(item);
      item.add(new Label("version", StringUtils.isBlank(version) == true ? "???" : version));
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
