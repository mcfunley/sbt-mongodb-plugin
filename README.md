# sbt-mongodb-plugin
Adds support for a MongoDB test fixture database in simple-build-tool.

By Dan McKinley - dan@etsy.com - [http://mcfunley.com](http://mcfunley.com)


## Overview

This plugin will run a MongoDB database before the test run and shut it down afterwards. It handles loading fixture data (as necessary), and can handle migration files. 

mongo, mongod and mongoimport should be on the $PATH for this to work. 


## Simple Example

To use, include in your plugin definition (for the basics of declaring plugins, [see here](http://code.google.com/p/simple-build-tool/wiki/SbtPlugins)) as follows:

<pre>
class Plugins(info : ProjectInfo) extends PluginDefinition(info) {
  val mongodb = "com.etsy" % "sbt-mongodb-plugin" % "latest.integration"
}
</pre>

Then add the MongoDatabase trait to your project:

<pre>
class MyProject(info : ProjectInfo) extends DefaultProject(info) 
  with MongoDatabase {
  
}
</pre>

At this point, MongoDB will automatically start and stop with your test run. To load data, put .json files in the test/mongofixtures directory. These will automatically be loaded, and the test database will be recreated any time the fixtures are changed. 

The naming of the fixture files should be as follows:

<pre>
<collection name>.fixtures.json
</pre>

Migrations work similarly: put .js files to be executed after the fixtures to the migrations directory (in the root of the project). 

By default, the fixture data and migrations are applied to the "test" collection. 

## Customization

The paths used by the plugin can be customized. You can override:

* **mongoDatabasePath** - the path for the generated database.
* **mongoFixturesDirectory** - the path to crawl for .json fixtures.
* **mongoMigrationsDirectory** - that path to crawl for .js migrations.
* **mongoFixturePaths** - a PathFinder; the .json files to load. 
* **mongoMigrationPaths** - a PathFinder; the .js files to run.

You can also customize the database name and the naming of the fixture files. 

* **mongoDatabaseName** - the database name to use. 
* **mongoCollectionName** - a function of type String => String that translates a fixture filename into the collection into which it is loaded.


## Version History

### Version 1.0.0 - 07-16-2010
Initial release
