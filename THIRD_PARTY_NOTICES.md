# Third-party notices

This app is licensed for noncommercial use only (see [LICENSE](LICENSE)). That license covers only the original code in this repository. The components below keep their own licenses, and nothing in LICENSE restricts the rights those licenses give you.

## projectM: LGPL-2.1

- **What:** libprojectM, the MilkDrop-compatible renderer. Copyright (C) 2003-2009 projectM Team and contributors.
- **Version:** v4.1.8, unmodified. It's a git submodule at `third_party/projectm` (commit `4967dc0`). Source: <https://github.com/projectM-visualizer/projectm/tree/v4.1.8>.
- **License:** GNU Lesser General Public License 2.1. Full text: [`third_party/projectm/LICENSE.txt`](third_party/projectm/LICENSE.txt), or <https://www.gnu.org/licenses/old-licenses/lgpl-2.1.html>.
- **How it's used:** dynamically linked. The APK ships it as its own library, `lib/<abi>/libprojectM-4.so`. The app calls only its public C API (`projectM-4/*.h`). You may replace that library with your own build of projectM (LGPL-2.1 §6). Rebuild with the steps in the README, or swap the `.so` in the APK and re-sign it. LICENSE allows you to modify this app and reverse-engineer it to debug such changes.
- **Test presets:** `app/src/main/assets/test-presets/` contains `206-wave.milk` and `251-wavecode-spectrum.milk`, copied unmodified from projectM's `presets/tests/` (LGPL-2.1).

### Components built into libprojectM

| Component | License | Copyright |
|---|---|---|
| [projectm-eval](https://github.com/projectM-visualizer/projectm-eval) | MIT | © 2023 The projectM Team |
| [hlslparser](https://github.com/unknownworlds/hlslparser) | MIT | © 2013–2014 Unknown Worlds Entertainment, Inc. |
| [GLM](https://github.com/g-truc/glm) | MIT (or The Happy Bunny License) | © 2005 G-Truc Creation |
| [SOIL2](https://github.com/SpartanJ/SOIL2), including [stb_image](https://github.com/nothings/stb) | MIT-0 (stb: public domain / MIT) | © 2020 Martín Lucas Golini; Sean Barrett |

MIT license text, which applies to each MIT component above with its copyright line:

```
Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```

## MilkDrop presets

- **Source:** the 200 presets in `app/src/main/assets/presets/` are an unmodified random selection from [presets-cream-of-the-crop](https://github.com/projectM-visualizer/presets-cream-of-the-crop) (commit `0180df2`). ISOSCELES curated and sorted that collection, and the projectM team distributes it. The files were renamed to `<category>-<original name>`.
- **Authors:** each preset is the work of its author, credited in the README's preset list. MilkDrop itself was created by Ryan Geiss.
- **License:** in almost all cases these presets were released without a license. The projectM team's position is that each author holds copyright, and that freely released presets can be treated as public domain. If you're a preset author and want yours removed, open an issue and it will be taken out.

## Android libraries in the APK: Apache-2.0

The following are licensed under the [Apache License 2.0](https://www.apache.org/licenses/LICENSE-2.0) and are copyright their respective authors:
- **AndroidX:** Car App Library (`androidx.car.app:app`, `app-projected`), core, activity, annotation, collection, concurrent-futures, lifecycle, media, savedstate, tracing, versionedparcelable, arch-core. © The Android Open Source Project.
- **Guava**, failureaccess and listenablefuture. © The Guava Authors.
- **Kotlin standard library.** © JetBrains s.r.o. and Kotlin Programming Language contributors.
- **JetBrains annotations.** © JetBrains s.r.o.
- **JSR-305 annotations** (`com.google.code.findbugs:jsr305`).
- **J2ObjC annotations.** © Google Inc.
- **JSpecify annotations.** © The JSpecify Authors.

Also included:
- **Checker Framework qualifiers** (`org.checkerframework:checker-qual`): MIT, © 2004-present by the Checker Framework developers.
- **LLVM libc++ / libc++abi** (Android NDK r27, statically linked into the native libraries): Apache-2.0 with LLVM Exception. © LLVM project contributors.

## Build tooling (not shipped in the APK)

- **Gradle wrapper** (`gradlew`, `gradle/wrapper/`): Apache-2.0, © Gradle Inc.
