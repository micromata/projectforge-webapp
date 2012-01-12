/////////////////////////////////////////////////////////////////////////////
//
// Project ProjectForge Community Edition
//         www.projectforge.org
//
// Copyright (C) 2001-2012 Kai Reinhard (k.reinhard@micromata.com)
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

package org.projectforge.core;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.LineNumberReader;
import java.util.Collection;

import org.apache.commons.io.FileUtils;
import org.junit.Test;

/**
 */
public class ModifyJavaFileHeadersTest
{
  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(ModifyJavaFileHeadersTest.class);

  private static final String PATH = "src";

  @Test
  public void doit() throws IOException
  {
    //doitReally();
  }

  private void doitReally() throws IOException
  {
    log.info("Modify all Java file headers.");
    @SuppressWarnings("unchecked")
    final Collection<File> files = FileUtils.listFiles(new File(PATH), new String[] { "java"}, true);
    int counter = 0;
    for (final File file : files) {
      if (file.getAbsolutePath().contains("org/projectforge/lucene/PF") == true) {
        continue;
      }
      FileReader reader = null;
      LineNumberReader in = null;
      final StringBuffer buf = new StringBuffer();
      try {
        reader = new FileReader(file);
        log.info("Processing '" + file.getAbsolutePath() + "'.");
        in = new LineNumberReader(reader);
        String line = "";
        boolean header = true;
        buf.append("/////////////////////////////////////////////////////////////////////////////\n").append("//\n") //
        .append("// Project ProjectForge Community Edition\n") //
        .append("//         www.projectforge.org\n") //
        .append("//\n") //
        .append("// Copyright (C) 2001-2012 Kai Reinhard (k.reinhard@micromata.de)\n") //
        .append("//\n") //
        .append("// ProjectForge is dual-licensed.\n") //
        .append("//\n") //
        .append("// This community edition is free software; you can redistribute it and/or\n") //
        .append("// modify it under the terms of the GNU General Public License as published\n") //
        .append("// by the Free Software Foundation; version 3 of the License.\n") //
        .append("//\n") //
        .append("// This community edition is distributed in the hope that it will be useful,\n") //
        .append("// but WITHOUT ANY WARRANTY; without even the implied warranty of\n") //
        .append("// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General\n") //
        .append("// Public License for more details.\n") //
        .append("//\n") //
        .append("// You should have received a copy of the GNU General Public License along\n") //
        .append("// with this program; if not, see http://www.gnu.org/licenses/.\n") //
        .append("//\n") //
        .append("/////////////////////////////////////////////////////////////////////////////\n\n"); //
        while ((line = in.readLine()) != null) {
          if (header == true) {
            if (line.trim().startsWith("//") == true || line.trim().length() == 0) {
              continue;
            } else {
              header = false;
            }
          }
          buf.append(line).append("\n");
        }
      } finally {
        if (reader != null) {
          reader.close();
        }
        if (in != null) {
          in.close();
        }
      }
      FileWriter out = null;
      try {
        out = new FileWriter(file);
        out.write(buf.toString());
      } finally {
        if (out != null) {
          out.close();
        }
      }
      counter++;
    }
    log.info("All Java files were modified (" + counter + " files).");
  }
}
