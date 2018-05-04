CREATE 
    ALGORITHM = UNDEFINED 
    DEFINER = `root`@`localhost` 
    SQL SECURITY DEFINER
VIEW `wr_fantasy_features` AS
    SELECT 
        `g`.`season` AS `season`,
        `g`.`wk` AS `wk`,
        `of`.`team` AS `team`,
        `of`.`player` AS `player`,
        `p`.`pname` AS `pname`,
        `p`.`pos1` AS `pos1`,
        `of`.`recy` AS `recy`,
        `of`.`tdrec` AS `tdrec`,
        `of`.`fp` AS `fp`,
        (SELECT 
                COALESCE(AVG(`offense`.`recy`), 0)
            FROM
                (`offense_fantasy` `offense`
                JOIN `games` ON ((`offense`.`gid` = `games`.`gid`)))
            WHERE
                ((`offense`.`player` = `of`.`player`)
                    AND (`games`.`season` = `g`.`season`)
                    AND (`games`.`wk` < `g`.`wk`)
                    AND (`games`.`wk` >= (`g`.`wk` - 3)))) AS `last_3_games_avg_player_recy`,
        (SELECT 
                COALESCE(AVG(`offense`.`tdrec`), 0)
            FROM
                (`offense_fantasy` `offense`
                JOIN `games` ON ((`offense`.`gid` = `games`.`gid`)))
            WHERE
                ((`offense`.`player` = `of`.`player`)
                    AND (`games`.`season` = `g`.`season`)
                    AND (`games`.`wk` < `g`.`wk`)
                    AND (`games`.`wk` >= (`g`.`wk` - 3)))) AS `last_3_games_avg_player_tdrec`,
        (CASE
            WHEN
                (`g`.`v` = `of`.`team`)
            THEN
                COALESCE((SELECT 
                                AVG(`features`.`pass_yds`)
                            FROM
                                `features`
                            WHERE
                                ((`features`.`season` = `g`.`season`)
                                    AND (`features`.`wk` < `g`.`wk`)
                                    AND (`features`.`team` = `g`.`v`))),
                        0)
            WHEN
                (`g`.`h` = `of`.`team`)
            THEN
                COALESCE((SELECT 
                                AVG(`features`.`pass_yds`)
                            FROM
                                `features`
                            WHERE
                                ((`features`.`season` = `g`.`season`)
                                    AND (`features`.`wk` < `g`.`wk`)
                                    AND (`features`.`team` = `g`.`h`))),
                        0)
        END) AS `team_avg_pass_yds_to_wk`,
        (CASE
            WHEN
                (`g`.`v` = `of`.`team`)
            THEN
                COALESCE((SELECT 
                                AVG(`features`.`rush_yds`)
                            FROM
                                `features`
                            WHERE
                                ((`features`.`season` = `g`.`season`)
                                    AND (`features`.`wk` < `g`.`wk`)
                                    AND (`features`.`team` = `g`.`v`))),
                        0)
            WHEN
                (`g`.`h` = `of`.`team`)
            THEN
                COALESCE((SELECT 
                                AVG(`features`.`rush_yds`)
                            FROM
                                `features`
                            WHERE
                                ((`features`.`season` = `g`.`season`)
                                    AND (`features`.`wk` < `g`.`wk`)
                                    AND (`features`.`team` = `g`.`h`))),
                        0)
        END) AS `team_avg_rush_yds_to_wk`,
        (CASE
            WHEN
                (`g`.`v` = `of`.`team`)
            THEN
                COALESCE((SELECT 
                                AVG(`features`.`points`)
                            FROM
                                `features`
                            WHERE
                                ((`features`.`season` = `g`.`season`)
                                    AND (`features`.`wk` < `g`.`wk`)
                                    AND (`features`.`team` = `g`.`v`))),
                        0)
            WHEN
                (`g`.`h` = `of`.`team`)
            THEN
                COALESCE((SELECT 
                                AVG(`features`.`points`)
                            FROM
                                `features`
                            WHERE
                                ((`features`.`season` = `g`.`season`)
                                    AND (`features`.`wk` < `g`.`wk`)
                                    AND (`features`.`team` = `g`.`h`))),
                        0)
        END) AS `team_avg_pts_to_wk`,
        (CASE
            WHEN
                (`g`.`v` = `of`.`team`)
            THEN
                COALESCE((SELECT 
                                AVG(`features`.`possession_time`)
                            FROM
                                `features`
                            WHERE
                                ((`features`.`season` = `g`.`season`)
                                    AND (`features`.`wk` < `g`.`wk`)
                                    AND (`features`.`team` = `g`.`v`))),
                        0)
            WHEN
                (`g`.`h` = `of`.`team`)
            THEN
                COALESCE((SELECT 
                                AVG(`features`.`possession_time`)
                            FROM
                                `features`
                            WHERE
                                ((`features`.`season` = `g`.`season`)
                                    AND (`features`.`wk` < `g`.`wk`)
                                    AND (`features`.`team` = `g`.`h`))),
                        0)
        END) AS `team_avg_pos_time_to_wk`,
        (CASE
            WHEN (`g`.`v` = `of`.`team`) THEN `g`.`h`
            WHEN (`g`.`h` = `of`.`team`) THEN `g`.`v`
        END) AS `defense`,
        (CASE
            WHEN
                (`g`.`v` = `of`.`team`)
            THEN
                COALESCE((SELECT 
                                AVG(`features`.`pass_yds_against`)
                            FROM
                                `features`
                            WHERE
                                ((`features`.`season` = `g`.`season`)
                                    AND (`features`.`wk` < `g`.`wk`)
                                    AND (`features`.`team` = `g`.`h`))),
                        0)
            WHEN
                (`g`.`h` = `of`.`team`)
            THEN
                COALESCE((SELECT 
                                AVG(`features`.`pass_yds_against`)
                            FROM
                                `features`
                            WHERE
                                ((`features`.`season` = `g`.`season`)
                                    AND (`features`.`wk` < `g`.`wk`)
                                    AND (`features`.`team` = `g`.`v`))),
                        0)
        END) AS `defense_accepted_avg_pass_yds_to_wk`,
        (CASE
            WHEN
                (`g`.`v` = `of`.`team`)
            THEN
                COALESCE((SELECT 
                                AVG(`features`.`rush_yds_against`)
                            FROM
                                `features`
                            WHERE
                                ((`features`.`season` = `g`.`season`)
                                    AND (`features`.`wk` < `g`.`wk`)
                                    AND (`features`.`team` = `g`.`h`))),
                        0)
            WHEN
                (`g`.`h` = `of`.`team`)
            THEN
                COALESCE((SELECT 
                                AVG(`features`.`rush_yds_against`)
                            FROM
                                `features`
                            WHERE
                                ((`features`.`season` = `g`.`season`)
                                    AND (`features`.`wk` < `g`.`wk`)
                                    AND (`features`.`team` = `g`.`v`))),
                        0)
        END) AS `defense_accepted_rush_avg_yds_to_wk`,
        (CASE
            WHEN
                (`g`.`v` = `of`.`team`)
            THEN
                COALESCE((SELECT 
                                AVG(`features`.`points`)
                            FROM
                                `features`
                            WHERE
                                ((`features`.`season` = `g`.`season`)
                                    AND (`features`.`wk` < `g`.`wk`)
                                    AND (`features`.`team` = `g`.`h`))),
                        0)
            WHEN
                (`g`.`h` = `of`.`team`)
            THEN
                COALESCE((SELECT 
                                AVG(`features`.`points`)
                            FROM
                                `features`
                            WHERE
                                ((`features`.`season` = `g`.`season`)
                                    AND (`features`.`wk` < `g`.`wk`)
                                    AND (`features`.`team` = `g`.`v`))),
                        0)
        END) AS `defense_accepted_avg_pts_to_wk`,
        (CASE
            WHEN
                (`g`.`v` = `of`.`team`)
            THEN
                COALESCE((SELECT 
                                AVG(`features`.`possession_time`)
                            FROM
                                `features`
                            WHERE
                                ((`features`.`season` = `g`.`season`)
                                    AND (`features`.`wk` < `g`.`wk`)
                                    AND (`features`.`team` = `g`.`h`))),
                        0)
            WHEN
                (`g`.`h` = `of`.`team`)
            THEN
                COALESCE((SELECT 
                                AVG(`features`.`possession_time`)
                            FROM
                                `features`
                            WHERE
                                ((`features`.`season` = `g`.`season`)
                                    AND (`features`.`wk` < `g`.`wk`)
                                    AND (`features`.`team` = `g`.`v`))),
                        0)
        END) AS `defense_avg_pos_time_to_wk`,
        (CASE
            WHEN
                (`g`.`v` = `of`.`team`)
            THEN
                COALESCE((SELECT 
                                AVG(`features`.`sacks_by_defense`)
                            FROM
                                `features`
                            WHERE
                                ((`features`.`season` = `g`.`season`)
                                    AND (`features`.`wk` < `g`.`wk`)
                                    AND (`features`.`team` = `g`.`h`))),
                        0)
            WHEN
                (`g`.`h` = `of`.`team`)
            THEN
                COALESCE((SELECT 
                                AVG(`features`.`sacks_by_defense`)
                            FROM
                                `features`
                            WHERE
                                ((`features`.`season` = `g`.`season`)
                                    AND (`features`.`wk` < `g`.`wk`)
                                    AND (`features`.`team` = `g`.`v`))),
                        0)
        END) AS `total_sacks_by_defense_to_wk`,
        (CASE
            WHEN
                (`g`.`v` = `of`.`team`)
            THEN
                COALESCE((SELECT 
                                AVG(`features`.`ints_by_defense`)
                            FROM
                                `features`
                            WHERE
                                ((`features`.`season` = `g`.`season`)
                                    AND (`features`.`wk` < `g`.`wk`)
                                    AND (`features`.`team` = `g`.`h`))),
                        0)
            WHEN
                (`g`.`h` = `of`.`team`)
            THEN
                COALESCE((SELECT 
                                AVG(`features`.`ints_by_defense`)
                            FROM
                                `features`
                            WHERE
                                ((`features`.`season` = `g`.`season`)
                                    AND (`features`.`wk` < `g`.`wk`)
                                    AND (`features`.`team` = `g`.`v`))),
                        0)
        END) AS `total_ints_by_defense_to_wk`,
        (CASE
            WHEN
                (`g`.`v` = `of`.`team`)
            THEN
                COALESCE((SELECT 
                                AVG(`features`.`fumbles_by_defense`)
                            FROM
                                `features`
                            WHERE
                                ((`features`.`season` = `g`.`season`)
                                    AND (`features`.`wk` < `g`.`wk`)
                                    AND (`features`.`team` = `g`.`h`))),
                        0)
            WHEN
                (`g`.`h` = `of`.`team`)
            THEN
                COALESCE((SELECT 
                                AVG(`features`.`fumbles_by_defense`)
                            FROM
                                `features`
                            WHERE
                                ((`features`.`season` = `g`.`season`)
                                    AND (`features`.`wk` < `g`.`wk`)
                                    AND (`features`.`team` = `g`.`v`))),
                        0)
        END) AS `total_fumbles_by_defense_to_wk`
    FROM
        ((`offense_fantasy` `of`
        JOIN `games` `g` ON ((`of`.`gid` = `g`.`gid`)))
        JOIN `player` `p` ON (((`of`.`player` = `p`.`player`)
            AND (`p`.`cteam` <> 'INA'))))
    WHERE
        (`p`.`pos1` = 'WR')