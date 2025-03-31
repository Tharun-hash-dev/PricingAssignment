package com.cmind.pricingservice.repository;

import com.cmind.pricingservice.model.Article;
import com.cmind.pricingservice.model.Price;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PriceRepository extends JpaRepository<Price, Long> {
    List<Price> findByArticleIdAndStoreId(String articleId, String storeId);
    Article findArticleByArticleIdAndStoreId(String articleId, String storeId);
}
