import play.api._

import models._
import anorm._

object Global extends GlobalSettings {
  
  override def onStart(app: Application) {
    InitialData.insert()
  }
  
}

/**
 * Initial set of data to be imported for testing
 * Default password is 'secret'
 */
object InitialData {
    
  def insert() = {   
    if(User.all.isEmpty) {
      
      Seq(
        User("Ronaldo", "e5e9fa1ba31ecd1ae84f75caaa474f3a663f05f4", "default"),
        User("Maradona", "e5e9fa1ba31ecd1ae84f75caaa474f3a663f05f4", "default"),
        User("Zidane", "e5e9fa1ba31ecd1ae84f75caaa474f3a663f05f4", "default"),
        User("Beckham", "e5e9fa1ba31ecd1ae84f75caaa474f3a663f05f4", "default"),
        User("Pele", "e5e9fa1ba31ecd1ae84f75caaa474f3a663f05f4", "default"),
        User("Messi", "e5e9fa1ba31ecd1ae84f75caaa474f3a663f05f4", "default")
      ).foreach(User.create)      
    }   
  }
  
}