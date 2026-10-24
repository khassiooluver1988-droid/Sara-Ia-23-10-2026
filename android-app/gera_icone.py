#!/usr/bin/env python3
"""Prepara o ícone oficial da Rebeca para o APK sem redesenhar a arte."""
from pathlib import Path
from PIL import Image, ImageOps

ROOT = Path(__file__).resolve().parent.parent
SOURCE = ROOT / "assets" / "icone-rebeca-android-oficial.png"
DEST_DIR = ROOT / "android-app" / "app" / "src" / "main" / "res"

if not SOURCE.is_file():
    raise SystemExit(
        "ERRO: imagem oficial do aplicativo ausente. "
        "Envie o PNG recebido em assets/icone-rebeca-android-oficial.png. "
        "O APK não será publicado com ícone substituto."
    )

with Image.open(SOURCE) as original:
    if original.format != "PNG":
        raise SystemExit("O ícone oficial deve ser um arquivo PNG.")
    icon = ImageOps.exif_transpose(original).convert("RGBA")
    if min(icon.size) < 384:
        raise SystemExit("A imagem deve ter pelo menos 384 px em cada lado.")

    # Não corta personagem, logomarca, texto, fundo nem detalhes.
    # Uma arte quadrada é redimensionada preservando seu enquadramento integral.
    for qualifier, pixels in [
        ("mipmap-mdpi", 48),
        ("mipmap-hdpi", 72),
        ("mipmap-xhdpi", 96),
        ("mipmap-xxhdpi", 144),
        ("mipmap-xxxhdpi", 192),
    ]:
        folder = DEST_DIR / qualifier
        folder.mkdir(parents=True, exist_ok=True)
        resized = ImageOps.contain(icon, (pixels, pixels), Image.Resampling.LANCZOS)
        if resized.size != (pixels, pixels):
            canvas = Image.new("RGBA", (pixels, pixels), "white")
            canvas.alpha_composite(resized, ((pixels-resized.width)//2, (pixels-resized.height)//2))
            resized = canvas
        resized.save(folder / "ic_launcher.png", "PNG", optimize=True)

    # Recurso sem reamostragem intermediária para a imagem completa do app.
    full_dir = DEST_DIR / "drawable-nodpi"
    full_dir.mkdir(parents=True, exist_ok=True)
    full = ImageOps.contain(icon, (512, 512), Image.Resampling.LANCZOS)
    if full.size != (512, 512):
        canvas = Image.new("RGBA", (512, 512), "white")
        canvas.alpha_composite(full, ((512-full.width)//2, (512-full.height)//2))
        full = canvas
    full.save(full_dir / "rebeca_logo.png", "PNG", optimize=True)

print("Ícone oficial validado e convertido para todas as densidades Android.")
