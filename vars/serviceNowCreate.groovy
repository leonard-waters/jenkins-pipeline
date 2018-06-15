#!/usr/bin/groovy
import com.activedisclosure.*

def call(body) {
  def response = serviceNow_createChange 
  serviceNowConfiguration: [
    instance: 'https://dfsco.service-now.com',
    producerId: 'TBD'
  ],
  credentialsId: 'service_now_creds'

  def createResponse = new JsonSlurper().parseText(response.content)
  def sysId = createResponse.result.sys_id
  def changeNumber = createResponse.result.number  
}
