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

import java.io.Serializable;
import java.util.Date;

import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.projectforge.AppVersion;
import org.projectforge.common.DateHelper;
import org.projectforge.core.Configuration;
import org.projectforge.user.PFUserContext;
import org.projectforge.web.WebConfiguration;
import org.projectforge.web.wicket.WebConstants;
import org.projectforge.web.wicket.WicketApplication;
import org.projectforge.web.wicket.WicketUtils;
import org.projectforge.web.wicket.components.DatePanel;
import org.projectforge.web.wicket.components.DatePanelSettings;
import org.projectforge.web.wicket.components.MaxLengthTextArea;
import org.projectforge.web.wicket.components.MinMaxNumberField;
import org.projectforge.web.wicket.components.SingleButtonPanel;
import org.projectforge.web.wicket.layout.AbstractFormRenderer;
import org.projectforge.web.wicket.layout.LayoutContext;
import org.projectforge.web.wicket.layout.LayoutLength;
import org.projectforge.web.wicket.layout.PanelContext;
import org.projectforge.web.wicket.layout.TextAreaLPanel;
import org.projectforge.web.wicket.layout.TextFieldLPanel;

/**
 * @author Kai Reinhard (k.reinhard@micromata.de)
 * 
 */
public class AdminFormRenderer extends AbstractFormRenderer
{
  private static final long serialVersionUID = 4515982116827709004L;

  final static LayoutLength LABEL_LENGTH = LayoutLength.HALF;

  final static LayoutLength VALUE_LENGTH = LayoutLength.ONEHALF;

  private final AdminPage adminPage;

  private final AdminForm adminForm;

  public AdminFormRenderer(final AdminForm container, final LayoutContext layoutContext, final AdminPage adminPage)
  {
    super(container, layoutContext);
    this.adminForm = container;
    this.adminPage = adminPage;
  }

  @SuppressWarnings("serial")
  @Override
  public void add()
  {
    doPanel.newFieldSetPanel(getString("system.admin.group.title.systemChecksAndFunctionality"));
    final Configuration cfg = Configuration.getInstance();

    doPanel.addLabel(getString("system.admin.group.title.systemChecksAndFunctionality.miscChecks"), new PanelContext(LABEL_LENGTH));
    RepeatingView repeatingView = doPanel.addRepeater(new PanelContext(VALUE_LENGTH)).getRepeatingView();
    repeatingView.add(new MyButtonPanel(repeatingView.newChildId(), "checkSystemIntegrity") {
      @Override
      public void onSubmit()
      {
        adminPage.checkSystemIntegrity();
      }
    }.getButtonPanel());
    repeatingView.add(new MyButtonPanel(repeatingView.newChildId(), "checkI18nProperties") {
      @Override
      public void onSubmit()
      {
        adminPage.checkI18nProperties();
      }
    }.getButtonPanel());

    doPanel.addLabel(getString("system.admin.group.title.systemChecksAndFunctionality.caches"), new PanelContext(LABEL_LENGTH));
    repeatingView = doPanel.addRepeater(new PanelContext(VALUE_LENGTH)).getRepeatingView();
    repeatingView.add(new MyButtonPanel(repeatingView.newChildId(), "refreshCaches") {
      @Override
      public void onSubmit()
      {
        adminPage.refreshCaches();
      }
    }.getButtonPanel());

    doPanel.addLabel(getString("system.admin.group.title.systemChecksAndFunctionality.configuration"), new PanelContext(LABEL_LENGTH));
    repeatingView = doPanel.addRepeater(new PanelContext(VALUE_LENGTH)).getRepeatingView();
    repeatingView.add(new MyButtonPanel(repeatingView.newChildId(), "rereadConfiguration") {
      @Override
      public void onSubmit()
      {
        adminPage.rereadConfiguration();
      }
    }.getButtonPanel());
    repeatingView.add(new MyButtonPanel(repeatingView.newChildId(), "exportConfiguration") {
      @Override
      public void onSubmit()
      {
        adminPage.exportConfiguration();
      }
    }.getButtonPanel());

    if (cfg.isMebConfigured() == true) {
      doPanel.addLabel(getString("meb.title.heading"), new PanelContext(LABEL_LENGTH));
      repeatingView = doPanel.addRepeater(new PanelContext(VALUE_LENGTH)).getRepeatingView();
      repeatingView.add(new MyButtonPanel(repeatingView.newChildId(), "checkUnseenMebMails") {
        @Override
        public void onSubmit()
        {
          adminPage.checkUnseenMebMails();
        }
      }.getButtonPanel());
      repeatingView.add(new MyButtonPanel(repeatingView.newChildId(), "importAllMebMails") {
        @Override
        public void onSubmit()
        {
          adminPage.importAllMebMails();
        }
      }.getButtonPanel());
    }

    doPanel.newGroupPanel(getString("system.admin.group.title.databaseActions"));
    doPanel.addLabel(getString("system.admin.group.title.databaseActions.userprefs"), new PanelContext(LABEL_LENGTH));
    repeatingView = doPanel.addRepeater(new PanelContext(VALUE_LENGTH)).getRepeatingView();
    repeatingView.add(new MyButtonPanel(repeatingView.newChildId(), "updateUserPrefs") {
      @Override
      public void onSubmit()
      {
        adminPage.updateUserPrefs();
      }
    }.getButtonPanel());

    doPanel.addLabel(getString("system.admin.group.title.databaseActions.dataBaseIndices"), new PanelContext(LABEL_LENGTH));
    repeatingView = doPanel.addRepeater(new PanelContext(VALUE_LENGTH)).getRepeatingView();
    repeatingView.add(new MyButtonPanel(repeatingView.newChildId(), "createMissingDatabaseIndices") {
      @Override
      public void onSubmit()
      {
        adminPage.createMissingDatabaseIndices();
      }
    }.getButtonPanel());
    repeatingView.add(new MyButtonPanel(repeatingView.newChildId(), "fixDBHistoryEntries") {
      @Override
      public void onSubmit()
      {
        adminPage.fixDBHistoryEntries();
      }
    }.getButtonPanel());

    doPanel.addLabel(getString("system.admin.group.title.databaseActions.export"), new PanelContext(LABEL_LENGTH));
    repeatingView = doPanel.addRepeater(new PanelContext(VALUE_LENGTH)).getRepeatingView();
    final MyButtonPanel buttonPanel = new MyButtonPanel(repeatingView.newChildId(), "dump") {
      @Override
      public void onSubmit()
      {
        adminPage.dump();
      }
    };
    repeatingView.add(buttonPanel.getButtonPanel());
    buttonPanel.button.add(WicketUtils.javaScriptConfirmDialogOnClick(getString("system.admin.button.dump.question")));
    repeatingView.add(new MyButtonPanel(repeatingView.newChildId(), "schemaExport") {
      @Override
      public void onSubmit()
      {
        adminPage.schemaExport();
      }
    }.getButtonPanel());

    doPanel.newGroupPanel(getString("system.admin.group.title.databaseSearchIndices"));
    doPanel.addTextField(new MinMaxNumberField<Integer>(TextFieldLPanel.INPUT_ID, new PropertyModel<Integer>(adminForm,
    "reindexNewestNEntries"), 0, Integer.MAX_VALUE), new PanelContext(LayoutLength.QUART,
        getString("system.admin.reindex.newestEntries"), LABEL_LENGTH).setTooltip(getString("system.admin.reindex.newestEntries.tooltip")));
    doPanel.addLabel(getString("system.admin.reindex.fromDate"), new PanelContext(LABEL_LENGTH));
    repeatingView = doPanel.addRepeater(new PanelContext(VALUE_LENGTH)).getRepeatingView();
    adminForm.reindexFromDatePanel = new DatePanel(repeatingView.newChildId(), new PropertyModel<Date>(adminForm, "reindexFromDate"),
        DatePanelSettings.get().withCallerPage(adminPage).withSelectProperty("reindexFromDate"));
    repeatingView.add(adminForm.reindexFromDatePanel);
    WicketUtils.addTooltip(adminForm.reindexFromDatePanel.getDateField(), new Model<String>() {
      @Override
      public String getObject()
      {
        return getString("system.admin.reindex.fromDate.tooltip")
        + (adminForm.reindexFromDate != null ? " (" + DateHelper.formatAsUTC(adminForm.reindexFromDate) + ")" : "");
      }
    });
    repeatingView.add(new MyButtonPanel(repeatingView.newChildId(), "reindex") {
      @Override
      public void onSubmit()
      {
        adminPage.reindex();
      }
    }.getButtonPanel());

    doPanel.newGroupPanel(getString("system.admin.group.title.alertMessage"));
    adminForm.alertMessage = WicketApplication.getAlertMessage();
    doPanel.addTextArea(new MaxLengthTextArea(TextAreaLPanel.TEXT_AREA_ID, getString("system.admin.group.title.alertMessage"),
        new PropertyModel<String>(adminForm, "alertMessage"), 1000), new PanelContext(LayoutLength.DOUBLE).setBreakBefore().setTooltip(
            getString("system.admin.group.title.alertMessage")));
    // doPanel.addLabel("", new PanelContext(LABEL_LENGTH));
    repeatingView = doPanel.addRepeater(new PanelContext(VALUE_LENGTH).setBreakBefore()).getRepeatingView();
    repeatingView.add(new MyButtonPanel(repeatingView.newChildId(), "setAlertMessage") {
      @Override
      public void onSubmit()
      {
        adminPage.setAlertMessage();
      }
    }.getButtonPanel());
    doPanel.addHelpLabel(getString("system.admin.alertMessage.copyAndPaste.title"), new PanelContext(LayoutLength.DOUBLE));
    doPanel.addLabel(PFUserContext.getLocalizedMessage("system.admin.alertMessage.copyAndPaste.text", AppVersion.NUMBER), new PanelContext(
        LayoutLength.DOUBLE));

    if (WebConfiguration.isDevelopmentMode() == true) {
      doPanel.newFieldSetPanel("Development modus");

      doPanel.addLabel("Create test objects", new PanelContext(LABEL_LENGTH));
      repeatingView = doPanel.addRepeater(new PanelContext(VALUE_LENGTH)).getRepeatingView();
      final Button button = new Button("button", new Model<String>("BookDO")) {
        @Override
        public final void onSubmit()
        {
          adminPage.createTestBooks();
        }
      };
      button.add(WebConstants.BUTTON_CLASS);
      button.add(WicketUtils.javaScriptConfirmDialogOnClick(adminPage.getLocalizedMessage(
          "system.admin.development.testObjectsCreationQuestion", AdminPage.NUMBER_OF_TEST_OBJECTS_TO_CREATE, "BookDO")));
      repeatingView.add(new SingleButtonPanel(repeatingView.newChildId(), button));
    }
  }

  private abstract class MyButtonPanel implements Serializable
  {
    private static final long serialVersionUID = -7100891342667728950L;

    private final Button button;

    private final SingleButtonPanel buttonPanel;

    @SuppressWarnings("serial")
    private MyButtonPanel(final String id, final String i18nKey)
    {
      button = new Button("button", new Model<String>(getString("system.admin.button." + i18nKey))) {
        @Override
        public final void onSubmit()
        {
          MyButtonPanel.this.onSubmit();
        }
      };
      button.add(WebConstants.BUTTON_CLASS);
      buttonPanel = new SingleButtonPanel(id, button);
      WicketUtils.addTooltip(button, getString("system.admin.button." + i18nKey + ".tooltip"));
    }

    public SingleButtonPanel getButtonPanel()
    {
      return buttonPanel;
    }

    public abstract void onSubmit();
  }
}
