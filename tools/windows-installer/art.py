"""Draws the installer's side picture (welcome/finish pages) and header picture."""
import sys
from PIL import Image, ImageDraw, ImageFont, ImageFilter

icon_png, out_dir = sys.argv[1], sys.argv[2]
INTER = "/usr/share/fonts/opentype/inter/"
GREEN_TOP, GREEN_BOTTOM = (0x2E, 0x7D, 0x32), (0x1A, 0x5E, 0x24)


def font(style, size):
    return ImageFont.truetype(INTER + "Inter-" + style + ".otf", size)


# Side picture: drawn at 2x (328 x 628) so it stays sharp on high-DPI screens.
W, H = 328, 628
side = Image.new("RGB", (W, H))
px = side.load()
for y in range(H):
    t = y / (H - 1)
    c = tuple(round(a + (b - a) * t) for a, b in zip(GREEN_TOP, GREEN_BOTTOM))
    for x in range(W):
        px[x, y] = c

rings = Image.new("RGBA", (W, H), (0, 0, 0, 0))
d = ImageDraw.Draw(rings)
d.ellipse((150, -90, 470, 230), outline=(255, 255, 255, 34), width=26)
d.ellipse((-140, 420, 150, 710), outline=(255, 255, 255, 26), width=22)
d.ellipse((230, 520, 300, 590), fill=(255, 255, 255, 22))
side = Image.alpha_composite(side.convert("RGBA"), rings)

icon = Image.open(icon_png).convert("RGBA").resize((132, 132), Image.LANCZOS)
shadow = Image.new("RGBA", (W, H), (0, 0, 0, 0))
mask = Image.new("L", (132, 132), 0)
ImageDraw.Draw(mask).rounded_rectangle((0, 0, 131, 131), radius=30, fill=110)
shadow.paste((0, 0, 0, 255), ((W - 132) // 2, 168), mask)
shadow = shadow.filter(ImageFilter.GaussianBlur(10))
side = Image.alpha_composite(side, shadow)
side.alpha_composite(icon, ((W - 132) // 2, 156))

d = ImageDraw.Draw(side)
title = font("Bold", 30)
for i, line in enumerate(["Computer-Aided", "Solutions"]):
    w = d.textlength(line, font=title)
    d.text(((W - w) / 2, 318 + i * 38), line, font=title, fill="white")
sub = font("Medium", 17)
for i, line in enumerate(["for Common Everyday", "Problems"]):
    w = d.textlength(line, font=sub)
    d.text(((W - w) / 2, 410 + i * 24), line, font=sub, fill=(220, 238, 221))
side.convert("RGB").save(out_dir + "/welcome.bmp")

# Header picture (top right of the inner pages): 300 x 114, white with the icon.
HW, HH = 300, 114
head = Image.new("RGBA", (HW, HH), (255, 255, 255, 255))
small = Image.open(icon_png).convert("RGBA").resize((84, 84), Image.LANCZOS)
head.alpha_composite(small, (HW - 84 - 22, (HH - 84) // 2))
head.convert("RGB").save(out_dir + "/header.bmp")
print("art ok")
