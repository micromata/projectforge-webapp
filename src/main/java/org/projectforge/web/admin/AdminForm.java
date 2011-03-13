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

import java.util.Date;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.projectforge.web.wicket.AbstractForm;
import org.projectforge.web.wicket.WebConstants;
import org.projectforge.web.wicket.WicketUtils;
import org.projectforge.web.wicket.components.DatePanel;
import org.projectforge.web.wicket.components.MaxLengthTextArea;
import org.projectforge.web.wicket.components.SingleButtonPanel;
import org.projectforge.web.wicket.layout.LayoutContext;

public class AdminForm extends AbstractForm<AdminForm, AdminPage>
{
  private static final long serialVersionUID = -2450673501083584299L;

  protected Integer reindexNewestNEntries = 1000;

  protected Date reindexFromDate;

  protected String logEntries;

  protected String formattedLogEntries;

  protected String alertMessage;

  protected DatePanel reindexFromDatePanel;

  private AdminFormRenderer renderer;

  public AdminForm(final AdminPage parentPage)
  {
    super(parentPage);
    renderer = new AdminFormRenderer(this, new LayoutContext(false), parentPage);
  }

  @SuppressWarnings("serial")
  protected void init()
  {
    add(new FeedbackPanel("feedback").setOutputMarkupId(true));
    renderer.add();

    add(new Label("formattedLogEntries", new Model<String>() {
      @Override
      public String getObject()
      {
        return formattedLogEntries;
      }
    }).setEscapeModelStrings(false));

    final MaxLengthTextArea logEntriesTextArea = new MaxLengthTextArea("logEntries", getString("system.admin.group.title.misc.logEntries"),
        new PropertyModel<String>(this, "logEntries"), 10000);
    WicketUtils.addTooltip(logEntriesTextArea, getString("system.admin.button.formatLogEntries.textarea.tooltip"));
    add(logEntriesTextArea);
    final Button formatButton = new Button("button", new Model<String>(getString("system.admin.button.formatLogEntries"))) {
      @Override
      public final void onSubmit()
      {
        parentPage.formatLogEntries();
      }
    };
    WicketUtils.addTooltip(formatButton, getString("system.admin.button.formatLogEntries.tooltip"));
    formatButton.add(WebConstants.BUTTON_CLASS);

    final SingleButtonPanel formatButtonPanel = new SingleButtonPanel("formatLogEntries", formatButton);
    add(formatButtonPanel);
  }
}
