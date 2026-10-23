#!/usr/bin/env python3
"""Check archive coverage, unchanged resources/literals and MCP member names."""
import json
from pathlib import Path
import re
import struct
import zipfile

ROOT = Path(__file__).resolve().parent.parent
NAMES = dict(line.split('\t') for line in (ROOT / 'mappings/members.tsv').read_text().splitlines())


def class_info(data):
    assert data[:4] == b'\xca\xfe\xba\xbe'
    count = struct.unpack_from('>H', data, 8)[0]
    pool, pos, index = [None] * count, 10, 1
    while index < count:
        tag = data[pos]
        pos += 1
        if tag == 1:
            length = struct.unpack_from('>H', data, pos)[0]
            pos += 2
            pool[index] = (tag, data[pos:pos + length])
            pos += length
        elif tag in (7, 8, 16, 19, 20):
            pool[index] = (tag, struct.unpack_from('>H', data, pos)[0])
            pos += 2
        elif tag in (3, 4, 9, 10, 11, 12, 17, 18):
            pool[index] = (tag, *struct.unpack_from('>HH', data, pos))
            pos += 4
        elif tag in (5, 6):
            pool[index] = (tag, data[pos:pos + 8])
            pos += 8
            index += 1
        elif tag == 15:
            pool[index] = (tag, *struct.unpack_from('>BH', data, pos))
            pos += 3
        else:
            raise ValueError(tag)
        index += 1

    def text(index):
        return pool[index][1].decode('utf-8', 'replace')

    member_refs = {(text(p[1]), text(p[2])) for p in pool if p and p[0] == 12}
    literals = {pool[p[1]][1] for p in pool if p and p[0] == 8}
    classes = {text(p[1]) for p in pool if p and p[0] == 7}
    # Class access, this, super, interfaces; then fields and methods.
    interfaces = struct.unpack_from('>H', data, pos + 6)[0]
    pos += 8 + 2 * interfaces
    declarations = []
    for kind in ('field', 'method'):
        total = struct.unpack_from('>H', data, pos)[0]
        pos += 2
        for _ in range(total):
            access, name, descriptor, attributes = struct.unpack_from('>HHHH', data, pos)
            pos += 8
            declarations.append((kind, access, text(name), text(descriptor)))
            for _ in range(attributes):
                length = struct.unpack_from('>I', data, pos + 2)[0]
                pos += 6 + length
    return member_refs, declarations, literals, classes


def main():
    class_count = resource_count = 0
    mapped, unresolved = set(), set()
    with zipfile.ZipFile(ROOT / 'input/Automagy-1.7.10-0.28.2.jar') as before, \
         zipfile.ZipFile(ROOT / 'Automagy-1.7.10-0.28.2-mcp.jar') as after:
        entries = {n for n in before.namelist() if not n.endswith('/')}
        assert entries == {n for n in after.namelist() if not n.endswith('/')}
        for name in sorted(entries):
            old, new = before.read(name), after.read(name)
            if not name.endswith('.class'):
                assert old == new, ('resource changed', name)
                resource_count += 1
                continue
            class_count += 1
            refs_a, decls_a, strings_a, classes_a = class_info(old)
            refs_b, decls_b, strings_b, classes_b = class_info(new)
            assert strings_a == strings_b, ('string literals changed', name)
            assert classes_a == classes_b, ('class references changed', name)
            assert {(NAMES.get(n, n), d) for n, d in refs_a} == refs_b, ('member references', name)
            assert [(k, a, NAMES.get(n, n), d) for k, a, n, d in decls_a] == decls_b, ('declarations', name)
            for member in {n for n, _ in refs_a} | {n for _, _, n, _ in decls_a}:
                if member in NAMES:
                    mapped.add(member)
                elif re.fullmatch(r'(?:func|field)_\d+_.*', member):
                    unresolved.add(member)

    java_root = ROOT / 'src/main/java'
    java_files = list(java_root.rglob('*.java'))
    with zipfile.ZipFile(ROOT / 'input/Automagy-1.7.10-0.28.2.jar') as archive:
        top_level = {n[:-6] + '.java' for n in archive.namelist() if n.endswith('.class') and '$' not in n}
    source_paths = {p.relative_to(java_root).as_posix() for p in java_files}
    assert top_level <= source_paths
    assert source_paths - top_level <= {'tuhljin/automagy/lib/DeferredItemDrops.java'}
    # Detect decompiler failure comments without mistaking the mod's own log messages for failures.
    markers = ('$VF:', "Couldn't be decompiled", 'could not be decompiled')
    for path in java_files:
        source = path.read_text()
        assert not any(marker in source for marker in markers), path

    report = dict(classes=class_count, java_files=len(java_files), resources_unchanged=resource_count,
                  mapped_srg_members=len(mapped), unmapped_srg_members=sorted(unresolved),
                  string_literals_preserved=True, member_references_verified=True,
                  top_level_source_coverage='100%', decompiler_failure_markers=0,
                  validation_scope='Archive integrity, mapped members, literals, source coverage; see logs/syntax.log for syntax. No full typecheck or game run.')
    (ROOT / 'logs/verification.json').write_text(json.dumps(report, ensure_ascii=False, indent=2) + '\n')
    print(json.dumps(report, ensure_ascii=False, indent=2))


if __name__ == '__main__':
    main()
