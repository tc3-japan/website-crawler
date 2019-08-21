package com.panforge.robotstxt;

import static com.panforge.robotstxt.URLDecoder.decode;
import static com.panforge.robotstxt.WildcardsCompiler.compile;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class IgnoreInvalidPatternMatchingStrategy implements MatchingStrategy {

  /**
   * @see com.panforge.robotstxt.MatchingStrategy#DEFAULT
   */
  @Override
  public boolean matches(String pattern, String pathToTest) {
    if (pathToTest == null) {
      return false;
    }
    if (pattern == null || pattern.isEmpty()) {
      return true;
    }

    String relativePath = decode(pathToTest);
    try {
      Pattern pt = compile(pattern);
      // Protection against Regular Expression Denial of Service.
      // https://www.owasp.org/index.php/Regular_expression_Denial_of_Service_-_ReDoS
      // @author vishnu rao
      Matcher timeBoundMatcher = TimeLimitedMatcherFactory.matcher(pt, relativePath);
      return timeBoundMatcher.find() && timeBoundMatcher.start() == 0;
    } catch (PatternSyntaxException e) {
      return false;
    } catch (TimeLimitedMatcherFactory.RegExpTimeoutException e) {
      return false;
    }
  }

}
