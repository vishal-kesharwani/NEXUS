-- Adds support for real Google Meet links via Google Calendar API integration
 
ALTER TABLE meetings
    ADD COLUMN google_event_id VARCHAR(255);
 
ALTER TABLE users
    ADD COLUMN google_access_token VARCHAR(2048),
    ADD COLUMN google_refresh_token VARCHAR(2048),
    ADD COLUMN google_token_expiry TIMESTAMP;
 