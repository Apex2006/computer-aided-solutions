-- =====================================================================
-- sample_data.sql : sample users, guides, votes, comments and reports.
-- Run AFTER schema.sql and seed.sql (and upgrade.sql on an older database).
-- Then run sample_media.sql for the schematics and step pictures.
-- Safe to run again: it first removes the sample data from an earlier run.
-- Every sample account uses an @users.cas.local email and the password User@2026.
-- =====================================================================

USE cas_db;

-- Remove sample data from an earlier run
DELETE FROM guides WHERE author_id IN (SELECT user_id FROM users WHERE email LIKE '%@users.cas.local');
DELETE FROM users WHERE email LIKE '%@users.cas.local';

-- Sample accounts (password for all: User@2026)
INSERT INTO users (username, email, password_hash, full_name, identity, interests, role,
                   expert_source, credential, expert_since, account_status, created_at) VALUES
    ('kyla.reyes', 'kyla.reyes@users.cas.local', '$2a$12$o9YJXR0LhPQA2ikZExtdL.e5AdbOaD07EWt42x1dYtHlxx4mSZtSy', 'Kyla Reyes', 'STUDENT', 'GARDENING,STUDIES,PETS', 'USER', NULL, NULL, NULL, 'ACTIVE', DATE_SUB(NOW(), INTERVAL 58 DAY)),
    ('migs.santos', 'migs.santos@users.cas.local', '$2a$12$o9YJXR0LhPQA2ikZExtdL.e5AdbOaD07EWt42x1dYtHlxx4mSZtSy', 'Miguel Santos', 'STUDENT', 'HOME_SECURITY,ENERGY_SAVING', 'USER', NULL, NULL, NULL, 'ACTIVE', DATE_SUB(NOW(), INTERVAL 57 DAY)),
    ('andrea.villanueva', 'andrea.villanueva@users.cas.local', '$2a$12$o9YJXR0LhPQA2ikZExtdL.e5AdbOaD07EWt42x1dYtHlxx4mSZtSy', 'Andrea Villanueva', 'STUDENT', 'STUDIES,HEALTH_WELLNESS', 'USER', NULL, NULL, NULL, 'ACTIVE', DATE_SUB(NOW(), INTERVAL 55 DAY)),
    ('renz.aquino', 'renz.aquino@users.cas.local', '$2a$12$o9YJXR0LhPQA2ikZExtdL.e5AdbOaD07EWt42x1dYtHlxx4mSZtSy', 'Renz Aquino', 'STUDENT', 'ENERGY_SAVING,HOME_SECURITY,STUDIES', 'USER', NULL, NULL, NULL, 'ACTIVE', DATE_SUB(NOW(), INTERVAL 54 DAY)),
    ('trisha.bautista', 'trisha.bautista@users.cas.local', '$2a$12$o9YJXR0LhPQA2ikZExtdL.e5AdbOaD07EWt42x1dYtHlxx4mSZtSy', 'Trisha Bautista', 'STUDENT', 'FOOD_COOKING,HOUSEKEEPING', 'USER', NULL, NULL, NULL, 'ACTIVE', DATE_SUB(NOW(), INTERVAL 52 DAY)),
    ('josh.ramos', 'josh.ramos@users.cas.local', '$2a$12$o9YJXR0LhPQA2ikZExtdL.e5AdbOaD07EWt42x1dYtHlxx4mSZtSy', 'Joshua Ramos', 'STUDENT', 'HOME_SECURITY,PETS', 'USER', NULL, NULL, NULL, 'ACTIVE', DATE_SUB(NOW(), INTERVAL 51 DAY)),
    ('nicole.fernandez', 'nicole.fernandez@users.cas.local', '$2a$12$o9YJXR0LhPQA2ikZExtdL.e5AdbOaD07EWt42x1dYtHlxx4mSZtSy', 'Nicole Fernandez', 'STUDENT', 'HEALTH_WELLNESS,GARDENING', 'USER', NULL, NULL, NULL, 'ACTIVE', DATE_SUB(NOW(), INTERVAL 49 DAY)),
    ('jm.castillo', 'jm.castillo@users.cas.local', '$2a$12$o9YJXR0LhPQA2ikZExtdL.e5AdbOaD07EWt42x1dYtHlxx4mSZtSy', 'John Mark Castillo', 'STUDENT', 'ENERGY_SAVING,STUDIES', 'USER', NULL, NULL, NULL, 'ACTIVE', DATE_SUB(NOW(), INTERVAL 47 DAY)),
    ('bea.mendoza', 'bea.mendoza@users.cas.local', '$2a$12$o9YJXR0LhPQA2ikZExtdL.e5AdbOaD07EWt42x1dYtHlxx4mSZtSy', 'Bea Mendoza', 'STUDENT', 'PETS,GARDENING', 'USER', NULL, NULL, NULL, 'ACTIVE', DATE_SUB(NOW(), INTERVAL 45 DAY)),
    ('ethan.lim', 'ethan.lim@users.cas.local', '$2a$12$o9YJXR0LhPQA2ikZExtdL.e5AdbOaD07EWt42x1dYtHlxx4mSZtSy', 'Ethan Lim', 'STUDENT', 'STUDIES,HOME_SECURITY', 'USER', NULL, NULL, NULL, 'ACTIVE', DATE_SUB(NOW(), INTERVAL 44 DAY)),
    ('sam.delosreyes', 'sam.delosreyes@users.cas.local', '$2a$12$o9YJXR0LhPQA2ikZExtdL.e5AdbOaD07EWt42x1dYtHlxx4mSZtSy', 'Samantha De los Reyes', 'STUDENT', 'FOOD_COOKING,HEALTH_WELLNESS', 'USER', NULL, NULL, NULL, 'ACTIVE', DATE_SUB(NOW(), INTERVAL 41 DAY)),
    ('kurt.pascual', 'kurt.pascual@users.cas.local', '$2a$12$o9YJXR0LhPQA2ikZExtdL.e5AdbOaD07EWt42x1dYtHlxx4mSZtSy', 'Kurt Pascual', 'STUDENT', 'ENERGY_SAVING,HOUSEKEEPING', 'USER', NULL, NULL, NULL, 'ACTIVE', DATE_SUB(NOW(), INTERVAL 40 DAY)),
    ('aly.manalo', 'aly.manalo@users.cas.local', '$2a$12$o9YJXR0LhPQA2ikZExtdL.e5AdbOaD07EWt42x1dYtHlxx4mSZtSy', 'Alyssa Manalo', 'STUDENT', 'STUDIES,PETS', 'USER', NULL, NULL, NULL, 'ACTIVE', DATE_SUB(NOW(), INTERVAL 37 DAY)),
    ('jerome.tan', 'jerome.tan@users.cas.local', '$2a$12$o9YJXR0LhPQA2ikZExtdL.e5AdbOaD07EWt42x1dYtHlxx4mSZtSy', 'Jerome Tan', 'STUDENT', 'HOME_SECURITY,ENERGY_SAVING', 'USER', NULL, NULL, NULL, 'ACTIVE', DATE_SUB(NOW(), INTERVAL 33 DAY)),
    ('mika.ocampo', 'mika.ocampo@users.cas.local', '$2a$12$o9YJXR0LhPQA2ikZExtdL.e5AdbOaD07EWt42x1dYtHlxx4mSZtSy', 'Mika Ocampo', 'STUDENT', 'HEALTH_WELLNESS,STUDIES', 'USER', NULL, NULL, NULL, 'ACTIVE', DATE_SUB(NOW(), INTERVAL 30 DAY)),
    ('lance.robles', 'lance.robles@users.cas.local', '$2a$12$o9YJXR0LhPQA2ikZExtdL.e5AdbOaD07EWt42x1dYtHlxx4mSZtSy', 'Lance Robles', 'STUDENT', 'GARDENING,ENERGY_SAVING', 'USER', NULL, NULL, NULL, 'ACTIVE', DATE_SUB(NOW(), INTERVAL 22 DAY)),
    ('pao.dizon', 'pao.dizon@users.cas.local', '$2a$12$o9YJXR0LhPQA2ikZExtdL.e5AdbOaD07EWt42x1dYtHlxx4mSZtSy', 'Paolo Dizon', 'HOBBYIST', 'ENERGY_SAVING,HOME_SECURITY,HOUSEKEEPING', 'USER', NULL, NULL, NULL, 'ACTIVE', DATE_SUB(NOW(), INTERVAL 59 DAY)),
    ('carlo.garcia', 'carlo.garcia@users.cas.local', '$2a$12$o9YJXR0LhPQA2ikZExtdL.e5AdbOaD07EWt42x1dYtHlxx4mSZtSy', 'Carlo Garcia', 'HOBBYIST', 'GARDENING,PETS', 'USER', NULL, NULL, NULL, 'ACTIVE', DATE_SUB(NOW(), INTERVAL 56 DAY)),
    ('jas.cruz', 'jas.cruz@users.cas.local', '$2a$12$o9YJXR0LhPQA2ikZExtdL.e5AdbOaD07EWt42x1dYtHlxx4mSZtSy', 'Jasmine Cruz', 'HOBBYIST', 'FOOD_COOKING,HOUSEKEEPING', 'USER', NULL, NULL, NULL, 'ACTIVE', DATE_SUB(NOW(), INTERVAL 53 DAY)),
    ('vince.navarro', 'vince.navarro@users.cas.local', '$2a$12$o9YJXR0LhPQA2ikZExtdL.e5AdbOaD07EWt42x1dYtHlxx4mSZtSy', 'Vince Navarro', 'HOBBYIST', 'HOME_SECURITY,ENERGY_SAVING', 'USER', NULL, NULL, NULL, 'ACTIVE', DATE_SUB(NOW(), INTERVAL 50 DAY)),
    ('tonio.salazar', 'tonio.salazar@users.cas.local', '$2a$12$o9YJXR0LhPQA2ikZExtdL.e5AdbOaD07EWt42x1dYtHlxx4mSZtSy', 'Antonio Salazar', 'HOBBYIST', 'GARDENING,FOOD_COOKING', 'USER', NULL, NULL, NULL, 'ACTIVE', DATE_SUB(NOW(), INTERVAL 48 DAY)),
    ('ria.domingo', 'ria.domingo@users.cas.local', '$2a$12$o9YJXR0LhPQA2ikZExtdL.e5AdbOaD07EWt42x1dYtHlxx4mSZtSy', 'Ria Domingo', 'HOBBYIST', 'PETS,HEALTH_WELLNESS', 'USER', NULL, NULL, NULL, 'ACTIVE', DATE_SUB(NOW(), INTERVAL 46 DAY)),
    ('dennis.uy', 'dennis.uy@users.cas.local', '$2a$12$o9YJXR0LhPQA2ikZExtdL.e5AdbOaD07EWt42x1dYtHlxx4mSZtSy', 'Dennis Uy', 'HOBBYIST', 'ENERGY_SAVING', 'USER', NULL, NULL, NULL, 'ACTIVE', DATE_SUB(NOW(), INTERVAL 39 DAY)),
    ('grace.valdez', 'grace.valdez@users.cas.local', '$2a$12$o9YJXR0LhPQA2ikZExtdL.e5AdbOaD07EWt42x1dYtHlxx4mSZtSy', 'Grace Valdez', 'HOBBYIST', 'GARDENING,HOUSEKEEPING', 'USER', NULL, NULL, NULL, 'ACTIVE', DATE_SUB(NOW(), INTERVAL 35 DAY)),
    ('noel.flores', 'noel.flores@users.cas.local', '$2a$12$o9YJXR0LhPQA2ikZExtdL.e5AdbOaD07EWt42x1dYtHlxx4mSZtSy', 'Noel Flores', 'HOBBYIST', 'PETS,HOME_SECURITY', 'USER', NULL, NULL, NULL, 'ACTIVE', DATE_SUB(NOW(), INTERVAL 43 DAY)),
    ('ivy.morales', 'ivy.morales@users.cas.local', '$2a$12$o9YJXR0LhPQA2ikZExtdL.e5AdbOaD07EWt42x1dYtHlxx4mSZtSy', 'Ivy Morales', 'HOBBYIST', 'FOOD_COOKING,STUDIES', 'USER', NULL, NULL, NULL, 'ACTIVE', DATE_SUB(NOW(), INTERVAL 42 DAY)),
    ('engr.mendoza', 'engr.mendoza@users.cas.local', '$2a$12$o9YJXR0LhPQA2ikZExtdL.e5AdbOaD07EWt42x1dYtHlxx4mSZtSy', 'Engr. Ramon Mendoza', 'PROFESSIONAL', 'ENERGY_SAVING,HOME_SECURITY', 'EXPERT', 'ADMIN', 'Registered Electronics Engineer, 9 years in building automation (sample account)', DATE_SUB(NOW(), INTERVAL 58 DAY), 'ACTIVE', DATE_SUB(NOW(), INTERVAL 60 DAY)),
    ('angela.torres', 'angela.torres@users.cas.local', '$2a$12$o9YJXR0LhPQA2ikZExtdL.e5AdbOaD07EWt42x1dYtHlxx4mSZtSy', 'Angela Torres', 'PROFESSIONAL', 'HOUSEKEEPING,ENERGY_SAVING', 'USER', NULL, NULL, NULL, 'ACTIVE', DATE_SUB(NOW(), INTERVAL 50 DAY)),
    ('mark.gonzales', 'mark.gonzales@users.cas.local', '$2a$12$o9YJXR0LhPQA2ikZExtdL.e5AdbOaD07EWt42x1dYtHlxx4mSZtSy', 'Mark Gonzales', 'PROFESSIONAL', 'HOME_SECURITY', 'USER', NULL, NULL, NULL, 'ACTIVE', DATE_SUB(NOW(), INTERVAL 47 DAY)),
    ('camille.rivera', 'camille.rivera@users.cas.local', '$2a$12$o9YJXR0LhPQA2ikZExtdL.e5AdbOaD07EWt42x1dYtHlxx4mSZtSy', 'Camille Rivera', 'PROFESSIONAL', 'HEALTH_WELLNESS,FOOD_COOKING', 'USER', NULL, NULL, NULL, 'ACTIVE', DATE_SUB(NOW(), INTERVAL 44 DAY)),
    ('rj.manalastas', 'rj.manalastas@users.cas.local', '$2a$12$o9YJXR0LhPQA2ikZExtdL.e5AdbOaD07EWt42x1dYtHlxx4mSZtSy', 'RJ Manalastas', 'PROFESSIONAL', 'ENERGY_SAVING,HOME_SECURITY', 'USER', NULL, NULL, NULL, 'ACTIVE', DATE_SUB(NOW(), INTERVAL 38 DAY)),
    ('lea.pineda', 'lea.pineda@users.cas.local', '$2a$12$o9YJXR0LhPQA2ikZExtdL.e5AdbOaD07EWt42x1dYtHlxx4mSZtSy', 'Lea Pineda', 'PROFESSIONAL', 'PETS,HOUSEKEEPING', 'USER', NULL, NULL, NULL, 'ACTIVE', DATE_SUB(NOW(), INTERVAL 34 DAY)),
    ('ken.yap', 'ken.yap@users.cas.local', '$2a$12$o9YJXR0LhPQA2ikZExtdL.e5AdbOaD07EWt42x1dYtHlxx4mSZtSy', 'Kenneth Yap', 'PROFESSIONAL', 'STUDIES,ENERGY_SAVING', 'USER', NULL, NULL, NULL, 'ACTIVE', DATE_SUB(NOW(), INTERVAL 29 DAY)),
    ('arnel.bernardo', 'arnel.bernardo@users.cas.local', '$2a$12$o9YJXR0LhPQA2ikZExtdL.e5AdbOaD07EWt42x1dYtHlxx4mSZtSy', 'Arnel Bernardo', 'PROFESSIONAL', 'GARDENING,FOOD_COOKING', 'USER', NULL, NULL, NULL, 'ACTIVE', DATE_SUB(NOW(), INTERVAL 25 DAY)),
    ('sir.belen', 'sir.belen@users.cas.local', '$2a$12$o9YJXR0LhPQA2ikZExtdL.e5AdbOaD07EWt42x1dYtHlxx4mSZtSy', 'Rodel Belen', 'EDUCATOR', 'STUDIES,ENERGY_SAVING', 'USER', NULL, NULL, NULL, 'ACTIVE', DATE_SUB(NOW(), INTERVAL 57 DAY)),
    ('liza.cabrera', 'liza.cabrera@users.cas.local', '$2a$12$o9YJXR0LhPQA2ikZExtdL.e5AdbOaD07EWt42x1dYtHlxx4mSZtSy', 'Liza Cabrera', 'EDUCATOR', 'STUDIES,HEALTH_WELLNESS', 'USER', NULL, NULL, NULL, 'ACTIVE', DATE_SUB(NOW(), INTERVAL 53 DAY)),
    ('joy.villareal', 'joy.villareal@users.cas.local', '$2a$12$o9YJXR0LhPQA2ikZExtdL.e5AdbOaD07EWt42x1dYtHlxx4mSZtSy', 'Joy Villareal', 'EDUCATOR', 'GARDENING,STUDIES', 'USER', NULL, NULL, NULL, 'ACTIVE', DATE_SUB(NOW(), INTERVAL 48 DAY)),
    ('edwin.lagman', 'edwin.lagman@users.cas.local', '$2a$12$o9YJXR0LhPQA2ikZExtdL.e5AdbOaD07EWt42x1dYtHlxx4mSZtSy', 'Edwin Lagman', 'EDUCATOR', 'HOME_SECURITY,STUDIES', 'USER', NULL, NULL, NULL, 'ACTIVE', DATE_SUB(NOW(), INTERVAL 41 DAY)),
    ('rose.alcantara', 'rose.alcantara@users.cas.local', '$2a$12$o9YJXR0LhPQA2ikZExtdL.e5AdbOaD07EWt42x1dYtHlxx4mSZtSy', 'Rosemarie Alcantara', 'EDUCATOR', 'FOOD_COOKING,HEALTH_WELLNESS', 'USER', NULL, NULL, NULL, 'ACTIVE', DATE_SUB(NOW(), INTERVAL 36 DAY)),
    ('nestor.ilagan', 'nestor.ilagan@users.cas.local', '$2a$12$o9YJXR0LhPQA2ikZExtdL.e5AdbOaD07EWt42x1dYtHlxx4mSZtSy', 'Nestor Ilagan', 'EDUCATOR', 'ENERGY_SAVING,PETS', 'USER', NULL, NULL, NULL, 'ACTIVE', DATE_SUB(NOW(), INTERVAL 27 DAY)),
    ('cheapkits.ph', 'cheapkits.ph@users.cas.local', '$2a$12$o9YJXR0LhPQA2ikZExtdL.e5AdbOaD07EWt42x1dYtHlxx4mSZtSy', 'Cheap Kits PH', 'HOBBYIST', '', 'USER', NULL, NULL, NULL, 'SUSPENDED', DATE_SUB(NOW(), INTERVAL 12 DAY));

-- Sample guides (schematics and step pictures are added by sample_media.sql)
INSERT INTO guides (author_id, title, category, summary, parts_list, precautions, steps, video_url,
                    estimated_cost, difficulty, risk_factor, status, approval, approved_by, approved_at, created_at) VALUES
    ((SELECT user_id FROM users WHERE username = 'jas.cruz'), 'Digital Kitchen Scale with HX711', 'FOOD_COOKING',
     'Weigh ingredients up to 5 kg on a homemade scale built from a load cell, an HX711 amplifier and a 16x2 LCD. Put a bowl on top and press the button to zero it.',
     'Arduino Uno\n5 kg load cell with HX711 amplifier board\n16x2 LCD with I2C backpack\nPush button\nTwo 15 x 15 cm plywood or acrylic plates\nM4 and M5 screws for the load cell\nJumper wires',
     'Do not put more than 5 kg on it or the load cell bends for good.\nMount the load cell so the top plate only rests on the end with the arrow.\nKeep liquids in a closed container when weighing near the electronics.',
     '1. Screw the load cell between the two plates so each plate only touches one end of the bar.\n2. Connect the load cell''s red, black, white and green wires to E+, E-, A- and A+ on the HX711.\n3. Wire the HX711 DT to pin 3 and SCK to pin 2, the LCD to A4 and A5, and the button between pin 4 and GND.\n4. Install the HX711 library by Bogdan Necula and the LiquidCrystal I2C library, then upload the sketch.\n5. Calibrate: put something you know the weight of, like a 500 g pack of sugar, and change CAL until the screen shows 500 g.\n6. Put your bowl on the scale, press the button to zero it, and start adding ingredients.',
     'https://www.youtube.com/watch?v=S12Mp8gDJmI', 930.00, 'BEGINNER', 'LOW', 'PUBLISHED',
     NULL, NULL, NULL, DATE_SUB(NOW(), INTERVAL 34 DAY));
INSERT INTO guides (author_id, title, category, summary, parts_list, precautions, steps, video_url,
                    estimated_cost, difficulty, risk_factor, status, approval, approved_by, approved_at, created_at) VALUES
    ((SELECT user_id FROM users WHERE username = 'ivy.morales'), 'Waterproof Food Thermometer with Alarm', 'FOOD_COOKING',
     'Check milk, soup, coffee water or melted chocolate with a stainless steel DS18B20 probe. An OLED shows the temperature and a buzzer beeps when it reaches your target.',
     'Arduino Nano\nDS18B20 waterproof temperature probe\n4.7k ohm resistor\n0.96 inch OLED display (I2C)\nActive buzzer\nTwo push buttons\nSmall project box',
     'Only the steel tip and the cable go into food. Keep the board away from steam and the stove.\nThe probe cable is only rated to about 100 C, so do not use it for frying oil or the oven.\nWash the probe with soap after each use and never put the box under water.',
     '1. Solder the 4.7k resistor between the probe''s red (VCC) and yellow (DATA) wires.\n2. Wire the probe: red to 5V, black to GND and yellow to pin 2.\n3. Connect the OLED to A4 and A5, the buzzer to pin 8, and the two buttons to pins 5 and 6.\n4. Install the OneWire, DallasTemperature and Adafruit SSD1306 libraries, then upload the sketch.\n5. Set your target with the buttons, put the probe in warm water, and wait for the beep.',
     'https://www.youtube.com/watch?v=tsRHey47n5A', 640.00, 'BEGINNER', 'LOW', 'PUBLISHED',
     NULL, NULL, NULL, DATE_SUB(NOW(), INTERVAL 29 DAY));
INSERT INTO guides (author_id, title, category, summary, parts_list, precautions, steps, video_url,
                    estimated_cost, difficulty, risk_factor, status, approval, approved_by, approved_at, created_at) VALUES
    ((SELECT user_id FROM users WHERE username = 'carlo.garcia'), 'Soil Moisture Traffic Light', 'GARDENING',
     'Stick a sensor in the pot and three LEDs tell you at a glance: green is fine, yellow means water soon, and red means water now.',
     'Arduino Nano\nSoil moisture sensor with comparator board (FC-28 or YL-69)\nRed, yellow and green 5 mm LEDs\nThree 220 ohm resistors\nSmall waterproof box\nUSB charger and cable',
     'Keep the Nano box out of the rain and off wet soil.\nThe metal probe slowly rusts, so the sketch only powers it for a moment once a minute (through pin 7).',
     '1. Push the sensor probe into the soil about 3 cm from the stem.\n2. Wire the sensor board: AO to A0, VCC to pin 7 and GND to GND.\n3. Wire each LED through a 220 ohm resistor: red to pin 9, yellow to pin 10 and green to pin 11.\n4. Upload the sketch and open the Serial Monitor to see the readings.\n5. Write down the number for dry soil and for just-watered soil, put them in the sketch, and close the box.',
     'https://www.youtube.com/watch?v=LvsQFsVpHro', 380.00, 'BEGINNER', 'LOW', 'PUBLISHED',
     NULL, NULL, NULL, DATE_SUB(NOW(), INTERVAL 37 DAY));
INSERT INTO guides (author_id, title, category, summary, parts_list, precautions, steps, video_url,
                    estimated_cost, difficulty, risk_factor, status, approval, approved_by, approved_at, created_at) VALUES
    ((SELECT user_id FROM users WHERE username = 'tonio.salazar'), 'Mini Greenhouse Fan Controller', 'GARDENING',
     'Keep seedlings from cooking on hot afternoons. A DHT11 reads the temperature inside a small greenhouse and switches a 12V fan on above 32 C.',
     'Arduino Uno\nDHT11 temperature and humidity module\n5V relay module (1 channel)\n12V DC fan (80 or 120 mm)\n12V 1A power adapter\nDC barrel jack adapter\nJumper wires and a small box',
     'Only switch the 12V fan with this relay. Do not connect 230V appliances to this build.\nKeep the adapter and the Arduino outside the greenhouse or in a sealed box, since it is very humid inside.\nCheck the fan''s red and black wires before plugging in the adapter.',
     '1. Mount the DHT11 in the middle of the greenhouse, out of direct sun.\n2. Wire the DHT11 data pin to pin 2, VCC to 5V and GND to GND.\n3. Wire the relay IN to pin 7, VCC to 5V and GND to GND.\n4. Connect the adapter''s + to the relay COM, the relay NO to the fan''s red wire, and the fan''s black wire back to the adapter''s -.\n5. Install the DHT sensor library by Adafruit and upload the sketch.\n6. Warm the sensor with your hand or a hair dryer on low and check that the fan turns on.',
     'https://www.youtube.com/watch?v=g34-9K6Mpyo', 900.00, 'INTERMEDIATE', 'MODERATE', 'PUBLISHED',
     NULL, NULL, NULL, DATE_SUB(NOW(), INTERVAL 24 DAY));
INSERT INTO guides (author_id, title, category, summary, parts_list, precautions, steps, video_url,
                    estimated_cost, difficulty, risk_factor, status, approval, approved_by, approved_at, created_at) VALUES
    ((SELECT user_id FROM users WHERE username = 'andrea.villanueva'), 'Pomodoro Study Timer', 'STUDIES',
     'A desk timer for the Pomodoro method: 25 minutes of focus, then a 5 minute break. An OLED shows the countdown and a short beep tells you when to switch.',
     'Arduino Nano\n0.96 inch OLED display (I2C)\nPush button\nPassive buzzer\nSmall case (soap box or 3D printed)\nUSB cable',
     'Keep the beep short and quiet if you study in a shared room.\nUse a USB charger or power bank, not a 9V battery, so it lasts the whole day.',
     '1. Connect the OLED: VCC to 5V, GND to GND, SDA to A4 and SCL to A5.\n2. Wire the button between pin 2 and GND, and the buzzer between pin 9 and GND.\n3. Install the Adafruit SSD1306 and Adafruit GFX libraries, then upload the sketch.\n4. Press the button once to start a focus session and again to pause it.\n5. After four focus sessions, take a longer 15 minute break.',
     'https://www.youtube.com/watch?v=_f6aSjYSVHM', 540.00, 'BEGINNER', 'LOW', 'PUBLISHED',
     NULL, NULL, NULL, DATE_SUB(NOW(), INTERVAL 31 DAY));
INSERT INTO guides (author_id, title, category, summary, parts_list, precautions, steps, video_url,
                    estimated_cost, difficulty, risk_factor, status, approval, approved_by, approved_at, created_at) VALUES
    ((SELECT user_id FROM users WHERE username = 'liza.cabrera'), 'Posture and Screen Distance Reminder', 'STUDIES',
     'Clip an ultrasonic sensor on top of your monitor. If your face stays closer than 40 cm for 10 seconds, a soft beep and a red LED remind you to sit back.',
     'Arduino Nano\nHC-SR04 ultrasonic sensor\nPassive buzzer\nRed LED and 220 ohm resistor\nBinder clip or 3D printed monitor clip\nUSB cable',
     'Aim the sensor at your face or chest, not at a wall behind you.\nThis is a reminder, not a medical device. See a doctor for back or eye pain.',
     '1. Clip the HC-SR04 on top of the monitor, facing you.\n2. Wire VCC to 5V, GND to GND, Trig to pin 9 and Echo to pin 10.\n3. Wire the buzzer to pin 8, and the LED through the resistor to pin 13.\n4. Upload the sketch and open the Serial Monitor to see your distance.\n5. Sit the way you normally do, note the distance, and set LIMIT about 10 cm closer than that.',
     'https://www.youtube.com/watch?v=_eBCxzMrL5Q', 400.00, 'BEGINNER', 'LOW', 'PUBLISHED',
     NULL, NULL, NULL, DATE_SUB(NOW(), INTERVAL 21 DAY));
INSERT INTO guides (author_id, title, category, summary, parts_list, precautions, steps, video_url,
                    estimated_cost, difficulty, risk_factor, status, approval, approved_by, approved_at, created_at) VALUES
    ((SELECT user_id FROM users WHERE username = 'pao.dizon'), 'Rain Alarm for the Clothesline', 'HOUSEKEEPING',
     'Never lose a load of laundry to a sudden downpour. A rain plate on the sampayan beeps a buzzer inside the house the moment the first drops land.',
     'Arduino Nano\nRain sensor plate with comparator module\nActive buzzer\nRed LED and 220 ohm resistor\n5 meters of 3-core wire\n5V USB charger or a 9V battery with clip\nSmall plastic box',
     'Only the sensor plate goes outside. The Nano and the comparator module stay indoors.\nMount the plate tilted so water runs off and it dries quickly after the rain.\nCover the wire joints at the plate with heat shrink or electrical tape.',
     '1. Mount the rain plate on the clothesline post, tilted about 30 degrees.\n2. Run the 3-core wire indoors and connect the plate to the comparator module.\n3. Wire the module: VCC to 5V, GND to GND and DO to pin 2.\n4. Wire the buzzer to pin 8, and the LED through the resistor to pin 13.\n5. Upload the sketch, then turn the module''s blue knob until its small LED turns off when the plate is dry.\n6. Sprinkle a few drops of water on the plate to test the alarm.',
     'https://www.youtube.com/watch?v=91xJI2pgOd0', 470.00, 'BEGINNER', 'LOW', 'PUBLISHED',
     NULL, NULL, NULL, DATE_SUB(NOW(), INTERVAL 44 DAY));
INSERT INTO guides (author_id, title, category, summary, parts_list, precautions, steps, video_url,
                    estimated_cost, difficulty, risk_factor, status, approval, approved_by, approved_at, created_at) VALUES
    ((SELECT user_id FROM users WHERE username = 'kurt.pascual'), 'Touchless Trash Can Lid', 'HOUSEKEEPING',
     'Wave your hand over the bin and the lid opens by itself, then closes after 3 seconds. No more touching a dirty lid in the kitchen.',
     'Arduino Uno\nHC-SR04 ultrasonic sensor\nMG90S metal gear servo\n4 x AA battery holder for the servo\nTrash can with a hinged lid\nPopsicle sticks, glue gun and strong string',
     'Power the servo from the battery pack, not the Arduino''s 5V pin, or the Arduino keeps resetting.\nConnect the battery pack''s - to the Arduino GND so both share the same ground.\nKeep fingers away from the lid hinge while testing.',
     '1. Hot glue the HC-SR04 on the front of the lid, facing up.\n2. Mount the servo on the back of the bin and tie its horn to the lid with string.\n3. Wire the sensor: VCC to 5V, GND to GND, Trig to pin 5 and Echo to pin 6.\n4. Wire the servo: signal to pin 9, red to the battery pack +, and brown to GND.\n5. Upload the sketch and change OPEN_ANGLE until the lid opens all the way.',
     'https://www.youtube.com/watch?v=pNcWlpd4Evo', 800.00, 'BEGINNER', 'LOW', 'PUBLISHED',
     NULL, NULL, NULL, DATE_SUB(NOW(), INTERVAL 27 DAY));
INSERT INTO guides (author_id, title, category, summary, parts_list, precautions, steps, video_url,
                    estimated_cost, difficulty, risk_factor, status, approval, approved_by, approved_at, created_at) VALUES
    ((SELECT user_id FROM users WHERE username = 'pao.dizon'), 'Room Light Auto-Off with Visitor Counter', 'ENERGY_SAVING',
     'Two IR sensors at the door count people going in and out. The room light turns on when the first person enters and turns off when the last one leaves, so lights are never left on in an empty room.',
     'Arduino Uno\nTwo IR obstacle sensor modules\n16x2 LCD with I2C backpack\n5V relay module rated 10A at 250V AC\nEnclosed project box for the relay\nWire rated for 10A\nWall switch box and screws',
     'The relay switches the 230V room light. Turn off the breaker before touching the light wiring.\nOnly the LIVE wire goes through the relay. Neutral goes straight to the light.\nKeep every 230V connection inside a closed box, and have an electrician check it before you switch the breaker back on.',
     '1. Mount the two IR sensors on the door frame about 10 cm apart, sensor A outside and sensor B inside.\n2. Wire sensor A OUT to pin 2, sensor B OUT to pin 3, and both VCC and GND to 5V and GND.\n3. Connect the LCD to A4 and A5, and the relay IN to pin 8.\n4. With the breaker OFF, cut the light''s LIVE wire and connect one end to the relay COM and the other end to NO.\n5. Upload the sketch and walk in and out a few times to check the count.\n6. Close the relay box and turn the breaker back on.',
     'https://www.youtube.com/watch?v=xWSSCCZsMEU', 1050.00, 'INTERMEDIATE', 'HIGH', 'PUBLISHED',
     'Y', (SELECT user_id FROM users WHERE username = 'engr.mendoza'), DATE_SUB(NOW(), INTERVAL 39 DAY), DATE_SUB(NOW(), INTERVAL 40 DAY));
INSERT INTO guides (author_id, title, category, summary, parts_list, precautions, steps, video_url,
                    estimated_cost, difficulty, risk_factor, status, approval, approved_by, approved_at, created_at) VALUES
    ((SELECT user_id FROM users WHERE username = 'rj.manalastas'), 'Home Energy Meter with PZEM-004T', 'ENERGY_SAVING',
     'Measure the voltage, current, power and kWh of a circuit at home with a PZEM-004T and an ESP32, and see the numbers on your phone over Wi-Fi.',
     'ESP32 development board\nPZEM-004T v3.0 with 100A CT coil\n5V 1A power supply module (HLK-PM01)\nEnclosed DIN-rail or project box\nWire rated for the circuit\nTerminal blocks',
     'This guide connects to 230V mains. Turn off the breaker before opening anything.\nThe CT coil clamps around the LIVE wire only. Never clamp both live and neutral together.\nMount everything in a closed, non-metal box and have a licensed electrician do the panel connections.',
     '1. With the breaker OFF, clamp the CT coil around the LIVE wire of the circuit you want to measure.\n2. Connect the PZEM''s L and N voltage terminals to the same circuit with terminal blocks.\n3. Wire the PZEM''s 5V, GND, TX and RX to the ESP32: TX to GPIO16 and RX to GPIO17.\n4. Install the PZEM004Tv30 library and upload the sketch with your Wi-Fi name and password.\n5. Close the box, turn the breaker on, and open the ESP32''s IP address on your phone.',
     'https://www.youtube.com/watch?v=0igolzx-FS4', 1350.00, 'ADVANCED', 'HIGH', 'PENDING',
     NULL, NULL, NULL, DATE_SUB(NOW(), INTERVAL 3 DAY));
INSERT INTO guides (author_id, title, category, summary, parts_list, precautions, steps, video_url,
                    estimated_cost, difficulty, risk_factor, status, approval, approved_by, approved_at, created_at) VALUES
    ((SELECT user_id FROM users WHERE username = 'josh.ramos'), 'Laser Tripwire Alarm', 'HOME_SECURITY',
     'Shine a laser across a hallway or doorway into a light sensor. When someone breaks the beam, a buzzer goes off until you press reset.',
     'Arduino Nano\n5V laser diode module\nLDR light sensor module\nActive buzzer\nPush button for reset\nTwo small boxes or 3D printed holders\nLong jumper wires',
     'Never look into the laser or point it at people''s eyes, even a small module can hurt eyes.\nMount the laser below eye level, for example at ankle height across the doorway.',
     '1. Mount the laser on one side of the doorway and the LDR module on the other side, at ankle height.\n2. Wire the laser S pin to pin 7, and its - to GND.\n3. Wire the LDR module: VCC to 5V, GND to GND and DO to pin 2.\n4. Wire the buzzer to pin 8 and the reset button between pin 4 and GND.\n5. Upload the sketch, aim the laser at the LDR, and turn the module''s knob until its LED just turns off.\n6. Walk through the beam to test the alarm, then press reset.',
     'https://www.youtube.com/watch?v=NEzStFXjyZs', 420.00, 'BEGINNER', 'LOW', 'PUBLISHED',
     NULL, NULL, NULL, DATE_SUB(NOW(), INTERVAL 35 DAY));
INSERT INTO guides (author_id, title, category, summary, parts_list, precautions, steps, video_url,
                    estimated_cost, difficulty, risk_factor, status, approval, approved_by, approved_at, created_at) VALUES
    ((SELECT user_id FROM users WHERE username = 'pao.dizon'), 'RFID Door Lock with Solenoid', 'HOME_SECURITY',
     'Tap an RFID card or key fob to unlock a cabinet or room door for 5 seconds. Unknown cards get a double beep and the door stays locked.',
     'Arduino Uno\nRC522 RFID reader with card and key fob\n12V solenoid lock\n5V relay module\n12V 2A power adapter\n1N4007 diode\nActive buzzer\nProject box',
     'The RC522 runs on 3.3V. Connecting it to 5V can damage it.\nPut the 1N4007 diode across the solenoid (stripe to +) to protect the relay from voltage spikes.\nKeep a key or another way in. If the power goes out, a fail-secure lock stays locked.',
     '1. Wire the RC522 to the Uno: SDA to 10, SCK to 13, MOSI to 11, MISO to 12, RST to 9, 3.3V to 3.3V and GND to GND.\n2. Wire the relay IN to pin 7 and the buzzer to pin 6.\n3. Connect the adapter''s + to the relay COM, NO to the lock''s +, and the lock''s - back to the adapter''s -.\n4. Install the MFRC522 library, upload the sketch, and open the Serial Monitor.\n5. Tap your card, copy the UID it prints into the sketch, and upload again.\n6. Mount the reader inside the door so it reads through the wood or plastic.',
     'https://www.youtube.com/watch?v=E4sRQ7teQ2w', 1200.00, 'INTERMEDIATE', 'MODERATE', 'PUBLISHED',
     NULL, NULL, NULL, DATE_SUB(NOW(), INTERVAL 19 DAY));
INSERT INTO guides (author_id, title, category, summary, parts_list, precautions, steps, video_url,
                    estimated_cost, difficulty, risk_factor, status, approval, approved_by, approved_at, created_at) VALUES
    ((SELECT user_id FROM users WHERE username = 'camille.rivera'), 'Touchless Hand Sanitizer Dispenser', 'HEALTH_WELLNESS',
     'Put your hand under the nozzle and a small pump squirts one dose of alcohol or soap. Made for the clinic door at work, cheap enough for every classroom.',
     'Arduino Nano\nIR obstacle sensor module\n5V relay module\n5V mini submersible pump and silicone tube\nPlastic bottle for the alcohol\n5V 2A USB adapter\nSmall project box',
     'Alcohol is flammable. Keep the electronics in a separate closed box above the bottle, never inside it.\nUse only a low voltage 5V pump.\nWipe up spills right away, alcohol and electronics do not mix.',
     '1. Put the pump in the bottle and run the tube up to a nozzle under the box.\n2. Mount the IR sensor next to the nozzle, facing down.\n3. Wire the sensor OUT to pin 2, and VCC and GND to 5V and GND.\n4. Wire the relay IN to pin 7, then the adapter''s + to COM, NO to the pump''s +, and the pump''s - to the adapter''s -.\n5. Upload the sketch and change SQUIRT_MS until one squirt is enough for your hands.',
     'https://www.youtube.com/watch?v=_vYrPlYFLcM', 650.00, 'BEGINNER', 'LOW', 'PUBLISHED',
     NULL, NULL, NULL, DATE_SUB(NOW(), INTERVAL 33 DAY));
INSERT INTO guides (author_id, title, category, summary, parts_list, precautions, steps, video_url,
                    estimated_cost, difficulty, risk_factor, status, approval, approved_by, approved_at, created_at) VALUES
    ((SELECT user_id FROM users WHERE username = 'mika.ocampo'), 'Heart Rate Monitor with Pulse Sensor', 'HEALTH_WELLNESS',
     'Rest your fingertip on a pulse sensor and see your heart rate in beats per minute on an OLED, with a small heart that blinks on every beat.',
     'Arduino Uno\nPulse Sensor (green round sensor)\n0.96 inch OLED display (I2C)\nVelcro strap for the finger\nJumper wires',
     'This is a school project, not a medical device. Do not use it to check a health condition.\nRest your finger lightly. Pressing hard squeezes out the blood and gives wrong readings.',
     '1. Wire the Pulse Sensor: + to 5V, - to GND and S to A0.\n2. Connect the OLED: VCC to 5V, GND to GND, SDA to A4 and SCL to A5.\n3. Install the PulseSensor Playground and Adafruit SSD1306 libraries.\n4. Upload the sketch, strap the sensor on your fingertip, and sit still for 10 seconds.\n5. Compare the BPM with a smartwatch or by counting your pulse for 30 seconds and doubling it.',
     'https://www.youtube.com/watch?v=JpN-S6ALUWw', 780.00, 'BEGINNER', 'LOW', 'PUBLISHED',
     NULL, NULL, NULL, DATE_SUB(NOW(), INTERVAL 16 DAY));
INSERT INTO guides (author_id, title, category, summary, parts_list, precautions, steps, video_url,
                    estimated_cost, difficulty, risk_factor, status, approval, approved_by, approved_at, created_at) VALUES
    ((SELECT user_id FROM users WHERE username = 'ria.domingo'), 'Automatic Pet Feeder with Real-Time Clock', 'PETS',
     'Feed your dog or cat at 7 AM and 6 PM even when nobody is home. A DS3231 clock keeps time and a servo turns a cereal dispenser for one portion.',
     'Arduino Uno\nDS3231 real-time clock module with CR2032 battery\nMG996R servo\nCereal dispenser or a big plastic bottle with a rotating flap\nPush button for manual feeding\n5V 2A adapter for the servo\nWood or acrylic stand',
     'Power the MG996R from its own 5V 2A adapter and connect its - to the Arduino GND.\nTest the portion size for a few days while you are home before trusting it alone.\nUse dry food only. Wet food spoils and jams the dispenser.',
     '1. Attach the servo horn to the dispenser''s knob or flap so one turn drops one portion.\n2. Wire the DS3231: VCC to 5V, GND to GND, SDA to A4 and SCL to A5.\n3. Wire the servo signal to pin 9, its red wire to the adapter +, and its brown wire to GND.\n4. Wire the feed button between pin 2 and GND.\n5. Install the RTClib library, upload the sketch once to set the clock, then upload it again with the setup line removed.\n6. Fill it with food and press the button to test one portion.',
     'https://www.youtube.com/watch?v=mjiexlL6Cfo', 950.00, 'INTERMEDIATE', 'LOW', 'PUBLISHED',
     NULL, NULL, NULL, DATE_SUB(NOW(), INTERVAL 26 DAY));
INSERT INTO guides (author_id, title, category, summary, parts_list, precautions, steps, video_url,
                    estimated_cost, difficulty, risk_factor, status, approval, approved_by, approved_at, created_at) VALUES
    ((SELECT user_id FROM users WHERE username = 'noel.flores'), 'Automatic Fish Feeder', 'PETS',
     'A small container on a servo tips a pinch of fish food into the tank every few hours. Turn a knob to choose how often, from every 4 to every 24 hours.',
     'Arduino Nano\nSG90 servo\n10k potentiometer\nPush button for feeding now\nSmall pill container or film canister\nUSB charger\nClamp or bracket for the tank rim',
     'Mount everything above the water on the tank rim, away from splashes.\nStart with a tiny portion. Overfeeding dirties the water and harms the fish.\nUnplug the charger before cleaning the tank.',
     '1. Drill a 5 mm hole in the side of the container and glue it to the servo horn.\n2. Clamp the servo on the tank rim so the container tips over the water.\n3. Wire the servo signal to pin 9, red to 5V and brown to GND.\n4. Wire the potentiometer''s middle pin to A0 and its outer pins to 5V and GND, and the button between pin 2 and GND.\n5. Upload the sketch, set the knob, and press the button to test a portion.',
     'https://www.youtube.com/watch?v=AQl1W4WqoNQ', 400.00, 'BEGINNER', 'LOW', 'PUBLISHED',
     NULL, NULL, NULL, DATE_SUB(NOW(), INTERVAL 13 DAY));
INSERT INTO guides (author_id, title, category, summary, parts_list, precautions, steps, estimated_cost,
                    difficulty, risk_factor, status, created_at) VALUES
    ((SELECT user_id FROM users WHERE username = 'cheapkits.ph'), 'CHEAP ARDUINO KITS 50% OFF!!! Message me', 'STUDIES', 'Best price arduino kits shipping nationwide, message me now!!!', 'Arduino kit',
     '', '1. Message me', NULL, 'BEGINNER', 'LOW', 'HIDDEN', DATE_SUB(NOW(), INTERVAL 11 DAY));

-- Video links for the starter guides that do not have one yet
UPDATE guides SET video_url = 'https://www.youtube.com/watch?v=t3ODW-eG3y0' WHERE title = 'Automatic Plant Watering System' AND video_url IS NULL;
UPDATE guides SET video_url = 'https://www.youtube.com/watch?v=pDpM-D-qMhA' WHERE title = 'Door Sensor Alarm with Buzzer' AND video_url IS NULL;
UPDATE guides SET video_url = 'https://www.youtube.com/watch?v=ugDPdgGekNo' WHERE title = 'Motion-Activated Hallway Light' AND video_url IS NULL;
UPDATE guides SET video_url = 'https://www.youtube.com/watch?v=D1Uvg5eZ4Xs' WHERE title = 'Laundry Done Notifier' AND video_url IS NULL;
UPDATE guides SET video_url = 'https://www.youtube.com/watch?v=Pk1PBwtSPE8' WHERE title = 'Bedroom Air Quality Monitor' AND video_url IS NULL;
UPDATE guides SET video_url = 'https://www.youtube.com/watch?v=AURxODjCcoo' WHERE title = 'Pet Water Bowl Refill Alert' AND video_url IS NULL;
UPDATE guides SET video_url = 'https://www.youtube.com/watch?v=SPpgMl_TeIc' WHERE title = 'Solar Phone Charging Station' AND video_url IS NULL;

-- Votes and comments (one row per person per guide; a NULL vote is a comment without a vote)
INSERT INTO reviews (guide_id, user_id, is_approved, comment, created_at)
SELECT g.guide_id, u.user_id, r.ok, r.cm, DATE_SUB(NOW(), INTERVAL r.h HOUR)
FROM (
    SELECT 'bea.mendoza' AS un, FALSE AS ok, 'Readings jump by 5 to 10 g for me. Probably my plates are too flexible.' AS cm, 771 AS h
    UNION ALL SELECT 'dennis.uy', TRUE, 'Calibrated it with a 1 kg bag of rice, accurate to about 2 grams.', 793
    UNION ALL SELECT 'sir.belen', TRUE, 'Used this for baking class, the tare button is so handy.', 602
    UNION ALL SELECT 'edwin.lagman', TRUE, 'My load cell had blue instead of green wire, still worked as A+.', 453
    UNION ALL SELECT 'jm.castillo', TRUE, 'Mas mura pa kaysa sa binili kong scale sa palengke. Salamat!', 247
    UNION ALL SELECT 'lance.robles', TRUE, 'Tip: let it warm up for a minute, the first readings drift a bit.', 4
    UNION ALL SELECT 'mark.gonzales', TRUE, NULL, 115
    UNION ALL SELECT 'angela.torres', TRUE, NULL, 102
    UNION ALL SELECT 'tonio.salazar', TRUE, NULL, 703
    UNION ALL SELECT 'vince.navarro', TRUE, NULL, 323
    UNION ALL SELECT 'arnel.bernardo', TRUE, NULL, 259
    UNION ALL SELECT 'ivy.morales', TRUE, NULL, 367
    UNION ALL SELECT 'engr.mendoza', TRUE, NULL, 765
    UNION ALL SELECT 'ken.yap', TRUE, NULL, 80
    UNION ALL SELECT 'pao.dizon', TRUE, NULL, 93
    UNION ALL SELECT 'trisha.bautista', NULL, 'Can it go up to 10 kg if I buy a bigger load cell?', 600
    UNION ALL SELECT 'jas.cruz', NULL, 'Yes, get a 10 kg load cell and recalibrate. The wiring and sketch stay the same.', 581
) r
JOIN users u ON u.username = r.un COLLATE utf8mb4_general_ci
JOIN guides g ON g.title = 'Digital Kitchen Scale with HX711';

INSERT INTO reviews (guide_id, user_id, is_approved, comment, created_at)
SELECT g.guide_id, u.user_id, r.ok, r.cm, DATE_SUB(NOW(), INTERVAL r.h HOUR)
FROM (
    SELECT 'vince.navarro' AS un, FALSE AS ok, 'Takes about 10 seconds to settle, slower than a store thermometer.' AS cm, 410 AS h
    UNION ALL SELECT 'nestor.ilagan', TRUE, 'Perfect for tempering chocolate, I set it to 31 C.', 601
    UNION ALL SELECT 'lea.pineda', TRUE, 'My baby''s milk is never too hot now. Thank you!', 569
    UNION ALL SELECT 'ethan.lim', TRUE, 'The 4.7k resistor is important, without it I got -127 C.', 512
    UNION ALL SELECT 'tonio.salazar', TRUE, 'Put it in a soap box and it looks legit.', 121
    UNION ALL SELECT 'migs.santos', TRUE, NULL, 616
    UNION ALL SELECT 'noel.flores', TRUE, NULL, 409
    UNION ALL SELECT 'aly.manalo', TRUE, NULL, 266
    UNION ALL SELECT 'lance.robles', TRUE, NULL, 579
    UNION ALL SELECT 'mika.ocampo', TRUE, NULL, 492
    UNION ALL SELECT 'andrea.villanueva', TRUE, NULL, 27
    UNION ALL SELECT 'sam.delosreyes', NULL, 'Can I use this for deep frying?', 619
    UNION ALL SELECT 'ivy.morales', NULL, 'No, the cable is only good up to about 100 C. Frying oil is much hotter, so get a K-type thermocouple for that.', 610
) r
JOIN users u ON u.username = r.un COLLATE utf8mb4_general_ci
JOIN guides g ON g.title = 'Waterproof Food Thermometer with Alarm';

INSERT INTO reviews (guide_id, user_id, is_approved, comment, created_at)
SELECT g.guide_id, u.user_id, r.ok, r.cm, DATE_SUB(NOW(), INTERVAL r.h HOUR)
FROM (
    SELECT 'ethan.lim' AS un, FALSE AS ok, 'The cheap fork sensor still rusted after two months. Get a capacitive one.' AS cm, 719 AS h
    UNION ALL SELECT 'trisha.bautista', TRUE, 'Built three of these for my mom''s orchids.', 469
    UNION ALL SELECT 'liza.cabrera', TRUE, 'Powering the probe from a pin is a smart trick, mine lasted way longer.', 692
    UNION ALL SELECT 'jm.castillo', TRUE, 'Great first Arduino project for my Grade 9 class.', 482
    UNION ALL SELECT 'camille.rivera', TRUE, 'Changed the numbers to 650 and 450 for cactus soil.', 733
    UNION ALL SELECT 'ken.yap', TRUE, NULL, 560
    UNION ALL SELECT 'sam.delosreyes', TRUE, NULL, 413
    UNION ALL SELECT 'pao.dizon', TRUE, NULL, 317
    UNION ALL SELECT 'rj.manalastas', TRUE, NULL, 664
    UNION ALL SELECT 'bea.mendoza', TRUE, NULL, 492
    UNION ALL SELECT 'edwin.lagman', TRUE, NULL, 413
    UNION ALL SELECT 'rose.alcantara', TRUE, NULL, 303
    UNION ALL SELECT 'dennis.uy', TRUE, NULL, 11
    UNION ALL SELECT 'kyla.reyes', NULL, 'Will a capacitive sensor work with the same code?', 63
    UNION ALL SELECT 'carlo.garcia', NULL, 'Yes. Just remember capacitive sensors read lower when dry, so flip the comparisons.', 57
) r
JOIN users u ON u.username = r.un COLLATE utf8mb4_general_ci
JOIN guides g ON g.title = 'Soil Moisture Traffic Light';

INSERT INTO reviews (guide_id, user_id, is_approved, comment, created_at)
SELECT g.guide_id, u.user_id, r.ok, r.cm, DATE_SUB(NOW(), INTERVAL r.h HOUR)
FROM (
    SELECT 'rj.manalastas' AS un, FALSE AS ok, 'My relay was active LOW so the fan ran backwards from what I expected. The comment in the code saved me though.' AS cm, 446 AS h
    UNION ALL SELECT 'liza.cabrera', FALSE, 'DHT11 is not very accurate, a DHT22 is worth the extra 100 pesos.', 36
    UNION ALL SELECT 'ken.yap', TRUE, 'Seedlings survived the April heat this year, salamat!', 299
    UNION ALL SELECT 'noel.flores', TRUE, 'The 2 degree gap is a nice touch, my first version clicked nonstop.', 326
    UNION ALL SELECT 'aly.manalo', TRUE, 'Used a computer fan from an old PC, works fine.', 470
    UNION ALL SELECT 'pao.dizon', TRUE, 'Added a second fan on the same relay, no problem with a 2A adapter.', 102
    UNION ALL SELECT 'edwin.lagman', TRUE, NULL, 529
    UNION ALL SELECT 'mika.ocampo', TRUE, NULL, 189
    UNION ALL SELECT 'sir.belen', TRUE, NULL, 492
    UNION ALL SELECT 'ivy.morales', TRUE, NULL, 274
    UNION ALL SELECT 'angela.torres', TRUE, NULL, 381
    UNION ALL SELECT 'lance.robles', TRUE, 'Worked on the first upload, rare for me haha.', 423
    UNION ALL SELECT 'arnel.bernardo', NULL, 'Can I run a 220V stand fan instead?', 179
    UNION ALL SELECT 'tonio.salazar', NULL, 'Please don''t with this guide. Switching 230V needs an enclosed relay box and an expert check. Stick to 12V fans here.', 163
) r
JOIN users u ON u.username = r.un COLLATE utf8mb4_general_ci
JOIN guides g ON g.title = 'Mini Greenhouse Fan Controller';

INSERT INTO reviews (guide_id, user_id, is_approved, comment, created_at)
SELECT g.guide_id, u.user_id, r.ok, r.cm, DATE_SUB(NOW(), INTERVAL r.h HOUR)
FROM (
    SELECT 'kyla.reyes' AS un, FALSE AS ok, 'Wish it saved the session count when unplugged.' AS cm, 160 AS h
    UNION ALL SELECT 'nicole.fernandez', TRUE, 'Finished my thesis chapter with this, no phone needed.', 7
    UNION ALL SELECT 'nestor.ilagan', TRUE, 'I changed it to 50 and 10 minutes for review season.', 322
    UNION ALL SELECT 'rj.manalastas', TRUE, 'Cute in a pink soap box hehe.', 682
    UNION ALL SELECT 'rose.alcantara', TRUE, 'Simple and it actually works. My roommate wants one too.', 599
    UNION ALL SELECT 'mika.ocampo', TRUE, 'Used an active buzzer at first, tone() needs the passive kind.', 590
    UNION ALL SELECT 'carlo.garcia', TRUE, NULL, 505
    UNION ALL SELECT 'trisha.bautista', TRUE, NULL, 140
    UNION ALL SELECT 'ria.domingo', TRUE, NULL, 486
    UNION ALL SELECT 'migs.santos', TRUE, NULL, 614
    UNION ALL SELECT 'engr.mendoza', TRUE, NULL, 518
    UNION ALL SELECT 'vince.navarro', TRUE, NULL, 429
    UNION ALL SELECT 'ethan.lim', TRUE, NULL, 140
    UNION ALL SELECT 'grace.valdez', TRUE, NULL, 602
    UNION ALL SELECT 'dennis.uy', TRUE, NULL, 423
    UNION ALL SELECT 'jerome.tan', TRUE, NULL, 154
    UNION ALL SELECT 'aly.manalo', NULL, 'Can I add a second button to skip a break?', 562
    UNION ALL SELECT 'andrea.villanueva', NULL, 'Sure, wire it to pin 3 and set left = 0 when it is pressed. It will jump to the next session.', 554
) r
JOIN users u ON u.username = r.un COLLATE utf8mb4_general_ci
JOIN guides g ON g.title = 'Pomodoro Study Timer';

INSERT INTO reviews (guide_id, user_id, is_approved, comment, created_at)
SELECT g.guide_id, u.user_id, r.ok, r.cm, DATE_SUB(NOW(), INTERVAL r.h HOUR)
FROM (
    SELECT 'noel.flores' AS un, FALSE AS ok, 'False alarms when I reach for my mouse. Upping the 10 seconds to 20 fixed it.' AS cm, 485 AS h
    UNION ALL SELECT 'kyla.reyes', TRUE, 'Made this with my Grade 10 robotics club, they loved it.', 15
    UNION ALL SELECT 'nicole.fernandez', TRUE, 'My neck feels better after a week. Annoying in a good way.', 15
    UNION ALL SELECT 'nestor.ilagan', TRUE, 'Set LIMIT to 45 for my laptop since the screen is lower.', 263
    UNION ALL SELECT 'trisha.bautista', TRUE, NULL, 351
    UNION ALL SELECT 'renz.aquino', TRUE, NULL, 168
    UNION ALL SELECT 'dennis.uy', TRUE, NULL, 256
    UNION ALL SELECT 'tonio.salazar', TRUE, NULL, 294
    UNION ALL SELECT 'kurt.pascual', TRUE, NULL, 89
    UNION ALL SELECT 'joy.villareal', TRUE, 'Will try this with my students next week.', 107
    UNION ALL SELECT 'ken.yap', NULL, 'Does it work with glasses or long hair?', 252
    UNION ALL SELECT 'liza.cabrera', NULL, 'Yes, ultrasound bounces off anything solid. Soft fabric like a hoodie can read a bit far, though.', 240
) r
JOIN users u ON u.username = r.un COLLATE utf8mb4_general_ci
JOIN guides g ON g.title = 'Posture and Screen Distance Reminder';

INSERT INTO reviews (guide_id, user_id, is_approved, comment, created_at)
SELECT g.guide_id, u.user_id, r.ok, r.cm, DATE_SUB(NOW(), INTERVAL r.h HOUR)
FROM (
    SELECT 'lea.pineda' AS un, FALSE AS ok, 'Dew in the early morning sets it off. I added a 2 minute wait before beeping.' AS cm, 800 AS h
    UNION ALL SELECT 'mika.ocampo', TRUE, 'Saved my uniform twice already this week. Rainy season essential.', 560
    UNION ALL SELECT 'trisha.bautista', TRUE, 'Lola approves. She says it''s better than looking at the sky every 5 minutes.', 873
    UNION ALL SELECT 'angela.torres', TRUE, 'I used a doorbell chime instead of a buzzer, louder and nicer.', 850
    UNION ALL SELECT 'andrea.villanueva', TRUE, 'Put the module in a Tupperware, been outside for a month with no problems.', 163
    UNION ALL SELECT 'aly.manalo', TRUE, 'The knob adjustment step is important, mine kept beeping from humidity at first.', 786
    UNION ALL SELECT 'kurt.pascual', TRUE, 'Simple, cheap and very Pinoy. Love it.', 626
    UNION ALL SELECT 'migs.santos', TRUE, 'Works with a 10 m wire too, tested it to our rooftop.', 385
    UNION ALL SELECT 'ria.domingo', TRUE, NULL, 541
    UNION ALL SELECT 'jas.cruz', TRUE, NULL, 853
    UNION ALL SELECT 'mark.gonzales', TRUE, NULL, 364
    UNION ALL SELECT 'ken.yap', TRUE, NULL, 989
    UNION ALL SELECT 'rose.alcantara', TRUE, NULL, 562
    UNION ALL SELECT 'engr.mendoza', TRUE, NULL, 440
    UNION ALL SELECT 'nestor.ilagan', TRUE, NULL, 192
    UNION ALL SELECT 'nicole.fernandez', TRUE, NULL, 678
    UNION ALL SELECT 'lance.robles', TRUE, NULL, 375
    UNION ALL SELECT 'kyla.reyes', TRUE, NULL, 722
    UNION ALL SELECT 'edwin.lagman', TRUE, NULL, 722
    UNION ALL SELECT 'carlo.garcia', TRUE, NULL, 445
    UNION ALL SELECT 'noel.flores', TRUE, 'Worked on the first upload, rare for me haha.', 576
    UNION ALL SELECT 'vince.navarro', TRUE, NULL, 441
    UNION ALL SELECT 'tonio.salazar', TRUE, NULL, 551
    UNION ALL SELECT 'liza.cabrera', TRUE, NULL, 273
    UNION ALL SELECT 'sam.delosreyes', TRUE, 'Very beginner friendly.', 199
    UNION ALL SELECT 'joy.villareal', TRUE, NULL, 352
    UNION ALL SELECT 'grace.valdez', NULL, 'Can it send a notification to my phone instead?', 138
    UNION ALL SELECT 'pao.dizon', NULL, 'Yes, swap the Nano for an ESP8266 and send a Telegram message when it gets wet. I''ll post that version soon.', 135
) r
JOIN users u ON u.username = r.un COLLATE utf8mb4_general_ci
JOIN guides g ON g.title = 'Rain Alarm for the Clothesline';

INSERT INTO reviews (guide_id, user_id, is_approved, comment, created_at)
SELECT g.guide_id, u.user_id, r.ok, r.cm, DATE_SUB(NOW(), INTERVAL r.h HOUR)
FROM (
    SELECT 'grace.valdez' AS un, FALSE AS ok, 'Batteries died after a week. A 5V 2A adapter for the servo is better.' AS cm, 16 AS h
    UNION ALL SELECT 'aly.manalo', FALSE, 'Lid slams shut, add a slow close loop with small steps.', 423
    UNION ALL SELECT 'nicole.fernandez', FALSE, NULL, 265
    UNION ALL SELECT 'sir.belen', TRUE, 'Mom thought it was magic. 10/10 school project.', 426
    UNION ALL SELECT 'ria.domingo', TRUE, 'Used the MG90S like you said, SG90 could not lift our lid.', 17
    UNION ALL SELECT 'jas.cruz', TRUE, 'Glued the sensor under a small roof so kitchen splashes don''t hit it.', 60
    UNION ALL SELECT 'trisha.bautista', TRUE, 'Clear steps, thank you!', 319
    UNION ALL SELECT 'ethan.lim', TRUE, 'Ang linis ng steps, thank you.', 576
    UNION ALL SELECT 'lea.pineda', TRUE, NULL, 360
    UNION ALL SELECT 'sam.delosreyes', TRUE, NULL, 468
    UNION ALL SELECT 'josh.ramos', TRUE, NULL, 631
    UNION ALL SELECT 'bea.mendoza', TRUE, 'Watched the video first, then followed the steps. Smooth.', 325
    UNION ALL SELECT 'mark.gonzales', TRUE, NULL, 610
    UNION ALL SELECT 'andrea.villanueva', TRUE, NULL, 124
    UNION ALL SELECT 'angela.torres', NULL, 'How do I make the lid close slowly?', 413
    UNION ALL SELECT 'kurt.pascual', NULL, 'Instead of lid.write(CLOSED), loop from OPEN_ANGLE down to 0 with a 15 ms delay per degree.', 395
) r
JOIN users u ON u.username = r.un COLLATE utf8mb4_general_ci
JOIN guides g ON g.title = 'Touchless Trash Can Lid';

INSERT INTO reviews (guide_id, user_id, is_approved, comment, created_at)
SELECT g.guide_id, u.user_id, r.ok, r.cm, DATE_SUB(NOW(), INTERVAL r.h HOUR)
FROM (
    SELECT 'jas.cruz' AS un, FALSE AS ok, 'Two people walking side by side count as one. Fine for a small room.' AS cm, 117 AS h
    UNION ALL SELECT 'angela.torres', FALSE, 'Some steps need more detail for beginners.', 123
    UNION ALL SELECT 'tonio.salazar', TRUE, 'Our org room''s light is finally off at night. Treasurer is happy.', 40
    UNION ALL SELECT 'noel.flores', TRUE, 'Engr. Mendoza''s review notes helped me fix my relay box, thank you.', 130
    UNION ALL SELECT 'trisha.bautista', TRUE, 'Placed the sensors at hip height so bags don''t double count.', 537
    UNION ALL SELECT 'rj.manalastas', TRUE, 'Great demo for our energy audit report.', 364
    UNION ALL SELECT 'lea.pineda', TRUE, 'LCD count is satisfying to watch hehe.', 613
    UNION ALL SELECT 'kyla.reyes', TRUE, 'Would be nice with a manual override switch, but works great.', 665
    UNION ALL SELECT 'sam.delosreyes', TRUE, NULL, 311
    UNION ALL SELECT 'joy.villareal', TRUE, 'Nice project for a school fair.', 131
    UNION ALL SELECT 'kurt.pascual', TRUE, NULL, 395
    UNION ALL SELECT 'arnel.bernardo', TRUE, NULL, 797
    UNION ALL SELECT 'ivy.morales', TRUE, NULL, 226
    UNION ALL SELECT 'liza.cabrera', TRUE, NULL, 189
    UNION ALL SELECT 'mark.gonzales', TRUE, NULL, 679
    UNION ALL SELECT 'mika.ocampo', TRUE, NULL, 556
    UNION ALL SELECT 'camille.rivera', TRUE, NULL, 408
    UNION ALL SELECT 'edwin.lagman', TRUE, NULL, 807
    UNION ALL SELECT 'ken.yap', TRUE, 'Clear steps, thank you!', 18
    UNION ALL SELECT 'vince.navarro', TRUE, NULL, 492
    UNION ALL SELECT 'dennis.uy', TRUE, NULL, 324
    UNION ALL SELECT 'jm.castillo', TRUE, 'Ang linis ng steps, thank you.', 609
    UNION ALL SELECT 'ethan.lim', TRUE, 'Gumana agad, salamat po!', 568
    UNION ALL SELECT 'ria.domingo', TRUE, 'Will try this with my students next week.', 841
    UNION ALL SELECT 'aly.manalo', TRUE, NULL, 650
    UNION ALL SELECT 'migs.santos', NULL, 'Why does the code wait inside the while loop?', 562
    UNION ALL SELECT 'pao.dizon', NULL, 'It waits for the second sensor so it knows the direction. If someone turns back halfway it can get stuck, so I''m adding a 2 second timeout next.', 550
    UNION ALL SELECT 'engr.mendoza', NULL, 'Approved. Use a relay module with an enclosed 10A relay, put a 2A fuse on the live input, and keep all 230V parts in a closed box.', 934
) r
JOIN users u ON u.username = r.un COLLATE utf8mb4_general_ci
JOIN guides g ON g.title = 'Room Light Auto-Off with Visitor Counter';

INSERT INTO reviews (guide_id, user_id, is_approved, comment, created_at)
SELECT g.guide_id, u.user_id, r.ok, r.cm, DATE_SUB(NOW(), INTERVAL r.h HOUR)
FROM (
    SELECT 'renz.aquino' AS un, FALSE AS ok, 'My LDR module''s output was the opposite, I had to change HIGH to LOW.' AS cm, 577 AS h
    UNION ALL SELECT 'kyla.reyes', FALSE, NULL, 353
    UNION ALL SELECT 'pao.dizon', TRUE, 'Little brother cannot sneak into my room anymore.', 207
    UNION ALL SELECT 'lea.pineda', TRUE, 'Used a mirror to bounce the beam around a corner, works!', 385
    UNION ALL SELECT 'nestor.ilagan', TRUE, 'Good intro to sensors for our Grade 8 class.', 369
    UNION ALL SELECT 'lance.robles', TRUE, 'Daytime sunlight on the sensor caused misses, a black straw over the LDR fixed it.', 472
    UNION ALL SELECT 'sam.delosreyes', TRUE, NULL, 563
    UNION ALL SELECT 'rj.manalastas', TRUE, NULL, 809
    UNION ALL SELECT 'noel.flores', TRUE, NULL, 321
    UNION ALL SELECT 'camille.rivera', TRUE, NULL, 645
    UNION ALL SELECT 'mark.gonzales', TRUE, NULL, 498
    UNION ALL SELECT 'jerome.tan', TRUE, NULL, 364
    UNION ALL SELECT 'grace.valdez', TRUE, NULL, 537
    UNION ALL SELECT 'aly.manalo', TRUE, NULL, 816
    UNION ALL SELECT 'bea.mendoza', TRUE, 'Gumana agad, salamat po!', 88
    UNION ALL SELECT 'edwin.lagman', NULL, 'Is a 5mW laser safe for a classroom demo?', 336
    UNION ALL SELECT 'josh.ramos', NULL, 'It''s low power but still not safe to look into. Mount it low, and tell students never to aim it at faces.', 323
) r
JOIN users u ON u.username = r.un COLLATE utf8mb4_general_ci
JOIN guides g ON g.title = 'Laser Tripwire Alarm';

INSERT INTO reviews (guide_id, user_id, is_approved, comment, created_at)
SELECT g.guide_id, u.user_id, r.ok, r.cm, DATE_SUB(NOW(), INTERVAL r.h HOUR)
FROM (
    SELECT 'joy.villareal' AS un, FALSE AS ok, 'Fail-secure lock means locked when power is out. I switched to a fail-safe one for our room.' AS cm, 341 AS h
    UNION ALL SELECT 'carlo.garcia', FALSE, NULL, 85
    UNION ALL SELECT 'bea.mendoza', TRUE, 'Installed it on our tool cabinet, only the officers can open it now.', 151
    UNION ALL SELECT 'nicole.fernandez', TRUE, 'The 3.3V warning saved my RC522. Thank you!', 153
    UNION ALL SELECT 'migs.santos', TRUE, 'Key fob works through 8 mm plywood.', 279
    UNION ALL SELECT 'jerome.tan', TRUE, 'Added a green LED for granted and red for denied, looks pro.', 148
    UNION ALL SELECT 'ria.domingo', TRUE, 'Clear diode tip, my first relay died without it.', 184
    UNION ALL SELECT 'tonio.salazar', TRUE, 'Watched the video first, then followed the steps. Smooth.', 98
    UNION ALL SELECT 'liza.cabrera', TRUE, NULL, 324
    UNION ALL SELECT 'aly.manalo', TRUE, 'Nice project for a school fair.', 333
    UNION ALL SELECT 'rj.manalastas', TRUE, NULL, 218
    UNION ALL SELECT 'jas.cruz', TRUE, NULL, 397
    UNION ALL SELECT 'rose.alcantara', TRUE, NULL, 277
    UNION ALL SELECT 'dennis.uy', TRUE, 'Will try this with my students next week.', 17
    UNION ALL SELECT 'grace.valdez', TRUE, 'Will try this with my students next week.', 76
    UNION ALL SELECT 'josh.ramos', TRUE, 'Built this over the weekend, works great.', 265
    UNION ALL SELECT 'angela.torres', TRUE, NULL, 362
    UNION ALL SELECT 'renz.aquino', TRUE, NULL, 3
    UNION ALL SELECT 'andrea.villanueva', TRUE, NULL, 34
    UNION ALL SELECT 'trisha.bautista', TRUE, NULL, 112
    UNION ALL SELECT 'camille.rivera', TRUE, NULL, 308
    UNION ALL SELECT 'sam.delosreyes', TRUE, NULL, 66
    UNION ALL SELECT 'edwin.lagman', TRUE, 'The schematic made the wiring easy to follow.', 250
    UNION ALL SELECT 'ken.yap', TRUE, 'Worked on the first upload, rare for me haha.', 39
    UNION ALL SELECT 'kyla.reyes', TRUE, NULL, 317
    UNION ALL SELECT 'kurt.pascual', TRUE, NULL, 62
    UNION ALL SELECT 'ethan.lim', TRUE, NULL, 257
    UNION ALL SELECT 'vince.navarro', NULL, 'Can I add more cards later without uploading again?', 91
    UNION ALL SELECT 'pao.dizon', NULL, 'Yes, save the UIDs in EEPROM and add a master card that puts it in add mode. I''ll update the guide with that.', 72
) r
JOIN users u ON u.username = r.un COLLATE utf8mb4_general_ci
JOIN guides g ON g.title = 'RFID Door Lock with Solenoid';

INSERT INTO reviews (guide_id, user_id, is_approved, comment, created_at)
SELECT g.guide_id, u.user_id, r.ok, r.cm, DATE_SUB(NOW(), INTERVAL r.h HOUR)
FROM (
    SELECT 'sir.belen' AS un, FALSE AS ok, 'The IR sensor triggers from sunlight near the window. Moved it and it''s fine.' AS cm, 321 AS h
    UNION ALL SELECT 'lance.robles', TRUE, 'Our school clinic uses it every day now.', 273
    UNION ALL SELECT 'dennis.uy', TRUE, '400 ms was too much for alcohol, 250 ms is perfect.', 773
    UNION ALL SELECT 'rj.manalastas', TRUE, 'Works with liquid soap too if you dilute it a little.', 679
    UNION ALL SELECT 'joy.villareal', TRUE, 'Mounted it by the canteen sink, kids love it.', 233
    UNION ALL SELECT 'josh.ramos', TRUE, 'Watched the video first, then followed the steps. Smooth.', 520
    UNION ALL SELECT 'trisha.bautista', TRUE, NULL, 332
    UNION ALL SELECT 'noel.flores', TRUE, NULL, 743
    UNION ALL SELECT 'andrea.villanueva', TRUE, NULL, 507
    UNION ALL SELECT 'ivy.morales', TRUE, NULL, 182
    UNION ALL SELECT 'edwin.lagman', TRUE, NULL, 476
    UNION ALL SELECT 'lea.pineda', TRUE, NULL, 707
    UNION ALL SELECT 'rose.alcantara', NULL, 'Will the pump corrode from alcohol?', 386
    UNION ALL SELECT 'camille.rivera', NULL, 'Mine is fine after 2 months with 70% isopropyl. Flush it with water every few weeks to be safe.', 373
) r
JOIN users u ON u.username = r.un COLLATE utf8mb4_general_ci
JOIN guides g ON g.title = 'Touchless Hand Sanitizer Dispenser';

INSERT INTO reviews (guide_id, user_id, is_approved, comment, created_at)
SELECT g.guide_id, u.user_id, r.ok, r.cm, DATE_SUB(NOW(), INTERVAL r.h HOUR)
FROM (
    SELECT 'mark.gonzales' AS un, FALSE AS ok, 'Readings jump a lot if you move even a little.' AS cm, 165 AS h
    UNION ALL SELECT 'engr.mendoza', FALSE, 'Cold fingers give no reading at all, warm them up first.', 138
    UNION ALL SELECT 'lance.robles', TRUE, 'Great for our Biology and ICT integration project.', 229
    UNION ALL SELECT 'sam.delosreyes', TRUE, 'My smartwatch said 76, this said 74. Close enough!', 136
    UNION ALL SELECT 'rj.manalastas', TRUE, 'The tip about pressing lightly is so true.', 76
    UNION ALL SELECT 'ethan.lim', TRUE, NULL, 139
    UNION ALL SELECT 'camille.rivera', TRUE, NULL, 75
    UNION ALL SELECT 'sir.belen', TRUE, NULL, 65
    UNION ALL SELECT 'kurt.pascual', TRUE, NULL, 296
    UNION ALL SELECT 'nestor.ilagan', TRUE, NULL, 207
    UNION ALL SELECT 'renz.aquino', TRUE, NULL, 283
    UNION ALL SELECT 'nicole.fernandez', NULL, 'Can I use a MAX30102 instead?', 198
    UNION ALL SELECT 'mika.ocampo', NULL, 'Yes, it''s more accurate and also reads oxygen, but it uses I2C and a different library, so the code changes.', 183
) r
JOIN users u ON u.username = r.un COLLATE utf8mb4_general_ci
JOIN guides g ON g.title = 'Heart Rate Monitor with Pulse Sensor';

INSERT INTO reviews (guide_id, user_id, is_approved, comment, created_at)
SELECT g.guide_id, u.user_id, r.ok, r.cm, DATE_SUB(NOW(), INTERVAL r.h HOUR)
FROM (
    SELECT 'grace.valdez' AS un, FALSE AS ok, 'Big kibble jams the flap sometimes. Works better with small pellets.' AS cm, 12 AS h
    UNION ALL SELECT 'nicole.fernandez', FALSE, 'Wish it had a screen to change feeding times without uploading.', 275
    UNION ALL SELECT 'arnel.bernardo', TRUE, 'Our aspin gets breakfast on time even when we leave early for school.', 574
    UNION ALL SELECT 'joy.villareal', TRUE, 'The DS3231 keeps time even after a brownout, very nice.', 260
    UNION ALL SELECT 'liza.cabrera', TRUE, 'Used a cereal dispenser from Shopee, fits the MG996R perfectly.', 135
    UNION ALL SELECT 'mika.ocampo', TRUE, NULL, 426
    UNION ALL SELECT 'camille.rivera', TRUE, NULL, 489
    UNION ALL SELECT 'aly.manalo', TRUE, NULL, 496
    UNION ALL SELECT 'dennis.uy', TRUE, NULL, 40
    UNION ALL SELECT 'kurt.pascual', TRUE, NULL, 125
    UNION ALL SELECT 'migs.santos', TRUE, NULL, 277
    UNION ALL SELECT 'jerome.tan', TRUE, NULL, 216
    UNION ALL SELECT 'ivy.morales', TRUE, NULL, 529
    UNION ALL SELECT 'josh.ramos', TRUE, NULL, 380
    UNION ALL SELECT 'bea.mendoza', NULL, 'How do I give 2 portions?', 110
    UNION ALL SELECT 'ria.domingo', NULL, 'Call feed() twice inside the if, or make the servo stay open longer, like 1200 ms instead of 700.', 91
) r
JOIN users u ON u.username = r.un COLLATE utf8mb4_general_ci
JOIN guides g ON g.title = 'Automatic Pet Feeder with Real-Time Clock';

INSERT INTO reviews (guide_id, user_id, is_approved, comment, created_at)
SELECT g.guide_id, u.user_id, r.ok, r.cm, DATE_SUB(NOW(), INTERVAL r.h HOUR)
FROM (
    SELECT 'aly.manalo' AS un, FALSE AS ok, 'The knob is hard to read. I marked the hours with a marker around it.' AS cm, 89 AS h
    UNION ALL SELECT 'joy.villareal', TRUE, 'My guppies are fed while I''m at school. Works for 3 weeks now.', 160
    UNION ALL SELECT 'kyla.reyes', TRUE, 'Film canister with a 4 mm hole gives the perfect pinch.', 45
    UNION ALL SELECT 'nicole.fernandez', TRUE, 'Kids at our school''s aquarium club built five of these.', 292
    UNION ALL SELECT 'ethan.lim', TRUE, NULL, 269
    UNION ALL SELECT 'ria.domingo', TRUE, NULL, 288
    UNION ALL SELECT 'mika.ocampo', TRUE, 'Clear steps, thank you!', 140
    UNION ALL SELECT 'ken.yap', TRUE, NULL, 24
    UNION ALL SELECT 'arnel.bernardo', TRUE, NULL, 264
    UNION ALL SELECT 'rose.alcantara', TRUE, NULL, 243
    UNION ALL SELECT 'lea.pineda', NULL, 'Does it forget the timer after a brownout?', 204
    UNION ALL SELECT 'noel.flores', NULL, 'Yes, millis() restarts at 0, so it feeds again after the set hours. Add a DS3231 if you want exact times.', 192
) r
JOIN users u ON u.username = r.un COLLATE utf8mb4_general_ci
JOIN guides g ON g.title = 'Automatic Fish Feeder';

INSERT INTO reviews (guide_id, user_id, is_approved, comment, created_at)
SELECT g.guide_id, u.user_id, r.ok, r.cm, DATE_SUB(NOW(), INTERVAL r.h HOUR)
FROM (
    SELECT 'tonio.salazar' AS un, TRUE AS ok, 'Parts were cheap online, came in under budget.' AS cm, 160 AS h
    UNION ALL SELECT 'rose.alcantara', FALSE, NULL, 283
    UNION ALL SELECT 'rj.manalastas', TRUE, NULL, 400
    UNION ALL SELECT 'pao.dizon', TRUE, NULL, 433
    UNION ALL SELECT 'lea.pineda', TRUE, NULL, 858
    UNION ALL SELECT 'nestor.ilagan', TRUE, 'Worked on the first upload, rare for me haha.', 684
) r
JOIN users u ON u.username = r.un COLLATE utf8mb4_general_ci
JOIN guides g ON g.title = 'Automatic Plant Watering System';

INSERT INTO reviews (guide_id, user_id, is_approved, comment, created_at)
SELECT g.guide_id, u.user_id, r.ok, r.cm, DATE_SUB(NOW(), INTERVAL r.h HOUR)
FROM (
    SELECT 'ken.yap' AS un, TRUE AS ok, 'Gumana agad, salamat po!' AS cm, 103 AS h
    UNION ALL SELECT 'sir.belen', TRUE, NULL, 202
    UNION ALL SELECT 'vince.navarro', TRUE, NULL, 564
    UNION ALL SELECT 'rj.manalastas', TRUE, NULL, 132
    UNION ALL SELECT 'josh.ramos', TRUE, NULL, 53
    UNION ALL SELECT 'nestor.ilagan', TRUE, 'Will try this with my students next week.', 900
    UNION ALL SELECT 'arnel.bernardo', TRUE, NULL, 834
    UNION ALL SELECT 'tonio.salazar', TRUE, NULL, 757
    UNION ALL SELECT 'lea.pineda', TRUE, NULL, 884
) r
JOIN users u ON u.username = r.un COLLATE utf8mb4_general_ci
JOIN guides g ON g.title = 'Door Sensor Alarm with Buzzer';

INSERT INTO reviews (guide_id, user_id, is_approved, comment, created_at)
SELECT g.guide_id, u.user_id, r.ok, r.cm, DATE_SUB(NOW(), INTERVAL r.h HOUR)
FROM (
    SELECT 'grace.valdez' AS un, TRUE AS ok, NULL AS cm, 180 AS h
    UNION ALL SELECT 'ivy.morales', TRUE, 'Ang linis ng steps, thank you.', 610
    UNION ALL SELECT 'sir.belen', FALSE, NULL, 59
    UNION ALL SELECT 'edwin.lagman', TRUE, 'Gumana agad, salamat po!', 166
    UNION ALL SELECT 'aly.manalo', TRUE, NULL, 389
    UNION ALL SELECT 'ethan.lim', TRUE, 'Worked on the first upload, rare for me haha.', 78
) r
JOIN users u ON u.username = r.un COLLATE utf8mb4_general_ci
JOIN guides g ON g.title = 'Motion-Activated Hallway Light';

INSERT INTO reviews (guide_id, user_id, is_approved, comment, created_at)
SELECT g.guide_id, u.user_id, r.ok, r.cm, DATE_SUB(NOW(), INTERVAL r.h HOUR)
FROM (
    SELECT 'ethan.lim' AS un, TRUE AS ok, 'Will try this with my students next week.' AS cm, 396 AS h
    UNION ALL SELECT 'engr.mendoza', TRUE, 'Ang linis ng steps, thank you.', 789
    UNION ALL SELECT 'nestor.ilagan', TRUE, NULL, 173
    UNION ALL SELECT 'lea.pineda', TRUE, 'Clear steps, thank you!', 83
    UNION ALL SELECT 'sir.belen', FALSE, NULL, 745
) r
JOIN users u ON u.username = r.un COLLATE utf8mb4_general_ci
JOIN guides g ON g.title = 'Laundry Done Notifier';

INSERT INTO reviews (guide_id, user_id, is_approved, comment, created_at)
SELECT g.guide_id, u.user_id, r.ok, r.cm, DATE_SUB(NOW(), INTERVAL r.h HOUR)
FROM (
    SELECT 'pao.dizon' AS un, TRUE AS ok, NULL AS cm, 270 AS h
    UNION ALL SELECT 'ria.domingo', TRUE, NULL, 241
    UNION ALL SELECT 'migs.santos', TRUE, NULL, 456
    UNION ALL SELECT 'sam.delosreyes', TRUE, NULL, 395
    UNION ALL SELECT 'ken.yap', TRUE, NULL, 668
    UNION ALL SELECT 'aly.manalo', TRUE, NULL, 596
    UNION ALL SELECT 'josh.ramos', TRUE, 'Saved this for our capstone ideas.', 780
    UNION ALL SELECT 'angela.torres', TRUE, NULL, 386
    UNION ALL SELECT 'liza.cabrera', TRUE, NULL, 185
) r
JOIN users u ON u.username = r.un COLLATE utf8mb4_general_ci
JOIN guides g ON g.title = 'Bedroom Air Quality Monitor';

INSERT INTO reviews (guide_id, user_id, is_approved, comment, created_at)
SELECT g.guide_id, u.user_id, r.ok, r.cm, DATE_SUB(NOW(), INTERVAL r.h HOUR)
FROM (
    SELECT 'kurt.pascual' AS un, TRUE AS ok, NULL AS cm, 462 AS h
    UNION ALL SELECT 'jerome.tan', FALSE, NULL, 687
    UNION ALL SELECT 'angela.torres', TRUE, NULL, 299
    UNION ALL SELECT 'aly.manalo', TRUE, NULL, 33
) r
JOIN users u ON u.username = r.un COLLATE utf8mb4_general_ci
JOIN guides g ON g.title = 'Pet Water Bowl Refill Alert';

INSERT INTO reviews (guide_id, user_id, is_approved, comment, created_at)
SELECT g.guide_id, u.user_id, r.ok, r.cm, DATE_SUB(NOW(), INTERVAL r.h HOUR)
FROM (
    SELECT 'arnel.bernardo' AS un, TRUE AS ok, NULL AS cm, 391 AS h
    UNION ALL SELECT 'camille.rivera', TRUE, NULL, 631
    UNION ALL SELECT 'carlo.garcia', FALSE, NULL, 597
    UNION ALL SELECT 'dennis.uy', TRUE, 'Ang linis ng steps, thank you.', 104
    UNION ALL SELECT 'lea.pineda', TRUE, 'Will try this with my students next week.', 864
    UNION ALL SELECT 'bea.mendoza', TRUE, 'Very beginner friendly.', 327
    UNION ALL SELECT 'nicole.fernandez', FALSE, NULL, 95
) r
JOIN users u ON u.username = r.un COLLATE utf8mb4_general_ci
JOIN guides g ON g.title = 'Solar Phone Charging Station';

-- Expert earned from votes: enough approvals from every identity group (stratified sampling rule)
UPDATE users SET role = 'EXPERT', expert_source = 'VOTES', expert_since = DATE_SUB(NOW(), INTERVAL 6 DAY) WHERE username = 'pao.dizon';

-- Reports and edit requests for the Admin Panel
INSERT INTO reports (guide_id, reporter_id, type, reason, status, resolved_by, created_at)
SELECT g.guide_id, u.user_id, 'EDIT_REQUEST', 'Please say that an SG90 is too weak for heavier lids. The MG90S worked for me.', 'OPEN', NULL, DATE_SUB(NOW(), INTERVAL 70 HOUR)
FROM guides g JOIN users u ON u.username = 'trisha.bautista' WHERE g.title = 'Touchless Trash Can Lid';
INSERT INTO reports (guide_id, reporter_id, type, reason, status, resolved_by, created_at)
SELECT g.guide_id, u.user_id, 'REPORT', 'Please make the eye safety warning bigger. Students will try to point it at each other.', 'OPEN', NULL, DATE_SUB(NOW(), INTERVAL 50 HOUR)
FROM guides g JOIN users u ON u.username = 'liza.cabrera' WHERE g.title = 'Laser Tripwire Alarm';
INSERT INTO reports (guide_id, reporter_id, type, reason, status, resolved_by, created_at)
SELECT g.guide_id, u.user_id, 'EDIT_REQUEST', 'Step 3 should link the PulseSensor Playground library page.', 'OPEN', NULL, DATE_SUB(NOW(), INTERVAL 20 HOUR)
FROM guides g JOIN users u ON u.username = 'camille.rivera' WHERE g.title = 'Heart Rate Monitor with Pulse Sensor';
INSERT INTO reports (guide_id, reporter_id, type, reason, status, resolved_by, created_at)
SELECT g.guide_id, u.user_id, 'REPORT', 'Spam, this is an ad not a guide.', 'RESOLVED', (SELECT user_id FROM users WHERE username = 'admin'), DATE_SUB(NOW(), INTERVAL 250 HOUR)
FROM guides g JOIN users u ON u.username = 'kyla.reyes' WHERE g.title = 'CHEAP ARDUINO KITS 50% OFF!!! Message me';
INSERT INTO reports (guide_id, reporter_id, type, reason, status, resolved_by, created_at)
SELECT g.guide_id, u.user_id, 'REPORT', 'Advertisement. Please remove.', 'RESOLVED', (SELECT user_id FROM users WHERE username = 'admin'), DATE_SUB(NOW(), INTERVAL 248 HOUR)
FROM guides g JOIN users u ON u.username = 'sir.belen' WHERE g.title = 'CHEAP ARDUINO KITS 50% OFF!!! Message me';

