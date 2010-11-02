package org.projectforge.web.gwiki;

import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;

import org.apache.commons.lang.StringUtils;
import org.apache.wicket.Component;
import org.apache.wicket.Localizer;
import org.apache.wicket.model.IModel;
import org.apache.wicket.resource.loader.IStringResourceLoader;

import de.micromata.genome.gwiki.model.GWikiWeb;
import de.micromata.genome.gwiki.page.impl.GWikiI18nElement;

/**
 * Using GWiki internationalization.
 * 
 * @author Roger Rene Kommer (r.kommer@micromata.de)
 * 
 */
public class GWikiLocalizer extends Localizer
{
  protected String[] modules;

  public GWikiLocalizer(String... modules)
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
  protected String getCacheKey(String key, Component component)
  {
    return super.getCacheKey(key, component);
  }

  @Override
  protected String getFromCache(String cacheKey)
  {
    return super.getFromCache(cacheKey);
  }

  @Deprecated
  @Override
  public String getString(String key, Component component, IModel< ? > model, Locale locale, String style, String defaultValue)
      throws MissingResourceException
  {
    return super.getString(key, component, model, locale, style, defaultValue);
  }

  @Override
  public String getString(String key, Component component, IModel< ? > model, String defaultValue) throws MissingResourceException
  {
    String ret = getI18NFromGWiki(key, component, model);
    if (ret != null) {
      return ret;
    }
    return super.getString(key, component, model, defaultValue);
  }

  @Override
  public String getStringIgnoreSettings(String key, Component component, IModel< ? > model, String defaultValue)
  {
    return super.getStringIgnoreSettings(key, component, model, defaultValue);
  }

  @Override
  public String substitutePropertyExpressions(Component component, String string, IModel< ? > model)
  {
    return super.substitutePropertyExpressions(component, string, model);
  }

  @Override
  protected Iterator<IStringResourceLoader> getStringResourceLoaders()
  {
    return super.getStringResourceLoaders();
  }

  @Override
  protected Map<String, String> newCache()
  {
    return super.newCache();
  }

  @Override
  protected void putIntoCache(String cacheKey, String string)
  {
    super.putIntoCache(cacheKey, string);
  }

}
