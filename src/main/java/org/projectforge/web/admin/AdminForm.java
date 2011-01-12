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

import java.io.Serializable;
import java.util.Date;

import org.apache.wicket.behavior.SimpleAttributeModifier;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.projectforge.Version;
import org.projectforge.core.Configuration;
import org.projectforge.user.PFUserContext;
import org.projectforge.web.wicket.AbstractForm;
import org.projectforge.web.wicket.WicketApplication;
import org.projectforge.web.wicket.WicketUtils;
import org.projectforge.web.wicket.components.DatePanel;
import org.projectforge.web.wicket.components.DatePanelSettings;
import org.projectforge.web.wicket.components.MaxLengthTextField;
import org.projectforge.web.wicket.components.MinMaxNumberField;
import org.projectforge.web.wicket.components.SingleButtonPanel;

public class AdminForm extends AbstractForm<AdminForm, AdminPage>
{
  private static final long serialVersionUID = -2450673501083584299L;

  protected Integer reindexNewestNEntries = 1000;

  protected Date reindexFromDate;

  protected String logEntries;

  protected String formattedLogEntries;

  protected String alertMessage;

  protected DatePanel reindexFromDatePanel;

  public AdminForm(final AdminPage parentPage)
  {
    super(parentPage);

  }

  @SuppressWarnings("serial")
  protected void init()
  {
    final Configuration cfg = Configuration.getInstance();
    new MyButtonPanel("checkSystemIntegrity") {
      @Override
      public void onSubmit()
      {
        parentPage.checkSystemIntegrity();
      }
    };
    new MyButtonPanel("refreshCaches") {
      @Override
      public void onSubmit()
      {
        parentPage.refreshCaches();
      }
    };
    new MyButtonPanel("checkI18nProperties") {
      @Override
      public void onSubmit()
      {
        parentPage.checkI18nProperties();
      }
    };
    new MyButtonPanel("checkUnseenMebMails", cfg.isMebConfigured()) {
      @Override
      public void onSubmit()
      {
        parentPage.checkUnseenMebMails();
      }
    };
    new MyButtonPanel("importAllMebMails", cfg.isMebConfigured()) {
      @Override
      public void onSubmit()
      {
        parentPage.importAllMebMails();
      }
    };
    new MyButtonPanel("rereadConfiguration") {
      @Override
      public void onSubmit()
      {
        parentPage.rereadConfiguration();
      }
    };
    new MyButtonPanel("exportConfiguration") {
      @Override
      public void onSubmit()
      {
        parentPage.exportConfiguration();
      }
    };
    new MyButtonPanel("updateUserPrefs", false) { // Currently not visible
      @Override
      public void onSubmit()
      {
        parentPage.updateUserPrefs();
      }
    };
    new MyButtonPanel("createMissingDatabaseIndices") {
      @Override
      public void onSubmit()
      {
        parentPage.createMissingDatabaseIndices();
      }
    };
    final MyButtonPanel dumpButtonPanel = new MyButtonPanel("dump", WicketApplication.isDevelopmentModus()) {
      @Override
      public void onSubmit()
      {
        parentPage.dump();
      }
    };
    dumpButtonPanel.button.add(WicketUtils.javaScriptConfirmDialogOnClick("Do you really want to dump the whole data-base?"));
    new MyButtonPanel("schemaExport") {
      @Override
      public void onSubmit()
      {
        parentPage.schemaExport();
      }
    };
    new MyButtonPanel("fixDBHistoryEntries", "system.admin.button.fixDBHistoryEntries.tooltip") {
      @Override
      public void onSubmit()
      {
        parentPage.fixDBHistoryEntries();
      }
    };
    add(new MinMaxNumberField<Integer>("reindexNewestNEntries", new PropertyModel<Integer>(this, "reindexNewestNEntries"), 0,
        Integer.MAX_VALUE));
    reindexFromDatePanel = new DatePanel("reindexFromDate", new PropertyModel<Date>(this, "reindexFromDate"), DatePanelSettings.get()
        .withCallerPage(parentPage));
    add(reindexFromDatePanel);
    new MyButtonPanel("reindex") {
      @Override
      public void onSubmit()
      {
        parentPage.reindex();
      }
    };
    add(new MaxLengthTextField("logEntries", new PropertyModel<String>(this, "logEntries"), 10000));
    add(new Label("formattedLogEntries", new Model<String>() {
      @Override
      public String getObject()
      {
        return formattedLogEntries;
      }
    }).setEscapeModelStrings(false));
    new MyButtonPanel("formatLogEntries") {
      @Override
      public void onSubmit()
      {
        parentPage.formatLogEntries();
      }
    };
    alertMessage = WicketApplication.getAlertMessage();
    add(new MaxLengthTextField("alertMessage", new PropertyModel<String>(this, "alertMessage"), 1000));
    new MyButtonPanel("setAlertMessage") {
      @Override
      public void onSubmit()
      {
        parentPage.setAlertMessage();
      }
    };
    add(new Label("copyAndPasteText1", PFUserContext.getLocalizedMessage("system.admin.alertMessage.copyAndPaste.text1", Version.NUMBER)));
    add(new Label("copyAndPasteText3", PFUserContext.getLocalizedMessage("system.admin.alertMessage.copyAndPaste.text3", Version.NUMBER)));
    final WebMarkupContainer forDevelopers = new WebMarkupContainer("forDevelopers");
    add(forDevelopers);
    if (WicketApplication.isDevelopmentModus() == true) {
      final Button button = new Button("button", new Model<String>("create test books")) {
        @Override
        public final void onSubmit()
        {
          parentPage.createTestBooks();
        }
      };
      button.add(WicketUtils.javaScriptConfirmDialogOnClick(getLocalizedMessage("system.admin.development.testObjectsCreationQuestion",
          AdminPage.NUMBER_OF_TEST_OBJECTS_TO_CREATE, "BookDO")));
      final SingleButtonPanel buttonPanel = new SingleButtonPanel("createTestBooks", button);
      if (Configuration.getInstance().isBookManagementConfigured() == false) {
        buttonPanel.setVisible(false);
      }
      forDevelopers.add(buttonPanel);
    } else {
      forDevelopers.setVisible(false);
    }
  }

  private abstract class MyButtonPanel implements Serializable
  {
    private static final long serialVersionUID = -7100891342667728950L;

    private Button button;

    private MyButtonPanel(final String i18nKey)
    {
      this(i18nKey, null, true);
    }

    private MyButtonPanel(final String i18nKey, final String tooltip)
    {
      this(i18nKey, tooltip, true);
    }

    private MyButtonPanel(final String i18nKey, final boolean visible)
    {
      this(i18nKey, null, visible);
    }

    @SuppressWarnings("serial")
    private MyButtonPanel(final String i18nKey, final String tooltip, final boolean visible)
    {
      button = new Button("button", new Model<String>(getString("system.admin.button." + i18nKey))) {
        @Override
        public final void onSubmit()
        {
          MyButtonPanel.this.onSubmit();
        }
      };
      SingleButtonPanel buttonPanel = new SingleButtonPanel(i18nKey, button);
      add(buttonPanel);
      if (visible == false) {
        buttonPanel.setVisible(false);
      }
      if (tooltip != null) {
        button.add(new SimpleAttributeModifier("title", getString(tooltip)));
      }
    }

    public abstract void onSubmit();
  }
}
