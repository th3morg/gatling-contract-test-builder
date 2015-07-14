package gatling.check.builder

import grails.converters.JSON
import grails.plugins.rest.client.RestBuilder
import grails.plugins.rest.client.RestResponse
import grails.transaction.Transactional
import org.codehaus.groovy.grails.web.json.JSONArray
import org.codehaus.groovy.grails.web.json.JSONObject
import org.springframework.http.HttpMethod

@Transactional
class CodeGenerationService {
	def rest = new RestBuilder()

	public RestResponse getHTTPResponseBody(String url, String method, body) {
		def response
		switch (HttpMethod.valueOf(method)) {
			case HttpMethod.GET:
				response = rest.get(url)
				break;
			case HttpMethod.POST:
				response = rest.post(url) {
					json body
				}
				break;
			case HttpMethod.PUT:
				response = rest.put(url) {
					json body
				}
				break;
			case HttpMethod.OPTIONS:
				response = rest.options(url)
				break;
			case HttpMethod.DELETE:
				response = rest.delete(url){
					json body
				}
				break;
			case HttpMethod.PATCH:
				response = rest.patch(url){
					json body
				}
				break;
			case HttpMethod.HEAD:
				response = rest.head(url){
					json body
				}
				break;
			case HttpMethod.TRACE:
				response = rest.trace(url){
					json body
				}
				break;
			default:
				throw new Exception("Unidentified HTTP Method $method. Please use one of the following ${HttpMethod.values().collect { it.name() }.join(", ")}.")
		}

		return response
	}

	String generateExecStatement(String requestPath, Map paramVariables, String method, String body, JSON json){
		String field = requestPath.substring(0, requestPath.indexOf("?") != -1 ? requestPath.indexOf("?"): requestPath.length()).replace("/", "_")
		paramVariables.each {
			field = field.replace("_${it.key}", "")
		}
		def gatlingBodyLines = []
		body?.eachLine {
			gatlingBodyLines << "|" + it
		}
		"""
\t\tval ${field} = exec(http(\"${field}\")
\t\t\t\t.${method.toLowerCase()}(s\"\"\"${requestPath}\"\"\".stripMargin)${gatlingBodyLines? "\n\t\t\t\t.body(\n\t\t\t\t\t\tStringBody(s\"\"\"" + gatlingBodyLines.join("\n") + "\n\t\t\t\t\t\t\"\"\".stripMargin)\n\t\t\t\t)" : ''}
${buildChecks(json.getProperties()["target"] as JSONObject, "").collect{
		"\t\t\t\t.check(bodyString.transform(s => findKey(toJSON(s), \"$it\")).is(true))"
	}.join("\n")
}
\t\t)
		"""
	}

	protected List<String> buildChecks(JSONObject node, String parentPath){
		List<String> checks = []
		node.each {
			String path = parentPath ? parentPath + "." + it.key : it.key
			if(it.value instanceof JSONObject){
				checks.addAll(buildChecks(it.value as JSONObject, path))
			} else if(it.value instanceof JSONArray){
				checks.addAll(buildChecks(it.value as JSONArray, path))
			} else {
				checks << path
			}
		}
		return checks
	}

	protected List<String> buildChecks(JSONArray node, String parentPath){
		List<String> checks = []
		if(node.isEmpty()) return checks
		def obj = node.get(0)
		if (obj instanceof JSONArray){
			checks.addAll(buildChecks(obj, parentPath))
		} else if(obj instanceof JSONObject){
			checks.addAll(buildChecks(obj, parentPath))
		} else {
			println "Found array with non-json object contents, adding the parent path to the checks since there is no depth"
			checks << parentPath
		}

		return checks
	}
}