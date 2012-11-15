/////////////////////////////////////////////////////////////////////////////
//
// Project   ProjectForge
//
// Copyright 2001-2009, Micromata GmbH, Kai Reinhard
//           All rights reserved.
//
/////////////////////////////////////////////////////////////////////////////

package org.projectforge.web.wicket.autocompletion;

/**
 * Markerinterface for forms which provides ignore feature of autocomplete fields
 * 
 * @author Johannes Unterstein (j.unterstein@micromata.de)
 * 
 */
public interface AutoCompleteIgnoreForm
{

  void ignore(PFAutoCompleteTextField< ? > autoCompleteField, String ignoreText);
}
