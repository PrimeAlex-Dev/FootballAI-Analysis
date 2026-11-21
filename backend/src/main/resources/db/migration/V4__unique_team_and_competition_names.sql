-- Existing duplicates require explicit resolution; do not delete or merge records.
CREATE UNIQUE INDEX uq_teams_name_ignore_case ON teams (lower(name));
CREATE UNIQUE INDEX uq_competitions_name_ignore_case ON competitions (lower(name));
