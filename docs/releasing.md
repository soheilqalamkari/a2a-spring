# Releasing

Release artifacts are prepared for the Central Publisher Portal. The root
`release` Maven profile attaches source and Javadoc JARs, signs artifacts with
GPG, and enables Sonatype's `central-publishing-maven-plugin`.

Before a release:

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

The current project is not published yet. Publication must wait until the
repository ownership, namespace, credentials, and maintainer review are
resolved.
