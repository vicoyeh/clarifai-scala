package clarifai

import scalaj.http._
import scala.util.parsing.json.JSON

// configuration
object Config {
  val baseURL = "https://api.clarifai.com"
  val version = "v1"
}

// case classes for representing JSON requests and responses
// info endpoint
case class InfoResults(
  maxImageSize: Double,
  defaultLanguage: String,
  maxVideoSize: Double,
  maxImageBytes: Double,
  minImageSize: Double,
  defaultModel: String,
  maxVideoBytes: Double,
  maxVideoDuration: Double,
  maxBatchSize: Double,
  maxVideoBatchSize: Double,
  minVideoSize: Double,
  apiVersion: Double
)
case class InfoResp(
  statusCode: String,
  statusMessage: String,
  results: InfoResults
)

// tag endpoint
case class TagResp(
  statusCode: String,
  statusMessage: String,
  meta: TagMeta,
  results: TagResults
)
case class TagMeta(
  tag: TagMetaTag
)
case class TagMetaTag(
  timestamp: Double,
  model: String,
  config: String
)
case class TagResults(
  docid: Double,
  url: String,
  statusCode: String,
  statusMessage: String,
  localId: String,
  result: TagResultsRes,
  docidStr: String
)
case class TagResultsRes(
  tag: TagResultsResTag
)
case class TagResultsResTag(
  conceptIds: Array[String],
  classes: Array[String],
  probs: Array[Double]
)

// main client api class
class ClarifaiClient(id: String, secret: String) {
  val clientID = id
  val clientSecret = secret
  var accessToken = "unassigned"
  var throttled = false

  /** Main Clarifai endpoints
    * 
    */

  // INFO
  def info(): Either[Option[String], InfoResp] = {
    val response = _commonHTTPRequest(None, "info", "GET", false)
    // check if received error
    
    response match {
      case Left(err) => Left(err)
      case Right(result) => {
        val rmap = JSON.parseFull(result).get.asInstanceOf[Map[String, Any]]
        val results = rmap.get("results").get.asInstanceOf[Map[String, Any]]
        
        Right(
          InfoResp(
            rmap.get("status_code").get.asInstanceOf[String],
            rmap.get("status_msg").get.asInstanceOf[String],
            InfoResults(
              results.get("max_image_size").get.asInstanceOf[Double],
              results.get("default_language").get.asInstanceOf[String],
              results.get("max_video_size").get.asInstanceOf[Double],
              results.get("max_image_bytes").get.asInstanceOf[Double],
              results.get("min_image_size").get.asInstanceOf[Double],
              results.get("default_model").get.asInstanceOf[String],
              results.get("max_video_bytes").get.asInstanceOf[Double],
              results.get("max_video_duration").get.asInstanceOf[Double],
              results.get("max_batch_size").get.asInstanceOf[Double],
              results.get("max_video_batch_size").get.asInstanceOf[Double],
              results.get("min_video_size").get.asInstanceOf[Double],
              results.get("api_version").get.asInstanceOf[Double]
            )
          )
        )
      }
    }
  }

  // TAG - TODO
  def tag(): Either[Option[String], TagResp] = {
    val response = _commonHTTPRequest(None, "tag", "POST", false)
    // check if received error
    response match {
      case Left(err) => Left(err)
      case Right(result) => {
        val rmap = JSON.parseFull(result).get.asInstanceOf[Array[Any]]
          val meta = rmap.get("meta").get.asInstanceOf[Map[String, Any]]
            val meta_tag = meta.get("tag").get.asInstanceOf[Map[String, Any]]

          val results = rmap.get("results").get.asInstanceOf[Map[String, Any]]
            val result = results.get("result").get.asInstanceOf[Map[String, Any]]
              val res_tag = result.get("tag").get.asInstanceOf[Map[String, Any]]

        Right(
          TagResp(
            rmap.get("status_code").get.asInstanceOf[String],
            rmap.get("status_msg").get.asInstanceOf[String],
            TagMeta(
              TagMetaTag(
                meta_tag.get("timestamp").get.asInstanceOf[Double],
                meta_tag.get("model").get.asInstanceOf[String],
                meta_tag.get("config").get.asInstanceOf[String]
              )
            ),
            TagResults(
              results.get("docid").get.asInstanceOf[Double],
              results.get("url").get.asInstanceOf[String],
              results.get("status_code").get.asInstanceOf[String],
              results.get("status_msg").get.asInstanceOf[String],
              results.get("local_id").get.asInstanceOf[String],
              TagResultsRes(
                TagResultsResTag(
                  res_tag.get("concept_ids").get.asInstanceOf[Array[String]],
                  res_tag.get("classes").get.asInstanceOf[Array[String]],
                  res_tag.get("probs").get.asInstanceOf[Array[Double]]
                )
              ),
              results.get("docid_str").get.asInstanceOf[String]
            )
          )
        )
      }
    }
  }

  /** Helper functions to handle HTTP requests and responses
    * Clients should not invoke these functions explicitly
    */
  def _requestAccessToken(): Option[String]  = {
    val form = Seq("grant_type" -> "client_credentials", 
                    "client_id" -> clientID,
                    "client_secret" -> clientSecret)

    val url = _buildURL("token")
    val response: HttpResponse[String] = Http(url).postForm(form)
                        .header("Authorization", ("Bearer " + accessToken))
                        .header("content-type", "application/x-www-form-urlencoded")
                        .asString
    
    if (response.isError) return Some("4XX OR 5XX ERROR")

    val json_body:Map[String,Any] = JSON.parseFull(response.body).get.asInstanceOf[Map[String, Any]]
    if (json_body.isEmpty) return Some("EMPTY_JSON")
    
    val token = json_body.get("access_token").get.asInstanceOf[String]
    if (token == "") return Some("EMPTY_TOKEN")

    _setAccessToken(token)
    None
  }

  def _commonHTTPRequest(data:Option[String] ,endpoint: String, verb:String, retry: Boolean)
        : Either[Option[String], String] = {
    val req_data = data match {
      case Some(i) => i
      case None => ""
    }

    val url = _buildURL(endpoint)
    var response: HttpResponse[String] = null
    verb match {
      case "POST" => {
        response = Http(url).postData(req_data)
                        .header("Authorization", ("Bearer " + accessToken))
                        .header("content-type", "application/x-www-form-urlencoded")
                        .asString
      }
      case "GET" => {
        response = Http(url).header("Authorization", ("Bearer " + accessToken)).asString
      }
      case _ => {
        return Left(Some("ILLEGAL_VERB"))
      }
    }

    // match http response code to specific functions
    response.code match {
      case 200|201 => {
        if (throttled) {
          _setThrottle(false)
        }
        //println(response.body)
        Right(response.body)
      }
      case 401 => {
        if (!retry) {
          val err = _requestAccessToken()
          if (err != None) {
            return Left(err)
          }
          return _commonHTTPRequest(data, endpoint, verb, true)
        }
        Left(Some("TOKEN_INVALID"))
      }
      case 429 => {
        _setThrottle(true)
        Left(Some("THROTTLED"))
      }
      case 400 => {
        Left(Some("ALL_ERROR"))
      }
      case 500 => {
        Left(Some("CLARIFAI_ERROR"))
      }
      case _ => {
        Left(Some("UNEXPECTED_STATUS_CODE"))
      }
    }
  }

  def _buildURL(endpoint: String): String = {
    val parts = Array(Config.baseURL, Config.version, endpoint)
    return parts.mkString("/")
  }

  def _setAccessToken(token: String) = {
    accessToken = token
  }

  def _setThrottle(throttle:Boolean) = {
    throttled = throttle
  }
}
