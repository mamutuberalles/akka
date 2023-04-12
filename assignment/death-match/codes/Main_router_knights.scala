package com.deathmatch

import akka.actor.typed.ActorRef
import akka.actor.typed.ActorSystem
import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors
import com.deathmatch.MessageHandler.StartApplication
import scala.util.Random
import com.deathmatch.Knight.Attack
import akka.actor.typed.{ ActorRef, Behavior, SupervisorStrategy }
import akka.actor.typed.receptionist.{ Receptionist, ServiceKey }
import akka.actor.typed.scaladsl.{ Behaviors, Routers }

object Knight {
  final case class Attack(dam: Integer, opponent: ActorRef[Attack])

  def apply(): Behavior[Attack] = Behaviors.setup { context =>
    var HP = 10
    var randomNumberGenerator = scala.util.Random
    var nameLength = randomNumberGenerator.nextInt(10)
    var name = new StringBuilder
    for (i <- 1 to nameLength)
    {
      name.append(randomNumberGenerator.nextPrintableChar())
    }
    Behaviors.receiveMessage { message => 
      
      var damageTreshold = randomNumberGenerator.nextInt(2) + 1
      var damageCaused = message.dam - damageTreshold
      if(damageCaused < 0)
      {
        damageCaused = 0
      }
      var tempopraryHealthPoints = HP - damageCaused
      context.log.info(s"$name took $damageCaused damage, current hp: $tempopraryHealthPoints")

      if(tempopraryHealthPoints <= 0)
      {
        context.log.info(s"$name knight died") 
        HP = tempopraryHealthPoints
        message.opponent ! Knight.Attack(0,context.self)
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

  final case class StartApplication(n: Integer)
  def apply(): Behavior[Knight.Attack] =
    Behaviors.setup { context =>
      val pool = Routers.pool(poolSize = 8) {
        Behaviors.supervise(Knight()).onFailure[Exception](SupervisorStrategy.restart)
      }
      var knights = 8
      val randomPool = pool.withPoolSize(8).withRandomRouting()
      val router = context.spawn(randomPool, "knight-pool")
      Behaviors.receiveMessage { message =>
        if(message.dam == 0)
        {           
            context.log.info(s"$knights knights are still alive" )
            knights -= 1

            if(knights == 0)
            {
              context.log.info("Fight over!")
              Behaviors.stopped
            }
            else {
              router ! Knight.Attack(0,context.self)
              Behaviors.same
            }            
        }
        else
        {         
          router ! Knight.Attack(message.dam, context.self)
          Behaviors.same
        }       
      }
    }  
}



object Main extends App {
  val messageHandler: ActorSystem[Knight.Attack] = ActorSystem(MessageHandler(), "Main")
  messageHandler ! Knight.Attack(0,null)
}

