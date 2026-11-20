"""Sample accounts. Everyone signs in with the same password: User@2026 (BCrypt hash from seed.sql)."""

PASSWORD_HASH = "$2a$12$o9YJXR0LhPQA2ikZExtdL.e5AdbOaD07EWt42x1dYtHlxx4mSZtSy"
DOMAIN = "users.cas.local"   # every sample account uses this email domain, so they are easy to remove

# username, full name, identity, interests, days since joining
USERS = [
    # Students
    ("kyla.reyes", "Kyla Reyes", "STUDENT", ["GARDENING", "STUDIES", "PETS"], 58),
    ("migs.santos", "Miguel Santos", "STUDENT", ["HOME_SECURITY", "ENERGY_SAVING"], 57),
    ("andrea.villanueva", "Andrea Villanueva", "STUDENT", ["STUDIES", "HEALTH_WELLNESS"], 55),
    ("renz.aquino", "Renz Aquino", "STUDENT", ["ENERGY_SAVING", "HOME_SECURITY", "STUDIES"], 54),
    ("trisha.bautista", "Trisha Bautista", "STUDENT", ["FOOD_COOKING", "HOUSEKEEPING"], 52),
    ("josh.ramos", "Joshua Ramos", "STUDENT", ["HOME_SECURITY", "PETS"], 51),
    ("nicole.fernandez", "Nicole Fernandez", "STUDENT", ["HEALTH_WELLNESS", "GARDENING"], 49),
    ("jm.castillo", "John Mark Castillo", "STUDENT", ["ENERGY_SAVING", "STUDIES"], 47),
    ("bea.mendoza", "Bea Mendoza", "STUDENT", ["PETS", "GARDENING"], 45),
    ("ethan.lim", "Ethan Lim", "STUDENT", ["STUDIES", "HOME_SECURITY"], 44),
    ("sam.delosreyes", "Samantha De los Reyes", "STUDENT", ["FOOD_COOKING", "HEALTH_WELLNESS"], 41),
    ("kurt.pascual", "Kurt Pascual", "STUDENT", ["ENERGY_SAVING", "HOUSEKEEPING"], 40),
    ("aly.manalo", "Alyssa Manalo", "STUDENT", ["STUDIES", "PETS"], 37),
    ("jerome.tan", "Jerome Tan", "STUDENT", ["HOME_SECURITY", "ENERGY_SAVING"], 33),
    ("mika.ocampo", "Mika Ocampo", "STUDENT", ["HEALTH_WELLNESS", "STUDIES"], 30),
    ("lance.robles", "Lance Robles", "STUDENT", ["GARDENING", "ENERGY_SAVING"], 22),
    # Hobbyists
    ("pao.dizon", "Paolo Dizon", "HOBBYIST", ["ENERGY_SAVING", "HOME_SECURITY", "HOUSEKEEPING"], 59),
    ("carlo.garcia", "Carlo Garcia", "HOBBYIST", ["GARDENING", "PETS"], 56),
    ("jas.cruz", "Jasmine Cruz", "HOBBYIST", ["FOOD_COOKING", "HOUSEKEEPING"], 53),
    ("vince.navarro", "Vince Navarro", "HOBBYIST", ["HOME_SECURITY", "ENERGY_SAVING"], 50),
    ("tonio.salazar", "Antonio Salazar", "HOBBYIST", ["GARDENING", "FOOD_COOKING"], 48),
    ("ria.domingo", "Ria Domingo", "HOBBYIST", ["PETS", "HEALTH_WELLNESS"], 46),
    ("dennis.uy", "Dennis Uy", "HOBBYIST", ["ENERGY_SAVING"], 39),
    ("grace.valdez", "Grace Valdez", "HOBBYIST", ["GARDENING", "HOUSEKEEPING"], 35),
    ("noel.flores", "Noel Flores", "HOBBYIST", ["PETS", "HOME_SECURITY"], 43),
    ("ivy.morales", "Ivy Morales", "HOBBYIST", ["FOOD_COOKING", "STUDIES"], 42),
    # Professionals
    ("engr.mendoza", "Engr. Ramon Mendoza", "PROFESSIONAL", ["ENERGY_SAVING", "HOME_SECURITY"], 60),
    ("angela.torres", "Angela Torres", "PROFESSIONAL", ["HOUSEKEEPING", "ENERGY_SAVING"], 50),
    ("mark.gonzales", "Mark Gonzales", "PROFESSIONAL", ["HOME_SECURITY"], 47),
    ("camille.rivera", "Camille Rivera", "PROFESSIONAL", ["HEALTH_WELLNESS", "FOOD_COOKING"], 44),
    ("rj.manalastas", "RJ Manalastas", "PROFESSIONAL", ["ENERGY_SAVING", "HOME_SECURITY"], 38),
    ("lea.pineda", "Lea Pineda", "PROFESSIONAL", ["PETS", "HOUSEKEEPING"], 34),
    ("ken.yap", "Kenneth Yap", "PROFESSIONAL", ["STUDIES", "ENERGY_SAVING"], 29),
    ("arnel.bernardo", "Arnel Bernardo", "PROFESSIONAL", ["GARDENING", "FOOD_COOKING"], 25),
    # Educators
    ("sir.belen", "Rodel Belen", "EDUCATOR", ["STUDIES", "ENERGY_SAVING"], 57),
    ("liza.cabrera", "Liza Cabrera", "EDUCATOR", ["STUDIES", "HEALTH_WELLNESS"], 53),
    ("joy.villareal", "Joy Villareal", "EDUCATOR", ["GARDENING", "STUDIES"], 48),
    ("edwin.lagman", "Edwin Lagman", "EDUCATOR", ["HOME_SECURITY", "STUDIES"], 41),
    ("rose.alcantara", "Rosemarie Alcantara", "EDUCATOR", ["FOOD_COOKING", "HEALTH_WELLNESS"], 36),
    ("nestor.ilagan", "Nestor Ilagan", "EDUCATOR", ["ENERGY_SAVING", "PETS"], 27),
    # A spam account the admin already suspended
    ("cheapkits.ph", "Cheap Kits PH", "HOBBYIST", [], 12),
]

# Expert added by the admin (shows "Admin approved")
ADMIN_EXPERTS = {
    "engr.mendoza": "Registered Electronics Engineer, 9 years in building automation (sample account)",
}
# Expert earned from votes (the stratified sampling rule); confirmed by the app's own check in testing
VOTE_EXPERTS = {"pao.dizon": 6}   # days since the badge was earned
SUSPENDED = {"cheapkits.ph"}
