package clarifai

/** The MIT License (MIT)
  Copyright (c) 2016 Kuan-Hsuan Yeh, Ji Min Kim

  Permission is hereby granted, free of charge, to any person obtaining a copy
  of this software and associated documentation files (the "Software"), to deal
  in the Software without restriction, including without limitation the rights
  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
  copies of the Software, and to permit persons to whom the Software is
  furnished to do so, subject to the following conditions:

  The above copyright notice and this permission notice shall be included in all
  copies or substantial portions of the Software.

  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
  SOFTWARE.
  */

/** Case classes for formating Clarifai response in JSON-like structures.
  *
  * This file contains response structures for all the endpoints.
  */

/** Info */
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
case class InfoResp(
  statusCode: String,
  statusMessage: String,
  results: InfoResults
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
  consumed_percentage: Double,
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