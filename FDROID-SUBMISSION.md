# F-Droid submission knowledge (Lumière)

Everything learned while submitting Lumière to F-Droid — app-specific facts,
F-Droid rules, and gotchas. Keep this updated as things change.

## App facts (ca.justinmo.lumiere)

- Package: `ca.justinmo.lumiere`, versionName `1.0` / versionCode `1`, GPL-3.0-only
- Single Gradle flavor `full` (was `fdroid`, `dev` removed). Release APK is
  unsigned by design — F-Droid re-signs with its own key.
- F-Droid build command: `./gradlew assembleFullRelease -Pstrict.release`
  - `-Pstrict.release` is a convention flag (fdroidclient mirrors it: when set,
    local static-analysis plugins are skipped). In this project it's a no-op
    (no checkstyle/PMD configured) but must stay accepted.
- Fastlane metadata: `fastlane/metadata/android/{en-US,fr}` — title, short/full
  description, changelog, icon, featureGraphic, phoneScreenshots.
- Category chosen for fdroiddata: `Flashlight` (must be in fdroiddata's
  `config/categories.yml` list).
- Repo: `github.com/JustinMichaudOuellette/Lumiere`; release tag `v1.0` points
  at `928a977…` — the same full hash used in the fdroiddata `commit:` field.

## F-Droid fastlane metadata rules

- Location: `fastlane/metadata/android/<locale>/` (en-US is the fallback locale).
- Required files: `short_description.txt` (≤80 chars, **no trailing dot**),
  `full_description.txt` (≤4000 chars). `title.txt` ≤50 chars.
- `changelogs/<versionCode>.txt` (≤500 chars) — filename must equal versionCode,
  no padding. Gives the "What's New" entry.
- Images under `images/`: `icon.png`, `featureGraphic.png` (landscape, ~1024x500),
  `phoneScreenshots/1.png`, `2.png`, … PNG or JPEG only.
- Latest-tab criteria (F-Droid client ≥1.6): Name, Icon, Summary, Description,
  License, a What's New for ≥1 release, ≥1 graphic, and ≥1 of those translated.
  License comes from fdroiddata metadata, not the app repo.

## fdroiddata build metadata (metadata/ca.justinmo.lumiere.yml)

- Field order is enforced by fdroidserver:
  Categories → License → AuthorName/Email/WebSite → WebSite → SourceCode →
  IssueTracker → (Translation/Changelog/Donate…) → RepoType/Repo →
  Builds → AutoUpdateMode → UpdateCheckMode → CurrentVersion → CurrentVersionCode.
- `commit:` must be the **full 40-hex hash** of the release commit (tags parse
  but current guidance says use the hash; matches the git tag).
- Build block:
  ```yaml
  - versionName: '1.0'
    versionCode: 1
    commit: 928a977c6a6c854a0f5127c35b1a882b0539c181
    subdir: app
    gradle:
      - assembleFullRelease
    gradleprops:
      - strict.release
  ```
- `gradle:` entries are passed to gradle **literally** — no flavor→task mapping,
  so write the full task name (`assembleFullRelease`, not `full`).
  `gradleprops:` entries become `-P<prop>` flags.
- `subdir: app` works even though `settings.gradle` is at the repo root: Gradle
  searches parent dirs for `settings.gradle`. APK is found at
  `app/build/outputs/apk/full/release/app-full-release-unsigned.apk`.
- Auto-update: `AutoUpdateMode: Version` + `UpdateCheckMode: Tags`; versionCode
  lives in the normal location (`app/build.gradle` defaultConfig), so no
  `UpdateCheckData` needed.

## Validation workflow (local)

- `pip install fdroidserver` (installed as `python -X utf8 -m fdroidserver.*`).
- From the fdroiddata clone root:
  - `fdroid lint ca.justinmo.lumiere` (exit 0 = clean)
  - `fdroid rewritemeta ca.justinmo.lumiere` (formats canonically, e.g. quotes
    version strings)
- Categories validated against `config/categories.yml` (108 valid entries).

## Submission workflow (done + remaining)

1. ✅ fastlane metadata in app repo, `v1.0` tag pushed to GitHub
2. ✅ fdroiddata clone at `~/repos/fdroiddata`, branch `ca.justinmo.lumiere`,
   committed "New App: ca.justinmo.lumiere", lint + rewritemeta pass
3. ⏳ Fork fdroiddata on GitLab, add `fork` remote, push branch (triggers CI lint)
4. ⏳ Open MR to `master` with "New App" label; wait for review (~24-48h after merge)

## Gotchas

- **Windows line endings / placeholder srclibs**: upstream fdroiddata has broken
  srclib files (`srclibs/MozAndroid*AS.yml` contain a bare string, no trailing
  newline) that crash this fdroidserver's parser during full-repo lint. Workaround:
  temporarily overwrite with minimal valid YAML, lint, then `git checkout -- srclibs/`.
- `fdroid lint` warns `unsafe permissions on 'config.yml' (should be 0600)` on
  Windows — harmless.
- Updating the app later: bump versionCode/versionName in `app/build.gradle`,
  add `fastlane/metadata/android/<locale>/changelogs/<newcode>.txt`, tag
  `v<versionName>` (annotated), update the yml `commit:` + CurrentVersion fields.
  AutoUpdateMode: Version picks up new tags automatically.
