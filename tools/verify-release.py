#!/usr/bin/env python3
"""Verify the installable patch jar against the pinned official Automagy release."""
import hashlib
import json
from pathlib import Path
import struct
import zipfile
from verify import class_info

root = Path(__file__).resolve().parent.parent
original = root / 'input/Automagy-1.7.10-0.28.2.jar'
patched = root / 'build/libs/Automagy-1.7.10-0.28.3.jar'
expected = {
    'entities/EntityAvaricePearl',
    'entities/EntityAvaricePearl$EntitySelectorItem',
    'entities/EntityItemDelayedFall',
    'gui/ContainerPortableArcaneCrafting',
    'gui/FakeTileMagicWorkbench',
    'items/ItemBucketCustom',
}
expected = {'tuhljin/automagy/' + name + '.class' for name in expected}
manifest = json.loads((root / 'provenance.json').read_text())
source_hash = next(entry['sha256'] for entry in manifest['inputs']
                   if entry['path'] == 'input/Automagy-1.7.10-0.28.2.jar')
assert hashlib.sha256(original.read_bytes()).hexdigest() == source_hash
with zipfile.ZipFile(original) as before, zipfile.ZipFile(patched) as after:
    entries = {name for name in before.namelist() if not name.endswith('/')}
    assert entries == {name for name in after.namelist() if not name.endswith('/')}
    assert len(after.namelist()) == len(set(after.namelist())), 'Duplicate ZIP entries'
    changed = {name for name in entries if before.read(name) != after.read(name)}
    assert changed == expected, changed
    assert after.testzip() is None
    for name in changed:
        assert struct.unpack_from('>H', after.read(name), 6)[0] == 52, name
    prefix = 'tuhljin/automagy/'
    refs, declarations, _, _ = class_info(after.read(prefix + 'entities/EntityAvaricePearl$EntitySelectorItem.class'))
    assert 'func_82704_a' in {name for _, _, name, _ in declarations}
    assert {'func_70089_S', 'func_92059_d', 'field_77994_a'} <= {name for name, _ in refs}
    pearl = after.read(prefix + 'entities/EntityAvaricePearl.class')
    assert b'cpw/mods/fml/relauncher/SideOnly' not in pearl
    assert ('method', 1, '<init>', '(Lnet/minecraft/world/World;DDDI)V') in class_info(pearl)[1]
    bucket = after.read(prefix + 'items/ItemBucketCustom.class')
    assert b'EventUtils' not in bucket and b'dreamcraft' not in bucket
    assert b'net/minecraftforge/event/ForgeEventFactory' in bucket
    refs, declarations, _, _ = class_info(after.read(prefix + 'gui/ContainerPortableArcaneCrafting.class'))
    assert {'func_75145_c', 'func_75144_a', 'func_75134_a'} <= {name for _, _, name, _ in declarations}
    classes = sum(name.endswith('.class') for name in entries)
    report = {
        'file': patched.name,
        'sha256': hashlib.sha256(patched.read_bytes()).hexdigest(),
        'original_sha256': source_hash,
        'classes': classes,
        'unchanged_classes': classes - len(changed),
        'unchanged_resources': sum(not name.endswith('.class') for name in entries),
        'changed_entries': sorted(changed),
        'java_class_version': 52,
        'srg_names_verified': True,
        'server_dispenser_constructor_present': True,
        'external_dreamcraft_dependency': False,
    }
    print(json.dumps(report, indent=2))
