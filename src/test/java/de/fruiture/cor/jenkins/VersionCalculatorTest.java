package de.fruiture.cor.jenkins;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class VersionCalculatorTest {

  @Test
  void gitCommands() {

    VersionCalculator vc = VersionCalculator.snapshot("rel-");

    assertThat(vc.getGitFindTagsCommand(), is("tag -l --merged HEAD 'rel-*'"));
    vc.tags("");
    assertThat(vc.getGitLogCommand(), is("log --pretty=oneline HEAD"));
    vc.messages("@CHANGE:MINOR bla");
    assertThat(vc.getNextVersion(), is("0.1.0-SNAPSHOT.0"));
    assertThat(vc.getGitNextTagCommand(), is("tag rel-0.1.0-SNAPSHOT.0"));

    vc = VersionCalculator.snapshot("rel-");

    assertThat(vc.getGitFindTagsCommand(), is("tag -l --merged HEAD 'rel-*'"));
    vc.tags("rel-0.1.0-SNAPSHOT.0");
    assertThat(vc.getGitLogCommand(), is("log --pretty=oneline rel-0.1.0-SNAPSHOT.0..HEAD"));
    vc.messages("@CHANGE:PATCH bla");
    assertThat(vc.getNextVersion(), is("0.1.0-SNAPSHOT.1"));
    assertThat(vc.getGitNextTagCommand(), is("tag rel-0.1.0-SNAPSHOT.1"));

    vc = VersionCalculator.release("rel-");

    assertThat(vc.getGitFindTagsCommand(), is("tag -l --merged HEAD 'rel-*'"));
    vc.tags("rel-0.1.0-SNAPSHOT.0 rel-0.1.0-SNAPSHOT.1");
    assertThat(vc.getGitLogCommand(), is("log --pretty=oneline rel-0.1.0-SNAPSHOT.1..HEAD"));
    vc.messages("@CHANGE:PATCH bla");
    assertThat(vc.getNextVersion(), is("0.1.0"));
    assertThat(vc.getGitNextTagCommand(), is("tag rel-0.1.0"));
  }

  @Test
  void gitCommandsWithoutPrefix() {

    VersionCalculator c1 = VersionCalculator.snapshot();

    assertThat(c1.getGitFindTagsCommand(), is("tag -l --merged HEAD"));
    c1.tags("");
    assertThat(c1.getGitLogCommand(), is("log --pretty=oneline HEAD"));
    c1.messages("@CHANGE:MINOR bla");
    assertThat(c1.getGitNextTagCommand(), is("tag 0.1.0-SNAPSHOT.0"));
  }

  static class ChangeDetectionTest {
    private final VersionCalculator detector = VersionCalculator.snapshot();

    @BeforeEach
    void setUp() {
      detector.tags("1.2.3 1.2.4-SNAPSHOT.0");
      assertThat(detector.getReferenceTag().get(), is("1.2.4-SNAPSHOT.0"));
    }

    @Test
    void simpleStory() {
      detector.messages("just bla bla");
      assertThat(detector.getNextVersion(), is("1.2.4-SNAPSHOT.1"));
    }

    @Test
    void detectPatchLevelChanges() {
      detector.messages("just CHANGE:PATCH bla bla");
      assertThat(detector.getNextVersion(), is("1.2.4-SNAPSHOT.1"));
    }

    @Test
    void detectMinorLevelChanges() {
      detector.messages("just CHANGE:MINOR bla bla");
      assertThat(detector.getNextVersion(), is("1.3.0-SNAPSHOT.0"));
    }

    @Test
    void detectMajorLevelChanges() {
      detector.messages("just CHANGE:MAJOR  bla @CHANGE:PATCH bla");
      assertThat(detector.getNextVersion(), is("2.0.0-SNAPSHOT.0"));
    }
  }
}
