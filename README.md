# Continuous Release for Jenkins

Allows releasing a reasonable next version number in a
 Jenkins pipeline with just a few lines of code.

The approach completely agnostic of what the actual build does.
It rather computes the next reasonable version number in advance and allows to feed it as parameter
into the remaining process. (Inspired by [Maven Release Plugin: Dead and Buried](https://axelfontaine.com/blog/dead-burried.html))
 
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

0. Determine the last successful release from git tags
0. Look at all commit messages since that tag to determine whether
    minimal (patch level), minor or major changes were made (based on keywords)
0. Determines the next suitable version number for the current build
0. Apply that version and execute whatever the build does
0. On Success, tag the current commit with that version (and push the tag)
    * or on failure, don't -- the next commit will compute the same version and try to release it

Incrementing the version is based on the rules of [semantic versioning](https://semver.org/),
as implemented by the [Java SemVer]()https://github.com/zafarkhaja/jsemver) library.

## Continuous Release means...

... that every successful build of a project is a release and increments
the version number of that project.

The `VersionCalculator` supports two strategies as to what type of release
is being produced by a build:

* `VersionCalculator.release()` always produces a regular release version, like `1.2.3`
* `VersionCalculator.snapshot()` always produces a snapshot release, like `1.2.3-SNAPSHOT.17`, which may
  be suitable if you don't really want to continuously release ;)

Both strategies can be combined, i.e. the `release` strategy will
correctly bump to the next release if the previous version was a snapshot,
and `snapshot` will bump to the next suitable snapshot version
if the previous version was a full release.
Both will properly increment major, minor or patch numbers.

## Details

### Tag prefix

You can define a prefix for the version tags.

```groovy
VersionCalculator.release("v")
```

This will make the tool assume that version tags start with `v`:

    v1.2.3
    v1.2.4-SNAPSHOT.1

And of course also generate tags with that prefix. All tags not matching
the prefix will be ignored.

### Change keywords

Minor and major version number will only be incremented when certain
keywords are found somewhere in the commit messages.

By default `CHANGE:MINOR` triggers a minor version bump, and
`CHANGE:MAJOR` triggers a major version bump.

You can change these by specifying list of regular expressions:

```groovy
vc.triggerMinorChange = [/compatible API change/]
vc.triggerMajorChange = [/breaking API change/]
```

### Commit messages

By default, only the output of `git log --pretty=oneline` is used for keyword search.
You can, however, perform any other command to obtain the text to search for keywords.

Use `vc.gitLogVersionRange` to get the correct range expression.


