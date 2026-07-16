# Releasing

The repository uses semantic versions, conventional commits and Release
Please. Normal releases do not require a developer to create tags manually.

## Normal release flow

1. Merge conventional commits to `main` only after CI succeeds.
2. The release workflow opens or updates a Release Please PR containing the
   next version and changelog.
3. Review that PR like any code change. Confirm the proposed major/minor/patch
   bump matches compatibility impact.
4. Merge the release PR. Release Please creates the `vX.Y.Z` tag and GitHub
   Release.
5. The same workflow runs `check`, validates Maven publication, builds the
   simple demo, injects the tag as the artifact version, and uploads
   library/demo artifacts to the GitHub Release.
6. JitPack builds immutable coordinates from the tag. Verify the badge and
   dependency shown in the README before announcing the release.

`feat:` creates a minor release and `fix:` creates a patch release. A breaking
change must include `BREAKING CHANGE:` in the commit footer and requires a major
release. Documentation-only and test-only commits normally do not trigger a
version bump.

## Release failure

Do not move or replace an existing tag. Fix the problem on `main`, let CI pass,
and create a patch release. If artifact upload alone failed, rerun the release
workflow for the tagged commit; the build remains reproducible from the tag.

## First release

`v0.1.0` is bootstrapped once when the repository is created. The manifest then
records `0.1.0`, so Release Please owns every subsequent version.
