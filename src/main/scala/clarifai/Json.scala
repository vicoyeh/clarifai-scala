package clarifai

/** Case classes for formating Clarifai response in JSON-like structures.
  *
  * This file contains response structures for all the endpoints.
  */

/** Info */
case class InfoResp(
  statusCode: String,
  statusMessage: String,
  results: InfoResults
)
case class InfoResults(
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
)

/** Tag */
case class TagResp(
  statusCode: String,
  statusMessage: String,
  meta: TagMeta,
  results: List[TagResult]
)
case class TagMeta(
  tag: TagMetaTag
)
case class TagMetaTag(
  timestamp: Double,
  model: String,
  config: String
)
case class TagResult(
  docid: Double,
  url: String,
  statusCode: String,
  statusMessage: String,
  localId: String,
  result: TagResultRes,
  docidStr: String
)
case class TagResultRes(
  tag: TagResultResTag
)
case class TagResultResTag(
  // conceptIDs: List[String],
  classes: List[String],
  probs: List[Double]
)

/** Usage */
case class UsageResp(
  statusCode: String,
  statusMessage: String,
  results: UsageResults
)
case class UsageResults(
  userThrottles: List[UsageResultUT],
  appThrottles: Map[String, Any]
)
case class UsageResultUT(
  name: String,
  consumed: Double,
  consumedPercentage: Double,
  limit: Double,
  units: String,
  waitTime: Double
)

/** Feedback */
case class FeedbackResp(
  statusCode: String,
  statusMessage: String
)

/** Color (beta) */
case class ColorResp(
  statusCode: String,
  statusMessage: String,
  results: List[ColorResults]
)
case class ColorResults(
  docid: Double,
  url: String,
  docidStr: String,
  colors: List[ResultsColors]
)
case class ResultsColors(
  w3c: Colorw3c,
  hex: String,
  density: Double
)
case class Colorw3c(
  hex: String,
  name: String
)