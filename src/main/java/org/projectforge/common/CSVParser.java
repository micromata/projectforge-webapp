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

package org.projectforge.common;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

/**
 * Simple csv parser
 * @author K.Reinhard@micromata.com
 * @author H.Spiewok@micromata.com (07/2005)
 */
public class CSVParser
{
  private char csvSeparatorChar = CSVWriter.DEFAULT_CSV_SEPARATOR_CHAR;

  public static final String ERROR_UNEXPECTED_QUOTATIONMARK = "Unexpected quotation mark \" (only allowed in quoted cells).";

  public static final String ERROR_QUOTATIONMARK_MISSED_AT_END_OF_CELL = "Quotation \" missed at the end of cell.";

  public static final String ERROR_DELIMITER_OR_NEW_LINE_EXPECTED_AFTER_QUOTATION_MARK = "Delimter or new line expected after quotation mark.";

  public static final String ERROR_UNEXPECTED_CHARACTER_AFTER_QUOTATION_MARK = "Unexpected character after quotation mark.";
  
  public enum Type
  {
    EOF, EOL, CHAR
  }

  private Reader source;

  private Type type;

  private int lineno = 1;

  private int colno = 0;

  private int val;

  private char cval;

  private int pushbackBuffer[] = new int[5];

  private int pushbackIndex = -1;

  public CSVParser(Reader source)
  {
    this.source = source;
  }

  /**
   * Returns null, if EOF.
   * @return
   */
  public List<String> parseLine()
  {
    if (type == Type.EOF) {
      return null;
    }
    List<String> result = null;
    do {
      String cell = parseCell();
      if (cell != null) {
        if (result == null) {
          result = new ArrayList<String>();
        }
        result.add(cell);
      }
    } while (type != Type.EOF && type != Type.EOL);
    return result;
  }

  public String parseCell()
  {
    skipWhitespaces();
    nextToken();
    if (type != Type.CHAR) {
      return null;
    }
    boolean quoted = false;
    if (cval == '"') {
      quoted = true; // value is quoted.
      nextToken();
    }
    StringBuffer buf = new StringBuffer();
    while (true) {
      if (type != Type.CHAR) {
        if (quoted == true) {
          throw new RuntimeException(createMessage(ERROR_QUOTATIONMARK_MISSED_AT_END_OF_CELL));
        }
        return buf.toString();
      }
      if (cval == '"') {
        if (quoted == false) {
          throw new RuntimeException(createMessage(ERROR_UNEXPECTED_QUOTATIONMARK, buf.toString()));
        }
        nextToken();
        if (type != Type.CHAR || cval == csvSeparatorChar) { // End of cell
          break;
        } else if (quoted == true && cval == '"') { // Escaped quotation mark
          buf.append(cval);
        } else if (Character.isWhitespace(cval) == true) {
          skipWhitespaces();
          nextToken();
          if (type != Type.CHAR || cval == csvSeparatorChar) {
            break;
          } else {
            throw new RuntimeException(createMessage(ERROR_DELIMITER_OR_NEW_LINE_EXPECTED_AFTER_QUOTATION_MARK));
          }
        } else {
          throw new RuntimeException(createMessage(ERROR_UNEXPECTED_CHARACTER_AFTER_QUOTATION_MARK));
        }
      } else if (quoted == false && cval == csvSeparatorChar) {
        break;
      } else {
        buf.append(cval);
      }
      nextToken();
    }
    return buf.toString();
  }
  
  public void setCsvSeparatorChar(final char csvSeparatorChar)
  {
    this.csvSeparatorChar = csvSeparatorChar;
  }

  private String createMessage(String msg, String s)
  {
    return createMessage(msg, s, lineno, colno);
  }

  private String createMessage(String msg)
  {
    return createMessage(msg, null, lineno, colno);
  }
  
  static String createMessage(String msg, String s, int line, int col) {
    return msg + " Error in line: " + line + " (" + col + ")" + (StringUtils.isNotBlank(s) ? ": " + s : "");
  }

  /**
   * Skips white spaces excluding new line ("\n" or "\r\n").
   */
  private void skipWhitespaces()
  {
    while (true) {
      nextToken();
      if (type != Type.CHAR || Character.isWhitespace(cval) == false) {
        unread();
        break;
      }
    }
  }

  public int lineno()
  {
    return lineno;
  }

  public boolean isIdentifierPart(char ch)
  {
    return Character.isUnicodeIdentifierPart(ch);
  }

  public Type nextToken()
  {
    cval = 0;
    type = Type.CHAR;
    val = read();
    if (val == -1) {
      // EOF
      type = Type.EOF;
      colno = 0;
      return type;
    }
    char c = (char) val;
    if (c == '\r') {
      val = read();
      if (val == -1) {
        unread(val); // EOF
      } else if ((char) val == '\n') {
        colno = 0;
        type = Type.EOL; // MS-DOS CR: \r\n
        return type;
      }
      unread(val); // No MS-DOS CR.
    } else if (c == '\n') {
      colno = 0;
      type = Type.EOL;
      return type;
    }
    type = Type.CHAR;
    cval = c;
    colno++;
    return type;
  }

  public void unread(int b)
  {
    if (b == '\n') {
      lineno--;
      colno = 0;
    } else {
      colno--;
    }
    pushbackBuffer[++pushbackIndex] = b;
  }

  public void unread()
  {
    unread(val);
  }

  public int read()
  {
    int b;
    if (pushbackIndex >= 0) {
      b = pushbackBuffer[pushbackIndex--];
    } else {
      try {
        b = source.read();
      } catch (IOException ex) {
        throw new RuntimeException("IOException in line: " + lineno, ex);
      }
    }
    if (b == '\n')
      lineno++;
    return b;
  }

}
