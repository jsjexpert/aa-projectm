#!/usr/bin/env python3
"""Pick N random texture-free presets from a presets-cream-of-the-crop checkout into app assets.

Usage: tools/pick-presets.py <cream-of-the-crop dir> [N=200] [seed=1]
  git clone --depth 1 https://github.com/projectM-visualizer/presets-cream-of-the-crop.git

"Texture-free" = every sampler_* the preset uses is one projectM generates itself
(main/blur/noise); anything else is an image file we don't ship.
"""
import os, random, re, shutil, sys

src = sys.argv[1]
n = int(sys.argv[2]) if len(sys.argv) > 2 else 200
seed = int(sys.argv[3]) if len(sys.argv) > 3 else 1
dst = os.path.join(os.path.dirname(os.path.abspath(__file__)), '..', 'app', 'src', 'main', 'assets', 'presets')

BUILTIN = re.compile(r'^(?:(?:fc|pc|fw|pw)_)?(main|noise_lq|noise_lq_lite|noise_mq|noise_hq|noisevol_lq|noisevol_hq|blur[123])$', re.I)
SAMPLER = re.compile(rb'sampler_([A-Za-z0-9_]+)')

ok = []
for d, dirs, files in os.walk(src):
    dirs[:] = [x for x in dirs if x != '.git' and x != '! Transition']  # transitions aren't visualizers
    for f in files:
        if f.endswith('.milk'):
            p = os.path.join(d, f)
            if all(BUILTIN.match(s.decode()) for s in SAMPLER.findall(open(p, 'rb').read())):
                ok.append(p)
ok.sort()
pick = random.Random(seed).sample(ok, min(n, len(ok)))

def md(t):  # escape markdown specials in preset titles
    return re.sub(r'([\\`*_\[\]<>#|])', r'\\\1', t)

shutil.rmtree(dst, ignore_errors=True)
os.makedirs(dst)
for p in pick:
    cat = os.path.relpath(p, src).split(os.sep)[0]
    name = re.sub(r'[^A-Za-z0-9._-]', '_', f'{cat}-{os.path.basename(p)}')
    shutil.copyfile(p, os.path.join(dst, name))
# Preset list + author credit in README, between markers. Author = text before the first " - " in the
# original filename (the MilkDrop naming convention); noted when the name has none.
by_cat = {}
for p in pick:
    cat = os.path.relpath(p, src).split(os.sep)[0]
    base = os.path.basename(p)[:-5]
    author, sep, title = base.partition(' - ')
    by_cat.setdefault(cat, []).append((title, author) if sep else (base, 'author not in filename'))
lines = [f'{len(pick)} presets, randomly picked from the {len(ok)} texture-free presets in '
         '[presets-cream-of-the-crop](https://github.com/projectM-visualizer/presets-cream-of-the-crop) '
         '(curated by ISOSCELES). Authors are taken from the original filenames.', '']
for cat in sorted(by_cat):
    lines += [f'<details><summary><b>{cat}</b> ({len(by_cat[cat])})</summary>', '']
    lines += [f'- {md(t)} — {md(a)}' for t, a in sorted(by_cat[cat], key=lambda x: x[0].lower())]
    lines += ['', '</details>', '']
readme = os.path.join(os.path.dirname(dst), '..', '..', '..', '..', 'README.md')
text = open(readme, encoding='utf-8').read()
start, end = '<!-- presets:start -->', '<!-- presets:end -->'
i, j = text.index(start) + len(start), text.index(end)
open(readme, 'w', encoding='utf-8').write(text[:i] + '\n' + '\n'.join(lines) + text[j:])

print(f'{len(ok)} texture-free presets found, copied {len(pick)} to {os.path.normpath(dst)}, listed in README')
