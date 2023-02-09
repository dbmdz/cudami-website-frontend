package de.digitalcollections.cudami.frontend.website.config;

import java.net.URI;
import java.util.Map;
import java.util.UUID;
import javax.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties(prefix = "cudami")
@Validated
public class CudamiConfig {

  private final Server server;

  private final Map<String, UUID> webpages;

  @NotNull private final UUID website;

  @ConstructorBinding
  public CudamiConfig(Server server, Map<String, UUID> webpages, UUID website) {
    this.server = server;
    this.webpages = webpages != null ? Map.copyOf(webpages) : null;
    this.website = website;
  }

  public Server getServer() {
    return server;
  }

  public UUID getWebpage(String name) {
    if (webpages == null) {
      return null;
    }
    return webpages.get(name);
  }

  public Map<String, UUID> getWebpages() {
    return webpages != null ? Map.copyOf(webpages) : null;
  }

  public UUID getWebsite() {
    return website;
  }

  public static class Server {

    private final URI url;

    public Server(URI url) {
      this.url = url;
    }

    public URI getUrl() {
      return url;
    }
  }
}
