from pathlib import Path
import struct, zlib
size = 240
rows = bytearray()
for y in range(size):
    rows.append(0)
    for x in range(size):
        dx, dy = x - 120, y - 120
        radius = dx*dx + dy*dy
        color = (22, 60, 44)
        if 75*75 <= radius <= 83*83:
            color = (228, 240, 232)
        if abs(dx) < 16 and -65 < dy < 55:
            color = (235, 88, 60) if dy < 0 else (240, 246, 241)
        rows.extend(color)
def chunk(kind, data):
    return struct.pack('>I', len(data)) + kind + data + struct.pack('>I', zlib.crc32(kind+data) & 0xffffffff)
png = b'\x89PNG\r\n\x1a\n' + chunk(b'IHDR', struct.pack('>IIBBBBB',size,size,8,2,0,0,0)) + chunk(b'IDAT', zlib.compress(bytes(rows))) + chunk(b'IEND',b'')
path=Path('zeppos/assets/default.r/icon.png')
path.parent.mkdir(parents=True,exist_ok=True)
path.write_bytes(png)
