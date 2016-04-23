# clarifai-scala
Clarifai API Scala Client

## Usage
sbt console
```scala
import clarifai._
val client = new  ClarifaiClient("<client_id>", "<client_secret>")
val ret = client.info()
val result:InfoResp = ret match {
	case Left(err) => null
	case Right(res) => res
}
```