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
				def paramVariables = getVariables()
				 def resp = codeGenerationService.getHTTPResponseBody(constructUrl(params.baseUrl + params.requestPath, paramVariables), params.method, params.requestBody)
				 map.responseBody = new JSON(JSON.parse((String)(resp.body)))
				 map.exec = codeGenerationService.generateExecStatement(params.requestPath,paramVariables, params.method, params.requestBody, map.responseBody)
				 map.responseStatus = resp.status
			}

			map.requestBody = params.requestBody
			map.baseUrl = params.baseUrl
			map.requestPath = params.requestPath
			return map
		}

	protected String constructUrl(String url, Map variables){
		variables.each{
			url = url.replace(it.key as String, it.value as String)
		}
		return url
	}

	protected Map getVariables(){
		Map map = [:]
		if(params.paramValue1){
			map.put(params.paramValue1, params.paramVariable1)
		}
		if(params.paramValue2){
			map.put(params.paramValue2, params.paramVariable2)
		}
		if(params.paramValue3){
			map.put(params.paramValue3, params.paramVariable3)
		}
		map
	}
}
