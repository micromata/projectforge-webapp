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

package org.projectforge.web.fibu;

import java.util.Map;
import java.util.Set;

import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.upload.FileUploadField;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.util.lang.Bytes;
import org.projectforge.common.ImportStorage;
import org.projectforge.fibu.kost.BusinessAssessment;
import org.projectforge.web.wicket.AbstractStandardForm;
import org.projectforge.web.wicket.components.SingleButtonPanel;
import org.projectforge.web.wicket.flowlayout.DivPanel;
import org.projectforge.web.wicket.flowlayout.FieldsetPanel;
import org.projectforge.web.wicket.flowlayout.FileUploadPanel;
import org.projectforge.web.wicket.flowlayout.RadioGroupPanel;

public class DatevImportForm extends AbstractStandardForm<DatevImportFilter, DatevImportPage>
{
  private static final long serialVersionUID = -4812284533159635654L;

  protected DatevImportFilter filter = new DatevImportFilter();

  protected FileUploadField fileUploadField;

  protected DatevImportStoragePanel storagePanel;

  public DatevImportForm(final DatevImportPage parentPage)
  {
    super(parentPage);
    initUpload(Bytes.megabytes(10));
  }

  @SuppressWarnings("serial")
  @Override
  protected void init()
  {
    super.init();
    gridBuilder.newGridPanel();
    {
      final FieldsetPanel fs = gridBuilder.newFieldset(getString("file"), "*.xls");
      fileUploadField = new FileUploadField(FileUploadPanel.WICKET_ID);
      fs.add(new FileUploadPanel(fs.newChildId(), fileUploadField));
      fs.add(new SingleButtonPanel(fs.newChildId(), new Button(SingleButtonPanel.WICKET_ID, new Model<String>("importAccounts")) {
        @Override
        public final void onSubmit()
        {
          parentPage.importAccountList();
        }
      }, getString("finance.datev.importAccountList"), SingleButtonPanel.GREY));
      fs.add(new SingleButtonPanel(fs.newChildId(), new Button(SingleButtonPanel.WICKET_ID, new Model<String>("importRecords")) {
        @Override
        public final void onSubmit()
        {
          parentPage.importAccountRecords();
        }
      }, getString("finance.datev.importAccountingRecords"), SingleButtonPanel.GREY));
      fs.add(new SingleButtonPanel(fs.newChildId(), new Button(SingleButtonPanel.WICKET_ID, new Model<String>("clearStorage")) {
        @Override
        public final void onSubmit()
        {
          parentPage.clear();
        }
      }, getString("finance.datev.clearStorage"), SingleButtonPanel.RESET) {
        /**
         * @see org.apache.wicket.Component#isVisible()
         */
        @Override
        public boolean isVisible()
        {
          return storagePanel.isVisible();
        }
      });
    }
    {
      final FieldsetPanel fs = gridBuilder.newFieldset(getString("filter"));
      final DivPanel radioGroupPanel = fs.addNewRadioBoxDiv();
      final RadioGroupPanel<String> radioGroup = new RadioGroupPanel<String>(radioGroupPanel.newChildId(), "filterType",
          new PropertyModel<String>(filter, "listType")) {
        /**
         * @see org.projectforge.web.wicket.flowlayout.RadioGroupPanel#wantOnSelectionChangedNotifications()
         */
        @Override
        protected boolean wantOnSelectionChangedNotifications()
        {
          return true;
        }
      };
      radioGroupPanel.add(radioGroup);
      fs.setLabelFor(radioGroup.getRadioGroup());
      radioGroup.add(new Model<String>("all"), getString("filter.all"));
      radioGroup.add(new Model<String>("modified"), getString("modified"));
      radioGroup.add(new Model<String>("faulty"), getString("filter.faulty"));
    }
    {
      // Statistics
      new BusinessAssessment4Fieldset(gridBuilder) {
        /**
         * @see org.projectforge.web.fibu.BusinessAssessment4Fieldset#getBusinessAssessment()
         */
        @Override
        protected BusinessAssessment getBusinessAssessment()
        {
          return storagePanel.businessAssessment;
        }

        @Override
        public boolean isVisible()
        {
          return storagePanel.businessAssessment != null;
        };
      };
    }
    gridBuilder.newGridPanel();
    final DivPanel panel = gridBuilder.getPanel();
    storagePanel = new DatevImportStoragePanel(panel.newChildId(), parentPage, filter);
    panel.add(storagePanel);
  }

  @Override
  public void onBeforeRender()
  {
    refresh();
    super.onBeforeRender();
  }

  protected ImportStorage< ? > getStorage()
  {
    return storagePanel.storage;
  }

  protected void setStorage(final ImportStorage< ? > storage)
  {
    storagePanel.storage = storage;
  }

  protected void setErrorProperties(final Map<String, Set<Object>> errorProperties)
  {
    storagePanel.errorProperties = errorProperties;
  }

  protected void setBusinessAssessment(final BusinessAssessment businessAssessment)
  {
    storagePanel.businessAssessment = businessAssessment;
  }

  protected void refresh()
  {
    storagePanel.storage = getStorage();
    if (storagePanel.storage == null) {
      storagePanel.storage = (ImportStorage< ? >) parentPage.getUserPrefEntry(DatevImportPage.KEY_IMPORT_STORAGE);
    }
    if (storagePanel.storage == null) {
      storagePanel.setVisible(false);
      return;
    }
    storagePanel.setVisible(true);
    storagePanel.refresh();
  }
}
