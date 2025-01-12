package com.ll.chatApp.domain.article.article.service;

import com.ll.chatApp.domain.article.article.entity.Article;
import com.ll.chatApp.domain.article.articleComment.entity.ArticleComment;
import com.ll.chatApp.domain.article.articleComment.service.ArticleCommentService;
import com.ll.chatApp.domain.article.articleTag.entity.ArticleTag;
import com.ll.chatApp.domain.article.articleTag.service.ArticleTagService;
import com.ll.chatApp.domain.member.member.entity.Member;
import com.ll.chatApp.domain.member.member.service.MemberService;
import com.ll.chatApp.global.rsData.RsData;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class ArticleServiceTest {
    @Autowired
    private ArticleService articleService;

    @Autowired
    private ArticleTagService articleTagService;

    @Autowired
    private MemberService memberService;

    @Autowired
    private ArticleCommentService articleCommentService;

    @DisplayName("글 쓰기")
    @Test
    void t1() {
        RsData<Article> writeRs = articleService.write(1L, "제목", "내용"); // 1은 회원번호
        Article article = writeRs.getData();

        assertThat(article.getId()).isGreaterThan(0L);
    }

    @DisplayName("1번 글을 가져온다.")
    @Test
    void t2() {
        Article article = articleService.findById(1L).get();
        assertThat(article.getTitle()).isEqualTo("제목1");
    }

    @DisplayName("1번 글의 작성자의 username 은 user1 이다.")
    @Test
    void t3() {
        Article article = articleService.findById(1L).get();
        Member author = article.getAuthor();

        assertThat(author.getUsername()).isEqualTo("user1");
    }
    @DisplayName("1번 글의 제목을 수정한다.")
    @Test
    void t4() {
        Article article = articleService.findById(1L).get();
        System.out.println(article);
        articleService.modify(article, "수정된 제목", "수정된 내용");

        Article article_ = articleService.findById(1L).get();
        System.out.println(article);
        assertThat(article_.getTitle()).isEqualTo("수정된 제목");
    }

    @DisplayName("2번 글에 댓글들을 추가한다.")
    @Test
    @Rollback(false)
    void t5() {
        Member member1 = memberService.findById(1L).get();
        Article article2 = articleService.findById(2L).get();

        article2.addComment(member1, "댓글 입니다.");
    }

    @DisplayName("1번 글의 댓글들을 수정한다.")
    @Test
    void t6() {
        Article article = articleService.findById(2L).get();

        article.getComments().forEach(comment -> {
            articleService.modifyComment(comment, comment.getBody() + "!!");
        });
    }

    @DisplayName("1번 글의 댓글 중 마지막 것을 삭제한다.")
    @Test
    void t7() {
        Article article = articleService.findById(2L).get();

        ArticleComment lastComment = article.getComments().getLast();

        article.removeComment(lastComment);
    }

    @DisplayName("게시물 별 댓글 수 출력")
    @Test
    void t8() {
        List<Article> articles = articleService.findAll();
        articles.forEach(article -> {
            System.out.println("게시물 번호: " + article.getId());
            System.out.println("댓글 수: " + article.getComments().size());
        });
    }

    @DisplayName("1번 게시물의 태그(String)를 반환한다.")
    @Test
    void t9() {
        Article article1 = articleService.findById(1L).get();

        String tagsStr = article1.getTagsStr();

        assertThat(tagsStr).isEqualTo("#자바 #백엔드");
    }

    @DisplayName("1번 게시물 toString")
    @Test
    void t10() {
        Article article1 = articleService.findById(1L).get();

        System.out.println(article1);
    }


    /*
     * 11번 테스트부터는 JPA에서 제공하지 않는 API를 사용하고 있다.
     *
     * 예를들어 11번 테스트는 findByAuthorId라는, JpaRepository에서 제공하지 않는
     * API를 사용하고 있는데, 신기한건 그게 또 동작을 한다는것이다.
     *
     * 이 이유는, Spring Data JPA의 메서드 이름 기반 쿼리 생성 기능덕분에 가능하다.
     * 일단 기본형인 findBy를 베이스로 뒤에 나오는 AuthorId를 해석한다음,
     * 기존의 방식대로 쿼리를 자동으로 작성하고 실행한다.
     * 이 기능이 동작하려면 내가 찾으려는 Entity에 해당 필드값이 선언되어있어야 한다.,
     *
     * 그리고 Spring Data Jpa의 명명만으로도 한계가 발생한다.
     * 그 다음에 등장한것이  QueryDSL인데,  Impl. cumstom을 참고할것.bui
     *
     * */

    @DisplayName("1번 회원이 작성한 댓글들")
    @Test
    void t11() {
        List<ArticleComment> articleComments = articleCommentService.findByAuthorId(1L);

        assertThat(articleComments.size()).isGreaterThan(0);
    }

    @DisplayName("1번 회원이 작성한 태그들")
    @Test
    void t12() {
        List<ArticleTag> articleTags = articleTagService.findByAuthorId(1L);

        assertThat(articleTags.size()).isGreaterThan(0);
    }

    @DisplayName("아이디가 user1 인 회원이 작성한 태그들")
    @Test
    void t13() {
        List<ArticleTag> articleTags = articleTagService.findByAuthorUsername("user1");

        assertThat(articleTags.size()).isGreaterThan(0);
    }
}