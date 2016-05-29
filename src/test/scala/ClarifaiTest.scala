import clarifai._

object Test {
	val id = "<client_id>"
	val secret = "<client_secret>"
	val client = new  ClarifaiClient(id, secret)

	/** Accesses info endpoint */
	val ret = client.info()
	val result:InfoResp = ret match {
	    case Left(err) => null
	    case Right(res) => res
	}

	print("Complete all tests!")
}