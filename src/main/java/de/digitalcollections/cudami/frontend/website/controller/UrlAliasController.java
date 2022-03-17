package de.digitalcollections.cudami.frontend.website.controller;

import de.digitalcollections.cudami.client.CudamiClient;
import de.digitalcollections.cudami.client.exceptions.HttpException;
import de.digitalcollections.cudami.client.identifiable.alias.CudamiUrlAliasClient;
import de.digitalcollections.cudami.frontend.website.config.CudamiConfig;
import de.digitalcollections.model.exception.ResourceNotFoundException;
import de.digitalcollections.model.identifiable.IdentifiableType;
import de.digitalcollections.model.identifiable.alias.LocalizedUrlAliases;
import de.digitalcollections.model.identifiable.alias.UrlAlias;
import de.digitalcollections.model.identifiable.entity.EntityType;
import java.util.Locale;
import java.util.UUID;
import javax.servlet.http.HttpServletRequest;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class UrlAliasController {

  private final UUID websiteUuid;

  private final CudamiUrlAliasClient cudamiUrlAliasClient;

  public UrlAliasController(CudamiClient cudamiClient, CudamiConfig cudamiConfig) {
    this.cudamiUrlAliasClient = cudamiClient.forUrlAliases();
    this.websiteUuid = cudamiConfig.getWebsite();
  }

  @GetMapping({"/{slug:.+}"})
  public String fallback(HttpServletRequest request, @PathVariable String slug)
      throws HttpException, ResourceNotFoundException {

    LocalizedUrlAliases localizedUrlAliases =
        cudamiUrlAliasClient.findPrimaryLinks(websiteUuid, slug);

    if (localizedUrlAliases.isEmpty()) {
      throw new ResourceNotFoundException();
    }

    Locale locale = getLocaleFromMatchingSlug(localizedUrlAliases);

    // We have found primaryLinks for the given slug, website and locale.
    // Now, check, if the slug from the request is the slug of the LocalizedUrlAlias
    UrlAlias firstMatchingUrlAlias = localizedUrlAliases.get(locale).get(0);
    if (slug.equals(firstMatchingUrlAlias.getSlug())) {
      request.setAttribute("request_uri", request.getRequestURI());
      // Make the correct forward now, depending on the target type
      UUID targetUUID = firstMatchingUrlAlias.getTargetUuid();
      IdentifiableType targetIdentifiableType = firstMatchingUrlAlias.getTargetIdentifiableType();

      // We have a "webpage"
      // FIXME: should be more generic for other identifiables not being an entity (e.g.
      // fileresources)
      if (IdentifiableType.RESOURCE.equals(targetIdentifiableType)) {
        return "forward:/p/" + targetUUID;
      }
      EntityType targetEntityType = firstMatchingUrlAlias.getTargetEntityType();
      switch (targetEntityType) {
          // FIXME: only example, not linked to a controller, yet
        case ARTICLE:
          return "forward:/article/" + targetUUID;
        default:
          throw new AssertionError();
      }
    } else {
      // Make a redirect to the primary slug
      return "redirect:/" + firstMatchingUrlAlias.getSlug();
    }
  }

  private Locale getLocaleFromMatchingSlug(LocalizedUrlAliases localizedUrlAliases) {
    // If we have only one possible matching language, we can just continue
    if (localizedUrlAliases.getTargetLanguages().size() == 1) {
      return localizedUrlAliases.getTargetLanguages().get(0);
    }

    // We have more than one, so let's either take the language from
    // the LocaleContextHolder, or the first one
    Locale locale = LocaleContextHolder.getLocale();
    if (!localizedUrlAliases.hasTargetLanguage(locale)) {
      locale = localizedUrlAliases.getTargetLanguages().get(0);
    }
    return locale;
  }
}
