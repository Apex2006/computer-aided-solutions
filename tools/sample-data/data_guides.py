"""Sixteen new guides, written fresh for the app (ideas are classic Instructables-style builds).

Wiring model used by the schematic renderer:
  board pins  -> component left pins   (nets named after the board pin, e.g. "D3", "A4", "GPIO17")
  bus nets    -> component left pins   ("5V", "3V3", "GND", or any other shared supply like "6V")
  right pins  -> load pins             (any other net name; colors set in "netcolors")
"""

UNO = {"name": "Arduino Uno", "alias": {"A4": "A4  SDA", "A5": "A5  SCL"}}
NANO = {"name": "Arduino Nano", "alias": {"A4": "A4  SDA", "A5": "A5  SCL"}}
ESP32 = {"name": "ESP32 DevKit", "alias": {"GPIO21": "GPIO21 SDA", "GPIO22": "GPIO22 SCL",
                                           "GPIO16": "GPIO16 RX2", "GPIO17": "GPIO17 TX2", "VIN": "VIN (5V)"}}

GUIDES = []

# ---------------------------------------------------------------- FOOD AND COOKING
GUIDES.append(dict(
    title="Digital Kitchen Scale with HX711", category="FOOD_COOKING", author="jas.cruz", days=34,
    status="PUBLISHED", difficulty="BEGINNER", risk="LOW", cost=930,
    summary="Weigh ingredients up to 5 kg on a homemade scale built from a load cell, an HX711 amplifier and a 16x2 LCD. Put a bowl on top and press the button to zero it.",
    parts=["Arduino Uno", "5 kg load cell with HX711 amplifier board", "16x2 LCD with I2C backpack", "Push button",
           "Two 15 x 15 cm plywood or acrylic plates", "M4 and M5 screws for the load cell", "Jumper wires"],
    precautions=["Do not put more than 5 kg on it or the load cell bends for good.",
                 "Mount the load cell so the top plate only rests on the end with the arrow.",
                 "Keep liquids in a closed container when weighing near the electronics."],
    steps=["Screw the load cell between the two plates so each plate only touches one end of the bar.",
           "Connect the load cell's red, black, white and green wires to E+, E-, A- and A+ on the HX711.",
           "Wire the HX711 DT to pin 3 and SCK to pin 2, the LCD to A4 and A5, and the button between pin 4 and GND.",
           "Install the HX711 library by Bogdan Necula and the LiquidCrystal I2C library, then upload the sketch.",
           "Calibrate: put something you know the weight of, like a 500 g pack of sugar, and change CAL until the screen shows 500 g.",
           "Put your bowl on the scale, press the button to zero it, and start adding ingredients."],
    video="https://www.youtube.com/watch?v=S12Mp8gDJmI",
    board=dict(UNO, pins=["5V", "GND", "D2", "D3", "D4", "A4", "A5"]),
    comps=[
        dict(id="hx", name="HX711 amplifier", color="yellow",
             left=[("VCC", "5V"), ("GND", "GND"), ("DT", "D3"), ("SCK", "D2")],
             right=[("E+", "E+"), ("E-", "E-"), ("A-", "A-"), ("A+", "A+")]),
        dict(id="lcd", name="16x2 LCD (I2C)", color="cyan",
             left=[("VCC", "5V"), ("GND", "GND"), ("SDA", "A4"), ("SCL", "A5")]),
        dict(id="btn", name="Tare button", color="green", left=[("1", "D4"), ("2", "GND")]),
    ],
    loads=[dict(id="cell", name="5 kg load cell", left=[("Red", "E+"), ("Black", "E-"), ("White", "A-"), ("Green", "A+")])],
    netcolors={"E+": "red", "E-": "gray", "A-": "white", "A+": "green"},
    images={
        3: dict(type="table", title="Wire the HX711, LCD and button", comps=["hx", "lcd", "btn"]),
        4: dict(type="code", title="Upload the sketch", file="kitchen_scale.ino", code="""#include <HX711.h>
#include <LiquidCrystal_I2C.h>

HX711 scale;
LiquidCrystal_I2C lcd(0x27, 16, 2);
float CAL = 420.0;            // change this in step 5

void setup() {
  pinMode(4, INPUT_PULLUP);   // tare button
  lcd.init(); lcd.backlight();
  scale.begin(3, 2);          // DT, SCK
  scale.set_scale(CAL);
  scale.tare();
}

void loop() {
  if (digitalRead(4) == LOW) scale.tare();
  lcd.setCursor(0, 0);
  lcd.print("Weight:");
  lcd.setCursor(0, 1);
  lcd.print(scale.get_units(5), 1);
  lcd.print(" g      ");
}"""),
        6: dict(type="lcd", title="Zero it and weigh", lines=["Weight:", "250.0 g"], caption="250 g of flour after taring the bowl"),
    },
    votes=(15, 0.9),
    comments=["Calibrated it with a 1 kg bag of rice, accurate to about 2 grams.",
              "Used this for baking class, the tare button is so handy.",
              "My load cell had blue instead of green wire, still worked as A+.",
              "Mas mura pa kaysa sa binili kong scale sa palengke. Salamat!",
              "Tip: let it warm up for a minute, the first readings drift a bit."],
    critiques=["Readings jump by 5 to 10 g for me. Probably my plates are too flexible."],
    qa=("trisha.bautista", "Can it go up to 10 kg if I buy a bigger load cell?",
        "Yes, get a 10 kg load cell and recalibrate. The wiring and sketch stay the same."),
))

GUIDES.append(dict(
    title="Waterproof Food Thermometer with Alarm", category="FOOD_COOKING", author="ivy.morales", days=29,
    status="PUBLISHED", difficulty="BEGINNER", risk="LOW", cost=640,
    summary="Check milk, soup, coffee water or melted chocolate with a stainless steel DS18B20 probe. An OLED shows the temperature and a buzzer beeps when it reaches your target.",
    parts=["Arduino Nano", "DS18B20 waterproof temperature probe", "4.7k ohm resistor", "0.96 inch OLED display (I2C)",
           "Active buzzer", "Two push buttons", "Small project box"],
    precautions=["Only the steel tip and the cable go into food. Keep the board away from steam and the stove.",
                 "The probe cable is only rated to about 100 C, so do not use it for frying oil or the oven.",
                 "Wash the probe with soap after each use and never put the box under water."],
    steps=["Solder the 4.7k resistor between the probe's red (VCC) and yellow (DATA) wires.",
           "Wire the probe: red to 5V, black to GND and yellow to pin 2.",
           "Connect the OLED to A4 and A5, the buzzer to pin 8, and the two buttons to pins 5 and 6.",
           "Install the OneWire, DallasTemperature and Adafruit SSD1306 libraries, then upload the sketch.",
           "Set your target with the buttons, put the probe in warm water, and wait for the beep."],
    video="https://www.youtube.com/watch?v=tsRHey47n5A",
    board=dict(NANO, pins=["5V", "GND", "D2", "D5", "D6", "D8", "A4", "A5"]),
    comps=[
        dict(id="probe", name="DS18B20 probe", note="4.7k from DATA to VCC", color="yellow",
             left=[("Red VCC", "5V"), ("Yellow DATA", "D2"), ("Black GND", "GND")]),
        dict(id="oled", name="OLED 0.96 in (I2C)", color="cyan",
             left=[("VCC", "5V"), ("GND", "GND"), ("SDA", "A4"), ("SCL", "A5")]),
        dict(id="buzz", name="Active buzzer", color="pink", left=[("+", "D8"), ("-", "GND")]),
        dict(id="btns", name="Buttons (- and +)", color="green", left=[("-", "D5"), ("+", "D6"), ("common", "GND")]),
    ],
    loads=[], netcolors={},
    images={
        2: dict(type="table", title="Wire the probe and the parts", comps=["probe", "oled", "buzz", "btns"]),
        4: dict(type="code", title="Upload the sketch", file="food_thermometer.ino", code="""#include <OneWire.h>
#include <DallasTemperature.h>

OneWire wire(2);
DallasTemperature probe(&wire);
float target = 65.0;

void setup() {
  pinMode(5, INPUT_PULLUP);
  pinMode(6, INPUT_PULLUP);
  pinMode(8, OUTPUT);
  probe.begin();
}

void loop() {
  if (digitalRead(5) == LOW) target -= 1;
  if (digitalRead(6) == LOW) target += 1;
  probe.requestTemperatures();
  float t = probe.getTempCByIndex(0);
  digitalWrite(8, t >= target);   // beep at target
  // show t and target on the OLED here
  delay(300);
}"""),
        5: dict(type="oled", title="Wait for the beep", lines=["TEMP", "63.5 C", "TARGET 65 C"], caption="Milk for the baby, almost there"),
    },
    votes=(11, 0.91),
    comments=["Perfect for tempering chocolate, I set it to 31 C.",
              "My baby's milk is never too hot now. Thank you!",
              "The 4.7k resistor is important, without it I got -127 C.",
              "Put it in a soap box and it looks legit."],
    critiques=["Takes about 10 seconds to settle, slower than a store thermometer."],
    qa=("sam.delosreyes", "Can I use this for deep frying?",
        "No, the cable is only good up to about 100 C. Frying oil is much hotter, so get a K-type thermocouple for that."),
))

# ---------------------------------------------------------------- GARDENING
GUIDES.append(dict(
    title="Soil Moisture Traffic Light", category="GARDENING", author="carlo.garcia", days=37,
    status="PUBLISHED", difficulty="BEGINNER", risk="LOW", cost=380,
    summary="Stick a sensor in the pot and three LEDs tell you at a glance: green is fine, yellow means water soon, and red means water now.",
    parts=["Arduino Nano", "Soil moisture sensor with comparator board (FC-28 or YL-69)", "Red, yellow and green 5 mm LEDs",
           "Three 220 ohm resistors", "Small waterproof box", "USB charger and cable"],
    precautions=["Keep the Nano box out of the rain and off wet soil.",
                 "The metal probe slowly rusts, so the sketch only powers it for a moment once a minute (through pin 7)."],
    steps=["Push the sensor probe into the soil about 3 cm from the stem.",
           "Wire the sensor board: AO to A0, VCC to pin 7 and GND to GND.",
           "Wire each LED through a 220 ohm resistor: red to pin 9, yellow to pin 10 and green to pin 11.",
           "Upload the sketch and open the Serial Monitor to see the readings.",
           "Write down the number for dry soil and for just-watered soil, put them in the sketch, and close the box."],
    video="https://www.youtube.com/watch?v=LvsQFsVpHro",
    board=dict(NANO, pins=["GND", "D7", "D9", "D10", "D11", "A0"]),
    comps=[
        dict(id="soil", name="Soil moisture sensor", note="probe goes in the pot", color="yellow",
             left=[("VCC", "D7"), ("GND", "GND"), ("AO", "A0")]),
        dict(id="red", name="Red LED + 220 ohm", color="pink", left=[("+", "D9"), ("-", "GND")]),
        dict(id="yel", name="Yellow LED + 220 ohm", color="orange", left=[("+", "D10"), ("-", "GND")]),
        dict(id="grn", name="Green LED + 220 ohm", color="green", left=[("+", "D11"), ("-", "GND")]),
    ],
    loads=[], netcolors={},
    images={
        2: dict(type="table", title="Wire the sensor and the LEDs", comps=["soil", "red", "yel", "grn"]),
        4: dict(type="code", title="Upload the sketch", file="soil_traffic_light.ino", code="""const int POWER = 7, SENSOR = A0;
const int RED = 9, YELLOW = 10, GREEN = 11;

void setup() {
  Serial.begin(9600);
  pinMode(POWER, OUTPUT);
  pinMode(RED, OUTPUT);
  pinMode(YELLOW, OUTPUT);
  pinMode(GREEN, OUTPUT);
}

void loop() {
  digitalWrite(POWER, HIGH);      // power the probe only now
  delay(10);
  int dry = analogRead(SENSOR);   // higher = drier
  digitalWrite(POWER, LOW);
  Serial.println(dry);
  digitalWrite(RED,    dry > 700);
  digitalWrite(YELLOW, dry > 500 && dry <= 700);
  digitalWrite(GREEN,  dry <= 500);
  delay(60000);                   // check once a minute
}"""),
        5: dict(type="serial", title="Find your dry and wet numbers", lines=["812   <- dry soil", "807", "366   <- right after watering", "371"]),
    },
    votes=(13, 0.92),
    comments=["Built three of these for my mom's orchids.",
              "Powering the probe from a pin is a smart trick, mine lasted way longer.",
              "Great first Arduino project for my Grade 9 class.",
              "Changed the numbers to 650 and 450 for cactus soil."],
    critiques=["The cheap fork sensor still rusted after two months. Get a capacitive one."],
    qa=("kyla.reyes", "Will a capacitive sensor work with the same code?",
        "Yes. Just remember capacitive sensors read lower when dry, so flip the comparisons."),
))

GUIDES.append(dict(
    title="Mini Greenhouse Fan Controller", category="GARDENING", author="tonio.salazar", days=24,
    status="PUBLISHED", difficulty="INTERMEDIATE", risk="MODERATE", cost=900,
    summary="Keep seedlings from cooking on hot afternoons. A DHT11 reads the temperature inside a small greenhouse and switches a 12V fan on above 32 C.",
    parts=["Arduino Uno", "DHT11 temperature and humidity module", "5V relay module (1 channel)", "12V DC fan (80 or 120 mm)",
           "12V 1A power adapter", "DC barrel jack adapter", "Jumper wires and a small box"],
    precautions=["Only switch the 12V fan with this relay. Do not connect 230V appliances to this build.",
                 "Keep the adapter and the Arduino outside the greenhouse or in a sealed box, since it is very humid inside.",
                 "Check the fan's red and black wires before plugging in the adapter."],
    steps=["Mount the DHT11 in the middle of the greenhouse, out of direct sun.",
           "Wire the DHT11 data pin to pin 2, VCC to 5V and GND to GND.",
           "Wire the relay IN to pin 7, VCC to 5V and GND to GND.",
           "Connect the adapter's + to the relay COM, the relay NO to the fan's red wire, and the fan's black wire back to the adapter's -.",
           "Install the DHT sensor library by Adafruit and upload the sketch.",
           "Warm the sensor with your hand or a hair dryer on low and check that the fan turns on."],
    video="https://www.youtube.com/watch?v=g34-9K6Mpyo",
    board=dict(UNO, pins=["5V", "GND", "D2", "D7"]),
    comps=[
        dict(id="dht", name="DHT11 sensor", color="yellow", left=[("VCC", "5V"), ("DATA", "D2"), ("GND", "GND")]),
        dict(id="relay", name="5V relay module", color="green", left=[("VCC", "5V"), ("GND", "GND"), ("IN", "D7")],
             right=[("COM", "12V+"), ("NO", "FAN+")]),
    ],
    loads=[dict(id="psu", name="12V 1A adapter", left=[("+", "12V+"), ("-", "12V-")]),
           dict(id="fan", name="12V DC fan", left=[("Red +", "FAN+"), ("Black -", "12V-")])],
    netcolors={"12V+": "orange", "FAN+": "orange", "12V-": "gray"},
    images={
        3: dict(type="table", title="Arduino side", comps=["dht", "relay"], side="left"),
        4: dict(type="table", title="12V fan side", comps=["relay"], side="right", loads=["psu", "fan"]),
        5: dict(type="code", title="Upload the sketch", file="greenhouse_fan.ino", code="""#include <DHT.h>

DHT dht(2, DHT11);
const int RELAY = 7;
const float ON_AT = 32.0, OFF_AT = 30.0;  // gap stops clicking

void setup() {
  Serial.begin(9600);
  pinMode(RELAY, OUTPUT);
  dht.begin();
}

void loop() {
  float t = dht.readTemperature();
  float h = dht.readHumidity();
  if (!isnan(t)) {
    // swap HIGH and LOW if your relay is active LOW
    if (t >= ON_AT)  digitalWrite(RELAY, HIGH);
    if (t <= OFF_AT) digitalWrite(RELAY, LOW);
    Serial.print(t); Serial.print(" C  ");
    Serial.print(h); Serial.println(" %");
  }
  delay(2000);
}"""),
        6: dict(type="serial", title="Test it with your hand", lines=["29.0 C  71.0 %", "31.0 C  70.0 %", "33.0 C  68.0 %   fan ON", "30.0 C  69.0 %   fan OFF"]),
    },
    votes=(12, 0.83),
    comments=["Seedlings survived the April heat this year, salamat!",
              "The 2 degree gap is a nice touch, my first version clicked nonstop.",
              "Used a computer fan from an old PC, works fine.",
              "Added a second fan on the same relay, no problem with a 2A adapter."],
    critiques=["My relay was active LOW so the fan ran backwards from what I expected. The comment in the code saved me though.",
               "DHT11 is not very accurate, a DHT22 is worth the extra 100 pesos."],
    qa=("arnel.bernardo", "Can I run a 220V stand fan instead?",
        "Please don't with this guide. Switching 230V needs an enclosed relay box and an expert check. Stick to 12V fans here."),
))

# ---------------------------------------------------------------- STUDIES
GUIDES.append(dict(
    title="Pomodoro Study Timer", category="STUDIES", author="andrea.villanueva", days=31,
    status="PUBLISHED", difficulty="BEGINNER", risk="LOW", cost=540,
    summary="A desk timer for the Pomodoro method: 25 minutes of focus, then a 5 minute break. An OLED shows the countdown and a short beep tells you when to switch.",
    parts=["Arduino Nano", "0.96 inch OLED display (I2C)", "Push button", "Passive buzzer", "Small case (soap box or 3D printed)", "USB cable"],
    precautions=["Keep the beep short and quiet if you study in a shared room.",
                 "Use a USB charger or power bank, not a 9V battery, so it lasts the whole day."],
    steps=["Connect the OLED: VCC to 5V, GND to GND, SDA to A4 and SCL to A5.",
           "Wire the button between pin 2 and GND, and the buzzer between pin 9 and GND.",
           "Install the Adafruit SSD1306 and Adafruit GFX libraries, then upload the sketch.",
           "Press the button once to start a focus session and again to pause it.",
           "After four focus sessions, take a longer 15 minute break."],
    video="https://www.youtube.com/watch?v=_f6aSjYSVHM",
    board=dict(NANO, pins=["5V", "GND", "D2", "D9", "A4", "A5"]),
    comps=[
        dict(id="oled", name="OLED 0.96 in (I2C)", color="cyan", left=[("VCC", "5V"), ("GND", "GND"), ("SDA", "A4"), ("SCL", "A5")]),
        dict(id="btn", name="Start / pause button", color="green", left=[("1", "D2"), ("2", "GND")]),
        dict(id="buzz", name="Passive buzzer", color="pink", left=[("+", "D9"), ("-", "GND")]),
    ],
    loads=[], netcolors={},
    images={
        2: dict(type="table", title="Wire the OLED, button and buzzer", comps=["oled", "btn", "buzz"]),
        3: dict(type="code", title="Upload the sketch", file="pomodoro.ino", code="""const unsigned long FOCUS = 25UL * 60 * 1000;
const unsigned long BREAK = 5UL * 60 * 1000;
bool focusing = true, running = false;
unsigned long left = FOCUS, last = 0;

void setup() { pinMode(2, INPUT_PULLUP); }

void loop() {
  if (digitalRead(2) == LOW) { running = !running; delay(300); }
  unsigned long now = millis();
  if (running) left -= min(left, now - last);
  last = now;
  if (left == 0) {                  // switch focus and break
    tone(9, focusing ? 880 : 660, 400);
    focusing = !focusing;
    left = focusing ? FOCUS : BREAK;
  }
  // draw left / 60000 and (left / 1000) % 60 on the OLED
}"""),
        4: dict(type="oled", title="Start a focus session", lines=["FOCUS", "24:59", "Session 1 of 4"], caption="Press once to start, again to pause"),
    },
    votes=(16, 0.94),
    comments=["Finished my thesis chapter with this, no phone needed.",
              "I changed it to 50 and 10 minutes for review season.",
              "Cute in a pink soap box hehe.",
              "Simple and it actually works. My roommate wants one too.",
              "Used an active buzzer at first, tone() needs the passive kind."],
    critiques=["Wish it saved the session count when unplugged."],
    qa=("aly.manalo", "Can I add a second button to skip a break?",
        "Sure, wire it to pin 3 and set left = 0 when it is pressed. It will jump to the next session."),
))

GUIDES.append(dict(
    title="Posture and Screen Distance Reminder", category="STUDIES", author="liza.cabrera", days=21,
    status="PUBLISHED", difficulty="BEGINNER", risk="LOW", cost=400,
    summary="Clip an ultrasonic sensor on top of your monitor. If your face stays closer than 40 cm for 10 seconds, a soft beep and a red LED remind you to sit back.",
    parts=["Arduino Nano", "HC-SR04 ultrasonic sensor", "Passive buzzer", "Red LED and 220 ohm resistor",
           "Binder clip or 3D printed monitor clip", "USB cable"],
    precautions=["Aim the sensor at your face or chest, not at a wall behind you.",
                 "This is a reminder, not a medical device. See a doctor for back or eye pain."],
    steps=["Clip the HC-SR04 on top of the monitor, facing you.",
           "Wire VCC to 5V, GND to GND, Trig to pin 9 and Echo to pin 10.",
           "Wire the buzzer to pin 8, and the LED through the resistor to pin 13.",
           "Upload the sketch and open the Serial Monitor to see your distance.",
           "Sit the way you normally do, note the distance, and set LIMIT about 10 cm closer than that."],
    video="https://www.youtube.com/watch?v=_eBCxzMrL5Q",
    board=dict(NANO, pins=["5V", "GND", "D8", "D9", "D10", "D13"]),
    comps=[
        dict(id="sonar", name="HC-SR04 sensor", color="yellow", left=[("VCC", "5V"), ("Trig", "D9"), ("Echo", "D10"), ("GND", "GND")]),
        dict(id="buzz", name="Passive buzzer", color="pink", left=[("+", "D8"), ("-", "GND")]),
        dict(id="led", name="Red LED + 220 ohm", color="orange", left=[("+", "D13"), ("-", "GND")]),
    ],
    loads=[], netcolors={},
    images={
        2: dict(type="table", title="Wire the sensor, buzzer and LED", comps=["sonar", "buzz", "led"]),
        4: dict(type="code", title="Upload the sketch", file="posture_reminder.ino", code="""const int TRIG = 9, ECHO = 10, BUZZ = 8, LED = 13;
const int LIMIT = 40;               // cm
unsigned long closeSince = 0;

void setup() {
  Serial.begin(9600);
  pinMode(TRIG, OUTPUT); pinMode(ECHO, INPUT); pinMode(LED, OUTPUT);
}

void loop() {
  digitalWrite(TRIG, LOW);  delayMicroseconds(2);
  digitalWrite(TRIG, HIGH); delayMicroseconds(10);
  digitalWrite(TRIG, LOW);
  long cm = pulseIn(ECHO, HIGH, 30000) / 58;
  Serial.println(cm);
  if (cm > 0 && cm < LIMIT) {
    if (closeSince == 0) closeSince = millis();
    if (millis() - closeSince > 10000) {
      digitalWrite(LED, HIGH); tone(BUZZ, 1000, 150);
    }
  } else { closeSince = 0; digitalWrite(LED, LOW); }
  delay(200);
}"""),
        5: dict(type="serial", title="Find your normal distance", lines=["62", "61", "58   <- normal sitting", "37   <- leaning in", "36"]),
    },
    votes=(10, 0.9),
    comments=["Made this with my Grade 10 robotics club, they loved it.",
              "My neck feels better after a week. Annoying in a good way.",
              "Set LIMIT to 45 for my laptop since the screen is lower."],
    critiques=["False alarms when I reach for my mouse. Upping the 10 seconds to 20 fixed it."],
    qa=("ken.yap", "Does it work with glasses or long hair?",
        "Yes, ultrasound bounces off anything solid. Soft fabric like a hoodie can read a bit far, though."),
))

# ---------------------------------------------------------------- HOUSEKEEPING
GUIDES.append(dict(
    title="Rain Alarm for the Clothesline", category="HOUSEKEEPING", author="pao.dizon", days=44,
    status="PUBLISHED", difficulty="BEGINNER", risk="LOW", cost=470,
    summary="Never lose a load of laundry to a sudden downpour. A rain plate on the sampayan beeps a buzzer inside the house the moment the first drops land.",
    parts=["Arduino Nano", "Rain sensor plate with comparator module", "Active buzzer", "Red LED and 220 ohm resistor",
           "5 meters of 3-core wire", "5V USB charger or a 9V battery with clip", "Small plastic box"],
    precautions=["Only the sensor plate goes outside. The Nano and the comparator module stay indoors.",
                 "Mount the plate tilted so water runs off and it dries quickly after the rain.",
                 "Cover the wire joints at the plate with heat shrink or electrical tape."],
    steps=["Mount the rain plate on the clothesline post, tilted about 30 degrees.",
           "Run the 3-core wire indoors and connect the plate to the comparator module.",
           "Wire the module: VCC to 5V, GND to GND and DO to pin 2.",
           "Wire the buzzer to pin 8, and the LED through the resistor to pin 13.",
           "Upload the sketch, then turn the module's blue knob until its small LED turns off when the plate is dry.",
           "Sprinkle a few drops of water on the plate to test the alarm."],
    video="https://www.youtube.com/watch?v=91xJI2pgOd0",
    board=dict(NANO, pins=["5V", "GND", "D2", "D8", "D13"]),
    comps=[
        dict(id="mod", name="Rain comparator module", note="stays indoors", color="yellow",
             left=[("VCC", "5V"), ("GND", "GND"), ("DO", "D2")], right=[("+", "PLATE+"), ("-", "PLATE-")]),
        dict(id="buzz", name="Active buzzer", color="pink", left=[("+", "D8"), ("-", "GND")]),
        dict(id="led", name="Red LED + 220 ohm", color="orange", left=[("+", "D13"), ("-", "GND")]),
    ],
    loads=[dict(id="plate", name="Rain plate (outside)", note="via 5 m wire", left=[("+", "PLATE+"), ("-", "PLATE-")])],
    netcolors={"PLATE+": "cyan", "PLATE-": "gray"},
    images={
        3: dict(type="table", title="Wire the module, buzzer and LED", comps=["mod", "buzz", "led"]),
        5: dict(type="code", title="Upload the sketch", file="rain_alarm.ino", code="""const int RAIN = 2, BUZZ = 8, LED = 13;

void setup() {
  pinMode(RAIN, INPUT);
  pinMode(BUZZ, OUTPUT);
  pinMode(LED, OUTPUT);
}

void loop() {
  bool wet = digitalRead(RAIN) == LOW;  // DO goes LOW when wet
  digitalWrite(LED, wet);
  if (wet) {                            // beep-beep until it dries
    digitalWrite(BUZZ, HIGH); delay(200);
    digitalWrite(BUZZ, LOW);  delay(800);
  } else {
    digitalWrite(BUZZ, LOW);  delay(500);
  }
}"""),
    },
    votes=(26, 0.96),
    comments=["Saved my uniform twice already this week. Rainy season essential.",
              "Lola approves. She says it's better than looking at the sky every 5 minutes.",
              "I used a doorbell chime instead of a buzzer, louder and nicer.",
              "Put the module in a Tupperware, been outside for a month with no problems.",
              "The knob adjustment step is important, mine kept beeping from humidity at first.",
              "Simple, cheap and very Pinoy. Love it.",
              "Works with a 10 m wire too, tested it to our rooftop."],
    critiques=["Dew in the early morning sets it off. I added a 2 minute wait before beeping."],
    qa=("grace.valdez", "Can it send a notification to my phone instead?",
        "Yes, swap the Nano for an ESP8266 and send a Telegram message when it gets wet. I'll post that version soon."),
))

GUIDES.append(dict(
    title="Touchless Trash Can Lid", category="HOUSEKEEPING", author="kurt.pascual", days=27,
    status="PUBLISHED", difficulty="BEGINNER", risk="LOW", cost=800,
    summary="Wave your hand over the bin and the lid opens by itself, then closes after 3 seconds. No more touching a dirty lid in the kitchen.",
    parts=["Arduino Uno", "HC-SR04 ultrasonic sensor", "MG90S metal gear servo", "4 x AA battery holder for the servo",
           "Trash can with a hinged lid", "Popsicle sticks, glue gun and strong string"],
    precautions=["Power the servo from the battery pack, not the Arduino's 5V pin, or the Arduino keeps resetting.",
                 "Connect the battery pack's - to the Arduino GND so both share the same ground.",
                 "Keep fingers away from the lid hinge while testing."],
    steps=["Hot glue the HC-SR04 on the front of the lid, facing up.",
           "Mount the servo on the back of the bin and tie its horn to the lid with string.",
           "Wire the sensor: VCC to 5V, GND to GND, Trig to pin 5 and Echo to pin 6.",
           "Wire the servo: signal to pin 9, red to the battery pack +, and brown to GND.",
           "Upload the sketch and change OPEN_ANGLE until the lid opens all the way."],
    video="https://www.youtube.com/watch?v=pNcWlpd4Evo",
    board=dict(UNO, pins=["5V", "GND", "D5", "D6", "D9"]),
    comps=[
        dict(id="sonar", name="HC-SR04 sensor", color="yellow", left=[("VCC", "5V"), ("Trig", "D5"), ("Echo", "D6"), ("GND", "GND")]),
        dict(id="servo", name="MG90S servo", color="cyan", left=[("Signal", "D9"), ("Red V+", "6V"), ("Brown GND", "GND")]),
        dict(id="batt", name="4 x AA battery pack", note="6V for the servo", color="orange", left=[("+", "6V"), ("-", "GND")]),
    ],
    loads=[], netcolors={},
    images={
        3: dict(type="table", title="Wire the sensor, servo and batteries", comps=["sonar", "servo", "batt"]),
        5: dict(type="code", title="Upload the sketch", file="touchless_bin.ino", code="""#include <Servo.h>

Servo lid;
const int TRIG = 5, ECHO = 6;
const int OPEN_ANGLE = 100, CLOSED = 0;

void setup() {
  pinMode(TRIG, OUTPUT); pinMode(ECHO, INPUT);
  lid.attach(9);
  lid.write(CLOSED);
}

void loop() {
  digitalWrite(TRIG, LOW);  delayMicroseconds(2);
  digitalWrite(TRIG, HIGH); delayMicroseconds(10);
  digitalWrite(TRIG, LOW);
  long cm = pulseIn(ECHO, HIGH, 30000) / 58;
  if (cm > 0 && cm < 20) {        // hand within 20 cm
    lid.write(OPEN_ANGLE);
    delay(3000);
    lid.write(CLOSED);
  }
  delay(100);
}"""),
    },
    votes=(14, 0.79),
    comments=["Mom thought it was magic. 10/10 school project.",
              "Used the MG90S like you said, SG90 could not lift our lid.",
              "Glued the sensor under a small roof so kitchen splashes don't hit it."],
    critiques=["Batteries died after a week. A 5V 2A adapter for the servo is better.",
               "Lid slams shut, add a slow close loop with small steps."],
    qa=("angela.torres", "How do I make the lid close slowly?",
        "Instead of lid.write(CLOSED), loop from OPEN_ANGLE down to 0 with a 15 ms delay per degree."),
))

# ---------------------------------------------------------------- ENERGY SAVING
GUIDES.append(dict(
    title="Room Light Auto-Off with Visitor Counter", category="ENERGY_SAVING", author="pao.dizon", days=40,
    status="PUBLISHED", difficulty="INTERMEDIATE", risk="HIGH", cost=1050, approved_by="engr.mendoza",
    summary="Two IR sensors at the door count people going in and out. The room light turns on when the first person enters and turns off when the last one leaves, so lights are never left on in an empty room.",
    parts=["Arduino Uno", "Two IR obstacle sensor modules", "16x2 LCD with I2C backpack", "5V relay module rated 10A at 250V AC",
           "Enclosed project box for the relay", "Wire rated for 10A", "Wall switch box and screws"],
    precautions=["The relay switches the 230V room light. Turn off the breaker before touching the light wiring.",
                 "Only the LIVE wire goes through the relay. Neutral goes straight to the light.",
                 "Keep every 230V connection inside a closed box, and have an electrician check it before you switch the breaker back on."],
    steps=["Mount the two IR sensors on the door frame about 10 cm apart, sensor A outside and sensor B inside.",
           "Wire sensor A OUT to pin 2, sensor B OUT to pin 3, and both VCC and GND to 5V and GND.",
           "Connect the LCD to A4 and A5, and the relay IN to pin 8.",
           "With the breaker OFF, cut the light's LIVE wire and connect one end to the relay COM and the other end to NO.",
           "Upload the sketch and walk in and out a few times to check the count.",
           "Close the relay box and turn the breaker back on."],
    video="https://www.youtube.com/watch?v=xWSSCCZsMEU",
    board=dict(UNO, pins=["5V", "GND", "D2", "D3", "D8", "A4", "A5"]),
    comps=[
        dict(id="irA", name="IR sensor A (outside)", color="yellow", left=[("VCC", "5V"), ("GND", "GND"), ("OUT", "D2")]),
        dict(id="irB", name="IR sensor B (inside)", color="green", left=[("VCC", "5V"), ("GND", "GND"), ("OUT", "D3")]),
        dict(id="lcd", name="16x2 LCD (I2C)", color="cyan", left=[("VCC", "5V"), ("GND", "GND"), ("SDA", "A4"), ("SCL", "A5")]),
        dict(id="relay", name="Relay module 10A", color="pink", left=[("VCC", "5V"), ("GND", "GND"), ("IN", "D8")],
             right=[("COM", "L-IN"), ("NO", "L-OUT")]),
    ],
    loads=[dict(id="mains", name="230V from breaker", left=[("LIVE", "L-IN"), ("NEUTRAL", "N")]),
           dict(id="lamp", name="Room light", left=[("LIVE", "L-OUT"), ("NEUTRAL", "N")])],
    netcolors={"L-IN": "mains", "L-OUT": "mains", "N": "neutral"},
    images={
        2: dict(type="table", title="Wire the sensors, LCD and relay", comps=["irA", "irB", "lcd", "relay"], side="left"),
        4: dict(type="table", title="Light side (breaker OFF)", comps=["relay"], side="right", loads=["mains", "lamp"]),
        5: dict(type="code", title="Upload the sketch", file="visitor_counter.ino", code="""const int A = 2, B = 3, RELAY = 8;
int inside = 0;

void setup() {
  pinMode(A, INPUT); pinMode(B, INPUT);
  pinMode(RELAY, OUTPUT);
}

// IR modules output LOW when something is in front of them
void loop() {
  if (digitalRead(A) == LOW) {          // A first: coming in
    while (digitalRead(B) == HIGH) {}
    inside++;
    delay(800);
  } else if (digitalRead(B) == LOW) {   // B first: going out
    while (digitalRead(A) == HIGH) {}
    if (inside > 0) inside--;
    delay(800);
  }
  digitalWrite(RELAY, inside > 0 ? HIGH : LOW);
  // print "Inside: " + inside on the LCD here
}"""),
        6: dict(type="lcd", title="Check the count", lines=["Inside: 3", "Light: ON"], caption="The light goes off when it reaches 0"),
    },
    votes=(25, 0.92),
    comments=["Our org room's light is finally off at night. Treasurer is happy.",
              "Engr. Mendoza's review notes helped me fix my relay box, thank you.",
              "Placed the sensors at hip height so bags don't double count.",
              "Great demo for our energy audit report.",
              "LCD count is satisfying to watch hehe.",
              "Would be nice with a manual override switch, but works great."],
    critiques=["Two people walking side by side count as one. Fine for a small room."],
    qa=("migs.santos", "Why does the code wait inside the while loop?",
        "It waits for the second sensor so it knows the direction. If someone turns back halfway it can get stuck, so I'm adding a 2 second timeout next."),
))

GUIDES.append(dict(
    title="Home Energy Meter with PZEM-004T", category="ENERGY_SAVING", author="rj.manalastas", days=3,
    status="PENDING", difficulty="ADVANCED", risk="HIGH", cost=1350,
    summary="Measure the voltage, current, power and kWh of a circuit at home with a PZEM-004T and an ESP32, and see the numbers on your phone over Wi-Fi.",
    parts=["ESP32 development board", "PZEM-004T v3.0 with 100A CT coil", "5V 1A power supply module (HLK-PM01)",
           "Enclosed DIN-rail or project box", "Wire rated for the circuit", "Terminal blocks"],
    precautions=["This guide connects to 230V mains. Turn off the breaker before opening anything.",
                 "The CT coil clamps around the LIVE wire only. Never clamp both live and neutral together.",
                 "Mount everything in a closed, non-metal box and have a licensed electrician do the panel connections."],
    steps=["With the breaker OFF, clamp the CT coil around the LIVE wire of the circuit you want to measure.",
           "Connect the PZEM's L and N voltage terminals to the same circuit with terminal blocks.",
           "Wire the PZEM's 5V, GND, TX and RX to the ESP32: TX to GPIO16 and RX to GPIO17.",
           "Install the PZEM004Tv30 library and upload the sketch with your Wi-Fi name and password.",
           "Close the box, turn the breaker on, and open the ESP32's IP address on your phone."],
    video="https://www.youtube.com/watch?v=0igolzx-FS4",
    board=dict(ESP32, pins=["VIN", "GND", "GPIO16", "GPIO17"]),
    comps=[
        dict(id="pzem", name="PZEM-004T v3.0", note="TTL side to ESP32", color="yellow",
             left=[("5V", "VIN"), ("RX", "GPIO17"), ("TX", "GPIO16"), ("GND", "GND")],
             right=[("L", "L"), ("N", "N"), ("CT", "CT")]),
    ],
    loads=[dict(id="mains", name="230V circuit", left=[("LIVE", "L"), ("NEUTRAL", "N")]),
           dict(id="ct", name="CT coil", note="clamped on LIVE only", left=[("CT", "CT")])],
    netcolors={"L": "mains", "N": "neutral", "CT": "orange"},
    images={
        3: dict(type="table", title="Wire the PZEM to the ESP32", comps=["pzem"], side="left"),
        5: dict(type="phone", title="Read it on your phone", heading="Energy Meter", rows=[("Voltage", "229.8 V"), ("Current", "2.41 A"), ("Power", "512 W"), ("Energy", "38.6 kWh")]),
    },
    votes=(0, 0), comments=[], critiques=[], qa=None,
))

# ---------------------------------------------------------------- HOME SECURITY
GUIDES.append(dict(
    title="Laser Tripwire Alarm", category="HOME_SECURITY", author="josh.ramos", days=35,
    status="PUBLISHED", difficulty="BEGINNER", risk="LOW", cost=420,
    summary="Shine a laser across a hallway or doorway into a light sensor. When someone breaks the beam, a buzzer goes off until you press reset.",
    parts=["Arduino Nano", "5V laser diode module", "LDR light sensor module", "Active buzzer", "Push button for reset",
           "Two small boxes or 3D printed holders", "Long jumper wires"],
    precautions=["Never look into the laser or point it at people's eyes, even a small module can hurt eyes.",
                 "Mount the laser below eye level, for example at ankle height across the doorway."],
    steps=["Mount the laser on one side of the doorway and the LDR module on the other side, at ankle height.",
           "Wire the laser S pin to pin 7, and its - to GND.",
           "Wire the LDR module: VCC to 5V, GND to GND and DO to pin 2.",
           "Wire the buzzer to pin 8 and the reset button between pin 4 and GND.",
           "Upload the sketch, aim the laser at the LDR, and turn the module's knob until its LED just turns off.",
           "Walk through the beam to test the alarm, then press reset."],
    video="https://www.youtube.com/watch?v=NEzStFXjyZs",
    board=dict(NANO, pins=["5V", "GND", "D2", "D4", "D7", "D8"]),
    comps=[
        dict(id="laser", name="Laser module", note="points at the LDR", color="pink", left=[("S", "D7"), ("-", "GND")]),
        dict(id="ldr", name="LDR sensor module", color="yellow", left=[("VCC", "5V"), ("GND", "GND"), ("DO", "D2")]),
        dict(id="buzz", name="Active buzzer", color="orange", left=[("+", "D8"), ("-", "GND")]),
        dict(id="btn", name="Reset button", color="green", left=[("1", "D4"), ("2", "GND")]),
    ],
    loads=[], netcolors={},
    images={
        4: dict(type="table", title="Wire the laser, sensor, buzzer and button", comps=["laser", "ldr", "buzz", "btn"]),
        5: dict(type="code", title="Upload the sketch", file="laser_tripwire.ino", code="""const int LDR = 2, RESET = 4, LASER = 7, BUZZ = 8;
bool alarm = false;

void setup() {
  pinMode(LDR, INPUT);
  pinMode(RESET, INPUT_PULLUP);
  pinMode(LASER, OUTPUT);
  pinMode(BUZZ, OUTPUT);
  digitalWrite(LASER, HIGH);
  delay(1000);                         // let the sensor settle
}

void loop() {
  // the module's DO goes HIGH when the beam is blocked
  if (digitalRead(LDR) == HIGH) alarm = true;
  if (digitalRead(RESET) == LOW) alarm = false;
  digitalWrite(BUZZ, alarm);
}"""),
    },
    votes=(15, 0.87),
    comments=["Little brother cannot sneak into my room anymore.",
              "Used a mirror to bounce the beam around a corner, works!",
              "Good intro to sensors for our Grade 8 class.",
              "Daytime sunlight on the sensor caused misses, a black straw over the LDR fixed it."],
    critiques=["My LDR module's output was the opposite, I had to change HIGH to LOW."],
    qa=("edwin.lagman", "Is a 5mW laser safe for a classroom demo?",
        "It's low power but still not safe to look into. Mount it low, and tell students never to aim it at faces."),
))

GUIDES.append(dict(
    title="RFID Door Lock with Solenoid", category="HOME_SECURITY", author="pao.dizon", days=19,
    status="PUBLISHED", difficulty="INTERMEDIATE", risk="MODERATE", cost=1200,
    summary="Tap an RFID card or key fob to unlock a cabinet or room door for 5 seconds. Unknown cards get a double beep and the door stays locked.",
    parts=["Arduino Uno", "RC522 RFID reader with card and key fob", "12V solenoid lock", "5V relay module", "12V 2A power adapter",
           "1N4007 diode", "Active buzzer", "Project box"],
    precautions=["The RC522 runs on 3.3V. Connecting it to 5V can damage it.",
                 "Put the 1N4007 diode across the solenoid (stripe to +) to protect the relay from voltage spikes.",
                 "Keep a key or another way in. If the power goes out, a fail-secure lock stays locked."],
    steps=["Wire the RC522 to the Uno: SDA to 10, SCK to 13, MOSI to 11, MISO to 12, RST to 9, 3.3V to 3.3V and GND to GND.",
           "Wire the relay IN to pin 7 and the buzzer to pin 6.",
           "Connect the adapter's + to the relay COM, NO to the lock's +, and the lock's - back to the adapter's -.",
           "Install the MFRC522 library, upload the sketch, and open the Serial Monitor.",
           "Tap your card, copy the UID it prints into the sketch, and upload again.",
           "Mount the reader inside the door so it reads through the wood or plastic."],
    video="https://www.youtube.com/watch?v=E4sRQ7teQ2w",
    board=dict(UNO, pins=["3V3", "5V", "GND", "D6", "D7", "D9", "D10", "D11", "D12", "D13"]),
    comps=[
        dict(id="rfid", name="RC522 RFID reader", note="3.3V only", color="cyan",
             left=[("3.3V", "3V3"), ("RST", "D9"), ("GND", "GND"), ("MISO", "D12"), ("MOSI", "D11"), ("SCK", "D13"), ("SDA", "D10")]),
        dict(id="relay", name="5V relay module", color="green", left=[("VCC", "5V"), ("GND", "GND"), ("IN", "D7")],
             right=[("COM", "12V+"), ("NO", "LOCK+")]),
        dict(id="buzz", name="Active buzzer", color="pink", left=[("+", "D6"), ("-", "GND")]),
    ],
    loads=[dict(id="psu", name="12V 2A adapter", left=[("+", "12V+"), ("-", "12V-")]),
           dict(id="lock", name="12V solenoid lock", note="1N4007 across it", left=[("+", "LOCK+"), ("-", "12V-")])],
    netcolors={"12V+": "orange", "LOCK+": "orange", "12V-": "gray"},
    images={
        1: dict(type="table", title="Wire the RC522 reader", comps=["rfid"]),
        3: dict(type="table", title="Lock side (12V)", comps=["relay"], side="right", loads=["psu", "lock"]),
        5: dict(type="serial", title="Copy your card's UID", lines=["Tap a card...", "UID: 73 A1 4F 2C   ACCESS DENIED", "(paste 73 A1 4F 2C into the sketch)", "UID: 73 A1 4F 2C   ACCESS GRANTED"]),
    },
    votes=(27, 0.93),
    comments=["Installed it on our tool cabinet, only the officers can open it now.",
              "The 3.3V warning saved my RC522. Thank you!",
              "Key fob works through 8 mm plywood.",
              "Added a green LED for granted and red for denied, looks pro.",
              "Clear diode tip, my first relay died without it."],
    critiques=["Fail-secure lock means locked when power is out. I switched to a fail-safe one for our room."],
    qa=("vince.navarro", "Can I add more cards later without uploading again?",
        "Yes, save the UIDs in EEPROM and add a master card that puts it in add mode. I'll update the guide with that."),
))

# ---------------------------------------------------------------- HEALTH AND WELLNESS
GUIDES.append(dict(
    title="Touchless Hand Sanitizer Dispenser", category="HEALTH_WELLNESS", author="camille.rivera", days=33,
    status="PUBLISHED", difficulty="BEGINNER", risk="LOW", cost=650,
    summary="Put your hand under the nozzle and a small pump squirts one dose of alcohol or soap. Made for the clinic door at work, cheap enough for every classroom.",
    parts=["Arduino Nano", "IR obstacle sensor module", "5V relay module", "5V mini submersible pump and silicone tube",
           "Plastic bottle for the alcohol", "5V 2A USB adapter", "Small project box"],
    precautions=["Alcohol is flammable. Keep the electronics in a separate closed box above the bottle, never inside it.",
                 "Use only a low voltage 5V pump.",
                 "Wipe up spills right away, alcohol and electronics do not mix."],
    steps=["Put the pump in the bottle and run the tube up to a nozzle under the box.",
           "Mount the IR sensor next to the nozzle, facing down.",
           "Wire the sensor OUT to pin 2, and VCC and GND to 5V and GND.",
           "Wire the relay IN to pin 7, then the adapter's + to COM, NO to the pump's +, and the pump's - to the adapter's -.",
           "Upload the sketch and change SQUIRT_MS until one squirt is enough for your hands."],
    video="https://www.youtube.com/watch?v=_vYrPlYFLcM",
    board=dict(NANO, pins=["5V", "GND", "D2", "D7"]),
    comps=[
        dict(id="ir", name="IR obstacle sensor", color="yellow", left=[("VCC", "5V"), ("GND", "GND"), ("OUT", "D2")]),
        dict(id="relay", name="5V relay module", color="green", left=[("VCC", "5V"), ("GND", "GND"), ("IN", "D7")],
             right=[("COM", "P5V+"), ("NO", "PUMP+")]),
    ],
    loads=[dict(id="psu", name="5V 2A adapter", left=[("+", "P5V+"), ("-", "P5V-")]),
           dict(id="pump", name="5V mini pump", left=[("+", "PUMP+"), ("-", "P5V-")])],
    netcolors={"P5V+": "red", "PUMP+": "red", "P5V-": "gray"},
    images={
        3: dict(type="table", title="Arduino side", comps=["ir", "relay"], side="left"),
        4: dict(type="table", title="Pump side", comps=["relay"], side="right", loads=["psu", "pump"]),
        5: dict(type="code", title="Upload the sketch", file="sanitizer.ino", code="""const int IR = 2, RELAY = 7;
const int SQUIRT_MS = 400;     // longer = bigger dose

void setup() {
  pinMode(IR, INPUT);
  pinMode(RELAY, OUTPUT);
}

void loop() {
  if (digitalRead(IR) == LOW) {        // hand detected
    digitalWrite(RELAY, HIGH);
    delay(SQUIRT_MS);
    digitalWrite(RELAY, LOW);
    while (digitalRead(IR) == LOW) {}  // wait for the hand to leave
    delay(500);
  }
}"""),
    },
    votes=(12, 0.92),
    comments=["Our school clinic uses it every day now.",
              "400 ms was too much for alcohol, 250 ms is perfect.",
              "Works with liquid soap too if you dilute it a little.",
              "Mounted it by the canteen sink, kids love it."],
    critiques=["The IR sensor triggers from sunlight near the window. Moved it and it's fine."],
    qa=("rose.alcantara", "Will the pump corrode from alcohol?",
        "Mine is fine after 2 months with 70% isopropyl. Flush it with water every few weeks to be safe."),
))

GUIDES.append(dict(
    title="Heart Rate Monitor with Pulse Sensor", category="HEALTH_WELLNESS", author="mika.ocampo", days=16,
    status="PUBLISHED", difficulty="BEGINNER", risk="LOW", cost=780,
    summary="Rest your fingertip on a pulse sensor and see your heart rate in beats per minute on an OLED, with a small heart that blinks on every beat.",
    parts=["Arduino Uno", "Pulse Sensor (green round sensor)", "0.96 inch OLED display (I2C)", "Velcro strap for the finger", "Jumper wires"],
    precautions=["This is a school project, not a medical device. Do not use it to check a health condition.",
                 "Rest your finger lightly. Pressing hard squeezes out the blood and gives wrong readings."],
    steps=["Wire the Pulse Sensor: + to 5V, - to GND and S to A0.",
           "Connect the OLED: VCC to 5V, GND to GND, SDA to A4 and SCL to A5.",
           "Install the PulseSensor Playground and Adafruit SSD1306 libraries.",
           "Upload the sketch, strap the sensor on your fingertip, and sit still for 10 seconds.",
           "Compare the BPM with a smartwatch or by counting your pulse for 30 seconds and doubling it."],
    video="https://www.youtube.com/watch?v=JpN-S6ALUWw",
    board=dict(UNO, pins=["5V", "GND", "A0", "A4", "A5"]),
    comps=[
        dict(id="pulse", name="Pulse Sensor", note="fingertip, light touch", color="pink", left=[("+", "5V"), ("-", "GND"), ("S", "A0")]),
        dict(id="oled", name="OLED 0.96 in (I2C)", color="cyan", left=[("VCC", "5V"), ("GND", "GND"), ("SDA", "A4"), ("SCL", "A5")]),
    ],
    loads=[], netcolors={},
    images={
        2: dict(type="table", title="Wire the sensor and the OLED", comps=["pulse", "oled"]),
        4: dict(type="code", title="Upload the sketch", file="heart_rate.ino", code="""#define USE_ARDUINO_INTERRUPTS true
#include <PulseSensorPlayground.h>

PulseSensorPlayground pulse;

void setup() {
  Serial.begin(9600);
  pulse.analogInput(A0);
  pulse.setThreshold(550);    // raise it if you get false beats
  pulse.begin();
}

void loop() {
  if (pulse.sawStartOfBeat()) {
    int bpm = pulse.getBeatsPerMinute();
    Serial.print("BPM: ");
    Serial.println(bpm);
    // draw bpm and a heart on the OLED here
  }
  delay(20);
}"""),
        5: dict(type="oled", title="Check your BPM", lines=["HEART RATE", "74 BPM", "<3"], caption="Resting heart rate after 10 seconds"),
    },
    votes=(11, 0.82),
    comments=["Great for our Biology and ICT integration project.",
              "My smartwatch said 76, this said 74. Close enough!",
              "The tip about pressing lightly is so true."],
    critiques=["Readings jump a lot if you move even a little.",
               "Cold fingers give no reading at all, warm them up first."],
    qa=("nicole.fernandez", "Can I use a MAX30102 instead?",
        "Yes, it's more accurate and also reads oxygen, but it uses I2C and a different library, so the code changes."),
))

# ---------------------------------------------------------------- PETS
GUIDES.append(dict(
    title="Automatic Pet Feeder with Real-Time Clock", category="PETS", author="ria.domingo", days=26,
    status="PUBLISHED", difficulty="INTERMEDIATE", risk="LOW", cost=950,
    summary="Feed your dog or cat at 7 AM and 6 PM even when nobody is home. A DS3231 clock keeps time and a servo turns a cereal dispenser for one portion.",
    parts=["Arduino Uno", "DS3231 real-time clock module with CR2032 battery", "MG996R servo", "Cereal dispenser or a big plastic bottle with a rotating flap",
           "Push button for manual feeding", "5V 2A adapter for the servo", "Wood or acrylic stand"],
    precautions=["Power the MG996R from its own 5V 2A adapter and connect its - to the Arduino GND.",
                 "Test the portion size for a few days while you are home before trusting it alone.",
                 "Use dry food only. Wet food spoils and jams the dispenser."],
    steps=["Attach the servo horn to the dispenser's knob or flap so one turn drops one portion.",
           "Wire the DS3231: VCC to 5V, GND to GND, SDA to A4 and SCL to A5.",
           "Wire the servo signal to pin 9, its red wire to the adapter +, and its brown wire to GND.",
           "Wire the feed button between pin 2 and GND.",
           "Install the RTClib library, upload the sketch once to set the clock, then upload it again with the setup line removed.",
           "Fill it with food and press the button to test one portion."],
    video="https://www.youtube.com/watch?v=mjiexlL6Cfo",
    board=dict(UNO, pins=["5V", "GND", "D2", "D9", "A4", "A5"]),
    comps=[
        dict(id="rtc", name="DS3231 clock", note="CR2032 keeps the time", color="cyan", left=[("VCC", "5V"), ("GND", "GND"), ("SDA", "A4"), ("SCL", "A5")]),
        dict(id="servo", name="MG996R servo", color="yellow", left=[("Signal", "D9"), ("Red V+", "SERVO5V"), ("Brown GND", "GND")]),
        dict(id="psu", name="5V 2A adapter", note="servo power only", color="orange", left=[("+", "SERVO5V"), ("-", "GND")]),
        dict(id="btn", name="Feed now button", color="green", left=[("1", "D2"), ("2", "GND")]),
    ],
    loads=[], netcolors={},
    images={
        2: dict(type="table", title="Wire the clock, servo and button", comps=["rtc", "servo", "psu", "btn"]),
        5: dict(type="code", title="Upload the sketch", file="pet_feeder.ino", code="""#include <RTClib.h>
#include <Servo.h>

RTC_DS3231 rtc;
Servo flap;
int lastFed = -1;

void feed() {
  flap.write(180); delay(700);   // one portion
  flap.write(0);   delay(700);
}

void setup() {
  pinMode(2, INPUT_PULLUP);
  flap.attach(9); flap.write(0);
  rtc.begin();
  // rtc.adjust(DateTime(F(__DATE__), F(__TIME__)));  first upload only
}

void loop() {
  DateTime now = rtc.now();
  bool mealTime = (now.hour() == 7 || now.hour() == 18) && now.minute() == 0;
  if (mealTime && now.hour() != lastFed) { feed(); lastFed = now.hour(); }
  if (digitalRead(2) == LOW) { feed(); delay(500); }
}"""),
    },
    votes=(14, 0.86),
    comments=["Our aspin gets breakfast on time even when we leave early for school.",
              "The DS3231 keeps time even after a brownout, very nice.",
              "Used a cereal dispenser from Shopee, fits the MG996R perfectly."],
    critiques=["Big kibble jams the flap sometimes. Works better with small pellets.",
               "Wish it had a screen to change feeding times without uploading."],
    qa=("bea.mendoza", "How do I give 2 portions?",
        "Call feed() twice inside the if, or make the servo stay open longer, like 1200 ms instead of 700."),
))

GUIDES.append(dict(
    title="Automatic Fish Feeder", category="PETS", author="noel.flores", days=13,
    status="PUBLISHED", difficulty="BEGINNER", risk="LOW", cost=400,
    summary="A small container on a servo tips a pinch of fish food into the tank every few hours. Turn a knob to choose how often, from every 4 to every 24 hours.",
    parts=["Arduino Nano", "SG90 servo", "10k potentiometer", "Push button for feeding now", "Small pill container or film canister",
           "USB charger", "Clamp or bracket for the tank rim"],
    precautions=["Mount everything above the water on the tank rim, away from splashes.",
                 "Start with a tiny portion. Overfeeding dirties the water and harms the fish.",
                 "Unplug the charger before cleaning the tank."],
    steps=["Drill a 5 mm hole in the side of the container and glue it to the servo horn.",
           "Clamp the servo on the tank rim so the container tips over the water.",
           "Wire the servo signal to pin 9, red to 5V and brown to GND.",
           "Wire the potentiometer's middle pin to A0 and its outer pins to 5V and GND, and the button between pin 2 and GND.",
           "Upload the sketch, set the knob, and press the button to test a portion."],
    video="https://www.youtube.com/watch?v=AQl1W4WqoNQ",
    board=dict(NANO, pins=["5V", "GND", "D2", "D9", "A0"]),
    comps=[
        dict(id="servo", name="SG90 servo", color="yellow", left=[("Signal", "D9"), ("Red V+", "5V"), ("Brown GND", "GND")]),
        dict(id="pot", name="10k potentiometer", note="sets 4 to 24 hours", color="cyan", left=[("Left", "5V"), ("Middle", "A0"), ("Right", "GND")]),
        dict(id="btn", name="Feed now button", color="green", left=[("1", "D2"), ("2", "GND")]),
    ],
    loads=[], netcolors={},
    images={
        3: dict(type="table", title="Wire the servo, knob and button", comps=["servo", "pot", "btn"]),
        5: dict(type="code", title="Upload the sketch", file="fish_feeder.ino", code="""#include <Servo.h>

Servo cup;
unsigned long lastFeed = 0;

void feed() {
  cup.write(160); delay(400);   // tip a pinch out
  cup.write(0);
  lastFeed = millis();
}

void setup() {
  pinMode(2, INPUT_PULLUP);
  cup.attach(9); cup.write(0);
}

void loop() {
  int hours = map(analogRead(A0), 0, 1023, 4, 24);
  unsigned long every = hours * 3600000UL;
  if (millis() - lastFeed >= every) feed();
  if (digitalRead(2) == LOW) { feed(); delay(500); }
}"""),
    },
    votes=(10, 0.9),
    comments=["My guppies are fed while I'm at school. Works for 3 weeks now.",
              "Film canister with a 4 mm hole gives the perfect pinch.",
              "Kids at our school's aquarium club built five of these."],
    critiques=["The knob is hard to read. I marked the hours with a marker around it."],
    qa=("lea.pineda", "Does it forget the timer after a brownout?",
        "Yes, millis() restarts at 0, so it feeds again after the set hours. Add a DS3231 if you want exact times."),
))


# ---------------------------------------------------------------- existing starter guides (schematic + video only)
EXISTING = [
    dict(title="Automatic Plant Watering System", video="https://www.youtube.com/watch?v=t3ODW-eG3y0",
         board=dict(UNO, pins=["5V", "GND", "D7", "A0"]),
         comps=[dict(id="soil", name="Soil moisture sensor", color="yellow", left=[("VCC", "5V"), ("GND", "GND"), ("AO", "A0")]),
                dict(id="relay", name="5V relay module", color="green", left=[("VCC", "5V"), ("GND", "GND"), ("IN", "D7")],
                     right=[("COM", "P+"), ("NO", "PUMP+")])],
         loads=[dict(id="psu", name="5V pump supply", left=[("+", "P+"), ("-", "P-")]),
                dict(id="pump", name="5V mini water pump", left=[("+", "PUMP+"), ("-", "P-")])],
         netcolors={"P+": "red", "PUMP+": "red", "P-": "gray"}),
    dict(title="Door Sensor Alarm with Buzzer", video="https://www.youtube.com/watch?v=pDpM-D-qMhA",
         board=dict(NANO, pins=["VIN", "GND", "D2", "D8"]),
         comps=[dict(id="reed", name="Magnetic reed switch", note="magnet goes on the door", color="yellow", left=[("1", "D2"), ("2", "GND")]),
                dict(id="buzz", name="Active buzzer", color="pink", left=[("+", "D8"), ("-", "GND")]),
                dict(id="bat", name="9V battery + switch", color="orange", left=[("+", "VIN"), ("-", "GND")])],
         loads=[], netcolors={}),
    dict(title="Motion-Activated Hallway Light", video="https://www.youtube.com/watch?v=ugDPdgGekNo",
         board=dict(NANO, pins=["5V", "GND", "D2", "D9"]),
         comps=[dict(id="pir", name="PIR motion sensor", color="yellow", left=[("VCC", "5V"), ("OUT", "D2"), ("GND", "GND")]),
                dict(id="mos", name="MOSFET module", color="green", left=[("SIG", "D9"), ("GND", "GND")],
                     right=[("VIN+", "12V+"), ("VIN-", "12V-"), ("V+", "S+"), ("V-", "S-")])],
         loads=[dict(id="psu", name="12V adapter", left=[("+", "12V+"), ("-", "12V-")]),
                dict(id="strip", name="12V LED strip (1 m)", left=[("+", "S+"), ("-", "S-")])],
         netcolors={"12V+": "orange", "12V-": "gray", "S+": "orange", "S-": "white"}),
    dict(title="Laundry Done Notifier", video="https://www.youtube.com/watch?v=D1Uvg5eZ4Xs",
         board=dict(name="ESP8266 NodeMCU", alias={"D2": "D2 (GPIO4)"}, pins=["3V3", "GND", "D2"]),
         comps=[dict(id="vib", name="SW-420 vibration sensor", color="yellow", left=[("VCC", "3V3"), ("GND", "GND"), ("DO", "D2")])],
         loads=[], netcolors={}),
    dict(title="Bedroom Air Quality Monitor", video="https://www.youtube.com/watch?v=Pk1PBwtSPE8",
         board=dict(ESP32, pins=["VIN", "3V3", "GND", "GPIO4", "GPIO21", "GPIO22", "GPIO25", "GPIO26", "GPIO27", "GPIO34"]),
         comps=[dict(id="mq", name="MQ-135 gas sensor", note="10k/20k divider on AO", color="yellow", left=[("VCC", "VIN"), ("GND", "GND"), ("AO", "GPIO34")]),
                dict(id="dht", name="DHT22 sensor", color="green", left=[("VCC", "3V3"), ("DATA", "GPIO4"), ("GND", "GND")]),
                dict(id="oled", name="OLED 0.96 in (I2C)", color="cyan", left=[("VCC", "3V3"), ("GND", "GND"), ("SDA", "GPIO21"), ("SCL", "GPIO22")]),
                dict(id="rgb", name="RGB LED (common -)", note="220 ohm on each color", color="pink",
                     left=[("R", "GPIO25"), ("G", "GPIO26"), ("B", "GPIO27"), ("-", "GND")])],
         loads=[], netcolors={}),
    dict(title="Pet Water Bowl Refill Alert", video="https://www.youtube.com/watch?v=AURxODjCcoo",
         board=dict(NANO, pins=["5V", "GND", "D8", "A0"]),
         comps=[dict(id="lvl", name="Water level sensor", note="only the tip in the water", color="cyan", left=[("+", "5V"), ("-", "GND"), ("S", "A0")]),
                dict(id="buzz", name="Buzzer", color="pink", left=[("+", "D8"), ("-", "GND")])],
         loads=[], netcolors={}),
    dict(title="Solar Phone Charging Station", video="https://www.youtube.com/watch?v=SPpgMl_TeIc",
         board=dict(name="TP4056 charger module", alias={}, pins=["IN+", "IN-", "B+", "B-", "OUT+", "OUT-"]),
         comps=[dict(id="pv", name="6V solar panel", note="TP4056 takes 8V max", color="orange", left=[("+", "IN+"), ("-", "IN-")]),
                dict(id="cells", name="18650 cells (parallel)", color="yellow", left=[("+", "B+"), ("-", "B-")]),
                dict(id="boost", name="USB boost converter", color="green", left=[("IN+", "OUT+"), ("IN-", "OUT-")], right=[("USB 5V", "USB")]),
                dict(id="bar", name="Battery level LED bar", color="cyan", left=[("+", "OUT+"), ("-", "OUT-")])],
         loads=[dict(id="phone", name="Phone", left=[("USB", "USB")])],
         netcolors={"USB": "red"}),
]

# A spam post the admin already hid (shows moderation in the Admin Panel)
SPAM = dict(title="CHEAP ARDUINO KITS 50% OFF!!! Message me", category="STUDIES", author="cheapkits.ph", days=11,
            summary="Best price arduino kits shipping nationwide, message me now!!!", parts="Arduino kit", precautions="", steps="1. Message me")
