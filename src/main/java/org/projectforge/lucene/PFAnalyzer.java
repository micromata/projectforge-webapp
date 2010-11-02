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

import java.io.IOException;
import java.io.Reader;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.LowerCaseFilter;
import org.apache.lucene.analysis.StopFilter;
import org.apache.lucene.analysis.TokenStream;

/**
 * Filters {@link PFTokenizer} with {@link PFFilter}, {@link
 * LowerCaseFilter}.
 * <br/>
 * Modified StandardAnalyzer without any stop words.
 */
public class PFAnalyzer extends Analyzer {
  //private Set stopSet;

  /** An array containing some common English words that are usually not
  useful for searching. */
  //public static final String[] STOP_WORDS = StopAnalyzer.ENGLISH_STOP_WORDS;

  /** Builds an analyzer with the default stop words ({@link #STOP_WORDS}). */
  public PFAnalyzer() {
    //this(STOP_WORDS);
  }

  /** Constructs a {@link PFTokenizer} filtered by a {@link
  PFFilter}, a {@link LowerCaseFilter} and a {@link StopFilter}. */
  public TokenStream tokenStream(String fieldName, Reader reader) {
    PFTokenizer tokenStream = new PFTokenizer(reader);
    tokenStream.setMaxTokenLength(maxTokenLength);
    TokenStream result = new PFFilter(tokenStream);
    result = new LowerCaseFilter(result);
    //result = new StopFilter(result, stopSet);
    return result;
  }

  private static final class SavedStreams {
    PFTokenizer tokenStream;
    TokenStream filteredTokenStream;
  }

  /** Default maximum allowed token length */
  public static final int DEFAULT_MAX_TOKEN_LENGTH = 255;

  private int maxTokenLength = DEFAULT_MAX_TOKEN_LENGTH;

  /**
   * Set maximum allowed token length.  If a token is seen
   * that exceeds this length then it is discarded.  This
   * setting only takes effect the next time tokenStream or
   * reusableTokenStream is called.
   */
  public void setMaxTokenLength(int length) {
    maxTokenLength = length;
  }
    
  /**
   * @see #setMaxTokenLength
   */
  public int getMaxTokenLength() {
    return maxTokenLength;
  }
  
  public TokenStream reusableTokenStream(String fieldName, Reader reader) throws IOException {
    SavedStreams streams = (SavedStreams) getPreviousTokenStream();
    if (streams == null) {
      streams = new SavedStreams();
      setPreviousTokenStream(streams);
      streams.tokenStream = new PFTokenizer(reader);
      streams.filteredTokenStream = new PFFilter(streams.tokenStream);
      streams.filteredTokenStream = new LowerCaseFilter(streams.filteredTokenStream);
      //streams.filteredTokenStream = new StopFilter(streams.filteredTokenStream, stopSet);
    } else {
      streams.tokenStream.reset(reader);
    }
    streams.tokenStream.setMaxTokenLength(maxTokenLength);
    
    return streams.filteredTokenStream;
  }
}
