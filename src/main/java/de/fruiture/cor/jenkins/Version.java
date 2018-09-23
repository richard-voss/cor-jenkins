package de.fruiture.cor.jenkins;

import com.github.zafarkhaja.semver.expr.Expression;
import java.io.Serializable;
import java.util.Objects;

/** Serializable facade around {@link com.github.zafarkhaja.semver.Version} */
public class Version implements Comparable<Version>, Serializable {

  private final String text;
  private final transient com.github.zafarkhaja.semver.Version _version;

  public Version(String text) {
    this.text = text;
    this._version = com.github.zafarkhaja.semver.Version.valueOf(text);
  }

  private Version(com.github.zafarkhaja.semver.Version _version) {
    this.text = _version.toString();
    this._version = _version;
  }

  public boolean satisfies(String expr) {
    return _version.satisfies(expr);
  }

  public boolean satisfies(Expression expr) {
    return _version.satisfies(expr);
  }

  public Version incrementMajorVersion() {
    return new Version(_version.incrementMajorVersion());
  }

  public Version incrementMajorVersion(String preRelease) {
    return new Version(_version.incrementMajorVersion(preRelease));
  }

  public Version incrementMinorVersion() {
    return new Version(_version.incrementMinorVersion());
  }

  public Version incrementMinorVersion(String preRelease) {
    return new Version(_version.incrementMinorVersion(preRelease));
  }

  public Version incrementPatchVersion() {
    return new Version(_version.incrementPatchVersion());
  }

  public Version incrementPatchVersion(String preRelease) {
    return new Version(_version.incrementPatchVersion(preRelease));
  }

  public Version incrementPreReleaseVersion() {
    // the implementation of incrementPreReleaseVersion is broken,
    // because it CHANGES the object it is called upon...
    // That's why we perform another copy here
    com.github.zafarkhaja.semver.Version _copy = com.github.zafarkhaja.semver.Version.valueOf(text);

    return new Version(_copy.incrementPreReleaseVersion());
  }

  public int getMajorVersion() {
    return _version.getMajorVersion();
  }

  public int getMinorVersion() {
    return _version.getMinorVersion();
  }

  public int getPatchVersion() {
    return _version.getPatchVersion();
  }

  public boolean isRelease() {
    return _version.getPreReleaseVersion().isEmpty();
  }

  public boolean isSnapshot() {
    return !isRelease();
  }

  public Version getNormalVersion() {
    return new Version(_version.getNormalVersion());
  }

  public String getPreReleaseVersion() {
    return _version.getPreReleaseVersion();
  }

  public String getBuildMetadata() {
    return _version.getBuildMetadata();
  }

  public boolean greaterThan(Version other) {
    return _version.greaterThan(other._version);
  }

  public boolean greaterThanOrEqualTo(Version other) {
    return _version.greaterThanOrEqualTo(other._version);
  }

  public boolean lessThan(Version other) {
    return _version.lessThan(other._version);
  }

  public boolean lessThanOrEqualTo(Version other) {
    return _version.lessThanOrEqualTo(other._version);
  }

  public int compareTo(Version other) {
    return _version.compareTo(other._version);
  }

  public int compareWithBuildsTo(Version other) {
    return _version.compareWithBuildsTo(other._version);
  }

  @Override
  public String toString() {
    return text;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Version version = (Version) o;
    return Objects.equals(text, version.text);
  }

  @Override
  public int hashCode() {
    return Objects.hash(text);
  }
}
