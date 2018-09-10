package de.fruiture.cor.jenkins;

import com.github.zafarkhaja.semver.Version;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class VersionCalculator {

  public static VersionCalculator snapshot(String prefix) {
    return new VersionCalculator(new VersioningStrategy.Snapshot(), prefix);
  }

  public static VersionCalculator snapshot() {
    return new VersionCalculator(new VersioningStrategy.Snapshot());
  }

  public static VersionCalculator release(String prefix) {
    return new VersionCalculator(new VersioningStrategy.Release(), prefix);
  }

  public static VersionCalculator release() {
    return new VersionCalculator(new VersioningStrategy.Release());
  }

  private final VersioningStrategy strategy;
  private final String prefix;

  private final SortedSet<Version> versions = new TreeSet<>();

  private List<Pattern> triggerMinorChange =
      Collections.singletonList(Pattern.compile("CHANGE:MINOR"));
  private List<Pattern> triggerMajorChange =
      Collections.singletonList(Pattern.compile("CHANGE:MAJOR"));

  public VersionCalculator(VersioningStrategy strategy, String prefix) {
    this.prefix = prefix;
    this.strategy = strategy;
    if (prefix != null) {
      assert prefix.matches("^\\w+$");
    }
  }

  public VersionCalculator(VersioningStrategy strategy) {
    this(strategy, null);
  }

  void tags(String foundGitTags) {
    Pattern p =
        prefix != null
            ? Pattern.compile(prefix + "-(\\d+.\\d+\\.\\d+\\S*)")
            : Pattern.compile("(\\d+.\\d+\\.\\d+\\S*)");

    Matcher matcher = p.matcher(foundGitTags);
    while (matcher.find()) {
      String v = matcher.group(1);
      try {
        versions.add(Version.valueOf(v));
      } catch (Exception ignored) {
        // ignore
      }
    }
  }

  private Optional<Version> lastRelease() {
    return versions.stream().filter(VersioningStrategy::isRelease).reduce((a, b) -> b);
  }

  private Version getBaseline() {
    return lastRelease().orElse(Version.forIntegers(0));
  }

  private Optional<Version> lastSnap(Version baseline) {
    return versions
        .stream()
        .filter(VersioningStrategy::isSnapshot)
        .filter(v -> v.greaterThan(baseline))
        .reduce((a, b) -> b);
  }

  public String getNextVersion() {
    Version baseline = getBaseline();
    return strategy.getNextVersion(baseline, lastSnap(baseline).orElse(null)).toString();
  }

  public Optional<String> getReferenceTag() {
    return lastRelease().map(r -> lastSnap(r).orElse(r)).map(Version::toString).map(this::prefixed);
  }

  public void messages(String messages) {
    if (!messages.trim().isEmpty()) {
      strategy.patchChange();
    }
    if (triggerMinorChange.stream().anyMatch(p -> p.matcher(messages).find())) {
      strategy.minorChange();
    }
    if (triggerMajorChange.stream().anyMatch(p -> p.matcher(messages).find())) {
      strategy.majorChange();
    }
  }

  private String prefixed(String v) {
    return prefix != null ? prefix + "-" + v : v;
  }

  public String getGitFindTagsCommand() {
    return prefix != null ? "tag --merged -l '" + prefix + "-*'" : "tag --merged";
  }

  public String getGitLogCommand() {
    return "log --pretty=oneline " + getReferenceTag().map(from -> from + "..HEAD").orElse("HEAD");
  }

  public String getGitNextTagCommand() {
    return "tag " + prefixed(getNextVersion());
  }

  public List<Pattern> getTriggerMinorChange() {
    return triggerMinorChange;
  }

  public void setTriggerMinorChange(List<Pattern> triggerMinorChange) {
    this.triggerMinorChange = triggerMinorChange;
  }

  public List<Pattern> getTriggerMajorChange() {
    return triggerMajorChange;
  }

  public void setTriggerMajorChange(List<Pattern> triggerMajorChange) {
    this.triggerMajorChange = triggerMajorChange;
  }
}
