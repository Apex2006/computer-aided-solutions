"""Draws wiring schematics (blueprint style, like guide 5) and step pictures (light style)."""
import io
import re
from PIL import Image, ImageDraw, ImageFont

F = "/usr/share/fonts/truetype/dejavu/"


def font(sz, bold=False, mono=False):
    n = ("DejaVuSansMono-Bold.ttf" if bold else "DejaVuSansMono.ttf") if mono else ("DejaVuSans-Bold.ttf" if bold else "DejaVuSans.ttf")
    return ImageFont.truetype(F + n, sz)


def ctext(d, xy, t, f, fill):
    b = d.textbbox((0, 0), t, font=f)
    d.text((xy[0] - (b[2] - b[0]) / 2, xy[1] - (b[3] - b[1]) / 2 - b[1]), t, font=f, fill=fill)


def rtext(d, xr, y, t, f, fill):
    w = d.textlength(t, font=f)
    d.text((xr - w, y), t, font=f, fill=fill)


def jpeg(im, q=84):
    buf = io.BytesIO()
    im.convert("RGB").save(buf, "JPEG", quality=q, optimize=True)
    return buf.getvalue()


# ------------------------------------------------------------------ schematic
BG, GRID1, GRID2 = "#0F2A3A", "#163A4F", "#1D4A63"
WH, CY = "#E8F1F5", "#7FD6F5"
COLORS = {"yellow": "#FFD54F", "green": "#9CE6A8", "cyan": "#7FD6F5", "pink": "#F48FB1", "orange": "#FFB74D",
          "violet": "#B39DDB", "white": "#E8F1F5", "red": "#FF8A80", "gray": "#B0BEC5", "mains": "#FF5252", "neutral": "#82B1FF"}
POWER = {"5V": "red", "VIN": "red", "SERVO5V": "red", "3V3": "orange", "GND": "gray", "6V": "orange"}
POWER_ORDER = ["5V", "VIN", "SERVO5V", "6V", "3V3", "GND"]


def board_pin_label(board, pin):
    return board.get("alias", {}).get(pin, {"3V3": "3.3V"}.get(pin, pin))


def schematic(g):
    board, comps, loads = g["board"], g["comps"], g.get("loads", [])
    bpins = board["pins"]
    netcolors = g.get("netcolors", {})

    # ---- vertical layout
    TOP = 150
    BX, BW, PIN_STEP = 70, 250, 36
    board_y = TOP
    pin_y = {p: board_y + 78 + i * PIN_STEP for i, p in enumerate(bpins)}
    board_h = 78 + len(bpins) * PIN_STEP + 10

    gap1_nets = []          # nets drawn as lanes between board and components
    for c in comps:
        for _, net in c["left"]:
            if net not in gap1_nets:
                gap1_nets.append(net)
    power_nets = sorted([n for n in gap1_nets if n in POWER], key=lambda n: POWER_ORDER.index(n))
    sig_nets = sorted([n for n in gap1_nets if n not in POWER], key=lambda n: bpins.index(n) if n in bpins else 99)
    LANE, BUS = 24, 28
    lane_x = {}
    x = BX + BW + 40
    for n in sig_nets:
        lane_x[n] = x
        x += LANE
    x += 18
    for n in power_nets:
        lane_x[n] = x
        x += BUS
    CX, CW = x + 40, 290

    taken = set(pin_y.values())

    def clash(ys):
        return any(abs(y - t) < 8 for y in ys for t in taken)

    cy = TOP
    comp_pos = {}
    for c in comps:
        head = 66 if c.get("note") else 46
        n = max(len(c["left"]), len(c.get("right", [])))
        h = head + n * 32 + 4
        while True:
            ys = [cy + head + i * 32 + 6 for i in range(n)]
            if not clash(ys):
                break
            cy += 9
        comp_pos[c["id"]] = (cy, h, head)
        taken.update(ys)
        cy += h + 26

    gap2_nets = []
    for c in comps:
        for _, net in c.get("right", []):
            if net not in gap2_nets:
                gap2_nets.append(net)
    for l in loads:
        for _, net in l["left"]:
            if net not in gap2_nets:
                gap2_nets.append(net)
    LX0 = CX + CW + 40
    lane2 = {n: LX0 + i * LANE for i, n in enumerate(gap2_nets)}
    LX = LX0 + len(gap2_nets) * LANE + 34
    LW = 250
    first_right = next((comp_pos[c["id"]][0] for c in comps if c.get("right")), TOP)
    ly = first_right
    load_pos = {}
    for l in loads:
        head = 66 if l.get("note") else 46
        h = head + len(l["left"]) * 32 + 4
        while clash([ly + head + i * 32 + 6 for i in range(len(l["left"]))]):
            ly += 9
        load_pos[l["id"]] = (ly, h, head)
        taken.update(ly + head + i * 32 + 6 for i in range(len(l["left"])))
        ly += h + 30

    bottom = max([board_y + board_h, cy] + [load_pos[l["id"]][0] + load_pos[l["id"]][1] for l in loads])
    W = max(1400, (LX + LW + 60) if loads else (CX + CW + 80))
    H = max(860, bottom + 150)
    im = Image.new("RGB", (W, H), BG)
    d = ImageDraw.Draw(im)
    for xx in range(0, W, 25):
        d.line([(xx, 0), (xx, H)], fill=GRID2 if xx % 100 == 0 else GRID1)
    for yy in range(0, H, 25):
        d.line([(0, yy), (W, yy)], fill=GRID2 if yy % 100 == 0 else GRID1)

    d.text((40, 30), (g["title"] + "  -  wiring schematic").upper(), font=font(28, True), fill=WH)
    sub = board["name"] + " + " + " + ".join(c["name"] for c in comps)
    if len(sub) > 120:
        sub = sub[:117] + "..."
    d.text((40, 72), sub, font=font(17), fill=CY)

    def col_of(net, default="yellow"):
        if net in netcolors:
            return netcolors[net]
        if net in POWER:
            return POWER[net]
        return default

    def width_of(net):
        c = col_of(net)
        return 5 if c in ("mains", "neutral") else 3

    # endpoints per net: (x_lane_side_x, y)
    ends = {n: [] for n in gap1_nets + gap2_nets}
    comp_color = {}
    for c in comps:
        y0, h, head = comp_pos[c["id"]]
        for i, (pin, net) in enumerate(c["left"]):
            ends[net].append(("c", CX, y0 + head + i * 32 + 6))
            if net not in POWER and net not in comp_color:
                comp_color[net] = c.get("color", "yellow")
        for i, (pin, net) in enumerate(c.get("right", [])):
            ends[net].append(("r", CX + CW, y0 + head + i * 32 + 6))
    for l in loads:
        y0, h, head = load_pos[l["id"]]
        for i, (pin, net) in enumerate(l["left"]):
            ends[net].append(("l", LX, y0 + head + i * 32 + 6))
    for net in gap1_nets:
        if net in bpins:
            ends[net].append(("b", BX + BW, pin_y[net]))

    def draw_net(net, xl):
        pts = ends[net]
        colname = col_of(net, comp_color.get(net, "yellow"))
        fill = COLORS[colname]
        w = width_of(net)
        ys = [p[2] for p in pts]
        if len(pts) > 1:
            d.line([(xl, min(ys)), (xl, max(ys))], fill=fill, width=w)
        for kind, px, py in pts:
            d.line([(px, py), (xl, py)], fill=fill, width=w)
            if min(ys) < py < max(ys):
                d.ellipse([xl - 6, py - 6, xl + 6, py + 6], fill=fill)

    for net in gap1_nets:
        draw_net(net, lane_x[net])
    for net in gap2_nets:
        draw_net(net, lane2[net])

    # board
    d.rounded_rectangle([BX, board_y, BX + BW, board_y + board_h], 14, outline=WH, width=3, fill=BG)
    ctext(d, (BX + BW / 2, board_y + 32), board["name"], font(21, True), WH)
    for p in bpins:
        y = pin_y[p]
        rtext(d, BX + BW - 14, y - 10, board_pin_label(board, p), font(16, mono=True), CY)
        d.ellipse([BX + BW - 5, y - 5, BX + BW + 5, y + 5], fill=WH)

    def box(x, y0, w, h, head, item, outline, pins_left, pins_right=()):
        d.rounded_rectangle([x, y0, x + w, y0 + h], 10, outline=outline, width=3, fill=BG)
        ctext(d, (x + w / 2, y0 + 22), item["name"], font(17, True), outline)
        if item.get("note"):
            ctext(d, (x + w / 2, y0 + 46), item["note"], font(13), WH)
        for i, (pin, net) in enumerate(pins_left):
            y = y0 + head + i * 32 + 6
            d.text((x + 12, y - 9), pin, font=font(15, mono=True), fill=WH)
        for i, (pin, net) in enumerate(pins_right):
            y = y0 + head + i * 32 + 6
            rtext(d, x + w - 12, y - 9, pin, font(15, mono=True), WH)

    for c in comps:
        y0, h, head = comp_pos[c["id"]]
        box(CX, y0, CW, h, head, c, COLORS[c.get("color", "yellow")], c["left"], c.get("right", []))
    for l in loads:
        y0, h, head = load_pos[l["id"]]
        box(LX, y0, LW, h, head, l, WH, l["left"])

    # legend + warning
    used = set(col_of(n, comp_color.get(n, "yellow")) for n in gap1_nets + gap2_nets)
    legend = []
    if "red" in used:
        legend.append(("red", "5V power"))
    if "orange" in used and any(n in ("3V3",) for n in gap1_nets):
        legend.append(("orange", "3.3V"))
    if any(n.startswith("12V") for n in gap2_nets):
        legend.append(("orange", "12V side"))
    if "gray" in used:
        legend.append(("gray", "ground"))
    if "mains" in used:
        legend.append(("mains", "230V live"))
    if "neutral" in used:
        legend.append(("neutral", "neutral"))
    lx, ly2 = 40, H - 70
    d.text((lx, ly2 - 34), "Colored lines: signal wires to each part.   Dots: wires joined.   Crossing lines without a dot are not joined.",
           font=font(15), fill=CY)
    for colname, label in legend:
        d.line([(lx, ly2 + 10), (lx + 36, ly2 + 10)], fill=COLORS[colname], width=5)
        d.text((lx + 44, ly2), label, font=font(16), fill=WH)
        lx += 44 + d.textlength(label, font=font(16)) + 36
    if g.get("risk") == "HIGH":
        msg = "DANGER: thick red and blue lines carry 230V. Turn the breaker off first."
    elif any(n.startswith("12V") for n in gap2_nets):
        msg = "CAUTION: only 12V on the orange lines. Never connect 230V here."
    else:
        msg = None
    if msg:
        wmsg = d.textlength(msg, font=font(16, True)) + 36
        d.rounded_rectangle([W - wmsg - 40, H - 82, W - 40, H - 38], 10, outline=COLORS["mains"], width=2)
        d.text((W - wmsg - 22, H - 70), msg, font=font(16, True), fill=COLORS["mains"])
    return jpeg(im, 84)


# ------------------------------------------------------------------ step pictures (light)
INK, MUTED, LINE, PAPER = "#1C2B1E", "#8A948B", "#DCE3D6", "#F6F8F3"
GREEN, BLUE = "#2E7D32", "#1565C0"


def card(title, num, h=640, sub="Illustration"):
    im = Image.new("RGB", (1024, h), PAPER)
    d = ImageDraw.Draw(im)
    d.rounded_rectangle([24, 24, 1000, h - 24], 22, fill="white", outline=LINE, width=2)
    d.rounded_rectangle([48, 48, 112, 112], 16, fill=GREEN)
    ctext(d, (80, 80), str(num), font(34, True), "white")
    d.text((132, 58), title, font=font(26, True), fill=INK)
    d.text((132, 94), sub, font=font(15, mono=sub != "Illustration"), fill=MUTED)
    return im, d


def endpoint_label(g, net, exclude):
    """Where a net goes, as plain words (board pin, or the other parts on it)."""
    board = g["board"]
    if net in board["pins"]:
        lab = board_pin_label(board, net)
        if net in POWER:
            return lab
        if re.match(r"^(D\d|A\d|GPIO)", net):
            return "Pin " + lab
        return board["name"] + " " + lab
    out = []
    for c in g["comps"] + g.get("loads", []):
        for pin, n in c["left"] + c.get("right", []):
            if n == net and (c["id"], pin) != exclude:
                out.append(f"{c['name']} {pin}")
    return ", ".join(out) if out else net


def table(g, spec, num):
    rows = []
    seen = set()
    side = spec.get("side", "all")
    listed = set()
    power = {}   # board power pin label -> part pin names (summarized under the table)
    for cid in spec["comps"]:
        c = next(x for x in g["comps"] if x["id"] == cid)
        pins = (c["left"] if side in ("all", "left") else []) + (c.get("right", []) if side in ("all", "right") else [])
        for pin, net in pins:
            if net in POWER and net in g["board"]["pins"]:
                power.setdefault(board_pin_label(g["board"], net), [])
                if pin not in power[board_pin_label(g["board"], net)]:
                    power[board_pin_label(g["board"], net)].append(pin)
                continue
            if net not in g["board"]["pins"]:
                if net in listed:
                    continue   # the other end of this wire is already in the table
                listed.add(net)
            rows.append((c["name"], pin, endpoint_label(g, net, (cid, pin))))
            seen.add((cid, pin))
    note = ""
    if power:
        def join_and(xs):
            return xs[0] if len(xs) == 1 else ", ".join(xs[:-1]) + " and " + xs[-1]
        note = "Power: connect " + ", and ".join(f"{join_and(pins)} to {lab}" for lab, pins in power.items()) + "."
        if not rows:
            rows, note = [(next(x for x in g["comps"] if x["id"] == spec["comps"][0])["name"], p, lab)
                          for lab, ps in power.items() for p in ps], ""
    for lid in spec.get("loads", []):
        l = next(x for x in g["loads"] if x["id"] == lid)
        for pin, net in l["left"]:
            other = [e for e in g["comps"] if any(n == net for _, n in e.get("right", []))]
            if other or net in listed:
                continue   # already listed from the component side or the other load
            listed.add(net)
            rows.append((l["name"], pin, endpoint_label(g, net, (lid, pin))))
    RH = 50
    h = max(640, 150 + (len(rows) + 1) * RH + 50 + (80 if note else 0))
    im, d = card(spec["title"], num, h)
    x0, cols = 70, [0, 310, 470, 884]
    head = ("Part", "Pin", "Connect to")
    for r, row in enumerate([head] + rows):
        y = 140 + r * RH
        d.rectangle([x0, y, x0 + cols[-1], y + RH], fill=GREEN if r == 0 else ("#F7F9F5" if r % 2 else "white"), outline=LINE)
        for ci, t in enumerate(row):
            f = font(18, True) if r == 0 or ci == 2 else font(18, mono=ci == 1)
            maxw = cols[ci + 1] - cols[ci] - 30
            while d.textlength(t, font=f) > maxw and len(t) > 4:
                t = t[:-4] + "..."
            d.text((x0 + cols[ci] + 16, y + 14), t, font=f, fill="white" if r == 0 else (BLUE if ci == 2 else INK))
    if note:
        y = 140 + (len(rows) + 1) * RH + 18
        words, line = note.split(" "), ""
        for w in words:
            if d.textlength(line + " " + w, font=font(18)) > 870 and line:
                d.text((x0, y), line, font=font(18), fill=INK)
                y += 28
                line = w
            else:
                line = (line + " " + w).strip()
        d.text((x0, y), line, font=font(18), fill=INK)
    return jpeg(im)


KEYWORDS = r"\b(?:void|int|const|float|bool|unsigned|long|if|else|while|return|true|false|for|char|byte)\b"


def code_card(spec, num):
    lines = spec["code"].split("\n")
    LH = 25
    h = max(640, 150 + len(lines) * LH + 60)
    im, d = card(spec["title"], num, h, spec["file"])
    ex0, ey0 = 56, 136
    d.rounded_rectangle([ex0, ey0, 968, h - 52], 14, fill="#1E1E1E")
    fm = font(17, mono=True)
    cw = d.textlength("M", font=fm)
    for i, line in enumerate(lines):
        y = ey0 + 18 + i * LH
        rtext(d, ex0 + 44, y, str(i + 1), fm, "#5A5A5A")
        x = ex0 + 62
        code, comment = line, ""
        m = re.search(r"//.*$", line)
        if m and '"' not in line[:m.start()].replace('\\"', "")[-1:]:
            code, comment = line[:m.start()], line[m.start():]
        for tok in re.split(r'("[^"]*"|\b\d+(?:\.\d+)?(?:UL)?\b|' + KEYWORDS + r"|#\w+)", code):
            if not tok:
                continue
            if tok.startswith('"'):
                col = "#CE9178"
            elif tok.startswith("#"):
                col = "#C586C0"
            elif re.fullmatch(KEYWORDS, tok):
                col = "#569CD6"
            elif re.fullmatch(r"\d+(?:\.\d+)?(?:UL)?", tok):
                col = "#B5CEA8"
            else:
                col = "#D4D4D4"
            d.text((x, y), tok, font=fm, fill=col)
            x += cw * len(tok)
        if comment:
            d.text((x, y), comment, font=fm, fill="#6A9955")
    return jpeg(im)


def oled_card(spec, num):
    im, d = card(spec["title"], num)
    # PCB-blue module with a black screen (yellow top band like the common 2-color OLED)
    d.rounded_rectangle([262, 150, 762, 470], 18, fill="#1B4F9C")
    for cx in (282, 742):
        for cyy in (170, 450):
            d.ellipse([cx - 9, cyy - 9, cx + 9, cyy + 9], fill="#0F2F5E")
    d.rectangle([302, 190, 722, 430], fill="#05080A")
    lines = spec["lines"]
    ctext(d, (512, 222), lines[0], font(24, True, mono=True), "#FFD54F")
    if len(lines) > 1:
        ctext(d, (512, 316), lines[1], font(64, True, mono=True), "#7FE3FF")
    if len(lines) > 2:
        ctext(d, (512, 398), lines[2], font(22, mono=True), "#7FE3FF")
    for i, p in enumerate(["GND", "VCC", "SCL", "SDA"]):
        ctext(d, (436 + i * 50, 450), p, font(11, True), "#CFE0FA")
    ctext(d, (512, 530), spec.get("caption", ""), font(20), INK)
    return jpeg(im)


def lcd_card(spec, num):
    im, d = card(spec["title"], num)
    d.rounded_rectangle([187, 160, 837, 450], 14, fill="#1E7A3A")
    d.rounded_rectangle([227, 200, 797, 410], 8, fill="#1D2B33")
    d.rectangle([247, 220, 777, 390], fill="#2C6BD6")
    fm = font(44, True, mono=True)
    for i, line in enumerate(spec["lines"][:2]):
        d.text((272, 238 + i * 72), line.ljust(16)[:16], font=fm, fill="#EAF2FF")
    ctext(d, (512, 530), spec.get("caption", ""), font(20), INK)
    return jpeg(im)


def serial_card(spec, num):
    im, d = card(spec["title"], num)
    d.rounded_rectangle([90, 140, 934, 560], 12, fill="white", outline="#B8C0B4", width=2)
    d.rounded_rectangle([90, 140, 934, 184], 12, fill="#00878F")
    d.rectangle([90, 168, 934, 184], fill="#00878F")
    d.text((110, 150), "Serial Monitor  -  COM3", font=font(18, True), fill="white")
    for i, line in enumerate(spec["lines"]):
        d.text((116, 206 + i * 38), line, font=font(20, mono=True), fill=INK)
    d.line([(90, 516), (934, 516)], fill="#DDE3DA", width=2)
    d.text((110, 526), "Autoscroll", font=font(15), fill=MUTED)
    rtext(d, 914, 526, "9600 baud", font(15), MUTED)
    return jpeg(im)


def phone_card(spec, num):
    im, d = card(spec["title"], num)
    d.rounded_rectangle([382, 130, 642, 600], 34, fill=INK)
    d.rounded_rectangle([396, 150, 628, 580], 24, fill=PAPER)
    d.text((414, 168), spec["heading"], font=font(20, True), fill=INK)
    d.text((414, 196), "Updated every 2 seconds", font=font(13), fill=MUTED)
    for i, (k, v) in enumerate(spec["rows"]):
        y = 228 + i * 86
        d.rounded_rectangle([410, y, 614, y + 74], 12, fill="white", outline=LINE, width=2)
        d.text((424, y + 10), k, font=font(15), fill=MUTED)
        d.text((424, y + 34), v, font=font(26, True), fill=GREEN)
    return jpeg(im)


def step_image(g, num, spec):
    t = spec["type"]
    if t == "table":
        return table(g, spec, num)
    if t == "code":
        return code_card(spec, num)
    if t == "oled":
        return oled_card(spec, num)
    if t == "lcd":
        return lcd_card(spec, num)
    if t == "serial":
        return serial_card(spec, num)
    if t == "phone":
        return phone_card(spec, num)
    raise ValueError(t)
