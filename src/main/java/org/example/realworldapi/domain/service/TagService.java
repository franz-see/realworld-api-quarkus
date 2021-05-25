package org.example.realworldapi.domain.service;

import lombok.AllArgsConstructor;
import org.example.realworldapi.domain.model.article.Article;
import org.example.realworldapi.domain.model.article.TagRelationshipRepository;
import org.example.realworldapi.domain.model.tag.Tag;
import org.example.realworldapi.domain.model.tag.TagBuilder;
import org.example.realworldapi.domain.model.tag.TagRepository;

import javax.inject.Singleton;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

@Singleton
@AllArgsConstructor
public class TagService {

    private final TagRepository tagRepository;
    private final TagRelationshipRepository tagRelationshipRepository;
    private final TagBuilder tagBuilder;

    public List<Tag> findByNameCreateIfNotExists(java.util.List<String> names) {
        final var tags = tagRepository.findByNames(names);
        tags.addAll(createTags(nonexistent(tags, names)));
        return tags;
    }

    public Tag create(String name) {
        final var tag = tagBuilder.build(name);
        tagRepository.save(tag);
        return tag;
    }

    public List<Tag> findArticleTags(Article article) {
        return tagRelationshipRepository.findArticleTags(article);
    }

    private List<Tag> createTags(List<String> names) {
        final var tags = new LinkedList<Tag>();
        names.forEach(name -> tags.add(create(name)));
        return tags;
    }

    private List<String> nonexistent(List<Tag> existing, List<String> allNames) {
        return allNames.stream()
                .filter(name -> existing.stream().noneMatch(tag -> tag.getName().equalsIgnoreCase(name)))
                .collect(Collectors.toList());
    }

    public List<Tag> find() {
        return tagRepository.findAllTags();
    }
}
