package clarifai

/** Imports scalaj library for handling HTTP request & response. */
import scalaj.http._
import scala.util.parsing.json.JSON

/** Configuration setting for the client wrapper. */
object Config {
  val baseURL = "https://api.clarifai.com"
  val version = "v1"
}

/** Client wrapper for accessing Clarifai endpoints.
  *
  * Currently supports the following endpoints:
  * - Tag
  * - Feedback
  * - Color (beta)
  * - Info
  * - Usage
  * You would need a Clarifai developer account for using the service.
  * Developer page: https://developer.clarifai.com
  *
  * @constructor create a new client object for accessing endpoints.
  * @param id client id for your Clarifai API applicaiton
  * @param secret client secret for your Clarifai API applicaiton
  */
class ClarifaiClient(id: String, secret: String) {
  val clientID = id
  val clientSecret = secret
  var accessToken = "unassigned"
  var throttled = false

  /** High-level functions for accessing Clarifai endpoints.
    * 
    * The following functions allow users to access the Clarifai service directly
    * and receive the response in JSON-like class structure.
    */

  /** Feedback - provides the ability to give feedback to the API about images and videos that were previously tagged.
    * 
    * @param Map of values for providing feedback to the API
    * @return Feedback response from the Clarifai endpoint 
    */
  def feedback(feedbackReq: Map[String, Any]): Either[Option[String], FeedbackResp] = {
    if ((!feedbackReq.contains("url") || feedbackReq.get("url").get.asInstanceOf[Array[String]].length < 1 )
      && (!feedbackReq.contains("docids") || feedbackReq.get("docids").get.asInstanceOf[Array[String]].length < 1 )) {
      return Left(Some("Needs at least one url or docid"))
    }

    if (feedbackReq.contains("url") && feedbackReq.contains("docids")) {
      return Left(Some("Request must provide exactly one of the following fields: urls or docids"))
    }

    /** Converts the user input into string format. */
    var data = ""
    data = data.concat(_extractStrArrWithAmp(feedbackReq, "add_tags"))
    data = data.concat(_extractStrArrWithAmp(feedbackReq, "remove_tags"))
    data = data.concat(_extractStrArrWithAmp(feedbackReq, "similar_docids"))
    data = data.concat(_extractStrArrWithAmp(feedbackReq, "dissimilar_docids"))
    data = data.concat(_extractStrArrWithAmp(feedbackReq, "search_click"))
    data = data.concat(_extractStrArrWithAmp(feedbackReq, "url"))
    data = data.concat(_extractStrArrWithAmp(feedbackReq, "docids"))
    data = data.dropRight(1) // Removes the last "&" character.

    /** Sends the HTTP request. */
    val response = _commonHTTPRequest(Some(data), "feedback", "POST", false)

    /** Returns the HTTP response into JSON-like class structure. */
    response match {
      case Left(err) => Left(err)
      case Right(result) => {
        val rmap = JSON.parseFull(result).get.asInstanceOf[Map[String, Any]]

        Right(
          FeedbackResp(
            rmap.get("status_code").get.asInstanceOf[String],
            rmap.get("status_msg").get.asInstanceOf[String]
          )
        )
      }
    }
  }

  /** Color (beta) - retrieves the dominant colors present in your images or videos. 
    * 
    * @param Array of urls for color detection
    * @return Color response from the Clarifai endpoint 
    */
  def color(colorReq: Array[String]): Either[Option[String], ColorResp] = {
    if (colorReq.length < 1 ) {
      return Left(Some("Needs at least one url"))
    }

    /** Converts the user input into string format. */
    var data = ""
    for (str <- colorReq) { data = data.concat("url=" + str + "&") }
    data = data.dropRight(1) // Removes the last "&" character.

    /** Sends the HTTP request. */
    val response = _commonHTTPRequest(Some(data), "color", "POST", false)
    
    /** Returns the HTTP response into JSON-like class structure. */
    response match {
      case Left(err) => Left(err)
      case Right(result) => {
        val rmap = JSON.parseFull(result).get.asInstanceOf[Map[String, Any]]
        val results = rmap.get("results").get.asInstanceOf[List[Map[String, Any]]]
        var resultsArr = List[ColorResults]()
        
        results.foreach((itemR: Map[String, Any]) => {
          val colors = itemR.get("colors").get.asInstanceOf[List[Map[String, Any]]]
          var colorsArr = List[ResultsColors]()

          colors.foreach((itemC: Map[String, Any]) => {
            val w3c_color = itemC.get("w3c").get.asInstanceOf[Map[String, Any]]
            val rColor:ResultsColors = ResultsColors(
              Colorw3c(
                w3c_color.get("hex").get.asInstanceOf[String],
                w3c_color.get("name").get.asInstanceOf[String]
              ),
              itemC.get("hex").get.asInstanceOf[String],
              itemC.get("density").get.asInstanceOf[Double]
            )
            colorsArr :::= List(rColor)
          })

          val cResult:ColorResults = ColorResults(
            itemR.get("docid").get.asInstanceOf[Double],
            itemR.get("url").get.asInstanceOf[String],
            itemR.get("docid_str").get.asInstanceOf[String],
            colorsArr
          )
          resultsArr :::= List(cResult)
        })

        Right(
          ColorResp(
            rmap.get("status_code").get.asInstanceOf[String],
            rmap.get("status_msg").get.asInstanceOf[String],
            resultsArr
          )
        )
      }
    }
  }

  /** Info - returns the current API details as well as any usage limits your account has.
    * 
    * @return Info response from the Clarifai endpoint 
    */
  def info(): Either[Option[String], InfoResp] = {
    /** Sends the HTTP request. */
    val response = _commonHTTPRequest(None, "info", "GET", false)
    
    /** Returns the HTTP response into JSON-like class structure. */
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

  /** Usage - returns your API usage for the current month and hour.
    * 
    * @return Usage response from the Clarifai endpoint 
    */
  def usage(): Either[Option[String], UsageResp] = {
    /** Sends the HTTP request. */
    val response = _commonHTTPRequest(None, "usage", "GET", false)
    
    /** Returns the HTTP response into JSON-like class structure. */
    response match {
      case Left(err) => Left(err)
      case Right(result) => {
        val rmap = JSON.parseFull(result).get.asInstanceOf[Map[String, Any]]
        val results = rmap.get("results").get.asInstanceOf[Map[String, Any]]
        
        var utArr = List[UsageResultUT]()
        val uThrottles = results.get("user_throttles").get.asInstanceOf[List[Map[String, Any]]]
        uThrottles.foreach((item: Map[String, Any]) => {
          val uThrottle:UsageResultUT = UsageResultUT(
            item.get("name").get.asInstanceOf[String],
            item.get("consumed").get.asInstanceOf[Double],
            item.get("consumed_percentage").get.asInstanceOf[Double],
            item.get("limit").get.asInstanceOf[Double],
            item.get("units").get.asInstanceOf[String],
            item.get("wait").get.asInstanceOf[Double]
          )
          utArr :::= List(uThrottle)
        })

        Right(
          UsageResp(
            rmap.get("status_code").get.asInstanceOf[String],
            rmap.get("status_msg").get.asInstanceOf[String],
            UsageResults(
              utArr,
              results.get("app_throttles").get.asInstanceOf[Map[String, Any]]
            )
          )
        )
      }
    }
  }

  /** Tag - tags the contents of your images or videos.
    * 
    * @param Map of values containing images and videos for tagging
    * @return Tag response from the Clarifai endpoint 
    */
  def tag(tagReq: Map[String, Any]): Either[Option[String], TagResp] = {
    if (!tagReq.contains("url") || tagReq.get("url").get.asInstanceOf[Array[String]].length < 1 ) {
      return Left(Some("Needs at least one url"))
    }

    /** Converts the user input into string format. */
    var data = ""
    data = data.concat(_extractStringWithAmp(tagReq, "model"))
    data = data.concat(_extractStringWithAmp(tagReq, "language"))
    /** TODO: select classes. */
    for (str <- tagReq.get("url").get.asInstanceOf[Array[String]]) {
      data = data.concat("url=" + str + "&")
    }
    data = data.dropRight(1) // Removes the last "&" character.
  
    val response = _commonHTTPRequest(Some(data), "tag", "POST", false)
    
    /** Returns the HTTP response into JSON-like class structure. */
    response match {
      case Left(err) => Left(err)
      case Right(result) => {
        val rmap = JSON.parseFull(result).get.asInstanceOf[Map[String, Any]]
        val meta = rmap.get("meta").get.asInstanceOf[Map[String, Any]]
        val meta_tag = meta.get("tag").get.asInstanceOf[Map[String, Any]]
        val results = rmap.get("results").get.asInstanceOf[List[Map[String, Any]]]
        var resultsArr = List[TagResult]()
        
        results.foreach((item: Map[String, Any]) => {
          val res = item.get("result").get.asInstanceOf[Map[String, Any]]
          val res_tag = res.get("tag").get.asInstanceOf[Map[String, Any]]

          val tResult:TagResult = TagResult(
            item.get("docid").get.asInstanceOf[Double],
            item.get("url").get.asInstanceOf[String],
            item.get("status_code").get.asInstanceOf[String],
            item.get("status_msg").get.asInstanceOf[String],
            item.get("local_id").get.asInstanceOf[String],
            TagResultRes(
              TagResultResTag(
                // res_tag.get("concept_ids").get.asInstanceOf[List[String]],
                res_tag.get("classes").get.asInstanceOf[List[String]],
                res_tag.get("probs").get.asInstanceOf[List[Double]]
              )
            ),
            item.get("docid_str").get.asInstanceOf[String]
          )

          resultsArr :::= List(tResult)
        })

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
            resultsArr
          )
        )
      }
    }
  }

  /** Functions for establishing the underlying conneciton with the Clarifai API service.
    * 
    * The following functions should be private. They help to establish HTTP connection 
    * and handle the user input and response error.
    */

  /** Requests access token from the Clarifai API service. */
  private def _requestAccessToken(): Option[String]  = {
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

  /** Sends HTTP request to the specified enpoint with the user input data. */
  private def _commonHTTPRequest(data:Option[String] ,endpoint: String, verb:String, retry: Boolean)
        : Either[Option[String], String] = {
    val req_data = data match {
      case Some(i) => i
      case None => ""
    }

    val url = _buildURL(endpoint)
    var response: HttpResponse[String] = null
    /** Sends HTTP request based on the method type. */
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

    /** Matches HTTP response code to the corresponding return value. */
    response.code match {
      case 200|201 => {
        if (throttled) {
          _setThrottle(false)
        }
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

  /** Helper functions for modifying request and response data. */
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

  def _extractStringWithAmp(obj: Map[String, Any], field: String): String = {
    if (obj.contains(field)) {
      field + "=" + obj.get(field).get.asInstanceOf[String] + "&"
    }
    else {
      ""
    }
  }

  def _extractStrArrWithAmp(obj: Map[String, Any], field: String): String= {
    if (obj.contains(field)) {
      var data = field + "="
      for (str <- obj.get(field).get.asInstanceOf[Array[String]]) {
        data = data.concat(str + ",")
      }
      data = data.dropRight(1) // Removes the last "," character.
      data + "&"
    }
    else {
      ""
    }
  }
}