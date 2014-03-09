package org.projectforge.web.scripting;

import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.projectforge.common.DateHelper;
import org.projectforge.export.ExportJson;
import org.projectforge.scripting.GroovyResult;
import org.projectforge.web.wicket.AbstractStandardFormPage;
import org.projectforge.web.wicket.DownloadUtils;

import java.util.Date;

/**
 * @author Johannes Unterstein (j.unterstein@micromata.de)
 */
public abstract class AbstractScriptingPage extends AbstractStandardFormPage
{

  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(AbstractScriptingPage.class);

  protected GroovyResult groovyResult;

  public AbstractScriptingPage(final PageParameters parameters)
  {
    super(parameters);
  }

  protected void jsonExport()
  {
    try {
      final ExportJson exportJson = (ExportJson) groovyResult.getResult();
      final StringBuilder sb = new StringBuilder();
      sb.append(exportJson.getJsonName()).append("_");
      sb.append(DateHelper.getTimestampAsFilenameSuffix(new Date())).append(".json");
      final String filename = sb.toString();
      DownloadUtils.setDownloadTarget(filename, exportJson.createResourceStreamWriter());
    } catch (final Exception ex) {
      error(getLocalizedMessage("error", ex.getMessage()));
      log.error(ex.getMessage(), ex);
    }
  }
}
