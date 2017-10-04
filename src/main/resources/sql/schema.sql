-- url pattern
DROP TABLE IF EXISTS `url_pattern`;
CREATE TABLE `url_pattern` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
    `url` VARCHAR(100) UNIQUE,
    `project` VARCHAR(50)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

