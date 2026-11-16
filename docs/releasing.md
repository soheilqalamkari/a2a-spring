# Releasing

Release scope and acceptance gates are defined in the
[A2A Spring release roadmap](release-roadmap.md). The planned sequence is
`0.1.x` community preview, `0.2.0` conformance, `0.3.0` production foundations,
and `1.0.0` stable Spring MVC. These are outcome-based milestones, not date
commitments.

Release artifacts are prepared for the Central Publisher Portal. The root
`release` Maven profile attaches source and Javadoc JARs, signs artifacts with
GPG, and enables Sonatype's `central-publishing-maven-plugin`. The plugin is
configured to publish validated deployments automatically and wait for the
`PUBLISHED` state, so a successful release workflow means Central has finished
publishing rather than only accepting or validating the bundle.

Before a release candidate:

1. Confirm the milestone's roadmap gates, including `mvn verify`, the enabled
   official server test contract, and the TCK for advertised capabilities.
2. Update the compatibility matrix, changelog, sample, dependency coordinates,
   known limitations, and TCK report link.

Before publishing artifacts:

1. Register and verify the `io.github.soheilqalamkari` namespace in the
   Central Portal.
2. Configure a `central` server entry in Maven `settings.xml` using a Central
   user token. Never commit the token or a private signing key.
3. Export the GPG private key as ASCII-armored text, for example:
   `gpg --armor --export-secret-keys <KEY_ID>`. Save the complete output,
   including the `BEGIN PGP PRIVATE KEY BLOCK` and `END PGP PRIVATE KEY BLOCK`
   lines, as the `GPG_PRIVATE_KEY` Actions secret. Set
   `MAVEN_GPG_PASSPHRASE` to that key's passphrase. Do not paste the key into
   issues, pull requests, chat, or committed files.
4. Supply a stable numeric release version (for example `0.2.0`) to the
   workflow. It rejects snapshot or malformed versions and runs the full Maven
   `clean deploy` lifecycle, including tests; never bypass verification.
5. Run the full test suite and inspect the generated sources, Javadoc, POM,
   signatures, and checksums.
6. For a local release, run `mvn -Prelease clean deploy` and verify the
   deployment in the Central Portal. For GitHub Actions, dispatch the manual
   `Release` workflow after adding `CENTRAL_USERNAME`, `CENTRAL_PASSWORD`,
   `GPG_PRIVATE_KEY`, and `MAVEN_GPG_PASSPHRASE` repository secrets. The
   workflow waits until Central reports the deployment as `PUBLISHED`.

Before publishing for the first time, run the manual `Release preflight`
workflow on `main` with the intended stable version. It uses the GPG secrets to
build and verify a signed Central bundle, but sets `skipUpload=true` and does
not use the Central credentials. It checks that the parent and library modules
are present, that library JAR, sources, Javadoc, and POM files have valid
signatures, and that the runnable sample is excluded. A successful preflight
does not publish or release the artifacts; publication still requires an
intentional run of the `Release` workflow.

Release `0.2.0` was published to Maven Central from source commit [`e84b430`](https://github.com/soheilqalamkari/a2a-spring/commit/e84b4308cd88bee2c1792ded957f513b7cb9306a). The [release workflow run #1](https://github.com/soheilqalamkari/a2a-spring/actions/runs/43844344677) completed successfully after Central reported the deployment as published. A clean external Spring Boot consumer then resolved `io.github.soheilqalamkari:a2a-spring-boot-starter-web:0.2.0` and packaged successfully using an isolated, empty local Maven repository. The earlier [preflight run #4](https://github.com/soheilqalamkari/a2a-spring/actions/runs/43839618320) verified signing and bundle contents before upload.

For the `1.0.0` milestone, continue compatibility and clean-consumer verification for the final release candidate. TCK results for `0.2.0` remain partial; see [TCK conformance](tck.md).
