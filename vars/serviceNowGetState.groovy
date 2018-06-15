def response = serviceNow_UpdateChangeItem serviceNowConfiguration: [instance: 'exampledev'], credentialsId: 'jenkins-vault', serviceNowItem: [sysId: 'adg98y29thukwfd97ihu23'], vaultConfiguration: [url: 'https://vault.example.com:8200', path: 'secret/for/service_now/']
echo response //NEW
