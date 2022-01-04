package $package$.frontend.services

import $package$.shared.model.worker.PingPong

import com.avsystem.commons.serialization.GenCodec
import com.avsystem.commons.serialization.json._

import io.udash.logging.CrossLogging

import org.scalajs.dom.raw.{ErrorEvent, File, MessageEvent}
import org.scalajs.dom.webworkers.Worker

import scala.scalajs.js

import scala.collection.mutable
import scala.concurrent.{ExecutionContext, Future, Promise}

class WorkerService(path: String)(implicit ec: ExecutionContext) extends CrossLogging {
  val worker = new Worker(path)
  private var nextRID = 0
  private val requests = mutable.Map.empty[Int, Promise[(String, js.Dynamic)]]

  worker.onerror = (e: ErrorEvent) => {
    logger.error(s"WorkerService onerror", e)
  }

  worker.onmessage = (m: MessageEvent) => {
    val data = m.data.asInstanceOf[js.Dynamic]
    val rid = data.rid.asInstanceOf[Int]
    val json = data.json.asInstanceOf[String]
    requests.get(rid) match {
      case Some(promise) =>
        requests.remove(rid)
        promise.success((data.json.asInstanceOf[String], data.dynamic))

      case None =>
        logger.error(s"Undefined request, rid: \$rid; json: \$json")
    }
  }

  private def request[REQ : GenCodec, RESP : GenCodec](method: String, req: REQ, dynamic: js.Dynamic = null): Future[(RESP, js.Dynamic)] = {
    val promise = Promise[(String, js.Dynamic)]()
    val rid = nextRID
    nextRID += 1

    requests.put(rid, promise)

    worker.postMessage(js.Dynamic.literal(
      "\$" -> method,
      "rid" -> rid,
      "json" -> JsonStringOutput.write(req),
      "dynamic" -> dynamic
    ))

    promise.future map { case (json, dynamic) =>
      (JsonStringInput.read[RESP](json), dynamic)
    }
  }

  /**
    * Send ping with payload and dynamic DOM object.
    */
  def ping(payload: String, file: File): Future[(String, File)] =
    request[PingPong.Request, PingPong.Response](
      PingPong.method, PingPong.Request(payload), file.asInstanceOf[js.Dynamic]
    ) map { case (resp, file) =>
      (resp.payload, file.asInstanceOf[File])
    }

}
