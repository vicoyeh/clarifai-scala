import clarifai._

object Sample extends App {
	// Visit https://developer.clarifai.com/ to get your client ID and secret
	val id = "<Client_Id>"
	val secret = "<Client_Secret>"

	// create a client instance
	val client = new ClarifaiClient(id, secret)
	// info endpoint
	val ret = client.info()
	val result:InfoResp = ret match {
	    case Left(err) => null
	    case Right(res) => res
	}

	// tag endpoint
	// model and lang parameters are optional
	val tagRet = client.tag(Map(
					"urls" -> Array("http://www.clarifai.com/img/metro-north.jpg",
									"http://www.clarifai.com/img/metro-north.jpg"), 
					"model" -> "nsfw-v1.0",
					"lang" -> "en"))
	val tag:TagResp = tagRet match {
		case Left(err) => null
		case Right(res) => res
	}

	// usage endpoint
	val usageRet = client.usage()
	val usage:UsageResp = usageRet match {
		case Left(err) => null
		case Right(res) => res
	}

	// color endpoint (Clarifai is currently beta-testing this endpoint)
	val colorRet = client.color(Array("https://samples.clarifai.com/metro-north.jpg"))
	val color:ColorResp = colorRet match {
		case Left(err) => null
		case Right(res) => res
	}
}