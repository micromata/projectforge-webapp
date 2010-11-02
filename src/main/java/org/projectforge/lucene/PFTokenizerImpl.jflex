package org.projectforge.lucene;

/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/*

NOTE: if you change StandardTokenizerImpl.jflex and need to regenerate
      the tokenizer, remember to use JRE 1.4 to run jflex (before
      Lucene 3.0).  This grammar now uses constructs (eg :digit:,
      :letter:) whose meaning can vary according to the JRE used to
      run jflex.  See
      https://issues.apache.org/jira/browse/LUCENE-1126 for details.

*/

import org.apache.lucene.analysis.Token;

%%

%class PFTokenizerImpl
%unicode
%integer
%function getNextToken
%pack
%char

%{

public static final int ALPHANUM          = PFTokenizer.ALPHANUM;
public static final int APOSTROPHE        = PFTokenizer.APOSTROPHE;
public static final int ACRONYM           = PFTokenizer.ACRONYM;
public static final int ISO_DATE          = PFTokenizer.ISO_DATE; // Kai
public static final int COMPANY           = PFTokenizer.COMPANY;
public static final int EMAIL             = PFTokenizer.EMAIL;
public static final int HOST              = PFTokenizer.HOST;
public static final int NUM               = PFTokenizer.NUM;
public static final int CJ                = PFTokenizer.CJ;

public static final String [] TOKEN_TYPES = PFTokenizer.TOKEN_TYPES;

public final int yychar()
{
    return yychar;
}

/**
 * Fills Lucene token with the current token text.
 */
final void getText(Token t) {
  t.setTermBuffer(zzBuffer, zzStartRead, zzMarkedPos-zzStartRead);
}
%}

THAI       = [\u0E00-\u0E59]

// basic word: a sequence of digits & letters (includes Thai to enable ThaiAnalyzer to function)
ALPHANUM   = ({LETTER}|{THAI}|[:digit:])+

// internal apostrophes: O'Reilly, you're, O'Reilly's
// use a post-filter to remove possesives
APOSTROPHE =  {ALPHA} ("'" {ALPHA})+

// acronyms: U.S.A., I.B.M., etc.
// use a post-filter to remove dots
ACRONYM    =  {LETTER} "." ({LETTER} ".")+

ISO_DATE   = [0-9]{4} "-" [0-9]{2} "-" [0-9]{2} // Kai

// company names like AT&T and Excite@Home and K+S. // Kai
COMPANY    =  {ALPHA} ("&"|"@"|"+") {ALPHA}

// email addresses
EMAIL      =  {ALPHANUM} (("."|"-"|"_") {ALPHANUM})* "@" {ALPHANUM} (("."|"-") {ALPHANUM})+

// hostname
HOST       =  {ALPHANUM} ((".") {ALPHANUM})+

// floating point, serial, model numbers, ip addresses, etc.
// every other segment must have at least one digit
NUM        = ({ALPHANUM} {P} {HAS_DIGIT}
           | {HAS_DIGIT} {P} {ALPHANUM}
           | {ALPHANUM} ({P} {HAS_DIGIT} {P} {ALPHANUM})+
           | {HAS_DIGIT} ({P} {ALPHANUM} {P} {HAS_DIGIT})+
           | {ALPHANUM} {P} {HAS_DIGIT} ({P} {ALPHANUM} {P} {HAS_DIGIT})+
           | {HAS_DIGIT} {P} {ALPHANUM} ({P} {HAS_DIGIT} {P} {ALPHANUM})+)

// punctuation
P	         = ("_"|"-"|"/"|"."|",")

// at least one digit
HAS_DIGIT  = ({LETTER}|[:digit:])* [:digit:] ({LETTER}|[:digit:])*

ALPHA      = ({LETTER})+

// From the JFlex manual: "the expression that matches everything of <a> not matched by <b> is !(!<a>|<b>)"
LETTER     = !(![:letter:]|{CJ})

// Chinese and Japanese (but NOT Korean, which is included in [:letter:])
CJ         = [\u3100-\u312f\u3040-\u309F\u30A0-\u30FF\u31F0-\u31FF\u3300-\u337f\u3400-\u4dbf\u4e00-\u9fff\uf900-\ufaff\uff65-\uff9f]

WHITESPACE = \r\n | [ \r\n\t\f]

%%

{ALPHANUM}                                                     { return ALPHANUM; }
{APOSTROPHE}                                                   { return APOSTROPHE; }
{ACRONYM}                                                      { return ACRONYM; }
{ISO_DATE}                                                     { return ISO_DATE; } // Kai
{COMPANY}                                                      { return COMPANY; }
{EMAIL}                                                        { return EMAIL; }
{HOST}                                                         { return HOST; }
{NUM}                                                          { return NUM; }
{CJ}                                                           { return CJ; }

/** Ignore the rest */
. | {WHITESPACE}                                               { /* ignore */ }
