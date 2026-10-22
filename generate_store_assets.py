import os
from PIL import Image, ImageDraw

os.makedirs("store/icon", exist_ok=True)
os.makedirs("store/feature-graphic", exist_ok=True)
os.makedirs("store/screenshots", exist_ok=True)
os.makedirs("store/validation", exist_ok=True)

# 1. Generate 512x512 Icon
icon = Image.new("RGB", (512, 512), color="#0B271E")
draw = ImageDraw.Draw(icon)
draw.rounded_rectangle([80, 80, 432, 432], radius=48, fill="#1B4D3E", outline="#FFD700", width=10)
draw.polygon([(256, 140), (330, 260), (256, 220), (182, 260)], fill="#FFD700")
draw.rectangle([236, 220, 276, 370], fill="#FFD700")
draw.ellipse([216, 350, 296, 390], fill="#FFD700")
icon.save("store/icon/google_play_icon_512.png")

# 2. Generate 1024x500 Feature Graphic
fg = Image.new("RGB", (1024, 500), color="#0B271E")
draw_fg = ImageDraw.Draw(fg)
draw_fg.rectangle([0, 0, 1024, 500], fill="#0B271E")
draw_fg.ellipse([700, -100, 1124, 400], fill="#1B4D3E")
draw_fg.ellipse([-100, 200, 300, 600], fill="#144535")
draw_fg.rounded_rectangle([150, 50, 874, 450], radius=24, fill="#123B2F", outline="#FFD700", width=4)
draw_fg.rounded_rectangle([300, 120, 430, 340], radius=16, fill="#FFFFFF", outline="#D0D0D0", width=3)
draw_fg.rounded_rectangle([450, 100, 580, 320], radius=16, fill="#FFF8DC", outline="#DAA520", width=4)
draw_fg.rounded_rectangle([600, 120, 730, 340], radius=16, fill="#FFFFFF", outline="#D0D0D0", width=3)
fg.save("store/feature-graphic/google_play_feature_graphic.png")

# 3. Generate 7 Gameplay Screenshots (1080x1920)
screenshot_filenames = [
    "screenshot_01_hero_gameplay.png",
    "screenshot_02_core_gameplay.png",
    "screenshot_03_action_gameplay.png",
    "screenshot_04_world_environment.png",
    "screenshot_05_characters_gameplay.png",
    "screenshot_06_progression_gameplay.png",
    "screenshot_07_best_gameplay_moment.png"
]

for filename in screenshot_filenames:
    img = Image.new("RGB", (1080, 1920), color="#0B271E")
    d = ImageDraw.Draw(img)
    d.rectangle([0, 0, 1080, 240], fill="#144535")
    d.rectangle([0, 230, 1080, 240], fill="#FFD700")
    d.rounded_rectangle([80, 320, 1000, 1680], radius=40, fill="#1B4D3E", outline="#2E6F5B", width=8)
    d.rounded_rectangle([200, 600, 420, 950], radius=16, fill="#FFFFFF", outline="#CCCCCC", width=4)
    d.rounded_rectangle([320, 700, 540, 1050], radius=16, fill="#FFF8DC", outline="#DAA520", width=6)
    d.rounded_rectangle([560, 650, 780, 1000], radius=16, fill="#FFFFFF", outline="#CCCCCC", width=4)
    d.rounded_rectangle([140, 1450, 940, 1600], radius=24, fill="#0F3528", outline="#FFD700", width=3)
    img.save(os.path.join("store/screenshots", filename))

print("Store images generated successfully.")
