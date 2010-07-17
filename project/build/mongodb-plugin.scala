import sbt._


class MongoDBPlugin(info : ProjectInfo) extends PluginProject(info) {
  override def outputDirectoryName = "build"
  override def mainScalaSourcePath = "src"
  override def mainResourcesPath   = "lib"
  override def testScalaSourcePath = "tests"

  override def managedStyle = ManagedStyle.Ivy
  val publishTo = "Etsy Repo" at "http://ivy.etsycorp.com"
  
  override def packageAction = 
    packageTask(packagePaths, jarPath, packageOptions).
    dependsOn(compile) describedAs BasicScalaProject.PackageDescription
}
