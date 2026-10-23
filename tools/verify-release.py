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
patched = root / 'build/libs/Automagy-1.7.10-0.28.6.jar'
expected = {
    'entities/EntityAvaricePearl',
    'entities/EntityAvaricePearl$EntitySelectorItem',
    'entities/EntityItemDelayedFall',
    'gui/ContainerPortableArcaneCrafting',
    'gui/FakeTileMagicWorkbench',
    'items/ItemBucketCustom',
    'gui/ContainerAutoHandMirror',
    'network/MessageGUIInventariumRequest',
    'network/MessageKeyPressed',
    'tiles/TileEntityVisReader',
    'tiles/TileEntityMirrorMultiDest',
    'blocks/BlockMirrorMultiDest',
    'items/ItemNetherHeart',
    'lib/inventory/MirrorspaceItems',
    'lib/inventory/MappedItemsOrderedByTimeAdded',
    'lib/inventory/MappedItemsWithGroupedCounts',
    'blocks/BlockThaumostaticRefuelerSpire',
    'tiles/TileEntityEssentiaLocus',
    'lib/TjUtil',
    'lib/events/AutomagyEventHandler',
    'tiles/TileEntityInventarium',
    'tiles/TileEntityInventarium$PairedContentsProvider',
    'tiles/TileEntityThaumostaticController',
    'tiles/TileEntitySpecialProcess',
    'tiles/TileEntityThirstyTank',
    'tiles/TileEntityThirstyTank$SourceLiquidResult',
    'tiles/TileEntityGolemWorkbench',
    'tiles/TileEntityGolemWorkbench$EntitySelectorCraftingGolem',
}
expected = {'tuhljin/automagy/' + name + '.class' for name in expected}
added = {'tuhljin/automagy/lib/DeferredItemDrops.class'}
manifest = json.loads((root / 'provenance.json').read_text())
source_hash = next(entry['sha256'] for entry in manifest['inputs']
                   if entry['path'] == 'input/Automagy-1.7.10-0.28.2.jar')
assert hashlib.sha256(original.read_bytes()).hexdigest() == source_hash
with zipfile.ZipFile(original) as before, zipfile.ZipFile(patched) as after:
    entries = {name for name in before.namelist() if not name.endswith('/')}
    assert entries | added == {name for name in after.namelist() if not name.endswith('/')}
    assert len(after.namelist()) == len(set(after.namelist())), 'Duplicate ZIP entries'
    changed = {name for name in entries if before.read(name) != after.read(name)}
    assert changed == expected, changed
    assert after.testzip() is None
    for name in changed | added:
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
    heart = after.read(prefix + 'items/ItemNetherHeart.class')
    assert b'onPlayerMultiBlockPlace' in heart and b'onPlayerBlockPlace' in heart
    request = after.read(prefix + 'network/MessageGUIInventariumRequest.class')
    assert b'ContainerScribe' in request and b'canRequestItems' in request and b'getRequesterCoord' in request
    refs, declarations, _, _ = class_info(after.read(prefix + 'tiles/TileEntityEssentiaLocus.class'))
    assert 'func_145845_h' in {name for _, _, name, _ in declarations}
    assert {'func_145837_r', 'func_145843_s', 'func_147438_o', 'func_147439_a', 'func_147468_f'} <= {name for name, _ in refs}
    assert 'func_145838_q' not in {name for name, _ in refs}, 'Locus must not drop a cached block type'
    refs, declarations, _, _ = class_info(after.read(prefix + 'blocks/BlockThaumostaticRefuelerSpire.class'))
    assert 'func_149749_a' in {name for _, _, name, _ in declarations}
    assert {'func_72805_g', 'func_147468_f'} <= {name for name, _ in refs}
    assert b'AutomagyDeferredDrops' in after.read(prefix + 'lib/DeferredItemDrops.class')
    assert b'onDeferredDropsTick' in after.read(prefix + 'lib/events/AutomagyEventHandler.class')
    assert b'DeferredItemDrops' in after.read(prefix + 'lib/TjUtil.class')
    for name in changed | added:
        assert b'dreamcraft' not in after.read(name), name
    classes = sum(name.endswith('.class') for name in entries)
    report = {
        'file': patched.name,
        'sha256': hashlib.sha256(patched.read_bytes()).hexdigest(),
        'original_sha256': source_hash,
        'classes': classes + len(added),
        'unchanged_classes': classes - len(changed),
        'unchanged_resources': sum(not name.endswith('.class') for name in entries),
        'changed_entries': sorted(changed),
        'added_entries': sorted(added),
        'java_class_version': 52,
        'srg_names_verified': True,
        'server_dispenser_constructor_present': True,
        'external_dreamcraft_dependency': False,
    }
    print(json.dumps(report, indent=2))
