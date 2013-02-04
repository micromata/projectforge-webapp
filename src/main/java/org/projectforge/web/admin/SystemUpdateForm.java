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

package org.projectforge.web.admin;

import java.util.SortedSet;

import org.apache.commons.lang.StringUtils;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.projectforge.Version;
import org.projectforge.admin.UpdateEntry;
import org.projectforge.admin.UpdatePreCheckStatus;
import org.projectforge.web.HtmlHelper;
import org.projectforge.web.wicket.AbstractForm;
import org.projectforge.web.wicket.bootstrap.GridBuilder;
import org.projectforge.web.wicket.components.SingleButtonPanel;
import org.projectforge.web.wicket.flowlayout.CheckBoxPanel;
import org.projectforge.web.wicket.flowlayout.FieldsetPanel;
import org.projectforge.web.wicket.flowlayout.MyComponentsRepeater;

public class SystemUpdateForm extends AbstractForm<SystemUpdateForm, SystemUpdatePage>
{
  private static final long serialVersionUID = 2492737003121592489L;

  protected WebMarkupContainer scripts;

  public boolean showOldUpdateScripts;

  private GridBuilder gridBuilder;

  /**
   * List to create content menu in the desired order before creating the RepeatingView.
   */
  protected MyComponentsRepeater<SingleButtonPanel> actionButtons;

  public SystemUpdateForm(final SystemUpdatePage parentPage)
  {
    super(parentPage);
  }

  @Override
  @SuppressWarnings("serial")
  protected void init()
  {
    add(createFeedbackPanel());
    gridBuilder = newGridBuilder(this, "flowform");
    gridBuilder.newGridPanel();
    {
      final FieldsetPanel fs = gridBuilder.newFieldset("Show all");
      fs.add(new CheckBoxPanel(fs.newChildId(), new PropertyModel<Boolean>(this, "showOldUpdateScripts"), null, true) {
        /**
         * @see org.projectforge.web.wicket.flowlayout.CheckBoxPanel#onSelectionChanged(java.lang.Boolean)
         */
        @Override
        protected void onSelectionChanged(final Boolean newSelection)
        {
          parentPage.refresh();
        }
      });
    }
    scripts = new WebMarkupContainer("scripts");
    add(scripts);
    updateEntryRows();

    actionButtons = new MyComponentsRepeater<SingleButtonPanel>("buttons");
    add(actionButtons.getRepeatingView());
    {
      final Button refreshButton = new Button(SingleButtonPanel.WICKET_ID, new Model<String>("refresh")) {
        @Override
        public final void onSubmit()
        {
          parentPage.refresh();
        }
      };
      final SingleButtonPanel refreshButtonPanel = new SingleButtonPanel(actionButtons.newChildId(), refreshButton, "refresh",
          SingleButtonPanel.DEFAULT_SUBMIT);
      actionButtons.add(refreshButtonPanel);
      setDefaultButton(refreshButton);
    }
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
    boolean odd = true;
    for (final UpdateEntry updateEntry : updateEntries) {
      if (showOldUpdateScripts == false && updateEntry.getPreCheckStatus() == UpdatePreCheckStatus.ALREADY_UPDATED) {
        continue;
      }
      final Version version = updateEntry.getVersion();
      final WebMarkupContainer item = new WebMarkupContainer(scriptRows.newChildId());
      scriptRows.add(item);
      if (odd == true) {
        item.add(AttributeModifier.append("class", "odd"));
      } else {
        item.add(AttributeModifier.append("class", "even"));
      }
      odd = !odd;
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
        item.add(new SingleButtonPanel("update", updateButton, "update"));
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

  /**
   * @see org.projectforge.web.wicket.AbstractForm#onBeforeRender()
   */
  @Override
  public void onBeforeRender()
  {
    super.onBeforeRender();
    actionButtons.render();
  }
}
