package clarifai-scala

// configuration
object Config {
	val base_url = "https://api.clarifai.com"
	val version = "v1"
}

class ClarifaiClient(clientID: String, clientSecret: String) {
	val clientID = clientID
	val clientSecret = clientSecret


	def requestAccessToken = {
		val formData = _encodeUrl(clientID, clientSecret)
	}

	def _encodeUrl(clientID: String, clientSecret: String): String = {
		var str = ""
		str += "grant_type="
	}
}