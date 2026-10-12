from reportlab.lib.pagesizes import letter
from reportlab.pdfgen import canvas
from reportlab.pdfbase import pdfmetrics

output_path = r"output/pdf/the-lock-on-mod_one-page-summary.pdf"

PAGE_W, PAGE_H = letter
MARGIN = 44
CONTENT_W = PAGE_W - (2 * MARGIN)

c = canvas.Canvas(output_path, pagesize=letter)

def wrap_text(text, font_name, font_size, max_width):
    words = text.split()
    if not words:
        return [""]
    lines = []
    line = words[0]
    for word in words[1:]:
        trial = line + " " + word
        if pdfmetrics.stringWidth(trial, font_name, font_size) <= max_width:
            line = trial
        else:
            lines.append(line)
            line = word
    lines.append(line)
    return lines

def draw_heading(y, text):
    c.setFont("Helvetica-Bold", 11.5)
    c.drawString(MARGIN, y, text)
    return y - 14

def draw_paragraph(y, text, size=9.5, leading=11.5):
    c.setFont("Helvetica", size)
    for line in wrap_text(text, "Helvetica", size, CONTENT_W):
        c.drawString(MARGIN, y, line)
        y -= leading
    return y - 3

def draw_bullets(y, items, size=9.5, leading=11):
    c.setFont("Helvetica", size)
    bullet_w = pdfmetrics.stringWidth("- ", "Helvetica", size)
    item_width = CONTENT_W - bullet_w
    for item in items:
        wrapped = wrap_text(item, "Helvetica", size, item_width)
        c.drawString(MARGIN, y, "-")
        c.drawString(MARGIN + bullet_w, y, wrapped[0])
        y -= leading
        for cont in wrapped[1:]:
            c.drawString(MARGIN + bullet_w, y, cont)
            y -= leading
    return y - 2

y = PAGE_H - MARGIN

c.setFont("Helvetica-Bold", 16)
c.drawString(MARGIN, y, "The Lock On Mod - One-Page App Summary")
y -= 18
c.setFont("Helvetica", 8)
c.drawString(MARGIN, y, "Source basis: README.md, build.gradle, src/main/java, src/main/resources/mcmod.info")
y -= 16

# What it is
y = draw_heading(y, "What it is")
y = draw_paragraph(
    y,
    "Zelda Targeting is a Minecraft Forge mod for Minecraft 1.12.2 that adds Zelda-style lock-on combat with camera steering, HUD overlays, and sound feedback. "
    "Repo metadata identifies version 1.3.0 and a client-focused architecture registered through Forge mod lifecycle hooks."
)

# Who it's for
y = draw_heading(y, "Who it is for")
y = draw_paragraph(
    y,
    "Primary persona: Minecraft 1.12.2 players and modpack users who want action-game style target locking and configurable combat HUD/audio behavior."
)

# What it does
y = draw_heading(y, "What it does")
y = draw_bullets(y, [
    "Adds lock-on and target cycling keybinds (default R to toggle, Q/E to cycle).",
    "Finds valid targets using range, angle, line-of-sight, and entity-type filters.",
    "Supports target priority modes: nearest, health, threat, and angle.",
    "Tracks a locked target and steers camera with smoothing/preset options and optional auto third-person.",
    "Renders reticles and HUD stats (health, distance, damage prediction, hits-to-kill, vulnerabilities).",
    "Plays themed lock/switch/lethal/lost sounds with per-event volume/pitch plus cooldown protection.",
    "Shows animated floating damage numbers and uses observed damage events for post-hit accuracy."
])

# How it works
y = draw_heading(y, "How it works (repo-evidenced architecture)")
y = draw_bullets(y, [
    "Entry + lifecycle: ZeldaTargetingMod delegates to CommonProxy/ClientProxy; config loads during preInit.",
    "Input/tick flow: TargetingManager listens on Forge event bus, handles key input, and updates lock state each client tick.",
    "Target pipeline: EntityDetector gathers candidates; TargetSelector ranks them; TargetTracker validates distance/dimension over time.",
    "Presentation + combat signals: TargetRenderer and DamageNumbersRenderer draw overlays; DamageEventListener caches LivingDamageEvent values; TargetingSounds handles audio cues.",
    "Config/UI path: GuiFactory opens GuiTargetingConfig, and TargetingConfig persists settings to zeldatargeting.cfg.",
    "External backend services or network API dependencies: Not found in repo."
])

# How to run
y = draw_heading(y, "How to run (minimal)")
y = draw_bullets(y, [
    "Install prerequisites from repo docs: Java 8+, Minecraft 1.12.2, Forge 14.23.5.2859+.",
    "From repo root, generate IDE run configs: gradlew genIntellijRuns or gradlew genEclipseRuns (README.txt).",
    "Import the Gradle project in your IDE and launch the generated client run configuration (build.gradle defines runs.client with workingDirectory run/).",
    "Single command run instruction in README (for example, explicit gradlew runClient step): Not found in repo."
])

if y < MARGIN:
    raise RuntimeError(f"Layout overflowed single page (y={y}).")

c.showPage()
c.save()
print(output_path)
