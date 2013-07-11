/////////////////////////////////////////////////////////////////////////////
//
// Project   ProjectForge
//
// Copyright 2001-2009, Micromata GmbH, Kai Reinhard
//           All rights reserved.
//
/////////////////////////////////////////////////////////////////////////////

package org.projectforge.plugins.chimney.resources;

import org.apache.wicket.request.resource.PackageResourceReference;
import org.apache.wicket.request.resource.ResourceReference;

public class FileResources
{
  public static final ResourceReference CHIMNEY_CSS = new PackageResourceReference(FileResources.class, "chimney.css");
  public static final ResourceReference I18NNEXT_JS = new PackageResourceReference(FileResources.class, "i18next-1.4.1.js");
  public static final ResourceReference JSGANTT_CSS = new PackageResourceReference(FileResources.class, "jsgantt/jsgantt.css");
  public static final ResourceReference JSGANTT_JS = new PackageResourceReference(FileResources.class, "jsgantt/jsgantt.js");
}
