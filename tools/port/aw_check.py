#!/usr/bin/env python3
"""Validate an access widener against the named (Mojmap) Minecraft jar.

Usage: aw_check.py <file.accesswidener> [minecraft-merged-named.jar]

Reports every entry whose class, field or method does not exist, so stale
entries from older Minecraft versions can be ported instead of silently
breaking Loom.
"""
import glob
import struct
import sys
import zipfile


def parse_class(data):
    pos = 10
    count = struct.unpack('>H', data[8:10])[0]
    cp = [None] * count
    i = 1
    while i < count:
        tag = data[pos]
        if tag == 1:
            ln = struct.unpack('>H', data[pos + 1:pos + 3])[0]
            cp[i] = data[pos + 3:pos + 3 + ln].decode('utf-8', 'replace')
            pos += 3 + ln
        elif tag in (3, 4):
            pos += 5
        elif tag in (5, 6):
            pos += 9
            i += 1
        elif tag in (7, 8, 16, 19, 20):
            pos += 3
        elif tag == 15:
            pos += 4
        elif tag in (9, 10, 11, 12, 17, 18):
            pos += 5
        else:
            raise ValueError('bad tag %d' % tag)
        i += 1
    pos += 6
    icount = struct.unpack('>H', data[pos:pos + 2])[0]
    pos += 2 + 2 * icount

    def members(pos):
        n = struct.unpack('>H', data[pos:pos + 2])[0]
        pos += 2
        out = set()
        for _ in range(n):
            _, ni, di, ac = struct.unpack('>HHHH', data[pos:pos + 8])
            pos += 8
            out.add((cp[ni], cp[di]))
            for _ in range(ac):
                ln = struct.unpack('>I', data[pos + 2:pos + 6])[0]
                pos += 6 + ln
        return out, pos

    fields, pos = members(pos)
    methods, pos = members(pos)
    return fields, methods


def main():
    aw = sys.argv[1]
    jar = sys.argv[2] if len(sys.argv) > 2 else sorted(glob.glob(
        '/root/.gradle/caches/fabric-loom/minecraftMaven/net/minecraft/minecraft-merged/1.21.11-*/minecraft-merged-1.21.11-*[0-9].jar'))[0]
    z = zipfile.ZipFile(jar)
    cache = {}

    def get(cls):
        if cls not in cache:
            try:
                cache[cls] = parse_class(z.read(cls + '.class'))
            except KeyError:
                cache[cls] = None
        return cache[cls]

    bad = 0
    for n, line in enumerate(open(aw), 1):
        line = line.split('#', 1)[0].strip()
        if not line or line.startswith('accessWidener'):
            continue
        parts = line.split()
        kind = parts[1]
        cls = parts[2]
        info = get(cls)
        if info is None:
            print('%d: missing class %s' % (n, cls))
            bad += 1
            continue
        if kind == 'field' and (parts[3], parts[4]) not in info[0]:
            print('%d: missing field %s.%s %s' % (n, cls, parts[3], parts[4]))
            bad += 1
        elif kind == 'method' and (parts[3], parts[4]) not in info[1]:
            cands = [d for (nm, d) in info[1] if nm == parts[3]]
            print('%d: missing method %s.%s%s (have: %s)' % (n, cls, parts[3], parts[4], ', '.join(cands) or 'none'))
            bad += 1
    print('%d problem(s)' % bad)
    sys.exit(1 if bad else 0)


if __name__ == '__main__':
    main()
