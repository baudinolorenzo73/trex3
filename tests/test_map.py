import sys
import tempfile
import unittest
import zipfile
from pathlib import Path
sys.path.insert(0, str(Path(__file__).resolve().parents[1] / "scripts"))
from check_map import inspect_map

class MapTest(unittest.TestCase):
    def make(self, entries):
        folder = tempfile.TemporaryDirectory()
        self.addCleanup(folder.cleanup)
        path = Path(folder.name) / "map.zip"
        with zipfile.ZipFile(path, "w") as archive:
            for name, value in entries:
                archive.writestr(name, value)
        return path

    def test_native_tiles(self):
        info = inspect_map(self.make([("11/1066/737.img", b"map data")]))
        self.assertEqual(info["tileCount"], 1)
        self.assertEqual(len(info["sha256"]), 64)

    def test_unexpected_paths(self):
        for path in ["../evil.img", "11/1/../../evil.img", "12/1/1.img", "11/2048/1.img"]:
            with self.subTest(path=path):
                with self.assertRaises(ValueError):
                    inspect_map(self.make([(path, b"x")]))

    def test_empty(self):
        with self.assertRaises(ValueError):
            inspect_map(self.make([]))
        with self.assertRaises(ValueError):
            inspect_map(self.make([("11/1/1.img", b"")]))

if __name__ == "__main__":
    unittest.main()
