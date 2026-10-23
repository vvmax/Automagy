#!/usr/bin/env python3
"""Reproduce SRG -> MCP bytecode remapping and Java decompilation (Java 17+)."""
import argparse
from pathlib import Path
import shutil
import subprocess
import zipfile

ROOT = Path(__file__).resolve().parent.parent


def run(command, log=None):
    if log:
        with (ROOT / 'logs' / log).open('w') as stream:
            subprocess.run(command, cwd=ROOT, check=True, stdout=stream, stderr=subprocess.STDOUT)
    else:
        subprocess.run(command, cwd=ROOT, check=True)


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--minecraft-jar', required=True, type=Path,
                        help='Minecraft 1.7.10 + Forge 1614 in MCP stable 12 namespace')
    parser.add_argument('--runtime', required=True, type=Path, help='JDK 8 directory for decompiler type information')
    parser.add_argument('--java', default='java', help='Java 17+ executable')
    parser.add_argument('--javac', default='javac', help='JDK 17+ compiler')
    args = parser.parse_args()
    for directory in ('logs', 'build', 'src/main/java', 'src/main/resources'):
        (ROOT / directory).mkdir(parents=True, exist_ok=True)
    run([args.javac, '-cp', 'tools/*', 'tools/RemapJar.java', 'tools/VerifySources.java'])
    for original, output, log in (
        ('input/Automagy-1.7.10-0.28.2.jar', 'Automagy-1.7.10-0.28.2-mcp.jar', 'remap.log'),
        ('input/Thaumcraft-1.7.10-4.2.3.5.jar', 'build/Thaumcraft-1.7.10-4.2.3.5-mcp.jar', 'remap-thaumcraft.log'),
    ):
        run([args.java, '-cp', 'tools:tools/*', 'RemapJar', original, output, 'mappings/members.tsv'], log)
    # Temporary output is replaced on each run; sources never become decompiler inputs.
    destination = ROOT / 'build/decompiled-final'
    if destination.exists():
        shutil.rmtree(destination)
    run([args.java, '-Xmx2G', '-jar', 'tools/vineflower-1.11.1.jar', '--folder',
         '--log-level=WARN', '--thread-count=4', '--indent-string=    ',
         '--include-runtime=' + str(args.runtime.resolve()),
         '--add-external=' + str(args.minecraft_jar.resolve()),
         '--add-external=build/Thaumcraft-1.7.10-4.2.3.5-mcp.jar',
         'Automagy-1.7.10-0.28.2-mcp.jar', str(destination)], 'decompile-final.log')
    for source in sorted(destination.rglob('*')):
        if source.is_file():
            relative = source.relative_to(destination)
            base = ROOT / ('src/main/java' if source.suffix == '.java' else 'src/main/resources')
            target = base / relative
            target.parent.mkdir(parents=True, exist_ok=True)
            shutil.copyfile(source, target)
    run([args.java, '-cp', 'tools', 'VerifySources', 'src/main/java'], 'syntax.log')
    with zipfile.ZipFile(ROOT / 'Automagy-1.7.10-0.28.2-sources.jar', 'w', zipfile.ZIP_DEFLATED) as archive:
        for folder in ('src/main/java', 'src/main/resources'):
            base = ROOT / folder
            for source in sorted(base.rglob('*')):
                if source.is_file():
                    archive.write(source, source.relative_to(base).as_posix())
    print('Created src/main/java, src/main/resources and Automagy-1.7.10-0.28.2-sources.jar')


if __name__ == '__main__':
    main()
