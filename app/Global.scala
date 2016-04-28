import play.api._
import org.joda.time.DateTime
import models._
import anorm._

object Global extends GlobalSettings {
  
  override def onStart(app: Application) {
    // Create the initial token if no users exist
    if(User.all.isEmpty && Token.all.isEmpty) {    
      Token.create(Token(Token.InitialToken, "", Token.Scope.Signup, new DateTime()))
    }

    // Load testing data if the deployment mode is not Production
    if (Play.current.configuration.getString("deployment.mode").get != "Production") {
      TestingData.insert()
    }
  }
}

/**
 * Initial set of data to be imported for testing
 * Default password is 'secret'
 */
object TestingData {
    
  def insert() = {
    if (User.all.isEmpty) {
      Seq(
        User("Ronaldo", "ronaldo@mail.com", "e5e9fa1ba31ecd1ae84f75caaa474f3a663f05f4", "Bowser"),
        User("Maradona", "maradona@mail.com", "e5e9fa1ba31ecd1ae84f75caaa474f3a663f05f4", "CaptainFalcon"),
        User("Zidane", "zidane@mail.com", "e5e9fa1ba31ecd1ae84f75caaa474f3a663f05f4", "Kratos"),
        User("Beckham", "beckham@mail.com", "e5e9fa1ba31ecd1ae84f75caaa474f3a663f05f4", "Mario"),
        User("Pele", "pele@mail.com", "e5e9fa1ba31ecd1ae84f75caaa474f3a663f05f4", "SolidSnake"),
        User("Messi", "messi@mail.com", "e5e9fa1ba31ecd1ae84f75caaa474f3a663f05f4", "Villian"),
        User("Neymar", "neymar@mail.com", "e5e9fa1ba31ecd1ae84f75caaa474f3a663f05f4", "Alien"),
        User("Rooney", "rooney@mail.com", "e5e9fa1ba31ecd1ae84f75caaa474f3a663f05f4", "Assassin"),
        User("Ronaldinho", "ronaldinho@mail.com", "e5e9fa1ba31ecd1ae84f75caaa474f3a663f05f4", "Bandit"),
        User("Xavi", "xavi@mail.com", "e5e9fa1ba31ecd1ae84f75caaa474f3a663f05f4", "ChunLi"),
        User("van Persie", "vanpersie@mail.com", "e5e9fa1ba31ecd1ae84f75caaa474f3a663f05f4", "Cloud"),
        User("Henry", "henry@mail.com", "e5e9fa1ba31ecd1ae84f75caaa474f3a663f05f4", "Crash"),
        User("Suarez", "suarez@mail.com", "e5e9fa1ba31ecd1ae84f75caaa474f3a663f05f4", "DonkeyKong"),
        User("Muller", "muller@mail.com", "e5e9fa1ba31ecd1ae84f75caaa474f3a663f05f4", "Dragonborn"),
        User("Robben", "robben@mail.com", "e5e9fa1ba31ecd1ae84f75caaa474f3a663f05f4", "Drake"),
        User("Dr Khumalo", "khumalo@mail.com", "e5e9fa1ba31ecd1ae84f75caaa474f3a663f05f4", "Droplet")
      ).foreach(User.create)
    }

    if (!Token.findByValue("test").isDefined) {
      Token.create(Token("test", "Rooney", Token.Scope.Signup, new DateTime()))
    }
  } 
}