package de.fruiture.cor.jenkins;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import com.github.zafarkhaja.semver.Version;
import de.fruiture.cor.jenkins.VersioningStrategy;
import org.junit.jupiter.api.Test;

class SnapshotTest {

  private final Version rel = Version.valueOf("1.2.3");
  private final Version snap = Version.valueOf("1.2.4-SNAPSHOT.17");

  private VersioningStrategy.Snapshot strategy = new VersioningStrategy.Snapshot();

  @Test
  void regularContinuation() {
    strategy.patchChange();
    assertThat(strategy.getNextVersion(rel, null), is(Version.valueOf("1.2.4-SNAPSHOT.0")));
    assertThat(strategy.getNextVersion(rel, snap), is(Version.valueOf("1.2.4-SNAPSHOT.18")));
  }

  @Test
  void afterMinorChange() {
    strategy.minorChange();
    assertThat(strategy.getNextVersion(rel, null), is(Version.valueOf("1.3.0-SNAPSHOT.0")));
    assertThat(strategy.getNextVersion(rel, snap), is(Version.valueOf("1.3.0-SNAPSHOT.0")));
  }

  @Test
  void afterMajorChange() {
    strategy.majorChange();
    assertThat(strategy.getNextVersion(rel, null), is(Version.valueOf("2.0.0-SNAPSHOT.0")));
    assertThat(strategy.getNextVersion(rel, snap), is(Version.valueOf("2.0.0-SNAPSHOT.0")));
  }

  @Test
  void noDowngrade() {
    strategy.majorChange();
    strategy.minorChange();
    strategy.patchChange();
    assertThat(strategy.getNextVersion(rel, null), is(Version.valueOf("2.0.0-SNAPSHOT.0")));
    assertThat(strategy.getNextVersion(rel, snap), is(Version.valueOf("2.0.0-SNAPSHOT.0")));
  }

  @Test
  void detectSnapshotLevel() {
    strategy.patchChange();

    assertThat(
        strategy.getNextVersion(Version.valueOf("1.2.3"), Version.valueOf("1.2.4-SNAPSHOT.17")),
        is(Version.valueOf("1.2.4-SNAPSHOT.18")));

    assertThat(
        strategy.getNextVersion(Version.valueOf("1.2.3"), Version.valueOf("1.5.0-SNAPSHOT.29")),
        is(Version.valueOf("1.5.0-SNAPSHOT.30")));

    strategy.minorChange();

    assertThat(
        strategy.getNextVersion(Version.valueOf("1.2.3"), Version.valueOf("1.3.0-SNAPSHOT.3")),
        is(Version.valueOf("1.3.0-SNAPSHOT.4")));

    assertThat(
        strategy.getNextVersion(Version.valueOf("1.2.3"), Version.valueOf("1.5.0-SNAPSHOT.29")),
        is(Version.valueOf("1.5.0-SNAPSHOT.30")));

    strategy.majorChange();

    assertThat(
        strategy.getNextVersion(Version.valueOf("1.2.3"), Version.valueOf("2.0.0-SNAPSHOT.7")),
        is(Version.valueOf("2.0.0-SNAPSHOT.8")));

    assertThat(
        strategy.getNextVersion(Version.valueOf("1.2.3"), Version.valueOf("2.1.15-SNAPSHOT.2")),
        is(Version.valueOf("2.1.15-SNAPSHOT.3")));
  }

  @Test
  void bootstrap() {
    strategy.patchChange();

    assertThat(
        strategy.getNextVersion(Version.valueOf("0.0.0"), null),
        is(Version.valueOf("0.0.1-SNAPSHOT.0")));

    strategy.minorChange();

    assertThat(
        strategy.getNextVersion(Version.valueOf("0.0.0"), null),
        is(Version.valueOf("0.1.0-SNAPSHOT.0")));

    strategy.majorChange();

    assertThat(
        strategy.getNextVersion(Version.valueOf("0.0.0"), null),
        is(Version.valueOf("1.0.0-SNAPSHOT.0")));
  }
}
