#!/usr/bin/env python3
"""Copies a mod jar without the Fabric-Loom-Version manifest attribute, so a mod built with a newer Loom
can be used as a dev-runtime dependency of this workspace's Loom. The classes are left untouched."""
import sys
import zipfile

src, dst = sys.argv[1], sys.argv[2]
with zipfile.ZipFile(src) as zin, zipfile.ZipFile(dst, 'w', zipfile.ZIP_DEFLATED) as zout:
    for item in zin.infolist():
        data = zin.read(item.filename)
        if item.filename == 'META-INF/MANIFEST.MF':
            lines = data.decode('utf-8').splitlines()
            data = ('\r\n'.join(l for l in lines if not l.startswith('Fabric-Loom-Version:')) + '\r\n').encode('utf-8')
        zout.writestr(item, data)
