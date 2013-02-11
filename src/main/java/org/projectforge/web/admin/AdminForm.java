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

import java.io.Serializable;
import java.util.Date;

import org.apache.commons.lang.StringUtils;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.projectforge.AppVersion;
import org.projectforge.common.DateHelper;
import org.projectforge.core.Configuration;
import org.projectforge.user.PFUserContext;
import org.projectforge.web.WebConfiguration;
import org.projectforge.web.wicket.AbstractStandardForm;
import org.projectforge.web.wicket.WicketApplication;
import org.projectforge.web.wicket.WicketUtils;
import org.projectforge.web.wicket.bootstrap.GridSize;
import org.projectforge.web.wicket.components.DatePanel;
import org.projectforge.web.wicket.components.MaxLengthTextArea;
import org.projectforge.web.wicket.components.MinMaxNumberField;
import org.projectforge.web.wicket.components.SingleButtonPanel;
import org.projectforge.web.wicket.flowlayout.DivPanel;
import org.projectforge.web.wicket.flowlayout.DivTextPanel;
import org.projectforge.web.wicket.flowlayout.FieldsetPanel;
import org.projectforge.web.wicket.flowlayout.InputPanel;
import org.projectforge.web.wicket.flowlayout.TextAreaPanel;

public class AdminForm extends AbstractStandardForm<AdminForm, AdminPage>
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

  @Override
  @SuppressWarnings("serial")
  protected void init()
  {
    super.init();
    gridBuilder.newSplitPanel(GridSize.COL50);
    gridBuilder.newFormHeading(getString("system.admin.group.title.systemChecksAndFunctionality"));
    final Configuration cfg = Configuration.getInstance();
    {
      final FieldsetPanel fs = gridBuilder.newFieldset(getString("system.admin.group.title.systemChecksAndFunctionality.miscChecks"))
          .supressLabelForWarning();
      fs.add(new MyButtonPanel(fs.newChildId(), "checkSystemIntegrity") {
        @Override
        public void onSubmit()
        {
          parentPage.checkSystemIntegrity();
        }
      }.getButtonPanel());
      fs.add(new MyButtonPanel(fs.newChildId(), "checkI18nProperties") {
        @Override
        public void onSubmit()
        {
          parentPage.checkI18nProperties();
        }
      }.getButtonPanel());
    }
    {
      final FieldsetPanel fs = gridBuilder.newFieldset(getString("system.admin.group.title.systemChecksAndFunctionality.caches"))
          .supressLabelForWarning();
      fs.add(new MyButtonPanel(fs.newChildId(), "refreshCaches") {
        @Override
        public void onSubmit()
        {
          parentPage.refreshCaches();
        }
      }.getButtonPanel());
    }
    {
      final FieldsetPanel fs = gridBuilder.newFieldset(getString("system.admin.group.title.systemChecksAndFunctionality.configuration"))
          .supressLabelForWarning();
      fs.add(new MyButtonPanel(fs.newChildId(), "rereadConfiguration") {
        @Override
        public void onSubmit()
        {
          parentPage.rereadConfiguration();
        }
      }.getButtonPanel());
      fs.add(new MyButtonPanel(fs.newChildId(), "exportConfiguration") {
        @Override
        public void onSubmit()
        {
          parentPage.exportConfiguration();
        }
      }.getButtonPanel());
    }
    if (cfg.isMebConfigured() == true) {
      final FieldsetPanel fs = gridBuilder.newFieldset(getString("meb.title.heading.short"), getString("meb.title.heading"))
          .supressLabelForWarning();
      fs.add(new MyButtonPanel(fs.newChildId(), "checkUnseenMebMails") {
        @Override
        public void onSubmit()
        {
          parentPage.checkUnseenMebMails();
        }
      }.getButtonPanel());
      fs.add(new MyButtonPanel(fs.newChildId(), "importAllMebMails") {
        @Override
        public void onSubmit()
        {
          parentPage.importAllMebMails();
        }
      }.getButtonPanel());
    }

    gridBuilder.newFormHeading(getString("system.admin.group.title.alertMessage"));
    {
      final FieldsetPanel fs = gridBuilder.newFieldset(getString("system.admin.group.title.alertMessage"));
      alertMessage = WicketApplication.getAlertMessage();

      fs.add(new MaxLengthTextArea(TextAreaPanel.WICKET_ID, new PropertyModel<String>(this, "alertMessage"), 1000));
    }
    {
      final FieldsetPanel fs = gridBuilder.newFieldset("").supressLabelForWarning();
      fs.add(new MyButtonPanel(fs.newChildId(), "setAlertMessage") {
        @Override
        public void onSubmit()
        {
          parentPage.setAlertMessage();
        }
      }.getButtonPanel());
      fs.add(new MyButtonPanel(fs.newChildId(), "clearAlertMessage") {
        @Override
        public void onSubmit()
        {
          parentPage.clearAlertMessage();
        }

        /**
         * @see org.projectforge.web.admin.AdminForm.MyButtonPanel#isVisible()
         */
        @Override
        public boolean isVisible()
        {
          return StringUtils.isNotBlank(alertMessage);
        }
      }.getButtonPanel());
    }
    {
      final FieldsetPanel fs = gridBuilder.newFieldset(getString("system.admin.alertMessage.copyAndPaste.title")).supressLabelForWarning();
      fs.add(new DivTextPanel(fs.newChildId(), PFUserContext.getLocalizedMessage("system.admin.alertMessage.copyAndPaste.text",
          AppVersion.NUMBER)));
    }

    gridBuilder.newSplitPanel(GridSize.COL50);
    gridBuilder.newFormHeading(getString("system.admin.group.title.databaseActions"));
    {
      final FieldsetPanel fs = gridBuilder.newFieldset(getString("system.admin.group.title.databaseActions.userprefs"))
          .supressLabelForWarning();
      fs.add(new MyButtonPanel(fs.newChildId(), "updateUserPrefs") {
        @Override
        public void onSubmit()
        {
          parentPage.updateUserPrefs();
        }
      }.getButtonPanel());
    }
    {
      final FieldsetPanel fs = gridBuilder.newFieldset(getString("system.admin.group.title.databaseActions.dataBaseIndices"))
          .supressLabelForWarning();
      fs.add(new MyButtonPanel(fs.newChildId(), "createMissingDatabaseIndices") {
        @Override
        public void onSubmit()
        {
          parentPage.createMissingDatabaseIndices();
        }
      }.getButtonPanel());
      fs.add(new MyButtonPanel(fs.newChildId(), "fixDBHistoryEntries") {
        @Override
        public void onSubmit()
        {
          parentPage.fixDBHistoryEntries();
        }
      }.getButtonPanel());
    }
    {
      final FieldsetPanel fs = gridBuilder.newFieldset(getString("system.admin.group.title.databaseActions.export")).supressLabelForWarning();
      final MyButtonPanel buttonPanel = new MyButtonPanel(fs.newChildId(), "dump") {
        @Override
        public void onSubmit()
        {
          parentPage.dump();
        }
      };
      buttonPanel.button.add(WicketUtils.javaScriptConfirmDialogOnClick(getString("system.admin.button.dump.question")));
      fs.add(buttonPanel.getButtonPanel());
      fs.add(new MyButtonPanel(fs.newChildId(), "schemaExport") {
        @Override
        public void onSubmit()
        {
          parentPage.schemaExport();
        }
      }.getButtonPanel());
    }
    gridBuilder.newFormHeading(getString("system.admin.group.title.databaseSearchIndices"));
    {
      final FieldsetPanel fs = gridBuilder.newFieldset(getString("system.admin.reindex.newestEntries"),
          getString("system.admin.reindex.newestEntries.subtitle"));
      fs.add(new MinMaxNumberField<Integer>(InputPanel.WICKET_ID, new PropertyModel<Integer>(this, "reindexNewestNEntries"), 0,
          Integer.MAX_VALUE));
      fs.addHelpIcon(getString("system.admin.reindex.newestEntries.tooltip"));
    }
    {
      final FieldsetPanel fs = gridBuilder.newFieldset(getString("system.admin.reindex.fromDate"));
      final DatePanel datePanel = new DatePanel(fs.newChildId(), new PropertyModel<Date>(this, "reindexFromDate"));
      fs.add(datePanel);
      fs.addHelpIcon(new Model<String>() {
        @Override
        public String getObject()
        {
          return getString("system.admin.reindex.fromDate.tooltip")
              + (reindexFromDate != null ? " (" + DateHelper.formatAsUTC(reindexFromDate) + ")" : "");
        }
      });
      fs.add(new MyButtonPanel(fs.newChildId(), "reindex") {
        @Override
        public void onSubmit()
        {
          parentPage.reindex();
        }
      }.getButtonPanel());
    }
    if (WebConfiguration.isDevelopmentMode() == true) {
      gridBuilder.newFormHeading("Development modus");
      final FieldsetPanel fs = gridBuilder.newFieldset("Create test objects").supressLabelForWarning();
      final Button button = new Button(SingleButtonPanel.WICKET_ID, new Model<String>("BookDO")) {
        @Override
        public final void onSubmit()
        {
          parentPage.createTestBooks();
        }
      };
      button.add(WicketUtils.javaScriptConfirmDialogOnClick(parentPage.getLocalizedMessage(
          "system.admin.development.testObjectsCreationQuestion", AdminPage.NUMBER_OF_TEST_OBJECTS_TO_CREATE, "BookDO")));
      fs.add(new SingleButtonPanel(fs.newChildId(), button, "BookDO", SingleButtonPanel.GREY));
    }

    gridBuilder.newGridPanel();
    {
      final FieldsetPanel fs = gridBuilder.newFieldset(getString("system.admin.group.title.misc.logEntries"));
      final MaxLengthTextArea logEntries = new MaxLengthTextArea(TextAreaPanel.WICKET_ID, new PropertyModel<String>(this, "logEntries"),
          10000);
      logEntries.add(AttributeModifier.append("style", "width: 100%; height: 20em;"));
      fs.add(logEntries);
      fs.addHelpIcon(getString("system.admin.button.formatLogEntries.textarea.tooltip"));
    }
    {
      final FieldsetPanel fs = gridBuilder.newFieldset("").supressLabelForWarning();
      fs.add(new MyButtonPanel(fs.newChildId(), "formatLogEntries") {
        @Override
        public void onSubmit()
        {
          parentPage.formatLogEntries();
        }
      }.getButtonPanel());
    }
    gridBuilder.newGridPanel();
    final DivPanel section = gridBuilder.getPanel();
    final DivTextPanel logMessages = new DivTextPanel(section.newChildId(), new Model<String>() {
      @Override
      public String getObject()
      {
        return formattedLogEntries;
      }
    });
    logMessages.getLabel().setEscapeModelStrings(false);
    section.add(logMessages);
  }

  private abstract class MyButtonPanel implements Serializable
  {
    private static final long serialVersionUID = -7100891342667728950L;

    private final Button button;

    private final SingleButtonPanel buttonPanel;

    @SuppressWarnings("serial")
    private MyButtonPanel(final String id, final String i18nKey)
    {
      button = new Button(SingleButtonPanel.WICKET_ID, new Model<String>()) {
        @Override
        public final void onSubmit()
        {
          MyButtonPanel.this.onSubmit();
        }

        /**
         * @see org.apache.wicket.Component#isVisible()
         */
        @Override
        public boolean isVisible()
        {
          return MyButtonPanel.this.isVisible();
        }
      };
      buttonPanel = new SingleButtonPanel(id, button, getString("system.admin.button." + i18nKey), SingleButtonPanel.GREY);
      WicketUtils.addTooltip(button, getString("system.admin.button." + i18nKey + ".tooltip"));
    }

    public SingleButtonPanel getButtonPanel()
    {
      return buttonPanel;
    }

    public abstract void onSubmit();

    public boolean isVisible()
    {
      return true;
    }
  }
}
