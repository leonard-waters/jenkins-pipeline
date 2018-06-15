def response = serviceNow_getCTask serviceNowConfiguration: [instance: 'exampledev'], credentialsId: 'jenkins-vault', serviceNowItem: [sysId: 'agsdh0wehosid9723h30h', ctask: 'UAT_TESTING'], vaultConfiguration: [url: 'https://vault.example.com:8200', path: 'secret/for/service_now/']
def ctaskSysId = createResponse.result[0].sys_id
def ctaskNumber = createResponse.result[0].number
