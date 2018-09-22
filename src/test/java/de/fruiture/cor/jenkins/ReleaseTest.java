package de.fruiture.cor.jenkins;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import org.junit.jupiter.api.Test;

class ReleaseTest {

  private final Version rel = new Version("1.2.3");
  private final Version snap = new Version("1.2.4-SNAPSHOT.17");

  private VersioningStrategy.Release strategy = new VersioningStrategy.Release();

  @Test
  void regularContinuation() {
    assertThat(strategy.getNextVersion(rel, null), is(new Version("1.2.4")));
    assertThat(strategy.getNextVersion(rel, snap), is(new Version("1.2.4")));
  };

  @Test
  void afterMinorChange() {
    strategy.minorChange();
    assertThat(strategy.getNextVersion(rel, null), is(new Version("1.3.0")));
    assertThat(strategy.getNextVersion(rel, snap), is(new Version("1.3.0")));
  }

  @Test
  void afterMajorChange() {
    strategy.majorChange();
    assertThat(strategy.getNextVersion(rel, null), is(new Version("2.0.0")));
    assertThat(strategy.getNextVersion(rel, snap), is(new Version("2.0.0")));
  }

  @Test
  void noDowngrade() {
    strategy.majorChange();
    strategy.minorChange();
    assertThat(strategy.getNextVersion(rel, null), is(new Version("2.0.0")));
    assertThat(strategy.getNextVersion(rel, snap), is(new Version("2.0.0")));
  }

  @Test
  void detectSnapshotLevel() {
    strategy.patchChange();

    assertThat(
        strategy.getNextVersion(new Version("1.2.3"), new Version("1.2.4-SNAPSHOT.17")),
        is(new Version("1.2.4")));

    assertThat(
        strategy.getNextVersion(new Version("1.2.3"), new Version("1.5.0-SNAPSHOT.29")),
        is(new Version("1.5.0")));

    strategy.minorChange();

    assertThat(
        strategy.getNextVersion(new Version("1.2.3"), new Version("1.2.4-SNAPSHOT.17")),
        is(new Version("1.3.0")));

    assertThat(
        strategy.getNextVersion(new Version("1.2.3"), new Version("1.3.3-SNAPSHOT.3")),
        is(new Version("1.3.3")));

    strategy.majorChange();

    assertThat(
        strategy.getNextVersion(new Version("1.2.3"), new Version("2.0.0-SNAPSHOT.4")),
        is(new Version("2.0.0")));

    assertThat(
        strategy.getNextVersion(new Version("1.2.3"), new Version("2.2.4-SNAPSHOT.4")),
        is(new Version("2.2.4")));

    assertThat(
        strategy.getNextVersion(new Version("1.2.3"), new Version("3.0.1-SNAPSHOT.4")),
        is(new Version("3.0.1")));
  }

  @Test
  void bootstrap() {
    strategy.patchChange();

    assertThat(strategy.getNextVersion(new Version("0.0.0"), null), is(new Version("0.0.1")));

    strategy.minorChange();

    assertThat(strategy.getNextVersion(new Version("0.0.0"), null), is(new Version("0.1.0")));

    strategy.majorChange();

    assertThat(strategy.getNextVersion(new Version("0.0.0"), null), is(new Version("1.0.0")));
  }
}
