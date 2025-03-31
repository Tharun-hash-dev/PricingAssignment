package com.cmind.pricingservice.service;

import com.cmind.pricingservice.model.Article;
import com.cmind.pricingservice.model.Price;
import com.cmind.pricingservice.repository.PriceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class PricingService {

    @Autowired
    PriceRepository priceRepo;

    @Cacheable("prices")
    public List<Price> getPrices(String storeId, String articleId) {
        Article article = priceRepo.findArticleByArticleIdAndStoreId(articleId, storeId);
        if (article == null) {
            return null;
        }

        List<Price> allPrices = priceRepo.findByArticleIdAndStoreId(articleId, storeId);
        if (allPrices == null || allPrices.isEmpty()) {
            return null;
        }

        return processOverlappingPrices(allPrices);
    }

    private List<Price> processOverlappingPrices(List<Price> prices) {
        List<Price> result = new ArrayList<>();
        List<Price> processed = new ArrayList<>();

        for (Price currentPrice : prices) {
            if (processed.contains(currentPrice)) {
                continue;
            }

            List<Price> overlaps = findOverlappingPrices(currentPrice, prices);

            if (overlaps.isEmpty()) {
                result.add(currentPrice);
                processed.add(currentPrice);
            } else {
                overlaps.add(currentPrice);
                if (allAmountsSame(overlaps)) {
                    result.add(mergeOverlapping(overlaps));
                } else {
                    markOverlapped(overlaps, result);
                }
                processed.addAll(overlaps);
            }
        }
        return result;
    }

    private List<Price> findOverlappingPrices(Price current, List<Price> all) {
        List<Price> overlaps = new ArrayList<>();
        for (Price other : all) {
            if (!current.equals(other) && areOverlapping(current, other)) {
                overlaps.add(other);
            }
        }
        return overlaps;
    }

    private boolean areOverlapping(Price p1, Price p2) {
        return !p1.getFrom().isAfter(p2.getTo()) && !p2.getFrom().isAfter(p1.getTo());
    }

    private boolean allAmountsSame(List<Price> prices) {
        if (prices.isEmpty()) {
            return true;
        }
        return prices.stream().map(Price::getAmount).distinct().count() == 1;
    }

    private Price mergeOverlapping(List<Price> prices) {
        ZonedDateTime minFrom = prices.stream().map(Price::getFrom).min(ZonedDateTime::compareTo).orElse(null);
        ZonedDateTime maxTo = prices.stream().map(Price::getTo).max(ZonedDateTime::compareTo).orElse(null);

        Price first = prices.get(0);
        Price merged = new Price();
        merged.setAmount(first.getAmount());
        merged.setCurrency(first.getCurrency());
        merged.setType(first.getType());
        merged.setSubtype(first.getSubtype());
        merged.setFrom(minFrom);
        merged.setTo(maxTo);
        return merged;
    }

    private void markOverlapped(List<Price> prices, List<Price> result) {
        for (Price p : prices) {
            p.setOverlapped(true);
            result.add(p);
        }
    }
}
