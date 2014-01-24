import akka.actor.{ActorRef, Actor, Props, ActorSystem}
import akka.io.IO
import akka.util.Timeout
import java.util.concurrent.TimeUnit
import spray.http.HttpResponse
import spray.httpx.RequestBuilding._
import spray.can.Http
import spray.routing._
import scala.concurrent.Future
import akka.pattern.ask

/**
 *
 * User: xudong
 * Date: 1/20/14
 * Time: 5:02 PM
 */
object DemoMain extends App {
  implicit val system = ActorSystem()
  val io = IO(Http)
  val demoHandler = system.actorOf(Props(classOf[DemoHandler], io))
  IO(Http) ! Http.Bind(demoHandler, interface = "0.0.0.0", port = 9999)
}

class DemoHandler(io: ActorRef) extends HttpServiceActor {
  def receive: Actor.Receive = runRoute(route)

  implicit val timeout = Timeout(3, TimeUnit.SECONDS)

  import context.dispatcher

  val route: Route = {
    get {
      path("sum-demo") {
        requestContext =>
          val start = System.currentTimeMillis()
          for {
            res1 <- (io ? Get("http://127.0.0.1:8888/random")).mapTo[HttpResponse]
            p1 = res1.entity.asString.toInt
            res2 <- (io ? Get("http://127.0.0.1:8888/random")).mapTo[HttpResponse]
            p2 = res2.entity.asString.toInt
            res3 <- (io ? Get(s"http://127.0.0.1:8888/add?p1=${p1}&p2=${p2}")).mapTo[HttpResponse]
            sum = res3.entity.asString.toInt
          } {
            val after = System.currentTimeMillis()
            println(s"sum  times spent ${after - start}")
            requestContext.complete(s"${p1} + ${p2} = ${sum}")
          }
      } ~
        path("morra-demo") {
          parameterMultiMap {
            params =>
              requestContext =>
                val start = System.currentTimeMillis()

                val uids = params("uids")
                val futures = uids map {
                  uid =>
                    (io ? Get(s"http://127.0.0.1:8888/random?uid=${uid}")).mapTo[HttpResponse] map (res => uid -> res.entity.asString.toInt)
                }

                Future.reduce(futures) {
                  (max, x) => if (x._2 > max._2) x else max
                }.onSuccess {
                  case (winner, guess) =>
                    val after = System.currentTimeMillis()
                    println(s"morra ${uids.size} times spent ${after - start}")

                    requestContext.complete(s"${winner} ${guess}")
                }
          }
        }
    }
  }
}