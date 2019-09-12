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

package com.topcoder.productsearch.common.url.normalizers;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import javax.xml.parsers.DocumentBuilderFactory;

import com.topcoder.productsearch.common.url.URLNormalizer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.util.ResourceUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.InputSource;

/**
 * Allows users to do regex substitutions on all/any URLs that are encountered,
 * which is useful for stripping session IDs from URLs.
 * 
 * <p>
 * This class uses the <tt>urlnormalizer.regex.file</tt> property. It should be
 * set to the file name of an xml file which should contain the patterns and
 * substitutions to be done on encountered URLs.
 * </p>
 * <p>
 * This class also supports different rules depending on the scope. Please see
 * the javadoc in {@link org.apache.nutch.net.URLNormalizers} for more details.
 * </p>
 * 
 * @author 
 */
public class RegexURLNormalizer implements URLNormalizer {

  /**
   * Name of regex file for url-normalization
   */
  @Value("${crawler-settings.urlnormalizer-regex-file}")
  private String urlNormalizerRegexFile;


  private static final Logger LOG = LoggerFactory
      .getLogger(RegexURLNormalizer.class);

  /**
   * Class which holds a compiled pattern and its corresponding substition
   * string.
   */
  private static class Rule {
    public Pattern pattern;

    public String substitution;
  }

  private List<Rule> rules;

  private static final List<Rule> EMPTY_RULES = Collections.emptyList();
  
  /**
   * The default constructor which is called from UrlNormalizerFactory
   * (normalizerClass.newInstance()) in method: getNormalizer()
   * 
   * @throws IOException*
   */
  // public RegexURLNormalizer() throws IOException {
  //   rules = readRegexConfigurationFile();
  // }



  /**
   * This function does the replacements by iterating through all the regex
   * patterns. It accepts a string url as input and returns the altered string.
   */
  public String regexNormalize(String urlString) {

    Iterator<Rule> i = rules.iterator();
    while (i.hasNext()) {
      Rule r = (Rule) i.next();
      Matcher matcher = r.pattern.matcher(urlString);
      urlString = matcher.replaceAll(r.substitution);
    }
    return urlString;
  }

  public String normalize(String urlString)
      throws MalformedURLException {
        
    return regexNormalize(urlString);
  }

  /**
   * Reads the configuration file and populates a List of Rules.
   * 
   * @throws IOException
   */
  private List<Rule> readRegexConfigurationFile() throws IOException {

    LOG.info("loading " + urlNormalizerRegexFile);

    Reader reader = null;
    InputStream inputStream = null;
    try {
      ClassLoader classLoader = getClass().getClassLoader();
      inputStream = classLoader.getResourceAsStream(urlNormalizerRegexFile);
      System.out.println("InputStream: "+inputStream.toString());
      reader = new InputStreamReader(inputStream, "UTF-8");
      return readConfiguration(reader);
      
    } catch (Exception e) {
      System.out.println("Error reading regex configuration file"+e.getMessage());
      LOG.error("Error loading rules from '" + urlNormalizerRegexFile +" "+ e);
      return EMPTY_RULES;
    } finally {
      if (reader!=null) {
        reader.close();
      }
    }
  }

  private List<Rule> readConfiguration(Reader reader) {
    List<Rule> rules = new ArrayList<Rule>();
    try {

      // borrowed heavily from code in Configuration.java
      Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder()
          .parse(new InputSource(reader));
      Element root = doc.getDocumentElement();
      if ((!"regex-normalize".equals(root.getTagName()))
          && (LOG.isErrorEnabled())) {
        LOG.error("bad conf file: top-level element not <regex-normalize>");
      }
      NodeList regexes = root.getChildNodes();
      for (int i = 0; i < regexes.getLength(); i++) {
        Node regexNode = regexes.item(i);
        if (!(regexNode instanceof Element))
          continue;
        Element regex = (Element) regexNode;
        if ((!"regex".equals(regex.getTagName())) && (LOG.isWarnEnabled())) {
          LOG.warn("bad conf file: element not <regex>");
        }
        NodeList fields = regex.getChildNodes();
        String patternValue = null;
        String subValue = null;
        for (int j = 0; j < fields.getLength(); j++) {
          Node fieldNode = fields.item(j);
          if (!(fieldNode instanceof Element))
            continue;
          Element field = (Element) fieldNode;
          if ("pattern".equals(field.getTagName()) && field.hasChildNodes())
            patternValue = ((Text) field.getFirstChild()).getData();
          if ("substitution".equals(field.getTagName())
              && field.hasChildNodes())
            subValue = ((Text) field.getFirstChild()).getData();
          if (!field.hasChildNodes())
            subValue = "";
        }
        if (patternValue != null && subValue != null) {
          Rule rule = new Rule();
          try {
            rule.pattern = Pattern.compile(patternValue);
          } catch (PatternSyntaxException e) {
            if (LOG.isErrorEnabled()) {
              LOG.error("skipped rule: " + patternValue + " -> " + subValue
                  + " : invalid regular expression pattern: " + e);
            }
            continue;
          }
          rule.substitution = subValue;
          rules.add(rule);
        }
      }
    } catch (RuntimeException e) {
        throw e;
    }
      catch (Exception e) {
      if (LOG.isErrorEnabled()) {
        LOG.error("error parsing conf file: " + e);
      }
      return EMPTY_RULES;
    }
    if (rules.size() == 0)
      return EMPTY_RULES;
    return rules;
  }

  /** Spits out patterns and substitutions that are in the configuration file. */
  public static void main(String args[]) throws PatternSyntaxException,
      IOException {
    RegexURLNormalizer normalizer = new RegexURLNormalizer();
    
    Iterator<Rule> i = normalizer.rules.iterator();
    System.out.println("* Rules:");
    while (i.hasNext()) {
      Rule r = i.next();
      System.out.print("  " + r.pattern.pattern() + " -> ");
      System.out.println(r.substitution);
    }
    
    System.exit(0);
  }

}
