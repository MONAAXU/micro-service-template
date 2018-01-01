import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import com.typesafe.scalalogging.{LazyLogging, Logger}
import org.slf4j.LoggerFactory

import scala.io.StdIn



object WebServer extends JsonSupport with AppInfoService with LazyLogging{
  def main(args: Array[String]) {

    implicit val system = ActorSystem("my-system")
    implicit val materializer = ActorMaterializer()

    implicit val executionContext = system.dispatcher

    val route = appInfoServiceRoute

    val bindingFuture = Http().bindAndHandle(route, "localhost", 8080)

    val logger = Logger(LoggerFactory.getLogger("my-logger"))
    logger.info(s"Server online at http://localhost:8080/\nPress RETURN to stop...")

    StdIn.readLine() // let it run until user presses return
    bindingFuture
      .flatMap(_.unbind()) // trigger unbinding from the port
      .onComplete(_ => system.terminate()) // and shutdown when done
  }
}