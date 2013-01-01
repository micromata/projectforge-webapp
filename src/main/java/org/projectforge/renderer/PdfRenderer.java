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

package org.projectforge.renderer;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.util.Map;

import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.lang.StringUtils;
import org.apache.fop.apps.FOPException;
import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.apps.Fop;
import org.apache.fop.apps.FopFactory;
import org.apache.fop.apps.MimeConstants;
import org.apache.log4j.Logger;
import org.projectforge.AppVersion;
import org.projectforge.core.ConfigXml;
import org.projectforge.core.Configuration;
import org.projectforge.core.ConfigurationParam;
import org.projectforge.core.InternalErrorException;
import org.projectforge.scripting.GroovyEngine;
import org.projectforge.user.PFUserContext;
import org.projectforge.user.PFUserDO;

/**
 * This class provides the functionality for rendering pdf files. The underlaying technology is XSL-FO. The dynamic data will be given in
 * xml format and the transformation will be done via xslt-scripts. For a better ease of use a meta language similiar to html will be used
 * instead of plain xsl-fo. The html file with jelly script elements will be rendered via xslt-scripts into xsl-fo and afterwards to pdf.
 * 
 * @author Kai Reinhard (k.reinhard@micromata.de)
 */
public class PdfRenderer
{
  private static final Logger log = Logger.getLogger(PdfRenderer.class);

  public final static String DEFAULT_FO_STYLE = "default-style-fo.xsl";

  private ConfigXml configXml;

  private String fontResourceDir;

  private String fontResourcePath;

  // private FontMap fontMap;

  /**
   * Relative to application's resource dir.
   */
  public void setFontResourceDir(final String fontResourceDir)
  {
    this.fontResourceDir = fontResourceDir;
  }

  private String getFontResourcePath()
  {
    if (fontResourcePath == null) {
      final File dir = new File(configXml.getFontsDirectory());
      if (dir.exists() == false) {
        log.error("Application's font dir does not exist: " + dir.getAbsolutePath());
      }
      this.fontResourcePath = dir.getAbsolutePath();
    }
    return fontResourcePath;
  }

  public void setConfigXml(final ConfigXml configXml)
  {
    this.configXml = configXml;
  }

  /*
   * private synchronized void initialize() { if (fontMap != null) { return; } fontMap = new FontMap(); final File fontDir = new
   * File(fontBaseDir); if (fontDir.isDirectory() == false) { log.warn("Given Font-Directory '" + fontBaseDir + "' does not exist. Can't
   * loading fonts.'"); return; } fontMap.loadFonts(fontDir); return; }
   */

  public byte[] render(final String stylesheet, final String groovyXml, final Map<String, Object> data)
  {
    // initialize();
    final PFUserDO user = PFUserContext.getUser();
    data.put("createdLabel", PFUserContext.getLocalizedString("created"));
    data.put("loggedInUser", user);
    data.put("baseDir", configXml.getResourcePath());
    data.put("appId", AppVersion.APP_ID);
    data.put("appVersion", AppVersion.NUMBER);
    data.put("organization", StringUtils.defaultString(Configuration.getInstance().getStringValue(ConfigurationParam.ORGANIZATION), AppVersion.APP_ID));
    final ByteArrayOutputStream baos = new ByteArrayOutputStream();
    log.info("stylesheet="
        + stylesheet
        + ", jellyXml="
        + groovyXml
        + ", baseDir="
        + configXml.getResourcePath()
        + ", fontBaseDir="
        + getFontResourcePath());
    // fopRenderer.processFo(styleSheet, xmlData, data, new PdfFopOutput(baos));
    // return baos.toByteArray();

    // configure fopFactory as desired
    final FopFactory fopFactory = FopFactory.newInstance();
    // Configuration cfg = fopFactory.getUserConfig();

    try {
      fopFactory.getFontManager().setFontBaseURL(getFontResourcePath());
    } catch (final MalformedURLException ex) {
      log.error(ex.getMessage(), ex);
    }
    /*
     * try { fopFactory.setUserConfig(baseDir + "/fop.config"); } catch (SAXException ex) { log.error(ex.getMessage(), ex); throw new
     * RuntimeException(ex); } catch (IOException ex) { log.error(ex.getMessage(), ex); throw new RuntimeException(ex); }
     */
    final FOUserAgent foUserAgent = fopFactory.newFOUserAgent();
    try {
      foUserAgent.getFactory().getFontManager().setFontBaseURL(getFontResourcePath());
    } catch (final MalformedURLException ex) {
      log.error(ex.getMessage(), ex);
    }
    // configure foUserAgent as desired

    try {
      // Construct fop with desired output format
      final Fop fop = fopFactory.newFop(MimeConstants.MIME_PDF, foUserAgent, baos);

      // Setup XSLT
      final TransformerFactory factory = TransformerFactory.newInstance();
      Object[] result = configXml.getInputStream(stylesheet);
      final InputStream xsltInputStream = (InputStream) result[0];
      final StreamSource xltStreamSource = new StreamSource(xsltInputStream);
      final String url = (String) result[1];
      if (url == null) {
        log.error("Url of xsl resource is null.");
        throw new InternalErrorException();
      }
      xltStreamSource.setSystemId(url);

      final Transformer transformer = factory.newTransformer(xltStreamSource);

      // Set the value of a <param> in the stylesheet
      for (final Map.Entry<String, Object> entry : data.entrySet()) {
        transformer.setParameter(entry.getKey(), entry.getValue());
      }

      // First run jelly through xmlData:
      result = configXml.getContent(groovyXml);
      final GroovyEngine groovyEngine = new GroovyEngine(data, PFUserContext.getLocale(), PFUserContext.getTimeZone());
      final String groovyXmlInput = groovyEngine.preprocessGroovyXml((String) result[0]);
      final String xmlData = groovyEngine.executeTemplate(groovyXmlInput);

      // Setup input for XSLT transformation
      final StringReader xmlDataReader = new StringReader(xmlData);
      final Source src = new StreamSource(xmlDataReader);

      // Resulting SAX events (the generated FO) must be piped through to FOP
      final Result res = new SAXResult(fop.getDefaultHandler());

      // Start XSLT transformation and FOP processing
      transformer.transform(src, res);
    } catch (final FOPException ex) {
      log.error(ex.getMessage(), ex);
      throw new RuntimeException(ex);
    } catch (final TransformerConfigurationException ex) {
      log.error(ex.getMessage(), ex);
      throw new RuntimeException(ex);
    } catch (final TransformerException ex) {
      log.error(ex.getMessage(), ex);
      throw new RuntimeException(ex);
    } finally {
      try {
        baos.close();
      } catch (final IOException ex) {
        log.error(ex.getMessage(), ex);
        throw new RuntimeException(ex);
      }
    }
    return baos.toByteArray();
  }
}
