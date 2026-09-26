#!/usr/bin/env python3
"""Maps official Create / Catnip / Ponder / Flywheel class names to their Create Fly equivalents.

Usage: create_class_map.py <createfly class list> <source dirs...>
Prints, for every such class imported by the given sources, the Create Fly candidates:
  exact  - same relative package under com.zurrtum.create(.client)
  moved  - found only by simple name elsewhere
  missing- no class with that simple name
"""
import os
import re
import sys
from collections import defaultdict

PREFIXES = {
    'com.simibubi.create.': ['com.zurrtum.create.', 'com.zurrtum.create.client.'],
    'net.createmod.catnip.': ['com.zurrtum.create.catnip.', 'com.zurrtum.create.client.catnip.'],
    'net.createmod.ponder.': ['com.zurrtum.create.ponder.', 'com.zurrtum.create.client.ponder.'],
    'dev.engine_room.flywheel.': ['com.zurrtum.create.client.flywheel.'],
    'dev.engine_room.vanillin.': ['com.zurrtum.create.client.vanillin.'],
}

classes = [line.strip() for line in open(sys.argv[1]) if line.strip()]
class_set = set(classes)
by_simple = defaultdict(list)
for c in classes:
    by_simple[c.rsplit('.', 1)[1]].append(c)

imports = defaultdict(set)
pattern = re.compile(r'^import\s+(?:static\s+)?([\w.]+)\s*;', re.M)
for root_dir in sys.argv[2:]:
    for dirpath, _, files in os.walk(root_dir):
        for f in files:
            if f.endswith('.java'):
                path = os.path.join(dirpath, f)
                for m in pattern.finditer(open(path, encoding='utf-8').read()):
                    imports[m.group(1)].add(path)

def resolve(name):
    for prefix, targets in PREFIXES.items():
        if name.startswith(prefix):
            rest = name[len(prefix):]
            # static imports / nested classes: shorten until a class matches
            parts = rest.split('.')
            for n in range(len(parts), 0, -1):
                rel = '.'.join(parts[:n])
                for t in targets:
                    if t + rel in class_set:
                        return 'exact', [t + rel + ('.' + '.'.join(parts[n:]) if n < len(parts) else '')]
            # by simple name (first capitalised segment)
            cls = next((p for p in parts if p[:1].isupper()), parts[-1])
            cands = by_simple.get(cls, [])
            return ('moved' if cands else 'missing'), cands
    return None, None

for name in sorted(imports):
    kind, cands = resolve(name)
    if kind is None:
        continue
    print(f'{kind}\t{name}\t{" | ".join(cands)}\t{len(imports[name])}')
