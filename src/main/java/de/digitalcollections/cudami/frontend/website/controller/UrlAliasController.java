package de.digitalcollections.cudami.frontend.website.controller;

import de.digitalcollections.cudami.client.CudamiClient;
import de.digitalcollections.cudami.client.identifiable.alias.CudamiUrlAliasClient;
import de.digitalcollections.cudami.frontend.website.config.CudamiConfig;
import de.digitalcollections.model.exception.ResourceNotFoundException;
import de.digitalcollections.model.exception.TechnicalException;
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

  private final CudamiUrlAliasClient cudamiUrlAliasClient;
  private final UUID websiteUuid;

  public UrlAliasController(CudamiClient cudamiClient, CudamiConfig cudamiConfig) {
    this.cudamiUrlAliasClient = cudamiClient.forUrlAliases();
    this.websiteUuid = cudamiConfig.getWebsite();
  }

  /**
   * Forwards to Identifiable specific endpoint if SLUG is a primary url alias. Redirects to primary
   * SLUG if given SLUG is not a primaray url alias.
   *
   * @param request current http request
   * @param slug human readable relative URL to an identifiable
   * @return target webpage or redirect to primary slug
   * @throws ResourceNotFoundException if slug is unknown
   * @throws TechnicalException if backend system error
   */
  @GetMapping({"/{slug:.+}"})
  public String fallback(HttpServletRequest request, @PathVariable String slug)
      throws ResourceNotFoundException, TechnicalException {

    // fields of UrlAlias we can work with:
    // boolean primary, String slug, Website website,
    // Locale targetLanguage, IdentifiableType targetIdentifiableType, EntityType targetEntityType,
    // UUID targetUuid
    // get all primary UrlAliases
    // * for our (configured) website (or "null"-website if otherwise not found)
    // * having same slug (no matter what target language)
    // * having same target identifiable
    // other words: get all primary url aliases for the configured website referring to the same
    // target identifiable(s) as the slug.
    LocalizedUrlAliases primaryUrlAliasesOfTargetIdentifiable =
        cudamiUrlAliasClient.getPrimaryLinks(websiteUuid, slug);

    // if slug does not exist at all: ResourceNotFoundException
    if (primaryUrlAliasesOfTargetIdentifiable.isEmpty()) {
      throw new ResourceNotFoundException();
    }

    UrlAlias firstMatchingPrimaryUrlAlias =
        getFirstMatchingPrimaryUrlAlias(primaryUrlAliasesOfTargetIdentifiable);
    if (slug.equals(firstMatchingPrimaryUrlAlias.getSlug())) {
      request.setAttribute("request_uri", request.getRequestURI());
      // Make the correct forward now, depending on the target type
      UUID targetUUID = firstMatchingPrimaryUrlAlias.getTargetUuid();
      IdentifiableType targetIdentifiableType =
          firstMatchingPrimaryUrlAlias.getTargetIdentifiableType();

      // We have a "webpage"
      // FIXME: should be more generic for other identifiables not being an entity (e.g.
      // fileresources)
      if (IdentifiableType.RESOURCE.equals(targetIdentifiableType)) {
        return "forward:/p/" + targetUUID;
      }
      EntityType targetEntityType = firstMatchingPrimaryUrlAlias.getTargetEntityType();
      switch (targetEntityType) {
          // FIXME: only example, not linked to a controller, yet
        case ARTICLE:
          return "forward:/article/" + targetUUID;
        default:
          throw new AssertionError();
      }
    } else {
      // Make a redirect to the primary slug
      return "redirect:/" + firstMatchingPrimaryUrlAlias.getSlug();
    }
  }

  private UrlAlias getFirstMatchingPrimaryUrlAlias(
      LocalizedUrlAliases primaryUrlAliasesOfTargetIdentifiable) {
    // get existing target locale / language
    Locale locale = getLocaleFromPrimaryUrlAliases(primaryUrlAliasesOfTargetIdentifiable);
    // We have found primaryLinks for the given slug, website and locale.
    // Now, check, if the slug from the request is the slug of the LocalizedUrlAlias
    UrlAlias firstMatchingPrimaryUrlAlias =
        primaryUrlAliasesOfTargetIdentifiable.get(locale).get(0);
    return firstMatchingPrimaryUrlAlias;
  }

  protected Locale getLocaleFromPrimaryUrlAliases(LocalizedUrlAliases localizedUrlAliases) {
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
