package org.example.realworldapi.domain.service;

import lombok.AllArgsConstructor;
import org.example.realworldapi.domain.model.article.Article;
import org.example.realworldapi.domain.model.article.TagRelationship;
import org.example.realworldapi.domain.model.article.TagRelationshipRepository;
import org.example.realworldapi.domain.model.tag.Tag;

import javax.inject.Singleton;
import java.util.List;

@Singleton
@AllArgsConstructor
public class TagRelationshipService {

    private final TagRelationshipRepository tagRelationshipRepository;

    public void createTagRelationship(Article article, List<Tag> tags) {
        tags.forEach(tag -> tagRelationshipRepository.save(new TagRelationship(article, tag)));
    }

    public void save(Article article, Tag tag) {
        tagRelationshipRepository.save(new TagRelationship(article, tag));
    }
}

