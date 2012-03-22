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

package org.projectforge.web.scripting;

import java.util.List;

import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.upload.FileUploadField;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.util.lang.Bytes;
import org.projectforge.fibu.kost.reporting.Report;
import org.projectforge.fibu.kost.reporting.ReportStorage;
import org.projectforge.scripting.GroovyResult;
import org.projectforge.scripting.ScriptDO;
import org.projectforge.web.fibu.ReportScriptingStorage;
import org.projectforge.web.wicket.AbstractStandardForm;
import org.projectforge.web.wicket.WicketUtils;
import org.projectforge.web.wicket.components.MaxLengthTextArea;
import org.projectforge.web.wicket.components.SingleButtonPanel;
import org.projectforge.web.wicket.flowlayout.DivPanel;
import org.projectforge.web.wicket.flowlayout.DivTextPanel;
import org.projectforge.web.wicket.flowlayout.FieldsetPanel;
import org.projectforge.web.wicket.flowlayout.FileUploadPanel;
import org.projectforge.web.wicket.flowlayout.Heading1Panel;
import org.springframework.util.CollectionUtils;

public class ScriptingForm extends AbstractStandardForm<ScriptDO, ScriptingPage>
{
  /**
   * Maximum length of Groovy script for form validation of text area (100.000 characters should be really enough).
   */
  public static final int MAX_GROOVY_LENGTH = 100000;

  private static final long serialVersionUID = 1868796548657011785L;

  protected FileUploadField fileUploadField;

  private String groovyResult;

  private String reportPathHeading;

  private DivPanel reportPathPanel;

  public ScriptingForm(final ScriptingPage parentPage)
  {
    super(parentPage);
    initUpload(Bytes.megabytes(1));
  }

  @Override
  @SuppressWarnings("serial")
  protected void init()
  {
    super.init();
    gridBuilder.newGrid16();
    reportPathPanel = gridBuilder.newSectionPanel();
    reportPathPanel.add(new Heading1Panel(reportPathPanel.newChildId(), new Model<String>() {
      /**
       * @see org.apache.wicket.model.Model#getObject()
       */
      @Override
      public String getObject()
      {
        return reportPathHeading;
      }
    }));
    {
      // Upload dump file
      final FieldsetPanel fs = gridBuilder.newFieldset(getString("file") + " (*.xsl, *.jrxml)", true);
      fileUploadField = new FileUploadField(FileUploadPanel.WICKET_ID);
      fs.add(new DivTextPanel(fs.newChildId(), new Model<String>() {
        @Override
        public String getObject()
        {
          final ReportScriptingStorage storage = getReportScriptingStorage();
          return storage != null ? storage.getLastAddedFilename() : "";
        }
      }));
      fs.add(new FileUploadPanel(fs.newChildId(), fileUploadField));
    }
    {
      final Button uploadButton = new Button(SingleButtonPanel.WICKET_ID, new Model<String>("upload")) {
        @Override
        public final void onSubmit()
        {
          parentPage.upload();
        }
      };
      final SingleButtonPanel uploadButtonPanel = new SingleButtonPanel(actionButtons.newChildId(), uploadButton, getString("upload"),
          SingleButtonPanel.DEFAULT_SUBMIT);
      actionButtons.add(uploadButtonPanel);
    }
    {
      final FieldsetPanel fs = gridBuilder.newFieldset(getString("label.groovyScript"));
      final MaxLengthTextArea textArea = new MaxLengthTextArea(fs.getTextAreaId(), new PropertyModel<String>(this, "groovyScript"),
          MAX_GROOVY_LENGTH);
      WicketUtils.setFocus(textArea);
      fs.add(textArea);
    }
    {
      final FieldsetPanel fs = gridBuilder.newFieldset(getString("label.groovy.result"));
      final DivTextPanel groovyResultPanel = new DivTextPanel(fs.newChildId(), new Model<String>() {
        /**
         * @see org.apache.wicket.model.Model#getObject()
         */
        @Override
        public String getObject()
        {
          return groovyResult;
        }
      });
      groovyResultPanel.getLabel().setEscapeModelStrings(false);
    }
    {
      final Button executeButton = new Button(SingleButtonPanel.WICKET_ID, new Model<String>("execute")) {
        @Override
        public final void onSubmit()
        {
          parentPage.execute();
        }
      };
      final SingleButtonPanel executeButtonPanel = new SingleButtonPanel(actionButtons.newChildId(), executeButton, getString("execute"));
      actionButtons.add(2, executeButtonPanel);
    }
  }

  @SuppressWarnings("serial")
  @Override
  public void onBeforeRender()
  {
    final GroovyResult groovyResult = parentPage.groovyResult;
    if (groovyResult != null && groovyResult.hasResult() == true) {
      //      groovyResultRow.setVisible(true);
      //      groovyResultLabel.setDefaultModel(new Model<String>() {
      //        @Override
      //        public String getObject()
      //        {
      //          final StringBuffer buf = new StringBuffer();
      //          buf.append(groovyResult.getResultAsHtmlString());
      //          if (groovyResult.getResult() != null && StringUtils.isNotEmpty(groovyResult.getOutput()) == true) {
      //            buf.append("<br/>\n");
      //            buf.append(HtmlHelper.escapeXml(groovyResult.getOutput()));
      //          }
      //          return buf.toString();
      //        }
      //      });
      //    } else {
      //      groovyResultRow.setVisible(false);
    }
    final ReportStorage reportStorage = parentPage.getReportStorage();
    final Report currentReport = reportStorage != null ? reportStorage.getCurrentReport() : null;
    final String reportPathHeading = getReportPath(currentReport);
    if (reportPathHeading != null) {
      reportPathPanel.setVisible(true);
    } else {
      reportPathPanel.setVisible(false);
    }
    super.onBeforeRender();
  }

  private String getReportPath(final Report report)
  {
    if (report == null) {
      return null;
    }
    final List<Report> ancestorList = report.getPath();
    if (CollectionUtils.isEmpty(ancestorList) == true) {
      return null;
    }
    final StringBuffer buf = new StringBuffer();
    for (final Report ancestor : ancestorList) {
      buf.append(ancestor.getId()).append(" -> ");
    }
    buf.append(report.getId());
    return buf.toString();
  }

  public String getGroovyScript()
  {
    return getReportScriptingStorage().getGroovyScript();
  }

  public void setGroovyScript(final String groovyScript)
  {
    getReportScriptingStorage().setGroovyScript(groovyScript);
  }

  private ReportScriptingStorage getReportScriptingStorage()
  {
    return parentPage.getReportScriptingStorage();
  }
}
