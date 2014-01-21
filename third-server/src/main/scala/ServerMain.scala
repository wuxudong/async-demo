import akka.actor.{Actor, Props, ActorSystem}
import akka.io.IO
import java.util.concurrent.TimeUnit
import scala.concurrent.duration.FiniteDuration
import scala.util.Random
import spray.can.Http
import spray.routing.{RequestContext, Route, HttpServiceActor}

/**
 *
 * User: xudong
 * Date: 1/20/14
 * Time: 11:55 AM
 */
object ServerMain extends App {
  implicit val system = ActorSystem()
  val delayMorraHandler = system.actorOf(Props[DelayMorraHandler])
  IO(Http) ! Http.Bind(delayMorraHandler, interface = "0.0.0.0", port = 8888)
}

class DelayMorraHandler extends HttpServiceActor {
  def receive: Actor.Receive = runRoute(route)

  import context.dispatcher

  val r = new Random()

  val route: Route = {
    get {
      path("random") {
        requestContext =>
          context.system.scheduler.scheduleOnce(FiniteDuration(200, TimeUnit.MILLISECONDS)) {
            requestContext.complete(s"${r.nextInt(3)}")
          }
      } ~
        path("add") {
          parameters('p1.as[Int], 'p2.as[Int]) {
            (p1:Int, p2:Int) => {
              requestContext: RequestContext =>
                context.system.scheduler.scheduleOnce(FiniteDuration(200, TimeUnit.MILLISECONDS)) {
                  requestContext.complete(s"${p1 + p2}")
                }
            }
          }
        }

    }
  }
}
