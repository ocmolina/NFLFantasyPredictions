CREATE DATABASE  IF NOT EXISTS `nfl` /*!40100 DEFAULT CHARACTER SET latin1 */;
USE `nfl`;
-- MySQL dump 10.13  Distrib 5.7.9, for Win64 (x86_64)
--
-- Host: localhost    Database: nfl
-- ------------------------------------------------------
-- Server version	5.5.5-10.1.10-MariaDB

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `all_teams`
--

DROP TABLE IF EXISTS `all_teams`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `all_teams` (
  `id` int(11) NOT NULL,
  `team` varchar(4) NOT NULL,
  `city` varchar(45) DEFAULT NULL,
  `name` varchar(45) DEFAULT NULL,
  `conference` varchar(3) DEFAULT NULL,
  `division` varchar(45) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `id_UNIQUE` (`id`),
  UNIQUE KEY `team_UNIQUE` (`team`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `all_teams`
--

LOCK TABLES `all_teams` WRITE;
/*!40000 ALTER TABLE `all_teams` DISABLE KEYS */;
INSERT INTO `all_teams` VALUES (1,'ARI','Arizona','Cardinals','NFC','NFC_West\r'),(2,'ATL','Atlanta','Falcons','NFC','NFC_South\r'),(3,'BAL','Baltimore','Ravens','AFC','AFC_North\r'),(4,'BUF','Buffalo','Bills','AFC','AFC_East\r'),(5,'CAR','Carolina','Panthers','NFC','NFC_South\r'),(6,'CHI','Chicago','Bears','NFC','NFC_North\r'),(7,'CIN','Cincinnati','Bengals','AFC','AFC_North\r'),(8,'CLE','Cleveland','Browns','AFC','AFC_North\r'),(9,'DAL','Dallas','Cowboys','NFC','NFC_East\r'),(10,'DEN','Denver','Broncos','AFC','AFC_West\r'),(11,'DET','Detroit','Lions','NFC','NFC_North\r'),(12,'GB','Green Bay','Packers','NFC','NFC_North\r'),(13,'HOU','Houston','Texans','AFC','AFC_South\r'),(14,'IND','Indianapolis','Colts','AFC','AFC_South\r'),(15,'JAC','Jacksonville','Jaguars','AFC','AFC_South\r'),(16,'KC','Kansas City','Chiefs','AFC','AFC_West\r'),(17,'MIA','Miami','Dolphins','AFC','AFC_East\r'),(18,'MIN','Minnesota','Vikings','NFC','NFC_North\r'),(19,'NE','New England','Patriots','AFC','AFC_East\r'),(20,'NO','New Orleans','Saints','NFC','NFC_South\r'),(21,'NYG','New York','Giants','NFC','NFC_East\r'),(22,'NYJ','New York','Jets','AFC','AFC_East\r'),(23,'OAK','Oakland','Raiders','AFC','AFC_West\r'),(24,'PHI','Philadelphia','Eagles','NFC','NFC_East\r'),(25,'PIT','Pittsburgh','Steelers','AFC','AFC_North\r'),(26,'SD','San Diego','Chargers','AFC','AFC_West\r'),(27,'SEA','Seattle','Seahawks','NFC','NFC_West\r'),(28,'SF','San Francisco','49ers','NFC','NFC_West\r'),(29,'STL','St. Louis','Rams','NFC','NFC_West\r'),(30,'TB','Tampa Bay','Buccaneers','NFC','NFC_South\r'),(31,'TEN','Tennessee','Titans','AFC','AFC_South\r'),(32,'WAS','Washington','Redskins','NFC','NFC_East\r');
/*!40000 ALTER TABLE `all_teams` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2016-09-14 15:01:55
