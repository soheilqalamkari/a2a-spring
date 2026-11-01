# Releasing

Release scope and acceptance gates are defined in the
[A2A Spring release roadmap](release-roadmap.md). The planned sequence is
`0.1.x` community preview, `0.2.0` conformance, `0.3.0` production foundations,
and `1.0.0` stable Spring MVC. These are outcome-based milestones, not date
commitments.

Release artifacts are prepared for the Central Publisher Portal. The root
`release` Maven profile attaches source and Javadoc JARs, signs artifacts with
GPG, and enables Sonatype's `central-publishing-maven-plugin`.

Before a release candidate:

1. Confirm the milestone's roadmap gates, including `mvn verify`, the enabled
   official server test contract, and the TCK for advertised capabilities.
2. Update the compatibility matrix, changelog, sample, dependency coordinates,
   known limitations, and TCK report link.

Before publishing artifacts:

1. Register and verify the `io.github.soheilghalamkari` namespace in the
   Central Portal.
2. Configure a `central` server entry in Maven `settings.xml` using a Central
   user token. Never commit the token or a private signing key.
3. Configure a GPG key and verify it is available to Maven.
4. Change the project version from `0.1.0-SNAPSHOT` to a release version.
5. Run the full test suite and inspect the generated sources, Javadoc, POM,
   signatures, and checksums.
6. For a local release, run `mvn -Prelease clean deploy` and complete or verify
   the deployment in the Central Portal. For GitHub Actions, use the manual
   `Release` workflow after adding `CENTRAL_USERNAME`, `CENTRAL_PASSWORD`,
   `GPG_PRIVATE_KEY`, and `MAVEN_GPG_PASSPHRASE` repository secrets.

The current project is not published yet. For the 1.0.0 milestone, verify
artifact resolution from a clean consumer project. If Maven Central publication
is blocked, publish reproducible build and local-install instructions as
allowed by the upstream integration guide.
