-- ============================================================
-- L2J Mobius CT 2.6 HighFive -> Salvation 140 (v2.0)
-- Community Board scheme buffer storage (SchemeBufferTable)
-- Run on the game database before starting the server.
-- ============================================================

CREATE TABLE IF NOT EXISTS `buffer_schemes` (
  `object_id` INT UNSIGNED NOT NULL DEFAULT 0,
  `scheme_name` VARCHAR(32) NOT NULL DEFAULT '',
  `skills` VARCHAR(2000) NOT NULL DEFAULT '',
  PRIMARY KEY (`object_id`, `scheme_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
