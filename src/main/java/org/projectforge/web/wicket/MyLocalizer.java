package org.projectforge.web.wicket;

import java.util.Locale;
import java.util.MissingResourceException;

import org.apache.commons.lang.StringUtils;
import org.apache.wicket.Component;
import org.apache.wicket.Localizer;
import org.apache.wicket.model.IModel;

import de.micromata.genome.gwiki.model.GWikiWeb;
import de.micromata.genome.gwiki.page.impl.GWikiI18nElement;

/**
 * Using GWiki internationalization.
 * 
 * @author Roger Rene Kommer (r.kommer@micromata.de)
 * 
 */
public class MyLocalizer extends Localizer
{
  protected String[] modules;

  public MyLocalizer(String... modules)
  {
    this.modules = modules;
  }

  protected String getI18NFromGWiki(String key, Component component, IModel< ? > model)
  {
    GWikiWeb wikiWeb = GWikiWeb.getWiki();
    Locale loc = component.getLocale();
    String lang = loc.getLanguage();
    if (StringUtils.isEmpty(lang) == true) {
      lang = "en";
    }

    for (String mod : modules) {
      GWikiI18nElement el = (GWikiI18nElement) wikiWeb.getElement(mod);
      String v = el.getMessage(lang, key);
      if (v != null) {
        return v;
      }
    }
    return null;
  }

  @Override
  public String getString(String key, Component component, IModel< ? > model, String defaultValue) throws MissingResourceException
  {
    final String ret = getI18NFromGWiki(key, component, model);
    if (ret != null) {
      return ret;
    }
    return super.getString(key, component, model, defaultValue);
  }
}
