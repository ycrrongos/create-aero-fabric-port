#!/usr/bin/env python3
"""Builds an index of top-level class names from source trees (for import fixing)."""
import os, sys, json

def index_tree(root, out):
    for dirpath, _, files in os.walk(root):
        for f in files:
            if f.endswith('.java'):
                rel = os.path.relpath(os.path.join(dirpath, f), root)
                fqn = rel[:-5].replace(os.sep, '.')
                out.setdefault(fqn.rsplit('.', 1)[-1], []).append(fqn)

if __name__ == '__main__':
    out = {}
    for root in sys.argv[2:]:
        index_tree(root, out)
    with open(sys.argv[1], 'w') as fp:
        json.dump(out, fp)
    print(len(out), 'simple names')
