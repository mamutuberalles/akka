/* @main def hello: Unit = 
  println("Hello world!")
  println(msg)

def msg = "I was compiled by Scala 3. :)"
 */
package mamut.deathmatch


import akka.actor.typed.ActorRef
import akka.actor.typed.ActorSystem
import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors
import mamut.deathmatch.KriegerMain.StartApp
import mamut.deathmatch.Krieger.SendDamage
//import akka.actor.testkit.typed.scaladsl.{ LogCapturing, ScalaTestWithActorTestKit }
import akka.actor.typed.{ ActorRef, Behavior, SupervisorStrategy }
import akka.actor.typed.receptionist.{ Receptionist, ServiceKey }
import akka.actor.typed.scaladsl.{ Behaviors, Routers }


object Krieger {

  final case class SendDamage(damage: Integer)

  def apply(): Behavior[SendDamage] = Behaviors.setup { 
      context => 
        var HP = 10
        Behaviors.receiveMessage { message =>
          HP -= message.damage
          context.log.info(s"Damage taken, current hp: ($HP)")
          if(HP <= 0)
          {
            context.log.info(s"Warrior dead with hp: ($HP)")
            Behaviors.stopped
          }
          else
          {
            Behaviors.same
          }
        }
    }

}



object KriegerMain{

  
    final case class StartApp()

   

    def apply(): Behavior[StartApp] = Behaviors.setup { 
      context => 
        

        Behaviors.receiveMessage { message =>
          val pool = Routers.pool(poolSize = 8) {
            Behaviors.supervise(Krieger()).onFailure[Exception](SupervisorStrategy.restart)
          }
          val router = context.spawn(pool, "krieger-pool")

          (0 to 30).foreach { n =>
          //Check if pool is terminated
            try  {router ! Krieger.SendDamage(4)}
            catch {case _: Throwable => context.log.info("Warrior already dead")}
          }


          /* val krieger = context.spawn(Krieger(), "krieger")
          krieger ! SendDamage(4)
          krieger ! SendDamage(4)
          krieger ! SendDamage(4)*/
          Behaviors.empty
        }
      

    }

}

object Main extends App {

  val kriegerMain : ActorSystem[KriegerMain.StartApp] = ActorSystem(KriegerMain(), "Krieger")

  kriegerMain ! StartApp()

}

