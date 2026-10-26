# BOHPTS Salvation — High Five compatibility work

This document concerns ONLY the new High Five server project.

## Scope
- Server base: L2J Mobius CT 2.6 HighFive.
- Target client: BOHPTS Salvation.
- The previously shared `system.7z` archive belongs to Lineage II Interlude and is OUT OF SCOPE.
- Do not use the Interlude archive as evidence for High Five client compatibility.
- Keep all new compatibility work isolated on the `import/mobius-highfive` branch until validated.

## Initial source checks
- The upstream `build.xml` declares JDK 27 and Ant 1.9.10+ as build requirements.
- `dist/game/config/Server.ini` currently allows protocol revisions `267;268;271;273`.
- The target client's actual protocol revision and packet compatibility have not yet been verified.

## Next steps
1. Obtain files/version details from the BOHPTS Salvation High Five client specifically.
2. Identify its exact client build and protocol revision.
3. Compare the client system files and protocol implementation with this server source.
4. Run build and login/game connection tests.
5. Implement only confirmed differences, on a separate feature branch.
