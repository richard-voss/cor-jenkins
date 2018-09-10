# Continuous Release for Jenkins

Allows releasing a reasonable next version number in a
 Jenkins pipeline with just a few lines of code.
 
```groovy
import de.fruiture.cor.jenkins.VersionCalculator

node {
  def vc = VersionCalculator.release()
   
  vc.tags(
    sh(script: "git ${vc.gitTagCommand}", returnStdout: true)
  )
  
  vc.messages(
    sh(script: "git ${vc.gitLogCommand}", returnStdout: true)
  )
  
  def version = vc.nextVersion
  
  // the actual build ...
  sh("mvn versions:set -DnewVersion=${version} versions:commit")
  sh("mvn clean verify")
  
  sh("git ${vc.gitNextTagCommand}")
  sh("git push --tags")
}
```

It is built to support the following process:

1. Determine the last successful release from git tags
1. Look at all commit messages since that tag to determine whether
    minimal (patch level), minor or major changes were made (based on keywords)
1. Determines the next suitable version number for the current build
1. Apply that version and execute whatever the build does
1. On Success, tag the current commit with that version (and push the tag)

Incrementing the version is based on the rules of [semantic versioning](https://semver.org/),
as implemented by [Java SemVer]()https://github.com/zafarkhaja/jsemver).

## Continuous Release means...

... that every successful build of a project is a release and increments
the version number of that project.

The `VersionCalculator` supports two strategies as to what type of release
is being produced by a build:

* `VersionCalculator.release()` always produces a regular release version, like `1.2.3`
* `VersionCalculator.snapshot()` always produces a snapshot release, like `1.2.3-SNAPSHOT.17`

Both strategies can be combined, i.e. the `release` strategy will
correctly bump to the next release if the previous version was a snapshot,
and `snapshot` will bump to the next suitable snapshot version
if the previous version was a full release.
Both will properly increment major, minor or patch numbers. 