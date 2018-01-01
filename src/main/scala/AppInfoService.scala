import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.server.Directives._
import spray.json.DefaultJsonProtocol

trait JsonSupport extends SprayJsonSupport with DefaultJsonProtocol {
  case class AppInfo(version: String, status: String)
  implicit val appInfoFormat = jsonFormat2(AppInfo)
}

trait AppInfoService extends JsonSupport{
  val appInfoServiceRoute =
    path("status") {
      get {
        complete(AppInfo("1.0.0","READY"))
      }
    }
}