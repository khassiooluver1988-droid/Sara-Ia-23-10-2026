from io import BytesIO
from pathlib import Path
import cairosvg
from PIL import Image, ImageOps

root = Path(__file__).resolve().parent.parent
icon_path = root / "assets" / "icone-rebeca-android.png"
logo_path = root / "assets" / "villa-europa-logo.svg"
dst = root / "android-app" / "app" / "src" / "main" / "res" / "drawable" / "rebeca_logo.png"
dst.parent.mkdir(parents=True, exist_ok=True)

if icon_path.is_file():
    icon = Image.open(icon_path).convert("RGBA")
    icon = ImageOps.fit(icon, (512, 512), Image.Resampling.LANCZOS, centering=(0.5, 0.5))
    icon.save(dst, "PNG", optimize=True)
    print(f"Ícone Android gerado da arte enviada: {icon_path}")
else:
    png = cairosvg.svg2png(url=str(logo_path), output_width=920)
    logo = Image.open(BytesIO(png)).convert("RGBA")
    logo.thumbnail((440, 320), Image.Resampling.LANCZOS)
    canvas = Image.new("RGBA", (512, 512), (255, 255, 255, 255))
    position = ((512 - logo.width) // 2, (512 - logo.height) // 2)
    canvas.alpha_composite(logo, position)
    canvas.save(dst, "PNG", optimize=True)
    print("ATENÇÃO: a arte aprovada do ícone não foi encontrada em assets/icone-rebeca-android.png; usando apenas a marca institucional provisória.")

print(f"Ícone: {dst}")
