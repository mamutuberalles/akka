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


object SwordsMan {
  final case class Thrust(dam: Integer, replyTo: ActorRef[Swing], handler: ActorRef[StartApplication])
  final case class Swing(dam: Integer, from: ActorRef[Thrust], handler: ActorRef[StartApplication])
  

  def apply(): Behavior[Thrust] = Behaviors.setup{ context => // Code runs when warrior is created
    var HP = 10
    Behaviors.receiveMessage { message => // Code runs when Thrust message is recieved

      var rand =  scala.util.Random  // Calculating received damage
      var DamageTreshold = rand.nextInt(2) + 1
      var damage = message.dam - DamageTreshold
      var temp = HP - damage

      context.log.info(s"Damage taken, current hp: ($temp)")

      if(temp <= 0)
      {
        context.log.info(s"Warrior swordsMan is dead")       // Dead man branch
        Behaviors.stopped
      }
      else
      {
        HP = temp
        var damageToInflict = rand.nextInt(5) + 5 // Fightng branch
        message.replyTo ! Swing(damageToInflict,context.self,message.handler) // Sending the SpearMan the message back
        Behaviors.same
      }
    }
  }
}



object SpearMan {

  /*final case class Thrust(dam: Integer, replyTo: ActorRef[Swing], handler: ActorRef[Die])
  final case class Swing(dam: Integer, from: ActorRef[Thrust], handler: ActorRef[Die])*/
  

  def apply(): Behavior[Swing] = Behaviors.setup{ context => // Code runs when warrior is created
    var HP = 10
    Behaviors.receiveMessage { message => // Code runs when Thrust message is recieved

      var rand =  scala.util.Random  // Calculating received damage
      var DamageTreshold = rand.nextInt(2) + 1
      var damage = message.dam - DamageTreshold
      var temp = HP - damage

      context.log.info(s"Damage taken, current hp: ($temp)")

      if(temp <= 0)
      {
        context.log.info(s"Warrior spearMan is dead")       // Dead man branch
        Behaviors.stopped
      }
      else
      {
        HP = temp
        var damageToInflict = rand.nextInt(5) + 5 // Fightng branch
        message.from ! Thrust(damageToInflict,context.self,message.handler) // Sending the SpearMan the message back
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
        val swordsMan = context.spawn(SwordsMan(), "swordsMan") // Creating the first warrior
        val spearMan = context.spawn(SpearMan(), "spearMan") // Defining the second warrior
        
        var rand =  scala.util.Random 
        var damage = rand.nextInt(5) + 1 // Calculating arbitrary damage

        swordsMan ! SwordsMan.Thrust(damage, spearMan, context.self) // Sending the swordsman the damage, with the Thrust message, with spearMan reference
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

