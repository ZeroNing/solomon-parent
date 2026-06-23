package com.steven.solomon.lambda;

import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.List;
import org.junit.jupiter.api.Test;

class LambdaEmptyFilterTest {

  @Test
  void aggregatesReturnNullWhenFilterMatchesNothing() {
    List<Integer> values = List.of(1, 2, 3);

    assertNull(Lambda.max(values, value -> value > 10, Integer::intValue));
    assertNull(Lambda.max(values, value -> value > 10, Integer::longValue));
    assertNull(Lambda.max(values, value -> value > 10, Integer::doubleValue));
    assertNull(Lambda.min(values, value -> value > 10, Integer::intValue));
    assertNull(Lambda.min(values, value -> value > 10, Integer::longValue));
    assertNull(Lambda.min(values, value -> value > 10, Integer::doubleValue));
    assertNull(Lambda.average(values, value -> value > 10, Integer::intValue));
    assertNull(Lambda.average(values, value -> value > 10, Integer::longValue));
    assertNull(Lambda.average(values, value -> value > 10, Integer::doubleValue));
  }
}
