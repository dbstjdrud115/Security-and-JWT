package com.ll.chatApp.domain.article.article.controller;

import com.ll.chatApp.domain.article.article.dto.ArticleDto;
import com.ll.chatApp.domain.article.article.dto.ArticleModifyRequest;
import com.ll.chatApp.domain.article.article.dto.ArticleWriteRequest;
import com.ll.chatApp.domain.article.article.entity.Article;
import com.ll.chatApp.domain.article.article.service.ArticleService;
import com.ll.chatApp.global.rsData.RsData;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/articles")
@RequiredArgsConstructor
public class ApiV1ArticleController {
    private final ArticleService articleService;

    /*
    * 설계 한다면 어디서부터 출발해야 할까?
    *
    *
    * 일단 유저 엔티티.  userName, userId(@Id걸림)
    *
    * */
    @GetMapping
    public List<ArticleDto> getArticles() {
        List<Article> articles = articleService.findAll();
        List<ArticleDto> articleDtoList = articles.stream()
                .map(ArticleDto::new)
                .toList();

        return articleDtoList;
    }

    @GetMapping({"/{id}"})
    private ArticleDto getArticle(@PathVariable("id") Long id) {
        //orElseGet(Article::new) 가 뭐지?
        Article article = articleService.findById(id).orElseGet(Article::new);
        return new ArticleDto(article);
    }

    @PostMapping
    public RsData writeArticle(@Valid @RequestBody ArticleWriteRequest req) {
        Article article =  articleService.write(req.getTitle(), req.getContent());
        ArticleDto articleDto = new ArticleDto(article);

        return new RsData<>(
                "200",
                "게시글이 작성에 성공하였습니다.",
                new ArticleDto(article));
    }

    @PatchMapping({"/{id}"})
    public RsData<ArticleDto> updateArticle(@PathVariable("id") Long id, @Valid @RequestBody ArticleModifyRequest req) {
        Article article = this.articleService.findById(id).orElseGet(null);
        Article modifiedArticle = this.articleService.modify(article, req.getTitle(), req.getContent());
        return new RsData<>("200", "게시물 수정 성공", new ArticleDto((modifiedArticle)));
    }

    @DeleteMapping({"/{id}"})
    public RsData<Void> deleteArticle(@PathVariable("id") Long id) {
        this.articleService.delete(id);
        return new RsData<>("200"
                , "게시글 작성완료"
                , null);
    }
}
