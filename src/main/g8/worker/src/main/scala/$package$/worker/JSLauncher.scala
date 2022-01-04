package $package$.worker

import $package$.shared.model.worker.PingPong

import com.avsystem.commons.serialization.GenCodec
import com.avsystem.commons.serialization.json._

import io.udash.logging.CrossLogging

import org.scalajs.dom.{ErrorEvent, MessageEvent}

import scala.scalajs.js
import scala.scalajs.js.annotation.JSExport

import scala.concurrent.Future
import scala.util.{Failure, Success}

import scala.concurrent.ExecutionContext.Implicits.global

object JSLauncher extends CrossLogging {
  private def processRequest[REQ : GenCodec, RESP : GenCodec](data: js.Dynamic)(cb: (REQ, js.Dynamic) => Future[(RESP, js.Dynamic)]): Unit = {
    val rid = data.rid.asInstanceOf[Int]
    val json = data.json.asInstanceOf[String]
    val req = JsonStringInput.read[REQ](json)
    val dynamic = data.dynamic
    cb(req, dynamic) onComplete {
      case Failure(ex) =>
        logger.error("Worker request failed: ", ex.getMessage, ex)

      case Success((resp, dynamic)) =>
        js.Dynamic.global.postMessage(js.Dynamic.literal(
          "rid" -> rid,
          "json" -> JsonStringOutput.write(resp),
          "dynamic" -> dynamic
        ))
    }
  }

  @JSExport
  def main(args: Array[String]): Unit = {
    logger.info("Worker loaded!")

    js.Dynamic.global.onerror = (e: ErrorEvent) =>
      logger.error(s"Worker onerror", e)

    js.Dynamic.global.onmessage = (m: MessageEvent) => {
      val data = m.data.asInstanceOf[js.Dynamic]
      try {
        data.\$.asInstanceOf[String] match {
          case PingPong.method =>
            processRequest[PingPong.Request, PingPong.Response](data) {
              case (req, file) =>
                Future.successful((PingPong.Response(req.payload.reverse), file))
            }

          case method =>
            logger.error(s"Worker get unexpected method: \$method")

        }
      } catch {
        case e: Throwable =>
          logger.error(s"Worker crashed: \${e.getMessage}", e)
      }
    }
  }
}
