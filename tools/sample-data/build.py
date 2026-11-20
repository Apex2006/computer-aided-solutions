"""Builds sample_data.sql, sample_media.sql and remove_sample_data.sql (plus preview images)."""
import io
import os
import random
import sys
import zipfile

sys.path.insert(0, os.path.dirname(__file__))
from data_users import USERS, PASSWORD_HASH, DOMAIN, ADMIN_EXPERTS, VOTE_EXPERTS, SUSPENDED  # noqa: E402
from data_guides import GUIDES, EXISTING, SPAM  # noqa: E402
import render  # noqa: E402

OUT = sys.argv[1]
os.makedirs(os.path.join(OUT, "preview"), exist_ok=True)
rng = random.Random(2026)


def q(s):
    if s is None:
        return "NULL"
    return "'" + str(s).replace("\\", "\\\\").replace("'", "''").replace("\n", "\\n") + "'"


def lines(items):
    return "\n".join(items)


def numbered(steps):
    return "\n".join(f"{i}. {s}" for i, s in enumerate(steps, 1))


def ago_days(d):
    return f"DATE_SUB(NOW(), INTERVAL {d} DAY)"


def user_id(u):
    return f"(SELECT user_id FROM users WHERE username = {q(u)})"


POSITIVE = ["Clear steps, thank you!", "Built this over the weekend, works great.", "Gumana agad, salamat po!",
            "Saved this for our capstone ideas.", "Very beginner friendly.", "The schematic made the wiring easy to follow.",
            "Watched the video first, then followed the steps. Smooth.", "Parts were cheap online, came in under budget.",
            "Will try this with my students next week.", "Nice project for a school fair.", "Ang linis ng steps, thank you.",
            "Worked on the first upload, rare for me haha."]
NEGATIVE = ["Some steps need more detail for beginners.", "Didn't work for me at first. Double check the wiring table."]

active = [u for u in USERS if u[0] not in SUSPENDED]
uname = {u[0]: u for u in USERS}

# ------------------------------------------------------------------ sample_data.sql
sql = []
sql.append("-- =====================================================================")
sql.append("-- sample_data.sql : sample users, guides, votes, comments and reports.")
sql.append("-- Run AFTER schema.sql and seed.sql (and upgrade.sql on an older database).")
sql.append("-- Then run sample_media.sql for the schematics and step pictures.")
sql.append("-- Safe to run again: it first removes the sample data from an earlier run.")
sql.append(f"-- Every sample account uses an @{DOMAIN} email and the password User@2026.")
sql.append("-- =====================================================================")
sql.append("")
sql.append("USE cas_db;")
sql.append("")
sql.append("-- Remove sample data from an earlier run")
sql.append(f"DELETE FROM guides WHERE author_id IN (SELECT user_id FROM users WHERE email LIKE '%@{DOMAIN}');")
sql.append(f"DELETE FROM users WHERE email LIKE '%@{DOMAIN}';")
sql.append("")
sql.append("-- Sample accounts (password for all: User@2026)")
rows = []
for un, full, ident, interests, days in USERS:
    role, src, cred, since = "USER", None, None, None
    if un in ADMIN_EXPERTS:
        role, src, cred, since = "EXPERT", "ADMIN", ADMIN_EXPERTS[un], ago_days(days - 2)
    status = "SUSPENDED" if un in SUSPENDED else "ACTIVE"
    rows.append(f"    ({q(un)}, {q(un + '@' + DOMAIN)}, {q(PASSWORD_HASH)}, {q(full)}, {q(ident)}, {q(','.join(interests))}, "
                f"{q(role)}, {q(src)}, {q(cred)}, {since or 'NULL'}, {q(status)}, {ago_days(days)})")
sql.append("INSERT INTO users (username, email, password_hash, full_name, identity, interests, role,\n"
           "                   expert_source, credential, expert_since, account_status, created_at) VALUES\n" + ",\n".join(rows) + ";")
sql.append("")

sql.append("-- Sample guides (schematics and step pictures are added by sample_media.sql)")
for g in GUIDES:
    approved = g.get("approved_by")
    sql.append(
        "INSERT INTO guides (author_id, title, category, summary, parts_list, precautions, steps, video_url,\n"
        "                    estimated_cost, difficulty, risk_factor, status, approval, approved_by, approved_at, created_at) VALUES\n"
        f"    ({user_id(g['author'])}, {q(g['title'])}, {q(g['category'])},\n"
        f"     {q(g['summary'])},\n"
        f"     {q(lines(g['parts']))},\n"
        f"     {q(lines(g['precautions']))},\n"
        f"     {q(numbered(g['steps']))},\n"
        f"     {q(g['video'])}, {g['cost']:.2f}, {q(g['difficulty'])}, {q(g['risk'])}, {q(g['status'])},\n"
        f"     {q('Y') if approved else 'NULL'}, {user_id(approved) if approved else 'NULL'}, "
        f"{ago_days(g['days'] - 1) if approved else 'NULL'}, {ago_days(g['days'])});")
sql.append(
    "INSERT INTO guides (author_id, title, category, summary, parts_list, precautions, steps, estimated_cost,\n"
    "                    difficulty, risk_factor, status, created_at) VALUES\n"
    f"    ({user_id(SPAM['author'])}, {q(SPAM['title'])}, {q(SPAM['category'])}, {q(SPAM['summary'])}, {q(SPAM['parts'])},\n"
    f"     {q(SPAM['precautions'])}, {q(SPAM['steps'])}, NULL, 'BEGINNER', 'LOW', 'HIDDEN', {ago_days(SPAM['days'])});")
sql.append("")

sql.append("-- Video links for the starter guides that do not have one yet")
for e in EXISTING:
    sql.append(f"UPDATE guides SET video_url = {q(e['video'])} WHERE title = {q(e['title'])} AND video_url IS NULL;")
sql.append("")


def review_block(title, entries):
    """entries: (username, is_approved or None, comment or None, hours_ago)"""
    parts = []
    for i, (u, ok, c, h) in enumerate(entries):
        okv = "NULL" if ok is None else ("TRUE" if ok else "FALSE")
        if i == 0:
            parts.append(f"    SELECT {q(u)} AS un, {okv} AS ok, {q(c)} AS cm, {h} AS h")
        else:
            parts.append(f"    UNION ALL SELECT {q(u)}, {okv}, {q(c)}, {h}")
    return ("INSERT INTO reviews (guide_id, user_id, is_approved, comment, created_at)\n"
            "SELECT g.guide_id, u.user_id, r.ok, r.cm, DATE_SUB(NOW(), INTERVAL r.h HOUR)\nFROM (\n" + "\n".join(parts) +
            f"\n) r\nJOIN users u ON u.username = r.un COLLATE utf8mb4_general_ci\nJOIN guides g ON g.title = {q(title)};")


sql.append("-- Votes and comments (one row per person per guide; a NULL vote is a comment without a vote)")
stats = {}
for g in GUIDES:
    count, p = g["votes"]
    if count == 0:
        continue
    hours = g["days"] * 24
    exclude = {g["author"]}
    if g.get("qa"):
        exclude.add(g["qa"][0])
    if g.get("approved_by"):
        exclude.add(g["approved_by"])
    pool = [u[0] for u in active if u[0] not in exclude]
    voters = rng.sample(pool, min(count, len(pool)))
    pos, neg = list(g["comments"]), list(g["critiques"])
    entries = []
    n_no = max(len(neg), round(count * (1 - p)))
    for i, u in enumerate(voters):
        ok = i >= n_no
        if ok:
            c = pos.pop(0) if pos else (rng.choice(POSITIVE) if rng.random() < 0.18 else None)
        else:
            c = neg.pop(0) if neg else (rng.choice(NEGATIVE) if rng.random() < 0.5 else None)
        entries.append((u, ok, c, rng.randint(2, max(3, hours - 6))))
    if g.get("qa"):
        asker, question, reply = g["qa"]
        hq = rng.randint(30, max(31, hours - 10))
        entries.append((asker, None, question, hq))
        entries.append((g["author"], None, reply, hq - rng.randint(2, 20)))
    if g.get("approved_by"):
        entries.append((g["approved_by"], None,
                        "Approved. Use a relay module with an enclosed 10A relay, put a 2A fuse on the live input, and keep all 230V parts in a closed box.",
                        hours - 26))
    stats[g["title"]] = (sum(1 for e in entries if e[1] is True), sum(1 for e in entries if e[1] is False))
    sql.append(review_block(g["title"], entries))
    sql.append("")

for e in EXISTING:
    n = rng.randint(4, 9)
    voters = rng.sample([u[0] for u in active], n)
    entries = [(u, rng.random() > 0.1, rng.choice(POSITIVE) if rng.random() < 0.35 else None, rng.randint(24, 900)) for u in voters]
    sql.append(review_block(e["title"], entries))
    sql.append("")

sql.append("-- Expert earned from votes: enough approvals from every identity group (stratified sampling rule)")
for u, d in VOTE_EXPERTS.items():
    sql.append(f"UPDATE users SET role = 'EXPERT', expert_source = 'VOTES', expert_since = {ago_days(d)} WHERE username = {q(u)};")
sql.append("")

sql.append("-- Reports and edit requests for the Admin Panel")
REPORTS = [
    ("Touchless Trash Can Lid", "trisha.bautista", "EDIT_REQUEST", "Please say that an SG90 is too weak for heavier lids. The MG90S worked for me.", "OPEN", None, 70),
    ("Laser Tripwire Alarm", "liza.cabrera", "REPORT", "Please make the eye safety warning bigger. Students will try to point it at each other.", "OPEN", None, 50),
    ("Heart Rate Monitor with Pulse Sensor", "camille.rivera", "EDIT_REQUEST", "Step 3 should link the PulseSensor Playground library page.", "OPEN", None, 20),
    (SPAM["title"], "kyla.reyes", "REPORT", "Spam, this is an ad not a guide.", "RESOLVED", "admin", 250),
    (SPAM["title"], "sir.belen", "REPORT", "Advertisement. Please remove.", "RESOLVED", "admin", 248),
]
for title, who, typ, reason, status, by, h in REPORTS:
    sql.append("INSERT INTO reports (guide_id, reporter_id, type, reason, status, resolved_by, created_at)\n"
               f"SELECT g.guide_id, u.user_id, {q(typ)}, {q(reason)}, {q(status)}, "
               f"{user_id(by) if by else 'NULL'}, DATE_SUB(NOW(), INTERVAL {h} HOUR)\n"
               f"FROM guides g JOIN users u ON u.username = {q(who)} WHERE g.title = {q(title)};")
sql.append("")

with open(os.path.join(OUT, "sample_data.sql"), "w", encoding="utf-8") as f:
    f.write("\n".join(sql) + "\n")

# ------------------------------------------------------------------ sample_media.sql (+ previews)
media = []
media.append("-- =====================================================================")
media.append("-- sample_media.sql : wiring schematics and step pictures for the sample")
media.append("-- guides, plus schematics for the starter guides that have none.")
media.append("-- Run AFTER sample_data.sql. In MySQL Workbench use File > Run SQL Script")
media.append("-- (the file is too big to open in the editor).")
media.append("-- =====================================================================")
media.append("")
media.append("USE cas_db;")
media.append("")
sizes = {}
for g in GUIDES:
    sch = render.schematic(g)
    shots = {n: render.step_image(g, n, spec) for n, spec in sorted(g["images"].items())}
    buf = io.BytesIO()
    with zipfile.ZipFile(buf, "w", zipfile.ZIP_STORED) as z:
        for n, data in shots.items():
            z.writestr(f"step-{n}.jpg", data)
    packed = buf.getvalue()
    slug = "".join(ch if ch.isalnum() else "_" for ch in g["title"].lower())[:40]
    with open(os.path.join(OUT, "preview", slug + "_schematic.jpg"), "wb") as f:
        f.write(sch)
    for n, data in shots.items():
        with open(os.path.join(OUT, "preview", f"{slug}_step{n}.jpg"), "wb") as f:
            f.write(data)
    sizes[g["title"]] = (len(sch), len(packed), len(shots))
    media.append(f"-- {g['title']}")
    media.append(f"UPDATE guides SET schematic = X'{sch.hex()}',\n    step_images = X'{packed.hex()}'\n"
                 f"WHERE title = {q(g['title'])} AND author_id = {user_id(g['author'])};")
    media.append("")
for e in EXISTING:
    eg = dict(e, risk="LOW")
    sch = render.schematic(eg)
    slug = "".join(ch if ch.isalnum() else "_" for ch in e["title"].lower())[:40]
    with open(os.path.join(OUT, "preview", slug + "_schematic.jpg"), "wb") as f:
        f.write(sch)
    sizes[e["title"]] = (len(sch), 0, 0)
    media.append(f"-- {e['title']} (starter guide, only if it has no schematic yet)")
    media.append(f"UPDATE guides SET schematic = X'{sch.hex()}'\nWHERE title = {q(e['title'])} AND schematic IS NULL;")
    media.append("")
with open(os.path.join(OUT, "sample_media.sql"), "w", encoding="utf-8") as f:
    f.write("\n".join(media) + "\n")

# ------------------------------------------------------------------ remove_sample_data.sql
with open(os.path.join(OUT, "remove_sample_data.sql"), "w", encoding="utf-8") as f:
    f.write("-- remove_sample_data.sql : removes every sample account and everything they made\n"
            "-- (their guides, votes, comments and reports). Your own data is not touched.\n\n"
            "USE cas_db;\n\n"
            f"DELETE FROM guides WHERE author_id IN (SELECT user_id FROM users WHERE email LIKE '%@{DOMAIN}');\n"
            f"DELETE FROM users WHERE email LIKE '%@{DOMAIN}';\n")

for t, (a, b, n) in sizes.items():
    print(f"{t[:44]:44s} schematic {a // 1024:4d} KB  steps {b // 1024:4d} KB ({n})")
for t, (y, n) in stats.items():
    print(f"votes {t[:40]:40s} yes {y:2d} no {n:2d}")
print("sample_data.sql", os.path.getsize(os.path.join(OUT, "sample_data.sql")) // 1024, "KB")
print("sample_media.sql", os.path.getsize(os.path.join(OUT, "sample_media.sql")) // 1024, "KB")
