#!/usr/bin/env python3
"""Download pinned original jars, preserving proxy and TLS configuration."""
import hashlib
import json
from pathlib import Path
import urllib.request

root = Path(__file__).resolve().parent.parent
manifest = json.loads((root / 'provenance.json').read_text())
for entry in manifest['inputs']:
    target = root / entry['path']
    target.parent.mkdir(parents=True, exist_ok=True)
    if target.exists():
        assert hashlib.sha256(target.read_bytes()).hexdigest() == entry['sha256'], target
        print('Verified', target.name)
        continue
    temporary = target.with_suffix(target.suffix + '.part')
    try:
        with urllib.request.urlopen(entry['url'], timeout=90) as response:
            data = response.read()
        assert hashlib.sha256(data).hexdigest() == entry['sha256'], target
        temporary.write_bytes(data)
        temporary.replace(target)
        print('Downloaded and verified', target.name)
    finally:
        temporary.unlink(missing_ok=True)
