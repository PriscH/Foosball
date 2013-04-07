import play.api._

import org.joda.time.DateTime

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
      Token.create(Token("initial", "Signup", new DateTime()))
      
      /*
      Seq(
        User("Ronaldo", "ronaldo@mail.com", "e5e9fa1ba31ecd1ae84f75caaa474f3a663f05f4", "brazil"),
        User("Maradona", "maradona@mail.com", "e5e9fa1ba31ecd1ae84f75caaa474f3a663f05f4", "mexico"),
        User("Zidane", "zidane@mail.com", "e5e9fa1ba31ecd1ae84f75caaa474f3a663f05f4", "france"),
        User("Beckham", "beckham@mail.com", "e5e9fa1ba31ecd1ae84f75caaa474f3a663f05f4", "england"),
        User("Pele", "pele@mail.com", "e5e9fa1ba31ecd1ae84f75caaa474f3a663f05f4", "spain"),
        User("Messi", "messi@mail.com", "e5e9fa1ba31ecd1ae84f75caaa474f3a663f05f4", "argentina")
      ).foreach(User.create)
      */
    }   
  }
  
}