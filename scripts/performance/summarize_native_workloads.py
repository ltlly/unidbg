#!/usr/bin/env python3
"""Summarize raw paired samples without discarding any successful sample."""
import collections
import json
import re
import statistics
import sys

samples = collections.defaultdict(list)
current = None
for line in open(sys.argv[1]):
    if line.startswith('SAMPLE '):
        current = dict(re.findall(r'(\w+)=(\S+)', line))
    elif line.startswith('RESULT '):
        values = dict(re.findall(r'(\w+)=(\S+)', line))
        current.update({'mode': values['mode'], 'count': values['count']})
        current.update({key: float(values[key]) for key in ('ns_per_op', 'bytes_per_op')})
    elif line.startswith('{'):
        values = json.loads(line)
        if values['exit_code'] != 0:
            raise RuntimeError('Failed sample: ' + str(current))
        current.update(values)
        samples[(current['arm64'], current['mode'], int(current['count']), current['phase'])].append(current)
output = []
for arm64, mode, count in sorted(set((k[0], k[1], k[2]) for k in samples)):
    before = samples[(arm64, mode, count, 'baseline')]
    after = samples[(arm64, mode, count, 'candidate')]
    if len(before) != 5 or len(after) != 5:
        raise RuntimeError('Expected five independent runs per variant')
    median = lambda group, key: statistics.median(x[key] for x in group)
    output.append({'arm64': arm64 == 'true', 'mode': mode, 'count': count,
        'baseline_ns': median(before, 'ns_per_op'), 'candidate_ns': median(after, 'ns_per_op'),
        'speedup': median(before, 'ns_per_op') / median(after, 'ns_per_op'),
        'baseline_bytes': median(before, 'bytes_per_op'), 'candidate_bytes': median(after, 'bytes_per_op'),
        'baseline_rss_kib': median(before, 'peak_rss_kib'), 'candidate_rss_kib': median(after, 'peak_rss_kib'),
        'rss_ratio': median(after, 'peak_rss_kib') / median(before, 'peak_rss_kib')})
print(json.dumps(output, indent=2))
