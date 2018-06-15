def messageJson = new JSONObject()
messageJson.putAll([
                short_description: 'My change',
                description: 'My longer description of the change'
        ])
def response = serviceNow_UpdateChangeItem serviceNowConfiguration: [instance: 'exampledev'], credentialsId: 'jenkins-vault', serviceNowItem: [table: 'change_request', sysId: 'adg98y29thukwfd97ihu23', body: messageJson.toString()], vaultConfiguration: [url: 'https://vault.example.com:8200', path: 'secret/for/service_now/']
