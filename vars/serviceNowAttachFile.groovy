#!/usr/bin/groovy
import com.activedisclosure.*

def call(body) {
  
}

def myFile = readFile file: 'my-test-file.txt'

serviceNow_attachFile serviceNowConfiguration: [
  instance: 'exampledev'],
  credentialsId: 'jenkins-vault',
  serviceNowItem: [
    sysId: 'agsdh0wehosid9723h30h',
    body: myFile
  ],
  vaultConfiguration: [
    url: 'https://vault.example.com:8200',
    path: 'secret/for/service_now/'
  ]
