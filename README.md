# clarifai-scala
Clarifai API Scala Client

## Usage
sbt console
```scala
import clarifai._
val client = new  ClarifaiClient("<client_id>", "<client_secret>")

// info endpoint
val infoRet = client.info()
val info:InfoResp = infoRet match {
	case Left(err) => null
	case Right(res) => res
}

// tag endpoint
// model and lang parameters are optional
val tagRet = client.tag(Map(
							"url" -> Array("http://www.clarifai.com/img/metro-north.jpg",
											"http://www.clarifai.com/img/metro-north.jpg"), 
							"model" -> "nsfw-v1.0",
							"language" -> "en"))
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

// feedback endpoint
val feedbackRet = client.feedback(Map(
									   "url" -> Array("http://www.clarifai.com/img/metro-north.jpg",
									   				   "http://www.clarifai.com/img/metro-north.jpg"),
									  	"add_tags" -> Array("cat", "animal")))
val feedback:FeedbackResp = feedbackRet match {
	case Left(err) => null
	case Right(res) => res
}

// color endpoint (Clarifai is currently beta-testing this endpoint)
val colorRet = client.color(Array("https://samples.clarifai.com/metro-north.jpg"))
val color:ColorResp = colorRet match {
	case Left(err) => null
	case Right(res) => res
}
```