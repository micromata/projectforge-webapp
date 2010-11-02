package org.projectforge.web.gwiki;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Locale;

import org.apache.wicket.util.resource.AbstractStringResourceStream;
import org.apache.wicket.util.time.Time;

import de.micromata.genome.gwiki.model.GWikiElement;
import de.micromata.genome.gwiki.model.GWikiElementInfo;
import de.micromata.genome.gwiki.model.GWikiWeb;
import de.micromata.genome.gwiki.page.GWikiContext;
import de.micromata.genome.gwiki.page.GWikiStandaloneContext;

/**
 * 
 * @author Roger Rene Kommer (r.kommer@micromata.de)
 * 
 */
public class GWikiPageResourceStream extends AbstractStringResourceStream
{

  private static final long serialVersionUID = 2976761416648716062L;

  protected String path;

  protected Locale locale;

  protected Charset charSet;

  public GWikiPageResourceStream(String path)
  {
    this.path = path;
  }

  public void close() throws IOException
  {
  }

  public String getContentType()
  {
    // TODO
    return "text/html";
    // return element.getElementInfo().getProps().getStringValue(GWikiPropKeys.);
    // return element.getElementInfo().getProps().getStringValue(GWikiPropKeys.);
  }

  public Locale getLocale()
  {
    return locale;
  }

  public void setLocale(Locale locale)
  {
    this.locale = locale;
  }

  /**
   * @see org.apache.wicket.util.watch.IModifiable#lastModifiedTime()
   */
  public Time lastModifiedTime()
  {
    GWikiElementInfo ei = GWikiWeb.getWiki().findElementInfo(path);
    if (ei == null) {
      return Time.now();
    }
    long time = ei.getModifiedAt().getTime();
    return Time.milliseconds(time);
  }

  public String getString()
  {
    GWikiStandaloneContext wikiContext = GWikiStandaloneContext.create();
    try {
      GWikiContext.setCurrent(wikiContext);
      GWikiElement el = wikiContext.getWikiWeb().findElement(path);
      if (el == null) {
        return "";
      }
      el.serve(wikiContext);
      String ret = wikiContext.getOutString();
      return ret;
    } finally {
      GWikiContext.setCurrent(null);
    }
  }

  public void setCharset(Charset charset)
  {
    this.charSet = charset;
  }

}
