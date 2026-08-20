ALTER TABLE visit
    ADD COLUMN IF NOT EXISTS ai_summary_coin_charged boolean NOT NULL DEFAULT false;
