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

package org.projectforge.web.imagecropper;

import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringUtils;
import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.CSSPackageResource;
import org.apache.wicket.markup.html.JavascriptPackageResource;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.protocol.http.servlet.ServletWebRequest;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.projectforge.common.StringHelper;
import org.projectforge.core.Configuration;
import org.projectforge.user.PFUserContext;
import org.projectforge.web.wicket.AbstractSecuredPage;
import org.projectforge.web.wicket.WicketUtils;


public class ImageCropperPage extends AbstractSecuredPage
{
  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(ImageCropperPage.class);

  public static final String PARAM_ENABLE_WHITEBOARD_FILTER = "enableWhiteboardFilter";

  public static final String PARAM_SHOW_UPLOAD_BUTTON = "showUploadButton";

  public static final String PARAM_LANGUAGE = "language";

  public static final String PARAM_RATIOLIST = "ratioList";

  public static final String PARAM_DEFAULT_RATIO = "defaultRatio";

  public static final String PARAM_FILE_FORMAT = "fileFormat";

  @SpringBean(name = "configuration")
  private Configuration configuration;

  private String ratioList = "1:1,1:2,2:1,1:3,2:3,3:1,3:2,1:4,3:4,4:1,4:3,1:5,2:5,3:5,4:5,5:1,5:2,5:3,5:4";

  private String defaultRatio = "2:3";

  private boolean showUploadButton;

  private boolean enableWhiteBoardFilter;

  private String defaultLanguage;

  private String fileFormat = "";

  /**
   * See list of constants PARAM_* for supported parameters.
   * @param parameters
   */
  public ImageCropperPage(PageParameters parameters)
  {
    super(parameters);
    if (parameters.containsKey(PARAM_SHOW_UPLOAD_BUTTON) == true) {
      setEnableWhiteBoardFilter(parameters.getBoolean(PARAM_SHOW_UPLOAD_BUTTON));
    }
    if (parameters.containsKey(PARAM_ENABLE_WHITEBOARD_FILTER) == true) {
      setEnableWhiteBoardFilter(parameters.getBoolean(PARAM_ENABLE_WHITEBOARD_FILTER));
    }
    if (parameters.containsKey(PARAM_LANGUAGE) == true) {
      setDefaultLanguage(parameters.getString(PARAM_LANGUAGE));
    }
    if (parameters.containsKey(PARAM_RATIOLIST) == true) {
      setRatioList(parameters.getString(PARAM_RATIOLIST));
    }
    if (parameters.containsKey(PARAM_DEFAULT_RATIO) == true) {
      setDefaultRatio(parameters.getString(PARAM_DEFAULT_RATIO));
    }
    if (parameters.containsKey(PARAM_FILE_FORMAT) == true) {
      setFileFormat(parameters.getString(PARAM_FILE_FORMAT));
    }
    add(CSSPackageResource.getHeaderContribution("imagecropper/history/history.css"));
    add(JavascriptPackageResource.getHeaderContribution("imagecropper/history/history.js"));
    add(JavascriptPackageResource.getHeaderContribution("imagecropper/AC_OETags.js"));
    final ServletWebRequest req = (ServletWebRequest) this.getRequest();
    String domain;
    if (StringUtils.isNotBlank(configuration.getDomain()) == true) {
      domain = configuration.getDomain();
    } else {
      domain = req.getHttpServletRequest().getScheme()
          + "://"
          + req.getHttpServletRequest().getLocalName()
          + ":"
          + req.getHttpServletRequest().getLocalPort();
    }
    final String url = domain + req.getHttpServletRequest().getContextPath() + "/secure/";
    final StringBuffer buf = new StringBuffer();
    appendVar(buf, "serverURL", url); // TODO: Wird wohl nicht mehr gebraucht.
    appendVar(buf, "uploadImageFileTemporaryServlet", url + "UploadImageFileTemporary");
    appendVar(buf, "uploadImageFileTemporaryServletParams", "filedirectory=tempimages;filename=image");
    appendVar(buf, "downloadImageFileServlet", url + "DownloadImageFile");
    appendVar(buf, "downloadImageFileServletParams", "filedirectory=tempimages;filename=image");
    appendVar(buf, "uploadImageFileServlet", url + "UploadImageFile");
    appendVar(buf, "uploadImageFileServletParams", "filedirectory=images;filename=image;croppedname=cropped");
    appendVar(buf, "upAndDownloadImageFileAsByteArrayServlet", url + "UpAndDownloadImageFileAsByteArray");
    appendVar(buf, "upAndDownloadImageFileAsByteArrayServletParams", "filename=image;croppedname=cropped");
    final HttpSession httpSession = req.getHttpServletRequest().getSession();
    appendVar(buf, "sessionid", httpSession.getId());
    appendVar(buf, "ratioList", ratioList);
    appendVar(buf, "defaultRatio", defaultRatio);
    appendVar(buf, "isUploadBtn", showUploadButton);
    appendVar(buf, "whiteBoardFilter", enableWhiteBoardFilter);
    appendVar(buf, "language", getDefaultLanguage());
    appendVar(buf, "fileFormat", fileFormat);
    appendVar(buf, "flashFile", WicketUtils.getAbsoluteUrl("/imagecropper/MicromataImageCropper"));
    add(new Label("javaScriptVars", buf.toString()).setEscapeModelStrings(false));
  }

  /**
   * Valid Ratio Examples: "1:4, 4:1, 1:2, 2:1, 1:3, 3:1, 2:3, 3:2" etc.
   */
  public String getRatioList()
  {
    return ratioList;
  }

  public void setRatioList(String ratioList)
  {
    this.ratioList = ratioList;
  }

  /**
   * Wird Variable leer gelassen, kann die Ratio frei gewählt werden Wird Variable mit gültigem Wert befüllt, wird die Ratio auf den
   * Konfigurierten Wert gesetzt
   */
  public String getDefaultRatio()
  {
    return defaultRatio;
  }

  public void setDefaultRatio(String defaultRatio)
  {
    this.defaultRatio = defaultRatio;
  }
  
  /**
   * If true then the upload button in ImageCropper flash app will be shown.
   */
  public boolean isShowUploadButton()
  {
    return showUploadButton;
  }
  
  public void setShowUploadButton(boolean showUploadButton)
  {
    this.showUploadButton = showUploadButton;
  }

  /**
   * Auf true gesetzt kann WhiteBoardFilter verwendet werden.
   */
  public boolean isEnableWhiteBoardFilter()
  {
    return enableWhiteBoardFilter;
  }

  public void setEnableWhiteBoardFilter(boolean enableWhiteBoardFilter)
  {
    this.enableWhiteBoardFilter = enableWhiteBoardFilter;
  }

  /**
   * Valid FileFormat: jpg, jpeg, gif, png. Wird Variable leer gelassen, können alle Formate ausgewählt werden. Wird Variable mit gültigem
   * Wert befüllt, können Images nur im jeweligen Dateiformat erzeugt werden.
   */
  public String getFileFormat()
  {
    return fileFormat;
  }

  public void setFileFormat(String fileFormat)
  {
    if (StringHelper.isIn(fileFormat, "png", "gif", "jpg", "jpeg") == true) {
      this.fileFormat = fileFormat;
    } else {
      log.error("Unsupported file format: " + fileFormat);
    }
  }

  /**
   * Valid language: DE, EN Wird Variable leer gelassen, wird die language des Users verwendet.
   */
  public String getDefaultLanguage()
  {
    if (defaultLanguage != null) {
      return defaultLanguage;
    }
    return PFUserContext.getLocale().getCountry();
  }

  public void setDefaultLanguage(String defaultLanguage)
  {
    if (StringHelper.isIn(defaultLanguage, "EN", "DE") == true) {
      this.defaultLanguage = defaultLanguage;
    } else {
      log.error("Unsupported language: " + defaultLanguage);
    }
  }

  /**
   * @return false
   * @see org.projectforge.web.wicket.AbstractBasePage#isBookmarkLinkIconVisible()
   */
  @Override
  protected boolean isBookmarkLinkIconVisible()
  {
    return false;
  }
  
  private ImageCropperPage appendVar(final StringBuffer buf, final String variable, final Object value)
  {
    buf.append("var ").append(variable).append(" = ");
    if (value == null) {
      buf.append("null");
    } else if (value instanceof String) {
      buf.append("\"").append(value).append("\"");
    } else {
      buf.append(value);
    }
    buf.append(";\n");
    return this;
  }

  @Override
  protected String getTitle()
  {
    return "ImageCropper";
  }
}
