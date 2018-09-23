package de.fruiture.cor.jenkins;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class VersionCalculator implements Serializable {

  public static final Version START = new Version("0.0.0");

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
    assert prefix == null || prefix.matches("^[\\w_\\-.]+$");
  }

  public VersionCalculator(VersioningStrategy strategy) {
    this(strategy, null);
  }

  void tags(String foundGitTags) {
    Pattern p =
        prefix != null
            ? Pattern.compile(Pattern.quote(prefix) + "(\\d+.\\d+\\.\\d+\\S*)")
            : Pattern.compile("(\\d+.\\d+\\.\\d+\\S*)");

    Matcher matcher = p.matcher(foundGitTags);
    while (matcher.find()) {
      String v = matcher.group(1);
      try {
        versions.add(new Version(v));
      } catch (Exception ignored) {
        // ignore
      }
    }
  }

  private Optional<Version> lastRelease() {
    return versions.stream().filter(Version::isRelease).reduce(VersionCalculator::last);
  }

  private Version getBaseline() {
    return lastRelease().orElse(START);
  }

  private Optional<Version> lastSnapAfter(Version baseline) {
    return versions
        .stream()
        .filter(Version::isSnapshot)
        .filter(v -> v.greaterThan(baseline))
        .reduce(VersionCalculator::last);
  }

  public Optional<String> getReferenceTag() {
    Optional<Version> best = lastRelease().map(r -> lastSnapAfter(r).orElse(r));

    if (!best.isPresent()) {
      best = lastSnapAfter(START);
    }

    return best.map(Version::toString).map(this::prefixed);
  }

  public String getNextVersion() {
    Version baseline = getBaseline();
    return strategy.getNextVersion(baseline, lastSnapAfter(baseline).orElse(null)).toString();
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
    return prefix != null ? prefix + v : v;
  }

  public String getGitFindTagsCommand() {
    return prefix != null
        ? ("tag -l --merged HEAD '" + prefixed("*") + "'")
        : "tag -l --merged HEAD";
  }

  public String getGitLogCommand() {
    return "log --pretty=oneline " + getGitLogVersionRange();
  }

  public String getGitLogVersionRange() {
    return getReferenceTag().map(from -> from + "..HEAD").orElse("HEAD");
  }

  public String getGitNextTagCommand() {
    return "tag " + getNextVersionTag();
  }

  public String getNextVersionTag() {
    return prefixed(getNextVersion());
  }

  public List<Pattern> getTriggerMinorChange() {
    return triggerMinorChange;
  }

  public void setTriggerMinorChange(List<?> triggerMinorChange) {
    this.triggerMinorChange = patterns(triggerMinorChange);
  }

  private static List<Pattern> patterns(List<?> rawList) {
    return rawList
        .stream()
        .map(
            o -> {
              if (o instanceof Pattern) {
                return (Pattern) o;
              } else {
                return Pattern.compile(o.toString());
              }
            })
        .collect(Collectors.toList());
  }

  public List<Pattern> getTriggerMajorChange() {
    return triggerMajorChange;
  }

  public void setTriggerMajorChange(List<?> triggerMajorChange) {
    this.triggerMajorChange = patterns(triggerMajorChange);
  }

  private static <T> T last(T a, T b) {
    return b;
  }
}
