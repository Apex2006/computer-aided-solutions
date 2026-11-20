-- remove_sample_data.sql : removes every sample account and everything they made
-- (their guides, votes, comments and reports). Your own data is not touched.

USE cas_db;

DELETE FROM guides WHERE author_id IN (SELECT user_id FROM users WHERE email LIKE '%@users.cas.local');
DELETE FROM users WHERE email LIKE '%@users.cas.local';
