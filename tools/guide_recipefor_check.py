"""Every <RecipeFor id="..."> in the guidebook must be an item one of our recipes actually produces.

guideme resolves that attribute through BuiltInRegistries.ITEM and then renders whatever crafting
recipes yield the item, so a dangling id is not a hard error - it silently renders an empty box.
"""
import glob
import json
import os
import re
import sys

ROOT = 'src/main/resources'
GUIDE = os.path.join(ROOT, 'assets/neoecoprototype/ae2guide')
DATA = os.path.join(ROOT, 'data/neoecoprototype')

produced = {}
for path in glob.glob(os.path.join(DATA, 'recipe/**/*.json'), recursive=True):
    with open(path, encoding='utf-8') as handle:
        blob = json.load(handle)
    out = blob.get('result') or blob.get('itemOutput') or blob.get('output') or {}
    rid = out.get('id')
    if isinstance(rid, list):
        rid = rid[0] if rid else None
    if rid:
        produced.setdefault(rid, []).append(os.path.relpath(path, ROOT))

tag = re.compile(r'<RecipeFor\s+id="([^"]+)"')
missing = []
checked = 0
for path in sorted(glob.glob(os.path.join(GUIDE, '**/*.md'), recursive=True)):
    with open(path, encoding='utf-8') as handle:
        text = handle.read()
    for line_no, line in enumerate(text.splitlines(), 1):
        for item_id in tag.findall(line):
            checked += 1
            if item_id not in produced:
                missing.append('%s:%d  %s' % (path, line_no, item_id))

print('RecipeFor tags checked: %d, our recipe outputs: %d' % (checked, len(produced)))
if missing:
    print('NO RECIPE PRODUCES THESE:')
    for entry in missing:
        print('  ' + entry)
    sys.exit(1)
print('OK: every RecipeFor id has at least one producing recipe in our namespace')
