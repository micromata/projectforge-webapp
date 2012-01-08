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

package org.projectforge.web.admin;

import java.util.SortedSet;

import org.apache.commons.lang.StringUtils;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.projectforge.Version;
import org.projectforge.admin.UpdateEntry;
import org.projectforge.admin.UpdatePreCheckStatus;
import org.projectforge.web.HtmlHelper;
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
    updateEntryRows();
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
  protected void updateEntryRows()
  {
    scripts.removeAll();
    final RepeatingView scriptRows = new RepeatingView("scriptRows");
    scripts.add(scriptRows);
    final SortedSet<UpdateEntry> updateEntries = parentPage.systemUpdater.getUpdateEntries();
    if (updateEntries == null) {
      return;
    }
    for (final UpdateEntry updateEntry : updateEntries) {
      if (showOldUpdateScripts == false && updateEntry.getPreCheckStatus() == UpdatePreCheckStatus.ALREADY_UPDATED) {
        continue;
      }
      final Version version = updateEntry.getVersion();
      final WebMarkupContainer item = new WebMarkupContainer(scriptRows.newChildId());
      scriptRows.add(item);
      item.add(new Label("regionId", updateEntry.getRegionId()));
      item.add(new Label("version", version.toString()));
      final String description = updateEntry.getDescription();
      item.add(new Label("description", StringUtils.isBlank(description) == true ? "" : description));
      item.add(new Label("date", updateEntry.getDate()));
      item.add(new Label("preCheckResult", new Model<String>() {
        @Override
        public String getObject()
        {
          final String preCheckResult = updateEntry.getPreCheckResult();
          return HtmlHelper.escapeHtml(preCheckResult, true);
        }
      }));
      if (updateEntry.getPreCheckStatus() == UpdatePreCheckStatus.OK) {
        final Button updateButton = new Button("button", new Model<String>("update")) {
          @Override
          public final void onSubmit()
          {
            parentPage.update(updateEntry);
          }
        };
        item.add(new SingleButtonPanel("update", updateButton));
      } else {
        item.add(new Label("update", new Model<String>() {
          @Override
          public String getObject()
          {
            final String runningResult = updateEntry.getRunningResult();
            return HtmlHelper.escapeHtml(runningResult, true);
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
