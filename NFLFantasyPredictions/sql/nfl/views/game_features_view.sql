CREATE 
    ALGORITHM = UNDEFINED 
    DEFINER = `root`@`localhost` 
    SQL SECURITY DEFINER
VIEW `game_features` AS
    SELECT 
        `games`.`season` AS `season`,
        `games`.`wk` AS `wk`,
        `details`.`tname` AS `team`,
        (CASE
            WHEN (`details`.`tname` = `games`.`v`) THEN `games`.`h`
            WHEN (`details`.`tname` = `games`.`h`) THEN `games`.`v`
        END) AS `opponent`,
        `details`.`ry` AS `rush_yds`,
        (SELECT 
                `game_details`.`ry`
            FROM
                `game_details`
            WHERE
                ((`game_details`.`gid` = `games`.`gid`)
                    AND (`game_details`.`tname` <> `details`.`tname`))) AS `rush_yds_against`,
        `details`.`py` AS `pass_yds`,
        (SELECT 
                `game_details`.`py`
            FROM
                `game_details`
            WHERE
                ((`game_details`.`gid` = `games`.`gid`)
                    AND (`game_details`.`tname` <> `details`.`tname`))) AS `pass_yds_against`,
        (SELECT 
                `game_details`.`sk`
            FROM
                `game_details`
            WHERE
                ((`game_details`.`gid` = `games`.`gid`)
                    AND (`game_details`.`tname` <> `details`.`tname`))) AS `sacks_by_defense`,
        `details`.`ints` AS `ints_by_defense`,
        (SELECT 
                `game_details`.`fum`
            FROM
                `game_details`
            WHERE
                ((`game_details`.`gid` = `games`.`gid`)
                    AND (`game_details`.`tname` <> `details`.`tname`))) AS `fumbles_by_defense`,
        `details`.`pts` AS `points`,
        (CASE
            WHEN (`details`.`tname` = `games`.`v`) THEN 1
            WHEN (`details`.`tname` = `games`.`h`) THEN 0
        END) AS `played_at`,
        (CASE
            WHEN
                ((`details`.`tname` = `games`.`v`)
                    AND (`games`.`ptsv` > `games`.`ptsh`))
            THEN
                1
            WHEN
                ((`details`.`tname` = `games`.`v`)
                    AND (`games`.`ptsv` <= `games`.`ptsh`))
            THEN
                0
            WHEN
                ((`details`.`tname` = `games`.`h`)
                    AND (`games`.`ptsh` > `games`.`ptsv`))
            THEN
                1
            WHEN
                ((`details`.`tname` = `games`.`h`)
                    AND (`games`.`ptsh` <= `games`.`ptsv`))
            THEN
                0
        END) AS `result`
    FROM
        (`games`
        JOIN `game_details` `details` ON ((`games`.`gid` = `details`.`gid`)))