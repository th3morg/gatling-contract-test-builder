# Gatling Contract Test Builder
This is a small Grails project that helps you create an exec http request statement which checks the structure of the JSON response of any RESTful API, yielding a simple contract test.
 
A contract test is one which validates that the structure of output from an API call matches the expectation of the consuming client application. A great project already exists ([pacto](https://github.com/thoughtworks/pacto)) but I was really interested in utilizing gatling as both a stress testing tool and a contract testing tool. 

The existing gatling recorder tool helps capture the requests you want to test and this check builder serves as a complimentary tool for building simple contract tests. In its current state, it validates the structure of the JSON, but does not make any assertions about the expected values within that response. Those can still be added manually if desired. 

## The Utility
This tool outputs the checks in a format that expects to have the following code as a part of your gatling test scenario. The checks will attempt to perform a transform on the BodyString of the HTTP response which utilizes these methods. 

```
import net.minidev.json.{JSONArray, JSONObject}
import net.minidev.json.parser.JSONParser

...

  def toJSON(x : String) : JSONObject = {
    parser.parse(x).asInstanceOf[JSONObject]
  }

  def getNextJSONObject(x: JSONArray) : JSONObject = {
    var ret : AnyRef = x
    while(!ret.isInstanceOf[JSONObject]){
      ret = ret.asInstanceOf[JSONArray].get(0)
    }
    ret.asInstanceOf[JSONObject]
  }

  def getItem(x: JSONObject, item: String) : JSONObject = {
    x.get(item) match {
      case i : JSONObject =>
        x.get(item).asInstanceOf[JSONObject]
      case h : JSONArray =>
        getNextJSONObject(x.get(item).asInstanceOf[JSONArray])
      case _ =>
        throw new Exception(s"You've set up your check incorrectly or the API has changed: Invalid path found while traversing JSON: ${x.toJSONString} : item: ${item}")
    }
  }

  def findKey(x : JSONObject, key : String) : Boolean = {
    val items = key.split('.').toList
    var node = x
    var a : Int = 1
    for(item <- items
      if a < items.size) {
      println("item: " + item)
      a += 1
      node = getItem(node, item)
    }

    node.containsKey(items.last)
  }
```

You'll need to import the JSON Smart jar into your gralde build file or maven pom. 

`compile 'net.minidev:json-smart:2.0'`

The Gatling Check Builder will crawl the JSON response for your request and output a full exec statement with a check for each property in the node. The code above is used to determine if the property is found. Here is an example exec that Gatling Check Builder generated given the URL [http://echo.jsontest.com/${1}/value/${2}/two/${3}/awesomeness](http://echo.jsontest.com/key/value/one/two/excellent/awesomeness).

```
		val _value_two_awesomeness = exec(http("_value_two_awesomeness")
				.get(s"""/${1}/value/${2}/two/${3}/awesomeness""".stripMargin)
				.check(bodyString.transform(s => findKey(toJSON(s), "one")).is(true))
				.check(bodyString.transform(s => findKey(toJSON(s), "excellent")).is(true))
				.check(bodyString.transform(s => findKey(toJSON(s), "key")).is(true))
		)
```

As you can see, the app allows for the use of variables and allows for you to specify the values to use for those variables during execution. The app generates the checks under the assumption that you have implemented the `findKey` method and the methods it depends on that are shown above.

## Running the app
Clone the project to your local machine and execute `grails run-app` on the command line to start the app on port 8080. The UI is simple, but self explanatory.

## Contributions
This was developed in a couple of days. It's not bullet proof or feature complete. Please feel free to fork and expand upon this project. I would be more than happy to accept pull requests and issues. 