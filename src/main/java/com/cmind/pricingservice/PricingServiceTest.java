package com.cmind.pricingservice;

import com.cmind.pricingservice.model.Price;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class PricingServiceTest {

    @Autowired
    private PricingService pricingService;

    private Price createPrice(String type, String subtype, double amount, String validFrom, String validTo) {
        Price price = new Price();
        price.setType(type);
        price.setSubtype(subtype);
        price.setCurrency("CAD");
        price.setAmount(BigDecimal.valueOf(amount));
        price.setFrom(ZonedDateTime.parse(validFrom));
        price.setTo(ZonedDateTime.parse(validTo));
        return price;
    }

    @Test
    public void testNonOverlappingPrices() {
        Price price1 = createPrice("retail", "regular", 30.0, "2023-01-01T00:00:00Z", "2023-01-31T23:59:59Z");
        Price price2 = createPrice("retail", "discounted", 25.0, "2023-02-01T00:00:00Z", "2023-02-28T23:59:59Z");

        List<Price> result = pricingService.handleOverlappingPrices(Arrays.asList(price1, price2));

        assertEquals(2, result.size());
        assertFalse(result.get(0).isOverlapped());
        assertFalse(result.get(1).isOverlapped());
    }

    @Test
    public void testExactOverlappingPricesWithSameAmount() {
        Price price1 = createPrice("retail", "regular", 30.0, "2023-01-01T00:00:00Z", "2023-01-31T23:59:59Z");
        Price price2 = createPrice("retail", "regular", 30.0, "2023-01-15T00:00:00Z", "2023-02-15T23:59:59Z");

        List<Price> result = pricingService.handleOverlappingPrices(Arrays.asList(price1, price2));

        assertEquals(1, result.size());
        assertEquals("2023-01-01T00:00:00Z", result.get(0).getFrom().toString());
        assertEquals("2023-02-15T23:59:59Z", result.get(0).getTo().toString());
        assertFalse(result.get(0).isOverlapped());
    }

    @Test
    public void testOverlappingPricesWithDifferentAmounts() {
        Price price1 = createPrice("retail", "regular", 30.0, "2023-01-01T00:00:00Z", "2023-01-31T23:59:59Z");
        Price price2 = createPrice("retail", "discounted", 25.0, "2023-01-15T00:00:00Z", "2023-02-15T23:59:59Z");

        List<Price> result = pricingService.handleOverlappingPrices(Arrays.asList(price1, price2));

        assertEquals(2, result.size());
        assertTrue(result.get(0).isOverlapped());
        assertTrue(result.get(1).isOverlapped());
    }

    @Test
    public void testContinuousOverlappingPricesWithSameAmount() {
        Price price1 = createPrice("retail", "regular", 30.0, "2023-01-01T00:00:00Z", "2023-01-31T23:59:59Z");
        Price price2 = createPrice("retail", "regular", 30.0, "2023-02-01T00:00:00Z", "2023-02-28T23:59:59Z");

        List<Price> result = pricingService.handleOverlappingPrices(Arrays.asList(price1, price2));

        assertEquals(2, result.size());
        assertEquals("2023-01-01T00:00:00Z", result.get(0).getFrom().toString());
        assertEquals("2023-01-31T00:00:00Z", result.get(0).getTo().toString());
        assertFalse(result.get(0).isOverlapped());
    }

    @Test
    public void testAlexScenario() {
        Price price1 = createPrice("retail", "regular", 30.0, "2023-01-01T00:00:00Z", "2023-01-31T23:59:59Z");
        Price price2 = createPrice("retail", "regular", 29.0, "2023-01-15T00:00:00Z", "2023-02-15T23:59:59Z");
        Price price3 = createPrice("retail", "regular", 29.0, "2023-01-16T00:00:00Z", "2023-03-15T23:59:59Z");
        Price price4 = createPrice("retail", "regular", 29.0, "2023-01-14T00:00:00Z", "2023-03-17T23:59:59Z");
        Price price5 = createPrice("retail", "regular", 30.0, "2023-01-02T00:00:00Z", "2023-03-30T23:59:59Z");

        List<Price> result = pricingService.handleOverlappingPrices(Arrays.asList(price1, price2, price3, price4, price5));

        assertEquals(2, result.size());
        assertEquals("2023-01-01T00:00:00Z", result.get(0).getFrom().toString());
        assertEquals("2023-03-30T23:59:59Z", result.get(0).getTo().toString());
        assertTrue(result.get(0).isOverlapped());
        assertEquals("2020-01-14T00:00:00Z", result.get(1).getFrom().toString());
        assertEquals("2023-03-17T23:59:59Z", result.get(1).getTo().toString());
        assertTrue(result.get(1).isOverlapped());
    }
}
