package org.projectforge.web.gwiki;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.Model;

import de.micromata.genome.gwiki.model.GWikiElement;
import de.micromata.genome.gwiki.model.GWikiWeb;
import de.micromata.genome.gwiki.page.GWikiContext;
import de.micromata.genome.gwiki.page.GWikiStandaloneContext;
import de.micromata.genome.gwiki.plugin.GWikiPluginRepository;

/**
 * Label fetched from gwiki fragment.
 * 
 * @author Roger Rene Kommer (r.kommer@micromata.de)
 * 
 */
public class GWikiLabel extends Label
{

  private static final long serialVersionUID = -3802667568734694375L;

  @SuppressWarnings("serial")
  public GWikiLabel(String id, final String wikiPage)
  {

    super(id, new Model<String>() {

      @Override
      public String getObject()
      {
        return getWikiPage(wikiPage);
      }

    });
    setEscapeModelStrings(false);
  }

  public static String getWikiPage(String pageId)
  {

    GWikiWeb wikiWeb = GWikiWeb.getWiki();
    GWikiElement el = wikiWeb.getElement(pageId);
    
    GWikiStandaloneContext wikiContext = GWikiStandaloneContext.create();
    
    // TODO (cclaus) this looks like a bug. A plugin is registered which doesn't exist
    GWikiPluginRepository pluginRepository = wikiContext.getWikiWeb().getDaoContext().getPluginRepository();
    pluginRepository.deactivatePlugin(wikiContext, "gwiki.s5slideshow");
    
    try {
      wikiContext.setWikiElement(el);
      el.serve(wikiContext);
      
      String sout = wikiContext.getOutString();
      return sout;
    } finally {
      GWikiContext.setCurrent(null);
    }
  }

}
