package de.digitalcollections.cudami.frontend.website.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "template")
public class TemplateConfig {

  private final String name;
  private final int navMaxLevel;

  public TemplateConfig(String name, int navMaxLevel) {
    this.name = name;
    this.navMaxLevel = navMaxLevel;
  }

  public String getName() {
    return name;
  }

  public int getNavMaxLevel() {
    return navMaxLevel;
  }
}
