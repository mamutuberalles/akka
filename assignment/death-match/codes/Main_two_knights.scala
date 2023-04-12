//#full-example
package com.deathmatch


import akka.actor.typed.ActorRef
import akka.actor.typed.ActorSystem
import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors
import com.deathmatch.MessageHandler.StartApplication
import scala.util.Random
//import com.deathmatch.MessageHandler.Die
import com.deathmatch.SwordsMan.Thrust
import com.deathmatch.SwordsMan.Swing
import com.deathmatch.Knight.Attack


object Knight {
  final case class Attack(dam: Integer, opponent: ActorRef[Attack])

  def apply(): Behavior[Attack] = Behaviors.setup { context =>
    var HP = 10
    var randomNumberGenerator = scala.util.Random
    var name = randomNumberGenerator.nextString(randomNumberGenerator.nextInt(10))
    Behaviors.receiveMessage { message => 
      
      var damageTreshold = randomNumberGenerator.nextInt(2) + 1
      var damageCaused = message.dam - damageTreshold
      var tempopraryHealthPoints = HP - damageCaused
      context.log.info(s"($name) took {$damageCaused} damage, current hp {$tempopraryHealthPoints}")

      if(tempopraryHealthPoints <= 0)
      {
        context.log.info(s"{$name} knight died") 
        Behaviors.stopped
      }
      else
      {
        HP = tempopraryHealthPoints
        var damageToInflict = randomNumberGenerator.nextInt(5) + 5 
        message.opponent ! Attack(damageToInflict,context.self)
        Behaviors.same
      }
    }
  }

}

object MessageHandler {

  final case class StartApplication(n: Integer) // Defined the start signal


  def apply(): Behavior[StartApplication] =
    Behaviors.setup { context => //Defining the behavior of the MessageHandler when starting the node
      Behaviors.receiveMessage { message => // When the StartApplication message is read, this is the code that runs
        if(message.n == 0)
        {
          Behaviors.stopped
        }
        val knight1 = context.spawn(Knight(), "knight1") // Creating the first warrior
        val knight2 = context.spawn(Knight(), "knight2") // Defining the second warrior

        //val pool = pool.withPoolSize(8).withRandomRouting()
        
        var rand =  scala.util.Random 
        var damage = rand.nextInt(5) + 1 // Calculating arbitrary starting damage

        knight1 ! Knight.Attack(damage, knight2) // Sending the swordsman the damage, with the Thrust message, with spearMan reference
        Behaviors.same
      }
    }

  
}



object Main extends App {
  //Build the actor system
  val messageHandler: ActorSystem[MessageHandler.StartApplication] = ActorSystem(MessageHandler(), "Main")
  // Start the simulation by sending the messageHandler the start signal
  messageHandler ! StartApplication(2)
  
}

