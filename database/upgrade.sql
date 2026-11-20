-- =====================================================================
-- upgrade.sql : adds step photos, schematics, video links and separate
-- votes and comments to an existing cas_db. Keeps every row you have.
-- Run it once in the TiDB SQL Editor (or MySQL Workbench as the root user).
-- =====================================================================
USE cas_db;

-- GUIDES: a schematic picture, one photo per step, and a video link
-- (one column per statement: TiDB can't use a column added in the same statement)
ALTER TABLE guides ADD COLUMN schematic   MEDIUMBLOB   NULL AFTER image;
ALTER TABLE guides ADD COLUMN step_images LONGBLOB     NULL AFTER schematic;
ALTER TABLE guides ADD COLUMN video_url   VARCHAR(500) NULL AFTER step_images;

-- REVIEWS: the vote becomes optional, so people can comment without voting
ALTER TABLE reviews MODIFY COLUMN is_approved BOOLEAN NULL;

-- Two sample comments without a vote (skip this part if you don't want them)
INSERT IGNORE INTO reviews (guide_id, user_id, is_approved, comment)
SELECT 5, user_id, NULL, 'Use a relay rated for 10A at 230V and add a fuse on the input.'
FROM users WHERE username = 'prof_sample';
INSERT IGNORE INTO reviews (guide_id, user_id, is_approved, comment)
SELECT 1, user_id, NULL, 'Thanks! I added the pump model to step 2.'
FROM users WHERE username = 'hobby_test';
