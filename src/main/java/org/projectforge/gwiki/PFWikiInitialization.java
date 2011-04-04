/////////////////////////////////////////////////////////////////////////////
//
// Project   ProjectForge
//
// Copyright 2001-2009, Micromata GmbH, Kai Reinhard
//           All rights reserved.
//
/////////////////////////////////////////////////////////////////////////////

package org.projectforge.gwiki;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

import de.micromata.genome.gwiki.model.GWikiArtefakt;
import de.micromata.genome.gwiki.model.GWikiElement;
import de.micromata.genome.gwiki.model.GWikiWebUtils;
import de.micromata.genome.gwiki.page.GWikiStandaloneContext;
import de.micromata.genome.gwiki.page.impl.GWikiWikiPageBaseArtefakt;

public class PFWikiInitialization extends HttpServlet
{
  private static final String PROJECT_FORGE = "ProjectForge";

  private static final String PF_CONTENT = "PF_CONTENT";

  private static final long serialVersionUID = 4151584372770766157L;

  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(PFWikiInitialization.class);

  @Override
  public void init(final ServletConfig config) throws ServletException
  {
    final GWikiStandaloneContext wikiContext = GWikiStandaloneContext.create();
    final String pageId = "index";
    final String content = "{children:depth=1|sort=title|withPageIntro=true}";
    final GWikiElement indexElement = wikiContext.getWikiWeb().findElement(pageId);

    if (indexElement != null && indexElement.getElementInfo().getProps().getBooleanValue(PF_CONTENT)) {
      return;
    }

    wikiContext.setSkin(PROJECT_FORGE);
    final String metaTemplateId = "admin/templates/StandardWikiPageMetaTemplate";
    final String title = "Overview";

    // Creates new GWiki Element
    final GWikiElement element = GWikiWebUtils.createNewElement(wikiContext, pageId, metaTemplateId, title);
    element.getElementInfo().getProps().setBooleanValue(PF_CONTENT, true);
    element.getElementInfo().getProps().setStringValue("SKIN", PROJECT_FORGE);

    // Gets GWiki 'MainPage'. All wiki content will be stored in this artifact
    final GWikiArtefakt< ? > part = element.getPart("MainPage");

    // Tests if the 'MainPage' is a GWikiWikiPage
    if (part != null && part instanceof GWikiWikiPageBaseArtefakt) {
      final GWikiWikiPageBaseArtefakt art = (GWikiWikiPageBaseArtefakt) part;

      // Sets storage data to the GWiki artifact
      art.setStorageData(content);
      art.compileFragements(wikiContext);
    }

    // saves Element and updates page caches
    wikiContext.getWikiWeb().saveElement(wikiContext, element, false);

    log.info("created gwiki index page as spaces overview");
  }

}
