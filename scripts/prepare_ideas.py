import json
import pathlib
import shutil
import sys
from PIL import Image

source, target = map(pathlib.Path, sys.argv[1:])
target.mkdir(parents=True, exist_ok=True)
catalog = json.loads((source / "catalog.json").read_text(encoding="utf-8"))
items = [item for group in catalog for item in group["images"]]
assert len(items) == 22
for old in target.glob("*.webp"):
    old.unlink()
before = after = 0
for item in items:
    path = source / item["file"]
    dest = target / (path.stem + ".webp")
    with Image.open(path) as original:
        image = original.convert("RGBA" if "A" in original.getbands() or "transparency" in original.info else "RGB")
        image.thumbnail((2048, 2048), Image.Resampling.LANCZOS)
        image.save(dest, "WEBP", quality=88, method=6)
    before += path.stat().st_size
    after += dest.stat().st_size
shutil.copyfile(source / "catalog.json", target / "catalog.json")
print(f"Catalog: {before / 1048576:.2f} MiB PNG -> {after / 1048576:.2f} MiB WebP")
