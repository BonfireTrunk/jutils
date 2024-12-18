package today.bonfire.oss.jutils.parallel;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ContextTest {

  @Test
  void testContextCreation() {
    String testData = "test";
    Context<String> context = Context.<String>builder()
                                     .data(testData)
                                     .name("testContext")
                                     .startTime(123L)
                                     .build();

    assertThat(context.data()).isEqualTo(testData);
    assertThat(context.name()).isEqualTo("testContext");
    assertThat(context.startTime()).isEqualTo(123L);
  }

  @Test
  void testContextOf() {
    String          testData = "test";
    Context<String> context  = Context.of(testData);

    assertThat(context.data()).isEqualTo(testData);
    assertThat(context.name()).isNull();
    assertThat(context.startTime()).isGreaterThan(0);
  }

  @Test
  void testEmptyContext() {
    Context<String> context = Context.empty();

    assertThat(context.data()).isNull();
    assertThat(context.name()).isNull();
    assertThat(context.startTime()).isGreaterThan(0);
    assertThat(context.hasData()).isFalse();
  }

  @Test
  void testHasData() {
    Context<String> withData    = Context.of("test");
    Context<String> withoutData = Context.empty();

    assertThat(withData.hasData()).isTrue();
    assertThat(withoutData.hasData()).isFalse();
  }

  @Test
  void testMerge() {
    Context<String> context1 = Context.<String>builder()
                                      .data("data1")
                                      .name("name1")
                                      .startTime(100L)
                                      .build();

    Context<String> context2 = Context.<String>builder()
                                      .data("data2")
                                      .name("name2")
                                      .startTime(50L)
                                      .build();

    Context<String> merged = context1.merge(context2);
    assertThat(merged.data()).isEqualTo("data2");
    assertThat(merged.name()).isEqualTo("name2");
    assertThat(merged.startTime()).isEqualTo(50L);
  }

  @Test
  void testMergeWithNull() {
    Context<String> context = Context.<String>builder()
                                     .data("data")
                                     .name("name")
                                     .startTime(100L)
                                     .build();

    Context<String> merged = context.merge(null);
    assertThat(merged).isEqualTo(context);
  }

  @Test
  void testMergeWithPartialData() {
    Context<String> context1 = Context.<String>builder()
                                      .data("data1")
                                      .name("name1")
                                      .startTime(100L)
                                      .build();

    Context<String> context2 = Context.<String>builder()
                                      .startTime(50L)
                                      .build();

    Context<String> merged = context1.merge(context2);
    assertThat(merged.data()).isEqualTo("data1");
    assertThat(merged.name()).isEqualTo("name1");
    assertThat(merged.startTime()).isEqualTo(50L);
  }
}
