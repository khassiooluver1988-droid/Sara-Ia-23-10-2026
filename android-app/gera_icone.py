from io import BytesIO
from pathlib import Path
import cairosvg
from PIL import Image

root = Path(__file__).resolve().parent.parent
src = root / "assets" / "villa-europa-logo.svg"
dst = root / "android-app" / "app" / "src" / "main" / "res" / "drawable" / "rebeca_logo.png"
dst.parent.mkdir(parents=True, exist_ok=True)

png = cairosvg.svg2png(url=str(src), output_width=920)
logo = Image.open(BytesIO(png)).convert("RGBA")
logo.thumbnail((440, 320), Image.Resampling.LANCZOS)
canvas = Image.new("RGBA", (512, 512), (255, 255, 255, 255))
position = ((512 - logo.width) // 2, (512 - logo.height) // 2)
canvas.alpha_composite(logo, position)
canvas.convert("RGB").save(dst, "PNG", optimize=True)
print(f"Ícone gerado: {dst}")
