package org.example.realworldapi.domain.service;

import lombok.AllArgsConstructor;
import org.example.realworldapi.domain.model.article.ArticleRepository;
import org.example.realworldapi.domain.model.provider.SlugProvider;

import javax.inject.Singleton;
import java.util.UUID;

@Singleton
@AllArgsConstructor
public class SlugService {

    private final ArticleRepository articleRepository;
    private final SlugProvider slugProvider;

    public String createByTitle(String title) {
        String slug = slugProvider.slugify(title);
        if (articleRepository.existsBySlug(slug)) {
            slug += UUID.randomUUID().toString();
        }
        return slug;
    }
}
