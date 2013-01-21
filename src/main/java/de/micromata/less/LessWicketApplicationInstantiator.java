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

package de.micromata.less;

import org.apache.wicket.protocol.http.WebApplication;
import org.lesscss.LessCompiler;

import java.io.File;
import java.io.Serializable;

/**
 * Compiler utility class for less resource files
 * 
 * @author Johannes Unterstein (j.unterstein@micromata.de)
 * 
 */
public class LessWicketApplicationInstantiator implements Serializable
{
  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(LessWicketApplicationInstantiator.class);

  public static LessResourceReference reference;

  private LessWicketApplicationInstantiator()
  {
  }

  public static void instantiate(WebApplication application, String folder, String lessPath, String cssPath) throws Exception
  {
    // compile file
    LessCompiler lessCompiler = new LessCompiler();

    final String relativeCssPath = folder + "/" + cssPath;
    final File referenceFile = new File(application.getClass().getClassLoader().getResource("i18nKeys.properties").toURI()).getParentFile();
    final File lessTargetFile = new File(referenceFile.getAbsolutePath() + "/" + folder + "/" + lessPath);
    final File cssTargetFile = new File(lessTargetFile.getAbsolutePath().replace(lessPath, "") + cssPath);

    log.info("compiling " + lessTargetFile.getAbsolutePath() + " to " + cssTargetFile.getAbsolutePath());
    lessCompiler.compile(lessTargetFile, cssTargetFile, false);

    // mount file
    reference = new LessResourceReference(relativeCssPath, cssTargetFile);
    application.mountResource(relativeCssPath, reference);
  }
}
