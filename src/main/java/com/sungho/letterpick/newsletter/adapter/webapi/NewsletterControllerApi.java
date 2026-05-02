package com.sungho.letterpick.newsletter.adapter.webapi;

import com.sungho.letterpick.newsletter.adapter.webapi.dto.NewsletterCategoriesResponse;
import com.sungho.letterpick.newsletter.adapter.webapi.dto.NewslettersResponse;
import com.sungho.letterpick.newsletter.application.provided.NewsletterSearchCondition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Pageable;

@Tag(name = "Newsletter", description = "뉴스레터 탐색 API")
public interface NewsletterControllerApi {

    @Operation(
            summary = "뉴스레터 카테고리 목록 조회",
            description = "뉴스레터 목록 화면의 카테고리 칩 필터에 사용할 카테고리 code/label 목록을 조회한다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공")
    })
    NewsletterCategoriesResponse getCategories();

    @Operation(
            summary = "뉴스레터 목록 조회",
            description = "비로그인 사용자를 포함해 letterPick에 등록된 뉴스레터 목록을 조회한다. category query parameter가 있으면 해당 카테고리로 필터링한다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "400", description = "요청 query parameter 형식 오류")
    })
    NewslettersResponse getNewslettersWithCategory(NewsletterSearchCondition searchCondition, Pageable pageable);
}
