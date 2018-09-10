package de.fruiture.cor.jenkins;

import static com.github.zafarkhaja.semver.Version.valueOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import com.github.zafarkhaja.semver.Version;
import org.junit.jupiter.api.Test;

class VersionLibraryTest {

  @Test
  void incrementSnapshotStrategy() {
    assertThat(
        valueOf("0.0.1-SNAPSHOT.0").incrementPreReleaseVersion(), is(valueOf("0.0.1-SNAPSHOT.1")));

    assertThat(
        valueOf("0.0.1-SNAPSHOT.0").incrementPatchVersion("SNAPSHOT.0"),
        is(valueOf("0.0.2-SNAPSHOT.0")));

    assertThat(
        valueOf("0.0.1-SNAPSHOT.0").incrementMinorVersion("SNAPSHOT.0"),
        is(valueOf("0.1.0-SNAPSHOT.0")));

    assertThat(
        valueOf("0.0.1-SNAPSHOT.0").incrementMajorVersion("SNAPSHOT.0"),
        is(valueOf("1.0.0-SNAPSHOT.0")));
  }

  @Test
  void incrementReleaseStrategy() {
    assertThat(valueOf("0.0.1").incrementPatchVersion(), is(valueOf("0.0.2")));
    assertThat(valueOf("0.0.1").incrementMinorVersion(), is(valueOf("0.1.0")));
    assertThat(valueOf("0.0.1").incrementMajorVersion(), is(valueOf("1.0.0")));

    assertThat(
        new Version.Builder(valueOf("0.0.1-SNAPSHOT.17").getNormalVersion()).build(),
        is(valueOf("0.0.1")));

    assertThat(valueOf("0.0.1-SNAPSHOT.17").incrementPatchVersion(), is(valueOf("0.0.2")));
    assertThat(valueOf("0.0.1-SNAPSHOT.17").incrementMinorVersion(), is(valueOf("0.1.0")));
    assertThat(valueOf("0.0.1-SNAPSHOT.17").incrementMajorVersion(), is(valueOf("1.0.0")));
  }
}
