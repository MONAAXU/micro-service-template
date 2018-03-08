import scala.util.{Failure, Success}
import scala.concurrent.{Await, Future, Promise}
import scala.concurrent.duration._
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.stream.ActorMaterializer
import akka.stream.scaladsl._
import akka.stream.{OverflowStrategy, QueueOfferResult}
import com.typesafe.sslconfig.akka.AkkaSSLConfig

object Client{
  def main(args: Array[String]) {

    implicit val system = ActorSystem()
    implicit val materializer = ActorMaterializer()
    implicit val executionContext = system.dispatcher
    val QueueSize = 10

    val poolClientFlow = Http().cachedHostConnectionPool[Promise[HttpResponse]]("localhost",8080)
    val queue =
      Source.queue[(HttpRequest, Promise[HttpResponse])](QueueSize, OverflowStrategy.dropNew)
        .via(poolClientFlow)
        .toMat(Sink.foreach({
          case ((Success(resp), p)) => p.success(resp)
          case ((Failure(e), p))    => p.failure(e)
        }))(Keep.left)
        .run()

    def queueRequest(request: HttpRequest): Future[HttpResponse] = {
      val responsePromise = Promise[HttpResponse]()
      queue.offer(request -> responsePromise).flatMap {
        case QueueOfferResult.Enqueued    => responsePromise.future
        case QueueOfferResult.Dropped     => Future.failed(new RuntimeException("Queue overflowed. Try again later."))
        case QueueOfferResult.Failure(ex) => Future.failed(ex)
        case QueueOfferResult.QueueClosed => Future.failed(new RuntimeException("Queue was closed (pool shut down) while running the request. Try again later."))
      }
    }

    val responseFuture: Future[HttpResponse] = queueRequest(HttpRequest(uri = "/"))

    val res = Await.result(responseFuture,10.second)
    print(Await.result(res.entity.dataBytes.runWith(Sink.head),10.seconds).utf8String)

    val badSslConfig = AkkaSSLConfig().mapSettings(s => s.withLoose((s.loose.withDisableHostnameVerification(true))))
    val badCtx = Http().createClientHttpsContext(badSslConfig)


  }
}