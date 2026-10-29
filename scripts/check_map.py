"""Validate a native Zepp map ZIP without extracting untrusted paths."""
import argparse
import hashlib
import json
import re
import zipfile
from pathlib import Path

TILE = re.compile(r"11/([0-9]+)/([0-9]+)\.img$")

def inspect_map(path):
    with zipfile.ZipFile(path) as archive:
        entries = [e for e in archive.infolist() if not e.is_dir()]
        if not entries:
            raise ValueError("Nessuna tile nella mappa")
        if len({e.filename for e in entries}) != len(entries):
            raise ValueError("Tile duplicate")
        total = 0
        for entry in entries:
            match = TILE.fullmatch(entry.filename)
            if not match or any(int(n) >= 2048 for n in match.groups()):
                raise ValueError("Percorso tile inatteso: " + entry.filename)
            if entry.flag_bits & 1 or entry.file_size == 0:
                raise ValueError("Tile vuota o cifrata")
            total += entry.file_size
        if total > 128 * 1024 * 1024:
            raise ValueError("Mappa troppo grande per questa prima prova")
        if archive.testzip() is not None:
            raise ValueError("Controllo CRC non riuscito")
        result = {"tileCount": len(entries), "uncompressedBytes": total}
    result["sha256"] = hashlib.sha256(Path(path).read_bytes()).hexdigest()
    return result

if __name__ == "__main__":
    parser = argparse.ArgumentParser()
    parser.add_argument("map")
    parser.add_argument("--report", required=True)
    args = parser.parse_args()
    report = inspect_map(args.map)
    Path(args.report).write_text(json.dumps(report, indent=2) + "\n")
    print(json.dumps(report))
