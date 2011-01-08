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

package org.projectforge.web.scripting;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.upload.FileUploadField;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.util.lang.Bytes;
import org.projectforge.fibu.kost.reporting.Report;
import org.projectforge.fibu.kost.reporting.ReportStorage;
import org.projectforge.scripting.GroovyResult;
import org.projectforge.scripting.ScriptDO;
import org.projectforge.web.HtmlHelper;
import org.projectforge.web.fibu.ReportScriptingStorage;
import org.projectforge.web.wicket.AbstractForm;
import org.projectforge.web.wicket.FocusOnLoadBehavior;
import org.projectforge.web.wicket.WebConstants;
import org.projectforge.web.wicket.components.MaxLengthTextArea;
import org.projectforge.web.wicket.components.SingleButtonPanel;
import org.springframework.util.CollectionUtils;

public class ReportScriptingForm extends AbstractForm<ScriptDO, ReportScriptingPage>
{
  /**
   * Maximum length of Groovy script for form validation of text area (100.000 characters should be really enough).
   */
  public static final int MAX_GROOVY_LENGTH = 100000;

  private static final long serialVersionUID = 1868796548657011785L;

  protected FileUploadField fileUploadField;

  private WebMarkupContainer groovyResultRow;

  private Label groovyResultLabel;

  private Label reportPathHeading;

  public ReportScriptingForm(final ReportScriptingPage parentPage)
  {
    super(parentPage);
    // set this form to multipart mode (always needed for uploads!)
    setMultiPart(true);
    // Add one file input field
    add(fileUploadField = new FileUploadField("fileInput"));
    setMaxSize(Bytes.megabytes(1));
  }

  @SuppressWarnings("serial")
  protected void init()
  {
    add(new FeedbackPanel("feedback").setOutputMarkupId(true));
    add(reportPathHeading = new Label("reportPath"));
    add(new Label("filename", new Model<String>() {
      @Override
      public String getObject()
      {
        final ReportScriptingStorage storage = getReportScriptingStorage();
        return storage != null ? storage.getLastAddedFilename() : "";
      }
    }));
    final Button uploadButton = new Button("button", new Model<String>(getString("upload"))) {
      @Override
      public final void onSubmit()
      {
        parentPage.upload();
      }
    };
    add(new SingleButtonPanel("upload", uploadButton));
    final Button executeButton = new Button("button", new Model<String>(getString("execute"))) {
      @Override
      public final void onSubmit()
      {
        parentPage.execute();
      }
    };
    executeButton.add(WebConstants.BUTTON_CLASS_DEFAULT);
    add(new SingleButtonPanel("execute", executeButton));
    add(new MaxLengthTextArea(this, "groovyScript", getString("label.groovyScript"), new PropertyModel<String>(this, "groovyScript"),
        MAX_GROOVY_LENGTH).add(new FocusOnLoadBehavior()));
    add(groovyResultRow = new WebMarkupContainer("groovyResultRow"));
    groovyResultRow.add(groovyResultLabel = (Label) new Label("groovyResult").setEscapeModelStrings(false));
  }

  @SuppressWarnings("serial")
  @Override
  public void onBeforeRender()
  {
    final GroovyResult groovyResult = parentPage.groovyResult;
    if (groovyResult != null && groovyResult.hasResult() == true) {
      groovyResultRow.setVisible(true);
      groovyResultLabel.setDefaultModel(new Model<String>() {
        @Override
        public String getObject()
        {
          final StringBuffer buf = new StringBuffer();
          buf.append(groovyResult.getResultAsHtmlString());
          if (groovyResult.getResult() != null && StringUtils.isNotEmpty(groovyResult.getOutput()) == true) {
            buf.append("<br/>\n");
            buf.append(HtmlHelper.escapeXml(groovyResult.getOutput()));
          }
          return buf.toString();
        }
      });
    } else {
      groovyResultRow.setVisible(false);
    }
    final ReportStorage reportStorage = parentPage.getReportStorage();
    final Report currentReport = reportStorage != null ? reportStorage.getCurrentReport() : null;
    final String reportPath = getReportPath(currentReport);
    if (reportPath != null) {
      reportPathHeading.setVisible(true);
      reportPathHeading.setDefaultModel(new Model<String>(reportPath));
    } else {
      reportPathHeading.setVisible(false);
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

  public void setGroovyScript(String groovyScript)
  {
    getReportScriptingStorage().setGroovyScript(groovyScript);
  }

  private ReportScriptingStorage getReportScriptingStorage()
  {
    return parentPage.getReportScriptingStorage();
  }
}
