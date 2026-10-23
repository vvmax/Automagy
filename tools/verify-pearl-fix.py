#!/usr/bin/env python3
"""Verify that the installable jar changes only the two pearl classes."""
import hashlib
import json
from pathlib import Path
import zipfile
from verify import class_info

root = Path(__file__).resolve().parent.parent
original = root / 'input/Automagy-1.7.10-0.28.2.jar'
patched = root / 'build/libs/Automagy-1.7.10-0.28.2-pearl-fix.1.jar'
expected = {
    'tuhljin/automagy/entities/EntityAvaricePearl.class',
    'tuhljin/automagy/entities/EntityAvaricePearl$EntitySelectorItem.class',
}
with zipfile.ZipFile(original) as before, zipfile.ZipFile(patched) as after:
    entries = {n for n in before.namelist() if not n.endswith('/')}
    assert entries == {n for n in after.namelist() if not n.endswith('/')}
    changed = {n for n in entries if before.read(n) != after.read(n)}
    assert changed == expected, changed
    assert after.testzip() is None
    selector_refs, selector_declarations, _, _ = class_info(after.read(
        'tuhljin/automagy/entities/EntityAvaricePearl$EntitySelectorItem.class'))
    assert 'func_82704_a' in {n for _, _, n, _ in selector_declarations}
    assert {'func_70089_S', 'func_92059_d', 'field_77994_a'} <= {n for n, _ in selector_refs}
    classes = sum(n.endswith('.class') for n in entries)
    report = {
        'file': patched.name,
        'sha256': hashlib.sha256(patched.read_bytes()).hexdigest(),
        'classes': classes,
        'unchanged_classes': classes - len(changed),
        'unchanged_resources': sum(not n.endswith('.class') for n in entries),
        'changed_entries': sorted(changed),
        'srg_names_verified': True,
    }
    print(json.dumps(report, indent=2))
