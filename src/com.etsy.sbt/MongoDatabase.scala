package com.etsy.sbt

import java.io.File
import _root_.sbt._
import Process._
import scala.actors.Actor
import scala.actors.Actor._


trait MongoDatabase extends BasicScalaProject {
  self : MavenStyleScalaPaths =>

  def mongoDatabasePath = testScalaSourcePath / ".mongodb"
  def mongoFixturesDirectory = testScalaSourcePath / "mongofixtures"
  def mongoMigrationsDirectory = "." / "migrations"
  def mongoDatabaseName = "tests"

  private val FixtureFile = """([^.]*)\.([^.]*)\.fixtures.json""".r

  def mongoCollectionName(basename : String) : (String, String) = basename match {
    case FixtureFile(dbname, name) => (dbname, name)
  }

  lazy val mongoFixturePaths = mongoFixturesDirectory ** "*.json"

  lazy val mongoMigrationPaths = mongoMigrationsDirectory ** "*.js"

  lazy val startMongo = task { startMongoAction ; None } describedAs (
    "Starts a MongoDB database for test fixtures.")

  lazy val stopMongo = task { stopMongoAction ; None } describedAs (
    "Stops the MongoDB test database.")

  lazy val cleanMongo = task { cleanMongoAction } describedAs (
    "Destroys the MongoDB test database.")

  def startMongoAction = { 
    killMongo
    if (mongoDatabaseMissing) {      
      createMongoDB
    } else if (mongoFixturesNewer) {
      log.info("Mongo fixtures newer than database, recreating.")
      createMongoDB
    } else if (mongoMigrationsNewer) {
      log.info("Mongo migrations newer than database, recreating.")
      createMongoDB
    } else {
      log.info("Mongo test db exists.")
      startMongoProcess
    }
  }

  def stopMongoAction = killMongo

  def cleanMongoAction = FileUtilities.clean(mongoDatabasePath, log)

  private def runMigrations =
    ("mongo etsy %s".format(mongoMigrationPaths.getPaths.reduceLeft(_+" "+_))) ! 

  private def loadFixtures = mongoFixturePaths.getPaths.foreach { p =>
    val (dbname, cn) = mongoCollectionName((new File(p)).getName)
    ("mongoimport -c %s -d %s --drop %s".format(cn, dbname, p)) ! 
  }

  private lazy val fileNewerThanDB = new SimpleFileFilter(
    Path.fromFile(_) newerThan mongoDatabasePath)

  private def mongoFixturesNewer = anyNewerThanDB(mongoFixturePaths)

  private def mongoMigrationsNewer = anyNewerThanDB(mongoMigrationPaths)

  private def anyNewerThanDB(paths : PathFinder) = 
    !(paths ** fileNewerThanDB).getPaths.isEmpty

  private def mongoDatabaseMissing = !mongoDatabasePath.isDirectory

  private def makeMongoDir = ("mkdir " + mongoDatabasePath) !

  private def createMongoDB = {
    cleanMongoAction
    makeMongoDir
    startMongoProcess
    loadFixtures
    runMigrations
  }

  private case object Start

  private lazy val mongoProcess = actor { 
    loop {
      receive {
        case Start => { 
          ("mongod --fork --nohttpinterface " +
           "--logpath mongo.log --dbpath " + mongoDatabasePath) !

          log.info("MongoDB daemon terminated.")
        }
      }
    }
  }


  private def startMongoProcess = {
    log.info("Starting MongoDB")
    mongoProcess ! Start
    Thread.sleep(1000)
  }

  private def killMongo = "killall mongod" ! 

  override def cleanAction = super.cleanAction dependsOn (cleanMongo)

  override def testListeners = super.testListeners ++ Seq(new TestsListener {
    def doInit = startMongoAction
    def doComplete(finalResult : Result.Value) = stopMongoAction
    def startGroup(name: String) {}
    def testEvent(event: TestEvent) {}
    def endGroup(name: String, t: Throwable) {}
    def endGroup(name: String, result: Result.Value) {}
  })

}
