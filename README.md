# Clarifai Scala Client
[<img src="https://img.shields.io/travis/scala/scala-parser-combinators.svg"/>](https://travis-ci.org/vic317yeh/clarifai-scala)
Scala client wrapper for the Clarifai image and video recognition API.

The client wrapper constructor takes two parameters: client_id and client_secret. You can find these values in your developer application page. To learn more about Clarifai image and video recognition API, check out the Clarifai developer page: https://developer.clarifai.com.

## Installation


## Dependency
This client wrapper uses [scalaj-http](https://github.com/scalaj/scalaj-http) for handling HTTP request and response.

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
