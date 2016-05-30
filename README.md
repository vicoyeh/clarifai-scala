# Clarifai Scala Client
[<img src="https://img.shields.io/travis/scala/scala-parser-combinators.svg"/>](https://travis-ci.org/vic317yeh/clarifai-scala)

Scala client wrapper for the Clarifai image and video recognition API.

The client wrapper constructor takes two parameters: client_id and client_secret. You can find these values in your developer application page. To learn more about Clarifai image and video recognition API, check out the Clarifai developer page: https://developer.clarifai.com.

## Installation
todo

## Dependency
This client wrapper uses [scalaj-http](https://github.com/scalaj/scalaj-http) for handling HTTP request and response.

## Usage
sbt console
```scala
import clarifai._
val client = new  ClarifaiClient("<client_id>", "<client_secret>")

// get the version of Clarifai API
val infoRet = client.info()
if (infoRet.isRight) {
	val info:InfoResp = infoRet.right.get
	println("API version is " + info.results.apiVersion)
} else {
	println("Error: " + infoRet.left.get)
}

// tag an image using url
val url = Array("http://www.clarifai.com/img/metro-north.jpg")
val tagRet = client.tag(Map("url" -> url))
val tag:TagResp = tagRet match {
	case Left(err) => {
		println("Error: " + err)
		System.exit(0)
		null
	}
	case Right(res) => res
}
// get API model
println("API model is " + tag.meta.tag.model)
// get the list of classes and probabilities for the given image
val firstImg = tag.results.head
val classes = firstImg.result.tag.classes.toArray
val probs = firstImg.result.tag.probs.toArray
for (i <- 0 to (classes.length - 1)) {
	println(classes(i) + ": " + probs(i))
}
```

## API
Note: we will use {} to represent the case class types and [] to represent the List type. Request is the parameter of the function and response is the return value of the function.
### Info
The info endpoint returns the current API details as well as any usage limits your account has.
```scala
val infoRet = client.info()
val info:InfoResp = infoRet match {
	case Left(err) => null
	case Right(res) => res
}
```
Request: None

Response: InfoResp
```text
{
	statusCode: String,
	statusMessage: String,
	results: {
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
	}
}
```

### Tag
The tag endpoint is used to tag the contents of your images or videos. Data is input into our system, processed with our deep learning platform and a list of tags is returned.
```scala
val tagRet = client.tag(Map(
							"url" -> Array("http://www.clarifai.com/img/metro-north.jpg",
											"http://www.clarifai.com/img/metro-north.jpg"), 
							"model" -> "nsfw-v1.0",
							"language" -> "en"))
val tag:TagResp = tagRet match {
	case Left(err) => null
	case Right(res) => res
}
```
Request: Map[String, Any]
```text
Map(
	"url" -> Array[String],
	"model" -> String,
	"language" -> String
)
url: required -> Array of url of image or video
model: optional -> String of [general-v1.3, nsfw-v1.0, weddings-v1.0, travel-v0.1]
language: optional -> String of a language code (ie. ar)

*If you use a language other than English, you must make sure the model you are using is general-v1.3.*
```
Response: TagResp
```text
{
	statusCode: String,
	statusMessage: String,
	meta: {
		tag: {
			timestamp: Double,
			model: String,
			config: String
		}
	},
	results: {
		docid: Double,
		url: String,
		statusCode: String,
		statusMessage: String,
		localId: String,
		result: {
			tag: {
				classes: List[String],
  				probs: List[Double]
  			}
		},
		docidStr: String
	}
}
```

### Usage
The usage endpoint returns your API usage for the current month and hour.
```scala
val usageRet = client.usage()
val usage:UsageResp = usageRet match {
	case Left(err) => null
	case Right(res) => res
}
```
Request: None

Response: UsageResp
```text
{
	statusCode: String,
	statusMessage: String,
	results: {
		userThrottles: [
			name: String,
			consumed: Double,
			consumedPercentage: Double,
			limit: Double,
			units: String,
			waitTime: Double
		],
		appThrottles: Map[String, Any]
	}
}

```

### Feedback
The feedback endpoint provides the ability to give feedback to the API about images and videos that were previously tagged. This is typically used to correct errors made by our deep learning platform.
```scala
val feedbackRet = client.feedback(Map(
									   "url" -> Array("http://www.clarifai.com/img/metro-north.jpg",
									   				   "http://www.clarifai.com/img/metro-north.jpg"),
									  	"add_tags" -> Array("cat", "animal")))
val feedback:FeedbackResp = feedbackRet match {
	case Left(err) => null
	case Right(res) => res
}
```
Request: Map[String, Any]
```text
Map(
	"url" or "docids" -> Array[String],
	"add_tags" -> Array[String],
	"remove_tags" -> Array[String],
	"similar_docids" -> Array[String],
	"dissimilar_docids" -> Array[String],
	"search_click" -> Array[String]
)
url or docids: required -> Array of url or docids of image or video
add_tags: optional -> Array of tags to add
remove_tags: optional -> Array of tags to remove
similar_url or similar_docids: optional -> Array of url or docids similar to url or docids
dissimilar_url or dissimilar_docids: optional -> Array of url or docids dissimilar to url or docids
search_click: optional -> Array of search terms that generated the search result
```
Response: UsageResp
```text
{
	statusCode: String,
	statusMessage: String
}
```

### Color (beta)
The color endpoint is used to retrieve the dominant colors present in your images or videos. Color values are returned in the hex format.
```scala
// Clarifai is currently beta-testing this endpoint
val colorRet = client.color(Array("https://samples.clarifai.com/metro-north.jpg"))
val color:ColorResp = colorRet match {
	case Left(err) => null
	case Right(res) => res
}
```
Request: Array[String]
```text
Map(
	"color" -> Array[String]
)
color: required -> Array of urls of image or video that present the dominant colors
```
Response: UsageResp
```text
{
	statusCode: String,
	statusMessage: String
}

Response: UsageResp
```text
{
	statusCode: String,
	statusMessage: String,
	results: [
		docid: Double,
		url: String,
		docidStr: String,
		colors: [
			w3c: {
				hex: String,
  				name: String
			},
			hex: String,
			density: Double
		]
	]
}
```
