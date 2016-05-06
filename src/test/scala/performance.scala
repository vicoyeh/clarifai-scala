import clarifai._



object Test {
	val id = ""
	val secret = ""
	val client = new  ClarifaiClient(id, secret)
	val ret = client.info()
	val result:InfoResp = ret match {
	    case Left(err) => null
	    case Right(res) => res
	}
	print("Test")
}