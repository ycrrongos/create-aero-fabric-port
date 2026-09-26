#!/usr/bin/env python3
"""
Mechanical 1.21.1 -> 1.21.11 (Mojmap) port helper.

* Applies explicit class/package renames (ResourceLocation -> Identifier, ...)
* Re-targets imports of classes that moved packages, when the simple name is unique in the index.

Only touches import lines and a small set of well known identifiers, never arbitrary code.
"""
import json, os, re, sys

RENAMES = {
    'net.minecraft.resources.ResourceLocation': 'net.minecraft.resources.Identifier',
    'net.minecraft.Util': 'net.minecraft.util.Util',
    'net.minecraft.FileUtil': 'net.minecraft.util.FileUtil',
    'net.minecraft.client.renderer.RenderType': 'net.minecraft.client.renderer.rendertype.RenderType',
    'net.minecraft.world.ItemInteractionResult': 'net.minecraft.world.InteractionResult',
}
PACKAGE_RENAMES = {
    'net.minecraft.advancements.critereon.': 'net.minecraft.advancements.criterion.',
}
IDENT_RENAMES = [
    (re.compile(r'\bResourceLocation\b'), 'Identifier'),
    (re.compile(r'\bItemInteractionResult\b'), 'InteractionResult'),
]
IMPORT_RE = re.compile(r'^(import\s+(?:static\s+)?)([\w.]+?)(\.\*)?;\s*$', re.M)


def top_level_exists(fqn, known):
    parts = fqn.split('.')
    for i in range(len(parts), 0, -1):
        if '.'.join(parts[:i]) in known:
            return '.'.join(parts[:i]), parts[i:]
    return None, None


def main():
    index = json.load(open(sys.argv[1]))
    known = set()
    for names in index.values():
        known.update(names)
    packages = {n.rsplit('.', 1)[0] for n in known}
    changed = 0
    unresolved = {}
    for root in sys.argv[2:]:
        for dirpath, _, files in os.walk(root):
            for f in files:
                if not f.endswith('.java'):
                    continue
                path = os.path.join(dirpath, f)
                src = open(path, encoding='utf-8').read()
                orig = src

                def repl(m):
                    prefix, name, star = m.group(1), m.group(2), m.group(3) or ''
                    for a, b in PACKAGE_RENAMES.items():
                        if name.startswith(a):
                            name = b + name[len(a):]
                    for a, b in RENAMES.items():
                        if name == a or name.startswith(a + '.'):
                            name = b + name[len(a):]
                    if not (name.startswith('net.minecraft') or name.startswith('com.mojang')):
                        return prefix + name + star + ';'
                    if star:
                        if name in packages or name in known:
                            return prefix + name + star + ';'
                        return prefix + name + star + ';'
                    top, rest = top_level_exists(name, known)
                    is_static = 'static' in prefix
                    if top is not None:
                        return prefix + name + ';'
                    # try to find the moved class
                    parts = name.split('.')
                    # guess top-level class = first capitalized segment
                    idx = next((i for i, p in enumerate(parts) if p[:1].isupper()), len(parts) - 1)
                    simple = parts[idx]
                    cands = [c for c in index.get(simple, []) if c.startswith('net.minecraft') or c.startswith('com.mojang')]
                    if len(cands) == 1:
                        return prefix + cands[0] + ('.' + '.'.join(parts[idx + 1:]) if idx + 1 < len(parts) else '') + ';'
                    unresolved.setdefault(name, []).append(path)
                    return prefix + name + ';'

                src = IMPORT_RE.sub(repl, src)
                for rx, rep in IDENT_RENAMES:
                    src = rx.sub(rep, src)
                if src != orig:
                    open(path, 'w', encoding='utf-8').write(src)
                    changed += 1
    print('changed files:', changed)
    for k, v in sorted(unresolved.items()):
        print('UNRESOLVED', k, len(v))


if __name__ == '__main__':
    main()
