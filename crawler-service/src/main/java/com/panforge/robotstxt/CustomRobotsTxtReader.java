package com.panforge.robotstxt;

import java.io.IOException;
import java.io.InputStream;

public class CustomRobotsTxtReader {
  public RobotsTxt read(InputStream input) throws IOException {
    RobotsTxtReader reader = new RobotsTxtReader(new IgnoreInvalidPatternMatchingStrategy(), WinningStrategy.DEFAULT);
    return reader.readRobotsTxt(input);
  }
}
