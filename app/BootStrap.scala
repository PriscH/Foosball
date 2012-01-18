import play.jobs._
import play.Play
import play.test._

import models._

@OnApplicationStart class BootStrap extends Job {

  override def doJob {
    // Import initial data if the database is empty
    if (Play.configuration.get("application.mode") == "dev" && User.count().single() == 0) {
      Yaml[List[Any]]("initial-data.yml").foreach {
        _ match {
          case user: User => User.create(user)
        }
      }
    }
  }

}