package org.example.realworldapi.application.web.resource;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import org.example.realworldapi.application.web.model.response.TagsResponse;
import org.example.realworldapi.domain.model.tag.Tag;
import org.example.realworldapi.infrastructure.web.qualifiers.NoWrapRootValueObjectMapper;
import org.example.realworldapi.domain.service.TagService;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

@Path("/tags")
@AllArgsConstructor
public class TagsResource {

  @NoWrapRootValueObjectMapper ObjectMapper objectMapper;
  private final TagService tagService;

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public Response getTags() throws JsonProcessingException {
    List<Tag> tags = tagService.find();
    return Response.ok(objectMapper.writeValueAsString(new TagsResponse(tags)))
        .status(Response.Status.OK)
        .build();
  }
}
