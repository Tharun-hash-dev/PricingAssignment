package com.cmind.pricingservice.controller;

import com.cmind.pricingservice.model.Article;
import com.cmind.pricingservice.model.Price;
import com.cmind.pricingservice.repository.PriceRepository;
import com.cmind.pricingservice.service.PricingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/pricing/v1/prices")
public class PricingController {

    private final Logger log = LoggerFactory.getLogger(PricingController.class);
    @Autowired
    PricingService priceService;
    @Autowired
    PriceRepository priceRepo;

    @Operation(summary = "Get prices")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Prices found", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = Map.class))}),
            @ApiResponse(responseCode = "404", description = "Prices not found", content = @Content)
    })
    @GetMapping("/{storeId}/{articleId}")
    public ResponseEntity<?> getPrices(
            @PathVariable String storeId,
            @PathVariable String articleId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "3") int size) {

        log.info("Request: store={}, article={}, page={}, size={}", storeId, articleId, page, size);
        List<Price> prices = priceService.getPrices(storeId, articleId, page, size);

        if (prices == null) {
            log.warn("No prices found");
            Map<String, Object> error = new HashMap<>();
            error.put("type", "Not_Found");
            error.put("title", "Unavailable prices");
            error.put("status", 404);
            error.put("detail", "No prices were found");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }

        Article article = priceRepo.findArticleByArticleIdAndStoreId(articleId, storeId);
        Map<String, Object> result = new HashMap<>();
        result.put("generated_date", ZonedDateTime.now());
        result.put("article", articleId);
        result.put("store", storeId);
        Map<String, Integer> meta = new HashMap<>();
        meta.put("page", page);
        meta.put("size", size);
        result.put("meta", meta);
        Map<String, String> props = new HashMap<>();
        props.put("uom", article.getUom());
        props.put("description", article.getDescription());
        props.put("brand", article.getBrand());
        props.put("model", article.getModel());
        result.put("properties", props);
        result.put("prices", prices);
        return ResponseEntity.ok(result);
    }
}
