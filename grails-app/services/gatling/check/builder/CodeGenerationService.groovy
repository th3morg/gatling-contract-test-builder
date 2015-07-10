package gatling.check.builder

import grails.converters.JSON
import grails.plugins.rest.client.RestBuilder
import grails.plugins.rest.client.RestResponse
import grails.transaction.Transactional
import org.springframework.http.HttpMethod

@Transactional
class CodeGenerationService {
	def rest = new RestBuilder()

	public RestResponse getHTTPResponseBody(String url, String method, body) {
		def response
		switch (HttpMethod.valueOf(method)) {
			case HttpMethod.GET:
				response = rest.get(url) {
				}
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
			default:
				throw new Exception("Unidentified HTTP Method $method. Please use one of the following ${HttpMethod.values().collect { it.name() }.join(", ")}.")
		}

		return response
	}

	String generateExecStatement(String requestPath, String method){
		String field = requestPath.substring(0, requestPath.indexOf("?")).replace("/", "_")

		"""
		val ${field} = exec(http(\"${field}\")
				.${method.toLowerCase()}(s\"\"\"${requestPath}\"\"\".stripMargin)
		)
		"""
	}

}