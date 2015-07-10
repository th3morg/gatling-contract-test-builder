package gatling.check.builder

import grails.converters.JSON

class GatlingController {
    CodeGenerationService codeGenerationService

    def index() {
        redirect(action: "generate")
    }

    def generate() {
			def map = [:]

			if(params.baseUrl){
				 def resp = codeGenerationService.getHTTPResponseBody(params.baseUrl + params.requestPath,params.method, params.body)
				 map.responseBody = new JSON(JSON.parse((String)(resp.body)))
				 map.exec = codeGenerationService.generateExecStatement(params.requestPath, params.method)
					map.requestStats = resp.status
			}


			map.baseUrl = params.baseUrl ?: "http://elb.yonder.it:8080"
			map.requestPath = params.requestPath ?: "/yonder-web/api/3f1bde0e-1838-45cd-8b9a-801ca99cef99/accounts/info?userId=330190"
			return map
		}
}
