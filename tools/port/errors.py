#!/usr/bin/env python3
"""Summarizes javac errors from a gradle log: unique (file, line, message) grouped by file."""
import re, sys, collections
log = open(sys.argv[1], encoding='utf-8', errors='replace').read().splitlines()
root = sys.argv[2] if len(sys.argv) > 2 else ''
errs = collections.OrderedDict()
i = 0
while i < len(log):
    m = re.match(r'^(/\S+\.java):(\d+): error: (.*)$', log[i])
    if m:
        f, ln, msg = m.group(1), int(m.group(2)), m.group(3)
        extra = []
        j = i + 1
        while j < len(log) and not re.match(r'^(/\S+\.java):(\d+): (error|warning)', log[j]) and j < i + 6:
            if 'symbol:' in log[j] or 'location:' in log[j] or 'required:' in log[j] or 'found:' in log[j]:
                extra.append(log[j].strip())
            j += 1
        key = (f, ln, msg)
        if key not in errs:
            errs[key] = extra
        i = j
    else:
        i += 1
byfile = collections.defaultdict(list)
for (f, ln, msg), extra in errs.items():
    byfile[f].append((ln, msg, extra))
print('TOTAL', len(errs), 'errors in', len(byfile), 'files')
for f in sorted(byfile, key=lambda k: -len(byfile[k])):
    print('==', f.replace(root, ''), len(byfile[f]))
    for ln, msg, extra in sorted(byfile[f])[:int(sys.argv[3]) if len(sys.argv) > 3 else 999]:
        print('  %d: %s %s' % (ln, msg, ' | '.join(e for e in extra if e.startswith('symbol'))))
