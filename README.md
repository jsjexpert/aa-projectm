# AA projectM (POC, sideload only)

projectM (MilkDrop-compatible) drawn into an Android Auto `NavigationTemplate` surface.

Audio: the **Visualizer API** by default (mono 8-bit, ~20 Hz, works with Spotify). It attaches to the session a player announces (remembered across launches, retried while Android Auto moves the track to its projection output). The global-mix fallback (session 0) is silent during projection, since media then plays on the remote-submix output. **Spotify only**: other players don't announce their session (tested: MX Player, TikTok), and YouTube won't play during projection. Stereo `AudioPlaybackCapture` (`CaptureService`) is kept in the source but disabled: commented out of the manifest and phone UI, because Spotify blocks it.

Permissions: only the microphone permission (`RECORD_AUDIO`), which the Visualizer API requires. It isn't logged as microphone use. Plus `MODIFY_AUDIO_SETTINGS` and the car template permissions, granted at install. No casting or screen-capture prompt, and no foreground service.

## Install a downloaded APK

You need:
- An Android 10+ phone (arm64), with Android Auto and Spotify installed.
- A car with Android Auto, or the Desktop Head Unit (see *Run on the DHU*).

1. Download `aa-projectm-v<version>-debug.apk` from this repo's **Releases** page. Optionally, check it against the SHA-256 in the release notes: run `sha256sum aa-projectm-*.apk` on Linux, or `Get-FileHash aa-projectm-*.apk` in Windows PowerShell.
2. Install it with either method:
   - **On the phone:** open the downloaded file, for example from the browser's downloads or the Files app. When prompted, allow *Install unknown apps* for that app, then tap **Install**. Play Protect may warn about an unknown developer; tap *More details → Install anyway*.
   - **From a computer** with USB or wireless debugging on:
     ```bash
     adb install -r aa-projectm-v<version>-debug.apk
     ```
3. Open **AA projectM** on the phone once and allow the microphone permission. The Visualizer API requires it; nothing is recorded.
4. Let Android Auto show sideloaded apps:
   1. Open Android Auto settings and tap *Version* about 10 times until developer mode turns on.
   2. Tap **⋮ → Developer settings** and enable **Unknown sources**.
5. Connect to the car (or start the DHU), play something in Spotify, and open **AA projectM** from the Android Auto launcher.

**About the signature:** the APK is a debug build. It's signed only with the build machine's debug key, not a release key, which is what makes sideloading possible. An update signed with a different key won't install over it ("App not installed" / "package conflicts"). In that case, uninstall AA projectM first, then install the new APK.

## Build
```bash
git submodule update --init --recursive
./setup-sdk.sh            # you accept the SDK licenses
./gradlew assembleDebug
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

## Run on the DHU
1. Phone: Android Auto settings → tap *Version* 10× → menu → *Developer settings* → enable *Unknown sources* → *Start head unit server*.
2. Open **AA projectM** on the phone once and grant permissions. Play music.
3. `./run-dhu.sh` (forwards port 5277 and starts the DHU)
4. On the DHU, open AA projectM. Tap the visuals once for the next preset (auto-switches every 30 s).
5. Triple-tap quickly for test mode: projectM's audio test presets (oscilloscope line; waveform + spectrum), held in place. Triple-tap again for the next test preset, single-tap to leave. Paused audio gives a flat line.

Check with `adb logcat -s viz`:
- `Visualizer on session N` shows which audio session is being visualized (0 = global mix).
- `vis peak` every ~5 s: the level on the attached session (0.0 = silence or the wrong session).
- `... fps` lines are frame-rate readouts per 300 frames.

## Known limits
- **Which players work:** the Visualizer source needs the player to announce its audio session (`ACTION_OPEN_AUDIO_EFFECT_CONTROL_SESSION`, the hook system equalizers use). Spotify does. Players that don't announce leave the visuals running without audio during projection: the global mix is silent then, and Android hides other apps' session IDs (`AudioPlaybackConfiguration` reports 0). Re-enabling `CaptureService` would cover players that allow capture, at the cost of a consent prompt on the phone.
- Reinstalling during a DHU session leaves the car surface held by the killed process (`EGL_BAD_ALLOC`, nothing drawn). Restart the DHU after each install.
- Spotify 9.2.x blocks playback capture (verified on SM-N986W / Android 13), hence the Visualizer source. If `CaptureService` is re-enabled, start it *before* pressing play, since routing is fixed when the app opens its stream.
- First DHU connect needs "Add new cars to Android Auto" on and the car approved on the unlocked phone.
- No `CAR_SPEED` permission, so the speed guard never receives speed and the visuals always render. Restore the permission (manifest + runtime request) before any demo in a car that can move.
- `HostValidator.ALLOW_ALL_HOSTS_VALIDATOR` is for sideloading only.
- Presets: 200 random texture-free picks from [presets-cream-of-the-crop](https://github.com/projectM-visualizer/presets-cream-of-the-crop) (7,925 qualify; all of them would add ~27 MB to the APK). Regenerate with `tools/pick-presets.py <checkout> [N] [seed]`. They play as a shuffled playlist. Some may not compile on GLES and render poorly; not screened yet. Visible/stable area is ignored, so the visuals run under the action strip.

## License and attribution

The APK carries the full license texts in `assets/licenses/` (copied from the repo at build time); the phone app shows them under *Open-source licenses*.

The code in this repository is licensed under the [PolyForm Noncommercial License 1.0.0](LICENSE). That means noncommercial use only.

It builds on others' work, which keeps its own licenses (details in [THIRD_PARTY_NOTICES.md](THIRD_PARTY_NOTICES.md)):
- **[projectM](https://github.com/projectM-visualizer/projectm)** by the projectM Team, the MilkDrop-compatible renderer: LGPL-2.1. It's used unmodified (v4.1.8) and dynamically linked as `libprojectM-4.so`, which you're free to replace with your own build.
- **MilkDrop**, created by Ryan Geiss. This app has no MilkDrop code; it plays MilkDrop-format presets through projectM.
- **Presets** by their individual authors (listed below), from the [presets-cream-of-the-crop](https://github.com/projectM-visualizer/presets-cream-of-the-crop) collection curated by ISOSCELES and distributed by the projectM team. Authors who want a preset removed can open an issue.
- **Android libraries:** AndroidX and the Car App Library, Guava and Kotlin stdlib (Apache-2.0); Checker Framework qualifiers (MIT); LLVM libc++ (Apache-2.0 with LLVM exception).

## Visualizers

Generated by `tools/pick-presets.py`; don't edit by hand.

<!-- presets:start -->
200 presets, randomly picked from the 7925 texture-free presets in [presets-cream-of-the-crop](https://github.com/projectM-visualizer/presets-cream-of-the-crop) (curated by ISOSCELES). Authors are taken from the original filenames.

<details><summary><b>Dancer</b> (23)</summary>

- 221 — author not in filename
- 241 — author not in filename
- 293 — author not in filename
- 418 squeakers — author not in filename
- breaking the beam only to hump the tower — suksma
- Cerebral Demons - Phat + EoS hall of ghouls Remix — Krash & Rovastar
- cyanred echoed beta6 — shifter
- dark side of the moon-clean mix — EoS
- dog vs hog vs chicken - naughty little people — suksma
- EoS\_Phat\_PeterP\_Sentinel\_Aware\_6 EoS edit C — author not in filename
- founding kneemoe --- Isosceles edit — whoraeckle'
- glowsticks v2 03 music shifter edit b (Stahl's Reactive RMX V2i - feat flexi + phat) — EoS
- glowsticks v2 04 music minimal - ghosts — EoS
- Hyperspace nossa — Rovastar
- liquid circuitry from the oblivion hive rw5 nz+ — shifter + Flexi
- Mash-Up Sphere Xibit Graffiti Warp me Tydye — AdamFX 2 Geiss
- Mashup (114) — $$$ Royal
- Mashup (123) — $$$ Royal
- Mashup (152) — $$$ Royal
- Mashup (270) — $$$ Royal
- more citylights — LuX
- niglige vs nigremortis — author not in filename
- Smoke and Fire (Jelly) — ORB

</details>

<details><summary><b>Drawing</b> (19)</summary>

-  Flexi  ft AdamFX Aderrasi , Martin n Rova - Witchcraft Take OverFX  A  — PHAT HD FX 2 FiShbRaiN 
- 414 bowel rebranding pr skinned to the teeth loadus morons like me who indulge in crying wolf chicken little paradigms — author not in filename
- antiinorganics primal dialectomy - silently observed pain body roam — suksma
- chris grease organiq stymied parchment phlossidite logos holographeme homophoneme proteome viramemet — author not in filename
- dripping\_piles\_B gained outside of mistranslation (dare rid vs tour ying) fixatives scathing hate — stbenge
- egg sacs canary cage slaughter 10000fps eternity — suksma
- fiShbRaiN - witchcraft (phugly shame on remix) - darning kneedless - biglet — suksma
- Fitting Butterfly - v2 - \[Loadus & Geiss - FractalDrop 22\] - REMIXED NEW22v2v2v2v2 - new — ShadowHarlequin
- Flowercraft — Stahlregen & fishbrain + geiss + martin 
- gunthry is out back bloodying up the pine trees nz+ — author not in filename
- lovecraft \[mr understatement\] \[organic aftermath\] — fishbrain and flexi
- mash0000 - sclera torture — DemonLD\_-\_Future\_domination
- mosaic mitosis - j4 jackless — shifter
- Plaidcraft — flexi + fishbrain + geiss + unchained + others
- purple 9-d rubber coins embossed with cataverses — suksma
- satanic teleprompter - sth shd fingre liphtid — suksma
- sawtooth grin nz+ — author not in filename
- Stonecraft (MashUp 19 - brdv's dark tides mix) squeakers — Stahlregen & fiShbRaiN + flexi + Geiss + shifter
- Witchcraft \[jelly compatibility check\] — fishbrain + Geiss + Stahlregen + Orb

</details>

<details><summary><b>Fractal</b> (17)</summary>

- \#11 Ice Drops — EvilJim
- 306 nz+ ain no hoehoe — author not in filename
- Accelerator (contractor Core) rmx — bdrv etAL Aderrasi
- Amorphous Magic carpet — Hexcollie
- Angels Of Glory \[Flexi - invoking nothing but bias + $this shall not retain\] --- Isosceles edit — TonyMilkdrop
- catch 333 magick clarity — author not in filename
- colorful marble (ghost mix) - rand wave 2 nz+ — cope + flexi
- ColourSturm II — LuxXx
- escape the worm Telek - EoS + Phat responsive edit bdrv etAL2 — bdrv + al shifter
- Fvese - MV Fun 3 - completely juiced by presses - pump me full of mercury and lead honey bun - flx spr col1 — suksma
- Glowstick Heart (MashUp 7) + PieturP — CantStop AdamFX 2 Stahlregen & Bmelgren + EoS + Geiss + Krash + Rocke + Rovastar
- Idiot24-7-mindbender7(superbendmix1) — author not in filename
- now for only $19-95, you can pray to a whole array of saviors — suksma
- phat\_RippleNebula\_update — author not in filename
- Rings Of Saturn — Dbleja
- skylight — EoS
- Zorn Church — EVET

</details>

<details><summary><b>Geometric</b> (25)</summary>

- 27\_super\_goats-orbus\_maximus poown — author not in filename
- angels of decay 01 soul — EoS
- Charmed Spun Sugar — Stahlregen & aderrasi + martin + rocke + rovastar
- chasers in the sky3 \[slipped\] fingre — fed \_ \[WHO\]
- Color Eater (Static Mix) --- Isosceles edit — Mstress
- crystal city — Halfbreak + martin
- Electric universe (variable spin) — Krash & yin
- EVET- The Ringer — author not in filename
- excerptise sweepia bindfoul outresloth — suksma
- fuzzball 3d (glasses) false auralary2 mbaleffiscienz — shifter
- god might as well - unc fs m roams — suksma
- god might as well2 - few tent — suksma
- Hedgehog dreams — Hexcollie
- imbuing terror joy - naked eye blue — suksma
- iterative - tendrils — amandio c
- martin in da mash 12 — martin + stahlregen
- matrix cube J convolver matrix — EoS
- movement within the trout - i love you, i thank you, it meant everything, don't forget — suksma
- noysez award losing single wrap womb dynamics e8 a'sha — cope + martin
- pus on my cat - doris the finkasaurus — suksma
- spun sugar — shifter
- spun sugar - causal mothritis — shifter
- Telek-goodoldcollingwoodforever — author not in filename
- typhoon season (inside) - fuckingdelic — amandio c
- xtramartin (201) — author not in filename

</details>

<details><summary><b>Hypnotic</b> (13)</summary>

- alien complex 00 — Flexi
- grind my glitch up \[112\] — flexi
- grind my glitch up \[189\] — flexi
- grind my glitch up \[201\] — flexi
- grind my glitch up \[242\] — flexi
- grind my glitch up \[247\] — flexi
- grind my glitch up \[340\] — flexi
- Lasershow \[bipolar notification party\] — GreatWho + Flexi
- lock and release bipolar 04 — martin + flexi
- negative infinity for not flinching flexi — suksma
- Paint Spill (Music Reactive Paint Mix) nz+ throtthel deadly death danger — Zylot
- Tricolors \[Flexi - priming + alien complex\] --- Isosceles edit — TonyMilkdrop
- Tricolors \[Flexi - pump up the jam + multiverse\] --- Isosceles edit — TonyMilkdrop

</details>

<details><summary><b>Particles</b> (7)</summary>

- cad bury chalk let egz + demonlord - i am aware of the fools i follow, there's nothing better to do — suksma
- charming tiles — martin
- circle of the taeyrunts — suksma
- gandalf does it best- Psychoed — EVET + fiShbRaiN + Geiss
- hypra-a-astral hypra-a-plane fairlly amazing bullshit — author not in filename
- inspired by unmitigated torture — skipper skipper kiss me hard
- Space (Twisted Dimension Mix) — Rovastar

</details>

<details><summary><b>Reaction</b> (38)</summary>

- Acid Sunrise — ORB
- dark tides (EoS+phat\_whisps of doom mix) - mash0000 - salt dunes time lapse — shifter
- Dawning The Peacocks \[Flexi - quitter\] --- Isosceles edit — TonyMilkdrop
- dead cold nz+ — goody + geiss 
- dilated eye, sphincter I foot — LuxXx
- Do Drive and Drug — LuxXx
- emergencey 2 --- Isosceles edit10b rings — Flexi
- emergencey 2 --- Isosceles edit3d sparse — Flexi
- emergencey 2 --- Isosceles edit8b circle — Flexi
- Harlequin's Fractal Encounter - more blood oaths nz — Rovastar
- if it's good enough for me to save, it's dead enough for you to re-m flx nz+ — author not in filename
- if it's good enough for me to save, it's dead enough for you to re-m flx roams jissom devilse — author not in filename
- Isosceles mashup25 — author not in filename
- Mashup (127) — $$$ Royal
- Mashup (155) — $$$ Royal
- Mashup (238) — $$$ Royal
- melting feather anthem - your dad's voice makes me horny — suksma
- Mostly Black and Mostly White — Jc
- nematodes (Jelly V3) — goody + EoS + martin
- never trust bdrv martin or flexi or amandio — author not in filename
- no religion  + disco Fruits Machine + Raron + mstress + 4 — A Remixed Digital Echasketch  Again 2 martin
- Nuclear \[Flexi - i don't want to be responsible + techstyle\] --- Isosceles edit1 — TonyMilkdrop
- phliping out ovre normal duqs — author not in filename
- quant combines ray - santa klaus vs anile nekrow PAIR rnz — X-pand
- shader circus \[07 you can do many funny things\] — Flexi
- shadow dynamics 03 — Flexi
- Simple Circuitodes  (wow) — LuxXx
- skin lard — author not in filename
- splatter effects 17 the wave, a google love story written in decay nz — flexi
- stuffing wolves into bay-os deoree — author not in filename
- sun pod gambit couch nz+ depppositional contrabandwidth — suksma
- target practice-5-liquid-2 — flexi
- Tie-Dye Supernova (Sunspots Mix) - timely pointlessness — Benjam and Zylot
- trial by pumpbot - log smell — suksma
- Tricolors \[Flexi - fwiw + $this shall not retain\] --- Isosceles edit — TonyMilkdrop
- unholy amulet — martin
- when i first mixed this, i loved it, now, because of your existence, i fucking hate it --- Isosceles edit — author not in filename
- whiteopal glimmer - flx insane psychlopaein formations - xenarch of the kovenand gate - tocksi — shifter

</details>

<details><summary><b>Sparkle</b> (16)</summary>

- artifactual intermutliversal borg web — suksma
- crystal palace - Schizotoxin - The Wild Iris Bloom - mess2 nz+ french cuisine vs russion hit men — Goody + martin
- crystal palace nz+ — martin
- crystal palace001d — Serge + martin
- dotes hostile undertake - greener grass (secret, indoor, hydroponic) roam3 — suksma
- Dots (Pixels - no Blur) — Stahlregen
- flexi - fractrip - lofted guts - flacc - pisce nz+ — suksma
- Fractopia bdrv132456 mix bleuarg - fingre — bdrv + al Rovastar
- geiss + dotes roams proph — suksma
- god might as well2 - rst filled to the brim with atheistic hate roam3 — suksma
- martin - neon space ps3 - dotes2 - flx flacc roam3 piss verticle — suksma
- phlegmly sczhultz hog rot2 — cpe domains flacc
- Rototranslate difference layers 2 + on dotes5 roam3 — Stahlregen + Amandio C
- uhnfareknesce roosta conflarian — suksma
- violent and insane homicidal air molecules - n777 — fleekus flokus
- violent and insane homicidal air molecules2 — fleekus flokus

</details>

<details><summary><b>Supernova</b> (6)</summary>

- Cloud Burst — ORB
- Curly Que (1) — Hexcollie
- Enni Cened Silmarilli — Dark One
- Mashup (83) — $$$ Royal
- splash! mexupa yay — stahlregen
- type o negative - world coming down — suksma

</details>

<details><summary><b>Waveform</b> (36)</summary>

- 370 — author not in filename
- 3D - Cosmic Dust — Geiss
- Acid Mandala v4b — Fumbling\_Foo & Flexi, Martin, Orb, Unchained
- Angel3 — Phat\_shifter
- Burning Down — Goody
- Cataracts (Soft Mix - Dots) — Stahlregen & Geiss + Phat + StudioMusic + Rovastar
- Cauldron - painterly 4 — Geiss
- Chromatic Abyss (Refined Abyss Mix) — Aderrasi
- Cosmic Dust 1 — Geiss
- crystal palace star010 — Serge + martin
- dotes hostile undertake + demonlord - blood in your eyes again - flacc nz+ neg — suksma
- echnometabole — suksma
- ed geining hateops - flx monochromatic joi - 1-ret — suksma
- ed geining hateops - flx shibbalibbadi nz — suksma
- ed geining hateops - poison earth, poison sterility — suksma
- Flexi - self similarity ft Martin Charisma with AdamFX K xfr+ — With AdamFX Presentation
- geiss remix 3b — Mig\_304
- Goody's Fatal Color Attraction (pliant reality remix) PS2-0 — author not in filename
- i did it all for the sex, drugs, music, and satan — rationalizing insanity using only mainstream religion
- Lightspeed-dune3 — author not in filename
- Liquid Beats (janky ripple warp reflecto) — Geiss
- Liquid Beats 2 (Reverse Jelly V3) — Geiss
- mosaique simple - foobar2000 nz+ — shifter
- Motion Blur 2 (Neon Jelly 2) — Geiss
- n19 3 layer overlap opt 3 flx nz+ loqo you, my friend, will be the johnny appleseed of weed turqly — suksma
- negative infinity for not flinching — suksma
- Neon Star Formation000 — Serge + Jc
- passionless internal cow revenge never exists — author not in filename
- Phahlsce Pseye boo jaw Z — AkashaDude & Geiss
- Space Gelatin — Stahlregen & Geiss + TobiasWolfBoi
- Space Gelatin (Color Xplosion 2-5) — Stahlregen & Geiss + TobiasWolfBoi
- spacefurnace — LuX\_-\_Geiss
- Star Forge v12a — Fumbling\_Foo & Flexi, Martin, Orb
- The Sunspot — Cope
- Trippy Sperm (Jelly) — Rovastar
- Virtual Horizons (Jelly V3) — Rovastar

</details>
<!-- presets:end -->
