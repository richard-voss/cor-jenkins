package de.fruiture.cor.jenkins;

import com.github.zafarkhaja.semver.Version;

public abstract class VersioningStrategy {
  public static boolean isRelease(Version v) {
    return v.getPreReleaseVersion().isEmpty();
  }

  public static boolean isSnapshot(Version v) {
    return !v.getPreReleaseVersion().isEmpty();
  }

  enum Change {
    PATCH,
    MINOR,
    MAJOR
  }

  protected Change change = Change.PATCH;

  abstract Version getNextVersion(Version lastRelease, Version lastSnapshot);

  void patchChange() {
    if (change.ordinal() < Change.PATCH.ordinal()) change = Change.PATCH;
  }

  void minorChange() {
    if (change.ordinal() < Change.MINOR.ordinal()) change = Change.MINOR;
  }

  void majorChange() {
    change = Change.MAJOR;
  }

  static class Release extends VersioningStrategy {
    @Override
    Version getNextVersion(Version lastRelease, Version lastSnapshot) {
      Version relMin;

      switch (change) {
        case MAJOR:
          relMin = lastRelease.incrementMajorVersion();
          break;
        case MINOR:
          relMin = lastRelease.incrementMinorVersion();
          break;
        case PATCH:
        default:
          relMin = lastRelease.incrementPatchVersion();
      }

      if (lastSnapshot != null) {
        Version snapRelease =
            new Version.Builder().setNormalVersion(lastSnapshot.getNormalVersion()).build();

        if (snapRelease.greaterThanOrEqualTo(relMin)) {
          return snapRelease;
        } else {
          return relMin;
        }
      } else {
        return relMin;
      }
    }
  }

  static class Snapshot extends VersioningStrategy {

    private static final String SNAPSHOT_START = "SNAPSHOT.0";

    @Override
    Version getNextVersion(Version lastRelease, Version lastSnapshot) {
      Version snapshotMin;
      switch (this.change) {
        case MAJOR:
          snapshotMin = lastRelease.incrementMajorVersion(SNAPSHOT_START);
          break;
        case MINOR:
          snapshotMin = lastRelease.incrementMinorVersion(SNAPSHOT_START);
          break;
        case PATCH:
        default:
          snapshotMin = lastRelease.incrementPatchVersion(SNAPSHOT_START);
          break;
      }

      if (lastSnapshot != null) {
        if (lastSnapshot.greaterThanOrEqualTo(snapshotMin)) {
          return lastSnapshot.incrementPreReleaseVersion();
        } else {
          return snapshotMin;
        }
      } else {
        return snapshotMin;
      }
    }
  }
}
