#!/usr/bin/env python3
"""Run ONE fresh JVM and report process RSS/CPU alongside benchmark output.

Set JAVA and UNIDBG_CLASSPATH to the chosen runtime and compiled reactor classpath.
Invoke from the repository root. Repeat in a parent shell for independent samples.
Linux/macOS only; ru_maxrss units are normalized below.
"""
import json
import os
import platform
import resource
import subprocess
import sys
import time

command = [os.environ.get('JAVA', 'java'), '-Xms128m', '-Xmx128m',
           '-cp', os.environ['UNIDBG_CLASSPATH'],
           'com.github.unidbg.performance.IntArrayBenchmark'] + sys.argv[1:]
start = time.monotonic()
result = subprocess.run(command, check=False)
usage = resource.getrusage(resource.RUSAGE_CHILDREN)
rss_kib = usage.ru_maxrss / 1024 if platform.system() == 'Darwin' else usage.ru_maxrss
print(json.dumps({'exit_code': result.returncode, 'wall_seconds': time.monotonic() - start,
                  'child_user_seconds': usage.ru_utime, 'child_system_seconds': usage.ru_stime,
                  'peak_rss_kib': rss_kib, 'host': platform.platform(),
                  'note': 'RSS includes JVM/emulator startup; allocation is reported separately.'}), flush=True)
sys.exit(result.returncode)
