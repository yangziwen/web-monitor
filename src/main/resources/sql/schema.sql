-- url pattern
DROP TABLE IF EXISTS `url_pattern`;
CREATE TABLE `url_pattern` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
    `url` VARCHAR(100) UNIQUE,
    `project` VARCHAR(50),
    unique key `url`(`url`),
    key `project`(`project`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


DROP TABLE IF EXISTS `url_metrics_result`;
CREATE TABLE `url_metrics_result` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
    `url` VARCHAR(100),
    `cnt` BIGINT DEFAULT '0',
    `error_cnt` BIGINT DEFAULT '0',
    `sum` BIGINT DEFAULT '0',
    `max` INT DEFAULT '0',
    `min` INT DEFAULT '0',
    `distribution` VARCHAR(300),
    `begin_time` DATETIME,
    `end_time` DATETIME,
    KEY idx_begin_end_time(`begin_time`, `end_time`),
    KEY idx_end_begin_time(`end_time`, `begin_time`),
    KEY idx_url(`url`)
) engine=InnoDB DEFAULT CHARSET=utf8;

