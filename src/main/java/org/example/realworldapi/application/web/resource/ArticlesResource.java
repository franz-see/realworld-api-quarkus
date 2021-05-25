package org.example.realworldapi.application.web.resource;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import org.example.realworldapi.application.web.model.request.NewArticleRequest;
import org.example.realworldapi.application.web.model.request.NewCommentRequest;
import org.example.realworldapi.application.web.model.request.UpdateArticleRequest;
import org.example.realworldapi.application.web.resource.utils.ResourceUtils;
import org.example.realworldapi.domain.model.article.ArticleFilter;
import org.example.realworldapi.domain.model.comment.DeleteCommentInput;
import org.example.realworldapi.domain.model.constants.ValidationMessages;
import org.example.realworldapi.infrastructure.web.qualifiers.NoWrapRootValueObjectMapper;
import org.example.realworldapi.infrastructure.web.security.annotation.Secured;
import org.example.realworldapi.infrastructure.web.security.profile.Role;
import org.example.realworldapi.domain.service.ArticlesService;
import org.example.realworldapi.domain.service.CommentService;

import javax.transaction.Transactional;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import java.util.List;
import java.util.UUID;

@Path("/articles")
@AllArgsConstructor
public class ArticlesResource {

  @NoWrapRootValueObjectMapper ObjectMapper objectMapper;
  private final ResourceUtils resourceUtils;

  private final ArticlesService articlesService;
  private final CommentService commentService;

  @GET
  @Path("/feed")
  @Secured({Role.USER, Role.ADMIN})
  @Produces(MediaType.APPLICATION_JSON)
  public Response feed(
      @QueryParam("offset") int offset,
      @QueryParam("limit") int limit,
      @Context SecurityContext securityContext)
      throws JsonProcessingException {
    final var loggedUserId = resourceUtils.getLoggedUserId(securityContext);
    final var articlesFilter =
        new ArticleFilter(offset, resourceUtils.getLimit(limit), loggedUserId, null, null, null);
    final var articlesPageResult = articlesService.findMostRecentByFilter(articlesFilter);
    return Response.ok(
            objectMapper.writeValueAsString(
                resourceUtils.articlesResponse(articlesPageResult, loggedUserId)))
        .status(Response.Status.OK)
        .build();
  }

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @Secured(optional = true)
  public Response getArticles(
      @QueryParam("offset") int offset,
      @QueryParam("limit") int limit,
      @QueryParam("tag") List<String> tags,
      @QueryParam("author") List<String> authors,
      @QueryParam("favorited") List<String> favorited,
      @Context SecurityContext securityContext)
      throws JsonProcessingException {
    final var loggedUserId = resourceUtils.getLoggedUserId(securityContext);
    final var filter =
        new ArticleFilter(
            offset, resourceUtils.getLimit(limit), loggedUserId, tags, authors, favorited);
    final var articlesPageResult = articlesService.findByFilter(filter);
    return Response.ok(
            objectMapper.writeValueAsString(
                resourceUtils.articlesResponse(articlesPageResult, loggedUserId)))
        .status(Response.Status.OK)
        .build();
  }

  @POST
  @Transactional
  @Secured({Role.ADMIN, Role.USER})
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public Response create(
      @Valid @NotNull(message = ValidationMessages.REQUEST_BODY_MUST_BE_NOT_NULL)
          NewArticleRequest newArticleRequest,
      @Context SecurityContext securityContext) {
    final var loggedUserId = resourceUtils.getLoggedUserId(securityContext);
    final var article = articlesService.create(newArticleRequest.toNewArticleInput(loggedUserId));
    return Response.ok(resourceUtils.articleResponse(article, loggedUserId))
        .status(Response.Status.CREATED)
        .build();
  }

  @GET
  @Path("/{slug}")
  @Produces(MediaType.APPLICATION_JSON)
  public Response findBySlug(
      @PathParam("slug") @NotBlank(message = ValidationMessages.SLUG_MUST_BE_NOT_BLANK)
          String slug) {
    final var article = articlesService.findBySlug(slug);
    return Response.ok(resourceUtils.articleResponse(article, null))
        .status(Response.Status.OK)
        .build();
  }

  @PUT
  @Transactional
  @Path("/{slug}")
  @Secured({Role.ADMIN, Role.USER})
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public Response update(
      @PathParam("slug") @NotBlank String slug,
      @Valid @NotNull UpdateArticleRequest updateArticleRequest,
      @Context SecurityContext securityContext) {
    final var loggedUserId = resourceUtils.getLoggedUserId(securityContext);
    final var updatedArticle =
        articlesService.updateBySlug(updateArticleRequest.toUpdateArticleInput(loggedUserId, slug));
    return Response.ok(resourceUtils.articleResponse(updatedArticle, null))
        .status(Response.Status.OK)
        .build();
  }

  @DELETE
  @Transactional
  @Path("/{slug}")
  @Secured({Role.ADMIN, Role.USER})
  @Produces(MediaType.APPLICATION_JSON)
  public Response delete(
      @PathParam("slug") @NotBlank(message = ValidationMessages.SLUG_MUST_BE_NOT_BLANK) String slug,
      @Context SecurityContext securityContext) {
    final var loggedUserId = resourceUtils.getLoggedUserId(securityContext);
    articlesService.deleteBySlug(loggedUserId, slug);
    return Response.ok().build();
  }

  @GET
  @Path("/{slug}/comments")
  @Secured(optional = true)
  @Produces(MediaType.APPLICATION_JSON)
  public Response getCommentsBySlug(
      @PathParam("slug") @NotBlank(message = ValidationMessages.SLUG_MUST_BE_NOT_BLANK) String slug,
      @Context SecurityContext securityContext)
      throws JsonProcessingException {
    final var loggedUserId = resourceUtils.getLoggedUserId(securityContext);
    final var comments = commentService.findByArticleSlug(slug);
    return Response.ok(
            objectMapper.writeValueAsString(resourceUtils.commentsResponse(comments, loggedUserId)))
        .status(Response.Status.OK)
        .build();
  }

  @POST
  @Transactional
  @Path("/{slug}/comments")
  @Secured({Role.ADMIN, Role.USER})
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public Response createComment(
      @PathParam("slug") @NotBlank(message = ValidationMessages.SLUG_MUST_BE_NOT_BLANK) String slug,
      @Valid NewCommentRequest newCommentRequest,
      @Context SecurityContext securityContext) {
    final var loggedUserId = resourceUtils.getLoggedUserId(securityContext);
    final var comment =
            commentService.create(newCommentRequest.toNewCommentInput(loggedUserId, slug));
    return Response.ok(resourceUtils.commentResponse(comment, loggedUserId))
        .status(Response.Status.OK)
        .build();
  }

  @DELETE
  @Transactional
  @Path("/{slug}/comments/{id}")
  @Secured({Role.ADMIN, Role.USER})
  @Produces(MediaType.APPLICATION_JSON)
  public Response deleteComment(
      @PathParam("slug") @NotBlank(message = ValidationMessages.SLUG_MUST_BE_NOT_BLANK) String slug,
      @PathParam("id") @NotNull(message = ValidationMessages.COMMENT_ID_MUST_BE_NOT_NULL) UUID id,
      @Context SecurityContext securityContext) {
    final var loggedUserId = resourceUtils.getLoggedUserId(securityContext);
    commentService.delete(new DeleteCommentInput(id, loggedUserId, slug));
    return Response.ok().build();
  }

  @POST
  @Transactional
  @Path("/{slug}/favorite")
  @Secured({Role.ADMIN, Role.USER})
  @Produces(MediaType.APPLICATION_JSON)
  public Response favoriteArticle(
      @PathParam("slug") @NotBlank(message = ValidationMessages.SLUG_MUST_BE_NOT_BLANK) String slug,
      @Context SecurityContext securityContext) {
    final var loggedUserId = resourceUtils.getLoggedUserId(securityContext);
    articlesService.favorite(slug, loggedUserId);
    final var article = articlesService.findBySlug(slug);
    return Response.ok(resourceUtils.articleResponse(article, loggedUserId))
        .status(Response.Status.OK)
        .build();
  }

  @DELETE
  @Transactional
  @Path("/{slug}/favorite")
  @Secured({Role.ADMIN, Role.USER})
  @Produces(MediaType.APPLICATION_JSON)
  public Response unfavoriteArticle(
      @PathParam("slug") @NotBlank(message = ValidationMessages.SLUG_MUST_BE_NOT_BLANK) String slug,
      @Context SecurityContext securityContext) {
    final var loggedUserId = resourceUtils.getLoggedUserId(securityContext);
    articlesService.unfavorite(slug, loggedUserId);
    final var article = articlesService.findBySlug(slug);
    return Response.ok(resourceUtils.articleResponse(article, loggedUserId))
        .status(Response.Status.OK)
        .build();
  }
}
