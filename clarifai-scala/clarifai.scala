package clarifai-scala


// configuration
object Config {
	val baseURL = "https://api.clarifai.com"
	val version = "v1"
}

// main client api class
class ClarifaiClient(id: String, secret: String) {
	val clientID = id
	val clientSecret = secret
	val accessToken: String
	var throttled = false

	def _requestAccessToken = {
		// val formData = _encodeUrl(clientID, clientSecret)
	}

	def _commonHTTPRequest(): Array[Byte] = {
		
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