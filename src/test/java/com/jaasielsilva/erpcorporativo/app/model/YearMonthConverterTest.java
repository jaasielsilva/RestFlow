package com.jaasielsilva.erpcorporativo.app.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.time.YearMonth;

import org.junit.jupiter.api.Test;

class YearMonthConverterTest {

    private final YearMonthConverter converter = new YearMonthConverter();

    @Test
    void convertToDatabaseColumn_shouldSerializeToYYYYMM() {
        assertEquals("2024-03", converter.convertToDatabaseColumn(YearMonth.of(2024, 3)));
    }

    @Test
    void convertToDatabaseColumn_shouldReturnNullForNull() {
        assertNull(converter.convertToDatabaseColumn(null));
    }

    @Test
    void convertToEntityAttribute_shouldDeserializeFromYYYYMM() {
        assertEquals(YearMonth.of(2024, 3), converter.convertToEntityAttribute("2024-03"));
    }

    @Test
    void convertToEntityAttribute_shouldReturnNullForNull() {
        assertNull(converter.convertToEntityAttribute(null));
    }

    @Test
    void convertToEntityAttribute_shouldReturnNullForBlank() {
        assertNull(converter.convertToEntityAttribute(""));
    }

    @Test
    void roundTrip_shouldPreserveValue() {
        YearMonth original = YearMonth.of(2025, 12);
        String serialized = converter.convertToDatabaseColumn(original);
        YearMonth deserialized = converter.convertToEntityAttribute(serialized);
        assertEquals(original, deserialized);
    }
}
