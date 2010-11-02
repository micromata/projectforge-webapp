package org.projectforge.web.gwiki;

import java.util.Locale;

import org.apache.wicket.util.resource.IResourceStream;
import org.apache.wicket.util.resource.locator.IResourceStreamLocator;

import de.micromata.genome.gwiki.model.GWikiElementInfo;
import de.micromata.genome.gwiki.model.GWikiWeb;

/**
 * Locates Content from the GWiki.
 * 
 * @author Roger Rene Kommer (r.kommer@micromata.de)
 * 
 */
public class GWikiResourceStreamLocator implements IResourceStreamLocator
{
  protected IResourceStreamLocator parent;

  public GWikiResourceStreamLocator(IResourceStreamLocator parent)
  {
    this.parent = parent;
  }

  public IResourceStream locate(Class< ? > clazz, String path)
  {
    return parent.locate(clazz, path);
  }

  public IResourceStream locate(Class< ? > clazz, String path, String style, Locale locale, String extension)
  {
    String id = path + "." + extension;
    GWikiWeb wiki = GWikiWeb.getWiki();
    GWikiElementInfo el = wiki.findElementInfo(id);
    if (el == null) {
      return parent.locate(clazz, path, style, locale, extension);
    }
    return new GWikiPageResourceStream(id);
  }

  public IResourceStreamLocator getParent()
  {
    return parent;
  }

  public void setParent(IResourceStreamLocator parent)
  {
    this.parent = parent;
  }

}
